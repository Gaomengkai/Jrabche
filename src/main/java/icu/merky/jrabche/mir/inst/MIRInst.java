/*
 * BSD 3-Clause License
 *
 * Copyright (c) 2023-2024, Gaomengkai
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

// This file is generated by jinja2, do not edit it directly.

package icu.merky.jrabche.mir.inst;


import icu.merky.jrabche.mir.MIRReg;
import icu.merky.jrabche.mir.MIRValue;

public class MIRInst {
    MIRInstEnum opCode;
    MIRValue[] operands;
    int opNum = -1;

    MIRInst(MIRInstEnum opCode, int opNum) {
        this.opCode = opCode;
        this.operands = new MIRValue[opNum];
        this.opNum = opNum;
    }


    /**
     * 慎用。
     */
    protected MIRInst() {

    }

    public void setOperand(int index, MIRValue value) {
        this.operands[index] = value;
    }

    public MIRValue getOperand(int index) {
        return this.operands[index];
    }

    public MIRInstEnum getOpCode() {
        return opCode;
    }

    public int getOpNum() {
        return opNum;
    }

    public void setOpNum(int opNum) {
        this.opNum = opNum;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.opCode.toString());
        sb.append(" ");
        for (int i = 0; i < this.opNum; i++) {
            sb.append(this.operands[i].toString());
            if (i != this.opNum - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public static MIRInst newMIRJ(String v0) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRJ, 1);

        // build args
        inst.setOperand(0, MIRValue.newLabel(v0));
        return inst;
    }

    public static MIRInst newMIRB(MIRReg v0, String v1, String v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRB, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newLabel(v1));
        inst.setOperand(2, MIRValue.newLabel(v2));
        return inst;
    }

    public static MIRInst newMIRLoad(MIRReg v0, MIRReg v1) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRLoad, 2);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        return inst;
    }

    public static MIRInst newMIRStore(MIRReg v0, MIRReg v1) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRStore, 2);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        return inst;
    }

    public static MIRInst newMIREq(MIRReg v0, MIRReg v1, MIRReg v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIREq, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newReg(v2));
        return inst;
    }

    public static MIRInst newMIRNe(MIRReg v0, MIRReg v1, MIRReg v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRNe, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newReg(v2));
        return inst;
    }

    public static MIRInst newMIRLt(MIRReg v0, MIRReg v1, MIRReg v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRLt, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newReg(v2));
        return inst;
    }

    public static MIRInst newMIRGt(MIRReg v0, MIRReg v1, MIRReg v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRGt, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newReg(v2));
        return inst;
    }

    public static MIRInst newMIRLe(MIRReg v0, MIRReg v1, MIRReg v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRLe, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newReg(v2));
        return inst;
    }

    public static MIRInst newMIRGe(MIRReg v0, MIRReg v1, MIRReg v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRGe, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newReg(v2));
        return inst;
    }

    public static MIRInst newMIRFEq(MIRReg v0, MIRReg v1, MIRReg v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRFEq, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newReg(v2));
        return inst;
    }

    public static MIRInst newMIRFNe(MIRReg v0, MIRReg v1, MIRReg v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRFNe, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newReg(v2));
        return inst;
    }

    public static MIRInst newMIRFLt(MIRReg v0, MIRReg v1, MIRReg v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRFLt, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newReg(v2));
        return inst;
    }

    public static MIRInst newMIRFGt(MIRReg v0, MIRReg v1, MIRReg v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRFGt, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newReg(v2));
        return inst;
    }

    public static MIRInst newMIRFLe(MIRReg v0, MIRReg v1, MIRReg v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRFLe, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newReg(v2));
        return inst;
    }

    public static MIRInst newMIRFGe(MIRReg v0, MIRReg v1, MIRReg v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRFGe, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newReg(v2));
        return inst;
    }

    public static MIRInst newMIRAdd(MIRReg v0, MIRReg v1, MIRReg v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRAdd, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newReg(v2));
        return inst;
    }

    public static MIRInst newMIRAdd(MIRReg v0, MIRReg v1, Integer v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRAdd, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newImm(v2));
        return inst;
    }

    public static MIRInst newMIRSub(MIRReg v0, MIRReg v1, MIRReg v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRSub, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newReg(v2));
        return inst;
    }

    public static MIRInst newMIRSub(MIRReg v0, MIRReg v1, Integer v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRSub, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newImm(v2));
        return inst;
    }

    public static MIRInst newMIRMul(MIRReg v0, MIRReg v1, MIRReg v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRMul, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newReg(v2));
        return inst;
    }

    public static MIRInst newMIRMul(MIRReg v0, MIRReg v1, Integer v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRMul, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newImm(v2));
        return inst;
    }

    public static MIRInst newMIRSDiv(MIRReg v0, MIRReg v1, MIRReg v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRSDiv, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newReg(v2));
        return inst;
    }

    public static MIRInst newMIRSDiv(MIRReg v0, MIRReg v1, Integer v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRSDiv, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newImm(v2));
        return inst;
    }

    public static MIRInst newMIRUDiv(MIRReg v0, MIRReg v1, MIRReg v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRUDiv, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newReg(v2));
        return inst;
    }

    public static MIRInst newMIRUDiv(MIRReg v0, MIRReg v1, Integer v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRUDiv, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newImm(v2));
        return inst;
    }

    public static MIRInst newMIRShl(MIRReg v0, MIRReg v1, MIRReg v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRShl, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newReg(v2));
        return inst;
    }

    public static MIRInst newMIRShl(MIRReg v0, MIRReg v1, Integer v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRShl, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newImm(v2));
        return inst;
    }

    public static MIRInst newMIRLShr(MIRReg v0, MIRReg v1, MIRReg v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRLShr, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newReg(v2));
        return inst;
    }

    public static MIRInst newMIRLShr(MIRReg v0, MIRReg v1, Integer v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRLShr, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newImm(v2));
        return inst;
    }

    public static MIRInst newMIRAShr(MIRReg v0, MIRReg v1, MIRReg v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRAShr, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newReg(v2));
        return inst;
    }

    public static MIRInst newMIRAShr(MIRReg v0, MIRReg v1, Integer v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRAShr, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newImm(v2));
        return inst;
    }

    public static MIRInst newMIRAnd(MIRReg v0, MIRReg v1, MIRReg v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRAnd, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newReg(v2));
        return inst;
    }

    public static MIRInst newMIRAnd(MIRReg v0, MIRReg v1, Integer v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRAnd, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newImm(v2));
        return inst;
    }

    public static MIRInst newMIRXor(MIRReg v0, MIRReg v1, MIRReg v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRXor, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newReg(v2));
        return inst;
    }

    public static MIRInst newMIRXor(MIRReg v0, MIRReg v1, Integer v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRXor, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newImm(v2));
        return inst;
    }

    public static MIRInst newMIROr(MIRReg v0, MIRReg v1, MIRReg v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIROr, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newReg(v2));
        return inst;
    }

    public static MIRInst newMIROr(MIRReg v0, MIRReg v1, Integer v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIROr, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newImm(v2));
        return inst;
    }

    public static MIRInst newMIRFAdd(MIRReg v0, MIRReg v1, MIRReg v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRFAdd, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newReg(v2));
        return inst;
    }

    public static MIRInst newMIRFAdd(MIRReg v0, MIRReg v1, Float v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRFAdd, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newFImm(v2));
        return inst;
    }

    public static MIRInst newMIRFSub(MIRReg v0, MIRReg v1, MIRReg v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRFSub, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newReg(v2));
        return inst;
    }

    public static MIRInst newMIRFSub(MIRReg v0, MIRReg v1, Float v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRFSub, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newFImm(v2));
        return inst;
    }

    public static MIRInst newMIRFMul(MIRReg v0, MIRReg v1, MIRReg v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRFMul, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newReg(v2));
        return inst;
    }

    public static MIRInst newMIRFMul(MIRReg v0, MIRReg v1, Float v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRFMul, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newFImm(v2));
        return inst;
    }

    public static MIRInst newMIRFDiv(MIRReg v0, MIRReg v1, MIRReg v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRFDiv, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newReg(v2));
        return inst;
    }

    public static MIRInst newMIRFDiv(MIRReg v0, MIRReg v1, Float v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRFDiv, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newFImm(v2));
        return inst;
    }

    public static MIRInst newMIRFRem(MIRReg v0, MIRReg v1, MIRReg v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRFRem, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newReg(v2));
        return inst;
    }

    public static MIRInst newMIRFRem(MIRReg v0, MIRReg v1, Float v2) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRFRem, 3);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        inst.setOperand(2, MIRValue.newFImm(v2));
        return inst;
    }

    public static MIRInst newMIRZExt(MIRReg v0, MIRReg v1) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRZExt, 2);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        return inst;
    }

    public static MIRInst newMIRSExt(MIRReg v0, MIRReg v1) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRSExt, 2);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        return inst;
    }

    public static MIRInst newMIRF2S(MIRReg v0, MIRReg v1) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRF2S, 2);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        return inst;
    }

    public static MIRInst newMIRS2F(MIRReg v0, MIRReg v1) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRS2F, 2);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        return inst;
    }

    public static MIRInst newMIRCopy(MIRReg v0, MIRReg v1) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRCopy, 2);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newReg(v1));
        return inst;
    }

    public static MIRInst newMIRLoadAdr(MIRReg v0, String v1) {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRLoadAdr, 2);

        // build args
        inst.setOperand(0, MIRValue.newReg(v0));
        inst.setOperand(1, MIRValue.newLabel(v1));
        return inst;
    }

    public static MIRInst newMIRReturn() {
        MIRInst inst = new MIRInst(MIRInstEnum.MIRReturn, 0);

        // build args
        return inst;
    }

}
