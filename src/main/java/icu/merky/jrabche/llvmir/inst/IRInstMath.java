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

import icu.merky.jrabche.helper.Helper;
import icu.merky.jrabche.llvmir.values.IRVal;

public class IRInstMath extends IRInst {
    MathOP mathOP;
    IRVal lhs, rhs;
    public IRInstMath(MathOP mathOP, IRVal v1, IRVal v2) {
        super(null, InstID.MathInst, Helper.ResolveType(v1.getType(), v2.getType()).toIRType());
        this.mathOP = mathOP;
        this.lhs = v1;
        this.rhs = v2;
    }

    @Override
    public String toString() {
        // %v18 = add i32 %v16, %v17
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" = ");
        if (this.type.isFloat())
            sb.append("f");
        else if (this.mathOP == MathOP.Rem || this.mathOP == MathOP.Div)
            sb.append("s");
        sb.append(mathOP.toString().toLowerCase()).append(" ");
        sb.append(type.toString()).append(" ");
        sb.append(lhs.asValue()).append(", ").append(rhs.asValue());
        return sb.toString();
    }

    @Override
    public boolean replace(IRVal inst, IRVal newInst) {
        if (lhs.equals(inst)) {
            lhs = newInst;
            return true;
        }
        if (rhs.equals(inst)) {
            rhs = newInst;
            return true;
        }
        return false;
    }

    @Override
    public String asValue() {
        return name;
    }

    public enum MathOP {
        Invalid, Add, Sub, Mul, Div, Rem, Shl, Shr, And, Or, Xor
    }
}
