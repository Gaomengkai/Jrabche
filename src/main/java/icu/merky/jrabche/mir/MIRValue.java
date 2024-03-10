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

package icu.merky.jrabche.mir;


public class MIRValue {
    MIRReg reg;
    Integer imm;
    Float fImm;
    String label;

    enum _ValueTag {
        IMM, F_IMM, REG, LABEL, INVALID
    }

    _ValueTag vt = _ValueTag.INVALID;
    MIRBasicType bt = MIRBasicType.Invalid;

    private MIRValue() {
    }

    public static MIRValue newImm(Integer imm) {
        MIRValue value = new MIRValue();
        value.setImm(imm);
        return value;
    }

    public static MIRValue newFImm(Float fImm) {
        MIRValue value = new MIRValue();
        value.setFImm(fImm);
        return value;
    }

    public static MIRValue newReg(MIRReg reg) {
        MIRValue value = new MIRValue();
        value.setReg(reg);
        return value;
    }

    public static MIRValue newLabel(String label) {
        MIRValue value = new MIRValue();
        value.setLabel(label);
        return value;
    }

    public boolean isImm() {
        return imm != null;
    }

    public boolean isFImm() {
        return fImm != null;
    }

    public boolean isReg() {
        return reg != null;
    }

    public boolean isLabel() {
        return label != null;
    }

    public void setImm(Integer imm) {
        clear();
        this.imm = imm;
        this.vt = _ValueTag.IMM;
        this.bt = MIRBasicType.Int32;
    }

    public void setFImm(Float fImm) {
        clear();
        this.fImm = fImm;
        this.vt = _ValueTag.F_IMM;
        this.bt = MIRBasicType.Float;
    }

    public void setReg(MIRReg reg) {
        clear();
        this.reg = reg;
        this.vt = _ValueTag.REG;
        this.bt = reg.type;
    }

    public void setLabel(String label) {
        clear();
        this.label = label;
        this.vt = _ValueTag.LABEL;
        this.bt = MIRBasicType.Ptr;
    }

    private void clear() {
        switch (this.vt) {
            case IMM -> this.imm = null;
            case F_IMM -> this.fImm = null;
            case REG -> this.reg = null;
            case LABEL -> this.label = null;
            default -> {
            }
        }
        this.vt = _ValueTag.INVALID;
    }

    public MIRReg getReg() {
        return reg;
    }

    public Integer getImm() {
        return imm;
    }

    public Float getFImm() {
        return fImm;
    }

    public String getLabel() {
        return label;
    }

    public _ValueTag getVt() {
        return vt;
    }

    public MIRBasicType getBt() {
        return bt;
    }

    public boolean isI32() {
        return bt == MIRBasicType.Int32;
    }

    public boolean isF32() {
        return bt == MIRBasicType.Float;
    }

    public boolean isPtr() {
        return bt == MIRBasicType.Ptr;
    }


    @Override
    public String toString() {
        switch (this.vt) {
            case IMM -> {
                return String.format("imm %d", this.imm);
            }
            case F_IMM -> {
                return String.format("fimm %.2f", this.fImm);
            }
            case REG -> {
                return this.reg.toString();
            }
            case LABEL -> {
                return String.format("label %s", this.label);
            }
            default -> {
                return "invalid";
            }
        }
    }
}
