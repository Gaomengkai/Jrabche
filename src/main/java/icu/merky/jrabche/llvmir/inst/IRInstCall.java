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

import icu.merky.jrabche.llvmir.types.FunctionType;
import icu.merky.jrabche.llvmir.values.IRVal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IRInstCall extends IRInst {
    List<IRVal> args;
    String funcName;

    /**
     * You MUST do implicit type conversion before calling this constructor.
     *
     * @param functionType Function type.
     * @param args         Arguments.
     */
    public IRInstCall(String funcName, FunctionType functionType, List<IRVal> args) {
        super(InstID.CallInst, functionType.getRetType());
        this.args = args;
        this.funcName = funcName;
        // check args type
        if (args.size() != functionType.getParamsType().size())
            throw new RuntimeException("Argument number mismatch.");
        for (int i = 0; i < args.size(); i++) {
            if (!args.get(i).getType().equals(functionType.getParamsType().get(i)))
                throw new RuntimeException("Argument type mismatch.");
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (name != null) {
            sb.append(name).append(" = ");
        }
        sb.append("call ").append(type.toString()).append(" @").append(funcName).append("(");
        for (int i = 0; i < args.size(); i++) {
            sb.append(args.get(i).getType().toString()).append(" ").append(args.get(i).asValue());
            if (i != args.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean replace(IRVal oldVal, IRVal newVal) {
        for (int i = 0; i < args.size(); i++) {
            if (args.get(i).equals(oldVal)) {
                args.set(i, newVal);
                return true;
            }
        }
        return false;
    }

    @Override
    public String asValue() {
        return name;
    }

    @Override
    public Set<IRVal> getUses() {
        return new HashSet<>(args);
    }
}
