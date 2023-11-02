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

package icu.merky.jrabche.opt;

import icu.merky.jrabche.llvmir.structures.IRBasicBlock;
import icu.merky.jrabche.llvmir.structures.IRFunction;
import icu.merky.jrabche.utils.ClassFinder;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class VIROptExecutor implements Runnable {

    List<Class<?>> basicBlockOptClasses = new ArrayList<>();
    Map<Class<?>, OnWhich> optOnMap = new HashMap<>();

    @Override
    public void run() {
        try {
            // do the optimization.
            Map<Class<?>, List<Class<?>>> topoMap = ClassFinder.getClassesInPackage("icu.merky.jrabche.opt")
                    .stream()
                    .filter(clazz -> clazz.isAnnotationPresent(OptOn.class))
                    .collect(HashMap::new, (m, clazz) -> {
                        OptOn optOn = clazz.getAnnotation(OptOn.class);
                        m.put(clazz, List.of(optOn.afterWhich()));
                        switch (optOn.value()) {
                            case Module:
                                addModuleOpt(clazz);
                            case Function:
                                addFunctionOpt(clazz);
                            case BasicBlock:
                                addBasicBlockOpt(clazz);
                            case Instruction:
                                addInstructionOpt(clazz);
                        }
                    }, Map::putAll);
            List<Class<?>> topologicalSorted;
            TopologicalSort<Class<?>> topologicalSort = new TopologicalSort<>();
            topologicalSorted = topologicalSort.sort(topoMap);
            // reverse
            Collections.reverse(topologicalSorted);
            // It's MyGO!!!!!

            for (Class<?> optClass : topologicalSorted) {
                if (!optOnMap.containsKey(optClass)) continue;
                OnWhich onWhich = optOnMap.get(optClass);
                Class<?> targetClass = switch (onWhich) {
                    case Function -> IRFunction.class;
                    case BasicBlock -> IRBasicBlock.class;
                    default ->
                            throw new RuntimeException("Optimizer class must be a subclass of the corresponding optimizer interface");
                };
                // TODO.
                optClass.getConstructor(targetClass).newInstance((Object) null);
            }
        } catch (ClassNotFoundException | IOException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void addInstructionOpt(Class<?> clazz) {
    }

    private void addBasicBlockOpt(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getConstructor(IRBasicBlock.class);
            basicBlockOptClasses.add(clazz);
            optOnMap.put(clazz, OnWhich.BasicBlock);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Optimizer class must have a public constructor and the constructor must have a parameter of type IRBasicBlock");
        }
    }

    private void addFunctionOpt(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getConstructor(IRFunction.class);
            basicBlockOptClasses.add(clazz);
            optOnMap.put(clazz, OnWhich.Function);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Optimizer class must have a public constructor and the constructor must have a parameter of type IRFunction");
        }
    }

    private void addModuleOpt(Class<?> clazz) {
    }

    enum OnWhich {Module, Function, BasicBlock, Instruction}

    static class TopologicalSort<T> {
        public List<T> sort(Map<T, List<T>> graph) {
            List<T> sorted = new ArrayList<>();
            Map<T, Integer> inDegree = new HashMap<>();
            for (T node : graph.keySet()) {
                inDegree.put(node, 0);
            }
            for (T node : graph.keySet()) {
                for (T neighbor : graph.get(node)) {
                    inDegree.put(neighbor, inDegree.get(neighbor) + 1);
                }
            }
            while (true) {
                T cur = null;
                for (T node : graph.keySet()) {
                    if (!inDegree.containsKey(node)) continue;
                    if (inDegree.get(node) == 0) {
                        cur = node;
                        break;
                    }
                }
                if (cur == null) {
                    break;
                }
                sorted.add(cur);
                for (T neighbor : graph.get(cur)) {
                    inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                }
                inDegree.remove(cur);
            }
            // if there ARE cycles, then we have not removed all edges from the graph
            if (inDegree.size() > 0) {
                throw new RuntimeException("Cycle in graph, topological sort not possible");
            }
            return sorted;
        }
    }
}
