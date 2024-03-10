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

import icu.merky.jrabche.exceptions.CompileException;
import icu.merky.jrabche.llvmir.inst.*;
import icu.merky.jrabche.llvmir.structures.IRModule;
import icu.merky.jrabche.llvmir.support.IRCopyRefData;
import icu.merky.jrabche.llvmir.types.IRType;

import java.util.ArrayList;

public class Lowering {
    IRModule irModule;
    MIRModule mirModule;
    LoweringContext C = new LoweringContext();
    ExitingSSAContext E = new ExitingSSAContext();

    public Lowering(IRModule irModule) {
        this.irModule = irModule;
        this.mirModule = new MIRModule();
        C.thisModule = mirModule;
    }

    public void exitSSA() {
        for (var F : irModule.getFunctions().values()) {
        }
    }

    MIRBasicType typeLowering(IRType irType) {
        if (irType.isFloat()) return MIRBasicType.Float;
        if (irType.isI32()) return MIRBasicType.Int32;
        if (irType.isI1()) return MIRBasicType.Bool;
        if (irType.isPointer()) return MIRBasicType.Ptr;
        if (irType.isArray()) return MIRBasicType.Ptr;
        throw new RuntimeException("Unknown type" + irType);
    }

    public void go() {
        for (var irFunction : irModule.getFunctions().values()) {
            var mirFunction = new MIRFunction();
            mirFunction.name = irFunction.getName();
            mirFunction.parent = mirModule;
            mirFunction.paramTypes = irFunction.getFunctionType().getParamsType().stream()
                    .map(this::typeLowering).collect(java.util.stream.Collectors.toCollection(ArrayList::new));
            C.thisFunction = mirFunction;
            for (var irBlock : irFunction.getBlocks()) {
                var mirBlock = new MIRBasicBlock(C.thisFunction, irBlock.getName());
                mirBlock.name = irBlock.getName();
                mirBlock.parent = mirFunction;
                C.thisBlock = mirBlock;
                C.blockIter = irBlock.getInsts().iterator();
                while (C.blockIter.hasNext()) {
                    var irInst = C.blockIter.next();
                    dispatchInst(C, irInst);
                }
            }
        }
    }

    private void dispatchInst(LoweringContext C, IRInst irInst) {
        switch (irInst.getInstID()) {
            case AllocaInst -> handleAllocaInst(C, (IRInstAlloca) irInst);
            case ReturnInst -> handleReturnInst(C, (IRInstReturn) irInst);
            case BrInst -> handleBrInst(C, (IRInstBr) irInst);
            case MathInst -> handleMathInst(C, (IRInstMath) irInst);
            case ICmpInst -> handleIcmpInst(C, (IRInstIcmp) irInst);
            case FCmpInst -> handleFCmpInst(C, (IRInstFcmp) irInst);
            case LoadInst -> handleLoadInst(C, (IRInstLoad) irInst);
            case StoreInst -> handleStoreInst(C, (IRInstStore) irInst);
            case BitCastInst -> handleBitCastInst(C, (IRInstBitCast) irInst);
            case GetElementPtrInst -> handleGetElementPtrInst(C, (IRInstGEP) irInst);
            case UnaryInst -> handleConvertInst(C, (IRInstUnary) irInst);
            case CallInst -> handleCallInst(C, (IRInstCall) irInst);
            case PhiInst -> handlePhiInst(C, (IRInstPhi) irInst);

        }

    }

    private void handlePhiInst(LoweringContext c, IRInstPhi irInst) {

    }

    private void handleCallInst(LoweringContext c, IRInstCall irInst) {

    }

    private void handleConvertInst(LoweringContext c, IRInstUnary irInst) {
    }

    private void handleGetElementPtrInst(LoweringContext c, IRInstGEP irInst) {

    }

    private void handleBitCastInst(LoweringContext c, IRInstBitCast irInst) {

    }

    private void handleStoreInst(LoweringContext c, IRInstStore irInst) {

    }

    private void handleLoadInst(LoweringContext c, IRInstLoad irInst) {

    }

    private void handleFCmpInst(LoweringContext c, IRInstFcmp irInst) {
    }

    private void handleIcmpInst(LoweringContext c, IRInstIcmp irInst) {
    }

    private void handleMathInst(LoweringContext c, IRInstMath irInst) {
        var op = irInst.getMathOP();
        boolean f = irInst.getType().isFloat();
        switch (op) {
            case Invalid -> {
                throw new CompileException("Fuck! Invalid Math OP");
            }
            case Add -> {
            }
            case Sub -> {
            }
            case Mul -> {
            }
            case Div -> {
            }
            case Rem -> {
            }
            case Shl -> {
            }
            case Shr -> {
            }
            case And -> {
            }
            case Or -> {
            }
            case Xor -> {
            }
        }
    }

    private void handleBrInst(LoweringContext c, IRInstBr irInst) {

    }

    private void handleReturnInst(LoweringContext c, IRInstReturn irInst) {
    }

    private void handleAllocaInst(LoweringContext c, IRInstAlloca irInst) {
    }

    private static class ExitingSSAContext {
        public IRCopyRefData ref;
    }
}

