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

package icu.merky.jrabche.mir.support;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static icu.merky.jrabche.mir.support.ArithmeticSupport.isPowerOf2;

class ArithmeticSupportTest {

    @Test
    void isPowerOf2Test() {
        int[] trues = {
                1, 2, 4, 8, 16, 32, 64, 128, 256, 512,
                0x40000000, 0x80000000,
        };
        int[] falses = {
                5, 6, 7, 9, 12, 18, 0x50000000
        };
        for (int i : trues) {
            Assertions.assertTrue(isPowerOf2(i));
        }
        for (int i : falses) {
            Assertions.assertFalse(isPowerOf2(i));
        }
    }

    @Test
    void log2iTest() {
        int[] trues = {
                1, 2, 4, 8, 16, 32, 64, 128, 256, 512,
                0x40000000,
        };
        for (int i : trues) {
            Assertions.assertEquals((int) (Math.log(i) / Math.log(2)), ArithmeticSupport.log2i(i), String.valueOf(i));
        }
    }
}