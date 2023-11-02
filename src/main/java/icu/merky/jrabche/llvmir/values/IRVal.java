package icu.merky.jrabche.llvmir.values;

import icu.merky.jrabche.llvmir.types.ArrayType;
import icu.merky.jrabche.llvmir.types.IRType;

abstract public class IRVal implements Cloneable, ValueRepresentable {
    protected IRType type;
    protected String name;
    protected boolean _const = false;

    public IRVal(IRType type, String name) {
        this.type = type;
        this.name = name;
    }

    public IRVal(IRType type) {
        this.type = type;
    }

    public boolean isConst() {
        return _const;
    }

    public void setConst(boolean _const) {
        this._const = _const;
    }

    public IRType getType() {
        return type;
    }

    public void setType(IRType type) {
        this.type = type;
    }

    public ArrayType getArrayType() {
        if (type instanceof ArrayType arrayType) {
            return arrayType;
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public IRVal clone() {
        try {
            IRVal clone = (IRVal) super.clone();
            clone.type = type.clone();
            clone.name = name;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
