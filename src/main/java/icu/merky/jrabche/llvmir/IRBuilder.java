package icu.merky.jrabche.llvmir;

import icu.merky.jrabche.llvmir.structures.IRBasicBlock;
import icu.merky.jrabche.llvmir.structures.IRFunction;
import icu.merky.jrabche.llvmir.values.IRValConst;

public interface IRBuilder {
    void addFunction(IRFunction function);

    void addGlobal(String name, IRValConst value);

    IRFunction curFunc();

    IRBasicBlock curBB();
}
