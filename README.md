# Singleton Metrics - MetricsRegistry Refactoring

## Implemented

Refactored `MetricsRegistry` into a proper thread-safe singleton using the **Bill Pugh Static Inner Holder** pattern.

- Made constructor private + added holder-based reflection guard (checks holder instance, not a boolean flag that could be reset via reflection)
- `SingletonHolder` inner class for lazy init - JVM class loading guarantees thread safety with zero sync overhead
- `readResolve()` and `writeReplace()` with `@Serial` to survive serialization round-trips
- Swapped `synchronized HashMap<String, Long>` for `ConcurrentHashMap<String, AtomicLong>` - lock-free increments via CAS
- Fixed `MetricsLoader` to use `getInstance()` instead of `new`

All 4 test harnesses pass: `App` (same identity hash), `ConcurrencyCheck` (1 instance across 80 threads), `ReflectionAttack` (throws exception), `SerializationCheck` (same object after round-trip).

---

## Original Assignment

Exercise A - Singleton Refactoring (Metrics Registry)

### Narrative
A CLI tool called **PulseMeter** collects runtime metrics (counters) and exposes them globally
so any part of the app can increment counters like `REQUESTS_TOTAL`, `DB_ERRORS`, etc.

The current implementation is **not a real singleton**, **not thread-safe**, and is vulnerable to
**reflection** and **serialization** breaking the singleton guarantee.

Your job is to refactor it into a **proper, thread-safe, lazy-initialized Singleton**.

### What you have (Starter)
- `MetricsRegistry` is *intended* to be global, but:
  - `getInstance()` can return different objects under concurrency.
  - The constructor is not private.
  - Reflection can create multiple instances.
  - Serialization/deserialization can produce a new instance.
- `MetricsLoader` incorrectly uses `new MetricsRegistry()`.

### Tasks
1) Make `MetricsRegistry` a proper, **thread-safe singleton**
   - **Lazy initialization**
   - **Private constructor**
   - Thread safety: pick one approach (recommended: static holder or double-checked locking)

2) Block reflection-based multiple construction
   - If the constructor is called when an instance already exists, throw an exception
   - (Hint: use a static flag/instance check inside the constructor)

3) Preserve singleton on serialization
   - Implement `readResolve()` so deserialization returns the same singleton instance

4) Update `MetricsLoader` to use the singleton
   - No `new MetricsRegistry()` anywhere in code

### Acceptance
- Single instance across threads within a JVM run.
- Reflection cannot construct a second instance.
- Deserialization returns the same instance.
- Loading metrics from `metrics.properties` works.
- Values are accessible via:
  - `increment(key)`
  - `getCount(key)`
  - `getAll()`

### Build/Run
```
cd singleton-metrics/src
javac com/example/metrics/*.java
java com.example.metrics.App
```

### Demo Commands
- Concurrency check: `java com.example.metrics.ConcurrencyCheck`
- Reflection attack check: `java com.example.metrics.ReflectionAttack`
- Serialization check: `java com.example.metrics.SerializationCheck`
