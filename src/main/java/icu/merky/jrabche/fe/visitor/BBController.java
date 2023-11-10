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

import icu.merky.jrabche.llvmir.structures.IRBasicBlock;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

public class BBController {
    public static AtomicInteger ifCount = new AtomicInteger(0);
    public static AtomicInteger whileCount = new AtomicInteger(0);
    public static AtomicInteger orCount = new AtomicInteger(0);
    public static AtomicInteger andCount = new AtomicInteger(0);
    List<BBLayer> layers = new Vector<>();

    public void pushIf(IRBasicBlock trueBB, IRBasicBlock falseBB, IRBasicBlock afterBB, int curLayerNum) {
        layers.add(new BBLayer(null, trueBB, falseBB, afterBB, curLayerNum, BBLayer.BBLayerType.IF));
    }

    public void pushWhile(IRBasicBlock trueBB, IRBasicBlock condBB, IRBasicBlock afterBB, int curLayerNum) {
        layers.add(new BBLayer(condBB, trueBB, afterBB, afterBB, curLayerNum, BBLayer.BBLayerType.WHILE));
    }

    public void pushOr(IRBasicBlock falseBB) {
        /*
         * if(a || b) {
         * ...
         * }
         * -> FALSE_BB HERE
         */
        layers.add(new BBLayer(null, null, falseBB, null, -1, BBLayer.BBLayerType.OR));
    }

    public void pushAnd(IRBasicBlock trueBB) {
        /*
         * if(a && b) {
         *                |-> TRUE_BB HERE
         * ...
         * }
         *
         */
        layers.add(new BBLayer(null, trueBB, null, null, -1, BBLayer.BBLayerType.AND));
    }

    public void pop() {
        layers.remove(layers.size() - 1);
    }

    public IRBasicBlock queryWhileCondBB() {
        for (int i = layers.size() - 1; i >= 0; i--) {
            if (layers.get(i).type == BBLayer.BBLayerType.WHILE) {
                return layers.get(i).condBB;
            }
        }
        return null;
    }

    public IRBasicBlock queryWhileBreakBB() {
        for (int i = layers.size() - 1; i >= 0; i--) {
            if (layers.get(i).type == BBLayer.BBLayerType.WHILE) {
                return layers.get(i).afterBB;
            }
        }
        return null;
    }

    public IRBasicBlock queryTrueBB() {
        for (int i = layers.size() - 1; i >= 0; i--) {
            if (layers.get(i).trueBB != null) {
                return layers.get(i).trueBB;
            }
        }
        return null;
    }

    public IRBasicBlock queryFalseBB() {
        for (int i = layers.size() - 1; i >= 0; i--) {
            if (layers.get(i).falseBB != null) {
                return layers.get(i).falseBB;
            }
        }
        return null;
    }

    private record BBLayer(
            IRBasicBlock condBB,
            IRBasicBlock trueBB,
            IRBasicBlock falseBB,
            IRBasicBlock afterBB,
            int curLayerNum,
            BBLayerType type
    ) {
        public enum BBLayerType {
            IF,
            WHILE,
            OR, AND
        }
    }
}
