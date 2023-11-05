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

package icu.merky.jrabche.helper;

import icu.merky.jrabche.exceptions.NotImplementedException;
import icu.merky.jrabche.llvmir.types.IRAtomType;
import icu.merky.jrabche.llvmir.types.InvalidType;
import icu.merky.jrabche.llvmir.values.IRVal;
import icu.merky.jrabche.llvmir.values.IRValConst;

import java.util.ArrayList;
import java.util.List;

public class InitList extends IRVal {
    public IRAtomType containedType;
    public List<InitList> initLists;
    public List<IRValConst> constVals;
    public List<ILType> witch;
    public List<Integer> indices;
    public InitList(IRAtomType atomType) {
        super(new InvalidType());
        containedType = atomType;
        witch = new ArrayList<>();
        indices = new ArrayList<>();
    }

    @Override
    public String asValue() {
        throw new NotImplementedException();
    }

    public void addIL(InitList il) {
        if (initLists == null)
            initLists = new ArrayList<>();
        initLists.add(il);
        indices.add(initLists.size() - 1);
        witch.add(ILType.IL);
    }

    public void addCV(IRValConst cv) {
        if (constVals == null)
            constVals = new ArrayList<>();
        constVals.add(cv);
        indices.add(constVals.size() - 1);
        witch.add(ILType.CV);
    }

    public int size() {
        return indices.size();
    }

    public enum ILType {IL, CV}
}
