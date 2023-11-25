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

import icu.merky.jrabche.llvmir.structures.IRFunction;
import icu.merky.jrabche.opt.llvmir.algorithms.BlockNode;
import icu.merky.jrabche.opt.llvmir.algorithms.BlockNodeBuilder;
import icu.merky.jrabche.opt.llvmir.annotations.DisabledOpt;
import icu.merky.jrabche.opt.llvmir.annotations.OptOn;

import java.util.*;

/**
 * Redundant Store Elimination<br>
 * if a single store DOMINATES other loads, then the store is redundant.
 */
@OptOn(value = OptOn.OptOnEnum.Function, ssa = false, name = "Redundant Load Store Elimination 2", afterWhich = {IROptRLSE.class})
@DisabledOpt
@Deprecated
public class IROptRLSE2 implements IRPass {
    IRFunction F;

    public IROptRLSE2(IRFunction function) {
        F = function;
    }

    @Override
    public boolean go() {
        // get dom tree
        BlockNodeBuilder bnb = new BlockNodeBuilder(F);
        bnb.build();

        BlockNode root = bnb.getRoot();
        int sizeBBs = F.getBlocks().size();
        List<BlockNode> dfo = bnb.getDfo();
        Map<BlockNode, BlockNode> idomMap = bnb.getIdomMap();
        Map<BlockNode, Set<BlockNode>> reversedIdomMap = new HashMap<>();
        idomMap.forEach((k, v) -> {
            reversedIdomMap.putIfAbsent(v, new HashSet<>());
            reversedIdomMap.get(v).add(k);
        });
        Map<BlockNode, Set<BlockNode>> domSet = new HashMap<>();
        // dfs idomMap to build domTree(domSet)
        dfs(root, reversedIdomMap, domSet);


        return false;
    }

    private void dfs(BlockNode root, Map<BlockNode, Set<BlockNode>> reversedIdomMap, Map<BlockNode, Set<BlockNode>> domSet) {
        if (!reversedIdomMap.containsKey(root)) return;
        // 后序遍历
        for (BlockNode child : reversedIdomMap.get(root)) {
            if (child != root) dfs(child, reversedIdomMap, domSet);
        }
        // 合并
        Set<BlockNode> dom = new HashSet<>();
        dom.add(root);
        for (BlockNode child : reversedIdomMap.get(root)) {
            if (child != null) dom.add(child);
            if (child != root && domSet.containsKey(child)) dom.addAll(domSet.get(child));
        }
        domSet.put(root, dom);
    }
}
