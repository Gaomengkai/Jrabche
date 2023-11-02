package icu.merky.jrabche.llvmir.structures;

import icu.merky.jrabche.llvmir.types.IRType;

import java.util.List;

public interface IRFunction {
    String getName();

    void setName(String name);

    void addParam(String name, IRType type);

    void addBlock(String name);

    void setCurrentBlock(String name);

    void setCurrentBlock(IRBasicBlock block);

    IRBasicBlock curBB();

    IRBasicBlock entryBB();

    IRBasicBlock getBlock(String name);

    List<IRBasicBlock> getBlocks();
}
