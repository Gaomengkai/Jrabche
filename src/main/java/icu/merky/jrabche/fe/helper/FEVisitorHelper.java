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

package icu.merky.jrabche.fe.helper;

import icu.merky.jrabche.fe.visitor.VisitorContext;
import icu.merky.jrabche.llvmir.inst.IRInstCmpFactory;
import icu.merky.jrabche.llvmir.inst.IRInstIcmp;
import icu.merky.jrabche.llvmir.inst.IRInstMath;
import icu.merky.jrabche.llvmir.inst.IRInstUnary;
import icu.merky.jrabche.llvmir.support.IRCompileTimeCalc;
import icu.merky.jrabche.llvmir.types.IRBasicType;
import icu.merky.jrabche.llvmir.values.IRVal;
import icu.merky.jrabche.llvmir.values.IRValConst;

import java.util.List;

public class FEVisitorHelper {

    public static void DoRuntimeConversion(VisitorContext C, IRBasicType atomType, IRVal val) {
        if (val instanceof IRValConst) {
            C.lastVal = IRCompileTimeCalc.DoCompileTimeConversion(atomType, val);
        } else {
            switch (atomType) {
                case INT -> {
                    if (val.getType().isI32()) {
                        C.lastVal = val;
                    } else if (val.getType().isFloat()) {
                        C.lastVal = C.addInst(new IRInstUnary(IRInstUnary.UnaryOP.FpToSi, val));
                    } else if (val.getType().isI1()) {
                        C.lastVal = C.addInst(new IRInstUnary(IRInstUnary.UnaryOP.ZExt, val));
                    } else {
                        throw new RuntimeException("Not a number");
                    }
                }
                case FLOAT -> {
                    if (val.getType().isI32()) {
                        C.lastVal = C.addInst(new IRInstUnary(IRInstUnary.UnaryOP.SiToFp, val));
                    } else if (val.getType().isFloat()) {
                        C.lastVal = val;
                    } else if (val.getType().isI1()) {
                        C.lastVal = C.addInst(new IRInstUnary(IRInstUnary.UnaryOP.ZExt, val));
                        C.lastVal = C.addInst(new IRInstUnary(IRInstUnary.UnaryOP.SiToFp, C.lastVal));
                    } else {
                        throw new RuntimeException("Not a number");
                    }
                }
                case POINTER -> {
                    if (val.getType().isPointer()) {
                        C.lastVal = val;
                    } else {
                        throw new RuntimeException("Not a pointer");
                    }
                }
                default -> throw new IllegalStateException("Unexpected value: " + atomType);
            }
        }
    }

    public static void DoRuntimeCalculation(VisitorContext C, IRVal lhs, IRVal rhs, IRInstMath.MathOP op) {
        if (lhs instanceof IRValConst lhsc && rhs instanceof IRValConst rhsc) {
            C.lastVal = IRCompileTimeCalc.DoCompileTimeCalculation(lhsc, rhsc, op);
            return;
        }
        var resolvedType = IRCompileTimeCalc.ResolveType(lhs.getType(), rhs.getType());
        DoRuntimeConversion(C, resolvedType, lhs);
        lhs = C.lastVal;
        DoRuntimeConversion(C, resolvedType, rhs);
        rhs = C.lastVal;
        if (resolvedType.equals(IRBasicType.INT)) {
            C.lastVal = C.addInst(new IRInstMath(op, lhs, rhs));
        } else if (resolvedType.equals(IRBasicType.FLOAT)) {
            C.lastVal = C.addInst(new IRInstMath(op, lhs, rhs));
        } else {
            throw new RuntimeException("MathOP error");
        }
    }

    public static void DoRuntimeComparison(VisitorContext C, IRVal lhs, IRVal rhs, IRInstIcmp.IcmpOp op) {
        if (lhs instanceof IRValConst lhsc && rhs instanceof IRValConst rhsc) {
            C.lastVal = IRCompileTimeCalc.DoCompileTimeComparison(lhsc, rhsc, op);
            return;
        }
        var resolvedType = IRCompileTimeCalc.ResolveType(lhs.getType(), rhs.getType());
        DoRuntimeConversion(C, resolvedType, lhs);
        lhs = C.lastVal;
        DoRuntimeConversion(C, resolvedType, rhs);
        rhs = C.lastVal;
        C.addAndUpdate(IRInstCmpFactory.createCmpInst(op, lhs, rhs, resolvedType));
    }

    public static void DoRuntimeBoolConversion(VisitorContext C, IRVal val) {
        if (val instanceof IRValConst) {
            C.lastVal = IRCompileTimeCalc.DoCompileTimeBoolConversion((IRValConst) val);
        } else {
            if (val.getType().isI1()) {
                C.lastVal = val;
                return;
            }
            DoRuntimeConversion(C, IRBasicType.INT, val);
            C.lastVal = C.addInst(new IRInstIcmp(IRInstIcmp.IcmpOp.NE, C.lastVal, IRValConst.Zero(IRBasicType.INT)));
        }
    }

    /**
     * /// According to shape, add N to cur.
     * /// \param shape [2][3]
     * /// \param cur [1][2]
     * /// \param N
     * /// \param startsAt
     * /// \param reset
     * void ArrayPosPlusN(
     * const std::deque<size_t>& shape,
     * std::deque<size_t>&       cur,
     * size_t                    N,
     * int                       startsAt = -1,
     * bool                      reset    = true
     * )
     * {
     * if (startsAt == -1) startsAt = shape.size() - 1;
     * for (int i = startsAt; i >= 0; --i) {   // ATTENTION!!!!!!!!
     * cur[i] += N;
     * if (cur[i] < shape[i]) { break; }
     * N = cur[i] / shape[i];
     * cur[i] %= shape[i];
     * }
     * if (reset) {
     * for (int i = startsAt + 1; i < shape.size(); ++i) { cur[i] = 0; }
     * }
     * }
     */
    public static void ArrayPosPlusN(List<Integer> shape, List<Integer> cur, int N, int startsAt) {
        if (startsAt == -1) startsAt = shape.size() - 1;
        for (int i = startsAt; i >= 0; --i) {
            cur.set(i, cur.get(i) + N);
            if (cur.get(i) < shape.get(i)) {
                break;
            }
            N = cur.get(i) / shape.get(i);
            cur.set(i, cur.get(i) % shape.get(i));
        }
        // for (int i = startsAt + 1; i < shape.size(); ++i) { cur[i] = 0; }
        for (int i = startsAt + 1; i < shape.size(); ++i) {
            cur.set(i, 0);
        }
    }

    public static void ArrayPosPlusN(List<Integer> shape, List<Integer> cur, int N) {
        ArrayPosPlusN(shape, cur, N, -1);
    }
}
