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

import icu.merky.jrabche.llvmir.StaticVariableCounter;
import icu.merky.jrabche.llvmir.structures.IRBasicBlock;
import icu.merky.jrabche.llvmir.structures.IRFunction;
import icu.merky.jrabche.llvmir.structures.IRModule;
import icu.merky.jrabche.llvmir.structures.impl.IRFunctionImpl;
import icu.merky.jrabche.opt.llvmir.annotations.DisabledOpt;
import icu.merky.jrabche.opt.llvmir.annotations.OptOn;
import icu.merky.jrabche.utils.ClassFinder;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static icu.merky.jrabche.RuntimeConfig.CFG_ENABLE_OUTPUT_RENAME;
import static icu.merky.jrabche.logger.JrabcheLogger.JL;

public class Executor implements Runnable {

    List<Class<?>> optClassesNoSSA = new ArrayList<>();
    List<Class<?>> optClassesSSA = new ArrayList<>();
    Map<Class<?>, OnWhich> optOnMap = new HashMap<>();
    IRModule M;

    public Executor(IRModule module) {
        this.M = module;
    }

    @Override
    public void run() {
        try {
            Map<Class<?>, List<Class<?>>> topoMapNoSSA = new HashMap<>();
            Map<Class<?>, List<Class<?>>> topoMapSSA = new HashMap<>();
            // do the optimization.
            ClassFinder.getClassesInPackage("icu.merky.jrabche.opt.llvmir").stream().filter(clazz -> clazz.isAnnotationPresent(OptOn.class)).filter(clazz -> !clazz.isAnnotationPresent(DisabledOpt.class)).forEach(clazz -> {
                OptOn optOn = clazz.getAnnotation(OptOn.class);
                boolean ssa = optOn.ssa();
                if (ssa) topoMapSSA.put(clazz, List.of(optOn.afterWhich()));
                else topoMapNoSSA.put(clazz, List.of(optOn.afterWhich()));
                switch (optOn.value()) {
                    case Module -> checkAndAddModuleOpt(clazz, ssa);
                    case Function -> checkAndAddFunctionOpt(clazz, ssa);
                    case BasicBlock -> checkAndAddBasicBlockOpt(clazz, ssa);
                }
            });
            List<Class<?>> noSSASorted;
            List<Class<?>> ssaSorted;
            noSSASorted = TopologicalSort.sort(topoMapNoSSA);
            ssaSorted = TopologicalSort.sort(topoMapSSA);
            // reverse
            Collections.reverse(noSSASorted);
            Collections.reverse(ssaSorted);
            // It's MyGO!!!!!

            runOpts(noSSASorted, ssaSorted);
        } catch (ClassNotFoundException | IOException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Run the optimizations.
     *
     * @param optNoSSA the list of optimizations that do not require SSA form.
     * @param optSSA   the list of optimizations that require SSA form.
     * @throws NoSuchMethodException if the optimizer class does not have a public constructor with a parameter of type IRModule.
     */
    private void runOpts(List<Class<?>> optNoSSA, List<Class<?>> optSSA) throws NoSuchMethodException {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Class<?> optClass : optNoSSA) {
                changed |= runOneOpt(optClass);

            }
        }

        // enter SSA form.
        runOnFunction(Mem2Reg.class);

        // Do SSA form optimizations
        changed = true;
        while (changed) {
            changed = false;
            for (Class<?> optClass : optSSA) {
                changed |= runOneOpt(optClass);
            }
        }
        if (CFG_ENABLE_OUTPUT_RENAME)
            postProcessRename();
    }

    private boolean runOneOpt(Class<?> optClass) throws NoSuchMethodException {
        if (optClass.equals(Mem2Reg.class)) return false;
        if (!optOnMap.containsKey(optClass)) return false;
        OnWhich onWhich = optOnMap.get(optClass);
        JL.DebugF("Optimizer %s Running.\n", optClass.getAnnotation(OptOn.class).name());
        return switch (onWhich) {
            case Module -> runOnModule(optClass);
            case Function -> runOnFunction(optClass);
            case BasicBlock -> runOnBlock(optClass);
        };
    }

    private boolean runOnModule(Class<?> optClazz) throws NoSuchMethodException {
        boolean changed = false;
        Constructor<?> c = optClazz.getConstructor(IRModule.class);
        try {
            var instance = c.newInstance(M);
            changed = (boolean) instance.getClass().getMethod("go").invoke(instance);
        } catch (IllegalAccessException | NoSuchMethodException | InstantiationException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            e.getTargetException().printStackTrace();
            throw new RuntimeException(e);
        }
        return changed;
    }

    private boolean runOnBlock(Class<?> clazz) throws NoSuchMethodException {
        boolean changed = false;
        Constructor<?> c = clazz.getConstructor(IRBasicBlock.class);
        for (var F : M.getFunctions().values()) {
            for (var B : F.getBlocks()) {
                try {
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

    private boolean runOnFunction(Class<?> clazz) throws NoSuchMethodException {
        boolean changed = false;
        Constructor<?> constructor = clazz.getConstructor(IRFunction.class);
        for (var F : M.getFunctions().values()) {
            try {
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

    private void checkAndAddBasicBlockOpt(Class<?> clazz, boolean ssa) {
        try {
            Constructor<?> constructor = clazz.getConstructor(IRBasicBlock.class);
            addOpt(clazz, ssa);
            optOnMap.put(clazz, OnWhich.BasicBlock);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Optimizer class must have a public constructor and the constructor must have a parameter of type IRBasicBlock");
        }
    }

    private void checkAndAddFunctionOpt(Class<?> clazz, boolean ssa) {
        try {
            Constructor<?> constructor = clazz.getConstructor(IRFunction.class);
            addOpt(clazz, ssa);
            optOnMap.put(clazz, OnWhich.Function);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Optimizer class must have a public constructor and the constructor must have a parameter of type IRFunction");
        }
    }

    private void checkAndAddModuleOpt(Class<?> clazz, boolean ssa) {
        try {
            Constructor<?> constructor = clazz.getConstructor(IRModule.class);
            addOpt(clazz, ssa);
            optOnMap.put(clazz, OnWhich.Module);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Optimizer class must have a public constructor and the constructor must have a parameter of type IRModule");
        }
    }

    private void addOpt(Class<?> clazz, boolean ssa) {
        if (ssa) optClassesSSA.add(clazz);
        else optClassesNoSSA.add(clazz);
    }

    private void postProcessRename() {
        for (var func : M.getFunctions().values()) {
            StaticVariableCounter.set(0);
            for (var fp : ((IRFunctionImpl) func).getFp().values()) {
                fp.setName(String.valueOf(StaticVariableCounter.getAndIncrement()));
            }
            for (var bb : func.getBlocks()) {
                bb.setName(String.valueOf(StaticVariableCounter.getAndIncrement()));
                for (var inst : bb.getInsts()) {
                    if (!inst.needName()) continue;
                    inst.setName(String.valueOf(StaticVariableCounter.getAndIncrement()));
                }
            }
        }
    }

    enum OnWhich {Module, Function, BasicBlock}

    static class TopologicalSort {
        public static <T> List<T> sort(Map<T, List<T>> graph) {
            List<T> sorted = new ArrayList<>();
            Map<T, Integer> inDegree = new HashMap<>();
            for (T node : graph.keySet()) {
                inDegree.put(node, 0);
            }
            for (T node : graph.keySet()) {
                for (T neighbor : graph.getOrDefault(node, List.of())) {
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
                for (T neighbor : graph.getOrDefault(cur, List.of())) {
                    inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                }
                inDegree.remove(cur);
            }
            // if there ARE cycles, then we have not removed all edges from the graph
            if (!inDegree.isEmpty()) {
                throw new RuntimeException("Cycle in graph, topological sort not possible");
            }
            return sorted;
        }
    }
}
