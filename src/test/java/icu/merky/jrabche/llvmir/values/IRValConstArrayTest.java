package icu.merky.jrabche.llvmir.values;

import icu.merky.jrabche.llvmir.types.ArrayType;
import icu.merky.jrabche.llvmir.types.IntType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class IRValConstArrayTest {
    @Test
    public void ArrayShapeShouldBeStored() {
        var arrType1 = new ArrayType(3,new ArrayType(2,new IntType()));
        var arrayVal = new IRValConstArray(arrType1);
        Assertions.assertEquals(3,arrayVal.getShapes().get(0));
        Assertions.assertEquals(2,arrayVal.getShapes().get(1));
        Assertions.assertEquals(2,arrayVal.getShapes().size());
    }
    @Test
    public void ArrayValTypeShouldBeInitializedAsZERO() {
        var arrType1 = new ArrayType(3,new ArrayType(2,new IntType()));
        var array= new IRValConstArray(arrType1);
        Assertions.assertEquals(3, array.getValTypes().size());
        Assertions.assertEquals(IRValConstArray.ValType.ZERO,array.getValTypes().get(0));
        Assertions.assertEquals(IRValConstArray.ValType.ZERO,array.getValTypes().get(1));
        Assertions.assertEquals(IRValConstArray.ValType.ZERO,array.getValTypes().get(2));
    }
}