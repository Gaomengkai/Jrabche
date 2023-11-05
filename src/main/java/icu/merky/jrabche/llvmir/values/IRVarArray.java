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

package icu.merky.jrabche.llvmir.values;

import icu.merky.jrabche.exceptions.NotImplementedException;
import icu.merky.jrabche.llvmir.types.ArrayType;
import icu.merky.jrabche.llvmir.types.IRType;
import icu.merky.jrabche.llvmir.values.IRValConstArray.ValType;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class IRVarArray extends IRVal {
    private final List<ValType> valTypes; // witch
    private final List<IRValConst> childVals;
    private final List<IRVarArray> childArrays;
    private final List<Integer> shapes;

    public IRVarArray(ArrayType ty) {
        super(ty);
        this.shapes = new Vector<>();
        // from "ty" initialize "shapes"
        IRType cur = ty;
        while (cur instanceof ArrayType a) {
            this.shapes.add(a.getSize());
            cur = a.getElementType();
        }
        valTypes = new ArrayList<>(this.shapes.get(0));
        childVals = new ArrayList<>(this.shapes.get(0));
        childArrays = new ArrayList<>(this.shapes.get(0));
        for (int i = 0; i < this.shapes.get(0); i++) {
            valTypes.add(ValType.ZERO);
            childArrays.add(null);
            childVals.add(null);
        }
    }

    public IRVarArray(IRValConstArray constArray) {
        super(constArray.type);
        this.shapes = constArray.getShapes();
        this.valTypes = constArray.getValTypes();
        this.childVals = constArray.getChildVals();
        if (constArray.getChildArrays() != null) {
            this.childArrays = new ArrayList<>();
            for (var childArray : constArray.getChildArrays()) {
                if(childArray!=null) this.childArrays.add(new IRVarArray(childArray));
            }
        } else {
            this.childArrays = null;
        }
    }

    @Override
    public String asValue() {
        throw new NotImplementedException();
    }

    public List<ValType> getValTypes() {
        return valTypes;
    }

    public List<IRValConst> getChildVals() {
        return childVals;
    }

    public List<IRVarArray> getChildArrays() {
        return childArrays;
    }

    public List<Integer> getShapes() {
        return shapes;
    }
}
