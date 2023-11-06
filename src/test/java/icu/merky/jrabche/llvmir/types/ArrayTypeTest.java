package icu.merky.jrabche.llvmir.types;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArrayTypeTest {

    @Test
    void getAtomType() {
        var t = new ArrayType(50,new ArrayType(20,new IntType()));
        assertEquals(IRBasicType.INT,t.getAtomType());
        var t2 = new ArrayType(30,new FloatType());
        assertEquals(IRBasicType.FLOAT,t2.getAtomType());
        var t3=new ArrayType(30,new ArrayType(40,new ArrayType(50,new ArrayType(60,new IntType()))));
        assertEquals(IRBasicType.INT,t3.getAtomType());
    }

    @Test
    void getSizeBytes() {
        var t=new ArrayType(30,new ArrayType(20,new IntType()));
        assertEquals(30*20*4,t.getSizeBytes());
        var t2=new ArrayType(30,new FloatType());
        assertEquals(30*4,t2.getSizeBytes());
    }

    @Test
    void testEquals() {
        var t1 = new ArrayType(30,new ArrayType(20,new IntType()));
        var t2 = new ArrayType(30,new ArrayType(20,new IntType()));
        var t3 = new ArrayType(30,new ArrayType(20,new FloatType()));
        var t4 = new ArrayType(30,new ArrayType(10,new IntType()));
        assertEquals(t1, t2);
        assertNotEquals(t1, t3);
        assertNotEquals(t1, t4);
    }
}