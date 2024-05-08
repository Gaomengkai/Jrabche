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

package icu.merky.jrabche.llvmir.support;

import icu.merky.jrabche.llvmir.inst.IRInstIcmp;
import icu.merky.jrabche.llvmir.inst.IRInstMath;
import icu.merky.jrabche.llvmir.types.IRBasicType;
import icu.merky.jrabche.llvmir.types.IRType;
import icu.merky.jrabche.llvmir.values.*;

public class IRCompileTimeCalc {
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

    public static int compare2ValConst(IRValConst lhs, IRValConst rhs) {
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

    public static IRBasicType ResolveType(IRType t1, IRType t2) {
        if (t1.isFloat() || t2.isFloat()) {
            return IRBasicType.FLOAT;
        }
        return IRBasicType.INT;
    }
}
