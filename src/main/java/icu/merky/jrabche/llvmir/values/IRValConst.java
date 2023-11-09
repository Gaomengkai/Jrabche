package icu.merky.jrabche.llvmir.values;

import icu.merky.jrabche.llvmir.types.IRBasicType;
import icu.merky.jrabche.llvmir.types.IRType;

public abstract class IRValConst extends IRVal {
    public IRValConst(IRType type) {
        super(type);
        setConst(true);
    }

    public boolean isZero() {
        if (this instanceof IRValConstInt) {
            return ((IRValConstInt) this).getValue() == 0;
        } else if(this instanceof IRValConstFloat) {
            return ((IRValConstFloat) this).getValue() == 0;
        } else if(this instanceof IRValConstBool) {
            return ((IRValConstBool) this).getValue() == 0;
        }
        return false;
    }

    public  static IRValConstInt ZeroInt() {
        return new IRValConstInt(0);
    }
    public static IRValConstFloat ZeroFloat() {
        return new IRValConstFloat(0);
    }
    public static IRValConstBool ZeroBool() {
        return new IRValConstBool(0);
    }
    public static IRValConst Zero(IRBasicType type) {
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
