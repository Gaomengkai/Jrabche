package icu.merky.jrabche.llvmir.values;

import icu.merky.jrabche.llvmir.types.FloatType;

public class IRValConstFloat extends IRValConst implements Wordzation {
    protected float value;

    private IRValConstFloat() {
        super(new FloatType());
    }

    public IRValConstFloat(float value) {
        this();
        this.value = value;
    }

    public IRValConstFloat(double value) {
        this((float) value);
    }

    public IRValConstFloat(long value) {
        this();
        // ((float)*reinterpret_cast<double*>(&value_))
        double v = Double.longBitsToDouble(value);
        this.value = (float) v;
    }

    public float getValue() {
        return value;
    }

    @Override
    public int toWord() {
        return Float.floatToIntBits(value);
    }

    @Override
    public IRValConstFloat clone() {
        var clone = (IRValConstFloat) super.clone();
        clone.value = value;
        return clone;
    }

    @Override
    public String asValue() {
        return String.valueOf(Double.doubleToLongBits(value));
    }
}
