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

import icu.merky.jrabche.llvmir.types.IRType;
import icu.merky.jrabche.llvmir.values.IRVal;

import java.util.Set;

public class IRInstBitCast extends IRInst {
    private IRVal val;

    public IRInstBitCast(IRVal val, IRType toType) {
        super(InstID.BitCastInst, toType);
        this.val = val;
    }

    @Override
    public String toString() {
        return String.format("%s = bitcast %s %s to %s", name, val.getType(), val.asValue(), getType());
    }

    // @Override
    public IRVal getOperand(int i) {
        if (i != 0) throw new IndexOutOfBoundsException();
        return val;
    }

    // @Override
    public void setOperand(int i, IRVal val) {
        if (i != 0) throw new IndexOutOfBoundsException();
        this.val = val;
    }

    // @Override
    public int getNumOperands() {
        return 1;
    }

    @Override
    public boolean replace(IRVal oldVal, IRVal newVal) {
        if (val == oldVal) {
            val = newVal;
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
        return Set.of(val);
    }
}
