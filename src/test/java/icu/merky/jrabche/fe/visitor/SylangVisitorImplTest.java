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

import icu.merky.jrabche.fe.helper.InitList;
import icu.merky.jrabche.fe.parser.SylangLexer;
import icu.merky.jrabche.fe.parser.SylangParser;
import icu.merky.jrabche.llvmir.TestBuilder;
import icu.merky.jrabche.llvmir.values.IRValConstFloat;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class SylangVisitorImplTest {

    @Test
    void visitInitList1() {
        String program = "{1,2,{3,4,0xff},{},{6,0667}}";
        SylangVisitorImpl visitor = new SylangVisitorImpl(new TestBuilder());
        visitor.C.isConst.dive(true);
        var parser = new SylangParser(new BufferedTokenStream(new SylangLexer(CharStreams.fromString(program))));
        var tree = parser.initVal();
        tree.accept(visitor);
        InitList lastList = (InitList) visitor.C.lastVal;
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
        InitList lastList = (InitList) visitor.C.lastVal;
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
        var n=(IRValConstFloat) visitor.C.lastVal;
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
        var n=(IRValConstFloat) C.lastVal;
        assertEquals(2.125, n.getValue());
    }
}