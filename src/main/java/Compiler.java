/*
 * BSD 3-Clause License
 *
 * Copyright (c) 2023-2024, Gaomengkai
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import icu.merky.jrabche.fe.Preprocessor;
import icu.merky.jrabche.fe.parser.SylangLexer;
import icu.merky.jrabche.fe.parser.SylangParser;
import icu.merky.jrabche.fe.visitor.SylangVisitorImpl;
import icu.merky.jrabche.llvmir.IRBuilder;
import icu.merky.jrabche.llvmir.IRBuilderImpl;
import icu.merky.jrabche.llvmir.structures.impl.IRModuleImpl;
import icu.merky.jrabche.logger.JrabcheLogger;
import icu.merky.jrabche.opt.llvmir.OptExecutor;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class Compiler {

    static class Args {
        public boolean emitLLVM;
        public boolean emitASM;
        public boolean enableO1;
        public boolean verbose;
        public File outputFile;
        public File inputFile;


        public Args() {
        }

        public boolean isEmitLLVM() {
            return emitLLVM;
        }

        public void setEmitLLVM(boolean emitLLVM) {
            this.emitLLVM = emitLLVM;
        }

        public boolean isEmitASM() {
            return emitASM;
        }

        public void setEmitASM(boolean emitASM) {
            this.emitASM = emitASM;
        }

        public boolean isEnableO1() {
            return enableO1;
        }

        public void setEnableO1(boolean enableO1) {
            this.enableO1 = enableO1;
        }

        public File getOutputFile() {
            return outputFile;
        }

        public void setOutputFile(File outputFile) {
            this.outputFile = outputFile;
        }

        public boolean isVerbose() {
            return verbose;
        }

        public void setVerbose(boolean verbose) {
            this.verbose = verbose;
        }

        public void isValid() {
            if (emitASM && emitLLVM) {
                throw new RuntimeException("Sorry, I have trouble with emitting assembly " +
                        "and LLVM IR at the same time.");
            }
            if (!(emitASM || emitLLVM)) {
                // hadn't specified the emitting method. Default emit asm.
                this.emitASM = true;
            }
            if (inputFile == null || !inputFile.isFile()) {
                throw new RuntimeException("Uhh, the input file you've given to me is invalid. " +
                        "Are you sure it is ACTUALLY a normal file?");
            }
            if (outputFile == null) {
                String suffix;
                if (this.emitASM) suffix = ".s";
                else suffix = ".ll";
                outputFile = new File(inputFile.getAbsoluteFile() + suffix);
            }
        }

    }

    static class ArgParser {
        Args args;

        ArgParser(String[] args) {
            Args a = new Args();
            var iter = Arrays.stream(args).iterator();
            if (!iter.hasNext()) {
                usage();
                System.exit(1);
            }
            while (iter.hasNext()) {
                String arg = iter.next();
                switch (arg) {
                    case "-O0":
                        a.setEnableO1(false);
                        break;
                    case "-O1":
                    case "-O2":
                    case "-O3":
                    case "-Ofast":
                        a.setEnableO1(true);
                        break;
                    case "-o":
                        if (!iter.hasNext()) {
                            throw new RuntimeException("Invalid output filename.");
                        }
                        a.setOutputFile(new File(iter.next()));
                        break;
                    case "-S":
                        a.setEmitASM(true);
                        break;
                    case "-emit-llvm":
                        a.setEmitLLVM(true);
                        break;
                    case "-d":
                    case "--debug":
                    case "--verbose":
                        a.setVerbose(true);
                        break;
                    case "-h":
                    case "--help":
                        usage();
                        System.exit(0);
                        break;
                    case "-v":
                    case "--version":
                        version();
                        System.exit(0);
                        break;
                    default:
                        a.inputFile = new File(arg);
                        break;
                }
            }
            this.args = a;
        }

        private void version() {
            System.out.println("""
                    Jrabche Compiler v0.1.0\

                    (c) 2023-2024 Gaomengkai\

                    Licensed under BSD 3-Clause License\

                    Github: github.com/gaomengkai/Jrabche""");
        }

        public Args getArgs() {
            return args;
        }

        void usage() {
            version();
            System.out.println("""
                                        
                                        
                    Usage: java -jar Compiler.jar [-S|-emit-llvm] [input.sy] [-o output] [options]\

                     while options are:\
                    -O0: Disable optimization
                    -O1: Enable optimization
                    -O2: Enable more optimization
                    -O3: Enable even more optimization
                    -Ofast: Enable all optimization
                    -d, --debug, --verbose: Enable debug mode
                    -h, --help: Show this help message
                    -v, --version: Show version information
                    -o [output]: Specify output file""");
        }
    }

    static class JrabcheCompiler {

        // TEMP FILE RESOLUTION
        private File tmpFile;

        private File getTmpFile() {
            if (tmpFile != null) {
                return tmpFile;
            }
            File temp = new File(System.getProperty("java.io.tmpdir"));
            File tempDir = new File(temp, "testll");
            boolean mkdirsed = tempDir.mkdirs();
            tmpFile = new File(tempDir, "J_tmp03.tmp.ll");
            return tmpFile;
        }

        private void resetTmpFile() {
            if (tmpFile != null && tmpFile.isFile()) {
                // del tmp file
                boolean deleted = tmpFile.delete();
            }
        }

        // COMPILER
        Args compilerArgs;

        String genIR(String source) {
            // --verbose
            if (compilerArgs.isVerbose()) {
                JrabcheLogger.JL.setLevel(JrabcheLogger.LoggerLevel.D);
            } else {
                JrabcheLogger.JL.setLevel(JrabcheLogger.LoggerLevel.W);
            }
            source = Preprocessor.preprocess(source);
            CharStream input = CharStreams.fromString(source);
            Lexer lexer = new SylangLexer(input);
            TokenStream tokens = new CommonTokenStream(lexer);
            SylangParser parser = new SylangParser(tokens);
            IRBuilder builder = new IRBuilderImpl();
            var visitor = new SylangVisitorImpl(builder);
            visitor.visit(parser.compUnit());
            IRModuleImpl module = (IRModuleImpl) builder.getModule();
            // -O1
            if (compilerArgs.isEnableO1()) {
                new OptExecutor(module).run();
            }
            return module.toString();
        }

        void genASM(String ir) throws IOException {
            // to tmp file.
            Utils.writeFile(getTmpFile().getAbsolutePath(), ir);
            // use clang or clang.exe to generate asm file.
            // clang -S ll/$file -o s/$file.s --target=riscv64 -fPIC -mabi=lp64f -fno-addrsig
            ProcessBuilder pb = new ProcessBuilder("clang", "-S", getTmpFile().getAbsolutePath(),
                    "-o", compilerArgs.outputFile.getAbsolutePath(), "--target=riscv64", "-fPIC", "-mabi=lp64f", "-fno-addrsig", "-w");
            pb.inheritIO();
            var clangProcess = pb.start();
            try {
                clangProcess.waitFor();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        public JrabcheCompiler(Args compilerArgs) {
            this.compilerArgs = compilerArgs;
        }

        public void compile() throws IOException {
            char[] inputChars = Utils.readFile(compilerArgs.inputFile.getAbsolutePath());
            String inputStr = new String(inputChars);
            String ir = genIR(inputStr);

            // -emit-llvm
            if (compilerArgs.isEmitLLVM()) {
                Utils.writeFile(compilerArgs.outputFile.getAbsolutePath(), ir);
                return;
            }

            // -S
            if (compilerArgs.isEmitASM()) {
                genASM(ir);
            }
        }
    }


    public static void main(String[] args) throws IOException {
        var parser = new ArgParser(args);
        // arg parser
        var a = parser.getArgs();
        a.isValid();
        var compiler = new JrabcheCompiler(a);
        compiler.compile();
    }
}