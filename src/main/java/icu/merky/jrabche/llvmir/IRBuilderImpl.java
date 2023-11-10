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

package icu.merky.jrabche.llvmir;

import icu.merky.jrabche.llvmir.structures.IRBasicBlock;
import icu.merky.jrabche.llvmir.structures.IRFunction;
import icu.merky.jrabche.llvmir.structures.IRModule;
import icu.merky.jrabche.llvmir.structures.impl.IRModuleImpl;
import icu.merky.jrabche.llvmir.types.FunctionType;
import icu.merky.jrabche.llvmir.values.IRVal;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class IRBuilderImpl implements IRBuilder {


    Map<String, IRVal> globals = new HashMap<>();
    Map<String, IRFunction> functions = new LinkedHashMap<>();
    Map<String, FunctionType> functionDeclarations = new HashMap<>();
    IRFunction curFunc;

    @Override
    public Map<String, FunctionType> getFunctionDeclarations() {
        return functionDeclarations;
    }

    @Override
    public void addFuncDeclaration(String name, FunctionType type) {
        functionDeclarations.put(name, type);
    }

    @Override
    public void addFunction(IRFunction function) {
        functions.put(function.getName(), function);
        curFunc = function;
    }

    @Override
    public void addGlobal(String name, IRVal value) {
        globals.put(name, value);
    }

    @Override
    public IRFunction curFunc() {
        return curFunc;
    }

    @Override
    public IRBasicBlock curBB() {
        return curFunc.curBB();
    }

    @Override
    public IRModule getModule() {
        IRModule module = new IRModuleImpl();
        module.setFunctions(functions);
        module.setGlobals(globals);
        module.setFunctionDeclarations(functionDeclarations);
        return module;
    }

    @Override
    public Map<String, IRVal> getGlobals() {
        return globals;
    }

    @Override
    public Map<String, IRFunction> getFunctions() {
        return functions;
    }
}
