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
import icu.merky.jrabche.llvmir.structures.IRModule;
import icu.merky.jrabche.llvmir.types.FunctionType;
import icu.merky.jrabche.llvmir.types.IRBasicType;
import org.junit.jupiter.api.Test;

import static icu.merky.jrabche.fe.visitor.Helper.LLCCompileTest;
import static icu.merky.jrabche.fe.visitor.SylangVisitorImplTest.getVisitorContext;
import static org.junit.jupiter.api.Assertions.*;

public class TestGlobal {
    @Test
    void TestGlobalRead() throws NoSuchFieldException, IllegalAccessException {
        String funcName = Thread.currentThread().getStackTrace()[1].getMethodName();
        String program = """
                const int k=0;
                int %s(int a1) {
                int b[10];
                if(k==1) {return 1;}
                return 0;
                }
                                
                int main() {
                int arr[10] = {1};
                int aa=getint();
                int bb=getint();
                putint(aa+bb);
                putch(10);
                putint(aa+bb+arr[0]);
                return 1;
                }""".formatted(funcName);
        IRBuilder builder = new IRBuilderImpl();
        VisitorContext C = getVisitorContext(program, builder);
        IRModule module = builder.getModule();
        String moduleStr = module.toString();
        String funcString = C.builder.curFunc().toString();
        System.out.println(moduleStr);

        // assertTrue(LLCCompileTest(funcString));
        assertTrue(LLCCompileTest(moduleStr));

        FunctionType functionType = C.queryFunctionType(funcName);
        assertNotNull(functionType);
        assertEquals(IRBasicType.INT, functionType.getRetType().getBasicType());
        assertEquals(1, functionType.getParamsType().size());
        assertEquals(IRBasicType.INT, functionType.getParamsType().get(0).getBasicType());
    }
}
