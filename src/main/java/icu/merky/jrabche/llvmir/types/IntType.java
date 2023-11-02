package icu.merky.jrabche.llvmir.types;

public class IntType extends IRType {
    private int bitWidth = 32;

    public IntType() {
        super(IRAtomType.INT);
    }

    public IntType(int bitWidth) {
        this();
        this.bitWidth = bitWidth;
    }

    public int getBitWidth() {
        return bitWidth;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof IntType) {
            return bitWidth == ((IntType) o).bitWidth;
        }
        return false;
    }

    @Override
    public String toString() {
        return "i" + bitWidth;
    }

    @Override
    public IRType clone() {
        return new IntType(bitWidth);
    }

}
