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

import icu.merky.jrabche.llvmir.StaticVariableCounter;
import icu.merky.jrabche.llvmir.inst.*;
import icu.merky.jrabche.llvmir.structures.IRBasicBlock;
import icu.merky.jrabche.llvmir.structures.IRFunction;
import icu.merky.jrabche.llvmir.values.IRVal;
import icu.merky.jrabche.llvmir.values.IRValUndef;
import icu.merky.jrabche.opt.llvmir.algorithms.BlockNode;
import icu.merky.jrabche.opt.llvmir.algorithms.BlockNodeBuilder;
import icu.merky.jrabche.opt.llvmir.annotations.OptOn;
import icu.merky.jrabche.opt.llvmir.annotations.PassOn;
import org.antlr.v4.runtime.misc.Pair;

import java.util.*;

import static icu.merky.jrabche.llvmir.support.InstUtil.RemoveDeletedInstructions;
import static icu.merky.jrabche.llvmir.support.InstUtil.ReplaceAllUsesWith;
import static icu.merky.jrabche.logger.JrabcheLogger.L;

@OptOn(value = OptOn.OptOnEnum.Function, changeSSA = true, ssa = false, name = "Memory to Register", afterWhich = {IROptRLSE.class})
@PassOn(PassOn.on.FUNCTION)
public class Mem2Reg implements IRPass {
    IRFunction F;
    ArrayList<IRInstAlloca> allocas = new ArrayList<>();
    Map<IRInstAlloca, Integer> allocaLookup = new HashMap<>();
    Map<IRBasicBlock, BlockNode> reversedBlockMap = new HashMap<IRBasicBlock, BlockNode>();
    Map<IRBasicBlock, Integer> bbNumbers = new HashMap<IRBasicBlock, Integer>();
    Map<Pair<Integer, Integer>, IRInstPhi> newPhiNodes = new HashMap<>();
    Map<IRInstPhi, Integer> phiToAllocaMap = new HashMap<>();
    Set<IRBasicBlock> visited = new HashSet<>();

    public Mem2Reg(IRFunction function) {
        F = function;
    }

    private static class AllocaInfo {
        public ArrayList<IRBasicBlock> DefBlocks = new ArrayList<>(4);
        public ArrayList<IRBasicBlock> UseBlocks = new ArrayList<>(4);
        public IRInstStore OnlyStore;
        public IRBasicBlock OnlyBlock;
        boolean OnlyUsedInOneBlock;

        private void clear() {
            DefBlocks.clear();
            UseBlocks.clear();
            OnlyStore = null;
            OnlyBlock = null;
            OnlyUsedInOneBlock = true;
        }

        /**
         * Scan the uses of the specified alloca, filling in the AllocaInfo used
         * by the rest of the pass to reason about the uses of this alloca.
         *
         * @param AI alloca instruction
         */
        public void AnalyzeAlloca(IRInstAlloca AI) {
            clear();
            for (IRInst U : AI.getUsedBy()) {
                if (U instanceof IRInstStore store) {
                    DefBlocks.add(store.getParent());
                    OnlyStore = store;
                } else {
                    IRInstLoad load = (IRInstLoad) U;
                    UseBlocks.add(load.getParent());
                }

                if (OnlyUsedInOneBlock) {
                    if (OnlyBlock == null)
                        OnlyBlock = U.getParent();
                    else if (OnlyBlock != U.getParent())
                        OnlyUsedInOneBlock = false;
                }
            }
        }
    }

    @Override
    public boolean go() {
        // 0. init dom frontiers
        BlockNodeBuilder bnb = new BlockNodeBuilder(F);
        bnb.build();
        bnb.buildLiveInOut();
        reversedBlockMap = bnb.getReverseBlockMap();
        BlockNode root = bnb.getRoot();

        AllocaInfo info = new AllocaInfo();

        // 1. find all alloca
        for (var I : F.entryBB().getInsts()) {
            if (I instanceof IRInstAlloca a) {
                if (isAllocaPromotable(a)) {
                    allocas.add(a);
                    allocaLookup.put(a, allocas.size() - 1);
                }
            } else {
                // fast path: if the first instruction is not alloca, then there is no alloca in this function.
                break;
            }
        }

        if (bbNumbers.isEmpty()) {
            int id = 0;
            for (var bn : bnb.getDfo()) {
                bbNumbers.put(bn.getVal(), id++);
            }
        }

        for (int allocaNum = 0; allocaNum < allocas.size(); allocaNum++) {
            IRInstAlloca ai = allocas.get(allocaNum);
            assert isAllocaPromotable(ai);

            info.AnalyzeAlloca(ai);
            // rewrite single alloca was finished by IROptRLSE pass.

            allocaLookup.put(ai, allocaNum);

            Set<IRBasicBlock> defBlocks = new HashSet<>(info.DefBlocks);

            Set<IRBasicBlock> liveInBlocks = new HashSet<>();
            computeLiveInBlocks(ai, bnb, liveInBlocks);

            Set<IRBasicBlock> phiBlocks = calculatePhiBlocks(defBlocks, liveInBlocks, bnb);

            List<IRBasicBlock> phiBlocksList = new ArrayList<>(phiBlocks);
            phiBlocksList.sort(Comparator.comparingInt(bbNumbers::get));

            for (IRBasicBlock phiBlock : phiBlocksList) {
                queuePhiNode(phiBlock, allocaNum);
            }
        }

        List<IRVal> values = new ArrayList<>(allocas.size());
        for (IRInstAlloca alloca : allocas) {
            values.add(IRValUndef.create(alloca.getAllocatedType()));
        }

        List<RenamePassData> renamePassWorkList = new ArrayList<>();
        renamePassWorkList.add(new RenamePassData(root.getVal(), null, values));

        do {
            RenamePassData rpd = renamePassWorkList.remove(renamePassWorkList.size() - 1);
            RenamePass(rpd.BB, rpd.Pred, rpd.Vals, renamePassWorkList);
        } while (!renamePassWorkList.isEmpty());

        visited.clear();

        allocas.forEach(IRInstAlloca::setDeleted);
        for (IRBasicBlock b : F.getBlocks()) {
            RemoveDeletedInstructions(b);
        }

        return false;
    }

    //
    private void RenamePass(IRBasicBlock bb, IRBasicBlock pred, List<IRVal> incomingVals, List<RenamePassData> workList) {

        Set<IRBasicBlock> visitedSuccs = new HashSet<IRBasicBlock>();
        while (true) {
            L.DebugF(" rename " + bb.getName());
            if (bb.getInsts().get(0) instanceof IRInstPhi) {
                int numEdges = pred.getSuc().size();

                // add entries to the phi node
                for (IRInst inst : bb.getInsts()) {
                    if (inst instanceof IRInstPhi phi) {
                        int allocaNo = phiToAllocaMap.get(phi);
                        for (int i = 0; i < numEdges; i++) {
                            L.DebugF("assign val `%s` to phi `%s`\n", incomingVals.get(allocaNo).asValue(), phi.getName());
                            phi.addIncoming(incomingVals.get(allocaNo), pred);
                        }
                        incomingVals.set(allocaNo, phi);
                        L.DebugF("Set Alloca Val `%s` = `%s`\n", allocas.get(allocaNo).getName(), phi.getName());
                    } else {
                        break;
                    }
                }
            }

            if (!visited.add(bb)) {
                L.DebugF("Skip `%s`\n", bb.getName());
                return;
            }

            for (var I : bb.getInsts()) {
                if (I.isDeleted()) continue;
                if (I instanceof IRInstLoad LI) {
                    IRVal src = LI.getFrom();
                    if (!(src instanceof IRInstAlloca)) {
                        continue;
                    }
                    if (!allocaLookup.containsKey(src)) {
                        continue;
                    }
                    IRVal v = incomingVals.get(allocaLookup.get(src));
                    ReplaceAllUsesWith(LI, v);
                    LI.setDeleted();
                } else if (I instanceof IRInstStore SI) {
                    IRVal dest = SI.getTo();
                    if (!(dest instanceof IRInstAlloca)) {
                        continue;
                    }
                    if (!allocaLookup.containsKey(dest)) {
                        continue;
                    }
                    L.DebugF("Set Alloca Val `%s` = `%s`\n", dest.asValue(), SI.getFrom().asValue());
                    incomingVals.set(allocaLookup.get(dest), SI.getFrom());
                    SI.setDeleted();
                }
            }

            if (bb.getSuc().size() == 0) {
                return;
            }
            visitedSuccs.clear();
            var succIter = bb.getSuc().iterator();
            var iterNext = succIter.next();
            visitedSuccs.add(iterNext);
            pred = bb;
            bb = iterNext;

            while (succIter.hasNext()) {
                var succ = succIter.next();
                if (visitedSuccs.add(succ)) {
                    workList.add(new RenamePassData(succ, pred, new ArrayList<>(incomingVals)));
                }
            }
        }
    }


    private boolean queuePhiNode(IRBasicBlock b, int allocaNum) {
        if (newPhiNodes.containsKey(new Pair<>(bbNumbers.get(b), allocaNum))) return false;
        IRInstPhi phi = new IRInstPhi(allocas.get(allocaNum).getAllocatedType());
        String phiName = allocas.get(allocaNum).getName() + "." + StaticVariableCounter.getAndIncrement();
        phi.setName(phiName);

        // insert into basicBlock
        b.getInsts().add(0, phi);
        newPhiNodes.put(new Pair<>(bbNumbers.get(b), allocaNum), phi);
        phiToAllocaMap.put(phi, allocaNum);
        return true;
    }

    private Set<IRBasicBlock> calculatePhiBlocks(Set<IRBasicBlock> defBlocks, Set<IRBasicBlock> liveInBlocks, BlockNodeBuilder bnb) {
        // 这种方式有问题，因为有可能会漏掉一些phi节点
        // 假如block2被插入了一个phi，那么block2也会成为defBlock，但是block2不会被加入到phiBlocks中.
        // 所以这里需要用到DF+集合
        Set<IRBasicBlock> phiBlocks = new HashSet<>();
        Queue<IRBasicBlock> queue = new LinkedList<>(defBlocks);
        Set<IRBasicBlock> visited = new HashSet<>();
        var DF = bnb.getDf();
        while (!queue.isEmpty()) {
            IRBasicBlock block = queue.poll();
            if (!visited.add(block)) continue;
            for (BlockNode dfBlock : DF.get(bnb.re(block))) {
                if (liveInBlocks.contains(dfBlock.getVal())) {
                    queue.add(dfBlock.getVal());
                    phiBlocks.add(dfBlock.getVal());
                }
            }
        }
        return phiBlocks;
    }

    private static void computeLiveInBlocks(IRInstAlloca AI, BlockNodeBuilder bnb, /*out*/ Set<IRBasicBlock> liveInBlocks) {
        assert liveInBlocks != null;
        assert bnb.getRoot().liveIn != null;
        for (BlockNode blockNode : bnb.getDfo()) {
            if (blockNode.liveIn.contains(AI)) {
                liveInBlocks.add(blockNode.getVal());
            }
        }
    }

    private boolean isAllocaPromotable(IRInstAlloca alloca) {
        return alloca.getAllocatedType().isScalar();
    }

    private record RenamePassData(
            IRBasicBlock BB,
            IRBasicBlock Pred,
            List<IRVal> Vals
    ) {
    }
}
