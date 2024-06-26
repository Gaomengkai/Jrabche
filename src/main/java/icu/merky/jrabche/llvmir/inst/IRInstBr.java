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

import icu.merky.jrabche.llvmir.structures.IRBasicBlock;
import icu.merky.jrabche.llvmir.types.VoidType;
import icu.merky.jrabche.llvmir.values.IRVal;
import icu.merky.jrabche.llvmir.values.IRValConstBool;

import java.util.Set;

public class IRInstBr extends IRInst implements BlockReplaceable {
    IRBasicBlock trueBB, falseBB;
    IRVal cond;

    public IRInstBr(IRVal cond, IRBasicBlock trueBB, IRBasicBlock falseBB) {
        super(InstID.BrInst, new VoidType());
        this.cond = cond;
        if (cond instanceof IRValConstBool boolVal) {
            if (boolVal.getValue() != 0) {
                this.trueBB = trueBB;
            } else {
                this.trueBB = falseBB;
            }
            this.falseBB = null;
            this.cond = null;
        } else {
            this.trueBB = trueBB;
            this.falseBB = falseBB;
        }
    }

    public IRInstBr(IRBasicBlock trueBB) {
        super(InstID.BrInst, new VoidType());
        this.trueBB = trueBB;
        this.falseBB = null;
        this.cond = null;
    }

    @Override
    public String toString() {
        if (cond == null) {
            // br label %L1
            return "br label %" + trueBB.getName();
        }
        // br i1 %v8, label %L1, label %L2
        return "br i1 " + cond.asValue() + ", label %" + trueBB.getName() + ", label %" + falseBB.getName();
    }

    @Override
    public boolean replace(IRVal oldVal, IRVal newVal) {
        if (cond == oldVal) {
            cond = newVal;
            // fast path to deal with CONST NEW VAL
            if (cond instanceof IRValConstBool boolConst) {
                if (boolConst.getValue() == 0) { // br false
                    trueBB = falseBB;
                }
                falseBB = null;
                cond = null;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean replaceBlock(IRBasicBlock old, IRBasicBlock newBB) {
        boolean ret = false;
        if (trueBB == old) {
            trueBB = newBB;
            ret = true;
        }
        if (falseBB == old) {
            falseBB = newBB;
            ret = true;
        }
        if (trueBB == falseBB) {
            cond = null;
            falseBB = null;
        }
        return ret;
    }

    @Override
    public Set<IRBasicBlock> getReplaceableBlocks() {
        if (null == cond) {
            return Set.of(trueBB);
        }
        return Set.of(trueBB, falseBB);
    }

    public IRBasicBlock getTrueBB() {
        return trueBB;
    }

    public IRBasicBlock getFalseBB() {
        return falseBB;
    }

    public IRVal getCond() {
        return cond;
    }

    @Override
    public String asValue() {
        return null;
    }

    @Override
    public Set<IRVal> getUses() {
        return cond == null ? Set.of() : Set.of(cond);
    }
}
