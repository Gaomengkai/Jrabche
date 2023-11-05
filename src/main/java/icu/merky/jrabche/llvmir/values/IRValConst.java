package icu.merky.jrabche.llvmir.values;

import icu.merky.jrabche.llvmir.types.IRAtomType;
import icu.merky.jrabche.llvmir.types.IRType;

public abstract class IRValConst extends IRVal {
    public IRValConst(IRType type) {
        super(type);
        setConst(true);
    }

    public  static IRValConstInt ZeroInt() {
        return new IRValConstInt(0);
    }
    public static IRValConstFloat ZeroFloat() {
        return new IRValConstFloat(0);
    }
    public static IRValConstI1 ZeroBool() {
        return new IRValConstI1(0);
    }
    public static IRValConst Zero(IRAtomType type) {
        switch (type) {
            case INT -> {
                return ZeroInt();
            }
            case FLOAT -> {
                return ZeroFloat();
            }
            default -> {
                throw new RuntimeException("Unknown type: " + type);
            }
        }
    }
    @Override
    public IRValConst clone() {
        var clone = (IRValConst) super.clone();
        clone.setConst(true);
        return clone;
    }

}
