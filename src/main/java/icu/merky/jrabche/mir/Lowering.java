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
import icu.merky.jrabche.llvmir.structures.IRBasicBlock;
import icu.merky.jrabche.llvmir.structures.IRModule;
import icu.merky.jrabche.llvmir.support.IRCopyRefData;
import icu.merky.jrabche.llvmir.values.IRVal;
import org.antlr.v4.runtime.misc.Pair;

import java.util.ArrayList;
import java.util.Map;

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
    }



    public void go() {
        var globals = irModule.getGlobals();
        var funcDecls = irModule.getFunctionDeclarations();
        C.registerGlobals(globals);
        C.registerFuncDecls(funcDecls);
        for (var irFunction : irModule.getFunctions().values()) {
            // dive into an atarashii function
            C.registerFunction(irFunction);

            // build mir block map
            C.buildMIRBlockMap(irFunction);

            // build ir value map
            C.buildIRVMap(irFunction);

            for (var irBlock : irFunction.getBlocks()) {
                // dive into an existed mir block
                C.diveIntoBlock(irBlock);

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
            default -> throw new CompileException("Fail to dispatch inst of xxx in selection matching MIR inst");
        }

    }

    private void handlePhiInst(LoweringContext c, IRInstPhi I) {
        var incoming = I.getIncoming();
        ArrayList<Pair<MIRBasicBlock, MIRValue>> mirIncoming = new ArrayList<>();
        for (Map.Entry<IRBasicBlock, IRVal> entry : incoming.entrySet()) {

            var irBB = entry.getKey();
            var mirBB = C.blockMap.get(irBB);

            IRVal value = entry.getValue();
            // mirIncoming.add(new Pair<>(mirBB, ))
        }
    }

    private void handleCallInst(LoweringContext c, IRInstCall I) {

    }

    private void handleConvertInst(LoweringContext c, IRInstUnary I) {
    }

    private void handleGetElementPtrInst(LoweringContext c, IRInstGEP I) {

    }

    private void handleBitCastInst(LoweringContext c, IRInstBitCast I) {

    }

    private void handleStoreInst(LoweringContext c, IRInstStore I) {

    }

    private void handleLoadInst(LoweringContext c, IRInstLoad I) {

    }

    private void handleFCmpInst(LoweringContext c, IRInstFcmp I) {
    }

    private void handleIcmpInst(LoweringContext c, IRInstIcmp I) {
    }

    private void handleMathInst(LoweringContext c, IRInstMath I) {
        var op = I.getMathOP();
        boolean f = I.getType().isFloat();
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

    private void handleBrInst(LoweringContext c, IRInstBr I) {

    }

    private void handleReturnInst(LoweringContext c, IRInstReturn I) {
    }

    private void handleAllocaInst(LoweringContext c, IRInstAlloca I) {
    }

    private static class ExitingSSAContext {
        public IRCopyRefData ref;
    }
}

