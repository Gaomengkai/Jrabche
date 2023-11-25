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

import java.util.*;

/**
 * Aho, A. V., Lam, M. S., Sethi, R., & Ullman, J. D. (2014).
 * Compilers: Principles, techniques, and tools (Second edition, Pearson new international edition).
 * Pearson.
 * <br>
 * Algorithm 9 41: Depth rst spanning tree and depth rst ordering
 *
 * @param <T> type of the value stored in the node
 */
public class DeepFirstOrder<T, V extends GraphNode<T>> {
    Set<V> visited;
    Map<V, Set<V>> edges;
    Map<V, Integer> order = new HashMap<>();
    int c = 0;

    public DeepFirstOrder(V root, int size) {
        visited = new HashSet<>(size);
        c = size - 1;
        edges = new HashMap<>(size);
        search(root);
    }

    void search(V n) {
        visited.add(n);
        for (var s : n.getSuccessors()) {
            if (!visited.contains(s)) {
                // n->s is a tree edge
                edges.putIfAbsent(n, new HashSet<>());
                edges.get(n).add((V) s);
                search((V) s);
            }
        }
        order.put(n, c--);
    }

    public List<V> getOrder() {
        List<V> res = new ArrayList<>(order.size());
        for (int i = 0; i < order.size(); i++) {
            res.add(null);
        }
        for (var entry : order.entrySet()) {
            res.set(entry.getValue(), entry.getKey());
        }
        return res;
    }
}
