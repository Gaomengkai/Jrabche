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

import icu.merky.jrabche.llvmir.types.IRBasicType;
import icu.merky.jrabche.llvmir.values.IRVal;

import java.util.Set;

public class IRInstReturn extends IRInst {
    IRVal opVal;

    public IRInstReturn(IRBasicType ty, IRVal opVal) {
        super(null, InstID.ReturnInst, ty.toIRType());
        this.opVal = opVal;
    }

    @Override
    public IRInstReturn clone() {
        IRInstReturn clone = (IRInstReturn) super.clone();
        clone.opVal = opVal;
        return clone;
    }

    @Override
    public String toString() {
        if (opVal == null) {
            return "ret void";
        } else {
            return "ret " + opVal.getType().toString() + " " + opVal.asValue();
        }
    }

    @Override
    public boolean replace(IRVal oldVal, IRVal newVal) {
        if (opVal == oldVal) {
            opVal = newVal;
            return true;
        }
        return false;
    }

    @Override
    public String asValue() {
        return name;
    }

    @Override
    public Set<IRVal> getUses() {
        return opVal == null ? Set.of() : Set.of(opVal);
    }
}

