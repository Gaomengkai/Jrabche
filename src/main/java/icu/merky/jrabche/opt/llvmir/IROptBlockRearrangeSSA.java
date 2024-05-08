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
import icu.merky.jrabche.llvmir.inst.IRInstBr;
import icu.merky.jrabche.llvmir.inst.IRInstPhi;
import icu.merky.jrabche.llvmir.structures.IRBasicBlock;
import icu.merky.jrabche.llvmir.structures.IRFunction;
import icu.merky.jrabche.llvmir.values.IRVal;
import icu.merky.jrabche.opt.llvmir.annotations.DisabledOpt;
import icu.merky.jrabche.opt.llvmir.annotations.OptOn;

import java.util.*;

import static icu.merky.jrabche.llvmir.structures.impl.IRFunctionImpl.BuildBBGraph;

@OptOn(value = OptOn.OptOnEnum.Function, ssa = true, afterWhich = {IROptDCE.class}, name = "Block Rearrange (SSA)")
@DisabledOpt
public class IROptBlockRearrangeSSA implements IROpt {
    private final IRFunction F;

    public IROptBlockRearrangeSSA(IRFunction function) {
        this.F = function;
    }

    @Override
    public boolean go() {
        boolean changed = false;
        changed |= cleanUpSinglePhi();
        changed |= removeUnreachableBB();
        changed |= directLinkBrBB();
        changed |= cleanUpSinglePhi();
        return changed;
    }

    private boolean cleanUpSinglePhi() {
        boolean changed = false;
        var replaceMap = new HashMap<IRVal, IRVal>();
        for (IRBasicBlock B : F.getBlocks()) {
            for (IRInst I : B.getInsts()) {
                if (I instanceof IRInstPhi phi) {
                    var incoming = phi.getIncoming();
                    if (incoming.size() == 1) {
                        replaceMap.put(phi, incoming.values().iterator().next());
                    }
                } else break;
            }
        }

        for (IRBasicBlock B : F.getBlocks()) {
            for (IRInst I : B.getInsts()) {
                for (IRVal use : I.getUses()) {
                    if (replaceMap.containsKey(use)) {
                        I.replace(use, replaceMap.get(use));
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }


    private boolean removeUnreachableBB() {
        boolean changed = false;
        BuildBBGraph(F);
        Set<IRBasicBlock> reachable = new HashSet<>();
        Queue<IRBasicBlock> queue = new LinkedList<>();
        queue.add(F.entryBB());
        reachable.add(F.entryBB());
        while (!queue.isEmpty()) {
            var bb = queue.poll();
            for (var next : bb.getSuc()) {
                if (reachable.add(next)) {
                    queue.add(next);
                }
            }
        }
        Set<IRBasicBlock> removeList = new HashSet<>();
        for (var bb : F.getBlocks()) {
            if (!reachable.contains(bb)) {
                removeList.add(bb);
                changed = true;
            }
        }
        for (var bb : removeList) {
            F.removeBlock(bb);
        }
        // fix phi instructions
        for (IRBasicBlock B : F.getBlocks()) {
            for (IRInst I : B.getInsts()) {
                if (I instanceof IRInstPhi phi) {
                    for (IRBasicBlock IN : new HashSet<>(phi.getIncoming().keySet())) {
                        if (removeList.contains(IN)) {
                            phi.removeIncoming(IN);
                        }
                    }
                }
            }
        }
        return changed;
    }

    private boolean directLinkBrBB() {
        boolean needContinue = false;
        boolean changed = false;
        boolean innerChanged;
        do {
            Map<IRBasicBlock, IRBasicBlock> aBrB = new HashMap<>();

            innerChanged = false;
            BuildBBGraph(F);


            for (var bb : F.getBlocks()) {
                if (bb.getSize() == 1 && bb.getTerminator().isBrInst()) {
                    IRInstBr br = (IRInstBr) bb.getTerminator();
                    if (br.getCond() == null) {
                        var target = br.getTrueBB();
                        aBrB.put(bb, target);
                    }
                }
            }

            // 考虑连续的链状结构。

            for (Map.Entry<IRBasicBlock, IRBasicBlock> edge : aBrB.entrySet()) {
                var here = edge.getKey();
                var froms = here.getPre();
                var phiBlock = edge.getValue();
                for (var I : phiBlock.getInsts()) {
                    if (I instanceof IRInstPhi phi) {
                        for (Map.Entry<IRBasicBlock, IRVal> phiIncoming : phi.getIncoming().entrySet()) {
                            if (phiIncoming.getKey() == here) {
                                // delete here incoming, add froms incomings.
                                var val = phiIncoming.getValue();
                                if (froms.isEmpty()) {
                                    continue;
                                }
                                phi.removeIncoming(here);
                                System.out.printf("Removed %s from phi instruction %s\n", here.getName(), phi.asValue());
                                for (IRBasicBlock from : froms) {
                                    while (aBrB.containsKey(from)) {
                                        from = aBrB.get(from);
                                    }
                                    phi.addIncoming(val, from);
                                    System.out.printf("Added %s to phi instruction %s\n", from.getName(), phi.asValue());
                                }
                                break; // java.util.ConcurrentModificationException
                            }
                        }
                    } else break;
                }
            }

            for (var bb : F.getBlocks()) {
                if (bb.getTerminator() instanceof IRInstBr I) {
                    var targets = I.getReplaceableBlocks();
                    for (var target : targets) {
                        if (aBrB.containsKey(target)) {
                            innerChanged |= I.replaceBlock(target, aBrB.get(target));
                        }
                    }
                }
            }
            changed |= innerChanged;
            needContinue = innerChanged;
        } while (needContinue);
        return changed;
    }

}
