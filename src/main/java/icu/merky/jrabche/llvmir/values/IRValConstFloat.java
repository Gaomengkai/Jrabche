package icu.merky.jrabche.llvmir.values;

import icu.merky.jrabche.llvmir.types.FloatType;

import java.math.BigInteger;

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

    static public IRValConstFloat FromHexBits(long value) {
        // ((float)*reinterpret_cast<double*>(&value_))
        double v = Double.longBitsToDouble(value);
        return new IRValConstFloat((float) v);
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
        long lNum = Double.doubleToLongBits(value);
        BigInteger bigInteger = BigInteger.valueOf(lNum);
        if (bigInteger.compareTo(BigInteger.ZERO) < 0) {
            bigInteger = bigInteger.add(BigInteger.ONE.shiftLeft(64));
        }
        return "0x" + bigInteger.toString(16);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IRValConstFloat && ((IRValConstFloat) obj).value == value;
    }
}
