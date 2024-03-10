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

package icu.merky.jrabche.llvmir.support;


import icu.merky.jrabche.llvmir.inst.fake.IRInstCopy;
import icu.merky.jrabche.support.AutoNewCollectionHashMap;

import java.util.HashSet;

/**
 * 一个拷贝指令的引用数据。由于在LLVM IR中，一个变量只能被定义一次，这是构成SSA模式的基石。
 * 但是在Lowering过程中，需要消去Phi指令，这就需要拷贝变量。为了保证拷贝变量的正确性，需要
 * 一个拷贝指令的引用数据。
 * <br/>
 * 例子：
 * <code>
 * BB1:
 * %a = add i32 1, 2
 * %b = add i32 3, 4
 * %c = add i32 %a, %b
 * BB2:
 * %d = add i32 5, 6
 * BB3:
 * %e = PHI i32 [%c, %BB1], [%d, %BB2]
 * ... using %e ...
 * </code>
 * <br/>
 * 拷贝过后，变成：
 * <code>
 * BB1:
 * %a = add i32 1, 2
 * %b = add i32 3, 4
 * %c = add i32 %a, %b
 * %e = copy i32 %c
 * BB2:
 * %d = add i32 5, 6
 * %e = copy i32 %d
 * BB3:
 * ... using %e ...
 * </code>
 * 在这种情况下，%e有两个定义。需要一个IRCopy->[IRCopy...]的映射。
 * 其中key存储在IRCopyRefData中，Value直接就是混合在正常的IR中。
 */
public class IRCopyRefData {
    AutoNewCollectionHashMap<IRInstCopy, IRInstCopy> copyPool = new AutoNewCollectionHashMap<>(HashSet::new);

    public IRInstCopy get(IRInstCopy irCopy) {
        return copyPool.get(irCopy).iterator().next();
    }

    public void put(IRInstCopy irCopy, IRInstCopy irCopy1) {
        copyPool.insert(irCopy, irCopy1);
    }

}
