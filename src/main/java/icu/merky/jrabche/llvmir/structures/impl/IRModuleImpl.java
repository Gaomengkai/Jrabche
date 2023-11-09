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

package icu.merky.jrabche.llvmir.structures.impl;

import icu.merky.jrabche.llvmir.structures.IRFunction;
import icu.merky.jrabche.llvmir.structures.IRModule;
import icu.merky.jrabche.llvmir.types.FunctionType;
import icu.merky.jrabche.llvmir.values.IRVal;
import icu.merky.jrabche.llvmir.values.IRValGlobal;

import java.util.Map;

public class IRModuleImpl implements IRModule {
    Map<String, IRVal> globals;
    Map<String, IRFunction> functions;
    Map<String, FunctionType> functionDeclarations;

    @Override
    public Map<String, FunctionType> getFunctionDeclarations() {
        return functionDeclarations;
    }

    @Override
    public void setFunctionDeclarations(Map<String, FunctionType> functionDeclarations) {
        this.functionDeclarations = functionDeclarations;
    }

    @Override
    public Map<String, IRVal> getGlobals() {
        return globals;
    }

    @Override
    public void setGlobals(Map<String, IRVal> globals) {
        this.globals = globals;
    }

    @Override
    public Map<String, IRFunction> getFunctions() {
        return functions;
    }

    @Override
    public void setFunctions(Map<String, IRFunction> functions) {
        this.functions = functions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // globals.
        // declare i32 @getarray(i32*)
        for (Map.Entry<String, FunctionType> entry : functionDeclarations.entrySet()) {
            String key = entry.getKey();
            FunctionType fType = entry.getValue();
            sb.append("declare ").append(fType.getRetType()).append(" @").append(key);
            sb.append("(");
            for (int i = 0; i < fType.getParamsType().size(); i++) {
                sb.append(fType.getParamsType().get(i));
                if (i != fType.getParamsType().size() - 1)
                    sb.append(", ");
            }
            sb.append(")\n");
        }
        sb.append("\n");

        // @dd = [constant] global [2 x [3 x i32]] zeroinitializer
        globals.forEach((name, val) -> {
            if (val instanceof IRValGlobal vg) {
                sb.append(vg.getName()).append(" = ");
                if (vg.isConst()) {
                    sb.append("constant ");
                } else {
                    sb.append("global ");
                }
                // if(vg.getElement().getType().isScalar()) {
                //     sb.append(vg.getElement().getType()).append(" ");
                // }
                sb.append(vg.getElement().getType()).append(" ");
                sb.append(vg.getElement().asValue()).append("\n");
            } else {
                sb.append("@").append(name).append(" = global ")
                        .append(val.getType())
                        .append(" ")
                        .append(val.asValue()).append("\n");
            }
        });

        // functions.
        functions.forEach((name, function) -> sb.append(function.toString()));
        return sb.toString();
    }
}
