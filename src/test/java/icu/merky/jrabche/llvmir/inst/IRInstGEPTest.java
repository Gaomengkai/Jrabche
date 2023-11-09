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

package icu.merky.jrabche.llvmir.inst;

import icu.merky.jrabche.llvmir.types.ArrayType;
import icu.merky.jrabche.llvmir.types.IRType;
import icu.merky.jrabche.llvmir.types.IntType;
import icu.merky.jrabche.llvmir.values.IRVal;
import icu.merky.jrabche.llvmir.values.IRValConstArray;
import icu.merky.jrabche.llvmir.values.IRValConstInt;
import org.junit.jupiter.api.Test;

import java.util.List;

import static icu.merky.jrabche.llvmir.types.PointerType.MakePointer;
import static org.junit.jupiter.api.Assertions.*;

class IRInstGEPTest {
    @Test
    void TestGEPType1() {
        IRType ty = new IntType();
        IRInstAlloca alloca = new IRInstAlloca("alloca", ty);
        IRInstGEP gep = new IRInstGEP(alloca, List.of(IRValConstInt.ZeroInt()));
        assertEquals(MakePointer(ty), gep.getType());
    }
    @Test
    void TestGEPType2() {
        // %2 = alloca [3 x i32]*, align 8
        IRType ty = new ArrayType(3, new IntType());
        IRInstAlloca alloca = new IRInstAlloca("alloca", ty);
        IRInstGEP gep = new IRInstGEP(alloca, List.of(IRValConstInt.ZeroInt()));
        assertEquals(MakePointer(ty), gep.getType());
    }

    @Test
    void TestGEPType3() {
        // %2 = alloca [3 x i32]*, align 8
        // %3 = getelementptr [3 x i32], [3 x i32]* %2, i64 0, i64 1    ;yields i32*
        IRType ty = new ArrayType(3, new IntType());
        IRInstAlloca alloca = new IRInstAlloca("alloca", ty);
        IRInstGEP gep = new IRInstGEP(alloca, List.of(IRValConstInt.ZeroInt(), IRValConstInt.fromInt(1)));
        assertEquals(MakePointer(new IntType()), gep.getType());
    }
    @Test
    void TestGEPType4() {
        // %2 = alloca [3 x i32]*, align 8
        // %3 = load [3 x i32]*, [3 x i32]** %2, align 8
        // %4 = getelementptr inbounds [3 x i32], [3 x i32]* %3, i64 2
        // %5 = getelementptr inbounds [3 x i32], [3 x i32]* %4, i64 0, i64 1
        IRType ty = MakePointer(new ArrayType(3, new IntType())); //[3 x i32]*
        IRInstAlloca alloca = new IRInstAlloca("%2", ty);
        assertEquals(MakePointer(ty), alloca.getType()); // %2 is [3 x i32]**
        IRInstLoad load = new IRInstLoad(alloca);
        IRInstGEP gep = new IRInstGEP(load, List.of(IRValConstInt.fromInt(2)));
        assertEquals(ty, gep.getType()); // %4 is [3 x i32]*
        gep = new IRInstGEP(gep, List.of(IRValConstInt.ZeroInt(), IRValConstInt.fromInt(1)));
        assertEquals(MakePointer(new IntType()), gep.getType()); // %5 is i32*
    }
}