package icu.merky.jrabche.llvmir.types;

public class PointerType extends IRType {
    private IRType elementType;

    public PointerType(IRType elementType) {
        super(IRAtomType.POINTER);
        this.elementType = elementType;
    }

    static public PointerType MakePointer(IRType elementType) {
        return new PointerType(elementType);
    }

    static public IRType dePointer(IRType type) {
        if (type instanceof PointerType p) {
            return p.getElementType();
        }
        throw new RuntimeException("Not a pointer");
    }

    public IRType getElementType() {
        return elementType;
    }

    public void setElementType(IRType elementType) {
        this.elementType = elementType;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PointerType p) {
            return this.getElementType().equals(p.getElementType());
        }
        return false;
    }

    @Override
    public String toString() {
        return elementType.toString() + "*";
    }

    @Override
    public IRType clone() {
        return new PointerType(elementType.clone());
    }
}
