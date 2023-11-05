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

import icu.merky.jrabche.llvmir.values.IRVal;

import static icu.merky.jrabche.llvmir.types.PointerType.dePointer;

public class IRInstLoad extends IRInst {
    //     %v21 = load i32, i32* %v20
    private IRVal ptr;

    public IRInstLoad(IRVal ptr) {
        super(null, InstID.LoadInst, dePointer(ptr.getType()));
        this.ptr = ptr;
    }

    @Override
    public IRInstLoad clone() {
        IRInstLoad clone = (IRInstLoad) super.clone();
        clone.ptr = ptr;
        return clone;
    }

    @Override
    public String toString() {
        return name + " = load " + type.toString() + ", " + ptr.getType().toString() + " " + ptr.asValue();
    }

    @Override
    public boolean replace(IRVal inst, IRVal newInst) {
        return false;
    }

    @Override
    public String asValue() {
        return name;
    }
}
