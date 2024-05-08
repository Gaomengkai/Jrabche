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

import icu.merky.jrabche.llvmir.types.InvalidType;
import icu.merky.jrabche.llvmir.values.IRVal;

import java.util.Set;

import static icu.merky.jrabche.llvmir.types.PointerType.MakePointer;

public class IRInstStore extends IRInst {
    // store [123 x i32]* %arg_0, [123 x i32]** %v0
    IRVal from;


    public IRVal getTo() {
        return to;
    }

    IRVal to;

    public IRInstStore(IRVal from, IRVal to) {
        super(InstID.StoreInst, new InvalidType());
        this.from = from;
        this.to = to;
        // check type.
        if (!MakePointer(from.getType()).equals(to.getType())) throw new RuntimeException("Type mismatch.");
    }

    public IRVal getFrom() {
        return from;
    }

    @Override
    public String toString() {
        // store [123 x i32]* %arg_0, [123 x i32]** %v0
        return "store " + from.getType() + " " + from.asValue() + ", " + to.getType() + " " + to.asValue();
    }

    @Override
    public boolean replace(IRVal oldVal, IRVal newVal) {
        if (from == oldVal) {
            from = newVal;
            return true;
        }
        if (to == oldVal) {
            to = newVal;
            return true;
        }
        return false;
    }

    @Override
    public String asValue() {
        return null;
    }

    @Override
    public Set<IRVal> getUses() {
        return Set.of(from, to);
    }
}
