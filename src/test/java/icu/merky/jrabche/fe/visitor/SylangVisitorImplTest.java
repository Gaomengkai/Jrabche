/*
 * BSD 3-Clause License
 *
 * Copyright (c) 2023, Gaomengkai
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package icu.merky.jrabche.fe.visitor;

import icu.merky.jrabche.fe.helper.ConstInitList;
import icu.merky.jrabche.fe.parser.SylangLexer;
import icu.merky.jrabche.fe.parser.SylangParser;
import icu.merky.jrabche.llvmir.IRBuilder;
import icu.merky.jrabche.llvmir.IRBuilderImpl;
import icu.merky.jrabche.llvmir.TestBuilder;
import icu.merky.jrabche.llvmir.types.ArrayType;
import icu.merky.jrabche.llvmir.types.IRBasicType;
import icu.merky.jrabche.llvmir.values.*;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SylangVisitorImplTest {

    public static VisitorContext getVisitorContext(String program) throws NoSuchFieldException, IllegalAccessException {
        return getVisitorContext(program, new IRBuilderImpl());
    }

    public static VisitorContext getVisitorContext(String program, IRBuilder builder) throws NoSuchFieldException, IllegalAccessException {
        SylangVisitorImpl visitor = new SylangVisitorImpl(builder);
        Field c = SylangVisitorImpl.class.getDeclaredField("C");
        c.setAccessible(true);
        VisitorContext C = (VisitorContext) c.get(visitor);
        var parser = new SylangParser(new BufferedTokenStream(new SylangLexer(CharStreams.fromString(program))));
        var tree = parser.compUnit();
        tree.accept(visitor);
        return C;
    }

    @Test
    void visitInitList1() {
        String program = "{1,2,{3,4,0xff},{},{6,0667}}";
        SylangVisitorImpl visitor = new SylangVisitorImpl(new TestBuilder());
        visitor.C.isConst.dive(true);
        var parser = new SylangParser(new BufferedTokenStream(new SylangLexer(CharStreams.fromString(program))));
        var tree = parser.initVal();
        tree.accept(visitor);
        ConstInitList lastList = (ConstInitList) visitor.C.lastVal;
        assertNotNull(lastList);
        assertEquals(3, lastList.initLists.size());
        assertEquals(2, lastList.constVals.size());
        assertEquals(3, lastList.initLists.get(0).constVals.size());
        assertEquals(0, lastList.initLists.get(1).size());
        assertEquals(2, lastList.initLists.get(2).constVals.size());
    }

    @Test
    void visitInitList2() {
        String program = "{1.1,2.2,{3.3,4.4,0x40000000},{},{6.6,7.7}}";
        SylangVisitorImpl visitor = new SylangVisitorImpl(new TestBuilder());
        visitor.C.isConst.dive(true);
        var parser = new SylangParser(new BufferedTokenStream(new SylangLexer(CharStreams.fromString(program))));
        var tree = parser.initVal();
        tree.accept(visitor);
        ConstInitList lastList = (ConstInitList) visitor.C.lastVal;
        assertNotNull(lastList);
        assertEquals(3, lastList.initLists.size());
        assertEquals(2, lastList.constVals.size());
        assertEquals(3, lastList.initLists.get(0).constVals.size());
        assertEquals(0, lastList.initLists.get(1).size());
        assertEquals(2, lastList.initLists.get(2).constVals.size());
    }

    @Test
    void visitHexFloatConst1() {
        String program = "0xb.1ep5";
        SylangVisitorImpl visitor = new SylangVisitorImpl(new TestBuilder());
        visitor.C.isConst.dive(true);
        var parser = new SylangParser(new BufferedTokenStream(new SylangLexer(CharStreams.fromString(program))));
        var tree = parser.number();
        tree.accept(visitor);
        var n = (IRValConstFloat) visitor.C.lastVal;
        assertEquals(355.75, n.getValue());
    }

    @Test
    void visitHexFloatConst2() throws NoSuchFieldException, IllegalAccessException {
        String program = "0x1.1p1";
        SylangVisitorImpl visitor = new SylangVisitorImpl(new TestBuilder());
        Field c = SylangVisitorImpl.class.getDeclaredField("C");
        c.setAccessible(true);
        VisitorContext C = (VisitorContext) c.get(visitor);
        C.isConst.dive(true);
        var parser = new SylangParser(new BufferedTokenStream(new SylangLexer(CharStreams.fromString(program))));
        var tree = parser.number();
        tree.accept(visitor);
        var n = (IRValConstFloat) C.lastVal;
        assertEquals(2.125, n.getValue());
    }

    @Test
    void visitConstDef1() throws NoSuchFieldException, IllegalAccessException {
        String program = "const int a=14;";
        SylangVisitorImpl visitor = new SylangVisitorImpl(new TestBuilder());
        Field c = SylangVisitorImpl.class.getDeclaredField("C");
        c.setAccessible(true);
        VisitorContext C = (VisitorContext) c.get(visitor);

        var parser = new SylangParser(new BufferedTokenStream(new SylangLexer(CharStreams.fromString(program))));
        var tree = parser.compUnit();
        tree.accept(visitor);
        assertNotNull(C.query("a"));
        var val = C.query("a");
        assertNull(C.lc.queryLocal("a"));
        assert val instanceof IRValGlobal;
        val = ((IRValGlobal) val).getValue();
        assertInstanceOf(IRValConstInt.class, val);
        assertEquals(14, ((IRValConstInt) val).getValue());
    }

    @Test
    void visitConstDef2() throws NoSuchFieldException, IllegalAccessException {
        String program = "const int apple[2][3]={1,2,3,4,5,6};";
        SylangVisitorImpl visitor = new SylangVisitorImpl(new TestBuilder());
        Field c = SylangVisitorImpl.class.getDeclaredField("C");
        c.setAccessible(true);
        VisitorContext C = (VisitorContext) c.get(visitor);

        var parser = new SylangParser(new BufferedTokenStream(new SylangLexer(CharStreams.fromString(program))));
        var tree = parser.compUnit();
        tree.accept(visitor);
        assertNotNull(C.query("apple"));
        var val = C.query("apple");
        val = ((IRValGlobal) val).getValue();
        assertInstanceOf(IRValConstArray.class, val);
        var aVal = (IRValConstArray) val;
        assertEquals(2, aVal.getShapes().get(0));
        assertEquals(3, aVal.getShapes().get(1));
        assertEquals(2, aVal.getValTypes().size());
        assertNull(aVal.getChildVals().get(0));
        assertNull(aVal.getChildVals().get(1));
        var elem = aVal.get(List.of(0, 0));
        assertInstanceOf(IRValConstInt.class, elem);
        assertEquals(1, ((IRValConstInt) elem).getValue());
        elem = aVal.get(List.of(0, 1));
        assertInstanceOf(IRValConstInt.class, elem);
        assertEquals(2, ((IRValConstInt) elem).getValue());
        elem = aVal.get(List.of(1, 2));
        assertInstanceOf(IRValConstInt.class, elem);
        assertEquals(6, ((IRValConstInt) elem).getValue());
    }

    @Test
    void visitVarDef1() throws NoSuchFieldException, IllegalAccessException {
        String program = "int apple=3;";
        SylangVisitorImpl visitor = new SylangVisitorImpl(new TestBuilder());
        Field c = SylangVisitorImpl.class.getDeclaredField("C");
        c.setAccessible(true);
        VisitorContext C = (VisitorContext) c.get(visitor);
        var parser = new SylangParser(new BufferedTokenStream(new SylangLexer(CharStreams.fromString(program))));
        var tree = parser.compUnit();
        tree.accept(visitor);

        assertNotNull(C.query("apple"));
        var val = C.query("apple");
        val = ((IRValGlobal) val).getValue();
        assertInstanceOf(IRValConstInt.class, val);
        assertEquals(3, ((IRValConstInt) val).getValue());
    }

    @Test
    void visitVarDef2() throws NoSuchFieldException, IllegalAccessException {
        String program = "int apple[3]={1,2};";
        SylangVisitorImpl visitor = new SylangVisitorImpl(new TestBuilder());
        Field c = SylangVisitorImpl.class.getDeclaredField("C");
        c.setAccessible(true);
        VisitorContext C = (VisitorContext) c.get(visitor);
        var parser = new SylangParser(new BufferedTokenStream(new SylangLexer(CharStreams.fromString(program))));
        var tree = parser.compUnit();
        tree.accept(visitor);

        assertNotNull(C.query("apple"));
        var val = C.query("apple");
        val = ((IRValGlobal) val).getValue();
        assertInstanceOf(IRValArray.class, val);
        var aVal = (IRValArray) val;
        assertEquals(IRBasicType.INT, ((ArrayType) aVal.getType()).getAtomType());
        assertEquals(3, aVal.getShapes().get(0));
        assertEquals(1, aVal.getShapes().size());
        assertEquals(3, aVal.getValTypes().size());
    }

    @Test
    void testFuncDef() throws NoSuchFieldException, IllegalAccessException {
        String program = "void abc(int d,int f[]){int e=d+1; int g=2;}";
        VisitorContext C = getVisitorContext(program);
        String funcString = C.builder.curFunc().toString();
        System.out.println(funcString);

        assertNotNull(C.queryFunctionType("abc"));
    }

    @Test
    void testIf() throws NoSuchFieldException, IllegalAccessException {
        String program = "void abc(int d){int e=d+1; if(e==1){e=e+1;}}";
        VisitorContext C = getVisitorContext(program);
        String funcString = C.builder.curFunc().toString();
        System.out.println(funcString);

        assertNotNull(C.queryFunctionType("abc"));
    }

    @Test
    void testIf2() throws NoSuchFieldException, IllegalAccessException {
        String program = """
                int main() {
                    int a=3;
                    int b=4;
                    if(a==b) {
                        a=5;
                    } else {
                        b=6;
                    }
                    return 7;
                    }
                """;
        VisitorContext C = getVisitorContext(program);
        String funcString = C.builder.curFunc().toString();
        System.out.println(funcString);

        assertNotNull(C.queryFunctionType("main"));
    }
}