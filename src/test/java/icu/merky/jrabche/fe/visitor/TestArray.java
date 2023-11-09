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

import icu.merky.jrabche.llvmir.IRBuilder;
import icu.merky.jrabche.llvmir.IRBuilderImpl;
import org.junit.jupiter.api.Test;

import static icu.merky.jrabche.fe.visitor.SylangVisitorImplTest.getVisitorContext;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestArray {
    @Test
    public void TestLocalArray() throws NoSuchFieldException, IllegalAccessException {
        String funcName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String program = """
                int %s() {
                    int a[10]={1};
                    return 0;
                }
                """.formatted(funcName);
        IRBuilder builder = new IRBuilderImpl();
        VisitorContext C = getVisitorContext(program,builder);
        String moduleString = builder.getModule().toString();
        System.out.println(moduleString);
        assertTrue(Helper.LLCCompileTest(moduleString));
    }

    @Test
    public void TestLocalArray2() throws NoSuchFieldException, IllegalAccessException {
        String funcName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String program = """
                int %s() {
                    int a[10][4]={1,2,3,{2,3,4},{},{5}};
                    return 0;
                }
                """.formatted(funcName);
        IRBuilder builder = new IRBuilderImpl();
        VisitorContext C = getVisitorContext(program,builder);
        String moduleString = builder.getModule().toString();
        System.out.println(moduleString);
        assertTrue(Helper.LLCCompileTest(moduleString));
    }

    @Test
    public void TestLocalArray3() throws NoSuchFieldException, IllegalAccessException {
        String funcName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String program = """
                int dd[2][3];
                int ddd[3][3][3];
                int t(int a[]) {
                    return a[2];
                }
                int t2(int a[][3]) {
                    t2(dd);
                    t2(ddd[1]);
                    return a[2][1];
                }
                int t3(int a[][3][3]) {
                    int b[3][3][3];
                    t3(b);
                    t3(a);
                    t3(ddd);
                    t2(a[1]);
                    t(b[1][1]);
                    return a[2][1][2]+dd[1][1];
                }
                """;
        IRBuilder builder = new IRBuilderImpl();
        VisitorContext C = getVisitorContext(program,builder);
        String moduleString = builder.getModule().toString();
        System.out.println(moduleString);
        assertTrue(Helper.LLCCompileTest(moduleString));
    }
    @Test
    public void TestGlobalArray1() throws NoSuchFieldException, IllegalAccessException {
        String funcName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String program = """
                int a[10][4]={1,2,3,4,{6,6,6},{},{5}};
                const int c[10][3]={1,2,3,4,5,6,7,8,9};
                int %s() {
                    int b=a[1][2]; // b=6
                    a[1][3]=b;
                    putint(a[1][3]); // =6
                    putch(10);
                    a[2][1]=c[1][2];
                    putint(a[3][0]);
                    return b+c[1][1];
                }
                int main(){return %s();}
                """.formatted(funcName,funcName);
        IRBuilder builder = new IRBuilderImpl();
        VisitorContext C = getVisitorContext(program,builder);
        String moduleString = builder.getModule().toString();
        System.out.println(moduleString);
        assertTrue(Helper.LLCCompileTest(moduleString));
        int retCodeShouldBe = 6+5;
        String outputShouldBe = "%d\n%d".formatted(6,5);
        Helper.LLIRunLastLL(retCodeShouldBe,outputShouldBe);
    }
    @Test
    public void TestGlobalArray2() throws NoSuchFieldException, IllegalAccessException {
        String funcName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String program = """
                int a[10][4]={};
                int %s() {
                    int b=a[1][2];
                    a[1][3]=b;
                    return 0;
                }
                """.formatted(funcName);
        IRBuilder builder = new IRBuilderImpl();
        VisitorContext C = getVisitorContext(program,builder);
        String moduleString = builder.getModule().toString();
        // System.out.println(moduleString);
        assertTrue(Helper.LLCCompileTest(moduleString));
    }
    @Test
    public void TestGlobalConstArray1() throws NoSuchFieldException, IllegalAccessException {
        String funcName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String program = """
                const int a[10][4]={1,2,3,{2,3,4},{},{5}};
                int %s() {
                    int b=a[1][2];
                    return 0;
                }
                """.formatted(funcName);
        IRBuilder builder = new IRBuilderImpl();
        VisitorContext C = getVisitorContext(program,builder);
        String moduleString = builder.getModule().toString();
        System.out.println(moduleString);
        assertTrue(Helper.LLCCompileTest(moduleString));
    }

    @Test
    public void TestGlobalConstArray2() throws NoSuchFieldException, IllegalAccessException {
        String funcName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String program = """
                const int a[10][4]={};
                int %s() {
                    int b=a[1][2];
                    putint(b);
                    return b;
                }
                int main(){
                return %s();
                }
                """.formatted(funcName,funcName);
        IRBuilder builder = new IRBuilderImpl();
        VisitorContext C = getVisitorContext(program,builder);
        String moduleString = builder.getModule().toString();
        // System.out.println(moduleString);
        assertTrue(Helper.LLCCompileTest(moduleString));
        assertTrue(Helper.LLIRunLastLL(0,"0"));
    }

    @Test
    public void TestGlobalConstArray3() throws NoSuchFieldException, IllegalAccessException {
        String funcName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String program = """
int main(){
    const int a[4][2] = {{1, 2}, {3, 4}, {}, 7};
    const int N = 3;
    int b[4][2] = {};
    int c[4][2] = {1, 2, 3, 4, 5, 6, 7, 8};
    int d[N + 1][2] = {1, 2, {3}, {5}, a[3][0], 8};
    int e[4][2][1] = {{d[2][1], {c[2][1]}}, {3, 4}, {5, 6}, {7, 8}};
    return e[3][1][0] + e[0][0][0] + e[0][1][0] + d[3][0];
}

                """;
        IRBuilder builder = new IRBuilderImpl();
        VisitorContext C = getVisitorContext(program,builder);
        String moduleString = builder.getModule().toString();
        // System.out.println(moduleString);
        assertTrue(Helper.LLCCompileTest(moduleString));
        assertTrue(Helper.LLIRunLastLL(21,""));
    }
}
