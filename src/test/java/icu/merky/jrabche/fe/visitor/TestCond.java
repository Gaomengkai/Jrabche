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

import org.junit.jupiter.api.Test;

import static icu.merky.jrabche.fe.visitor.SylangVisitorImplTest.getVisitorContext;
import static icu.merky.jrabche.fe.visitor.Helper.LLCCompileTest;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestCond {
    @Test
    void AAndB() throws NoSuchFieldException, IllegalAccessException {
        String program = """
                int AAndB(int a1, int a2) {
                if(a1==0&&a2==0) return 1;
                return 0;
                }""";
        VisitorContext C = getVisitorContext(program);
        String funcString = C.builder.curFunc().toString();
        System.out.println(funcString);

        assertTrue(LLCCompileTest(funcString));
    }

    @Test
    void AOrB() throws NoSuchFieldException, IllegalAccessException {
        String program = """
                int AOrB(int a, int b) {
                if(a==0||b==1) return 1;
                return 0;
                }""";
        VisitorContext C = getVisitorContext(program);
        String funcString = C.builder.curFunc().toString();
        System.out.println(funcString);

        assertTrue(LLCCompileTest(funcString));
    }
    @Test
    void ComplexIfWhileOrAnd1() throws NoSuchFieldException, IllegalAccessException {
        String funcName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String program = """
                int %s(int a, int b) {
                if(a==0||b==1) return 1;
                else if(a==1&&b==0) return 2;
                else if(a==1||b==1) return 3;
                else if(a==0&&b==0.0) return 4;
                else return 5;
                }
                """.formatted(funcName);
        VisitorContext C = getVisitorContext(program);
        String funcString = C.builder.curFunc().toString();
        System.out.println(funcString);

        assertTrue(LLCCompileTest(funcString));
    }
}
