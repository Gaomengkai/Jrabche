package icu.merky.jrabche.llvmir.types;

import java.util.LinkedList;
import java.util.List;

public class ArrayType extends IRType {
    private IRType elementType;
    private int size;
    private IRBasicType atomType = IRBasicType.INVALID;

    public ArrayType(int size, IRType elementType) {
        super(IRBasicType.ARRAY);
        this.size = size;
        this.elementType = elementType;
    }

    public static ArrayType FromShape(IRBasicType atomType, List<Integer> shape) {
        if (shape.size() == 0) {
            throw new RuntimeException("Shape must be non-empty");
        }
        if (shape.size() == 1) {
            return new ArrayType(shape.get(0), atomType.toIRType());
        }
        return new ArrayType(shape.get(0), FromShape(atomType, shape.subList(1, shape.size())));
    }

    public IRType getElementType() {
        return elementType;
    }

    public void setElementType(IRType elementType) {
        this.elementType = elementType;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public IRBasicType getAtomType() {
        if (atomType == IRBasicType.INVALID) {
            if (elementType.isInt()) {
                atomType = IRBasicType.INT;
            } else if (elementType.isFloat()) {
                atomType = IRBasicType.FLOAT;
            } else if (elementType.isArray()) {
                atomType = ((ArrayType) elementType).getAtomType();
            } else {
                throw new RuntimeException("Unknown atom type");
            }
        }
        return atomType;
    }

    public List<Integer> getShape() {
        if (elementType.isArray()) {
            List<Integer> shape = ((ArrayType) elementType).getShape();
            shape.add(0, size);
            return shape;
        } else {
            return new LinkedList<>(List.of(size));
        }
    }

    public int getSizeBytes() {
        if (size == -1) {
            // pointer
            return 8;
        } else if (elementType.isInt()) {
            return size * ((IntType) elementType).getBitWidth() / 8;
        } else if (elementType.isFloat()) {
            return size * 4;
        } else if (elementType.isArray()) {
            return size * ((ArrayType) elementType).getSizeBytes();
        } else {
            throw new RuntimeException("Unknown atom type");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ArrayType arr) {
            return arr.elementType.equals(elementType) && arr.size == size;
        }
        return false;
    }

    @Override
    public String toString() {
        return "[" + size + " x " + elementType.toString() + "]";
    }

    @Override
    public IRType clone() {
        return new ArrayType(size, elementType.clone());
    }
}
