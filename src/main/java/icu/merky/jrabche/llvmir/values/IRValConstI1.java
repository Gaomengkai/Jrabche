package icu.merky.jrabche.llvmir.values;

import icu.merky.jrabche.llvmir.types.IntType;

public class IRValConstI1 extends IRValConst implements Wordzation {
    protected int value;

    public IRValConstI1(int value) {
        super(new IntType(1));
    }

    public int getValue() {
        return value;
    }

    @Override
    public int toWord() {
        return value;
    }

    @Override
    public IRValConstI1 clone() {
        var clone = (IRValConstI1) super.clone();
        clone.value = value;
        return clone;
    }

    @Override
    public String asValue() {
        if (value != 0) return "true";
        return "false";
    }
}
