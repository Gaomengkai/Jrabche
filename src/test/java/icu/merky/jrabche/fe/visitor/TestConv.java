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

import icu.merky.jrabche.llvmir.types.FunctionType;
import icu.merky.jrabche.llvmir.types.IRBasicType;
import org.junit.jupiter.api.Test;

import static icu.merky.jrabche.fe.visitor.SylangVisitorImplTest.getVisitorContext;
import static org.junit.jupiter.api.Assertions.*;

public class TestConv {
    @Test
    void FloatCmp() throws NoSuchFieldException, IllegalAccessException {
        String program = """
                int floatcmp1(float a1, float a2) {
                if(a1<a2)return -1;
                else if(a1>a2) return 1;
                else return 0;
                }""";
        VisitorContext C = getVisitorContext(program);
        String funcString = C.builder.curFunc().toString();
        System.out.println(funcString);
        assertTrue(Helper.LLCCompileTest(funcString));
        ;

        assertNotNull(C.queryFunctionType("floatcmp1"));
    }

    @Test
    void FloatCmp2() throws NoSuchFieldException, IllegalAccessException {
        String program = """
                int floatcmp2(int a1, float a2) {
                if(a1<a2)return -1;
                else if(a1>a2) return 1;
                else return 0;
                }""";
        VisitorContext C = getVisitorContext(program);
        String funcString = C.builder.curFunc().toString();
        System.out.println(funcString);
        // compile test
        assertTrue(Helper.LLCCompileTest(funcString));

        assertNotNull(C.queryFunctionType("floatcmp2"));
    }

    @Test
    void IntCmp1() throws NoSuchFieldException, IllegalAccessException {
        String program = """
                int intcmp1(int a1, int a2) {
                if(a1<a2)return -1;
                else if(a1>a2) return 1;
                else return 0;
                }""";
        VisitorContext C = getVisitorContext(program);
        String funcString = C.builder.curFunc().toString();
        System.out.println(funcString);
        // compile test
        assertTrue(Helper.LLCCompileTest(funcString));

        assertNotNull(C.queryFunctionType("intcmp1"));
    }

    @Test
    void IntCmp2() throws NoSuchFieldException, IllegalAccessException {
        String program = """
                int intcmp2(float a1, int a2) {
                if(a1<a2)return -1;
                else if(a1>a2) return 1;
                else return 0;
                }""";
        VisitorContext C = getVisitorContext(program);
        String funcString = C.builder.curFunc().toString();
        System.out.println(funcString);
        // compile test
        assertTrue(Helper.LLCCompileTest(funcString));

        assertNotNull(C.queryFunctionType("intcmp2"));
    }

    @Test
    void I1toI32() throws NoSuchFieldException, IllegalAccessException {
        String program = """
                int i1toi32(int a1) {
                if(a1<2==1) {
                return 1;}
                return 0;
                }""";
        VisitorContext C = getVisitorContext(program);
        String funcString = C.builder.curFunc().toString();
        System.out.println(funcString);
        // compile test
        assertTrue(Helper.LLCCompileTest(funcString));

        assertNotNull(C.queryFunctionType("i1toi32"));
    }

    @Test
    void I1toI32_2() throws NoSuchFieldException, IllegalAccessException {
        String program = """
                int i1toi32_2(int a1) {
                if(!a1==1) {
                return 1;}
                return 0;
                }""";
        VisitorContext C = getVisitorContext(program);
        String funcString = C.builder.curFunc().toString();
        System.out.println(funcString);

        assertTrue(Helper.LLCCompileTest(funcString));

        FunctionType functionType = C.queryFunctionType("i1toi32_2");
        assertNotNull(functionType);
        assertEquals(IRBasicType.INT, functionType.getRetType().getBasicType());
        assertEquals(1, functionType.getParamsType().size());
        assertEquals(IRBasicType.INT, functionType.getParamsType().get(0).getBasicType());
    }

    @Test
    void ConstCmp1() throws NoSuchFieldException, IllegalAccessException {
        String program = """
                int ConstCmp1(int a1) {
                if(1+1==3) {
                return 1;}
                return 0;
                }""";
        VisitorContext C = getVisitorContext(program);
        String funcString = C.builder.curFunc().toString();
        System.out.println(funcString);

        assertTrue(Helper.LLCCompileTest(funcString));
    }

}
