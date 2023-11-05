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

import icu.merky.jrabche.fe.parser.SylangParser;
import icu.merky.jrabche.fe.parser.SylangVisitor;
import icu.merky.jrabche.helper.Helper;
import icu.merky.jrabche.helper.InitList;
import icu.merky.jrabche.llvmir.IRBuilder;
import icu.merky.jrabche.llvmir.inst.*;
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
        this.C = new VisitorContext();
        C.builder = builder;
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
        var atomType = C.bType.toAtomType();
        var name = ctx.Ident().getText();
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
            InitList il = (InitList) initVal;
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
        IRAtomType atomType = C.bType.toAtomType();
        String name = ctx.Ident().getText();
        List<Integer> shape = getShapeFromConstExp(ctx.exp());
        // global, must be a const.
        if (C.lc.inGlobal()) globalVarDef(ctx, atomType, name, shape);
            // local var, may not be all const.
        else {
            if (shape.isEmpty()) {
                // local var, scalar
                var alloca = C.addAlloca(new IRInstAlloca(name, atomType.toIRType()));
                if (ctx.initVal() != null) {
                    // local var, scalar with init
                    try (AutoDive ignored = new AutoDive(C.needLoad, true)) {
                        ctx.initVal().accept(this);
                        DoRuntimeConversion(C, atomType, C.lastVal);
                        C.lc.push(name, C.lastVal);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                } else {
                    // local var, scalar without init
                    C.push(name, alloca);
                }
            } else {
                // TODO impl local array
            }
        }

        return null;
    }

    @Finished
    private void globalVarDef(SylangParser.VarDefContext ctx, IRAtomType atomType, String name, List<Integer> shape) {
        try (AutoDive ignored = new AutoDive(C.isConst, true)) {
            if (shape.isEmpty()) {
                // scalar
                if (ctx.initVal() != null) {
                    ctx.initVal().accept(this);
                    IRValConst converted = Helper.DoCompileTimeConversion(atomType, C.lastVal);
                    C.lc.push(name, converted);
                } else {
                    C.lc.push(name, IRValConstInt.Zero(C.bType.toAtomType()));
                }
            } else {
                // array
                if (ctx.initVal() != null) {
                    ctx.initVal().accept(this);
                    InitList il = (InitList) C.lastVal;
                    IRValConstArray carr = IRValConstArray.FromInitList(il, ArrayType.FromShape(atomType, shape));
                    C.lc.push(name, new IRVarArray(carr));
                } else {
                    C.lc.push(name, new IRVarArray(ArrayType.FromShape(atomType, shape)));
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
            var il = new InitList(C.bType.toAtomType());
            for (SylangParser.InitValContext initValContext : ctx.initVal()) {
                initValContext.accept(this);
                if (C.lastVal instanceof InitList ilGot) {
                    il.addIL(ilGot);
                } else {
                    il.addCV((IRValConst) C.lastVal);
                }
            }
            C.lastVal = il;
        } else {
// TODO
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
        var funcRetType = C.bType.toAtomType().toIRType();
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
        C.globalFunctionSymbolTable.put(name, funcType);
        C.lc.dive();
        C.inAtarashiiFunction = true;
        // add fParams alloca and store.
        fpTypes.forEach(this::dealFpInit);
        ctx.block().accept(this);
        C.inAtarashiiFunction = false;
        C.lc.ascend();
        C.builder.curFunc().finishFunction();
        return null;
    }

    private void dealFpInit(FPType fpType) {
        var alloca = C.addAlloca(new IRInstAlloca(fpType.name(), fpType.type()));
        C.lc.push(fpType.name(), alloca);
        var f = C.builder.curFunc().addFP(fpType);
        C.addInst(new IRInstStore(f, alloca));
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
        C.lastFPType = new FPType(C.bType.toAtomType().toIRType(), name);
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
            C.lastFPType = new FPType(PointerType.MakePointer(C.bType.toAtomType().toIRType()),
                    ctx.Ident().getText());
        } else {
            ArrayType arrayType = ArrayType.FromShape(C.bType.toAtomType(), shape);
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
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Void visitAssign(SylangParser.AssignContext ctx) {
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
        visitChildren(ctx);
        IRAtomType atomType = C.builder.curFunc().getFunctionType().toAtomType();
        if (atomType == IRAtomType.VOID) {
            C.addInst(new IRInstReturn(IRAtomType.VOID, null));
        } else {
            if (ctx.exp() == null) throw new RuntimeException("returning void in non-void function");
            ctx.exp().accept(this);
            DoRuntimeConversion(C, atomType, C.lastVal);
            C.addInst(new IRInstReturn(atomType, C.lastVal));
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
    public Void visitCond(SylangParser.CondContext ctx) {
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
        var valAlloca = C.query(name);
        List<IRVal> indices = ctx.exp().stream().collect(ArrayList<IRVal>::new, (list, exp) -> {
            exp.accept(this);
            list.add(C.lastVal);
        }, ArrayList::addAll);
        if (!valAlloca.getType().isPointer()) {
            throw new RuntimeException("getting lVal from a non-pointer");
        }
        var valInnerType = ((IRInstAlloca) valAlloca).getAllocatedType();
        if (!C.needLoad.get()) {
            // on left of =
            // this branch returns a pointer(either alloca or getelementptr)
            // need not load, scalar
            if (valInnerType.isInt() || valInnerType.isFloat()) {
                C.lastVal = valAlloca;
            }
            // need not load, array
            else {
                // TODO
            }

        }
        // need load
        else {
            // need load, scalar
            if (valInnerType.isInt() || valInnerType.isFloat()) {
                C.lastVal = C.addInst(new IRInstLoad(valAlloca));
            }
            // need load, array
            else {
                // TODO
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
        return null;
    }


    /**
     * Visit a parse tree produced by {@link SylangParser#funcRParam}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Void visitFuncRParam(SylangParser.FuncRParamContext ctx) {
        return null;
    }

    /**
     * Visit a parse tree produced by {@link SylangParser#funcRParams}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Void visitFuncRParams(SylangParser.FuncRParamsContext ctx) {
        return null;
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
    public Void visitIfElse(SylangParser.IfElseContext ctx) {
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
    public Void visitWhile(SylangParser.WhileContext ctx) {
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
    public Void visitBreak(SylangParser.BreakContext ctx) {
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
    public Void visitContinue(SylangParser.ContinueContext ctx) {
        return null;
    }

    /*
     *                                       M A T H
     */


    /**
     * Visit a parse tree produced by the {@code mul_}
     * labeled alternative in {@link SylangParser#mulExp}.
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

        ctx.mulExp().accept(this);
        IRVal left = C.lastVal;
        ctx.unaryExp().accept(this);
        IRVal right = C.lastVal;

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
     * labeled alternative in {@link SylangParser#addExp}.
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

        ctx.addExp().accept(this);
        IRVal left = C.lastVal;
        ctx.mulExp().accept(this);
        IRVal right = C.lastVal;

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
    public Void visitUnary_(SylangParser.Unary_Context ctx) {
        return null;
    }


    /**
     * Visit a parse tree produced by the {@code relExp_}
     * labeled alternative in {@link SylangParser#relExp}.
     * <br/> lt,gt,leq,geq
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Void visitRelExp_(SylangParser.RelExp_Context ctx) {
        return null;
    }


    /**
     * Visit a parse tree produced by the {@code neq}
     * labeled alternative in {@link SylangParser#eqExp}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Void visitNeq(SylangParser.NeqContext ctx) {
        return null;
    }

    /**
     * Visit a parse tree produced by the {@code eq}
     * labeled alternative in {@link SylangParser#eqExp}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Void visitEq(SylangParser.EqContext ctx) {
        return null;
    }

    /*
     *                                       L O G I C
     */

    /**
     * Visit a parse tree produced by the {@code and}
     * labeled alternative in {@link SylangParser#lAndExp}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Void visitAnd(SylangParser.AndContext ctx) {
        return null;
    }

    /**
     * Visit a parse tree produced by the {@code or}
     * labeled alternative in {@link SylangParser#lOrExp}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Void visitOr(SylangParser.OrContext ctx) {
        return null;
    }


    /*
     *                                       U S E L E S S
     */


    /**
     * Visit a parse tree produced by the {@code lOrExp_lAnd}
     * labeled alternative in {@link SylangParser#lOrExp}.
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
     * labeled alternative in {@link SylangParser#lAndExp}.
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
     * labeled alternative in {@link SylangParser#relExp}.
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
     * labeled alternative in {@link SylangParser#eqExp}.
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
     * labeled alternative in {@link SylangParser#addExp}.
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
     * labeled alternative in {@link SylangParser#mulExp}.
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
