package com.example.metrics;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

// thread-safe singleton using Bill Pugh holder pattern
public class MetricsRegistry implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ConcurrentHashMap<String, AtomicLong> counters = new ConcurrentHashMap<>();

    // holder class - JVM guarantees thread-safe lazy init
    private static class SingletonHolder {
        private static final MetricsRegistry INSTANCE = new MetricsRegistry();
    }

    // private ctor + reflection guard (checks holder instead of boolean flag - can't be reset via reflection)
    private MetricsRegistry() {
        if (SingletonHolder.INSTANCE != null) {
            throw new IllegalStateException("Singleton already instantiated - reflection attack blocked");
        }
    }

    public static MetricsRegistry getInstance() {
        return SingletonHolder.INSTANCE;
    }

    // serialization guards
    @Serial
    private Object readResolve() {
        return SingletonHolder.INSTANCE;
    }

    @Serial
    private Object writeReplace() {
        return SingletonHolder.INSTANCE;
    }

    // counter ops - lock-free via AtomicLong
    public void setCount(String key, long value) {
        counters.computeIfAbsent(key, k -> new AtomicLong()).set(value);
    }

    public void increment(String key) {
        counters.computeIfAbsent(key, k -> new AtomicLong()).incrementAndGet();
    }

    public long getCount(String key) {
        AtomicLong counter = counters.get(key);
        return counter != null ? counter.get() : 0L;
    }

    public Map<String, Long> getAll() {
        return Collections.unmodifiableMap(
                counters.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()))
        );
    }
}
