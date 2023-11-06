package icu.merky.jrabche.llvmir.values;

import icu.merky.jrabche.llvmir.types.IntType;

import java.util.Objects;

public class IRValConstBool extends IRValConst implements Wordzation {
    protected int value;

    public IRValConstBool(int value) {
        super(new IntType(1));
        this.value=value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public int toWord() {
        return value;
    }

    @Override
    public IRValConstBool clone() {
        var clone = (IRValConstBool) super.clone();
        clone.value = value;
        return clone;
    }

    @Override
    public String asValue() {
        if (value != 0) return "true";
        return "false";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IRValConstBool that = (IRValConstBool) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
