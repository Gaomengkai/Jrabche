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

import icu.merky.jrabche.llvmir.types.IRType;
import icu.merky.jrabche.llvmir.values.IRVal;
import icu.merky.jrabche.llvmir.values.ValueRepresentable;

public abstract class IRInst extends IRVal implements Cloneable, Replaceable, ValueRepresentable {
    protected InstID instID;
    private boolean deleted = false;

    public IRInst(String name, InstID instID, IRType valType) {
        super(valType);
        this.name = name;
        this.type = valType;
        this.instID = instID;
    }
    public IRInst(InstID instID, IRType valType) {
        super(valType);
        this.type = valType;
        this.instID = instID;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public void setDeleted() {
        this.deleted = true;
    }

    @Override
    public IRInst clone() {
        var clone = (IRInst) super.clone();
        clone.deleted = false;
        clone.instID = instID;
        return clone;
    }

    @Override
    public abstract String toString();

    public boolean isAllocaInst() {
        return instID == InstID.AllocaInst;
    }

    public boolean isReturnInst() {
        return instID == InstID.ReturnInst;
    }

    public boolean isBrInst() {
        return instID == InstID.BrInst;
    }

    public boolean isTerminatorInst() {
        return isReturnInst() || isBrInst();
    }

    public boolean isMathInst() {
        return instID == InstID.MathInst;
    }

    public boolean isICmpInst() {
        return instID == InstID.ICmpInst;
    }

    public boolean isFCmpInst() {
        return instID == InstID.FCmpInst;
    }

    public boolean isLoadInst() {
        return instID == InstID.LoadInst;
    }

    public boolean isStoreInst() {
        return instID == InstID.StoreInst;
    }

    public boolean isBitCastInst() {
        return instID == InstID.BitCastInst;
    }

    public boolean isGetElementPtrInst() {
        return instID == InstID.GetElementPtrInst;
    }

    public boolean isConvertInst() {
        return instID == InstID.ConvertInst;
    }

    public boolean isCallInst() {
        return instID == InstID.CallInst;
    }

    public boolean isPhiInst() {
        return instID == InstID.PhiInst;
    }
    public boolean needName() {
        if( isTerminatorInst() ||isStoreInst())
            return false;
        if(isCallInst()) {
            if(this instanceof IRInstCall call)
                return call.getType().isVoid();
            throw new RuntimeException("CallInst need IRInstCall");
        }
        return true;
    }

    public InstID getInstID() {
        return instID;
    }

    public IRType getValType() {
        return type;
    }

    @Override
    public void setName(String name) {
        super.setName(name.startsWith("%") ? name : "%" + name);
    }

    protected enum InstID {
        Invalid,
        AllocaInst,
        ReturnInst,
        BrInst,
        MathInst,
        ICmpInst,
        FCmpInst,
        LoadInst,
        StoreInst,
        BitCastInst,
        GetElementPtrInst,
        ConvertInst,
        CallInst,
        PhiInst,
    }
}
