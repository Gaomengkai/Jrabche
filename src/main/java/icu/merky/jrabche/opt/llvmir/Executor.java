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

package icu.merky.jrabche.opt.llvmir;

import icu.merky.jrabche.llvmir.structures.IRBasicBlock;
import icu.merky.jrabche.llvmir.structures.IRFunction;
import icu.merky.jrabche.llvmir.structures.IRModule;
import icu.merky.jrabche.opt.llvmir.annotations.DisabledOpt;
import icu.merky.jrabche.opt.llvmir.annotations.OptOn;
import icu.merky.jrabche.utils.ClassFinder;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static icu.merky.jrabche.logger.JrabcheLogger.L;

public class Executor implements Runnable {

    List<Class<?>> basicBlockOptClasses = new ArrayList<>();
    Map<Class<?>, OnWhich> optOnMap = new HashMap<>();
    IRModule module;

    public Executor(IRModule module) {
        this.module = module;
    }

    @Override
    public void run() {
        try {
            // do the optimization.
            Map<Class<?>, List<Class<?>>> topoMap = ClassFinder.getClassesInPackage("icu.merky.jrabche.opt.llvmir")
                    .stream()
                    .filter(clazz -> clazz.isAnnotationPresent(OptOn.class))
                    .filter(clazz -> !clazz.isAnnotationPresent(DisabledOpt.class))
                    .collect(HashMap::new, (m, clazz) -> {
                        OptOn optOn = clazz.getAnnotation(OptOn.class);
                        m.put(clazz, List.of(optOn.afterWhich()));
                        switch (optOn.value()) {
                            case Module -> addModuleOpt(clazz);
                            case Function -> addFunctionOpt(clazz);
                            case BasicBlock -> addBasicBlockOpt(clazz);
                            case Instruction -> addInstructionOpt(clazz);
                        }
                    }, Map::putAll);
            List<Class<?>> topologicalSorted;
            TopologicalSort<Class<?>> topologicalSort = new TopologicalSort<>();
            topologicalSorted = topologicalSort.sort(topoMap);
            // reverse
            Collections.reverse(topologicalSorted);
            // It's MyGO!!!!!

            runOpts(topologicalSorted);
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void runOpts(List<Class<?>> optimizers) {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Class<?> optClass : optimizers) {
                if (optClass.equals(Mem2Reg.class)) continue; // skip mem2reg
                if (!optOnMap.containsKey(optClass)) continue;
                OnWhich onWhich = optOnMap.get(optClass);
                L.InfoF("Optimizer %s Running.\n", optClass.getAnnotation(OptOn.class).name());
                changed |= switch (onWhich) {
                    case Module, Instruction -> false;
                    case Function -> runOnFunction(optClass);
                    case BasicBlock -> runOnBlock(optClass);
                };
            }
        }
        runOnFunction(Mem2Reg.class);
    }

    private boolean runOnBlock(Class<?> clazz) {
        boolean changed = false;
        for (var F : module.getFunctions().values()) {
            for (var B : F.getBlocks()) {
                try {
                    Constructor<?> c = clazz.getConstructor(IRBasicBlock.class);
                    var instance = c.newInstance(B);
                    changed |= (boolean) instance.getClass().getMethod("go").invoke(instance);
                } catch (InvocationTargetException e) {
                    e.getTargetException().printStackTrace();
                    throw new RuntimeException(e);
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return changed;
    }

    private boolean runOnFunction(Class<?> clazz) {
        boolean changed = false;
        for (var F : module.getFunctions().values()) {
            try {
                Constructor<?> constructor = clazz.getConstructor(IRFunction.class);
                var instance = constructor.newInstance(F);
                changed |= (boolean) instance.getClass().getMethod("go").invoke(instance);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(clazz.getName() + "Optimizer class must have a public constructor and the constructor must have a parameter of type IRFunction");
            } catch (InvocationTargetException e) {
                e.getTargetException().printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return changed;
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
        try {
            Constructor<?> constructor = clazz.getConstructor(IRModule.class);
            basicBlockOptClasses.add(clazz);
            optOnMap.put(clazz, OnWhich.Module);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Optimizer class must have a public constructor and the constructor must have a parameter of type IRModule");
        }
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
