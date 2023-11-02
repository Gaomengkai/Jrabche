package icu.merky.jrabche.llvmir.values;

import icu.merky.jrabche.llvmir.types.ZeroInitializerType;

public class IRValZeroInitializer extends IRValConst {
    public IRValZeroInitializer() {
        super(new ZeroInitializerType());
    }

    @Override
    public String asValue() {
        return "zeroinitializer";
    }
}
