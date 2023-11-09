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

package icu.merky.jrabche.fe.visitor;

import org.junit.jupiter.api.Assertions;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class Helper {
    static boolean LLCCompileTest(String funcString) {
        // find a temp dir
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File tempFile = new File(tempDir, "test.ll");
        // System.out.println(tempFile.getAbsolutePath());
        // write to temp file
        try {
            java.nio.file.Files.writeString(tempFile.toPath(), funcString);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return false;
        }
        // compile
        try {
            // save p's stdout,stderr to a str
            Process p = Runtime.getRuntime().exec("llc -O0 " + tempFile.getAbsolutePath()
                    + " -o " + tempDir.getAbsolutePath() + "/test.s"
            );
            p.waitFor();
            if (p.exitValue() == 0) return true;
            System.err.println("llc message:");
            var reader = new BufferedReader(
                    (new InputStreamReader(p.getErrorStream())));
            reader.lines().forEach(System.err::println);
            return false;
        } catch (java.io.IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    static boolean LLIRunLastLL(int retCodeExpected, String stdoutExpected, String input) {
        /*
         * lli --extra-archive="D:\SDK\mingw64\lib\gcc\x86_64-w64-mingw32\13.1.0\libgcc.a" --extra-archive="D:\Code\3\sylib\cmake-build-debug-gcc13\libsy.a" --extra-archive="D:\SDK\mingw64\x86_64-w64-mingw32\lib\libmingwex.a" test.ll
         * */
        try {
            // save p's stdout,stderr to a str
            String cmd =
                    "lli --extra-archive=\"D:\\SDK\\mingw64\\lib\\gcc\\x86_64-w64-mingw32\\13.1.0\\libgcc.a\" --extra-archive=\"D:\\Code\\3\\sylib\\cmake-build-debug-gcc13\\libsy.a\" --extra-archive=\"D:\\SDK\\mingw64\\x86_64-w64-mingw32\\lib\\libmingwex.a\" ";
            File lastll = new File(System.getProperty("java.io.tmpdir"), "test.ll");
            cmd += lastll.getAbsolutePath();
            Process p = Runtime.getRuntime().exec(cmd);
            if (input != null) {
                p.getOutputStream().write(input.getBytes());
                p.getOutputStream().flush();
            }
            p.waitFor();

            // ret code
            Assertions.assertEquals(retCodeExpected, p.exitValue());

            var stderrReader = new BufferedReader(
                    (new InputStreamReader(p.getErrorStream())));
            stderrReader.lines().forEach(System.err::println);

            var stdoutReader = new BufferedReader(
                    (new InputStreamReader(p.getInputStream())));
            char[] stdoutBuf = new char[1024];
            StringBuilder stdout = new StringBuilder();
            int i;
            while ((i = stdoutReader.read(stdoutBuf)) != -1) {
                stdout.append(stdoutBuf, 0, i);
            }

            // stdout
            String[] outList = stdout.toString().split("\n|\r\n");
            String[] outListExpected = stdoutExpected.split("\n|\r\n");
            Assertions.assertEquals(outListExpected.length, outList.length);
            for (int j = 0; j < outList.length; j++) {
                Assertions.assertEquals(outListExpected[j], outList[j],"At Line %d.".formatted(j));
            }
            return true;
        } catch (java.io.IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    static boolean LLIRunLastLL(int retCodeExpected, String stdoutExpected) {
        return LLIRunLastLL(retCodeExpected, stdoutExpected, null);
    }
}
