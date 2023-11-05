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

package icu.merky.jrabche.llvmir.structures;

import icu.merky.jrabche.llvmir.inst.IRInst;

import java.util.List;

public interface IRBasicBlock {
    /**
     * @param inst the inst to be added
     * @return the position of the inst
     */
    int addInst(IRInst inst);

    /**
     * @param position the position of the inst to be deleted
     * @param inst     the inst to be deleted
     * @return the position of the inst
     */
    int addInst(int position, IRInst inst);

    void addInstFront(IRInst inst);

    /**
     * @return the name of the inst
     */
    String getName();

    /**
     * @param name the name of the inst to be deleted
     */
    void setName(String name);

    /**
     * @return the insts in the basic block
     */
    List<IRInst> getInst();

    /**
     * @return the terminator of the basic block
     */
    IRInst getTerminator();

    /**
     * @return whether the relations between basic blocks have been built
     */
    boolean relationsBuilt();

    void setRelationBuild(boolean val);

    /**
     * @return the previous  basic blocks
     */
    List<IRBasicBlock> getPrev();

    /**
     * @return the next basic blocks
     */
    List<IRBasicBlock> getNext();

    /**
     * @param prev the previous basic block to be added
     */
    void addPrev(IRBasicBlock prev);

    /**
     * @param next the next basic block to be added
     */
    void addNext(IRBasicBlock next);

    /**
     * @return whether the basic block is a terminator
     */
    boolean checkTerminator();
}
