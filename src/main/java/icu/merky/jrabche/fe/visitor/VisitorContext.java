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
import icu.merky.jrabche.llvmir.values.IRValGlobal;

import java.util.*;

import static icu.merky.jrabche.llvmir.types.PointerType.MakePointer;

public class VisitorContext {
    public BType bType = BType.INVALID;
    public GlobalSwitcher isConst = new GlobalSwitcher("isConst");
    public GlobalSwitcher needLoad = new GlobalSwitcher("needLoad");
    // symbol table global
    public Map<String, FunctionType> gFuncSymTbl = new HashMap<>();
    // local symbol table
    public LayerCtrl lc = new LayerCtrl();
    public IRBuilder builder;
    public IRVal lastVal;
    public IRType lastType;
    public FPType lastFPType;
    public BBController bbc = new BBController();
    public boolean inCond = false;
    public IC ic = new IC();
    boolean inAtarashiiFunction = false;
    Renamer renamer = new Renamer();
    Set<String> functionUsedSymbols = new HashSet<>();

    public VisitorContext(IRBuilder builder) {
        this.builder = builder;
        this.initBuiltinFunctions();
    }

    private void initBuiltinFunctions() {
        // void @llvm.memset.p0.i32(i32* %v1, i8 0, i32 12, i1 false)
        var memsetType = new FunctionType(new VoidType(), List.of(MakePointer(new IntType(32)), new IntType(8), new IntType(32), new IntType(1)));
        gFuncSymTbl.put("llvm.memset.p0.i32", memsetType);
        // i32 @getint()
        var getintType = new FunctionType(new IntType(32), List.of());
        gFuncSymTbl.put("getint", getintType);
        // i32 @getfloat()
        var getfloatType = new FunctionType(new FloatType(), List.of());
        gFuncSymTbl.put("getfloat", getfloatType);
        // void @putint(i32)
        var putintType = new FunctionType(new VoidType(), List.of(new IntType(32)));
        gFuncSymTbl.put("putint", putintType);
        // void @putfloat(float)
        var putfloatType = new FunctionType(new VoidType(), List.of(new FloatType()));
        gFuncSymTbl.put("putfloat", putfloatType);
        // i32 @getarray(i32*)
        var getarrayType = new FunctionType(new IntType(32), List.of(new PointerType(new IntType(32))));
        gFuncSymTbl.put("getarray", getarrayType);
        // i32 @getfarray(float*)
        var getfarrayType = new FunctionType(new IntType(32), List.of(new PointerType(new FloatType())));
        gFuncSymTbl.put("getfarray", getfarrayType);
        // i32 @getch()
        var getchType = new FunctionType(new IntType(32), List.of());
        gFuncSymTbl.put("getch", getchType);
        // void @putch(i32)
        var putchType = new FunctionType(new VoidType(), List.of(new IntType(32)));
        gFuncSymTbl.put("putch", putchType);
        // void @putarray(i32, i32*)
        var putarrayType = new FunctionType(new VoidType(), List.of(new IntType(32), new PointerType(new IntType(32))));
        gFuncSymTbl.put("putarray", putarrayType);
        // void @putfarray(i32, float*)
        var putfarrayType = new FunctionType(new VoidType(), List.of(new IntType(32), new PointerType(new FloatType())));
        gFuncSymTbl.put("putfarray", putfarrayType);
        // void @_sysy_starttime(i32)
        var _sysy_starttimeType = new FunctionType(new VoidType(), List.of(new IntType(32)));
        gFuncSymTbl.put("_sysy_starttime", _sysy_starttimeType);
        // void @_sysy_stoptime(i32)
        var _sysy_stoptimeType = new FunctionType(new VoidType(), List.of(new IntType(32)));
        gFuncSymTbl.put("_sysy_stoptime", _sysy_stoptimeType);

        gFuncSymTbl.forEach(builder::addFuncDeclaration);
    }

    public IRVal query(String name) {
        return lc.query(name);
    }

    public FunctionType queryFunctionType(String name) {
        return gFuncSymTbl.get(name);
    }

    public IRInst addInst(IRInst inst) {
        builder.curFunc().curBB().addInst(inst);
        return inst;
    }

    /**
     * <h3>Attention: <code>C.lastVal</code> modified.</h3>
     *
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
        IRVal val = initVal;
        // a const should be pushed to global symbol table
        var renamed = lc.inGlobal() ? name : renamer.getNextLocalConstName(name);
        val = new IRValGlobal(initVal); // const must be in global.
        val.setConst(true);
        val.setName("@" + renamed);
        lc.push(name, val);
        builder.addGlobal(renamed, val);
    }

    public String getNextRepeatName(String name) {
        return renamer.getNextLocalRepeatName(name);
    }

    public void pushVar(String name, IRVal val) {
        if (lc.inGlobal()) {
            val = new IRValGlobal(val);
            val.setName("@" + name);
            builder.addGlobal(name, val);
        }
        if (val.getName().length() > 15) {
            // hash to 16*HEX str, such as a7583658486293fa
            val.setName("vh." + Integer.toHexString(name.hashCode()));
        }
        if (functionUsedSymbols.contains(val.getName())) {
            val.setName(getNextRepeatName(val.getName()));
        }
        functionUsedSymbols.add(val.getName());
        lc.push(name, val);
    }

    public void pushConstVar(String name, IRVal val) {
        val.setConst(true);
        pushVar(name, val);
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
         * Only query local(in-func)
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

        public IRVal queryGlobal(String name) {
            // only global
            var layer = layers.get(0);
            return layer.valSymbolTable.getOrDefault(name, null);
        }
    }

    static class IC {
        /*
            // only for local array def.
    std::vector<size_t>        curShape;
    std::vector<size_t>        curArrayPos;
    size_t                     curArrayDim;
    string                     curArrId;
    shared_ptr<IRCtrl::IRType> curArrType;
    // end  for local array def
         */
        public List<Integer> curShape = new ArrayList<>();
        public List<Integer> curArrayPos = new ArrayList<>();
        public int curArrayDim;
        public IRVal curArr;
        public IRType curArrType;
    }
}
