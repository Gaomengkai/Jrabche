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

package icu.merky.jrabche.opt.llvmir.algorithms;

import icu.merky.jrabche.llvmir.inst.IRInstAlloca;
import icu.merky.jrabche.llvmir.inst.IRInstLoad;
import icu.merky.jrabche.llvmir.inst.IRInstStore;
import icu.merky.jrabche.llvmir.structures.IRBasicBlock;

import java.util.*;

public class BlockNode extends GraphNode<IRBasicBlock> {
    public Set<IRInstAlloca> liveIn = new HashSet<>(), liveOut = new HashSet<>(),
            def = new HashSet<>(), use = new HashSet<>();
    boolean BlockNodesBuilt = false;
    boolean DefUseBuilt = false;
    int size = 0;

    public BlockNode(IRBasicBlock val) {
        super(val);
    }

    public static void BuildLiveInOut(BlockNode root) {
        // livein[s] = gen[s] ∪ (liveout[s] - kill[s])
        // liveout[final] = φ
        // liveout[s] = ∪ livein[p] for p in s.succ
        for (Iterator<BlockNode> it = root.DFOIter(); it.hasNext(); ) {
            var bn = it.next();
            if (!bn.DefUseBuilt) {
                bn.BuildDefUse();
            }
        }
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Iterator<BlockNode> it = root.DFOIter(); it.hasNext(); ) {
                var bn = it.next();

                if (!bn.isExitNode()) {
                    Set<IRInstAlloca> newLiveOut = new HashSet<>();
                    for (var s : bn.getSuccessors()) {
                        newLiveOut.addAll(((BlockNode) s).liveIn);
                    }
                    bn.liveOut = newLiveOut;
                }

                Set<IRInstAlloca> newIn = new HashSet<>();
                newIn.addAll(bn.use);
                newIn.addAll(bn.liveOut);
                newIn.removeAll(bn.def);
                if (!newIn.equals(bn.liveIn)) {
                    changed = true;
                    bn.liveIn = newIn;
                }
            }
        }
    }

    public static void BuildBlockNodes(BlockNode root, Map<IRBasicBlock, BlockNode> reversedBlockMap) {
        Queue<BlockNode> q = new LinkedList<>();
        Map<IRBasicBlock, BlockNode> built = new HashMap<>();
        Set<IRBasicBlock> visited = new HashSet<>();
        q.add(root);
        while (!q.isEmpty()) {
            BlockNode cur = q.poll();
            if (visited.contains(cur.val)) continue;
            visited.add(cur.val);
            for (IRBasicBlock s : cur.val.getSuc()) {
                BlockNode sNode;
                if (!built.containsKey(s)) {
                    sNode = new BlockNode(s);
                    if (reversedBlockMap != null) reversedBlockMap.put(s, sNode);
                    cur.addSuccessor(sNode);
                    built.put(s, sNode);
                    q.add(sNode);
                } else {
                    sNode = built.get(s);
                    cur.addSuccessor(sNode);
                }
            }
            cur.BlockNodesBuilt = true;
        }
        BuildPredecessor(root);
        root.size = visited.size();
    }

    public static Map<IRBasicBlock, BlockNode> BuildReversedBlockMap(BlockNode root) {
        Map<IRBasicBlock, BlockNode> map = new HashMap<>();
        for (Iterator<BlockNode> it = root.DFOIter(); it.hasNext(); ) {
            var bn = it.next();
            map.put(bn.val, bn);
        }
        return map;
    }

    @Override
    public String toString() {
        return val.getName();
    }

    public boolean isExitNode() {
        return val.getTerminator() instanceof icu.merky.jrabche.llvmir.inst.IRInstReturn;
    }

    static class InternalBNIter implements Iterator<BlockNode> {
        private final Iterator<BlockNode> iterator;

        InternalBNIter(List<BlockNode> dfo) {
            this.iterator = dfo.stream().iterator();
        }

        @Override
        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        @Override
        public BlockNode next() {
            return this.iterator.next();
        }
    }

    public Iterator<BlockNode> DFOIter() {
        if (!this.BlockNodesBuilt) {
            BuildBlockNodes(this, null);
        }
        List<BlockNode> dfo = new DeepFirstOrder<>(this, this.size).getOrder();
        return new InternalBNIter(dfo);
    }

    public IRBasicBlock getBlock() {
        return val;
    }

    public static Iterator<BlockNode> DFOIter(BlockNodeBuilder bnb) {
        List<BlockNode> dfo = bnb.getDfo();
        return new InternalBNIter(dfo);
    }

    void BuildDefUse() {
        DefUseBuilt = true;
        for (var I : this.val.getInsts()) {
            if (I instanceof IRInstLoad load && load.getFrom() instanceof IRInstAlloca from) {
                if (!def.contains(from)) {
                    use.add(from);
                }
            } else if (I instanceof IRInstStore store && store.getTo() instanceof IRInstAlloca to) {
                if (!use.contains(to)) {
                    def.add(to);
                }
            }
        }
    }
}
