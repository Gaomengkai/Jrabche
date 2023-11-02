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

import icu.merky.jrabche.fe.helper.Helper;
import icu.merky.jrabche.fe.helper.InitList;
import icu.merky.jrabche.fe.parser.SylangParser;
import icu.merky.jrabche.fe.parser.SylangVisitor;
import icu.merky.jrabche.llvmir.IRBuilder;
import icu.merky.jrabche.llvmir.types.ArrayType;
import icu.merky.jrabche.llvmir.values.IRValConst;
import icu.merky.jrabche.llvmir.values.IRValConstArray;
import icu.merky.jrabche.llvmir.values.IRValConstFloat;
import icu.merky.jrabche.llvmir.values.IRValConstInt;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;

import java.util.ArrayList;

import static icu.merky.jrabche.fe.helper.Helper.GetIntNumFromCVal;

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
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Void visitVarDecl(SylangParser.VarDeclContext ctx) {
        return null;
    }

    /**
     * Visit a parse tree produced by {@link SylangParser#varDef}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Void visitVarDef(SylangParser.VarDefContext ctx) {
        return null;
    }

    /**
     * Visit a parse tree produced by the {@code init}
     * labeled alternative in {@link SylangParser#initVal}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Void visitInit(SylangParser.InitContext ctx) {
        ctx.exp().accept(this);
        return null;
    }

    /**
     * Visit a parse tree produced by the {@code initList}
     * labeled alternative in {@link SylangParser#initVal}.<br>
     * <code>Lbrace (initVal (Comma initVal)*)? Rbrace	# initList;</code>
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
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Void visitFuncDef(SylangParser.FuncDefContext ctx) {
        return null;
    }

    /**
     * Visit a parse tree produced by {@link SylangParser#funcType}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
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
    public Void visitFuncFParams(SylangParser.FuncFParamsContext ctx) {
        return null;
    }

    /**
     * Visit a parse tree produced by the {@code scalarParam}
     * labeled alternative in {@link SylangParser#funcFParam}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Void visitScalarParam(SylangParser.ScalarParamContext ctx) {
        return null;
    }

    /**
     * Visit a parse tree produced by the {@code arrayParam}
     * labeled alternative in {@link SylangParser#funcFParam}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Void visitArrayParam(SylangParser.ArrayParamContext ctx) {
        return null;
    }

    /**
     * Visit a parse tree produced by {@link SylangParser#block}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Void visitBlock(SylangParser.BlockContext ctx) {
        return null;
    }

    /**
     * Visit a parse tree produced by {@link SylangParser#blockItem}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Void visitBlockItem(SylangParser.BlockItemContext ctx) {
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
     * Visit a parse tree produced by the {@code exprStmt}
     * labeled alternative in {@link SylangParser#stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Void visitExprStmt(SylangParser.ExprStmtContext ctx) {
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
    public Void visitBlockStmt(SylangParser.BlockStmtContext ctx) {
        return null;
    }

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

    /**
     * Visit a parse tree produced by the {@code return}
     * labeled alternative in {@link SylangParser#stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Void visitReturn(SylangParser.ReturnContext ctx) {
        return null;
    }

    /**
     * Visit a parse tree produced by {@link SylangParser#exp}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Void visitExp(SylangParser.ExpContext ctx) {
        ctx.addExp().accept(this);
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
    public Void visitPrimaryExp_exp(SylangParser.PrimaryExp_expContext ctx) {
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
    public Void visitLValExpr(SylangParser.LValExprContext ctx) {
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
    public Void visitPrimaryExp_number(SylangParser.PrimaryExp_numberContext ctx) {
        ctx.number().accept(this);
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
    public Void visitNumber(SylangParser.NumberContext ctx) {
        if (ctx.floatConst() != null) {
            ctx.floatConst().accept(this);
        } else {
            ctx.intConst().accept(this);
        }
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
    public Void visitUnaryExp_primary(SylangParser.UnaryExp_primaryContext ctx) {
        ctx.primaryExp().accept(this);
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

    /**
     * Visit a parse tree produced by the {@code mulExp_unray}
     * labeled alternative in {@link SylangParser#mulExp}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Void visitMulExp_unary(SylangParser.MulExp_unaryContext ctx) {
        ctx.unaryExp().accept(this);
        return null;
    }

    /**
     * Visit a parse tree produced by the {@code mul_}
     * labeled alternative in {@link SylangParser#mulExp}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Void visitMul_(SylangParser.Mul_Context ctx) {
        return null;
    }

    /**
     * Visit a parse tree produced by the {@code add_}
     * labeled alternative in {@link SylangParser#addExp}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Void visitAdd_(SylangParser.Add_Context ctx) {
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
    public Void visitAddExp_mul(SylangParser.AddExp_mulContext ctx) {
        ctx.mulExp().accept(this);
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
        if (ctx.Lt() != null) {
            System.out.println("Lt");
        } else if (ctx.Leq() != null) {
            System.out.println("Leq");
        } else if (ctx.Gt() != null) {
            System.out.println("Gt");
        } else if (ctx.Geq() != null) {
            System.out.println("Geq");
        }
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
    public Void visitRelExp_add(SylangParser.RelExp_addContext ctx) {
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
    public Void visitEqExp_rel(SylangParser.EqExp_relContext ctx) {
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

    /**
     * Visit a parse tree produced by the {@code lAndExp_eq}
     * labeled alternative in {@link SylangParser#lAndExp}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Void visitLAndExp_eq(SylangParser.LAndExp_eqContext ctx) {
        return null;
    }

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

    /**
     * Visit a parse tree produced by the {@code lOrExp_lAnd}
     * labeled alternative in {@link SylangParser#lOrExp}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Void visitLOrExp_lAnd(SylangParser.LOrExp_lAndContext ctx) {
        return null;
    }
}
