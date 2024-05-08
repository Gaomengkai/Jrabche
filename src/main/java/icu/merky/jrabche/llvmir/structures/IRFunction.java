package icu.merky.jrabche.llvmir.structures;

import icu.merky.jrabche.llvmir.inst.IRInstAlloca;
import icu.merky.jrabche.llvmir.types.FPType;
import icu.merky.jrabche.llvmir.types.FunctionType;
import icu.merky.jrabche.llvmir.types.IRType;
import icu.merky.jrabche.llvmir.values.IRValFP;

import java.util.List;
import java.util.Map;

public abstract class IRFunction {
    // SSA defines
    boolean inSSA = false;

    public void leaveSSA() {
        inSSA = false;
    }

    public boolean isInSSA() {
        return inSSA;
    }

    public void enterSSA() {
        inSSA = true;
    }

    // IR defines
    public abstract FunctionType getFunctionType();

    public abstract String getName();

    public abstract void setName(String name);

    public abstract void addParam(String name, IRType type);

    public abstract IRValFP addFP(FPType ty);

    public abstract IRBasicBlock addBlock();

    public abstract void setCurrentBlock(String name);

    public abstract void setCurrentBlock(IRBasicBlock block);

    public abstract IRBasicBlock curBB();

    public abstract IRBasicBlock entryBB();

    public abstract IRBasicBlock getBlock(String name);

    public abstract List<IRBasicBlock> getBlocks();

    public abstract void removeBlock(IRBasicBlock block);

    public abstract IRInstAlloca addAlloca(IRInstAlloca inst);

    public abstract List<IRInstAlloca> getAlloca();

    public abstract void finishFunction();

    public abstract Map<String, IRValFP> getFp();
}
