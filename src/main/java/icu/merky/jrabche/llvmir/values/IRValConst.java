package icu.merky.jrabche.llvmir.values;

import icu.merky.jrabche.llvmir.types.IRType;

public abstract class IRValConst extends IRVal {
    public IRValConst(IRType type) {
        super(type);
        setConst(true);
    }

    @Override
    public IRValConst clone() {
        var clone = (IRValConst) super.clone();
        clone.setConst(true);
        return clone;
    }

}
