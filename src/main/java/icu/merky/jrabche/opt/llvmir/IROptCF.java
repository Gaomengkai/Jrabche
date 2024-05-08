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

import icu.merky.jrabche.llvmir.inst.IRInst;
import icu.merky.jrabche.llvmir.inst.IRInstIcmp;
import icu.merky.jrabche.llvmir.inst.IRInstMath;
import icu.merky.jrabche.llvmir.inst.IRInstPhi;
import icu.merky.jrabche.llvmir.structures.IRBasicBlock;
import icu.merky.jrabche.llvmir.structures.IRFunction;
import icu.merky.jrabche.llvmir.structures.impl.IRFunctionImpl;
import icu.merky.jrabche.llvmir.values.IRVal;
import icu.merky.jrabche.llvmir.values.IRValConstBool;
import icu.merky.jrabche.llvmir.values.IRValConstInt;
import icu.merky.jrabche.logger.JrabcheLogger;
import icu.merky.jrabche.opt.llvmir.annotations.OptOn;
import icu.merky.jrabche.support.AutoNewCollectionHashMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static icu.merky.jrabche.llvmir.support.IRCompileTimeCalc.DoCompileTimeCalculation;
import static icu.merky.jrabche.llvmir.support.IRCompileTimeCalc.DoCompileTimeComparison;

@OptOn(value = OptOn.OptOnEnum.Function, ssa = true, afterWhich = {IROptDCE.class}, name = "Const Folding")
// @DisabledOpt
public class IROptCF implements IROpt {

    private final IRFunction F;

    public IROptCF(IRFunction F) {
        this.F = F;
    }

    @Override
    public boolean go() {
        return goOnce();
    }

    private boolean goOnce() {

        boolean changed = false;

        Map<IRVal, IRVal> replaceMap = new HashMap<>();
        for (IRBasicBlock B : F.getBlocks()) {
            for (IRInst I : B.getInsts()) {
                if (I instanceof IRInstMath MI && MI.getType().isInt()) {
                    var L = MI.getLhs();
                    var R = MI.getRhs();
                    if (L instanceof IRValConstInt CL && R instanceof IRValConstInt CR) {
                        var res = DoCompileTimeCalculation(CL, CR, MI.getMathOP());
                        replaceMap.put(I, res);
                    }
                }
                if (I instanceof IRInstIcmp CI) {
                    var L = CI.getLhs();
                    var R = CI.getRhs();
                    if (L instanceof IRValConstInt CL && R instanceof IRValConstInt CR) {
                        IRValConstBool res = DoCompileTimeComparison(CL, CR, CI.getOp());
                        replaceMap.put(I, res);
                    }
                }
            }
        }

        // replace
        for (IRBasicBlock B : F.getBlocks()) {
            for (IRInst I : B.getInsts()) {
                for (IRVal U : I.getUses()) {
                    if (replaceMap.containsKey(U)) {
                        changed |= I.replace(U, replaceMap.get(U));
                        JrabcheLogger.JL.DebugF("%s == %s in %s\n", U.asValue(), replaceMap.get(U).asValue(), I);
                    }
                }
            }
        }

        changed |= cleanUpUnreachablePhiPred();
        return changed;
    }

    private boolean cleanUpUnreachablePhiPred() {
        boolean changed = false;

        IRFunctionImpl.BuildBBGraph(F);

        AutoNewCollectionHashMap<IRInstPhi, IRBasicBlock> willBeRemoved = new AutoNewCollectionHashMap<>(HashSet::new);

        for (IRBasicBlock B : F.getBlocks()) {
            for (IRInst I : B.getInsts()) {
                if (I instanceof IRInstPhi phi) {
                    for (IRBasicBlock P : phi.getIncoming().keySet()) {
                        if (!B.getPre().contains(P)) {
                            willBeRemoved.insert(phi, P);
                        }
                    }
                } else break;
            }
        }

        for (IRInstPhi phi : willBeRemoved.keySet()) {
            for (IRBasicBlock b : willBeRemoved.get(phi)) {
                changed |= phi.removeIncoming(b);
            }
        }

        return changed;
    }
}
