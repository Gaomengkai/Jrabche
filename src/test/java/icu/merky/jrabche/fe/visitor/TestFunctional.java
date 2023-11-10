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

public class TestFunctional {
    @Test
    public void F09() throws NoSuchFieldException, IllegalAccessException {
        String funcName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String program = """
                int a;
                int func(int p){
                	p = p - 1;
                	return p;
                }
                int main(){
                	int b;
                	a = 10;
                	b = func(a);
                	return b;
                }

                                """;
        IRBuilder builder = new IRBuilderImpl();
        VisitorContext C = getVisitorContext(program, builder);
        String moduleString = builder.getModule().toString();
        // System.out.println(moduleString);
        assertTrue(Helper.LLCCompileTest(moduleString));
        assertTrue(Helper.LLIRunLastLL(9, ""));
    }

    @Test
    public void F21() throws NoSuchFieldException, IllegalAccessException {
        String funcName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String program = """
                // test if-else-if
                int ifElseIf() {
                  int a;
                  a = 5;
                  int b;
                  b = 10;
                  if(a == 6 || b == 0xb) {
                    return a;
                  }
                  else {
                    if (b == 10 && a == 1)
                      a = 25;
                    else if (b == 10 && a == -5)
                      a = a + 15;
                    else
                      a = -+a;
                  }

                  return a;
                }

                int main(){
                  putint(ifElseIf());
                  return 0;
                }
                                """;
        IRBuilder builder = new IRBuilderImpl();
        VisitorContext C = getVisitorContext(program, builder);
        String moduleString = builder.getModule().toString();
        System.out.println(moduleString);
        assertTrue(Helper.LLCCompileTest(moduleString));
        assertTrue(Helper.LLIRunLastLL(0, "-5"));
    }

    @Test
    public void F64() throws NoSuchFieldException, IllegalAccessException {
        String funcName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String program = """
                int ints[10000];
                int intt;
                int chas[10000];
                int chat;
                int i=0, ii=1;
                int c;
                int get[10000];
                int get2[10000];

                int isdigit(int x) {
                    if (x >= 48 && x <= 57)
                        return 1;
                    return 0;
                }

                int power(int b, int a) {
                    int result = 1;
                    while (a != 0) {
                        result = result * b;
                        a = a - 1;
                    }
                    return result;
                }

                int getstr(int get[]) {
                    int x = getch();
                    int length = 0;
                    while (x != 13 && x != 10) {
                        get[length] = x;
                        length = length + 1;
                        x = getch();
                    }
                    return length;
                }

                void intpush(int x)
                {
                    intt = intt + 1;
                    ints[intt] = x;
                }
                void chapush(int x)
                {
                    chat = chat + 1;
                    chas[chat] = x;
                }
                int intpop()
                {
                    intt = intt - 1;
                    return ints[intt + 1];
                }
                int chapop()
                {
                    chat = chat - 1;
                    return chas[chat + 1];
                }
                void intadd(int x)
                {
                    ints[intt] = ints[intt] * 10;
                    ints[intt] = ints[intt] + x;
                }

                int find()
                {
                    c = chapop();
                    get2[ii] = 32;
                    get2[ii + 1] = c;
                    ii = ii + 2;
                    if (chat == 0) return 0;
                    return 1;
                }

                int main()
                {
                    intt=0;
                    chat=0;
                    int lengets = getstr(get);
                    while (i < lengets)
                    {
                        if (isdigit(get[i]) == 1)
                        {
                            get2[ii] = get[i];
                            ii = ii + 1;
                        }
                        else
                        {
                            if(get[i] == 40) chapush(40);
                            if(get[i] == 94) chapush(94);
                            if(get[i] == 41)
                            {
                                c = chapop();
                                while (c != 40)
                                {
                                    get2[ii] = 32;
                                    get2[ii + 1]=c;
                                    ii = ii + 2;
                                    c = chapop();
                                }
                            }
                            if (get[i] == 43)
                            {
                                while (chas[chat] == 43 || chas[chat] == 45 || chas[chat] == 42 || chas[chat] == 47 || chas[chat] == 37 || chas[chat] == 94)
                                {
                                    if (find()==0)break;
                                }
                                chapush(43);
                            }
                            if (get[i] == 45)
                            {
                                while (chas[chat] == 43 || chas[chat] == 45 ||chas[chat] == 42 || chas[chat] == 47 || chas[chat] == 37 || chas[chat] == 94)
                                {
                                    if(find()==0)break;
                                }
                                chapush(45);
                            }
                            if(get[i] == 42)
                            {
                                while (chas[chat] == 42 || chas[chat] == 47 ||chas[chat] == 37 || chas[chat] == 94)
                                {
                                    if (find()==0)break;
                                }
                                chapush(42);
                            }
                            if (get[i] == 47)
                            {
                                while (chas[chat] == 42 || chas[chat] == 47 || chas[chat] == 37 || chas[chat] == 94)
                                {
                                    if (find()==0)break;
                                }
                                chapush(47);
                            }
                            if (get[i] == 37)
                            {
                                while (chas[chat] == 42 || chas[chat] == 47 || chas[chat] == 37 || chas[chat] == 94)
                                {
                                    if (find()==0)break;
                                }
                                chapush(37);
                            }
                            get2[ii] = 32;
                            ii = ii + 1;
                        }
                        i = i + 1;
                    }
                    while(chat > 0)
                    {
                        int c = chapop();
                        get2[ii] = 32;
                        get2[ii + 1]=c;
                        ii = ii + 2;
                    }
                    get2[ii]= 64;
                    i = 1;
                    while (get2[i] != 64)
                    {
                        if (get2[i] == 43 || get2[i] == 45 || get2[i] == 42 || get2[i] == 47 || get2[i] == 37 || get2[i] == 94)
                        {
                            int a=intpop();int b=intpop();int c;
                            if (get2[i] == 43) c = a + b;
                            if (get2[i] == 45) c = b - a;
                            if (get2[i] == 42) c = a * b;
                            if (get2[i] == 47) c = b / a;
                            if (get2[i] == 37) c = b % a;
                            if (get2[i] == 94) c = power(b,a);
                            intpush(c);
                        }
                        else
                        {
                            if(get2[i] != 32)
                            {
                                intpush(get2[i] - 48);
                                ii=1;
                                while(get2[i+ii] != 32)
                                {
                                    intadd(get2[i+ii] - 48);
                                    ii = ii + 1;
                                }
                                i = i + ii-1;
                            }
                        }
                        i = i + 1;
                    }
                    putint(ints[1]);
                    return 0;
                }
                """;
        IRBuilder builder = new IRBuilderImpl();
        VisitorContext C = getVisitorContext(program, builder);
        String moduleString = builder.getModule().toString();
        System.out.println(moduleString);
        assertTrue(Helper.LLCCompileTest(moduleString));
        assertTrue(Helper.LLIRunLastLL(0, "2", "(4 - (3 - 5) * 2 + 100) % (2^3 - 1) / 2 + 1\n"));
    }
}
