/*
 * BSD 3-Clause License
 *
 * Copyright (c) 2023, Gaomengkai
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

package icu.merky.jrabche;

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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static icu.merky.jrabche.fe.visitor.FETestConfig.*;
import static icu.merky.jrabche.logger.JrabcheLogger.JL;

public class AutoTest {
    private final File sydir = new File(SY_DIR);

    private static void compare(String sourceFilename, int exitValue, File outFileExpected, File tempOutFile) throws IOException {
        // compare
        var endl_pattern = Pattern.compile("\r\n");

        char[] stdout = Utils.readFile(tempOutFile.getAbsolutePath());
        var stdoutStr = new String(stdout);
        if (!stdoutStr.endsWith("\n") && !stdoutStr.isEmpty()) stdoutStr += "\n";
        stdoutStr += exitValue;
        stdoutStr += "\n";
        Matcher stdoutMatcher = endl_pattern.matcher(stdoutStr);
        stdoutStr = stdoutMatcher.replaceAll("\n");
        stdoutStr = stdoutStr.replaceAll("\r", "");

        char[] stdoutExpected = Utils.readFile(outFileExpected.getAbsolutePath());
        var stdoutExpectedStr = new String(stdoutExpected);
        if (!stdoutExpectedStr.endsWith("\n")) stdoutExpectedStr += "\n";
        Matcher stdoutExpectedMatcher = endl_pattern.matcher(stdoutExpectedStr);
        stdoutExpectedStr = stdoutExpectedMatcher.replaceAll("\n");
        stdoutStr = stdoutStr.replaceAll("\r", "");
        Assertions.assertEquals(stdoutExpectedStr, stdoutStr, "Test failed: " + sourceFilename);
    }

    private static void genIR(File syFile, File tempFile, boolean enableOpt) throws IOException {
        // 编译耗时计时
        var compileStart = System.currentTimeMillis();
        // to ir.
        char[] inputChars = Utils.readFile(syFile.getAbsolutePath());
        String inputStr = new String(inputChars);
        inputStr = Preprocessor.preprocess(inputStr);
        CharStream input = CharStreams.fromString(inputStr);
        Lexer lexer = new SylangLexer(input);
        TokenStream tokens = new CommonTokenStream(lexer);
        SylangParser parser = new SylangParser(tokens);
        IRBuilder builder = new IRBuilderImpl();
        var visitor = new SylangVisitorImpl(builder);
        visitor.visit(parser.compUnit());
        IRModuleImpl module = (IRModuleImpl) builder.getModule();
        if (enableOpt) {
            new OptExecutor(module).run();
        }
        String ir = module.toString();
        if (ENABLE_IR_OUTPUT)
            System.out.println(module.toStringNoDecl());

        // write to $temp$/test.ll
        Utils.writeFile(tempFile.getAbsolutePath(), ir);
        JL.InfoF("%s,%dms\n", syFile.getName(), System.currentTimeMillis() - compileStart);
    }

    List<File> getAllSyFile() {
        return new ArrayList<>(List.of(Objects.requireNonNull(sydir.listFiles((dir, name) -> name.endsWith(".sy")))));
    }

    void testRunOne(File syFile, File inFile, File outFileExpected, boolean enableOpt) throws IOException {
        File temp = new File(System.getProperty("java.io.tmpdir"));
        File tempDir = new File(temp, "testll");
        tempDir.mkdirs();
        File tempFile = new File(tempDir, "test.ll");
        File tempOutFile = new File(tempDir, "test.out");

        genIR(syFile, tempFile, enableOpt);

        // use lli to run
        ProcessBuilder pb = new ProcessBuilder(EXE_LLI,
                "--extra-archive=\"" + LIB_GCC + "\"",
                "--extra-archive=\"" + LIB_SY + "\"",
                "--extra-archive=\"" + LIB_MINGWEX + "\"",
                tempFile.getAbsolutePath());
        pb.redirectErrorStream(true);
        if (inFile != null) pb.redirectInput(ProcessBuilder.Redirect.from(inFile));
        pb.redirectOutput(ProcessBuilder.Redirect.to(tempOutFile));

        Process p = pb.start();
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int exitValue = p.exitValue();
        exitValue &= 0xff;


        // compare
        compare(syFile.getName(), exitValue, outFileExpected, tempOutFile);
    }

    void testRunOneOnEXE(File syFile, File inFile, File outFileExpected, boolean enableOpt) throws IOException {
        File temp = new File(System.getProperty("java.io.tmpdir"));
        File tempDir = new File(temp, "testll");
        tempDir.mkdirs();
        File tempFile = new File(tempDir, "test.ll");
        File tempOutFile = new File(tempDir, "test.out");
        File tempExeFile = new File(tempDir, "test.exe");
        // to ir.
        genIR(syFile, tempFile, enableOpt);

        // use clang to compile
        ProcessBuilder pb = new ProcessBuilder(EXE_CLANG, tempFile.getAbsolutePath(), LIB_SY, "-o", tempExeFile.getAbsolutePath());
        pb.redirectErrorStream(true);
        var clangProcess = pb.start();
        try {
            clangProcess.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        pb = new ProcessBuilder(tempExeFile.getAbsolutePath());
        if (inFile != null) pb.redirectInput(ProcessBuilder.Redirect.from(inFile));
        pb.redirectOutput(ProcessBuilder.Redirect.to(tempOutFile));

        Process p = pb.start();
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int exitValue = p.exitValue();
        exitValue &= 0xff;


        compare(syFile.getName(), exitValue, outFileExpected, tempOutFile);
    }

    void testSpec(int no, boolean useEXE, boolean enableOpt) {
        // 计时
        var start = System.currentTimeMillis();
        String noPattern;
        if (no < 100) {
            noPattern = "%02d_";
        } else if (no < 1000) {
            noPattern = "%03d";
        } else if (no < 10000) {
            noPattern = "%04d";
        } else {
            noPattern = "%d";
        }
        JL.InfoF("Testing %s\n", String.format(noPattern, no));
        // search %2d*.sy
        var syFiles = getAllSyFile();
        var syFile = syFiles.stream().filter(f -> f.getName().startsWith(String.format(noPattern, no))).findFirst().orElse(null);
        Assertions.assertNotNull(syFile);
        // search %2d*.in
        String inFileName = syFile.getName().replace(".sy", ".in");
        String outFileName = syFile.getName().replace(".sy", ".out");
        var inFile = new File(syFile.getParentFile(), inFileName);
        if (!inFile.exists()) inFile = null;
        var outFileExpected = new File(syFile.getParentFile(), outFileName);
        Assertions.assertTrue(outFileExpected.exists());
        try {
            if (useEXE) testRunOneOnEXE(syFile, inFile, outFileExpected, enableOpt);
            else testRunOne(syFile, inFile, outFileExpected, enableOpt);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            JL.InfoF("Cost %dms\n", System.currentTimeMillis() - start);
        }
    }

    @Test
    void testNo0_25() {
        JL.setLevel(JrabcheLogger.LoggerLevel.I);
        ENABLE_IR_OUTPUT = false;
        for (int i = 0; i <= 25; i++) {
            testSpec(i, false, ENABLE_IR_OPT);
        }
    }

    @Test
    void testNo26_50() {
        JL.setLevel(JrabcheLogger.LoggerLevel.I);
        ENABLE_IR_OUTPUT = false;
        for (int i = 26; i <= 50; i++) {
            testSpec(i, false, ENABLE_IR_OPT);
        }
    }

    @Test
    void testNo51_75() {
        JL.setLevel(JrabcheLogger.LoggerLevel.I);
        ENABLE_IR_OUTPUT = false;
        for (int i = 51; i <= 75; i++) {
            testSpec(i, false, ENABLE_IR_OPT);
        }
    }

    @Test
    void testNo76_99() {
        JL.setLevel(JrabcheLogger.LoggerLevel.I);
        ENABLE_IR_OUTPUT = false;
        for (int i = 76; i <= 99; i++) {
            if (i == 95) testSpec(i, true, ENABLE_IR_OPT);
            else testSpec(i, false, ENABLE_IR_OPT);
        }
    }


    void testAll() {
        ENABLE_IR_OUTPUT = false;
        testNo0_25();
        testNo26_50();
        testNo51_75();
        testNo76_99();
    }


    @Test
    void testOne() {
        ENABLE_IR_OUTPUT = true;
        JL.setLevel(JrabcheLogger.LoggerLevel.D);
        testSpec(21, true, ENABLE_IR_OPT);
    }
}
