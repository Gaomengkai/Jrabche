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

import java.util.Objects;

public class MIRReg {

    int id;
    MIRBasicType type;
    MIRRegTag tag;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MIRReg mirReg = (MIRReg) o;
        return id == mirReg.id && type == mirReg.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type);
    }

    private MIRReg() {
    }

    public static MIRReg NewBool(int id) {
        MIRReg reg = new MIRReg();
        reg.id = id;
        reg.type = MIRBasicType.Bool;
        return reg;
    }

    public static MIRReg NewInt64(int id) {
        MIRReg reg = new MIRReg();
        reg.id = id;
        reg.type = MIRBasicType.Int64;
        return reg;
    }

    public static MIRReg NewFloat(int id) {
        MIRReg reg = new MIRReg();
        reg.id = id;
        reg.type = MIRBasicType.Float;
        return reg;
    }

    public static MIRReg NewInt32(int id) {
        MIRReg reg = new MIRReg();
        reg.id = id;
        reg.type = MIRBasicType.Int32;
        return reg;
    }

    public static MIRReg NewPtr(int id) {
        MIRReg reg = new MIRReg();
        reg.id = id;
        reg.type = MIRBasicType.Ptr;
        return reg;
    }

    public int getId() {
        return id;
    }

    public boolean isBool() {
        return type == MIRBasicType.Bool;
    }

    public boolean isInt64() {
        return type == MIRBasicType.Int64;
    }

    public boolean isInt32() {
        return type == MIRBasicType.Int32;
    }

    public boolean isFloat() {
        return type == MIRBasicType.Float;
    }

    public boolean isPtr() {
        return type == MIRBasicType.Ptr;
    }

}
