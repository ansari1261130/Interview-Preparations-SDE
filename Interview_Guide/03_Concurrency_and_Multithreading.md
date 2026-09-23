# 03 · Concurrency and Multithreading

A complete Java backend interview study guide for race conditions, executors, locks, the Java Memory Model, concurrent collections, Java 21 virtual threads, and distributed concurrency. Answers are tuned for SDE-1/SDE-2 backend interviews and production debugging.

🟢 Beginner · 🟡 Intermediate · 🔴 Advanced · ⚫ FAANG · 🔥 = very frequently asked

## Table of Contents
- [Real Interview Questions](#real-interview-questions)
- [Thread Basics and Executors](#thread-basics-and-executors)
- [Locks, Memory Model, and Atomicity](#locks-memory-model-and-atomicity)
- [Concurrent Collections and Coordination](#concurrent-collections-and-coordination)
- [Async, ForkJoin, and Virtual Threads](#async-forkjoin-and-virtual-threads)
- [Production and Distributed Concurrency](#production-and-distributed-concurrency)
- [Testing Concurrent Code](#testing-concurrent-code)
- [Revision Cheat Sheet](#revision-cheat-sheet)
- [Flashcards](#flashcards)

## Real Interview Questions

### Q1. What is a race condition in Java?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** They want to know if you understand correctness under interleaving.

**Short answer (say this first, 20-40 sec):**
A race condition happens when output depends on unpredictable thread timing. Shared mutable state is read and written without one atomic boundary. `count++` is read-modify-write, so two threads can overwrite each other. Fix it with atomic classes, locks, immutability, or thread confinement.

**Detailed answer:**
Bug:
```java
import java.util.concurrent.*;
class RaceBug {
  static int count;
  public static void main(String[] args) throws Exception {
    ExecutorService p = Executors.newFixedThreadPool(8);
    CountDownLatch start = new CountDownLatch(1);
    for (int t = 0; t < 8; t++) {
      p.submit(() -> {
        start.await();
        for (int i=0;i<100_000;i++) count++;
        return null;
      });
    }
    start.countDown(); p.shutdown(); p.awaitTermination(5, TimeUnit.SECONDS);
    System.out.println(count); // often less than 800000
  }
}
```
Fix:
```java
import java.util.concurrent.atomic.AtomicInteger;
class RaceFix {
  private final AtomicInteger count = new AtomicInteger();
  void inc(){
    count.incrementAndGet();
  }
}
```
Use a lock if the invariant spans multiple fields.

**Common mistakes:**
- Assuming `volatile int` makes `count++` atomic.
- Testing once and trusting timing.
- Synchronizing writes but not reads.

**Follow-ups:**
- *Is every race condition a data race?* → No; race condition is broader and includes bad ordering across DB or services.
- *Can immutability help?* → Yes, immutable objects remove shared mutable state.

### Q2. How do you prevent race conditions?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** They test whether you choose the right safety mechanism.

**Short answer (say this first, 20-40 sec):**
First avoid sharing mutable state. If sharing is required, protect every access with the same lock, use atomic classes for single-variable updates, concurrent collections for map/queue operations, and DB transactions for persistent invariants. The key is making the whole invariant atomic.

**Detailed answer:**
Bug: check-then-act is split.
```java
import java.util.*;
class StockBug {
  private final Map<String,Integer> stock = new HashMap<>(Map.of("book",1));
  boolean buy(String sku){
    Integer q=stock.get(sku);
    if(q!=null && q>0){
      stock.put(sku,q-1);
      return true;
    }
    return false;
  }
}
```
Fix:
```java
import java.util.*;
class StockFix {
  private final Object lock = new Object();
  private final Map<String,Integer> stock = new HashMap<>(Map.of("book",1));
  boolean buy(String sku){
    synchronized(lock){
      int q=stock.getOrDefault(sku,0);
      if(q<=0) return false;
      stock.put(sku,q-1);
      return true;
    }
  }
}
```
Options: immutable DTOs, local variables, `synchronized`, `ReentrantLock`, `AtomicInteger`, `ConcurrentHashMap.merge`, DB `SELECT FOR UPDATE`, and unique constraints.

**Common mistakes:**
- Locking on public strings or boxed integers.
- Using different locks for the same data.
- Forgetting multi-pod or DB-level races.

**Follow-ups:**
- *When use atomics?* → Single independent variable updates.
- *When use a DB lock?* → When multiple JVMs can update the same persistent row.

### Q3. `putIfAbsent` vs `computeIfAbsent`: atomicity and lazy evaluation?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Caches are a frequent source of subtle production bugs.

**Short answer (say this first, 20-40 sec):**
On `ConcurrentHashMap`, both are atomic for a key. `putIfAbsent(key, value)` evaluates `value` before the call, so expensive work can run even when the key exists. `computeIfAbsent(key, fn)` computes lazily only if absent and installs the result atomically.

**Detailed answer:**
Bug: eager load still runs.
```java
import java.util.concurrent.*; import java.util.concurrent.atomic.AtomicInteger;
class PutIfAbsentBug {
  static final ConcurrentHashMap<String,String> cache = new ConcurrentHashMap<>();
  static final AtomicInteger loads = new AtomicInteger();
  static String load(){ loads.incrementAndGet(); return "v"; }
  public static void main(String[] a){
    cache.put("k","old");
    cache.putIfAbsent("k", load());
    System.out.println(loads.get());
  }
}
```
Fix:
```java
import java.util.concurrent.*;
class ComputeFix {
  final ConcurrentHashMap<String,String> cache = new ConcurrentHashMap<>();
  String get(String k){ return cache.computeIfAbsent(k, key -> slowLoad(key)); }
  String slowLoad(String k){ return "v-" + k; }
}
```
Precision: mapping functions should be short, not recursively update the same key, and returning `null` records no mapping.

**Common mistakes:**
- Using get-then-put instead of an atomic map method.
- Doing long blocking I/O inside `computeIfAbsent` on hot keys.
- Expecting `HashMap.computeIfAbsent` to be thread-safe.

**Follow-ups:**
- *Which is lazy?* → `computeIfAbsent`.
- *Which for cheap constants?* → `putIfAbsent` is fine.

### Q4. Two synchronized methods are still racy. Why and how do you investigate?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** They test lock scope and compound invariants.

**Short answer (say this first, 20-40 sec):**
Synchronized instance methods lock only the current object. Two instances have different monitors, static and instance locks differ, and a race remains if an invariant spans multiple method calls. Investigate by identifying shared state, lock object identity, all read/write paths, and whether the full business operation is atomic.

**Detailed answer:**
Bug:
```java
class AccountBug {
  int balance = 100;
  synchronized boolean hasEnough(int a){ return balance >= a; }
  synchronized void withdraw(int a){ balance -= a; }
  static void pay(AccountBug x){ if(x.hasEnough(80)){ sleep(); x.withdraw(80); } }
  static void sleep(){
    try{
      Thread.sleep(10);
    }
    catch(InterruptedException e){
      Thread.currentThread().interrupt();
    }
  }
}
```
Fix:
```java
class AccountFix {
  private int balance = 100;
  synchronized boolean withdrawIfEnough(int a){ if(balance<a) return false; balance-=a; return true; }
}
```
```mermaid
flowchart TD
  A["Race or corrupt state observed"] --> B["Find shared mutable state"]
  B --> C["List all reads and writes"]
  C --> D{"Same lock object?"}
  D -->|"no"| E["Unify lock or use atomic API"]
  D -->|"yes"| F{"Invariant spans calls?"}
  F -->|"yes"| G["Create one atomic operation"]
  F -->|"no"| H["Check publication, DB, and distributed ordering"]
```

**Common mistakes:**
- Thinking `synchronized` means global locking.
- Logging no object identity while debugging.
- Locking getters/setters but not the business operation.

**Follow-ups:**
- *How prove different monitors?* → Log `System.identityHashCode(this)` and thread names.
- *Can volatile fix this?* → No, the check and update must be atomic.

### Q5. A big shared `HashMap` is used by many threads. What can go wrong?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Unsafe `HashMap` is a classic Java backend failure.

**Short answer (say this first, 20-40 sec):**
`HashMap` is not thread-safe. Concurrent mutation can lose updates, corrupt internal structure during resize, cause inconsistent reads, throw iteration exceptions, or create high CPU loops in old JDKs. Use `ConcurrentHashMap`, external locking, immutable snapshots, or a single-writer design.

**Detailed answer:**
Bug:
```java
import java.util.*; import java.util.concurrent.*;
class HashMapBug {
  static final Map<Integer,Integer> map = new HashMap<>();
  public static void main(String[] args) throws Exception {
    ExecutorService p=Executors.newFixedThreadPool(8);
    for(int t=0;t<8;t++){
      final int b=t*10000;
      p.submit(() -> { for(int i=0;i<10000;i++) map.put(b+i,i); });
    }
    p.shutdown();
    p.awaitTermination(5,TimeUnit.SECONDS);
    System.out.println(map.size());
  }
}
```
Fix:
```java
import java.util.concurrent.*;
class HashMapFix {
  private final ConcurrentHashMap<Integer,Integer> map = new ConcurrentHashMap<>();
  void put(int k,int v){
    map.put(k,v);
  }
}
```
For multi-key invariants, protect the whole operation with one lock even if the map is concurrent.

**Common mistakes:**
- Replacing the map but keeping non-atomic get-then-put logic.
- Iterating a synchronized wrapper without external synchronization.
- Treating resize as read-only.

**Follow-ups:**
- *Is `Collections.synchronizedMap` enough?* → For individual calls, yes; iteration and compound actions still need external synchronization.
- *When use immutable snapshot?* → Read-heavy config refreshed occasionally.

### Q5b. In which scenario would you use a big `HashMap`, and how do you size it?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** They want to see if you understand the memory, resizing, and GC cost of large in-memory maps, and know the alternatives.

**Short answer (say this first, 20-40 sec):**
I use a big `HashMap` for fast O(1) in-memory lookups on a bounded, fits-in-heap dataset: reference data (country/product codes), a pre-computed index built at startup, de-duplication in a batch job, or a local cache in front of a DB. I presize it with `initialCapacity = expectedSize / 0.75 + 1` to avoid repeated resizes, make it read-only (or `ConcurrentHashMap`) if threads share it, and switch to Caffeine, Redis, or a primitive map once memory, eviction, or multi-instance needs appear.

**Detailed answer:**
Good scenarios:

| Scenario | Why a big map fits | Watch out for |
|---|---|---|
| Startup-loaded reference data (e.g., 500K SKUs) | O(1) lookup, no DB round trip | Heap size, refresh strategy (swap in an immutable snapshot) |
| Batch job de-duplication / joins in memory | Much faster than per-row DB calls | Data must fit in heap; otherwise stream or use the DB |
| Frequency counting / grouping (`groupingBy`, `merge`) | Natural aggregation structure | Key cardinality explosion |
| Local L1 cache in front of Redis/DB | Microsecond reads | Needs size limit + TTL -> use Caffeine, not a raw map |

Sizing & resizing:
```java
int expected = 1_000_000;
// Default load factor 0.75: resize happens when size > capacity * 0.75.
// Presizing avoids ~20 rehash rounds (16 -> 32 -> ... -> 2^21) during loading.
Map<String, Product> bySku = new HashMap<>((int) (expected / 0.75f) + 1);

// Java 19+ convenience method that does the same math:
Map<String, Product> bySku19 = HashMap.newHashMap(expected);
```
- Capacity is always rounded up to a power of two; each resize allocates a new table and rehashes every entry (O(n) pause + garbage).
- Memory: each entry is a `Node` object (~32-48 bytes with header, hash, key, value, next references), **plus** the key and value objects themselves. 1M `String -> Long` entries can easily cost 100+ MB of heap; boxed primitives make it worse.
- GC: a huge long-lived map is promoted to the old generation; frequent rebuilds create GC pressure. Rebuild off to the side and swap one `volatile` reference.

Thread-safe read-mostly pattern:
```java
class SkuIndex {
    private volatile Map<String, Product> snapshot = Map.of();

    void reload(List<Product> products) {
        Map<String, Product> fresh = HashMap.newHashMap(products.size());
        for (Product p : products) {
            fresh.put(p.sku(), p);
        }
        snapshot = Collections.unmodifiableMap(fresh); // atomic publish
    }

    Product find(String sku) {
        return snapshot.get(sku);
    }
}
```
When NOT to use a big `HashMap`:
- Data doesn't fit comfortably in heap, or it grows without bound -> DB/Redis.
- You need eviction/TTL -> Caffeine (W-TinyLFU) or `LinkedHashMap` with `removeEldestEntry` for a simple LRU.
- Multiple app instances must see the same data -> Redis (shared cache).
- Primitive keys/values at very large scale -> primitive collections (e.g., Eclipse Collections, fastutil) to avoid boxing.
- Sorted/range queries needed -> `TreeMap`.

```mermaid
flowchart TD
    A["Need fast key lookup"] --> B{"Fits comfortably in heap?"}
    B -->|"no"| R["Use DB or Redis"]
    B -->|"yes"| C{"Shared across instances?"}
    C -->|"yes"| R
    C -->|"no"| D{"Needs eviction or TTL?"}
    D -->|"yes"| CA["Use Caffeine cache"]
    D -->|"no"| E{"Mutated by many threads?"}
    E -->|"yes"| F["ConcurrentHashMap or volatile immutable snapshot"]
    E -->|"no"| G["Presized HashMap"]
```

**Common mistakes:**
- Not presizing, so loading 1M entries triggers many resize + rehash cycles.
- Using a raw `HashMap` as an unbounded cache, which leads to a memory leak and eventually `OutOfMemoryError`.
- Mutable keys whose `hashCode` changes after insertion, so entries become unreachable.
- Assuming a local map is consistent across multiple pods.

**Follow-ups:**
- *What happens if many keys collide?* → Since Java 8, a bucket with more than 8 nodes (and table capacity ≥ 64) is treeified into a red-black tree, so the worst case becomes O(log n) instead of O(n).
- *Why is load factor 0.75?* → It's a time/space trade-off: lower means fewer collisions but more memory; higher saves memory but lengthens chains.
- *How would you find out that the map is the memory problem?* → `jmap -histo:live <pid>` or a heap dump in Eclipse MAT; look for `HashMap$Node` and the dominator tree.

### Q6. HashMap deadlock or infinite loop in Java 7 resize vs Java 8?
`🔴 Advanced` `🔥` `Stretch`

**Why they ask:** They check historical accuracy and version awareness.

**Short answer (say this first, 20-40 sec):**
In Java 7, concurrent `HashMap` resize could corrupt bucket linked lists because transfer reordered nodes, sometimes creating a cycle. A later `get` could loop forever and burn CPU; this is usually an infinite loop, not a monitor deadlock. Java 8 changed resize mechanics and adds tree bins, but `HashMap` is still unsafe for concurrent mutation.

**Detailed answer:**
Bug stress for Java 7, not deterministic and not a valid program:
```java
import java.util.*;
class Java7ResizeBug {
  static final Map<Integer,Integer> m = new HashMap<>(2);
  public static void main(String[] a) throws Exception {
    Runnable r=()->{
      for(int i=0;i<1_000_000;i++) m.put(i,i);
    }
    ;
    Thread t1=new Thread(r);
    Thread t2=new Thread(r);
    t1.start();
    t2.start();
    t1.join();
    t2.join();
  }
}
```
Fix:
```java
import java.util.concurrent.*;
class ResizeFix {
  private final ConcurrentHashMap<Integer,Integer> m = new ConcurrentHashMap<>();
  void put(int k,int v){
    m.put(k,v);
  }
}
```
Java 7 used linked-list transfer vulnerable to corruption under races. Java 8 is more robust internally but gives no thread-safety guarantee for `HashMap`.

**Common mistakes:**
- Saying Java 8 made `HashMap` concurrent.
- Calling every high CPU hang a deadlock.
- Depending on the reproducer to fail every time.

**Follow-ups:**
- *How detect it?* → Thread dump or profiler shows threads stuck in `HashMap.get` or traversal.
- *Correct production fix?* → Stop concurrent mutation; use CHM or locks.

### Q7. Why does `ConcurrentHashMap` reject null keys and values?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** It tests API precision and concurrent ambiguity.

**Short answer (say this first, 20-40 sec):**
`ConcurrentHashMap` forbids null because `get(key) == null` must unambiguously mean absent. In concurrent code, allowing null would make it unclear whether the key is absent, mapped to null, removed, or not yet visible. Use `Optional`, a sentinel value, or a typed result for negative caching.

**Detailed answer:**
Bug:
```java
import java.util.concurrent.*;
class NullBug {
  public static void main(String[] a){
    new ConcurrentHashMap<String,String>().put("x", null);
  }
}
```
Fix:
```java
import java.util.*; import java.util.concurrent.*;
class NullFix {
  final ConcurrentHashMap<String, Optional<String>> cache = new ConcurrentHashMap<>();
  Optional<String> get(String k){
    return cache.computeIfAbsent(k, this::load);
  }
  Optional<String> load(String k){
    return Optional.empty();
  }
}
```
`HashMap` allows one null key and many null values, but it is not thread-safe.

**Common mistakes:**
- Using null as a negative-cache marker.
- Calling `containsKey` then `get` and assuming the pair is atomic.
- Returning null from `computeIfAbsent` and expecting a cached null.

**Follow-ups:**
- *Does CHM allow null keys?* → No.
- *Best negative-cache value?* → `Optional.empty()` or a domain-specific sentinel.

### Q8. Shared `HashMap` passes tests but fails in production. Why?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** They want production debugging instincts.

**Short answer (say this first, 20-40 sec):**
Tests often have low concurrency, small data, deterministic timing, and no real CPU or GC pressure. Production adds many threads, resizes, collisions, and rare interleavings, so unsafe maps fail as missing entries, latency spikes, or high CPU. Reproduce with aligned thread starts, high key counts, repeated runs, and production-like pool sizes.

**Detailed answer:**
Bug:
```java
import java.util.*;
class TestPassesBug {
  static final Map<Integer,Integer> cache=new HashMap<>();
  static int get(int k){
    Integer v=cache.get(k);
    if(v==null){
      v=k*10;
      cache.put(k,v);
    }
    return v;
  }
}
```
Fix:
```java
import java.util.concurrent.*;
class TestPassesFix {
  final ConcurrentHashMap<Integer,Integer> cache=new ConcurrentHashMap<>();
  int get(int k){
    return cache.computeIfAbsent(k, x -> x*10);
  }
}
```
Stress tests should use `CountDownLatch` start gates, many iterations, and invariant assertions rather than only checking no exception.

**Common mistakes:**
- Treating intermittent as impossible.
- Fixing by increasing timeouts.
- Not measuring cache hit ratio and map size.

**Follow-ups:**
- *What evidence to collect?* → Thread dumps, CPU profile, cache metrics, key cardinality.
- *What tool for JMM-level tests?* → jcstress.

### Q9. Shared `HashMap` causes timeouts but no crash. How?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** Many concurrency failures are latency failures.

**Short answer (say this first, 20-40 sec):**
Unsafe shared state can cause threads to spin, traverse corrupted structures, duplicate slow work, or create GC pressure. The JVM stays alive, but requests time out because worker threads are busy or queued. A common pattern is cache stampede from non-atomic get-load-put.

**Detailed answer:**
Bug:
```java
import java.util.*;
class StampedeBug {
  static final Map<String,String> c=new HashMap<>();
  static String get(String k){
    String v=c.get(k);
    if(v==null){
      v=slow(k);
      c.put(k,v);
    }
    return v;
  }
  static String slow(String k){
    try{
      Thread.sleep(500);
    }
    catch(InterruptedException e){
      Thread.currentThread().interrupt();
    }
    return k;
  }
}
```
Fix:
```java
import java.util.concurrent.*;
class StampedeFix {
  final ConcurrentHashMap<String,CompletableFuture<String>> c=new ConcurrentHashMap<>();
  final ExecutorService io=Executors.newFixedThreadPool(20);
  String get(String k) throws Exception {
    return c.computeIfAbsent(
            k,
            x -> CompletableFuture.supplyAsync(() -> slow(x), io))
        .get(1, TimeUnit.SECONDS);
  }
  static String slow(String k){
    return k;
  }
}
```
Investigate with thread dumps, CPU profiles, queue depth, hit ratio, load count, and downstream call count.

**Common mistakes:**
- Waiting for an exception when the symptom is p99 latency.
- Retrying aggressively and amplifying load.
- Ignoring executor saturation.

**Follow-ups:**
- *What is cache stampede?* → Many threads recompute the same missing key simultaneously.
- *Why cache futures?* → It deduplicates in-flight loads.

### Q10. Where do concurrency issues occur: JVM, DB, or distributed systems?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** Backend correctness crosses process boundaries.

**Short answer (say this first, 20-40 sec):**
Concurrency issues happen in JVM memory, database rows, message queues, and multi-pod workflows. JVM locks protect only one process; DB transactions protect persistent state; distributed systems need idempotency, constraints, queue partitioning, or distributed locks. Use the narrowest mechanism that protects the real shared resource.

**Detailed answer:**
Bug and fix examples:
```java
// JVM bug: count++ is racy. Fix: AtomicInteger.incrementAndGet().
```
```sql
-- PostgreSQL lost update fix
BEGIN;
SELECT balance FROM account WHERE id = 1 FOR UPDATE;
UPDATE account SET balance = balance - 10 WHERE id = 1;
COMMIT;
```
```java
import java.util.concurrent.*;
class IdempotencyFix {
  final ConcurrentHashMap<String,String> done=new ConcurrentHashMap<>();
  String handle(String requestId){
    return done.computeIfAbsent(requestId, this::doOnce);
  }
  String doOnce(String id){
    return "ok";
  }
}
```
Layer mapping: JVM race -> locks/atomics; DB lost update -> transaction/version/constraint; distributed duplicate -> idempotency key, queue partition, Redis lock with token and TTL.

**Common mistakes:**
- Using `synchronized` for multi-pod correctness.
- Trusting Redis locks without DB constraints.
- Forgetting queue redelivery.

**Follow-ups:**
- *Strongest final guard?* → Usually a DB constraint or transactional update.
- *When use Redis lock?* → Short cross-node critical sections, with expiry and safe release token.
## Thread Basics and Executors

### Q11. Explain the Java thread lifecycle.
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Thread dumps use these states.

**Short answer (say this first, 20-40 sec):**
A thread moves from `NEW` to `RUNNABLE` after `start()`, may enter blocked or waiting states, and finally becomes `TERMINATED`. `RUNNABLE` means eligible to run or currently running. These states help debug deadlocks, blocking, and starvation.

**Detailed answer:**
```mermaid
stateDiagram-v2
  [*] --> NEW
  NEW --> RUNNABLE: start
  RUNNABLE --> BLOCKED: monitor unavailable
  BLOCKED --> RUNNABLE: monitor acquired
  RUNNABLE --> WAITING: wait or join
  WAITING --> RUNNABLE: notify or completion
  RUNNABLE --> TIMEDWAITING: sleep or timed wait
  TIMEDWAITING --> RUNNABLE: timeout
  RUNNABLE --> TERMINATED: run exits
  TERMINATED --> [*]
```
Code: `new Thread(r)` is `NEW`; `start()` schedules execution; `join()` waits for termination.

**Common mistakes:**
- Calling `run()` directly.
- Thinking `RUNNABLE` always means consuming CPU.
- Confusing `BLOCKED` with `WAITING`.

**Follow-ups:**
- *Can a thread restart?* → No, `start()` twice throws `IllegalThreadStateException`.
- *What creates BLOCKED?* → Waiting to enter a `synchronized` monitor.

### Q12. Compare `Runnable`, `Callable`, and `Future`.
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** These are executor fundamentals.

**Short answer (say this first, 20-40 sec):**
`Runnable` has no result and no checked exception in its method signature. `Callable<T>` returns a value and can throw checked exceptions. `Future<T>` is the handle returned by submitted work and supports result retrieval, timeout, cancellation, and completion checks.

**Detailed answer:**
```java
import java.util.concurrent.*;
ExecutorService pool = Executors.newSingleThreadExecutor();
Future<?> r = pool.submit(() -> System.out.println("log"));
Future<Integer> c = pool.submit(() -> 42);
r.get(); System.out.println(c.get(1, TimeUnit.SECONDS));
pool.shutdown();
```
`Future.get()` blocks and wraps task exceptions in `ExecutionException`.

**Common mistakes:**
- Forgetting that `get()` can block indefinitely.
- Swallowing `InterruptedException`.
- Using `execute` when result handling is required.

**Follow-ups:**
- *Which one returns a value?* → `Callable`.
- *Which API composes async steps better?* → `CompletableFuture`.

### Q13. Why use `ExecutorService` instead of creating threads manually?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Real services need controlled concurrency.

**Short answer (say this first, 20-40 sec):**
`ExecutorService` reuses worker threads, limits concurrency, queues tasks, exposes `Future`s, and supports controlled shutdown. Creating a raw thread per request is expensive and can exhaust memory or OS thread limits.

**Detailed answer:**
```java
ExecutorService pool = Executors.newFixedThreadPool(20);
Future<String> f = pool.submit(() -> callRemote());
String result = f.get(500, TimeUnit.MILLISECONDS);
pool.shutdown();
```
Avoid unbounded factories like `newCachedThreadPool()` for uncontrolled production traffic.

**Common mistakes:**
- Not shutting down custom executors.
- Blocking forever in pool tasks.
- Sharing one pool for all workloads.

**Follow-ups:**
- *`execute` vs `submit`?* → `execute` has no result; `submit` returns `Future`.
- *What happens after shutdown?* → New tasks are rejected.

### Q14. Explain `ThreadPoolExecutor` parameters and submission flow.
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** Wrong pool settings cause outages.

**Short answer (say this first, 20-40 sec):**
A `ThreadPoolExecutor` is configured with core size, max size, keep-alive, queue, thread factory, and rejection handler. It creates core workers first, then queues tasks, then grows up to max only when the queue is full, then rejects.

**Detailed answer:**
```mermaid
flowchart TD
  A["Submit task"] --> B{"Workers less than core?"}
  B -->|"yes"| C["Create core worker"]
  B -->|"no"| D{"Queue accepts task?"}
  D -->|"yes"| E["Wait in queue"]
  D -->|"no"| F{"Workers less than max?"}
  F -->|"yes"| G["Create extra worker"]
  F -->|"no"| H["Reject task"]
```
```java
ThreadPoolExecutor p = new ThreadPoolExecutor(
    10,
    50,
    60,
    TimeUnit.SECONDS,
    new ArrayBlockingQueue<>(500),
    r -> new Thread(r, "orders-worker"),
    new ThreadPoolExecutor.CallerRunsPolicy());
```

**Common mistakes:**
- Using unbounded queues and expecting max size to help.
- No thread names.
- Silent discard rejection.

**Follow-ups:**
- *What is `CallerRunsPolicy`?* → Caller executes rejected task, creating backpressure.
- *What is `SynchronousQueue`?* → Zero-capacity direct handoff queue.

### Q15. How do you size a thread pool?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** Sizing affects latency and throughput.

**Short answer (say this first, 20-40 sec):**
CPU-bound pools start near CPU cores. I/O-bound pools can be larger because threads wait. A common estimate is `threads = cores * (1 + waitTime / computeTime)`, then validate with load tests, p99 latency, queue depth, and downstream capacity.

**Detailed answer:**
```java
int cores = Runtime.getRuntime().availableProcessors();
int cpuThreads = cores;
int ioThreads = cores * 8; // starting point, not a law
```
Watch active count, queue size, rejections, context switches, DB pool usage, and downstream rate limits.

**Common mistakes:**
- Adding threads when the DB pool has fewer connections.
- Mixing CPU and blocking I/O in one pool.
- Optimizing without load tests.

**Follow-ups:**
- *What does Little's Law say?* → Concurrency is roughly throughput times latency.
- *Can more threads reduce throughput?* → Yes, via context switching and contention.

## Locks, Memory Model, and Atomicity

### Q16. How does `synchronized` work: monitor, instance vs static?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** It is the base locking primitive.

**Short answer (say this first, 20-40 sec):**
Every object can be a monitor. An instance synchronized method locks `this`, while a static synchronized method locks the class object. Unlocking a monitor happens-before a later lock on the same monitor, giving both mutual exclusion and visibility.

**Detailed answer:**
```java
class SyncDemo {
  synchronized void instanceMethod() { }
  static synchronized void staticMethod() { }
  void block(){ synchronized(this){ } }
}
```
Instance and static locks do not block each other because they are different monitors.

**Common mistakes:**
- Locking on mutable or public objects.
- Assuming static and instance locks are the same.
- Holding monitors during network calls.

**Follow-ups:**
- *Is synchronized reentrant?* → Yes.
- *Is it fair?* → No fairness guarantee.

### Q17. `synchronized` vs `ReentrantLock`, including `tryLock` and fairness?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** They test lock trade-offs.

**Short answer (say this first, 20-40 sec):**
`synchronized` is simpler and releases automatically. `ReentrantLock` supports timed acquisition, interruptible lock acquisition, optional fairness, lock status methods, and multiple `Condition`s. Use `ReentrantLock` only when those features matter.

**Detailed answer:**
```java
import java.util.concurrent.*; import java.util.concurrent.locks.*;
class LockDemo {
  private final ReentrantLock lock = new ReentrantLock(true);
  boolean update() throws InterruptedException {
    if(!lock.tryLock(100, TimeUnit.MILLISECONDS)) return false;
    try {
      return true;
    }
    finally {
      lock.unlock();
    }
  }
}
```
Fair locks reduce starvation risk but can lower throughput.

**Common mistakes:**
- Missing `unlock()` in `finally`.
- Enabling fairness without measurement.
- Using locks for slow remote calls.

**Follow-ups:**
- *Why use `tryLock`?* → Timeout, graceful fallback, deadlock avoidance.
- *Can `synchronized` be interrupted while blocked?* → No.

### Q18. What is `ReadWriteLock`?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** It is useful for read-heavy shared state.

**Short answer (say this first, 20-40 sec):**
`ReadWriteLock` allows many readers together but only one writer exclusively. It helps when reads dominate and read sections are not trivial. It can hurt when writes are frequent or writers starve.

**Detailed answer:**
```java
import java.util.*; import java.util.concurrent.locks.*;
class Catalog {
  private final Map<String,String> m=new HashMap<>();
  private final ReadWriteLock rw=new ReentrantReadWriteLock();
  String get(String k){
    rw.readLock().lock();
    try{
      return m.get(k);
    }
    finally{
      rw.readLock().unlock();
    }
  }
  void put(String k,String v){
    rw.writeLock().lock();
    try{
      m.put(k,v);
    }
    finally{
      rw.writeLock().unlock();
    }
  }
}
```

**Common mistakes:**
- Trying unsafe read-lock to write-lock upgrade.
- Using it for tiny operations.
- Forgetting writer starvation.

**Follow-ups:**
- *Can readers and writer run together?* → No.
- *Can multiple readers run together?* → Yes.

### Q19. What is `StampedLock`?
`🔴 Advanced` `Stretch`

**Why they ask:** It checks advanced lock awareness.

**Short answer (say this first, 20-40 sec):**
`StampedLock` supports write locks, read locks, and optimistic reads. An optimistic read must be validated before using the read values. It is not reentrant and is more complex than `ReadWriteLock`.

**Detailed answer:**
```java
import java.util.concurrent.locks.*;
class Point {
  double x,y;
  final StampedLock sl=new StampedLock();
  double dist(){
    long s=sl.tryOptimisticRead();
    double a=x,b=y;
    if(!sl.validate(s)){
      s=sl.readLock();
      try{
        a=x;
        b=y;
      }
      finally{
        sl.unlockRead(s);
      }
    }
    return Math.hypot(a,b);
  }
  void move(double dx,double dy){
    long s=sl.writeLock();
    try{
      x+=dx;
      y+=dy;
    }
    finally{
      sl.unlockWrite(s);
    }
  }
}
```

**Common mistakes:**
- Not validating optimistic reads.
- Assuming reentrancy.
- Using it where maintainability matters more.

**Follow-ups:**
- *Why optimistic read?* → Avoid blocking if no writer changed state.
- *Should beginners prefer it?* → Usually no.

### Q20. Explain `volatile` and happens-before.
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** JMM basics are essential.

**Short answer (say this first, 20-40 sec):**
`volatile` gives visibility and ordering for a variable. A write to a volatile field happens-before every later read of that field. It does not make compound actions like `x++` atomic.

**Detailed answer:**
```java
class StopFlag {
  private volatile boolean running = true;
  void stop(){
    running=false;
  }
  void loop(){
    while(running){
      doWork();
    }
  }
  void doWork(){
  }
}
```
Other happens-before edges: `Thread.start`, successful `Thread.join`, monitor unlock-to-lock, volatile write-to-read, and task completion to `Future.get`.

**Common mistakes:**
- Using volatile counters.
- Making a map reference volatile and assuming map internals are safe.
- Publishing objects unsafely.

**Follow-ups:**
- *Visibility vs atomicity?* → Visibility means seeing writes; atomicity means indivisible update.
- *Does volatile block threads?* → No.

### Q21. CAS, atomic classes, and `LongAdder`?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** Atomics are common in counters and lock-free code.

**Short answer (say this first, 20-40 sec):**
CAS updates a value only if it still equals an expected value. Atomic classes use CAS loops for lock-free single-variable updates. `LongAdder` spreads contention across cells and is excellent for high-contention metrics, but not ideal when every read must be exactly linearizable.

**Detailed answer:**
```java
import java.util.concurrent.atomic.*;
class Reserve {
  private final AtomicInteger stock=new AtomicInteger(10);
  boolean one(){
    while(true){
      int s=stock.get();
      if(s<=0) return false;
      if(stock.compareAndSet(s,s-1)) return true;
    }
  }
}
class Metrics {
  private final LongAdder requests=new LongAdder();
  void mark(){
    requests.increment();
  }
  long count(){
    return requests.sum();
  }
}
```

**Common mistakes:**
- CAS loops without exit paths.
- Using `LongAdder` for bank balances.
- Assuming lock-free means wait-free.

**Follow-ups:**
- *Why is LongAdder faster under contention?* → It shards updates.
- *What is linearizable?* → Operations appear atomic at one point in time.

### Q22. What is the ABA problem?
`🔴 Advanced` `Stretch`

**Why they ask:** It tests lock-free correctness.

**Short answer (say this first, 20-40 sec):**
ABA occurs when a value changes from A to B and back to A, so a CAS sees A and wrongly assumes nothing changed. It matters in algorithms where identity or links matter. Use version stamps, `AtomicStampedReference`, or designs that avoid node reuse.

**Detailed answer:**
```java
import java.util.concurrent.atomic.AtomicStampedReference;
class AbaFix {
  final AtomicStampedReference<String> ref=new AtomicStampedReference<>("A",0);
  boolean set(String next){
    int[] st=new int[1];
    String cur=ref.get(st);
    return ref.compareAndSet(cur,next,st[0],st[0]+1);
  }
}
```
The stamp changes even if the value cycles back.

**Common mistakes:**
- Assuming CAS proves no intermediate changes.
- Applying ABA fear to simple counters unnecessarily.
- Writing custom lock-free stacks casually.

**Follow-ups:**
- *Where does ABA matter?* → Lock-free stacks, queues, and pointer-like references.
- *Main fix?* → Add a version/stamp.

### Q23. What is double-checked locking?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** It is a classic safe-publication trap.

**Short answer (say this first, 20-40 sec):**
Double-checked locking checks a singleton field before and inside a synchronized block to avoid locking after initialization. It is correct only when the field is `volatile`. Without `volatile`, another thread can observe a partially constructed object.

**Detailed answer:**
```java
class SafeSingleton {
  private static volatile SafeSingleton instance;
  private SafeSingleton(){
  }
  static SafeSingleton get(){
    SafeSingleton local=instance;
    if(local==null){
      synchronized(SafeSingleton.class){
        local=instance;
        if(local==null){
          local=new SafeSingleton();
          instance=local;
        }
      }
    }
    return local;
  }
}
```
Simpler: use enum singleton or initialization-on-demand holder.

**Common mistakes:**
- Missing `volatile`.
- Overusing singletons in Spring apps.
- Publishing `this` during construction.

**Follow-ups:**
- *Why volatile?* → Prevents unsafe publication and reordering.
- *Simpler lazy pattern?* → Static holder class.

### Q24. What is safe publication?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** Visibility bugs often come from unsafe publication.

**Short answer (say this first, 20-40 sec):**
Safe publication means another thread can see an object only after construction is complete and visible. Use final fields, static initialization, volatile references, locks, or thread-safe containers. Without it, stale or partially initialized state may be observed.

**Detailed answer:**
```java
class SafePub { static final Helper HELPER = new Helper(42); record Helper(int value){} }
class VolatilePub {
  private static volatile Helper helper;
  static void init(){
    helper=new Helper(42);
  }
  record Helper(int value){
  }
}
```
Do not let `this` escape constructors.

**Common mistakes:**
- Assigning to a non-volatile static from one thread and reading from another.
- Mutable fields in supposedly immutable objects.
- Assuming reference assignment alone publishes internals.

**Follow-ups:**
- *Are final fields special?* → Yes, if the object is constructed correctly.
- *Does Spring help?* → It safely publishes beans, but your mutable bean state still needs safety.

### Q25. How do you handle interruption correctly?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Clean shutdown depends on it.

**Short answer (say this first, 20-40 sec):**
Interruption is cooperative cancellation. When a blocking method throws `InterruptedException`, either stop work or restore the interrupt flag using `Thread.currentThread().interrupt()`. Never swallow interrupts silently.

**Detailed answer:**
```java
try { Thread.sleep(1000); }
catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
```
Loop pattern:
```java
while (!Thread.currentThread().isInterrupted()) { doOneUnit(); }
```

**Common mistakes:**
- Treating interrupt as forceful kill.
- Logging and continuing forever.
- Using blocking APIs without timeouts.

**Follow-ups:**
- *What does `Future.cancel(true)` do?* → Attempts to interrupt the task.
- *Can interrupt break monitor entry?* → No, not `synchronized` BLOCKED.

## Concurrent Collections and Coordination

### Q26. Java 7 vs Java 8 `ConcurrentHashMap` internals?
`🔴 Advanced` `🔥` `Must-know SDE-2`

**Why they ask:** Version precision matters.

**Short answer (say this first, 20-40 sec):**
Java 7 used segmented locking: multiple segment objects each guarded part of the table. Java 8 removed fixed segments and uses CAS for empty bins, synchronized bin locking for updates, tree bins under heavy collisions, and cooperative resizing. Reads are mostly non-blocking.

**Detailed answer:**
```java
import java.util.concurrent.*;
class Freq {
  final ConcurrentHashMap<String,Long> m=new ConcurrentHashMap<>();
  void inc(String w){
    m.merge(w,1L,Long::sum);
  }
}
```
Java 8 `concurrencyLevel` is mostly a sizing hint, not a fixed segment count.

**Common mistakes:**
- Saying Java 8 CHM is lock-free everywhere.
- Using `size()` as a cheap exact value under mutation.
- Mutating non-thread-safe values stored in CHM.

**Follow-ups:**
- *Are CHM iterators fail-fast?* → No, weakly consistent.
- *What are tree bins?* → Red-black trees used for long collision chains.

### Q27. When use `CopyOnWriteArrayList`?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** It tests read-heavy collection selection.

**Short answer (say this first, 20-40 sec):**
`CopyOnWriteArrayList` copies its array on every write and gives readers a stable snapshot. Use it for many reads and rare writes, such as listener lists. Avoid it for large or frequently modified lists.

**Detailed answer:**
```java
import java.util.concurrent.CopyOnWriteArrayList;
class Listeners {
  final CopyOnWriteArrayList<Runnable> list=new CopyOnWriteArrayList<>();
  void add(Runnable r){
    list.add(r);
  }
  void fire(){
    for(Runnable r:list) r.run();
  }
}
```
Iterators do not throw `ConcurrentModificationException` and do not see later writes.

**Common mistakes:**
- Using it for write-heavy workloads.
- Expecting iterator to reflect new elements.
- Assuming deep immutability of contained objects.

**Follow-ups:**
- *Best use case?* → Listener registry.
- *Why are writes costly?* → They copy the backing array.

### Q28. BlockingQueue and producer-consumer?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** It is core safe handoff and backpressure.

**Short answer (say this first, 20-40 sec):**
`BlockingQueue` is a thread-safe queue where producers block or time out when full and consumers block or time out when empty. It is the preferred producer-consumer primitive over manual wait/notify. Bounded queues provide backpressure.

**Detailed answer:**
```java
import java.util.concurrent.*;
class PC {
  public static void main(String[] a) throws Exception {
    BlockingQueue<String> q=new ArrayBlockingQueue<>(10);
    Thread p=new Thread(() -> {
      try{
        q.put("job");
        q.put("STOP");
      }catch(InterruptedException e){
        Thread.currentThread().interrupt();
      }
    });
    Thread c=new Thread(() -> {
      try{
        for(;;){
          String x=q.take();
          if("STOP".equals(x)) break;
          System.out.println(x);
        }
      }catch(InterruptedException e){
        Thread.currentThread().interrupt();
      }
    });
    p.start();
    c.start();
    p.join();
    c.join();
  }
}
```

**Common mistakes:**
- Using unbounded queues by default.
- Forgetting poison pills or shutdown protocol.
- Using `add` when `put` or `offer` is intended.

**Follow-ups:**
- *What is `SynchronousQueue`?* → Zero-capacity handoff.
- *What is backpressure?* → Slowing producers when consumers lag.

### Q29. Explain `wait`, `notify`, and `notifyAll`.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** It checks monitor protocol.

**Short answer (say this first, 20-40 sec):**
They are monitor methods and must be called while holding the object's monitor. `wait` releases the monitor and suspends the thread. Always wait in a `while` loop because wakeups can be spurious or the condition may no longer be true.

**Detailed answer:**
```java
import java.util.*;
class Buffer<T>{
  private final Queue<T> q=new ArrayDeque<>();
  synchronized void put(T x){
    q.add(x);
    notifyAll();
  }
  synchronized T take() throws InterruptedException {
    while(q.isEmpty()) wait();
    return q.remove();
  }
}
```
Prefer `BlockingQueue` for most producer-consumer code.

**Common mistakes:**
- Using `if` instead of `while`.
- Calling `wait` without owning the monitor.
- Using `notify` when several condition types exist.

**Follow-ups:**
- *Why does wait release lock?* → So another thread can change the condition.
- *Modern alternative?* → `BlockingQueue`, `Condition`, latches.

### Q30. CountDownLatch vs CyclicBarrier vs Semaphore?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** These primitives solve different coordination problems.

**Short answer (say this first, 20-40 sec):**
`CountDownLatch` is one-shot and releases waiters when count hits zero. `CyclicBarrier` waits for a fixed group repeatedly. `Semaphore` limits permits, often controlling access to a scarce resource.

**Detailed answer:**
```java
CountDownLatch start = new CountDownLatch(1); start.countDown(); start.await();
CyclicBarrier barrier = new CyclicBarrier(3, () -> System.out.println("ready"));
Semaphore dbCalls = new Semaphore(10); dbCalls.acquire(); try { callDb(); } finally { dbCalls.release(); }
```
Use latch for test start gates, barrier for phase algorithms, semaphore for rate/concurrency limiting.

**Common mistakes:**
- Trying to reset `CountDownLatch`.
- Forgetting semaphore release in `finally`.
- Not handling broken barriers.

**Follow-ups:**
- *Which limits API calls?* → `Semaphore`.
- *Which starts many threads at once?* → `CountDownLatch`.

### Q31. Atomic compound operations on `ConcurrentHashMap`?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** CHM is often misused.

**Short answer (say this first, 20-40 sec):**
Use `putIfAbsent`, `remove(key,value)`, `replace`, `compute`, `computeIfAbsent`, `computeIfPresent`, and `merge` for atomic per-key read-modify-write operations. `ConcurrentHashMap` makes individual operations thread-safe, but not your multi-line sequences.

**Detailed answer:**
Bug:
```java
Integer old = counts.get("java"); counts.put("java", old == null ? 1 : old + 1);
```
Fix:
```java
counts.merge("java", 1, Integer::sum);
```
Keep compute functions short and side-effect-safe.

**Common mistakes:**
- `containsKey` then `put`.
- Long blocking calls inside mapping functions.
- Mutating unsafe values stored in the map.

**Follow-ups:**
- *Atomic for whole map?* → No, per key/bin.
- *Can mapping return null?* → Yes, but no mapping is recorded or it may remove depending on method.

### Q32. How do concurrent collections differ from synchronized wrappers?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** It reveals API selection skill.

**Short answer (say this first, 20-40 sec):**
Synchronized wrappers serialize method calls with one lock. Concurrent collections use finer-grained locking, CAS, weakly consistent iterators, and specialized algorithms to scale better. Compound actions still need atomic APIs or external locking.

**Detailed answer:**
```java
Map<String,Integer> sync = Collections.synchronizedMap(new HashMap<>());
synchronized(sync){ for(String k: sync.keySet()) System.out.println(k); }
ConcurrentHashMap<String,Integer> chm = new ConcurrentHashMap<>(); chm.merge("x",1,Integer::sum);
```

**Common mistakes:**
- Iterating synchronized wrappers without locking the wrapper.
- Assuming CHM makes every sequence atomic.
- Expecting fail-fast CHM iterators.

**Follow-ups:**
- *What is weak consistency?* → Iterator may reflect some concurrent changes without throwing.
- *When choose wrapper?* → Small low-contention code.

### Q33. How would you design a thread-safe cache?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** Caches combine concurrency, memory, and downstream protection.

**Short answer (say this first, 20-40 sec):**
Use concurrent maps or a library like Caffeine, define max size and TTL, deduplicate in-flight loads, handle loader failures, and expose metrics. Do not build an infinite `HashMap` cache with get-load-put.

**Detailed answer:**
```java
import java.util.concurrent.*;
class AsyncCache<K,V>{
  private final ConcurrentHashMap<K,CompletableFuture<V>> m=new ConcurrentHashMap<>();
  private final Executor ex;
  AsyncCache(Executor ex){
    this.ex=ex;
  }
  CompletableFuture<V> get(K k, java.util.function.Function<K,V> loader){
    return m.computeIfAbsent(k, x -> CompletableFuture.supplyAsync(() -> loader.apply(x), ex));
  }
}
```
Production needs TTL, maximum size, hit ratio, load latency, eviction count, and failure removal.

**Common mistakes:**
- Infinite maps causing heap growth.
- Cache stampede.
- Caching null ambiguously.

**Follow-ups:**
- *Common Java cache library?* → Caffeine.
- *What if load fails?* → Remove or cache failure deliberately with short TTL.

### Q34. What is backpressure?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** Overload behavior matters in backend systems.

**Short answer (say this first, 20-40 sec):**
Backpressure slows or rejects producers when consumers cannot keep up. Without it, queues grow, memory rises, p99 latency explodes, and retries amplify traffic. Use bounded queues, semaphores, rate limits, bulkheads, and clear 429/503 responses.

**Detailed answer:**
```java
import java.util.concurrent.*;
class Limited {
  private final Semaphore s=new Semaphore(100);
  String handle() throws InterruptedException {
    if(!s.tryAcquire(50, TimeUnit.MILLISECONDS)) return "429";
    try{
      return "ok";
    }
    finally{
      s.release();
    }
  }
}
```

**Common mistakes:**
- Unbounded queues.
- Immediate retry storms.
- One pool for every dependency.

**Follow-ups:**
- *What is a bulkhead?* → Separate resource pool per workload/dependency.
- *Which rejection policy gives backpressure?* → `CallerRunsPolicy`.

### Q35. What are common executor rejection policies?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** Rejection decides overload safety.

**Short answer (say this first, 20-40 sec):**
`AbortPolicy` throws, `CallerRunsPolicy` makes the caller run the task, `DiscardPolicy` silently drops, and `DiscardOldestPolicy` drops the oldest queued task. For important work, avoid silent loss; prefer explicit backpressure or fail-fast.

**Detailed answer:**
```java
ThreadPoolExecutor p = new ThreadPoolExecutor(
    4,
    8,
    30,
    TimeUnit.SECONDS,
    new ArrayBlockingQueue<>(100),
    new ThreadPoolExecutor.CallerRunsPolicy());
```
Record rejection counts and queue depth as metrics.

**Common mistakes:**
- Silent discard for business-critical tasks.
- No rejection monitoring.
- Unbounded queues hiding overload.

**Follow-ups:**
- *HTTP response on overload?* → Usually 429 or 503.
- *Why caller-runs helps?* → It slows producers.
## Async, ForkJoin, and Virtual Threads

### Q36. How do you chain `CompletableFuture`s and handle exceptions?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** Async composition is common in service code.

**Short answer (say this first, 20-40 sec):**
Use `thenApply` for synchronous transformation, `thenCompose` when the next step returns a future, `thenCombine` for independent futures, and `exceptionally` or `handle` for failures. Use a custom executor for blocking I/O instead of relying on the common pool.

**Detailed answer:**
```java
import java.util.concurrent.*;
class CfDemo {
  final ExecutorService io=Executors.newFixedThreadPool(20);
  CompletableFuture<String> summary(String id){
    return CompletableFuture.supplyAsync(() -> user(id), io)
        .thenCompose(u -> CompletableFuture.supplyAsync(() -> orders(u), io))
        .thenApply(n -> "orders="+n)
        .exceptionally(ex -> "fallback");
  }
  String user(String id){
    return id;
  }
  int orders(String u){
    return 3;
  }
}
```
```mermaid
flowchart LR
  A["load user"] --> B["thenCompose load orders"]
  B --> C["thenApply build response"]
  B --> D["exceptionally fallback"]
  C --> E["result"]
  D --> E
```

**Common mistakes:**
- Using `thenApply` and creating nested futures.
- Blocking with `join()` in the same pool.
- Running blocking I/O on the common pool.

**Follow-ups:**
- *`handle` vs `exceptionally`?* → `handle` receives success or failure; `exceptionally` only handles failure.
- *How combine independent calls?* → `thenCombine` or `allOf`.

### Q37. Explain `ForkJoinPool` and work stealing.
`🔴 Advanced` `Stretch`

**Why they ask:** Parallel streams and recursive tasks use it.

**Short answer (say this first, 20-40 sec):**
`ForkJoinPool` is optimized for divide-and-conquer CPU tasks. Workers keep deques and idle workers steal tasks from busy workers. It works best for non-blocking, independent subtasks with enough work per task.

**Detailed answer:**
```java
import java.util.concurrent.*;
class SumTask extends RecursiveTask<Long>{
  final int[] a;
  final int lo,hi;
  SumTask(int[] a,int lo,int hi){
    this.a=a;
    this.lo=lo;
    this.hi=hi;
  }
  protected Long compute(){
    if(hi-lo<=1000){
      long s=0;
      for(int i=lo;i<hi;i++) s+=a[i];
      return s;
    }
    int mid=(lo+hi)>>>1;
    SumTask left=new SumTask(a,lo,mid);
    left.fork();
    return new SumTask(a,mid,hi).compute()+left.join();
  }
}
```

**Common mistakes:**
- Blocking on I/O inside ForkJoin tasks.
- Creating too many tiny tasks.
- Forking both halves instead of computing one directly.

**Follow-ups:**
- *Default pool for parallel streams?* → Common `ForkJoinPool`.
- *What is work stealing?* → Idle workers take tasks from other workers' queues.

### Q38. What are parallel stream pitfalls?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** `.parallelStream()` is frequently misused.

**Short answer (say this first, 20-40 sec):**
Parallel streams can help CPU-bound independent work on large collections. They can hurt for small tasks, blocking I/O, shared mutable state, ordered work, or when the common pool is already busy. Measure before using them.

**Detailed answer:**
Bug:
```java
List<Integer> out = new ArrayList<>();
List.of(1,2,3,4).parallelStream().forEach(out::add); // unsafe ArrayList mutation
```
Fix:
```java
List<Integer> out = List.of(1,2,3,4).parallelStream().map(x -> x * 2).toList();
```
Avoid side effects and blocking calls.

**Common mistakes:**
- Adding `.parallel()` as a magic speed fix.
- Mutating external collections.
- Forgetting common-pool contention.

**Follow-ups:**
- *Can order change?* → Yes unless ordered operations are used, often with performance cost.
- *Best for DB calls?* → Usually no; use explicit bounded executor or reactive/async design.

### Q39. What are Java 21 virtual threads and pinning?
`🔴 Advanced` `🔥` `Stretch`

**Why they ask:** Modern Java backend interviews increasingly ask this.

**Short answer (say this first, 20-40 sec):**
Virtual threads are lightweight Java threads finalized in Java 21. They make blocking-style I/O scale because a blocked virtual thread usually unmounts from its carrier platform thread. Pinning happens when it cannot unmount, commonly during blocking inside a `synchronized` block or native call.

**Detailed answer:**
```java
try (ExecutorService ex = Executors.newVirtualThreadPerTaskExecutor()) {
  Future<String> f = ex.submit(() -> { Thread.sleep(100); return Thread.currentThread().toString(); });
  System.out.println(f.get());
}
```
Pinning risk:
```java
class PinRisk { synchronized void call() throws InterruptedException { Thread.sleep(1000); } }
```
Keep monitor sections short and avoid blocking I/O inside them.

**Common mistakes:**
- Using virtual threads for CPU speedups.
- Pooling virtual threads.
- Ignoring downstream limits like DB connections.

**Follow-ups:**
- *Should you create one virtual thread per task?* → Yes, that is the common model.
- *Do they remove need for timeouts?* → No.

## Production and Distributed Concurrency

### Q40. What are the four deadlock conditions and a Java fix?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Deadlock theory maps to real bugs.

**Short answer (say this first, 20-40 sec):**
Deadlock requires mutual exclusion, hold-and-wait, no preemption, and circular wait. Break any one condition to prevent it; in code, lock ordering usually breaks circular wait. Avoid holding locks during external calls.

**Detailed answer:**
```java
class Deadlock {
  static final Object A=new Object(), B=new Object();
  public static void main(String[] x){
    new Thread(() -> { synchronized(A){ sleep(); synchronized(B){} } }).start();
    new Thread(() -> { synchronized(B){ sleep(); synchronized(A){} } }).start();
  }
  static void sleep(){
    try{
      Thread.sleep(50);
    }
    catch(InterruptedException e){
      Thread.currentThread().interrupt();
    }
  }
}
```
Fix:
```java
record Account(int id){
}
static void lockBoth(Account a, Account b){
  Account first=a.id()<b.id()?a:b;
  Account second=a.id()<b.id()?b:a;
  synchronized(first){
    synchronized(second){
    }
  }
}
```
```mermaid
flowchart LR
  T1["Thread 1 holds A"] --> W1["waits for B"]
  T2["Thread 2 holds B"] --> W2["waits for A"]
  W1 --> T2
  W2 --> T1
```

**Common mistakes:**
- Locking resources in request-dependent order.
- Holding locks during HTTP calls.
- Ignoring DB deadlocks.

**Follow-ups:**
- *Which condition does ordering break?* → Circular wait.
- *Can databases deadlock?* → Yes, row locks can deadlock too.

### Q41. How do you detect a Java deadlock with `jstack`?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** Production debugging matters.

**Short answer (say this first, 20-40 sec):**
Find the PID and run `jcmd <pid> Thread.print` or `jstack -l <pid>`. Look for `Found one Java-level deadlock`, thread states, `waiting to lock`, and `locked` monitor IDs. Take multiple dumps if it is a hang but no deadlock is reported.

**Detailed answer:**
```text
jcmd <pid> Thread.print
jstack -l <pid>
```
A deadlock report identifies owner thread, waiting thread, monitor ID, and application stack frame.

**Common mistakes:**
- Restarting before capturing evidence.
- Confusing deadlock with pool starvation.
- Not naming threads.

**Follow-ups:**
- *What if no deadlock appears?* → Check blocked I/O, DB locks, infinite loops, and executor queues.
- *How many dumps?* → Often three dumps 10-30 seconds apart.

### Q42. What is livelock?
`🟡 Intermediate` `Stretch`

**Why they ask:** It distinguishes active non-progress from blocking.

**Short answer (say this first, 20-40 sec):**
Livelock means threads keep running and reacting to each other but no useful progress is made. Unlike deadlock, threads are active. Fix with randomized backoff, priority rules, or deterministic ownership.

**Detailed answer:**
```java
static void backoff(){
  try {
    Thread.sleep(java.util.concurrent.ThreadLocalRandom.current().nextInt(1,10));
  }
  catch(InterruptedException e){
    Thread.currentThread().interrupt();
  }
}
```
Common examples are retry loops that collide forever or two polite actors constantly yielding.

**Common mistakes:**
- Calling every hang a deadlock.
- Retrying immediately without jitter.
- Ignoring high CPU with zero throughput.

**Follow-ups:**
- *Where seen in distributed systems?* → Lock retries and conflict resolution loops.
- *Main symptom?* → Activity without progress.

### Q43. What is starvation?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** It explains tail latency and unfairness.

**Short answer (say this first, 20-40 sec):**
Starvation occurs when a thread or task waits indefinitely because other work keeps taking CPU, locks, or executor capacity. The system may still process some requests. Causes include unfair locks, priority misuse, long tasks in small pools, and high-priority traffic.

**Detailed answer:**
```java
ExecutorService p = Executors.newFixedThreadPool(2);
p.submit(() -> blockForever()); p.submit(() -> blockForever()); p.submit(() -> System.out.println("starved"));
```
Separate long blocking work from latency-sensitive work.

**Common mistakes:**
- One shared executor for all task types.
- No queue-time metrics.
- Assuming fairness always improves throughput.

**Follow-ups:**
- *Metric to watch?* → Task queue wait time.
- *Can fair locks help?* → Sometimes, with possible throughput cost.

### Q44. ThreadLocal and leaks in thread pools?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** Request context leaks are common in web apps.

**Short answer (say this first, 20-40 sec):**
`ThreadLocal` stores data per thread. In a pool, worker threads are reused, so values can leak into later requests if not removed. Always clear ThreadLocals in `finally`, and do not depend on them across async thread switches.

**Detailed answer:**
Bug:
```java
static final ThreadLocal<String> USER = new ThreadLocal<>();
void handle(String u){ USER.set(u); process(); } // leak
```
Fix:
```java
void handle(String u){ try{ USER.set(u); process(); } finally { USER.remove(); } }
```

**Common mistakes:**
- Hidden global state.
- Missing cleanup on exceptions.
- Assuming propagation to another executor thread.

**Follow-ups:**
- *Does InheritableThreadLocal solve pools?* → Usually no; it can leak stale values.
- *Virtual threads?* → ThreadLocals work but heavy use increases memory.

### Q45. What is pool starvation deadlock?
`🔴 Advanced` `🔥` `Must-know SDE-2`

**Why they ask:** Futures commonly cause this.

**Short answer (say this first, 20-40 sec):**
Pool starvation deadlock happens when all workers are occupied by tasks waiting for other tasks submitted to the same pool. The dependent tasks never run because no worker is free. Avoid blocking inside the same pool; compose futures or use separate executors.

**Detailed answer:**
```java
ExecutorService p = Executors.newFixedThreadPool(1);
Future<String> outer = p.submit(() -> p.submit(() -> "done").get()); // deadlock
```
Fix by using composition or a different executor for the inner work.

**Common mistakes:**
- `Future.get()` inside same small executor.
- No timeout.
- Blocking in common ForkJoinPool.

**Follow-ups:**
- *How detect?* → Thread dump shows workers waiting on futures while queue has tasks.
- *Does larger pool fix?* → It may mask; design is still fragile.

### Q46. Why does `synchronized` fail with multiple pods?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** Backend services are horizontally scaled.

**Short answer (say this first, 20-40 sec):**
`synchronized` only protects one JVM heap. With multiple pods, each pod has its own lock object, so two pods can enter the same critical section at once. Use DB transactions, unique constraints, idempotency, queue partitioning, or distributed locks for cross-pod coordination.

**Detailed answer:**
```mermaid
sequenceDiagram
  participant C as Client
  participant P1 as Pod1
  participant P2 as Pod2
  participant DB as Database
  C->>P1: POST order key K
  C->>P2: Retry order key K
  P1->>P1: synchronized local lock
  P2->>P2: synchronized local lock
  P1->>DB: Insert order K
  P2->>DB: Insert order K
  DB-->>P2: Unique constraint conflict
```
Final guard: unique key or transactional invariant in the DB.

**Common mistakes:**
- Scaling pods without revisiting locks.
- Redis lock without TTL and ownership token.
- No idempotency for retries.

**Follow-ups:**
- *Best for payments?* → Idempotency key plus transactional storage.
- *Can Redis lock expire early?* → Yes, so still use final constraints.

### Q47. DB lock vs Redis lock vs idempotency?
`🔴 Advanced` `🔥` `Must-know SDE-2`

**Why they ask:** Distributed correctness needs tool selection.

**Short answer (say this first, 20-40 sec):**
DB locks protect rows inside transactions. Redis locks coordinate short cross-node sections but need TTL, unique token, and safe release. Idempotency makes retries safe by returning the same result for the same logical request.

**Detailed answer:**
```mermaid
flowchart TD
  A["Concurrency problem"] --> B{"Persistent data invariant?"}
  B -->|"yes"| C["DB transaction, lock, or constraint"]
  B -->|"no"| D{"Duplicate retry?"}
  D -->|"yes"| E["Idempotency key"]
  D -->|"no"| F["Redis lock or queue partition"]
```
```sql
BEGIN;
SELECT * FROM inventory WHERE sku='A' FOR UPDATE;
UPDATE inventory SET qty = qty - 1 WHERE sku='A' AND qty > 0;
COMMIT;
```

**Common mistakes:**
- Holding DB locks during external API calls.
- Releasing a Redis lock you do not own.
- Treating idempotency as mutual exclusion for all cases.

**Follow-ups:**
- *What is a fencing token?* → Monotonic token used to reject stale lock holders.
- *Strongest final guard?* → DB constraint or conditional update.

### Q48. How do you avoid lost updates in the database?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** Java locking cannot protect rows across pods.

**Short answer (say this first, 20-40 sec):**
Use optimistic locking with a version column, pessimistic row locks like `SELECT FOR UPDATE`, atomic conditional updates, and proper isolation. Pick optimistic for low contention and retryable conflicts; pessimistic for high contention or strict serialization.

**Detailed answer:**
```sql
-- Atomic stock decrement
UPDATE inventory SET qty = qty - 1 WHERE sku = 'A' AND qty > 0;
-- Optimistic update
UPDATE product SET price = 120, version = version + 1 WHERE id = 10 AND version = 3;
```
If affected rows are zero, retry or report a conflict.

**Common mistakes:**
- Read-calculate-write without version predicate.
- Holding row locks while calling external services.
- Assuming `READ COMMITTED` prevents every lost update.

**Follow-ups:**
- *When optimistic?* → Low conflict, user can retry.
- *When pessimistic?* → High conflict or cannot tolerate overwrite.

### Q49. What is an idempotency key?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** Retries are normal in distributed systems.

**Short answer (say this first, 20-40 sec):**
An idempotency key identifies one logical operation. The server stores the key, request hash, status, and response so retries return the same result instead of doing the operation again. It is crucial for payments, order creation, and webhook handling.

**Detailed answer:**
```sql
CREATE TABLE idempotency_keys (idempotency_key text PRIMARY KEY, request_hash text NOT NULL, response_body jsonb, created_at timestamptz DEFAULT now());
```
The key insert and business operation should happen in one transaction where possible.

**Common mistakes:**
- Generating the key only on the server after duplicate work begins.
- Not storing response or request hash.
- Keeping keys for too short a retry window.

**Follow-ups:**
- *Different payload with same key?* → Reject as conflict.
- *Does it replace DB constraints?* → No, it complements them.

### Q50. What is queue partitioning for concurrency?
`🔴 Advanced` `Stretch`

**Why they ask:** Queues can replace locks at scale.

**Short answer (say this first, 20-40 sec):**
Queue partitioning routes messages with the same key to the same partition, preserving order for that key while allowing parallelism across keys. It avoids cross-node locks for workflows like order events. It still needs idempotent consumers because messages may be retried.

**Detailed answer:**
```mermaid
flowchart LR
  A["Events"] --> B{"Hash by orderId"}
  B --> C["Partition 0"]
  B --> D["Partition 1"]
  B --> E["Partition 2"]
  C --> F["Consumer A"]
  D --> G["Consumer B"]
  E --> H["Consumer C"]
```
Ordering is per partition/key, not global.

**Common mistakes:**
- Low-cardinality partition keys.
- Assuming global ordering.
- Non-idempotent consumers.

**Follow-ups:**
- *Good key for order workflow?* → `orderId`.
- *Hot key issue?* → One partition can bottleneck.

### Q51. How investigate high CPU caused by concurrency?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** This is a common production incident.

**Short answer (say this first, 20-40 sec):**
Confirm CPU, capture thread dumps and a CPU profile, map hot OS threads to Java stacks, and look for busy loops, HashMap traversal, CAS spinning, retry storms, or GC pressure. Correlate with deploys, traffic, queue depth, and downstream errors.

**Detailed answer:**
```mermaid
flowchart TD
  A["High CPU alert"] --> B["Capture thread dumps"]
  B --> C["Capture CPU profile"]
  C --> D{"Hot stack in app code?"}
  D -->|"yes"| E["Inspect loops, retries, maps, CAS"]
  D -->|"no"| F["Inspect GC, logging, native calls"]
```
Commands: `jcmd <pid> Thread.print`, `jcmd <pid> JFR.start duration=60s filename=profile.jfr`.

**Common mistakes:**
- Restarting before evidence.
- Looking only at average latency.
- Ignoring retry amplification.

**Follow-ups:**
- *Busy spin symptom?* → RUNNABLE threads on same loop with high CPU.
- *If CPU is GC?* → Inspect allocation and heap leaks.

### Q52. How investigate request timeouts from executor saturation?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** Thread pools often bottleneck APIs.

**Short answer (say this first, 20-40 sec):**
Check active threads, queue depth, rejection count, task duration, and downstream waits. Thread dumps reveal whether workers are blocked on DB, HTTP, locks, or futures. Fix by bounding queues, splitting pools, adding timeouts, and aligning pool sizes with downstream capacity.

**Detailed answer:**
```java
ThreadPoolExecutor e = new ThreadPoolExecutor(10,20,30,TimeUnit.SECONDS,new ArrayBlockingQueue<>(500));
int active = e.getActiveCount(); int queued = e.getQueue().size(); long done = e.getCompletedTaskCount();
```
Queue time is part of request latency.

**Common mistakes:**
- Increasing max threads while DB pool remains small.
- No timeout on downstream calls.
- One shared executor for unrelated dependencies.

**Follow-ups:**
- *What is bulkheading?* → Isolating pools by dependency/workload.
- *Key metrics?* → Active, queued, rejected, completed, task duration.

### Q53. What is false sharing?
`🔴 Advanced` `Stretch`

**Why they ask:** It appears in performance-heavy concurrency.

**Short answer (say this first, 20-40 sec):**
False sharing happens when independent variables updated by different threads sit on the same CPU cache line. Threads invalidate each other's cache line even though they do not logically share data. It hurts performance, not usually correctness.

**Detailed answer:**
```java
class Counters { volatile long a; volatile long b; }
```
Mitigate with library classes like `LongAdder`, better data layout, and JMH measurement before tuning.

**Common mistakes:**
- Calling all contention false sharing.
- Hand-padding without measurement.
- Microbenchmarking without JMH.

**Follow-ups:**
- *Why LongAdder helps?* → It spreads updates across cells.
- *Is it a correctness bug?* → Usually no, performance bug.

### Q54. How can concurrency utilities cause memory leaks?
`🟡 Intermediate` `Stretch`

**Why they ask:** Leaks often come from queues and ThreadLocals.

**Short answer (say this first, 20-40 sec):**
Leaks happen when unbounded queues grow, `Future`s are retained forever, ThreadLocal values are not removed, or scheduled tasks hold references. Symptoms are rising heap, GC pressure, and latency. Use bounded queues, cleanup, cancellation, and metrics.

**Detailed answer:**
Bug:
```java
ExecutorService p = Executors.newFixedThreadPool(4); // unbounded queue internally
for(;;) p.submit(() -> slowWork());
```
Fix with bounded `ThreadPoolExecutor` and rejection policy.

**Common mistakes:**
- Keeping every `Future` in a list.
- Forgetting `ThreadLocal.remove()`.
- No queue size metrics.

**Follow-ups:**
- *How diagnose?* → Heap dump dominator tree and queue metrics.
- *Why fixed pool can leak?* → Its queue is effectively unbounded.

### Q55. How explain concurrency in a Spring Boot project?
`🟢 Beginner` `Stretch`

**Why they ask:** They connect theory to your resume.

**Short answer (say this first, 20-40 sec):**
Spring Boot handles multiple requests concurrently using server threads. Singleton beans are shared, so services should be stateless or protect mutable fields. For an AI email assistant, keep request data in method parameters, set HTTP timeouts, and limit external API concurrency.

**Detailed answer:**
Bug:
```java
@Service class BadEmailService {
  private String lastTone;
  String reply(String email,String tone){
    lastTone=tone;
    return call(email,lastTone);
  }
}
```
Fix:
```java
@Service class EmailService { String reply(String email,String tone){ return call(email,tone); } }
```
Do not store per-request data in singleton fields.

**Common mistakes:**
- Assuming one request at a time.
- Missing timeouts for external APIs.
- Putting request context in static fields.

**Follow-ups:**
- *Are Spring singletons thread-safe?* → Only if your code is thread-safe.
- *Where store request data?* → Parameters, local variables, or request scope.

## Testing Concurrent Code

### Q56. How do you test concurrent code?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** Timing-dependent bugs require special tests.

**Short answer (say this first, 20-40 sec):**
Use deterministic start gates, many iterations, timeouts, and assertions on invariants. Avoid `Thread.sleep` as synchronization. For low-level memory model code use jcstress; for backend code use integration stress with real executors and DB transactions.

**Detailed answer:**
```java
import java.util.concurrent.*; import java.util.concurrent.atomic.*;
class Stress {
  void test() throws Exception {
    AtomicInteger c=new AtomicInteger();
    ExecutorService p=Executors.newFixedThreadPool(8);
    CountDownLatch start=new CountDownLatch(1);
    for(int t=0;t<8;t++) {
      p.submit(() -> {
        start.await();
        for(int i=0;i<10000;i++) c.incrementAndGet();
        return null;
      });
    }
    start.countDown();
    p.shutdown();
    if(!p.awaitTermination(5,TimeUnit.SECONDS)) throw new AssertionError();
    if(c.get()!=80000) throw new AssertionError(c.get());
  }
}
```

**Common mistakes:**
- One lucky test run.
- Sleeping instead of coordinating.
- Testing with mocks when DB isolation is the issue.

**Follow-ups:**
- *What is a start gate?* → A latch that releases workers simultaneously.
- *What should assertions check?* → Business invariants.

### Q57. What is jcstress?
`🔴 Advanced` `Stretch`

**Why they ask:** It is the right tool for JMM-level validation.

**Short answer (say this first, 20-40 sec):**
jcstress is an OpenJDK harness for concurrency stress tests. It runs small actor methods under many interleavings and records observed outcomes. Use it for custom atomics, safe publication, and memory-model assumptions.

**Detailed answer:**
Conceptual shape:
```java
class ReorderingShape { int x,y,r1,r2; void actor1(){ x=1; r1=y; } void actor2(){ y=1; r2=x; } }
```
Actual jcstress uses annotations from `org.openjdk.jcstress.annotations` and outcome declarations.

**Common mistakes:**
- Using JUnit alone to prove JMM behavior.
- Treating one run as proof.
- Writing lock-free code when JDK classes exist.

**Follow-ups:**
- *Use for service tests?* → Usually no; use integration/load tests.
- *Use for library concurrency?* → Yes.

### Q58. How do you review Java concurrency code?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** It tests practical engineering judgment.

**Short answer (say this first, 20-40 sec):**
Identify shared mutable state, verify all access uses the same protection, ensure compound actions are atomic, and inspect executor boundaries. Then check timeouts, interruption, safe publication, DB/distributed scope, tests, and metrics.

**Detailed answer:**
Checklist: shared state, ownership, lock identity, atomic map APIs, bounded queues, named threads, rejection policy, downstream limits, ThreadLocal cleanup, transaction boundaries, idempotency, and invariant tests.

**Common mistakes:**
- Reviewing syntax instead of interleavings.
- Ignoring deployment topology.
- No observability for queues and locks.

**Follow-ups:**
- *First review question?* → What mutable state is shared?
- *Final proof?* → Test or metric for the invariant.

### Q59. How do you choose between lock, atomic, concurrent collection, and DB transaction?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** Correct mechanism selection is more important than memorization.

**Short answer (say this first, 20-40 sec):**
Use atomics for one independent variable, locks for multi-field in-JVM invariants, concurrent collections for shared data structures, and DB transactions or constraints for persistent cross-process invariants. If multiple pods are involved, a JVM lock is insufficient.

**Detailed answer:**
```mermaid
flowchart TD
  A["Shared invariant"] --> B{"Single variable in one JVM?"}
  B -->|"yes"| C["Atomic class"]
  B -->|"no"| D{"Multiple fields in one JVM?"}
  D -->|"yes"| E["Lock or synchronized"]
  D -->|"no"| F{"Shared collection?"}
  F -->|"yes"| G["Concurrent collection atomic API"]
  F -->|"no"| H["DB transaction or distributed design"]
```

**Common mistakes:**
- Using CHM for multi-key transactions.
- Using DB lock for purely local counters.
- Using Redis lock as the only correctness guard.

**Follow-ups:**
- *Best final guard for unique order?* → DB unique constraint.
- *Best for metrics counter?* → `LongAdder`.

### Q60. What production metrics would you add for concurrency?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** Reliable systems need visibility.

**Short answer (say this first, 20-40 sec):**
Expose executor active count, queue depth, completed tasks, rejections, task duration, lock wait time, cache hit/load counts, retry counts, and DB lock wait/deadlock metrics. These reveal saturation before users see outages. Metrics must be tagged by pool/dependency.

**Detailed answer:**
```java
int active = executor.getActiveCount();
int queued = executor.getQueue().size();
long completed = executor.getCompletedTaskCount();
long total = executor.getTaskCount();
```
Also track p95/p99 latency, timeout count, idempotency conflicts, semaphore permits, and downstream concurrency.

**Common mistakes:**
- Only average latency.
- No per-pool names.
- No rejection or queue metrics.

**Follow-ups:**
- *Most important executor metric?* → Queue depth and rejection count.
- *Metric for cache stampede?* → Load count spikes for same key and low hit ratio.

## Revision Cheat Sheet

- Race condition: correctness depends on timing; make the full invariant atomic.
- Avoid shared mutable state first; then use locks, atomics, concurrent collections, or DB transactions.
- `volatile` is visibility and ordering, not `count++` atomicity.
- Instance `synchronized` locks `this`; static `synchronized` locks the class object.
- `ReentrantLock` adds timed/interruptible/fair locking; unlock in `finally`.
- `ReadWriteLock` helps read-heavy workloads; `StampedLock` needs validation and is not reentrant.
- CAS updates only if expected value still matches; ABA needs stamping/versioning.
- `HashMap` is unsafe for concurrent mutation; Java 7 resize could create cyclic chains.
- `ConcurrentHashMap` forbids null; use `Optional` or sentinels.
- Prefer `computeIfAbsent`, `merge`, and `remove(key,value)` over get-then-put.
- `BlockingQueue` gives safe producer-consumer handoff and backpressure.
- `CountDownLatch` one-shot; `CyclicBarrier` reusable; `Semaphore` limits permits.
- `CompletableFuture` composes async work; custom executor for blocking I/O.
- Deadlock needs four conditions; lock ordering breaks circular wait.
- ThreadLocal in pools must be removed in `finally`.
- Java 21 virtual threads scale blocking I/O; avoid pinning by blocking inside monitors.
- JVM locks do not work across pods; use DB constraints, idempotency, queues, or safe distributed locks.
- Test with start gates, repeated runs, invariant assertions, timeouts, and jcstress for JMM code.

## Flashcards

- Q → What does `volatile` not provide? A → Atomic compound updates.
- Q → `synchronized` instance method locks what? A → `this`.
- Q → Static synchronized locks what? A → The `Class` object.
- Q → Why no null in CHM? A → Null must mean absent unambiguously.
- Q → Lazy cache method in CHM? A → `computeIfAbsent`.
- Q → Counter under high contention? A → `LongAdder` for metrics, `AtomicLong` for exact linearizable value.
- Q → Four deadlock conditions? A → Mutual exclusion, hold-and-wait, no preemption, circular wait.
- Q → Start many test threads together? A → `CountDownLatch`.
- Q → Java 21 lightweight threads? A → Virtual threads.
- Q → Cross-pod duplicate protection? A → Idempotency key plus DB constraint.
- Q → Backpressure executor policy? A → `CallerRunsPolicy`.
- Q → Cache stampede fix? A → Atomic in-flight deduplication.
- Q → ThreadLocal pool rule? A → Remove in `finally`.
- Q → Detect deadlock command? A → `jcmd <pid> Thread.print` or `jstack -l`.
- Q → Best final guard for uniqueness? A → Database unique constraint.