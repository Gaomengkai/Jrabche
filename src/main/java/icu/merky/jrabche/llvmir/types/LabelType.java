package icu.merky.jrabche.llvmir.types;

public class LabelType extends IRType {
    public LabelType() {
        super(IRAtomType.LABEL);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof LabelType;
    }

    @Override
    public String toString() {
        return "label";
    }

    @Override
    public IRType clone() {
        return new LabelType();
    }
}
