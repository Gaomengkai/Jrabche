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
import icu.merky.jrabche.llvmir.inst.IRInstAlloca;
import icu.merky.jrabche.llvmir.inst.IRInstLoad;
import icu.merky.jrabche.llvmir.inst.IRInstStore;
import icu.merky.jrabche.llvmir.structures.IRBasicBlock;
import icu.merky.jrabche.llvmir.structures.IRFunction;
import icu.merky.jrabche.llvmir.values.IRVal;
import icu.merky.jrabche.opt.llvmir.annotations.OptOn;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static icu.merky.jrabche.logger.JrabcheLogger.JL;

/**
 * Redundant Load Store Elimination
 */
@OptOn(value = OptOn.OptOnEnum.Function, name = "Redundant Load Store Elimination", ssa = false)
public class IROptRLSE implements IRPass {
    private final IRFunction F;

    public IROptRLSE(IRFunction function) {
        this.F = function;
    }

    @Override
    public boolean go() {
        boolean changed = false;
        for (var B : F.getBlocks()) {
            changed |= goOnBB(B);
        }
        new BuildUses(F).go();
        changed |= goOnRemoveStore();
        return changed;
    }

    private boolean goOnRemoveStore() {
        boolean changed = false;
        for (var B : F.getBlocks()) {
            for (var I : B.getInsts()) {
                if (I instanceof IRInstAlloca) {
                    // zero use, delete.
                    if (I.getUsedBy().isEmpty()) {
                        JL.Debug("RLSE: delete alloca " + I);
                        I.setDeleted();
                        changed = true;
                    }
                    // only store, delete.
                    if (I.getUsedBy().stream().allMatch(inst -> inst instanceof IRInstStore)) {
                        I.getUsedBy().forEach(inst -> {
                            JL.Debug("RLSE: delete  " + inst);
                            inst.setDeleted();
                        });
                        JL.Debug("RLSE: delete alloca " + I);
                        I.setDeleted();
                        changed = true;
                    }
                }
            }
            B.getInsts().removeIf(IRInst::isDeleted);
        }
        return changed;
    }

    public boolean goOnBB(IRBasicBlock B) {
        // stage 1: remove redundant load.
        Map<IRInstAlloca, IRVal> defs = new HashMap<>();
        Map<IRInstLoad, IRVal> loadReplaceMap = new HashMap<>();
        Map<IRInstAlloca, Set<IRInstLoad>> loadMap = new HashMap<>();
        for (var I : B.getInsts()) {
            if (I instanceof IRInstStore store) {
                // store self
                for (var op : I.getUses()) {
                    if (op instanceof IRInstLoad load) {
                        I.replace(op, loadReplaceMap.getOrDefault(load, op));
                    }
                }
                // kill and gen
                if (store.getTo() instanceof IRInstAlloca alloca) {
                    // kill all load
                    if (loadMap.containsKey(alloca)) {
                        for (var load : loadMap.get(alloca)) {
                            loadReplaceMap.remove(load);
                        }
                    }
                    defs.put(alloca, store.getFrom());
                }
            } else if (I instanceof IRInstLoad load) {
                if (load.getFrom() instanceof IRInstAlloca alloca) {
                    IRVal v = defs.getOrDefault(alloca, null);
                    if (v != null) {
                        loadReplaceMap.put(load, v);
                        loadMap.putIfAbsent(alloca, new HashSet<>());
                        loadMap.get(alloca).add(load);
                    }
                }
            } else {
                // fast path
                if (loadReplaceMap.isEmpty()) {
                    continue;
                }
                // other inst
                for (var op : I.getUses()) {
                    if (op instanceof IRInstLoad load) {
                        I.replace(op, loadReplaceMap.getOrDefault(load, op));
                    }
                }
            }
        }
        boolean changedInner = true;
        boolean changedOuter = false;
        while (changedInner) {
            changedInner = false;
            Set<IRInstLoad> usedLoads = new HashSet<>();
            for (var I : B.getInsts()) {
                for (var U : I.getUses()) {
                    if (U instanceof IRInstLoad load) {
                        usedLoads.add(load);
                    }
                }
            }
            for (var I : B.getInsts()) {
                if (I instanceof IRInstLoad load) {
                    if (!usedLoads.contains(load)) {
                        JL.Debug("RLSE: delete load   " + load);
                        load.setDeleted();
                        changedInner = true;
                        changedOuter = true;
                    }
                }
            }
            // true delete.
            B.getInsts().removeIf(IRInst::isDeleted);
        }

        // stage2: remove redundant store.
        // because we have removed redundant load, so we can remove redundant store.
        Map<IRInstAlloca, IRInstStore> storeMap = new HashMap<>();
        for (var I : B.getInsts()) {
            if (I instanceof IRInstStore store) {
                if (store.getTo() instanceof IRInstAlloca alloca) {
                    IRInstStore oldStore = storeMap.getOrDefault(alloca, null);
                    if (oldStore != null) {
                        JL.Debug("RLSE: delete store  " + oldStore);
                        oldStore.setDeleted();
                        changedOuter = true;
                    }
                    storeMap.put(alloca, store);
                }
            }
        }
        B.getInsts().removeIf(IRInst::isDeleted);
        return changedOuter;
    }
}
