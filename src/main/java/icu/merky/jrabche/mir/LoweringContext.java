/*
 * BSD 3-Clause License
 *
 * Copyright (c) 2023-2024, Gaomengkai
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

import icu.merky.jrabche.exceptions.NotImplementedException;
import icu.merky.jrabche.llvmir.inst.IRInst;
import icu.merky.jrabche.llvmir.structures.IRBasicBlock;
import icu.merky.jrabche.llvmir.structures.IRFunction;
import icu.merky.jrabche.llvmir.types.FunctionType;
import icu.merky.jrabche.llvmir.types.IRType;
import icu.merky.jrabche.llvmir.values.IRVal;
import icu.merky.jrabche.llvmir.values.IRValFP;
import icu.merky.jrabche.mir.inst.MIRInst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static icu.merky.jrabche.mir.MIRReg.NewReg;

public final class LoweringContext {
    public Iterator<IRInst> blockIter;
    MIRModule thisModule;
    MIRFunction thisFunction;
    MIRBasicBlock thisBlock;
    Map<IRBasicBlock, MIRBasicBlock> blockMap;
    Map<IRVal, MIRValue> valueMap;
    int regId = 10000;

    public int nextRegId() {
        regId++;
        return regId;
    }

    public void emit(MIRInst inst) {
        thisBlock.insts.add(inst);
    }

    public void emitCopy(MIRValue dst, MIRValue src) {
    }

    void registerBlock(IRBasicBlock irBasicBlock) {
        thisBlock = new MIRBasicBlock(thisFunction, irBasicBlock.getName());
        blockMap.put(irBasicBlock, thisBlock);
    }

    void diveIntoBlock(IRBasicBlock irBasicBlock) {
        thisBlock = blockMap.get(irBasicBlock);
    }

    static MIRBasicType typeLowering(IRType irType) {
        if (irType.isFloat()) return MIRBasicType.Float;
        if (irType.isI32()) return MIRBasicType.Int32;
        if (irType.isI1()) return MIRBasicType.Bool;
        if (irType.isPointer()) return MIRBasicType.Ptr;
        if (irType.isArray()) return MIRBasicType.Ptr;
        throw new RuntimeException("Unknown type" + irType);
    }

    void registerFunction(IRFunction irFunction) {
        var mirFunction = new MIRFunction();
        mirFunction.name = irFunction.getName();
        mirFunction.parent = thisModule;
        mirFunction.paramTypes = irFunction.getFunctionType().getParamsType().stream()
                .map(LoweringContext::typeLowering).collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        mirFunction.regs = new HashMap<>();

        thisFunction = mirFunction;
    }


    void buildMIRBlockMap(IRFunction irFunction) {
        for (IRBasicBlock irBasicBlock : irFunction.getBlocks()) {
            var mirBB = new MIRBasicBlock(thisFunction, irBasicBlock.getName());
            this.blockMap.put(irBasicBlock, mirBB);
        }
    }

    /**
     * Mapping ir value towards mir values in such a way which builds a new MIR value type in mir function.
     *
     * @param irFunction LLVM IR Function
     */
    public void buildIRVMap(IRFunction irFunction) {
        valueMap = new HashMap<>();
        // args
        var args = irFunction.getFp().values();
        for (IRValFP arg : args) {
            var mirType = typeLowering(arg.getType());
            var mirReg = NewReg(nextRegId(), mirType);
            valueMap.put(arg, MIRValue.newReg(mirReg));
        }
        // blocks
        for (IRBasicBlock block : irFunction.getBlocks()) {
            for (IRInst inst : block.getInsts()) {
                // inst self
                valueMap.put(inst, MIRValue.newReg(NewReg(nextRegId(), typeLowering(inst.getType()))));
            }
        }
    }

    public void registerGlobals(Map<String, IRVal> globals) {
        throw new NotImplementedException();
    }

    public void registerFuncDecls(Map<String, FunctionType> funcDecls) {
        throw new NotImplementedException();
    }
}
