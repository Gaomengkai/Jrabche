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
import icu.merky.jrabche.llvmir.types.IntType;
import icu.merky.jrabche.llvmir.values.IRVal;

public class IRInstIcmp extends IRInst {
    private IcmpOp op;
    private IRVal lhs, rhs;
    private IRType opType;
    /**
     * Create icmp instruction
     *
     * @param op  icmp op
     * @param lhs left operand
     * @param rhs right operand
     */
    public IRInstIcmp(IcmpOp op, IRVal lhs, IRVal rhs) {
        super(null, InstID.ICmpInst, new IntType(1));
        this.op = op;
        this.lhs = lhs;
        this.rhs = rhs;
        this.opType = new IntType(32);
    }

    @Override
    public IRInstIcmp clone() {
        IRInstIcmp clone = (IRInstIcmp) super.clone();
        clone.op = op;
        // do not clone value
        clone.lhs = lhs;
        clone.rhs = rhs;
        clone.opType = opType;
        return clone;
    }

    @Override
    public String toString() {
        //     %v13 = icmp sge i32 %v11, %v12
        return name +
                " = icmp " +
                op.toString() +
                " " +
                opType.toString() +
                " " +
                lhs.getName() +
                ", " + rhs.getName();
    }

    @Override
    public boolean replace(IRVal inst, IRVal newInst) {
        boolean replaced = false;
        if (lhs == inst) {
            lhs = newInst;
            replaced = true;
        }
        if (rhs == inst) {
            rhs = newInst;
            replaced = true;
        }
        return replaced;
    }

    public IcmpOp getOp() {
        return op;
    }

    public IRVal getLhs() {
        return lhs;
    }

    public IRVal getRhs() {
        return rhs;
    }

    @Override
    public String asValue() {
        return name;
    }

    public enum IcmpOp {
        EQ, NE, SGT, SGE, SLT, SLE, UGE, UGT, ULE, ULT;

        @Override
        public String toString() {
            return switch (this) {
                case EQ -> "eq";
                case NE -> "ne";
                case SGT -> "sgt";
                case SGE -> "sge";
                case SLT -> "slt";
                case SLE -> "sle";
                case UGE -> "uge";
                case UGT -> "ugt";
                case ULE -> "ule";
                case ULT -> "ult";
            };
        }
    }
}
