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

import icu.merky.jrabche.llvmir.IRBuilderImpl;
import icu.merky.jrabche.opt.llvmir.IROptBlockRearrange;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static icu.merky.jrabche.fe.visitor.SylangVisitorImplTest.getVisitorContext;

public class LiveInOutTest {
    @Test
    void case1() throws NoSuchFieldException, IllegalAccessException {
        var program = """
                int main() {
                int a=getint();
                int b=getint();
                int c=0;
                while(a<b){
                if(b>0){
                a=a+1;
                c=c+1;
                }
                }
                putint(a);
                putint(b);
                return 0;
                }
                               
                """;
        var builder = new IRBuilderImpl();
        getVisitorContext(program, builder);
        var module = builder.getModule();
        var F = module.getFunctions().get("main");
        new IROptBlockRearrange(F).optimize();

        BlockNodeBuilder bnb = new BlockNodeBuilder(F);
        bnb.build();
        bnb.buildLiveInOut();
        BlockNode root = new BlockNode(F.entryBB());
        for (Iterator<BlockNode> it = root.DFOIter(); it.hasNext(); ) {
            var x = it.next();
            System.out.println(x.val.getName());
            System.out.println("def,use:" + x.def + "," + x.use);
            System.out.println("in,out:" + x.liveIn + "," + x.liveOut);
        }
    }
}
