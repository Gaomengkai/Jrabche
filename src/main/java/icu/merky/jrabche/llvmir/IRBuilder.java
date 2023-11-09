package icu.merky.jrabche.llvmir;

import icu.merky.jrabche.llvmir.structures.IRBasicBlock;
import icu.merky.jrabche.llvmir.structures.IRFunction;
import icu.merky.jrabche.llvmir.structures.IRModule;
import icu.merky.jrabche.llvmir.types.FunctionType;
import icu.merky.jrabche.llvmir.values.IRVal;

import java.util.Map;

public interface IRBuilder {
    Map<String, IRVal> getGlobals();

    Map<String, IRFunction> getFunctions();

    Map<String, FunctionType> getFunctionDeclarations();

    void addFuncDeclaration(String name, FunctionType type);

    void addFunction(IRFunction function);

    void addGlobal(String name, IRVal value);

    IRFunction curFunc();

    IRBasicBlock curBB();
    IRModule getModule();
}
