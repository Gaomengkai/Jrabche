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

package icu.merky.jrabche.llvmir.inst;

import icu.merky.jrabche.llvmir.types.IRBasicType;
import icu.merky.jrabche.llvmir.values.IRVal;

import java.util.HashMap;
import java.util.Map;

public class IRInstCmpFactory{
    static private final Map<IRInstIcmp.IcmpOp, IRInstFcmp.FcmpOp> opMap = new HashMap<>();
    static {
        opMap.put(IRInstIcmp.IcmpOp.EQ, IRInstFcmp.FcmpOp.UEQ);
        opMap.put(IRInstIcmp.IcmpOp.NE, IRInstFcmp.FcmpOp.UNE);
        opMap.put(IRInstIcmp.IcmpOp.SGT, IRInstFcmp.FcmpOp.UGT);
        opMap.put(IRInstIcmp.IcmpOp.SGE, IRInstFcmp.FcmpOp.UGE);
        opMap.put(IRInstIcmp.IcmpOp.SLT, IRInstFcmp.FcmpOp.ULT);
        opMap.put(IRInstIcmp.IcmpOp.SLE, IRInstFcmp.FcmpOp.ULE);
        opMap.put(IRInstIcmp.IcmpOp.UGT, null);
        opMap.put(IRInstIcmp.IcmpOp.UGE, null);
        opMap.put(IRInstIcmp.IcmpOp.ULT, null);
        opMap.put(IRInstIcmp.IcmpOp.ULE, null);
    }
    public static IRInst createCmpInst(IRInstIcmp.IcmpOp op, IRVal lhs, IRVal rhs, IRBasicType type){
        if(type== IRBasicType.INT){
            return new IRInstIcmp(op, lhs, rhs);
        } else if(type== IRBasicType.FLOAT){
            return new IRInstFcmp(opMap.get(op), lhs, rhs);
        } else {
            throw new RuntimeException("Unknown type");
        }
    }
}
