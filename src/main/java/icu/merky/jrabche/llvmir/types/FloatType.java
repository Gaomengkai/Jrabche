package icu.merky.jrabche.llvmir.types;

public class FloatType extends IRType {
    public FloatType() {
        super(IRBasicType.FLOAT);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof FloatType;
    }

    @Override
    public String toString() {
        return "float";
    }

    @Override
    public IRType clone() {
        return new FloatType();
    }
}
