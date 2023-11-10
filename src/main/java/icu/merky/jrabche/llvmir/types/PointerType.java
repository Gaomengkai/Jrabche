package icu.merky.jrabche.llvmir.types;

public class PointerType extends IRType {
    private IRType elementType;

    public PointerType(IRType elementType) {
        super(IRBasicType.POINTER);
        this.elementType = elementType;
    }

    static public PointerType MakePointer(IRType elementType) {
        return new PointerType(elementType);
    }

    static public PointerType MakePointer(IRType elementType, int level) {
        PointerType pointerType = new PointerType(elementType);
        for (int i = 1; i < level; i++) {
            pointerType = new PointerType(pointerType);
        }
        return pointerType;
    }

    static public IRType DePointer(IRType type) {
        if (type instanceof PointerType p) {
            return p.getElementType();
        }
        throw new RuntimeException("Not a pointer");
    }

    static public IRType DePointer(IRType type, int level) {
        for (int i = 0; i < level; i++) {
            type = DePointer(type);
        }
        return type;
    }

    static public int PointerLevel(IRType type) {
        int level = 0;
        while (type instanceof PointerType p) {
            level++;
            type = p.getElementType();
        }
        return level;
    }

    public int getPointerLevel() {
        return PointerLevel(this);
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
