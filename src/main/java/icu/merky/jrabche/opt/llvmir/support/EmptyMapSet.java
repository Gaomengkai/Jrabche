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

package icu.merky.jrabche.opt.llvmir.support;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class EmptyMapSet<K, V> implements Map<K, Set<V>> {
    Map<K, Set<V>> originalMap;

    public EmptyMapSet(Map<K, Set<V>> originalMap) {
        this.originalMap = originalMap;
    }

    @Override
    public int size() {
        return originalMap.size();
    }

    @Override
    public boolean isEmpty() {
        return originalMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return originalMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return originalMap.containsValue(value);
    }

    @Override
    public Set<V> get(Object key) {
        if (originalMap.containsKey(key)) {
            return originalMap.get(key);
        } else {
            return Set.of();
        }
    }

    @Override
    public Set<V> put(K key, Set<V> value) {
        return this.originalMap.put(key, value);
    }

    @Override
    public Set<V> remove(Object key) {
        return this.originalMap.remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends Set<V>> m) {
        this.originalMap.putAll(m);
    }

    @Override
    public void clear() {
        this.originalMap.clear();
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        return this.originalMap.keySet();
    }

    @NotNull
    @Override
    public Collection<Set<V>> values() {
        return this.originalMap.values();
    }

    @NotNull
    @Override
    public Set<Entry<K, Set<V>>> entrySet() {
        return this.originalMap.entrySet();
    }
}
