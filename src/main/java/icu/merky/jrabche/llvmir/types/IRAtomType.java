package icu.merky.jrabche.llvmir.types;

public enum IRAtomType {
    INVALID, INT, FLOAT, VOID, ARRAY, POINTER, FUNCTION, LABEL, ZEROINITIALIZER;

    public IRType toIRType() {
        return switch (this) {
            case INT -> new IntType();
            case FLOAT -> new FloatType();
            case VOID -> new VoidType();
            default -> throw new RuntimeException("Invalid IRAtomType");
        };
    }
}
