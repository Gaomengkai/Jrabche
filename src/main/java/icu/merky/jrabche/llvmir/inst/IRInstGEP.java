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
import icu.merky.jrabche.llvmir.types.InvalidType;
import icu.merky.jrabche.llvmir.types.PointerType;
import icu.merky.jrabche.llvmir.values.IRVal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static icu.merky.jrabche.llvmir.types.PointerType.MakePointer;

public class IRInstGEP extends IRInst {
    // GetElementPtrInst
    private IRVal ptr;
    private final List<IRVal> indices;
    private final PointerType pointerType;

    public IRInstGEP(IRVal ptr, List<IRVal> indices) {
        super(InstID.GetElementPtrInst, new InvalidType());
        this.ptr = ptr;
        this.indices = indices;
        assert ptr.getType() instanceof PointerType;
        this.pointerType = (PointerType) ptr.getType();
        this.calculateType();
    }

    private void calculateType() {
        // %3 = load i32*, i32** %2, align 8
        // %4 = getelementptr i32, i32* %3, i64 2
        // in this case, %3 is i32*, %4 is also i32*

        //%3 = load [3 x i32]*, [3 x i32]** %2, align 8
        //%4 = getelementptr [3 x i32], [3 x i32]* %3, i64 2
        //%5 = getelementptr [3 x i32], [3 x i32]* %4, i64 0, i64 1
        // in this case, %3 is [3 x i32]*, %4 is also [3 x i32]*, %5 is i32*
        PointerType lastType = pointerType;
        if (indices.size() == 1) {
            this.type = lastType;
            return;
        }
        for (int i = 1; i < indices.size(); i++) {
            assert lastType != null;
            lastType = MakePointer(
                    ((ArrayType)
                            lastType.getElementType()).getElementType());
        }
        this.type = lastType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (name != null) {
            sb.append(name).append(" = ");
        }
        sb.append("getelementptr ")
                .append(pointerType.getElementType().toString())
                .append(", ")
                .append(pointerType.toString())
                .append(" ")
                .append(ptr.asValue());
        for (IRVal index : indices) {
            sb.append(", ")
                    .append(index.getType().toString())
                    .append(" ")
                    .append(index.asValue());
        }
        return sb.toString();
    }

    @Override
    public boolean replace(IRVal oldVal, IRVal newVal) {
        if (ptr.equals(oldVal)) {
            ptr = newVal;
            return true;
        }
        for (int i = 0; i < indices.size(); i++) {
            if (indices.get(i).equals(oldVal)) {
                indices.set(i, newVal);
                return true;
            }
        }
        return false;
    }

    @Override
    public String asValue() {
        return name;
    }

    @Override
    public Set<IRVal> getUses() {
        var res = new HashSet<>(indices);
        res.add(ptr);
        return res;
    }
}
