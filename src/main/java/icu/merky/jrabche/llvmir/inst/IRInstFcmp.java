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

import icu.merky.jrabche.llvmir.types.FloatType;
import icu.merky.jrabche.llvmir.types.IRType;
import icu.merky.jrabche.llvmir.types.IntType;
import icu.merky.jrabche.llvmir.values.IRVal;

import java.util.Set;

public class IRInstFcmp extends IRInst {

    private FcmpOp op;
    private IRVal lhs, rhs;
    private IRType opType;

    /**
     * Create a new fcmp instruction
     *
     * @param op  The comparison operation to perform
     * @param lhs The first operand
     * @param rhs The second operand
     */
    public IRInstFcmp(FcmpOp op, IRVal lhs, IRVal rhs) {
        super(null, InstID.FCmpInst, new IntType(1));
        this.op = op;
        this.lhs = lhs;
        this.rhs = rhs;
        this.opType = new FloatType();
    }

    @Override
    public String toString() {
        return name + " = fcmp " + op.toString().toLowerCase() + " " + opType.toString() + " " + lhs.asValue() + ", " + rhs.asValue();
    }

    @Override
    public IRInstFcmp clone() {
        IRInstFcmp clone = (IRInstFcmp) super.clone();
        clone.op = op;
        clone.lhs = lhs;
        clone.rhs = rhs;
        clone.opType = opType.clone();
        return clone;
    }

    @Override
    public boolean replace(IRVal oldVal, IRVal newVal) {
        if (lhs == oldVal) {
            lhs = newVal;
            return true;
        }
        if (rhs == oldVal) {
            rhs = newVal;
            return true;
        }
        return false;
    }

    public FcmpOp getOp() {
        return op;
    }

    public IRVal getLhs() {
        return lhs;
    }

    public IRVal getRhs() {
        return rhs;
    }

    public IRType getOpType() {
        return opType;
    }

    @Override
    public String asValue() {
        return name;
    }

    @Override
    public Set<IRVal> getUses() {
        return Set.of(lhs, rhs);
    }

    public enum FcmpOp {FALSE, OEQ, OGE, OGT, OLE, OLT, TRUE, UEQ, UGE, UGT, ULE, ULT, UNE, UNO}
}
