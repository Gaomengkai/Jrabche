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

package icu.merky.jrabche.llvmir.structures.impl;

import icu.merky.jrabche.llvmir.inst.IRInst;
import icu.merky.jrabche.llvmir.structures.IRBasicBlock;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class IRBasicBlockImpl implements IRBasicBlock {
    private String name;
    private final LinkedList<IRInst> instList;
    private final List<IRBasicBlock> prevs;
    private final List<IRBasicBlock> nexts;
    private boolean relationsWasBuilt;

    public IRBasicBlockImpl() {
        instList = new LinkedList<>();
        this.prevs = new ArrayList<>();
        this.nexts = new ArrayList<>();
    }

    @Override
    public int addInst(IRInst inst) {
        instList.add(inst);
        return instList.size() - 1;
    }

    @Override
    public int addInst(int position, IRInst inst) {
        instList.add(position, inst);
        return position;
    }

    @Override
    public void addInstFront(IRInst inst) {
        instList.addFirst(inst);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public List<IRInst> getInst() {
        return this.instList;
    }

    @Override
    public IRInst getTerminator() {
        if (!this.checkTerminator()) return null;
        return this.instList.getLast();
    }

    @Override
    public boolean relationsBuilt() {
        return this.relationsWasBuilt;
    }

    @Override
    public void setRelationBuild(boolean val) {
        this.relationsWasBuilt = val;
    }

    @Override
    public List<IRBasicBlock> getPrev() {
        return this.prevs;
    }

    @Override
    public List<IRBasicBlock> getNext() {
        return this.nexts;
    }

    @Override
    public void addPrev(IRBasicBlock prev) {
        this.prevs.add(prev);
    }

    @Override
    public void addNext(IRBasicBlock next) {
        this.nexts.add(next);
    }

    @Override
    public boolean checkTerminator() {
        if (this.instList.size() < 1) return false;
        return instList.getLast().isTerminatorInst();
    }

    @Override
    public String toString() {
        StringBuilder sb =new StringBuilder();
        sb.append(name).append(":\n");
        for (IRInst inst : instList) {
            sb.append("\t").append(inst.toString()).append("\n");
        }
        return sb.toString();
    }
}
