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
import icu.merky.jrabche.llvmir.structures.IRFunction;
import icu.merky.jrabche.opt.llvmir.annotations.DisabledOpt;
import icu.merky.jrabche.opt.llvmir.annotations.OptOn;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

@OptOn(value = OptOn.OptOnEnum.Function, name = "Dead Code Elimination v1", ssa = false, afterWhich = {IROptRLSE.class})
@DisabledOpt
public class IROptDCE implements IROpt {

    private final IRFunction F;

    public IROptDCE(IRFunction f) {
        F = f;
    }

    @Override
    public boolean go() {
        // optimized for scala alloca, temp reg, etc.
        // not optimized for array, struct, etc.
        Set<IRInstAlloca> allocaSet = new HashSet<>();
        for (var I : F.entryBB().getInsts()) {
            if (I instanceof IRInstAlloca) {
                allocaSet.add((IRInstAlloca) I);
            } else break;
        }
        Set<IRInst> sideEff = new HashSet<>();
        for (var B : F.getBlocks()) {
            for (var I : B.getInsts()) {
                if (I.isCallInst() || I.isBrInst() || I.isReturnInst()) {
                    for (var U : I.getUses()) {
                        if (U instanceof IRInst inst) sideEff.add(inst);
                    }
                }
            }
        }
        BitSet live = new BitSet();
        int i = 0;

        return false;
    }
}
