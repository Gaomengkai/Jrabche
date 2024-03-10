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

package icu.merky.jrabche.opt.llvmir.algorithms;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class GraphNode<T> {
    T val;
    private final Set<GraphNode<T>> successors;


    private final Set<GraphNode<T>> predecessor;

    public GraphNode(T val) {
        this.val = val;
        successors = new HashSet<>();
        predecessor = new HashSet<>();
    }

    public static <T> void BuildPredecessor(GraphNode<T> root) {
        Queue<GraphNode<T>> q = new LinkedList<>();
        Set<GraphNode<T>> visited = new HashSet<>();
        q.add(root);
        while (!q.isEmpty()) {
            GraphNode<T> cur = q.poll();
            if (visited.contains(cur)) continue;
            visited.add(cur);
            for (var s : cur.getSuccessors()) {
                s.predecessor.add(cur);
                q.add(s);
            }
        }
    }

    void addSuccessor(GraphNode<T> child) {
        successors.add(child);
    }

    void removeSuccessor(GraphNode<T> child) {
        successors.remove(child);
    }

    public Set<GraphNode<T>> getSuccessors() {
        return successors;
    }

    public Set<GraphNode<T>> getPredecessors() {
        return predecessor;
    }

    public T getVal() {
        return val;
    }

    @Override
    public String toString() {
        return val.toString();
    }
}