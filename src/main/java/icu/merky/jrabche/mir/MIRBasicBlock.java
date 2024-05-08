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

package icu.merky.jrabche.mir;

import icu.merky.jrabche.mir.inst.MIRInst;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class MIRBasicBlock {
    MIRFunction parent;
    String name;

    LinkedList<MIRInst> insts;

    Set<MIRBasicBlock> successors = new HashSet<>();
    Set<MIRBasicBlock> predecessors = new HashSet<>();

    public MIRBasicBlock(MIRFunction parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    void addInst(MIRInst inst) {
        insts.add(inst);
    }

    void addInstFirst(MIRInst inst) {
        insts.addFirst(inst);
    }

    int getSize() {
        return insts.size();
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    LinkedList<MIRInst> getInsts() {
        return insts;
    }

    Set<MIRBasicBlock> getSuccessors() {
        return successors;
    }

    Set<MIRBasicBlock> getPredecessors() {
        return predecessors;
    }

    void setSuccessors(Set<MIRBasicBlock> successors) {
        this.successors = successors;
    }

    void setPredecessors(Set<MIRBasicBlock> predecessors) {
        this.predecessors = predecessors;
    }

    void addSuccessor(MIRBasicBlock successor) {
        successors.add(successor);
    }

    void addPredecessor(MIRBasicBlock predecessor) {
        predecessors.add(predecessor);
    }

    public MIRFunction getParent() {
        return parent;
    }

    public void setParent(MIRFunction parent) {
        this.parent = parent;
    }
}
