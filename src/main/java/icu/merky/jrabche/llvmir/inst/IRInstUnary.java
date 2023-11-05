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

package icu.merky.jrabche.llvmir.inst;

import icu.merky.jrabche.llvmir.types.FloatType;
import icu.merky.jrabche.llvmir.types.IntType;
import icu.merky.jrabche.llvmir.values.IRVal;

public class IRInstUnary  extends IRInst{
    public enum UnaryOP {
        Invalid, FpToSi, SiToFp, ZExt, SExt
    }
    UnaryOP unaryOP;
    IRVal v1;
    public IRInstUnary(UnaryOP unaryOP, IRVal v1) {
        super(null, InstID.ConvertInst, unaryOP==UnaryOP.SiToFp?new FloatType():new IntType());
        this.unaryOP=unaryOP;
        this.v1=v1;
    }
    @Override
    public String toString() {
        // %Val = zext i1 %value to i32
        return switch (unaryOP) {
            case FpToSi -> name + " = fptosi " + v1.getType().toString()+ " " + v1.asValue() + " to " + type.toString();
            case SiToFp -> name + " = sitofp " + v1.getType().toString() + " " + v1.asValue() + " to " + type.toString();
            case ZExt -> name + " = zext " + v1.getType().toString()+ " " + v1.asValue() + " to " + type.toString();
            case SExt -> name + " = sext " + v1.getType().toString() + " " + v1.asValue() + " to " + type.toString();
            default -> null;
        };
    }

    @Override
    public boolean replace(IRVal inst, IRVal newInst) {
        if (v1 == inst) {
            v1 = newInst;
            return true;
        }
        return false;
    }

    @Override
    public String asValue() {
        return name;
    }
}
