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

import icu.merky.jrabche.llvmir.types.IRAtomType;
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

    public static float getFloatNumFromCVal(IRValConst lastVal) {
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
}
