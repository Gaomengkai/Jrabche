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

package icu.merky.jrabche.llvmir.inst;

import icu.merky.jrabche.llvmir.structures.IRBasicBlock;
import icu.merky.jrabche.llvmir.types.IRType;
import icu.merky.jrabche.llvmir.values.IRVal;
import org.antlr.v4.runtime.misc.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class IRInstPhi extends IRInst {
    // List<Pair<IRVal, IRBasicBlock>> incoming;

    Map<IRBasicBlock, IRVal> incoming;

    public IRInstPhi(IRType valType) {
        super(InstID.PhiInst, valType);
        incoming = new HashMap<>();
    }

    public void addIncoming(IRVal val, IRBasicBlock block) {
        incoming.put(block, val);
    }

    public void addIncoming(Pair<IRVal, IRBasicBlock> pair) {
        incoming.put(pair.b, pair.a);
    }


    public Map<IRBasicBlock, IRVal> getIncoming() {
        return incoming;
    }

    @Override
    public String toString() {
        // %v1 = PHI i32 [ 0, %entry ], [ %v2, %if.then ]
        StringBuilder sb = new StringBuilder();
        if (name != null) sb.append(name).append(" = ");
        sb.append("phi ").append(type.toString()).append(" ");
        int i = 0;
        for (var entry : incoming.entrySet()) {
            sb.append("[ ").append(entry.getValue().asValue()).append(", ").append("%").append(entry.getKey().getName()).append(" ]");
            if (i != incoming.size() - 1) {
                sb.append(", ");
            }
            i++;
        }
        sb.delete(sb.length(), sb.length());
        return sb.toString();
    }

    @Override
    public Set<IRVal> getUses() {
        // todo.
        return Set.of();
    }

    @Override
    public boolean replace(IRVal inst, IRVal newInst) {
        return false;
    }

    @Override
    public String asValue() {
        return name;
    }
}
