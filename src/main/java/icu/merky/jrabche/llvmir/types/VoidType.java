package icu.merky.jrabche.llvmir.types;

public class VoidType extends IRType {
    public VoidType() {
        super(IRBasicType.VOID);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof VoidType;
    }

    @Override
    public String toString() {
        return "void";
    }

    @Override
    public IRType clone() {
        return new VoidType();
    }
}
