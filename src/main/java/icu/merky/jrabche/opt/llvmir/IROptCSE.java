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

package icu.merky.jrabche.opt.llvmir;

import icu.merky.jrabche.llvmir.inst.IRInstMath;
import icu.merky.jrabche.llvmir.structures.IRBasicBlock;
import icu.merky.jrabche.llvmir.values.IRVal;
import icu.merky.jrabche.llvmir.values.IRValConstInt;
import icu.merky.jrabche.opt.llvmir.annotations.OptOn;

import java.util.HashMap;

@OptOn(ssa = false, value = OptOn.OptOnEnum.BasicBlock, name = "Common Subexpression Elimination")
// @DisabledOpt
public class IROptCSE implements IROpt {
    private final IRBasicBlock bb;

    static class IMathTriple {
        IRInstMath.MathOP op;
        IRVal lhs, rhs;

        public IMathTriple(IRInstMath.MathOP op, IRVal lhs, IRVal rhs) {
            this.op = op;
            this.lhs = lhs;
            this.rhs = rhs;
        }

        @Override
        public int hashCode() {
            int hash1 = op.ordinal();
            int hash2 = 0, hash3 = 0;
            if (lhs instanceof IRValConstInt lInt) {
                hash2 = lInt.getValue();
            } else {
                hash2 = lhs.hashCode();
            }
            if (rhs instanceof IRValConstInt rInt) {
                hash3 = rInt.getValue();
            } else {
                hash3 = rhs.hashCode();
            }

            return hash1 << 30 ^ hash2 << 15 ^ hash3;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof IMathTriple triple) {
                if (!triple.op.equals(this.op)) return false;
                if (op == IRInstMath.MathOP.Add || op == IRInstMath.MathOP.Mul) {
                    // 交换律比较
                    return (baseEq(lhs, triple.lhs) && baseEq(rhs, triple.rhs))
                            || (baseEq(lhs, triple.rhs) && baseEq(rhs, triple.lhs));
                } else return baseEq(lhs, triple.lhs) && baseEq(rhs, triple.rhs);
            }
            return false;
        }

        private static boolean baseEq(IRVal lhs, IRVal rhs) {
            if (lhs instanceof IRValConstInt constInt) {
                if (rhs instanceof IRValConstInt constInt1) {
                    return constInt.getValue() == constInt1.getValue();
                }
                return false;
            }
            return lhs == rhs; // pointer equality
        }
    }

    public IROptCSE(IRBasicBlock bb) {
        this.bb = bb;
    }

    @Override
    public boolean go() {
        boolean changed = false;
        HashMap<IMathTriple, IRVal> calculated = new HashMap<>();
        HashMap<IRVal, IRVal> replaceMap = new HashMap<>();

        for (var inst : bb.getInsts()) {
            if (inst instanceof IRInstMath math) {
                if (!inst.getType().isInt()) continue;
                var triple = new IMathTriple(math.getMathOP(), math.getLhs(), math.getRhs());
                if (calculated.containsKey(triple)) {
                    replaceMap.put(inst, calculated.get(triple));
                } else {
                    calculated.put(triple, inst);
                }
            }
        }

        for (var inst : bb.getInsts()) {
            for (var used : inst.getUses()) {
                if (replaceMap.containsKey(used)) {
                    changed |= inst.replace(used, replaceMap.get(used));
                }
            }
        }
        return changed;
    }
}
