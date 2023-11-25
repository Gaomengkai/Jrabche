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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cooper, K. D., Harvey, T. J., & Kennedy, K. (2006). <i>A simple, fast dominance algorithm.</i>
 * <a href="https://hdl.handle.net/1911/96345">view the paper</a>
 *
 * @param <T> the type in the graph node.
 */
public class SFDA<T, V extends GraphNode<T>> {
    List<? extends V> nodes;
    Map<V, V> doms = new HashMap<>();
    V startNode;
    Map<V, Integer> order = new HashMap<>(); // small is more front

    public SFDA(List<? extends V> nodes) {
        this.nodes = nodes;
        for (int i = 0; i < nodes.size(); i++) {
            order.put(nodes.get(i), i);
        }
        startNode = nodes.get(0);
        // doms[start node] <- start node
        doms.put(nodes.get(0), nodes.get(0));
        go();
    }


    /**
     * @return the IDom map.
     */
    public Map<V, V> getIDom() {
        return doms;
    }

    void go() {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (var b : nodes) {
                // for all nodes, b, in reverse postorder (except start node)
                if (b == startNode) continue;
                V newIdom = null;
                // new idom first (processed) predecessor of b /* (pick one) */
                for (var p : b.getPredecessors()) {
                    if (doms.containsKey((V) p)) {
                        newIdom = (V) p;
                        break;
                    }
                }
                // for all other predecessors, p, of b
                for (var p : b.getPredecessors()) {
                    if (p == newIdom) continue;/* other */
                    // if doms[p] is defined /* i.e., if doms[p] already calculated */
                    if (doms.containsKey((V) p)) {
                        // newIdom <- intersect(newIdom, p)
                        newIdom = intersect(newIdom, (V) p);
                    }
                }
                // doms[b] <- newIdom
                if (doms.get(b) != newIdom) {
                    doms.put(b, newIdom);
                    changed = true;
                }
            }
        }
        doms.put(startNode, null); // a trick not according to the paper
    }

    V intersect(V b1, V b2) {
        V finger1 = b1;
        V finger2 = b2;
        while (finger1 != finger2) {
            while (order.get(finger1) > order.get(finger2)) {
                finger1 = doms.get(finger1);
            }
            while (order.get(finger2) > order.get(finger1)) {
                finger2 = doms.get(finger2);
            }
        }
        return finger1;
    }
}