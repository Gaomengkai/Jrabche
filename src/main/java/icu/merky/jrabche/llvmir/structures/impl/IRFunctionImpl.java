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

import icu.merky.jrabche.exceptions.NotImplementedException;
import icu.merky.jrabche.fe.visitor.FPType;
import icu.merky.jrabche.llvmir.inst.IRInst;
import icu.merky.jrabche.llvmir.inst.IRInstAlloca;
import icu.merky.jrabche.llvmir.inst.IRInstReturn;
import icu.merky.jrabche.llvmir.structures.IRBasicBlock;
import icu.merky.jrabche.llvmir.structures.IRFunction;
import icu.merky.jrabche.llvmir.types.FunctionType;
import icu.merky.jrabche.llvmir.types.IRBasicType;
import icu.merky.jrabche.llvmir.types.IRType;
import icu.merky.jrabche.llvmir.values.IRValConst;
import icu.merky.jrabche.llvmir.values.IRValFP;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class IRFunctionImpl implements IRFunction {
    FunctionType functionType;
    String name;
    List<IRBasicBlock> bbs;
    IRBasicBlock curBB;
    IRBasicBlock entryBB;
    List<IRInstAlloca> alloca;
    /**
     * Function parameters. Cannot store ANY value.
     * Just a placeholder to store the function parameter types.
     * To match the LLVM IR `store` instruction, we use IRVal here.
     */
    Map<String, IRValFP> fp = new LinkedHashMap<>();

    AtomicInteger counter = new AtomicInteger(0);

    public IRFunctionImpl(String name, FunctionType functionType) {
        this.functionType = functionType;
        entryBB = new IRBasicBlockImpl();
        entryBB.setName("entry");
        this.bbs = new ArrayList<>();
        this.alloca = new ArrayList<>();
        bbs.add(entryBB);
        curBB = entryBB;
        this.name = name;
    }

    @Override
    public FunctionType getFunctionType() {
        return functionType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void addParam(String name, IRType type) {
        throw new NotImplementedException();
    }

    public void addFPs(List<FPType> fps) {
        for (FPType fpType : fps) {
            fp.put(fpType.name(), new IRValFP(fpType.type()));
        }
    }

    @Override
    public IRValFP addFP(FPType ty) {
        IRValFP value = new IRValFP(ty.type());
        fp.put(ty.name(), value);
        return value;
    }

    @Override
    public IRBasicBlock addBlock() {
        IRBasicBlock newBB = new IRBasicBlockImpl();
        bbs.add(newBB);
        return newBB;
    }

    @Override
    public void setCurrentBlock(String name) {
        for (IRBasicBlock bb : bbs) {
            if (bb.getName().equals(name)) {
                curBB = bb;
                return;
            }
        }
        throw new RuntimeException("No such block");
    }

    @Override
    public void setCurrentBlock(IRBasicBlock block) {
        for (IRBasicBlock bb : bbs) {
            if (bb == block) {
                curBB = bb;
                return;
            }
        }
        throw new RuntimeException("No such block");
    }

    @Override
    public IRBasicBlock curBB() {
        return curBB;
    }

    @Override
    public IRBasicBlock entryBB() {
        return entryBB;
    }

    @Override
    public IRBasicBlock getBlock(String name) {
        for (IRBasicBlock bb : bbs) {
            if (bb.getName().equals(name)) {
                return bb;
            }
        }
        throw new RuntimeException("No such block");
    }

    @Override
    public List<IRBasicBlock> getBlocks() {
        return bbs;
    }

    @Override
    public IRInstAlloca addAlloca(IRInstAlloca inst) {
        alloca.add(inst);
        return inst;
    }

    @Override
    public List<IRInstAlloca> getAlloca() {
        return alloca;
    }

    @Override
    public void finishFunction() {
        // add alloca to entry block
        for (int i = alloca.size() - 1; i >= 0; i--) {
            entryBB.addInstFront(alloca.get(i));
        }
        alloca.clear();
        // rename variables.

        // 1. fp
        for (Map.Entry<String, IRValFP> entry : fp.entrySet()) {
            int count = counter.getAndIncrement();
            entry.getValue().setName("a." + count);
        }
        // 2. unnamed
        for (IRBasicBlock bb : bbs) {
            for (IRInst irInst : bb.getInst()) {
                if (irInst.getName() == null && irInst.needName()) {
                    int count = counter.getAndIncrement();
                    irInst.setName("v." + count);
                }
            }
        }
        // 3. check ret
        for (IRBasicBlock bb : bbs) {
            if ((bb.getTerminator() == null)) {
                if (this.getFunctionType().getRetType().getBasicType() != IRBasicType.VOID) {
                    var bType = this.getFunctionType().getRetType().toBasicType();
                    bb.addInst(new IRInstReturn(bType, IRValConst.Zero(bType)));
                } else
                    bb.addInst(new IRInstReturn(IRBasicType.VOID, null));
            }
        }
        // 4. chunk bb
        for (IRBasicBlock bb : bbs) {
            bb.chunkAfterTerminator();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("define ").append(functionType.getRetType().toString()).append(" @").append(name).append("(");
        boolean first = true;
        for (Map.Entry<String, IRValFP> entry : fp.entrySet()) {
            if (!first) sb.append(", ");
            first = false;
            sb.append(entry.getValue().getType().toString()).append(" ").append(entry.getValue().getName());
        }
        sb.append(") {\n");
        bbs.stream().map(Object::toString).forEach(sb::append);
        sb.append("}\n");
        return sb.toString();
    }
}
