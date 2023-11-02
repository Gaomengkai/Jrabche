package icu.merky.jrabche.llvmir.types;

import java.util.Objects;

abstract public class IRType implements Cloneable {
    public IRAtomType type;

    public IRType(IRAtomType type_) {
        type = type_;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IRType irType = (IRType) o;
        return type == irType.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public abstract String toString();

    public boolean isInt() {
        return type == IRAtomType.INT;
    }

    public boolean isFloat() {
        return type == IRAtomType.FLOAT;
    }

    public boolean isVoid() {
        return type == IRAtomType.VOID;
    }

    public boolean isArray() {
        return type == IRAtomType.ARRAY;
    }

    public boolean isPointer() {
        return type == IRAtomType.POINTER;
    }

    public boolean isFunction() {
        return type == IRAtomType.FUNCTION;
    }

    public boolean isLabel() {
        return type == IRAtomType.LABEL;
    }

    public boolean isZeroInitializer() {
        return type == IRAtomType.ZEROINITIALIZER;
    }

    public IRAtomType toAtomType() {
        return type;
    }

    @Override
    public abstract IRType clone();
}
