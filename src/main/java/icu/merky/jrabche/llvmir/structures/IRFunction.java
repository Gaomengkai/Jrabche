package icu.merky.jrabche.llvmir.structures;

import icu.merky.jrabche.fe.visitor.FPType;
import icu.merky.jrabche.llvmir.inst.IRInstAlloca;
import icu.merky.jrabche.llvmir.types.FunctionType;
import icu.merky.jrabche.llvmir.types.IRType;
import icu.merky.jrabche.llvmir.values.IRValFP;

import java.util.List;

public interface IRFunction {
    FunctionType getFunctionType();

    String getName();

    void setName(String name);

    void addParam(String name, IRType type);

    IRValFP addFP(FPType ty);

    void addBlock(String name);

    void setCurrentBlock(String name);

    void setCurrentBlock(IRBasicBlock block);

    IRBasicBlock curBB();

    IRBasicBlock entryBB();

    IRBasicBlock getBlock(String name);

    List<IRBasicBlock> getBlocks();
    IRInstAlloca addAlloca(IRInstAlloca inst);
    List<IRInstAlloca> getAlloca();

    void finishFunction();
}
