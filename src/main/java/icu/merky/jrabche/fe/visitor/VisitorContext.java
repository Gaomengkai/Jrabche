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

package icu.merky.jrabche.fe.visitor;

import icu.merky.jrabche.llvmir.IRBuilder;
import icu.merky.jrabche.llvmir.inst.IRInst;
import icu.merky.jrabche.llvmir.inst.IRInstAlloca;
import icu.merky.jrabche.llvmir.types.*;
import icu.merky.jrabche.llvmir.values.IRVal;
import icu.merky.jrabche.llvmir.values.IRValConst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VisitorContext {
    public BType bType = BType.INVALID;
    public GlobalSwitcher isConst = new GlobalSwitcher("isConst");
    public GlobalSwitcher needLoad = new GlobalSwitcher("needLoad");
    // symbol table global
    public Map<String, FunctionType> globalFunctionSymbolTable = new HashMap<>();
    // local symbol table
    public LayerCtrl lc = new LayerCtrl();
    public IRBuilder builder;
    public IRVal lastVal;
    public IRType lastType;
    public FPType lastFPType;
    public BBController bbController = new BBController();
    boolean inAtarashiiFunction = false;
    Renamer renamer = new Renamer();

    public VisitorContext(IRBuilder builder) {
        this.builder = builder;
        this.initBuiltinFunctions();
    }

    private void initBuiltinFunctions() {
        // void @llvm.memset.p0.i32(i32* %v1, i8 0, i32 12, i1 false)
        var memsetType = new FunctionType(new VoidType(), List.of(new IntType(32), new IntType(8), new IntType(32), new IntType(1)));
        globalFunctionSymbolTable.put("llvm.memset.p0.i32", memsetType);
        // i32 @getint()
        var getintType = new FunctionType(new IntType(32), List.of());
        globalFunctionSymbolTable.put("getint", getintType);
        // i32 @getfloat()
        var getfloatType = new FunctionType(new FloatType(), List.of());
        globalFunctionSymbolTable.put("getfloat", getfloatType);
        // void @putint(i32)
        var putintType = new FunctionType(new VoidType(), List.of(new IntType(32)));
        globalFunctionSymbolTable.put("putint", putintType);
        // void @putfloat(float)
        var putfloatType = new FunctionType(new VoidType(), List.of(new FloatType()));
        globalFunctionSymbolTable.put("putfloat", putfloatType);
        // i32 @getarray(i32*)
        var getarrayType = new FunctionType(new IntType(32), List.of(new PointerType(new IntType(32))));
        globalFunctionSymbolTable.put("getarray", getarrayType);
        // i32 @getfarray(float*)
        var getfarrayType = new FunctionType(new FloatType(), List.of(new PointerType(new FloatType())));
        globalFunctionSymbolTable.put("getfarray", getfarrayType);
        // i32 @getch()
        var getchType = new FunctionType(new IntType(32), List.of());
        globalFunctionSymbolTable.put("getch", getchType);
        // void @putch(i32)
        var putchType = new FunctionType(new VoidType(), List.of(new IntType(32)));
        globalFunctionSymbolTable.put("putch", putchType);
        // void @putarray(i32, i32*)
        var putarrayType = new FunctionType(new VoidType(), List.of(new IntType(32), new PointerType(new IntType(32))));
        globalFunctionSymbolTable.put("putarray", putarrayType);
        // void @putfarray(i32, float*)
        var putfarrayType = new FunctionType(new VoidType(), List.of(new IntType(32), new PointerType(new FloatType())));
        globalFunctionSymbolTable.put("putfarray", putfarrayType);
        // void @_sysy_starttime(i32)
        var _sysy_starttimeType = new FunctionType(new VoidType(), List.of(new IntType(32)));
        globalFunctionSymbolTable.put("_sysy_starttime", _sysy_starttimeType);
        // void @_sysy_stoptime(i32)
        var _sysy_stoptimeType = new FunctionType(new VoidType(), List.of(new IntType(32)));
        globalFunctionSymbolTable.put("_sysy_stoptime", _sysy_stoptimeType);
    }

    public void push(String name, IRVal val) {
        lc.push(name, val);
    }

    public IRVal query(String name) {
        return lc.query(name);
    }

    public FunctionType queryFunctionType(String name) {
        return globalFunctionSymbolTable.get(name);
    }

    public IRInst addInst(IRInst inst) {
        builder.curFunc().curBB().addInst(inst);
        return inst;
    }

    /**
     * <h3>Attention: <code>C.lastVal</code> modified.</h3>
     * @param inst instruction to be added
     */
    public void addAndUpdate(IRInst inst) {
        lastVal = addInst(inst);
    }

    public IRInstAlloca addAlloca(IRInstAlloca alloca) {
        return builder.curFunc().addAlloca(alloca);
    }

    /**
     * <h3>Attention: <code>builder.addGlobal</code> called.</h3>
     *
     * @param name    name of the variable
     * @param initVal initial value of the variable
     */
    public void pushConst(String name, IRValConst initVal) {
        // a const should be pushed to global symbol table
        var renamed = lc.inGlobal() ? name : renamer.getNextLocalConstName(name);
        lc.push(renamed, initVal);
        builder.addGlobal(renamed, initVal);
    }

    static class Layer {
        public Map<String, IRVal> valSymbolTable = new HashMap<>();
    }

    static class LayerCtrl {
        public Layer cur;
        // layer0 is global
        List<Layer> layers = new ArrayList<>();

        public LayerCtrl() {
            layers.add(new Layer());
            cur = layers.get(0);
        }

        public void dive() {
            cur = new Layer();
            layers.add(cur);
        }

        public void ascend() {
            layers.remove(layers.size() - 1);
            cur = layers.get(layers.size() - 1);
        }

        public int getLayerCount() {
            return layers.size();
        }

        public int getLayerIndex() {
            return layers.size() - 1;
        }

        public boolean inGlobal() {
            return layers.size() == 1;
        }

        public void push(String name, IRVal val) {
            cur.valSymbolTable.put(name, val);
        }

        public IRVal query(String name) {
            for (int i = layers.size() - 1; i >= 0; i--) {
                var layer = layers.get(i);
                if (layer.valSymbolTable.containsKey(name)) {
                    return layer.valSymbolTable.get(name);
                }
            }
            return null;
        }

        /**
         * Only query local
         *
         * @param name name of the variable
         * @return null if not found
         */
        public IRVal queryLocal(String name) {
            // only local
            for (int i = layers.size() - 1; i > 0; i--) {
                var layer = layers.get(i);
                if (layer.valSymbolTable.containsKey(name)) {
                    return layer.valSymbolTable.get(name);
                }
            }
            return null;
        }
    }
}
