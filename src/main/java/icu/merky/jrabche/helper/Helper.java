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
import icu.merky.jrabche.llvmir.inst.IRInstMath;
import icu.merky.jrabche.llvmir.inst.IRInstUnary;
import icu.merky.jrabche.llvmir.types.IRAtomType;
import icu.merky.jrabche.llvmir.types.IRType;
import icu.merky.jrabche.llvmir.values.*;

public class Helper {
    public static int GetIntNumFromCVal(IRValConst lastVal) {
        if (lastVal instanceof IRValConstInt intVal) {
            return intVal.getValue();
        } else if (lastVal instanceof IRValConstFloat floatVal) {
            return (int) floatVal.getValue();
        } else if (lastVal instanceof IRValConstI1 boolVal) {
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
        } else if (lastVal instanceof IRValConstI1 boolVal) {
            return boolVal.getValue();
        } else {
            throw new RuntimeException("Not a number");
        }
    }

    public static IRValConst DoCompileTimeCalculation(IRValConst lhs, IRValConst rhs, IRInstMath.MathOP op) {
        var resolvedType = ResolveType(lhs.getType(), rhs.getType());
        var resolvedAtomType = resolvedType.toAtomType();
        lhs = DoCompileTimeConversion(resolvedAtomType, lhs);
        rhs = DoCompileTimeConversion(resolvedAtomType, rhs);
        if (resolvedAtomType.equals(IRAtomType.INT)) {
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
        } else if (resolvedAtomType.equals(IRAtomType.FLOAT)) {
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

    public static IRValConst DoCompileTimeConversion(IRAtomType atomType, IRVal val) {
        if (val instanceof IRValConst) {
            switch (atomType) {
                case INT -> {
                    if (val instanceof IRValConstInt) {
                        return (IRValConstInt) val;
                    } else if (val instanceof IRValConstFloat) {
                        return new IRValConstInt((int) ((IRValConstFloat) val).getValue());
                    } else if (val instanceof IRValConstI1) {
                        return new IRValConstInt(((IRValConstI1) val).getValue());
                    } else {
                        throw new RuntimeException("Not a number");
                    }
                }
                case FLOAT -> {
                    if (val instanceof IRValConstInt) {
                        return new IRValConstFloat(((IRValConstInt) val).getValue());
                    } else if (val instanceof IRValConstFloat) {
                        return (IRValConstFloat) val;
                    } else if (val instanceof IRValConstI1) {
                        return new IRValConstFloat(((IRValConstI1) val).getValue());
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

    public static void DoRuntimeConversion(VisitorContext C, IRAtomType atomType, IRVal val) {
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
            }
        }
    }

    public static IRType ResolveType(IRType t1, IRType t2) {
        return (t1.isFloat() || t2.isFloat()) ? (t1.isFloat() ? t1 : t2) : t1;
    }
}
