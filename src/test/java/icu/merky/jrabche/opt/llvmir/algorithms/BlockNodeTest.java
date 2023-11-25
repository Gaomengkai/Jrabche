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

package icu.merky.jrabche.opt.llvmir.algorithms;

import icu.merky.jrabche.llvmir.structures.IRBasicBlock;
import icu.merky.jrabche.llvmir.structures.impl.IRBasicBlockImpl;
import org.junit.jupiter.api.Test;

class BlockNodeTest {

    @Test
    void buildBlockNodes() {
        // 0: 1  ;1: 2,5  ;2: 3  ;3: 1,4  ;4:  ;5: 6,8  ;6: 7  ;7: 3  ;8: 7
        IRBasicBlock b0 = new IRBasicBlockImpl();
        b0.setName("0");
        IRBasicBlock b1 = new IRBasicBlockImpl();
        b1.setName("1");
        IRBasicBlock b2 = new IRBasicBlockImpl();
        b2.setName("2");
        IRBasicBlock b3 = new IRBasicBlockImpl();
        b3.setName("3");
        IRBasicBlock b4 = new IRBasicBlockImpl();
        b4.setName("4");
        IRBasicBlock b5 = new IRBasicBlockImpl();
        b5.setName("5");
        IRBasicBlock b6 = new IRBasicBlockImpl();
        b6.setName("6");
        IRBasicBlock b7 = new IRBasicBlockImpl();
        b7.setName("7");
        IRBasicBlock b8 = new IRBasicBlockImpl();
        b8.setName("8");
        b0.addNext(b1);
        b1.addNext(b5);
        b1.addNext(b2);
        b2.addNext(b3);
        b3.addNext(b1);
        b3.addNext(b4);
        b5.addNext(b6);
        b5.addNext(b8);
        b6.addNext(b7);
        b7.addNext(b3);
        b8.addNext(b7);

        var root = new BlockNode(b0);
        BlockNodeBuilder builder = new BlockNodeBuilder(root, 9);
        builder.build();
    }
}