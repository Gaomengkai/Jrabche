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

import icu.merky.jrabche.llvmir.inst.IRInstBr;
import icu.merky.jrabche.llvmir.structures.IRBasicBlock;
import icu.merky.jrabche.llvmir.structures.IRFunction;
import icu.merky.jrabche.opt.llvmir.annotations.DisabledOpt;
import icu.merky.jrabche.opt.llvmir.annotations.OptOn;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static icu.merky.jrabche.llvmir.structures.impl.IRFunctionImpl.BuildBBGraph;
import static icu.merky.jrabche.logger.JrabcheLogger.JL;

@OptOn(value = OptOn.OptOnEnum.Function, name = "Block Rearrange", ssa = false)
@DisabledOpt
public class IROptBlockRearrange implements IROpt {
    private final IRFunction F;
    private int mergeCount = 0;
    private int removeCount = 0;

    public IROptBlockRearrange(IRFunction function) {
        this.F = function;
    }

    @Override
    public boolean go() {
        boolean changed = false;
        boolean innerChanged = false;
        boolean needContinue = false;
        Map<IRBasicBlock, IRBasicBlock> blockMap = new HashMap<>();
        changed |= directLinkBrBB(changed, blockMap);
        do {
            innerChanged = mergeBB();
            innerChanged |= removeUnreachableBB();
            changed |= innerChanged;
        } while (innerChanged);
        if (mergeCount + removeCount > 0)
            JL.Debug("BR  :func=" + F.getName() + "\tmerge " + mergeCount + " blocks, remove " + removeCount + " blocks");
        return changed;
    }


    private boolean removeUnreachableBB() {
        boolean changed = false;
        boolean innerChanged;
        boolean needContinue;
        do {
            innerChanged = false;
            BuildBBGraph(F);
            Iterator<IRBasicBlock> iterator = F.getBlocks().iterator();
            while (iterator.hasNext()) {
                var bb = iterator.next();
                if (bb.getPre().isEmpty() && bb != F.entryBB()) {
                    iterator.remove();
                    innerChanged = true;
                    removeCount++;
                }
            }
            changed |= innerChanged;
            needContinue = innerChanged;
        } while (needContinue);
        return changed;
    }

    /**
     * Merge block X,Y if:<br>
     * 1. X has only one successor Y<br>
     * 2. Y has only one predecessor X<br>
     * 3. X and Y are not the entry block<br>
     * Then copy all the instructions from Y to X, and replace all the uses of Y (actually there's no
     * other uses) with X.<br>
     *
     * @return whether changed
     */
    private boolean mergeBB() {
        boolean changed = false;
        boolean innerChanged = false;
        boolean needContinue = false;
        do {
            innerChanged = false;
            BuildBBGraph(F);
            for (IRBasicBlock X : F.getBlocks()) {
                if (X.getSuc().size() == 1 && X.getSuc().get(0).getPre().size() == 1 && X != F.entryBB()) {
                    var Y = X.getSuc().get(0);
                    if (Y.getPre().size() == 1 && Y.getPre().get(0) == X) {
                        // merge X,Y
                        // System.out.println("merge " + X.getName() + " from " + Y.getName());
                        // 0. remove terminator of X
                        X.getInsts().remove(X.getInsts().size() - 1);
                        // 1. move insts from Y to X
                        for (var inst : Y.getInsts()) {
                            X.addInst(inst);
                        }
                        mergeCount++;
                        innerChanged = true;
                    }
                }
            }
            changed |= innerChanged;
            needContinue = innerChanged;
        } while (needContinue);
        return changed;
    }

    private boolean directLinkBrBB(boolean changed, Map<IRBasicBlock, IRBasicBlock> blockMap) {
        boolean needContinue;
        boolean innerChanged;
        do {
            innerChanged = false;
            for (var bb : F.getBlocks()) {
                if (bb.getSize() == 1 && bb.getTerminator().isBrInst()) {
                    var br = (IRInstBr) bb.getTerminator();
                    if (br.getCond() == null) {
                        var target = br.getTrueBB();
                        blockMap.put(bb, target);
                    }
                }
            }

            for (var bb : F.getBlocks()) {
                if (bb.getTerminator() instanceof IRInstBr br) {
                    var target1 = br.getTrueBB();
                    if (blockMap.containsKey(target1)) {
                        br.replaceBlock(target1, blockMap.get(target1));
                        innerChanged = true;
                    }
                    var target2 = br.getFalseBB();
                    if (target2 != null && blockMap.containsKey(target2)) {
                        br.replaceBlock(target2, blockMap.get(target2));
                        innerChanged = true;
                    }
                }
            }
            changed |= innerChanged;
            needContinue = innerChanged;
        } while (needContinue);
        return changed;
    }
}
