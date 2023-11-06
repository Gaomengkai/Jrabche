package icu.merky.jrabche.llvmir.types;

import java.util.Objects;

abstract public class IRType implements Cloneable {
    public IRBasicType type;

    public IRType(IRBasicType type_) {
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
        return type == IRBasicType.INT;
    }
    public boolean isI32() {
        if(type == IRBasicType.INT) {
            return ((IntType)this).getBitWidth() == 32;
        }
        return false;
    }
    public boolean isI1() {
        if(type == IRBasicType.INT) {
            return ((IntType)this).getBitWidth() == 1;
        }
        return false;
    }

    public boolean isFloat() {
        return type == IRBasicType.FLOAT;
    }

    public boolean isVoid() {
        return type == IRBasicType.VOID;
    }

    public boolean isArray() {
        return type == IRBasicType.ARRAY;
    }

    public boolean isPointer() {
        return type == IRBasicType.POINTER;
    }

    public boolean isFunction() {
        return type == IRBasicType.FUNCTION;
    }

    public boolean isLabel() {
        return type == IRBasicType.LABEL;
    }

    public boolean isZeroInitializer() {
        return type == IRBasicType.ZEROINITIALIZER;
    }

    public IRBasicType toBasicType() {
        return type;
    }
    public IRBasicType getBasicType() {return type;}

    @Override
    public abstract IRType clone();
}
