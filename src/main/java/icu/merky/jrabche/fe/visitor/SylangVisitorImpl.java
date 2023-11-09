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

package icu.merky.jrabche.fe.visitor;

import icu.merky.jrabche.exceptions.CompileException;
import icu.merky.jrabche.exceptions.NotImplementedException;
import icu.merky.jrabche.fe.parser.SylangParser;
import icu.merky.jrabche.fe.parser.SylangVisitor;
import icu.merky.jrabche.helper.ConstInitList;
import icu.merky.jrabche.helper.Helper;
import icu.merky.jrabche.llvmir.IRBuilder;
import icu.merky.jrabche.llvmir.inst.*;
import icu.merky.jrabche.llvmir.structures.IRBasicBlock;
import icu.merky.jrabche.llvmir.structures.impl.IRFunctionImpl;
import icu.merky.jrabche.llvmir.types.*;
import icu.merky.jrabche.llvmir.values.*;
import icu.merky.jrabche.utils.Finished;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static icu.merky.jrabche.helper.Helper.*;

public class SylangVisitorImpl extends AbstractParseTreeVisitor<Void> implements SylangVisitor<Void> {
    public final VisitorContext C;

    public SylangVisitorImpl(IRBuilder builder) {
        this.C = new VisitorContext(builder);
    }

    /**
     * Visit a parse tree produced by {@link SylangParser#compUnit}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitCompUnit(SylangParser.CompUnitContext ctx) {
        visitChildren(ctx);
        return null;
    }

    /**
     * Visit a parse tree produced by {@link SylangParser#compUnitItem}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitCompUnitItem(SylangParser.CompUnitItemContext ctx) {
        visitChildren(ctx);
        return null;
    }

    /**
     * Visit a parse tree produced by {@link SylangParser#decl}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitDecl(SylangParser.DeclContext ctx) {
        visitChildren(ctx);
        return null;
    }

    /**
     * Visit a parse tree produced by {@link SylangParser#constDecl}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitConstDecl(SylangParser.ConstDeclContext ctx) {
        // constDecl: Const bType constDef (Comma constDef)* Semicolon;
        C.isConst.dive(true);
        ctx.bType().accept(this);
        for (SylangParser.ConstDefContext constDefContext : ctx.constDef()) {
            constDefContext.accept(this);
        }
        C.isConst.ascend();
        return null;
    }

    /**
     * Visit a parse tree produced by the {@code int}
     * labeled alternative in {@link SylangParser#bType}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitInt(SylangParser.IntContext ctx) {
        C.bType = BType.INT;
        return null;
    }

    /**
     * Visit a parse tree produced by the {@code float}
     * labeled alternative in {@link SylangParser#bType}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitFloat(SylangParser.FloatContext ctx) {
        C.bType = BType.FLOAT;
        return null;
    }

    /**
     * Visit a parse tree produced by {@link SylangParser#constDef}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitConstDef(SylangParser.ConstDefContext ctx) {
        // constDef: Ident (Lbracket exp Rbracket)* Assign initVal;
        var atomType = C.bType.toBasicType();
        var name = ctx.Ident().getText();
        if (C.query(name) != null) {
            throw new CompileException("redefinition of variable `" + name + "`", ctx.Ident());
        }
        ctx.initVal().accept(this);
        var initVal = C.lastVal;

        var shape = ctx.exp().stream().collect(ArrayList<Integer>::new, (list, exp) -> {
            exp.accept(this);
            list.add(GetIntNumFromCVal((IRValConst) C.lastVal));
        }, ArrayList::addAll);

        if (shape.isEmpty()) {
            // scalar
            IRValConst converted = Helper.DoCompileTimeConversion(atomType, initVal);
            C.pushConst(name, converted);
        } else {
            // array
            ArrayType arrayType = ArrayType.FromShape(atomType, shape);
            ConstInitList il = (ConstInitList) initVal;
            IRValConstArray carr = IRValConstArray.FromInitList(il, arrayType);
            C.pushConst(name, carr);
        }
        return null;
    }

    /**
     * Visit a parse tree produced by {@link SylangParser#varDecl}.
     * <p>
     * <code>varDecl: bType varDef (Comma varDef)* Semicolon;</code>
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitVarDecl(SylangParser.VarDeclContext ctx) {
        // varDecl: bType varDef (Comma varDef)* Semicolon;
        ctx.bType().accept(this);
        ctx.varDef().forEach(a -> a.accept(this));
        return null;
    }

    /**
     * Visit a parse tree produced by {@link SylangParser#varDef}.
     * <code>varDef: Ident (Lbracket exp Rbracket)* (Assign initVal)?;</code>
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Void visitVarDef(SylangParser.VarDefContext ctx) {
        // varDef: Ident (Lbracket exp Rbracket)* (Assign initVal)?;
        IRBasicType atomType = C.bType.toBasicType();
        String name = ctx.Ident().getText();
        List<Integer> shape = getShapeFromConstExp(ctx.exp());
        // global, must be a const.
        if (C.lc.inGlobal())
            globalVarDef(ctx, atomType, name, shape);
            // local var, may not be all const.
        else {
            String renamed = name;
            if (C.lc.cur.valSymbolTable.get(name) != null) {
                throw new CompileException("redefinition of variable `" + name + "`", ctx.Ident());
            }
            if (C.query(name) != null) {
                renamed = C.getNextRepeatName(name);
            }
            if (shape.isEmpty()) {
                // local var, scalar
                var alloca = C.addAlloca(new IRInstAlloca(renamed, atomType.toIRType()));
                if (ctx.initVal() != null) {
                    // local var, scalar with init
                    try (AutoDive ignored = new AutoDive(C.needLoad, true)) {
                        ctx.initVal().accept(this);
                        DoRuntimeConversion(C, atomType, C.lastVal);
                        C.addAndUpdate(new IRInstStore(C.lastVal, alloca));
                        C.pushVar(name, alloca);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                } else {
                    // local var, scalar without init
                    C.pushVar(name, alloca);
                }
            } else {
                // local var, array
                ArrayType arrayType = ArrayType.FromShape(atomType, shape);
                IRInstAlloca alloca = C.addAlloca(new IRInstAlloca(name, arrayType));
                if (ctx.initVal() != null) {
                    // local var, array with init
                    try (AutoDive ignored = new AutoDive(C.needLoad, true)) {
                        initCInitCtx(shape, arrayType, alloca);
                        C.pushVar(name, alloca);
                        CAddMemZero(C, arrayType.getSizeBytes(), alloca);
                        ctx.initVal().accept(this);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                } else {
                    // local var, array without init
                    C.pushVar(name, alloca);
                    // in sy def, local arr must init with 0.
                    // %v1 = bitcast [3 x i32]* %v0 to i32*
                    // call void @llvm.memset.p0.i32(i32* %v1, i8 0, i32 <size in bytes>, i1 false)
                    CAddMemZero(C, arrayType.getSizeBytes(), alloca);
                }
            }
        }

        return null;
    }

    /**
     * Initialize the context for array init.<br>
     * Must call before visit non-const array init.
     *
     * @param shape     shape of array
     * @param arrayType array type
     * @param varArray  array var
     */
    private void initCInitCtx(List<Integer> shape, ArrayType arrayType, IRVal varArray) {
        C.ic.curShape = new ArrayList<>(shape);
        C.ic.curArrayPos.clear();
        C.ic.curArrayPos.addAll(Collections.nCopies(C.ic.curShape.size(), 0));
        C.ic.curArrayDim = 0;
        C.ic.curArr = varArray;
        C.ic.curArrType = arrayType;
    }

    private void CAddMemZero(VisitorContext C, int sizeInBytes, IRVal ptr) {
        var zero8 = IRValConstInt.ZeroInt();
        zero8.setType(new IntType(8));
        C.addAndUpdate(new IRInstBitCast(ptr, new PointerType(new IntType())));
        C.addAndUpdate(new IRInstCall("llvm.memset.p0.i32",
                C.queryFunctionType("llvm.memset.p0.i32"),
                List.of(
                        C.lastVal,
                        zero8,
                        IRValConstInt.fromInt(sizeInBytes),
                        IRValConstInt.ZeroBool()
                )
        ));
    }

    @Finished
    private void globalVarDef(SylangParser.VarDefContext ctx, IRBasicType atomType, String name, List<Integer> shape) {
        if (C.query(name) != null) {
            throw new CompileException("redefinition of variable `" + name + "`", ctx.Ident());
        }
        try (AutoDive ignored = new AutoDive(C.isConst, true)) {
            if (shape.isEmpty()) {
                // scalar
                if (ctx.initVal() != null) {
                    ctx.initVal().accept(this);
                    IRValConst converted = Helper.DoCompileTimeConversion(atomType, C.lastVal);
                    C.pushVar(name, converted);
                } else {
                    C.pushVar(name, IRValConstInt.Zero(C.bType.toBasicType()));
                }
            } else {
                // array
                if (ctx.initVal() != null) {
                    ctx.initVal().accept(this);
                    ConstInitList il = (ConstInitList) C.lastVal;
                    IRValConstArray carr = IRValConstArray.FromInitList(il, ArrayType.FromShape(atomType, shape));
                    C.pushVar(name, new IRValArray(carr));
                } else {
                    // C.pushVar(name, new IRVarArray(ArrayType.FromShape(atomType, shape)));
                    C.pushVar(name, new IRValArray(ArrayType.FromShape(atomType,shape)));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Visit a parse tree produced by the {@code init}
     * labeled alternative in {@link SylangParser#initVal}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Void visitInitExp(SylangParser.InitExpContext ctx) {
        visitChildren(ctx);
        return null;
    }

    /**
     * Visit a parse tree produced by the {@code initList}
     * labeled alternative in {@link SylangParser#initVal}.<br>
     * <code>Lbrace (initVal (Comma initVal)*)? Rbrace	# initList;</code>
     * <br>did not check value type
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Void visitInitList(SylangParser.InitListContext ctx) {
        if (C.isConst.get()) {
            var il = new ConstInitList(C.bType.toBasicType());
            for (SylangParser.InitValContext initValContext : ctx.initVal()) {
                initValContext.accept(this);
                if (C.lastVal instanceof ConstInitList ilGot) {
                    il.addIL(ilGot);
                } else {
                    il.addCV((IRValConst) C.lastVal);
                }
            }
            C.lastVal = il;
        } else {
            var innerType = C.bType;
            for (SylangParser.InitValContext initValContext : ctx.initVal()) {
                int thisDimBefore = C.ic.curArrayPos.get(C.ic.curArrayDim);

                // $accept
                C.ic.curArrayDim++;
                initValContext.accept(this);
                C.ic.curArrayDim--;
                // $accept#

                IRVal lastVal = C.lastVal;
                if (lastVal.getType().isScalar()) {
                    DoRuntimeConversion(C, innerType.toBasicType(), lastVal);
                    List<IRVal> indices = new ArrayList<>();
                    indices.add(IRValConstInt.ZeroInt());
                    for (int i = 0; i < C.ic.curArrayPos.size(); i++) {
                        indices.add(IRValConstInt.fromInt(C.ic.curArrayPos.get(i)));
                    }
                    C.addAndUpdate(new IRInstGEP(C.ic.curArr, indices));
                    C.addAndUpdate(new IRInstStore(lastVal, C.lastVal));
                    ArrayPosPlusN(C.ic.curShape, C.ic.curArrayPos, 1);
                } else {
                    // this branch is InitList.
                    if (C.ic.curArrayPos.get(C.ic.curArrayDim) == thisDimBefore) {
                        ArrayPosPlusN(C.ic.curShape, C.ic.curArrayPos, 1, C.ic.curArrayDim);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Visit a parse tree produced by {@link SylangParser#funcDef}.
     * <code>funcDef: funcType Ident Lparen funcFParams? Rparen block;</code>
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Void visitFuncDef(SylangParser.FuncDefContext ctx) {
        ctx.funcType().accept(this);
        var funcRetType = C.bType.toBasicType().toIRType();
        String name = ctx.Ident().getText();
        List<FPType> fpTypes = new ArrayList<>();
        if (ctx.funcFParams() != null) {
            ctx.funcFParams().funcFParam().forEach(a -> {
                a.accept(this);
                fpTypes.add(C.lastFPType);
            });
        }
        var funcType = new FunctionType(funcRetType, fpTypes.stream().collect(
                ArrayList<IRType>::new,
                (list, fpType) -> list.add(fpType.type()),
                ArrayList::addAll
        ));
        C.builder.addFunction(new IRFunctionImpl(name, funcType));
        C.gFuncSymTbl.put(name, funcType);
        C.lc.dive();
        C.inAtarashiiFunction = true;
        // add fParams alloca and store.
        fpTypes.forEach(fpType -> {
            var alloca = C.addAlloca(new IRInstAlloca(fpType.name(), fpType.type()));
            C.lc.push(fpType.name(), alloca);
            var f = C.builder.curFunc().addFP(fpType);
            C.addInst(new IRInstStore(f, alloca));
        });
        // accept blocks
        ctx.block().accept(this);
        C.inAtarashiiFunction = false;
        C.lc.ascend();
        C.builder.curFunc().finishFunction();
        return null;
    }

    /**
     * Visit a parse tree produced by {@link SylangParser#funcType}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitFuncType(SylangParser.FuncTypeContext ctx) {
        if (ctx.Void() != null) {
            C.bType = BType.VOID;
        } else if (ctx.bType() != null) {
            ctx.bType().accept(this);
        }
        return null;
    }

    /**
     * Visit a parse tree produced by {@link SylangParser#funcFParams}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitFuncFParams(SylangParser.FuncFParamsContext ctx) {
        visitChildren(ctx);
        return null;
    }

    /**
     * Visit a parse tree produced by the {@code scalarParam}
     * labeled alternative in {@link SylangParser#funcFParam}.
     * <code>bType Ident</code>
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitScalarParam(SylangParser.ScalarParamContext ctx) {
        var name = ctx.Ident().getText();
        ctx.bType().accept(this);
        C.lastFPType = new FPType(C.bType.toBasicType().toIRType(), name);
        return null;
    }

    /**
     * Visit a parse tree produced by the {@code arrayParam}
     * labeled alternative in {@link SylangParser#funcFParam}.
     * <code>bType Ident Lbracket Rbracket (Lbracket exp Rbracket)*</code>
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitArrayParam(SylangParser.ArrayParamContext ctx) {
        ctx.bType().accept(this);
        List<Integer> shape = getShapeFromConstExp(ctx.exp());
        if (shape.isEmpty()) {
            C.lastFPType = new FPType(PointerType.MakePointer(C.bType.toBasicType().toIRType()),
                    ctx.Ident().getText());
        } else {
            ArrayType arrayType = ArrayType.FromShape(C.bType.toBasicType(), shape);
            PointerType pointerType = PointerType.MakePointer(arrayType);
            C.lastFPType = new FPType(pointerType, ctx.Ident().getText());
        }
        return null;
    }

    @NotNull
    private List<Integer> getShapeFromConstExp(List<SylangParser.ExpContext> exp2) {
        List<Integer> shape;
        try (AutoDive ignored = new AutoDive(C.isConst, true)) {
            shape = exp2.stream().collect(ArrayList<Integer>::new, (list, exp) -> {
                exp.accept(this);
                list.add(GetIntNumFromCVal((IRValConst) C.lastVal));
            }, ArrayList::addAll);
        } catch (Exception e) {
            shape = Collections.emptyList();
        }
        return shape;
    }

    /**
     * Visit a parse tree produced by {@link SylangParser#block}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitBlock(SylangParser.BlockContext ctx) {
        boolean lastAtarashii = C.inAtarashiiFunction;
        if (!C.inAtarashiiFunction) {
            C.lc.dive();
        } else {
            C.inAtarashiiFunction = false;
        }
        ctx.blockItem().forEach(a -> a.accept(this));
        if (!lastAtarashii) {
            C.lc.ascend();
        } else {
            C.inAtarashiiFunction = true;
        }
        return null;
    }

    /**
     * Visit a parse tree produced by the {@code assign}
     * labeled alternative in {@link SylangParser#stmt}.
     * <code>stmt:
     * lVal Assign exp Semicolon					# assign</code>
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitAssign(SylangParser.AssignContext ctx) {
        // L
        C.needLoad.dive(false);
        ctx.lVal().accept(this);
        C.needLoad.ascend();
        IRVal lVal = C.lastVal;
        // R
        C.needLoad.dive(true);
        ctx.exp().accept(this);
        C.needLoad.ascend();
        IRVal rVal = C.lastVal;
        // checkType
        assert lVal.getType().isPointer();
        // lVal is from alloca or getelementptr, とにかく，lVal is a pointer.
        IRType lValInnerType = ((PointerType) lVal.getType()).getElementType();
        DoRuntimeConversion(C, lValInnerType.toBasicType(), rVal);
        C.lastVal = C.addInst(new IRInstStore(C.lastVal, lVal));
        return null;
    }


    /**
     * Visit a parse tree produced by the {@code return}
     * labeled alternative in {@link SylangParser#stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitReturn(SylangParser.ReturnContext ctx) {
        IRBasicType atomType = C.builder.curFunc().getFunctionType().getRetType().toBasicType();
        if (atomType == IRBasicType.VOID) {
            C.addAndUpdate(new IRInstReturn(IRBasicType.VOID, null));
        } else {
            if (ctx.exp() == null) throw new RuntimeException("returning void in non-void function");
            C.needLoad.dive(true);
            ctx.exp().accept(this);
            C.needLoad.ascend();
            DoRuntimeConversion(C, atomType, C.lastVal);
            C.addAndUpdate(new IRInstReturn(atomType, C.lastVal));
        }
        return null;
    }


    /**
     * Visit a parse tree produced by {@link SylangParser#cond}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitCond(SylangParser.CondContext ctx) {
        visitChildren(ctx);
        return null;
    }

    /**
     * Visit a parse tree produced by {@link SylangParser#lVal}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Void visitLVal(SylangParser.LValContext ctx) {
        var name = ctx.Ident().getText();
        var valQueried = C.query(name);
        if (valQueried.getType().isScalar()) {
            C.lastVal = valQueried;
            return null;
        }
        C.needLoad.dive(true);
        List<IRVal> indices = ctx.exp().stream().collect(ArrayList<IRVal>::new, (list, exp) -> {
            exp.accept(this);
            list.add(C.lastVal);
        }, ArrayList::addAll);
        C.needLoad.ascend();
        if (!valQueried.getType().isPointer()) {
            throw new RuntimeException("getting lVal from a non-pointer");
        }
        var pointerType = (PointerType) valQueried.getType();
        var valInnerType = pointerType.getElementType();

        // if queried is global const scalar, need not load.
        if (valQueried instanceof IRValGlobal gsc) {
            if (gsc.isConst() && gsc.getValue().getType().isScalar()) {
                C.lastVal = gsc.getValue();
                return null;
            }
        }
        // needn't load. excluding CALL args.
        // because while accessing CALL args, the C.needLoad will be set to true.
        if (!C.needLoad.get()) {
            // lVal = xxx
            // ^~~~
            // this branch returns a pointer(either alloca or getelementptr)
            // also may ba a IRValGlobalScalar.

            // need not load, scalar
            if (valInnerType.isScalar()) {
                C.lastVal = valQueried;
            }
            // needn't load, array
            else {
                // use getelementptr to get the pointer to the array.
                if (pointerType.getPointerLevel() == 2) {
                    C.addAndUpdate(new IRInstLoad(valQueried));
                    C.addAndUpdate(new IRInstGEP(C.lastVal, indices));
                } else {
                    indices.add(0, IRValConstInt.ZeroInt());
                    C.addAndUpdate(new IRInstGEP(valQueried, indices));
                }
            }

        }
        // need load
        // xxx = lVal
        //       ^~~~   <--this branch returns a value.
        else {
            // need load, scalar
            if (valInnerType.isScalar()) {
                C.lastVal = C.addInst(new IRInstLoad(valQueried));
            }
            // need load, directly use const.
            else if (valQueried instanceof IRValGlobal garr
                    && valQueried.isConst()
                    && garr.getValue() instanceof IRValConstArray carr
                    && indices.size() == carr.getShapes().size()
                    && indices.stream().allMatch(x -> x instanceof IRValConstInt)
            ) {
                C.lastVal = carr.get(indices.stream().collect(ArrayList<Integer>::new, (list, irVal) -> {
                    list.add(GetIntNumFromCVal((IRValConst) irVal));
                }, ArrayList::addAll));
            }
            // need load, array
            else {
                // TODO test, optimize code
                boolean justLoadTheValue = false;
                int idxCnt = 0;
                if (valInnerType instanceof PointerType pType) {
                    if (pType.getElementType().isScalar()) {
                        idxCnt = 1;
                    } else {
                        idxCnt = ((ArrayType) pType.getElementType()).getShape().size() + 1;
                    }
                } else {
                    idxCnt = ((ArrayType) valInnerType).getShape().size();
                }
                justLoadTheValue = indices.size() == idxCnt;
                if (justLoadTheValue) {
                    // need load, array's element
                    if (pointerType.getPointerLevel() == 2) {
                        // fp copies. like %v = alloca [3 x i32]*, %v is [3 x i32]**
                        C.addAndUpdate(new IRInstLoad(valQueried));
                        C.addAndUpdate(new IRInstGEP(C.lastVal, indices));
                    } else {
                        indices.add(0, IRValConstInt.ZeroInt());
                        C.addAndUpdate(new IRInstGEP(valQueried, indices));
                    }
                    C.addAndUpdate(new IRInstLoad(C.lastVal));
                } else {
                    // need load, array's pointer, only appears in CALL args.
                    if (pointerType.getPointerLevel() == 2) {
                        // need load, array's pointer, fp copies.
                        // like %v = alloca [3 x i32]*, %v is [3 x i32]**
                        C.addAndUpdate(new IRInstLoad(valQueried));
                        if (!indices.isEmpty()) {
                            indices.add(IRValConstInt.ZeroInt());
                            C.addAndUpdate(new IRInstGEP(C.lastVal, indices));
                        }
                    } else {
                        // need load, array's pointer, non-fp copies.
                        // add 0 front and end.
                        indices.add(0, IRValConstInt.ZeroInt());
                        indices.add(IRValConstInt.ZeroInt());
                        C.addAndUpdate(new IRInstGEP(valQueried, indices));
                    }
                }
            }
        }
        return null;
    }


    /**
     * Visit a parse tree produced by the {@code call}
     * labeled alternative in {@link SylangParser#unaryExp}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Void visitCall(SylangParser.CallContext ctx) {
        List<IRVal> args = new ArrayList<>();
        C.needLoad.dive(true);
        if (ctx.funcRParams() != null) {
            ctx.funcRParams().funcRParam().forEach(a -> {
                a.accept(this);
                args.add(C.lastVal);
            });
        }
        C.needLoad.ascend();
        var funcName = ctx.Ident().getText();
        var funcType = C.gFuncSymTbl.get(funcName);
        if (funcType == null) {
            throw new CompileException("function `" + funcName + "` not found", ctx.Ident());
        }
        C.addAndUpdate(new IRInstCall(funcName, funcType, args));
        return null;
    }


    /**
     * Visit a parse tree produced by {@link SylangParser#funcRParam}.
     * <code>funcRParam: exp | stringConst;</code>
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitFuncRParam(SylangParser.FuncRParamContext ctx) {
        ctx.exp().accept(this);
        return null;
    }

    /**
     * You shouldn't call this function.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    @Deprecated
    public Void visitFuncRParams(SylangParser.FuncRParamsContext ctx) {
        // You shouldn't call this function.
        // FuncRParams should be handled in visitCall.
        throw new NotImplementedException();
    }

    /*
     *                                       B R A N C H
     */

    /**
     * Visit a parse tree produced by the {@code ifElse}
     * labeled alternative in {@link SylangParser#stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitIfElse(SylangParser.IfElseContext ctx) {
        // prepare
        C.needLoad.dive(true);
        IRBasicBlock trueBB, falseBB, afterBB;
        int ifElseCount = BBController.ifCount.getAndIncrement();
        trueBB = C.builder.curFunc().addBlock();
        trueBB.setName("if.then." + ifElseCount);
        afterBB = C.builder.curFunc().addBlock();
        afterBB.setName("if.end." + ifElseCount);
        if (ctx.Else() != null) {
            falseBB = C.builder.curFunc().addBlock();
            falseBB.setName("if.else." + ifElseCount);
        } else {
            falseBB = afterBB;
        }

        C.bbc.pushIf(trueBB, falseBB, afterBB, C.lc.getLayerIndex());

        // 1. add COND sen
        C.inCond = true;
        ctx.cond().accept(this);
        C.inCond = false;
        IRVal cond = C.lastVal;

        // 2. add BR
        C.addInst(new IRInstBr(cond, trueBB, falseBB));

        // 3. move to true
        C.builder.curFunc().setCurrentBlock(trueBB);

        // 4. accept true
        ctx.stmt(0).accept(this);

        // 5. add BR
        C.addInst(new IRInstBr(afterBB));

        // 6-8 if there is else
        if (ctx.Else() != null) {
            // 6. move to FalseBB
            C.builder.curFunc().setCurrentBlock(falseBB);
            if (ctx.stmt().size() == 2) {
                // 7. accept false
                ctx.stmt(1).accept(this);
            }

            // 8. add BR
            C.addInst(new IRInstBr(afterBB));
        }

        // 9. move to AfterBB
        C.builder.curFunc().setCurrentBlock(afterBB);

        // 10. status
        C.bbc.pop();
        C.needLoad.ascend();
        return null;
    }

    /**
     * Visit a parse tree produced by the {@code while}
     * labeled alternative in {@link SylangParser#stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitWhile(SylangParser.WhileContext ctx) {
        C.needLoad.dive(true);
        IRBasicBlock condBB, bodyBB, afterBB;
        int whileCount = BBController.whileCount.getAndIncrement();
        condBB = C.builder.curFunc().addBlock();
        condBB.setName("while.cond." + whileCount);
        bodyBB = C.builder.curFunc().addBlock();
        bodyBB.setName("while.body." + whileCount);
        afterBB = C.builder.curFunc().addBlock();
        afterBB.setName("while.end." + whileCount);

        C.bbc.pushWhile(bodyBB, condBB, afterBB, C.lc.getLayerIndex());
        C.addAndUpdate(new IRInstBr(condBB));

        C.builder.curFunc().setCurrentBlock(condBB);
        C.inCond = true;
        ctx.cond().accept(this);
        C.inCond = false;
        IRVal cond = C.lastVal;
        C.addAndUpdate(new IRInstBr(cond, bodyBB, afterBB));

        C.builder.curFunc().setCurrentBlock(bodyBB);
        ctx.stmt().accept(this);
        C.addAndUpdate(new IRInstBr(condBB));

        C.builder.curFunc().setCurrentBlock(afterBB);

        C.bbc.pop();
        C.needLoad.ascend();
        return null;
    }

    /**
     * Visit a parse tree produced by the {@code break}
     * labeled alternative in {@link SylangParser#stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitBreak(SylangParser.BreakContext ctx) {
        IRBasicBlock whileBreakBB = C.bbc.queryWhileBreakBB();
        C.addInst(new IRInstBr(whileBreakBB));
        return null;
    }

    /**
     * Visit a parse tree produced by the {@code continue}
     * labeled alternative in {@link SylangParser#stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitContinue(SylangParser.ContinueContext ctx) {
        IRBasicBlock whileCondBB = C.bbc.queryWhileCondBB();
        C.addInst(new IRInstBr(whileCondBB));
        return null;
    }

    /*
     *                                       M A T H
     */


    /**
     * Visit a parse tree produced by the {@code mul_}
     * labeled alternative in {@link SylangParser#}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitMul_(SylangParser.Mul_Context ctx) {
        IRInstMath.MathOP mathOP;
        if (ctx.Mul() != null) mathOP = IRInstMath.MathOP.Mul;
        else if (ctx.Div() != null) mathOP = IRInstMath.MathOP.Div;
        else if (ctx.Mod() != null) mathOP = IRInstMath.MathOP.Rem;
        else throw new RuntimeException("unknown mathOP");

        C.needLoad.dive(true);
        ctx.mulExp().accept(this);
        IRVal left = C.lastVal;
        ctx.unaryExp().accept(this);
        IRVal right = C.lastVal;
        C.needLoad.ascend();

        boolean isConst = C.isConst.get() || ((left instanceof IRValConst) && (right instanceof IRValConst));

        if (isConst) {
            C.lastVal = DoCompileTimeCalculation((IRValConst) left, (IRValConst) right, mathOP);
        } else {
            C.lastVal = C.addInst(new IRInstMath(mathOP, left, right));
        }
        return null;
    }

    /**
     * Visit a parse tree produced by the {@code add_}
     * labeled alternative in {@link SylangParser#}.
     * <code>addExp Add mulExp</code>
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitAdd_(SylangParser.Add_Context ctx) {
        IRInstMath.MathOP mathOP;
        if (ctx.Add() != null) mathOP = IRInstMath.MathOP.Add;
        else mathOP = IRInstMath.MathOP.Sub;

        C.needLoad.dive(true);
        ctx.addExp().accept(this);
        IRVal left = C.lastVal;
        ctx.mulExp().accept(this);
        IRVal right = C.lastVal;
        C.needLoad.ascend();

        boolean isConst = C.isConst.get() || ((left instanceof IRValConst) && (right instanceof IRValConst));

        if (isConst) {
            C.lastVal = DoCompileTimeCalculation((IRValConst) left, (IRValConst) right, mathOP);
        } else {
            C.lastVal = C.addInst(new IRInstMath(mathOP, left, right));
        }
        return null;
    }

    /**
     * Visit a parse tree produced by the {@code unray_}
     * labeled alternative in {@link SylangParser#unaryExp}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitUnary_(SylangParser.Unary_Context ctx) {
        // add sub not
        enum Ops {
            ADD, SUB, NOT
        }
        Ops op;
        if (ctx.Add() != null) op = Ops.ADD;
        else if (ctx.Sub() != null) op = Ops.SUB;
        else if (ctx.Not() != null) op = Ops.NOT;
        else throw new RuntimeException("unknown unary op");

        C.needLoad.dive(true);
        ctx.unaryExp().accept(this);
        C.needLoad.ascend();
        IRVal val = C.lastVal;
        var atomType = val.getType().toBasicType();

        boolean isConst = C.isConst.get() || ((val instanceof IRValConst));
        switch (op) {
            case ADD -> {
                // do nothing
            }
            case SUB -> {
                var zero = IRValConst.Zero(atomType);
                DoRuntimeCalculation(C, zero, val, IRInstMath.MathOP.Sub);
            }
            case NOT -> {
                var zero = IRValConst.Zero(atomType);
                C.addAndUpdate(IRInstCmpFactory.createCmpInst(IRInstIcmp.IcmpOp.EQ, val, zero, atomType));
            }
        }
        return null;
    }


    /**
     * Visit a parse tree produced by the {@code relExp_}
     * labeled alternative in {@link SylangParser#}.
     * <br/> lt,gt,leq,geq
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitRelExp_(SylangParser.RelExp_Context ctx) {
        // LT GT LEQ GEQ
        IRInstIcmp.IcmpOp op;
        if (ctx.Leq() != null) op = IRInstIcmp.IcmpOp.SLE;
        else if (ctx.Lt() != null) op = IRInstIcmp.IcmpOp.SLT;
        else if (ctx.Gt() != null) op = IRInstIcmp.IcmpOp.SGT;
        else if (ctx.Geq() != null) op = IRInstIcmp.IcmpOp.SGE;
        else throw new RuntimeException("unknown icmp op");

        C.needLoad.dive(true);
        ctx.relExp().accept(this);
        IRVal left = C.lastVal;
        ctx.addExp().accept(this);
        IRVal right = C.lastVal;
        C.needLoad.ascend();
        DoRuntimeComparison(C, left, right, op);
        return null;
    }


    /**
     * Visit a parse tree produced by the {@code neq}
     * labeled alternative in {@link SylangParser#}.
     * <code>	eqExp Neq relExp	# neq;</code>
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitNeq(SylangParser.NeqContext ctx) {
        IRInstIcmp.IcmpOp op = IRInstIcmp.IcmpOp.NE;
        C.needLoad.dive(true);
        ctx.eqExp().accept(this);
        IRVal left = C.lastVal;
        ctx.relExp().accept(this);
        IRVal right = C.lastVal;
        C.needLoad.ascend();
        DoRuntimeComparison(C, left, right, op);
        return null;
    }

    /**
     * Visit a parse tree produced by the {@code eq}
     * labeled alternative in {@link SylangParser#}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitEq(SylangParser.EqContext ctx) {
        IRInstIcmp.IcmpOp op = IRInstIcmp.IcmpOp.EQ;
        C.needLoad.dive(true);
        ctx.eqExp().accept(this);
        IRVal left = C.lastVal;
        ctx.relExp().accept(this);
        IRVal right = C.lastVal;
        C.needLoad.ascend();
        DoRuntimeComparison(C, left, right, op);
        return null;
    }

    /*
     *                                       L O G I C
     */

    /**
     * Visit a parse tree produced by the {@code and}
     * labeled alternative in {@link SylangParser#}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Void visitAnd(SylangParser.AndContext ctx) {
        C.needLoad.dive(true);
        int andCount = BBController.andCount.getAndIncrement();
        IRBasicBlock trueBB = C.builder.curFunc().addBlock();
        trueBB.setName("and.next." + andCount);
        C.bbc.pushAnd(trueBB);

        ctx.lAndExp().accept(this);
        DoRuntimeBoolConversion(C, C.lastVal);
        IRVal left = C.lastVal;
        if (!C.inCond) {
            throw new NotImplementedException();
        }
        C.addInst(new IRInstBr(left, C.bbc.queryTrueBB(), C.bbc.queryFalseBB()));

        C.builder.curFunc().setCurrentBlock(trueBB);
        C.bbc.pop();

        ctx.eqExp().accept(this);
        DoRuntimeBoolConversion(C, C.lastVal);

        C.needLoad.dive(false);
        return null;
    }

    /**
     * Visit a parse tree produced by the {@code or}
     * labeled alternative in {@link SylangParser#}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Void visitOr(SylangParser.OrContext ctx) {
        C.needLoad.dive(true);
        int orCount = BBController.orCount.getAndIncrement();
        IRBasicBlock falseBB = C.builder.curFunc().addBlock();
        falseBB.setName("or.next." + orCount);
        C.bbc.pushOr(falseBB);

        ctx.lOrExp().accept(this);
        DoRuntimeBoolConversion(C, C.lastVal);
        IRVal left = C.lastVal;
        if (!C.inCond) {
            throw new NotImplementedException();
        }
        C.addInst(new IRInstBr(left, C.bbc.queryTrueBB(), C.bbc.queryFalseBB()));

        C.builder.curFunc().setCurrentBlock(falseBB);
        C.bbc.pop();

        ctx.lAndExp().accept(this);
        DoRuntimeBoolConversion(C, C.lastVal);

        C.needLoad.dive(false);
        return null;
    }


    /*
     *                                       U S E L E S S
     */


    /**
     * Visit a parse tree produced by the {@code lOrExp_lAnd}
     * labeled alternative in {@link SylangParser#}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitLOrExp_lAnd(SylangParser.LOrExp_lAndContext ctx) {
        visitChildren(ctx);
        return null;
    }

    /**
     * Visit a parse tree produced by the {@code lAndExp_eq}
     * labeled alternative in {@link SylangParser#}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Void visitLAndExp_eq(SylangParser.LAndExp_eqContext ctx) {
        visitChildren(ctx);
        return null;
    }


    /**
     * Visit a parse tree produced by the {@code relExp_add}
     * labeled alternative in {@link SylangParser#}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitRelExp_add(SylangParser.RelExp_addContext ctx) {
        visitChildren(ctx);
        return null;
    }

    /**
     * Visit a parse tree produced by the {@code eqExp_rel}
     * labeled alternative in {@link SylangParser#}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitEqExp_rel(SylangParser.EqExp_relContext ctx) {
        visitChildren(ctx);
        return null;
    }

    /**
     * Visit a parse tree produced by the {@code addExp_mul}
     * labeled alternative in {@link SylangParser#}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitAddExp_mul(SylangParser.AddExp_mulContext ctx) {
        visitChildren(ctx);
        return null;
    }


    /**
     * Visit a parse tree produced by the {@code mulExp_unray}
     * labeled alternative in {@link SylangParser#}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitMulExp_unary(SylangParser.MulExp_unaryContext ctx) {
        visitChildren(ctx);
        return null;
    }

    /**
     * Visit a parse tree produced by the {@code unaryExp_primary}
     * labeled alternative in {@link SylangParser#unaryExp}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitUnaryExp_primary(SylangParser.UnaryExp_primaryContext ctx) {
        visitChildren(ctx);
        return null;
    }

    /**
     * Visit a parse tree produced by the {@code primaryExp_number}
     * labeled alternative in {@link SylangParser#primaryExp}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitPrimaryExp_number(SylangParser.PrimaryExp_numberContext ctx) {
        visitChildren(ctx);
        return null;
    }

    /**
     * Visit a parse tree produced by the {@code decIntConst}
     * labeled alternative in {@link SylangParser#intConst}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitDecIntConst(SylangParser.DecIntConstContext ctx) {
        int num = Integer.parseInt(ctx.DecIntConst().getText());
        C.lastVal = new IRValConstInt(num);
        return null;
    }

    /**
     * Visit a parse tree produced by the {@code octIntConst}
     * labeled alternative in {@link SylangParser#intConst}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitOctIntConst(SylangParser.OctIntConstContext ctx) {
        int num = Integer.parseInt(ctx.OctIntConst().getText(), 8);
        C.lastVal = new IRValConstInt(num);
        return null;
    }

    /**
     * Visit a parse tree produced by the {@code hexIntConst}
     * labeled alternative in {@link SylangParser#intConst}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitHexIntConst(SylangParser.HexIntConstContext ctx) {
        String withoutPrefix = ctx.HexIntConst().getText().substring(2);
        int num = Integer.parseInt(withoutPrefix, 16);
        C.lastVal = new IRValConstInt(num);
        return null;
    }

    /**
     * Visit a parse tree produced by the {@code decFloatConst}
     * labeled alternative in {@link SylangParser#floatConst}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitDecFloatConst(SylangParser.DecFloatConstContext ctx) {
        float num = Float.parseFloat(ctx.DecFloatConst().getText());
        C.lastVal = new IRValConstFloat(num);
        return null;
    }

    /**
     * Visit a parse tree produced by the {@code hexFloatConst}
     * labeled alternative in {@link SylangParser#floatConst}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitHexFloatConst(SylangParser.HexFloatConstContext ctx) {
        // stored like 0xe.bp2.
        float num = Float.parseFloat(ctx.getText());
        C.lastVal = new IRValConstFloat(num);
        return null;
    }

    /**
     * Visit a parse tree produced by {@link SylangParser#number}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitNumber(SylangParser.NumberContext ctx) {
        visitChildren(ctx);
        return null;
    }

    /**
     * Visit a parse tree produced by {@link SylangParser#stringConst}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Void visitStringConst(SylangParser.StringConstContext ctx) {
        return null;
    }

    /**
     * Visit a parse tree produced by the {@code primaryExp_exp}
     * labeled alternative in {@link SylangParser#primaryExp}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitPrimaryExp_exp(SylangParser.PrimaryExp_expContext ctx) {
        visitChildren(ctx);
        return null;
    }


    /**
     * Visit a parse tree produced by the {@code lValExpr}
     * labeled alternative in {@link SylangParser#primaryExp}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitLValExpr(SylangParser.LValExprContext ctx) {
        visitChildren(ctx);
        return null;
    }

    /**
     * Visit a parse tree produced by {@link SylangParser#exp}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitExp(SylangParser.ExpContext ctx) {
        visitChildren(ctx);
        return null;
    }

    /**
     * Visit a parse tree produced by {@link SylangParser#blockItem}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitBlockItem(SylangParser.BlockItemContext ctx) {
        visitChildren(ctx);
        return null;
    }

    /**
     * Visit a parse tree produced by the {@code exprStmt}
     * labeled alternative in {@link SylangParser#stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitExprStmt(SylangParser.ExprStmtContext ctx) {
        visitChildren(ctx);
        return null;
    }

    /**
     * Visit a parse tree produced by the {@code blockStmt}
     * labeled alternative in {@link SylangParser#stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    @Finished
    public Void visitBlockStmt(SylangParser.BlockStmtContext ctx) {
        visitChildren(ctx);
        return null;
    }
}
