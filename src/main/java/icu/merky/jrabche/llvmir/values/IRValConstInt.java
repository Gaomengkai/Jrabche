package icu.merky.jrabche.llvmir.values;

import icu.merky.jrabche.llvmir.types.IntType;

public class IRValConstInt extends IRValConst implements Wordzation {
    protected int value;

    public IRValConstInt(int value) {
        super(new IntType());
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public int toWord() {
        return value;
    }

    @Override
    public IRValConstInt clone() {
        var clone = (IRValConstInt) super.clone();
        clone.value = value;
        return clone;
    }

    @Override
    public String asValue() {
        return String.valueOf(value);
    }
}