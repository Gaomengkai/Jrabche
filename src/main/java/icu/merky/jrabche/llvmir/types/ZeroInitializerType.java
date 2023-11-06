package icu.merky.jrabche.llvmir.types;

public class ZeroInitializerType extends IRType {
    public ZeroInitializerType() {
        super(IRBasicType.ZEROINITIALIZER);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ZeroInitializerType;
    }

    @Override
    public String toString() {
        return "zeroinitializer";
    }

    @Override
    public IRType clone() {
        return new ZeroInitializerType();
    }
}
