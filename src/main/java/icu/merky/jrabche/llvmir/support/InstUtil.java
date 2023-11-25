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

import icu.merky.jrabche.llvmir.inst.IRInst;
import icu.merky.jrabche.llvmir.structures.IRBasicBlock;
import icu.merky.jrabche.llvmir.values.IRVal;

import java.util.Iterator;

public class InstUtil {
    /**
     * Replace all uses of an instruction with a new value
     * <br>
     * e.g.: <code>%v1 = load i32, ptr %v2<br>%v3 = add i32 ...,<b>%v1</b></code>
     * <br>
     * <code>ReplaceAllUsesWith(%v1, %v2)</code>
     * <br>
     * will result in:
     * <br>
     * <code>%v3 = add i32 ...,<b>%v2</b></code>
     *
     * @param inst   the instruction to be replaced
     * @param newVal the new value to replace the instruction
     * @return whether the instruction is replaced
     */
    public static boolean ReplaceAllUsesWith(IRInst inst, IRVal newVal) {
        boolean changed = false;
        for (IRInst user : inst.getUsedBy()) {
            changed |= user.replace(inst, newVal);
        }
        return changed;
    }

    public static boolean RemoveDeletedInstructions(IRBasicBlock bb) {
        boolean changed = false;
        for (Iterator<IRInst> it = bb.getInsts().iterator(); it.hasNext(); ) {
            var inst = it.next();
            if (inst.isDeleted()) {
                it.remove();
                changed = true;
            }
        }
        return changed;
    }
}
