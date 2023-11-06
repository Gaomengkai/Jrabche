package icu.merky.jrabche.llvmir.types;

public class InvalidType extends IRType {
    public InvalidType() {
        super(IRBasicType.INVALID);
    }


    @Override
    public String toString() {
        throw new RuntimeException("InvalidType.toString()");
    }

    @Override
    public IRType clone() {
        return new InvalidType();
    }
}
