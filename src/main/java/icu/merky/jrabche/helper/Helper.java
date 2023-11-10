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

package icu.merky.jrabche.helper;

import icu.merky.jrabche.fe.visitor.VisitorContext;
import icu.merky.jrabche.llvmir.inst.IRInstCmpFactory;
import icu.merky.jrabche.llvmir.inst.IRInstIcmp;
import icu.merky.jrabche.llvmir.inst.IRInstMath;
import icu.merky.jrabche.llvmir.inst.IRInstUnary;
import icu.merky.jrabche.llvmir.types.IRBasicType;
import icu.merky.jrabche.llvmir.types.IRType;
import icu.merky.jrabche.llvmir.values.*;

import java.util.List;

public class Helper {
    public static int GetIntNumFromCVal(IRValConst lastVal) {
        if (lastVal instanceof IRValConstInt intVal) {
            return intVal.getValue();
        } else if (lastVal instanceof IRValConstFloat floatVal) {
            return (int) floatVal.getValue();
        } else if (lastVal instanceof IRValConstBool boolVal) {
            return boolVal.getValue();
        } else {
            throw new RuntimeException("Not a number");
        }
    }

    public static float GetFloatNumFromCVal(IRValConst lastVal) {
        if (lastVal instanceof IRValConstInt intVal) {
            return intVal.getValue();
        } else if (lastVal instanceof IRValConstFloat floatVal) {
            return floatVal.getValue();
        } else if (lastVal instanceof IRValConstBool boolVal) {
            return boolVal.getValue();
        } else {
            throw new RuntimeException("Not a number");
        }
    }

    private static int compare2ValConst(IRValConst lhs, IRValConst rhs) {
        Integer lInt = null, rInt = null;
        Float lFloat = null, rFloat = null;
        if (lhs instanceof IRValConstBool lhsI1) {
            lInt = lhsI1.getValue();
        } else if (lhs instanceof IRValConstInt lhsInt) {
            lInt = lhsInt.getValue();
        } else if (lhs instanceof IRValConstFloat lhsFloat) {
            lFloat = lhsFloat.getValue();
        }
        if (rhs instanceof IRValConstBool rhsI1) {
            rInt = rhsI1.getValue();
        } else if (rhs instanceof IRValConstInt rhsInt) {
            rInt = rhsInt.getValue();
        } else if (rhs instanceof IRValConstFloat rhsFloat) {
            rFloat = rhsFloat.getValue();
        }
        // same type
        if (lInt != null && rInt != null) {
            return lInt.compareTo(rInt);
        } else if (lFloat != null && rFloat != null) {
            return lFloat.compareTo(rFloat);
        }
        // different type
        if (lInt != null && rFloat != null) {
            return lInt.compareTo(rFloat.intValue());
        } else if (lFloat != null && rInt != null) {
            return lFloat.compareTo(rInt.floatValue());
        }
        throw new RuntimeException("Not a number");
    }

    public static IRValConstBool DoCompileTimeComparison(IRValConst lhs, IRValConst rhs, IRInstIcmp.IcmpOp op) {
        return switch (op) {
            case EQ -> new IRValConstBool(compare2ValConst(lhs, rhs) == 0 ? 1 : 0);
            case NE -> new IRValConstBool(compare2ValConst(lhs, rhs) != 0 ? 1 : 0);
            case SGT -> new IRValConstBool(compare2ValConst(lhs, rhs) > 0 ? 1 : 0);
            case SGE -> new IRValConstBool(compare2ValConst(lhs, rhs) >= 0 ? 1 : 0);
            case SLT -> new IRValConstBool(compare2ValConst(lhs, rhs) < 0 ? 1 : 0);
            case SLE -> new IRValConstBool(compare2ValConst(lhs, rhs) <= 0 ? 1 : 0);
            default -> throw new RuntimeException("IcmpOP error");
        };
    }

    public static IRValConst DoCompileTimeCalculation(IRValConst lhs, IRValConst rhs, IRInstMath.MathOP op) {
        var resolvedType = ResolveType(lhs.getType(), rhs.getType());
        lhs = DoCompileTimeConversion(resolvedType, lhs);
        rhs = DoCompileTimeConversion(resolvedType, rhs);
        if (resolvedType.equals(IRBasicType.INT)) {
            var l = ((IRValConstInt) lhs).getValue();
            var r = ((IRValConstInt) rhs).getValue();
            return switch (op) {
                case Add -> new IRValConstInt(l + r);
                case Sub -> new IRValConstInt(l - r);
                case Mul -> new IRValConstInt(l * r);
                case Div -> new IRValConstInt(l / r);
                case Rem -> new IRValConstInt(l % r);
                case Shl -> new IRValConstInt(l << r);
                case Shr -> new IRValConstInt(l >> r);
                case And -> new IRValConstInt(l & r);
                case Or -> new IRValConstInt(l | r);
                case Xor -> new IRValConstInt(l ^ r);
                default -> throw new RuntimeException("MathOP error");
            };
        } else if (resolvedType.equals(IRBasicType.FLOAT)) {
            var l = GetFloatNumFromCVal(lhs);
            var r = GetFloatNumFromCVal(rhs);
            return switch (op) {
                case Add -> new IRValConstFloat(l + r);
                case Sub -> new IRValConstFloat(l - r);
                case Mul -> new IRValConstFloat(l * r);
                case Div -> new IRValConstFloat(l / r);
                default -> throw new RuntimeException("MathOP error");
            };
        } else {
            throw new RuntimeException("MathOP error");
        }
    }

    public static IRValConst DoCompileTimeConversion(IRBasicType atomType, IRVal val) {
        if (val instanceof IRValConst) {
            switch (atomType) {
                case INT -> {
                    if (val instanceof IRValConstInt) {
                        return (IRValConstInt) val;
                    } else if (val instanceof IRValConstFloat) {
                        return new IRValConstInt((int) ((IRValConstFloat) val).getValue());
                    } else if (val instanceof IRValConstBool) {
                        return new IRValConstInt(((IRValConstBool) val).getValue());
                    } else {
                        throw new RuntimeException("Not a number");
                    }
                }
                case FLOAT -> {
                    if (val instanceof IRValConstInt) {
                        return new IRValConstFloat((float) ((IRValConstInt) val).getValue());
                    } else if (val instanceof IRValConstFloat) {
                        return (IRValConstFloat) val;
                    } else if (val instanceof IRValConstBool) {
                        return new IRValConstFloat(((IRValConstBool) val).getValue());
                    } else {
                        throw new RuntimeException("Not a number");
                    }
                }
                default -> throw new RuntimeException("Not a number");
            }
        } else {
            throw new RuntimeException("Not a const");
        }
    }

    public static IRValConstBool DoCompileTimeBoolConversion(IRValConst val) {
        if (val instanceof IRValConstBool) {
            return (IRValConstBool) val;
        } else if (val instanceof IRValConstInt) {
            return new IRValConstBool(((IRValConstInt) val).getValue());
        } else if (val instanceof IRValConstFloat) {
            return new IRValConstBool(((IRValConstFloat) val).getValue() == 0 ? 0 : 1);
        } else {
            throw new RuntimeException("Not a bool");
        }
    }

    public static void DoRuntimeConversion(VisitorContext C, IRBasicType atomType, IRVal val) {
        if (val instanceof IRValConst) {
            C.lastVal = DoCompileTimeConversion(atomType, val);
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
            C.lastVal = DoCompileTimeCalculation(lhsc, rhsc, op);
            return;
        }
        var resolvedType = ResolveType(lhs.getType(), rhs.getType());
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
            C.lastVal = DoCompileTimeComparison(lhsc, rhsc, op);
            return;
        }
        var resolvedType = ResolveType(lhs.getType(), rhs.getType());
        DoRuntimeConversion(C, resolvedType, lhs);
        lhs = C.lastVal;
        DoRuntimeConversion(C, resolvedType, rhs);
        rhs = C.lastVal;
        C.addAndUpdate(IRInstCmpFactory.createCmpInst(op, lhs, rhs, resolvedType));
    }

    public static void DoRuntimeBoolConversion(VisitorContext C, IRVal val) {
        if (val instanceof IRValConst) {
            C.lastVal = DoCompileTimeBoolConversion((IRValConst) val);
        } else {
            if (val.getType().isI1()) {
                C.lastVal = val;
                return;
            }
            DoRuntimeConversion(C, IRBasicType.INT, val);
            C.lastVal = C.addInst(new IRInstIcmp(IRInstIcmp.IcmpOp.NE, C.lastVal, IRValConst.Zero(IRBasicType.INT)));
        }
    }

    public static IRBasicType ResolveType(IRType t1, IRType t2) {
        if (t1.isFloat() || t2.isFloat()) {
            return IRBasicType.FLOAT;
        }
        return IRBasicType.INT;
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
