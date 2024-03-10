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

import icu.merky.jrabche.llvmir.structures.IRBasicBlock;
import icu.merky.jrabche.llvmir.structures.IRFunction;
import icu.merky.jrabche.opt.llvmir.support.EmptyMapSet;

import java.util.*;

public class BlockNodeBuilder {
    private final BlockNode root;
    private final int sizeBBs;

    List<BlockNode> dfo;
    Map<BlockNode, BlockNode> idomMap;
    Map<BlockNode, Set<BlockNode>> df;
    Map<IRBasicBlock, BlockNode> reversedBlockMap = new HashMap<>();

    public BlockNodeBuilder(BlockNode root, int sizeBBs) {
        this.root = root;
        this.sizeBBs = sizeBBs;
    }

    public BlockNodeBuilder(IRFunction F) {
        this.root = new BlockNode(F.entryBB());
        this.sizeBBs = F.getBlocks().size();
    }

    public BlockNode getRoot() {
        return root;
    }

    public void build() {
        BlockNode.BuildBlockNodes(root, reversedBlockMap);
    }

    public Map<IRBasicBlock, BlockNode> getReverseBlockMap() {
        if (reversedBlockMap.isEmpty()) {
            build();
        }
        return reversedBlockMap;
    }

    public BlockNode re(IRBasicBlock bb) {
        return getReverseBlockMap().get(bb);
    }

    public void buildLiveInOut() {
        BlockNode.BuildLiveInOut(root);
    }

    public List<BlockNode> getDfo() {
        if (dfo == null)
            dfo = new DeepFirstOrder<>(root, root.size).getOrder();
        return dfo;
    }

    public Map<BlockNode, BlockNode> getIdomMap() {
        if (idomMap == null)
            idomMap = new SFDA<>(getDfo()).getIDom();
        return idomMap;
    }

    public Map<BlockNode, Set<BlockNode>> getDf() {
        if (df == null)
            df = new EmptyMapSet<>(new DF<>(getDfo(), getIdomMap()).getDF());
        return df;
    }

    public Map<BlockNode, Set<BlockNode>> getDfPlus() {
        var df = getDf();
        var dfPlus = new HashMap<BlockNode, Set<BlockNode>>();
        dfsDfPlus(root, df, dfPlus);
        return new EmptyMapSet<>(dfPlus);
    }

    private void dfsDfPlus(BlockNode cur, Map<BlockNode, Set<BlockNode>> df, Map<BlockNode, Set<BlockNode>> dfPlus) {
        // deep first search, post order.
        if (cur == null) return;
        if (!df.containsKey(cur)) return;
        dfPlus.putIfAbsent(cur, new HashSet<>());
        for (var s : df.get(cur)) {
            dfsDfPlus(s, df, dfPlus);
            dfPlus.get(cur).addAll(dfPlus.get(s));
        }
        dfPlus.get(cur).add(cur);
    }
}
