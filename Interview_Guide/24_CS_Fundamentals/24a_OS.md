# 24a · CS Fundamentals: Operating Systems

Operating systems questions test whether you understand how Java backend code actually runs: processes, threads, scheduling, synchronization, memory, files, and IPC. Use these answers to connect theory to Spring Boot servers, JVM threads, database clients, Redis clients, and containerized deployments.

**Legend:** 🟢 Beginner · 🟡 Intermediate · 🔴 Advanced · ⚫ FAANG · 🔥 = very frequently asked

## Table of Contents
- [Beginner guide to this file](#beginner-guide-to-this-file)
- [Processes, Threads, and CPU Execution](#processes-threads-and-cpu-execution)
- [Scheduling](#scheduling)
- [Synchronization and Deadlocks](#synchronization-and-deadlocks)
- [Memory Management](#memory-management)
- [IPC, Files, and Java Thread Models](#ipc-files-and-java-thread-models)
- [Revision Cheat Sheet](#revision-cheat-sheet)
- [Flashcards](#flashcards)

## Beginner guide to this file

### What this file teaches
This file teaches the operating-system ideas behind Java backend behavior: how programs become processes,
how threads get CPU time, how memory is translated and protected, how processes communicate, and why file
and network calls may block. Interviewers ask these topics to see whether you can connect textbook concepts
to Spring Boot latency, Java Virtual Machine threads, database clients, containers, and production
debugging.

### Mental model
Think of the operating system as a city traffic controller. Processes are buildings, threads are delivery
vans, CPU cores are roads, memory is mapped property, files are registered storage, and synchronization is
the traffic light system that prevents crashes.

- Processes provide isolation: each program gets its own address space and kernel-managed resources.
- Threads share a process but need synchronization when they touch shared memory.
- Scheduling decides which runnable thread uses a CPU core and when it must yield.
- Memory management translates each process's virtual addresses to physical memory and protects boundaries.
- Inter-process communication gives isolated processes safe channels to exchange data.
- Files, sockets, and system calls are controlled doors from application code into kernel services.

### How to think through any question here
1. Identify the OS layer involved: process, thread, scheduler, synchronization primitive, memory, file, or
   IPC.
2. Ask what resource is scarce or shared: CPU time, lock ownership, frames, file descriptors, buffers, or
   sockets.
3. Separate correctness from performance: isolation and mutual exclusion prevent corruption, while
   scheduling and locality affect latency.
4. For scheduling questions, draw a timeline and compute waiting, turnaround, preemption, and context-switch
   cost.
5. For synchronization questions, name the shared state, the critical section, the ordering rule, and any
   deadlock condition.
6. For memory questions, trace virtual address to page table, Translation Lookaside Buffer, frame, page
   fault, and possible replacement.
7. Finish by connecting the concept to Java: thread pools, synchronized, garbage collection, heap sizing,
   blocking calls, containers, and observability.

### Pattern recognition cues
| If the question mentions... | Think about... | Why it fits |
|---|---|---|
| process versus thread | Isolation versus shared memory | Processes protect address spaces; threads are cheaper but need synchronization. |
| context switch or quantum | Scheduler overhead | The CPU must save one execution context and load another. |
| FCFS, SJF, Round Robin, or priority | Scheduling policy trade-offs | Each policy optimizes fairness, waiting time, responsiveness, or importance differently. |
| starvation or aging | Fairness | Some work may wait forever unless priority or queue rules change over time. |
| mutex, semaphore, or monitor | Critical-section control | The primitive decides who may enter and who waits. |
| deadlock | Circular waiting | The system is stuck because each participant holds something another needs. |
| paging, TLB, or page fault | Virtual memory translation | The OS maps virtual pages to physical frames and handles missing pages. |
| FIFO, LRU, or Optimal | Page replacement | The question is about which page leaves memory when frames are full. |
| fragmentation | Wasted memory shape | Waste can be inside allocated blocks or scattered between them. |
| fork, exec, zombie, or orphan | Process lifecycle | Unix-like systems split creation, replacement, exit, and reaping. |
| green, OS, or virtual threads | Scheduler ownership | The runtime or OS may decide when logical work uses platform threads. |
| Java backend latency | OS resource pressure | Thread count, blocking I/O, memory pressure, file descriptors, and containers affect service behavior. |

### Abbreviations and key terms
| Short form | Expanded form | Plain meaning |
|---|---|---|
| CPU | Central Processing Unit | The processor core time that schedulers assign to runnable work. |
| CS | Computer Science | The broader field that includes operating systems, algorithms, and systems design. |
| DB | Database | Persistent storage often reached through blocking operating-system and network calls. |
| FAANG | Facebook, Amazon, Apple, Netflix, Google | A shorthand for large product-company interview style. |
| FCFS | First-Come, First-Served | A scheduler that runs jobs in arrival order. |
| FD | File Descriptor | A small kernel handle for an open file, socket, or pipe. |
| FIFO | First-In, First-Out | A policy that removes or serves the oldest item first. |
| GC | Garbage Collection | Automatic memory cleanup in the Java Virtual Machine. |
| HTTP | Hypertext Transfer Protocol | The request-response protocol commonly used by backend services. |
| I/O | Input and Output | Work involving files, disks, terminals, networks, databases, or other devices. |
| IPC | Inter-Process Communication | Mechanisms that let isolated processes exchange data. |
| JDBC | Java Database Connectivity | The Java API used by many applications to talk to relational databases. |
| JVM | Java Virtual Machine | The runtime process that executes compiled Java bytecode. |
| KB | Kilobyte | Roughly one thousand bytes of memory or storage. |
| LRU | Least Recently Used | A replacement policy that evicts the page or entry unused for the longest time. |
| OOM | Out Of Memory | A failure when a process or container cannot allocate required memory. |
| OS | Operating System | The software layer that manages processes, scheduling, memory, files, and devices. |
| PCB | Process Control Block | Kernel metadata needed to manage and resume a process. |
| PID | Process Identifier | The operating-system number assigned to a running process. |
| RAM | Random Access Memory | Physical memory used for active code, data, and cached pages. |
| RR | Round Robin | A scheduler that gives each runnable task a time quantum in rotation. |
| SDE-1 | Software Development Engineer level 1 | Entry-level software engineering role. |
| SJF | Shortest Job First | A scheduler that prefers the job with the smallest expected CPU burst. |
| TCP | Transmission Control Protocol | A reliable byte-stream network protocol used by many services. |
| TLB | Translation Lookaside Buffer | A fast cache for virtual-page to physical-frame translations. |
| Critical section | Not an abbreviation | Code that must be protected because it touches shared mutable state. |
| Deadlock | Not an abbreviation | A stuck state where participants wait forever for resources held by each other. |
| Kernel mode | Not an abbreviation | Privileged CPU mode where the operating system can access hardware and memory controls. |
| Page fault | Not an abbreviation | A trap that occurs when a referenced virtual page is missing or not allowed. |
| Scheduler | Not an abbreviation | The OS component that chooses which runnable task gets CPU time. |
| Semaphore | Not an abbreviation | A counter-based synchronization primitive used to control permits. |
| Thrashing | Not an abbreviation | A state where the system spends too much time paging instead of doing useful work. |
| Virtual memory | Not an abbreviation | The private address-space abstraction backed by physical memory and sometimes disk. |

### Common beginner traps
- Saying a thread has no private memory; each thread has its own stack and registers even though heap
  objects are shared.
- Treating every slow Java request as a Java bug while ignoring scheduling, blocking system calls, and
  memory pressure.
- Mixing ready, running, and waiting states when drawing scheduling transitions.
- Confusing starvation with deadlock; starvation may still have system progress while one participant waits
  forever.
- Memorizing paging terms without tracing virtual address to page table, Translation Lookaside Buffer, and
  frame.
- Forgetting that file, socket, and database work enters the kernel and can block or exhaust file
  descriptors.

### How to study this file
1. Start with the short answers and attach each definition to a concrete Java backend example.
2. Redraw every timeline, state diagram, or memory flow by hand until you can explain each transition.
3. For scheduling and page-replacement examples, recompute the numbers without looking, then compare with
   the answer.
4. Use detailed answers to connect theory to thread pools, Java Virtual Machine memory, file descriptors,
   sockets, and containers.
5. Use flashcards for active recall, but always add one symptom you would see in production.
6. Review common mistakes before interviews because OS questions often test precise distinctions.

## Processes, Threads, and CPU Execution

### Q1. What is the difference between a process and a thread?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** It is the base for concurrency, JVM, and web-server questions.

**Beginner lens:** Think of a process as a separate house and threads as workers inside one house. Houses
have their own walls and resources, so processes are isolated; workers in the same house share rooms and
tools, so threads need rules when touching shared heap objects. Interviewers are testing the base
distinction behind Java Virtual Machine processes, request threads, memory safety, and why synchronization
is needed.

**Short answer (say this first, 20-40 sec):**
A process is an independent running program with its own address space and resources. A thread is an execution path inside a process that shares memory with other threads of the same process. Processes are isolated but heavier; threads are lighter but require synchronization.

**Detailed answer:**
| Aspect | Process | Thread |
|---|---|---|
| Memory | Separate address space | Shared process memory |
| Creation | Heavier | Lighter |
| Failure isolation | Better | One bad thread can corrupt process state |
| Communication | IPC | Shared variables, queues |
| Java example | JVM process | request-handling thread |
- Spring Boot usually runs as one JVM process with many threads serving HTTP requests.

**Common mistakes:**
- Saying threads never have private memory; each has its own stack/registers.
- Ignoring synchronization for shared heap objects.
- Assuming threads always improve performance.

**Follow-ups:**
- *Why use processes?* → Isolation, security, separate failures.
- *Why use threads?* → Concurrency with lower overhead and shared memory.

### Q2. What is a Process Control Block (PCB)?
`🟢 Beginner` `Medium` `Must-know SDE-1`

**Why they ask:** It checks OS internals behind context switching.

**Beginner lens:** A Process Control Block is the kernel clipboard for a process. When the operating system
pauses a process, it needs a place to store identity, state, registers, program counter, scheduling data,
memory mappings, and open files so the process can resume later. Recognize this in context-switch questions.
The key mental model is that the PCB describes and manages the process; it is not the application heap
itself.

**Short answer (say this first, 20-40 sec):**
A PCB is the kernel data structure that stores everything needed to manage and resume a process: PID, state, registers, program counter, memory info, open files, scheduling info, and accounting data.

**Detailed answer:**
- Identity: PID, parent PID, user.
- CPU context: registers, program counter, stack pointer.
- Scheduling: priority, state, CPU time.
- Memory: page table pointer, address-space metadata.
- Resources: open file descriptors, signals, IPC handles.

**Common mistakes:**
- Confusing PCB with the process memory itself.
- Forgetting open files and scheduling info.
- Saying PCB is stored in user space; it is maintained by the kernel.

**Follow-ups:**
- *Why PCB needed?* → To pause and later resume a process correctly.
- *Does a thread have similar metadata?* → Yes, thread control block stores thread context.

### Q3. What is a context switch?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** It explains CPU scheduling overhead and latency.

**Beginner lens:** A context switch is like putting a bookmark in one book, picking up another book, and
later returning exactly where you stopped. The operating system saves registers and scheduling state,
chooses another runnable task, and loads its state. It enables multitasking but costs time and cache
locality. Recognize this when questions mention too many threads, Round Robin, blocking Input and Output, or
p99 latency in Java services.

**Short answer (say this first, 20-40 sec):**
A context switch is when the CPU stops running one process or thread and starts another by saving the old execution context and loading the new one. It enables multitasking but costs CPU time and cache/TLB locality.

**Detailed answer:**
- Triggered by timer interrupt, blocking I/O, higher-priority task, syscall, or page fault.
- Kernel saves registers/program counter and updates state.
- Scheduler chooses next runnable task.
- CPU caches may become less useful after switching.
- In Java servers, too many runnable threads can increase context-switch overhead.

**Common mistakes:**
- Calling every function call a context switch.
- Ignoring cache effects.
- Assuming more threads always means more throughput.

**Follow-ups:**
- *Thread vs process context switch?* → Thread switch within same process is usually cheaper because address space may remain same.
- *How reduce context switches?* → Use sensible thread pools, async I/O where suitable, and avoid blocking unnecessarily.

### Q4. What are user mode and kernel mode?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** It tests OS protection fundamentals.

**Beginner lens:** User mode is the public area where applications run with limited privileges; kernel mode
is the control room where the operating system can touch hardware, memory maps, files, and devices.
Applications must use controlled doors such as system calls or interrupts to enter. The interviewer is
testing protection: a buggy Java service should not be able to overwrite arbitrary memory or crash the whole
machine directly.

**Short answer (say this first, 20-40 sec):**
User mode runs application code with restricted privileges. Kernel mode runs OS code with full hardware access. Switching to kernel mode happens through controlled mechanisms like system calls, interrupts, and exceptions.

**Detailed answer:**
- User mode cannot directly access arbitrary memory or devices.
- Kernel mode can manage memory, scheduling, files, networking, and devices.
- Protection prevents a buggy app from crashing the entire OS.
- Java file or socket operations eventually invoke kernel services through the JVM/native libraries.

**Common mistakes:**
- Saying all Java code runs in kernel mode.
- Confusing kernel mode with admin/root user.
- Ignoring the cost of frequent user-kernel transitions.

**Follow-ups:**
- *What is a trap?* → Controlled switch to kernel mode due to syscall/exception.
- *Why not run everything in kernel mode?* → Security and stability.

### Q5. What is a system call?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Backend code constantly uses OS services.

**Beginner lens:** A system call is asking the operating-system service counter to do privileged work. Java
file reads, socket writes, process creation, and memory mapping eventually go through native code into the
kernel. Recognize this when backend code blocks on files, network, databases, or ProcessBuilder. The
practical concern is not that every syscall is bad, but that frequent, blocking, or unbatched calls can add
latency and failure modes.

**Short answer (say this first, 20-40 sec):**
A system call is a controlled request from a user program to the kernel for privileged operations like file I/O, networking, process creation, or memory mapping.

**Detailed answer:**
- Examples: `open`, `read`, `write`, `socket`, `connect`, `fork`, `exec`, `mmap`.
- Java wrappers: `Files.readString`, JDBC socket I/O, HTTP clients, `ProcessBuilder`.
- Syscalls involve user-kernel transition, so batching I/O can improve performance.

**Common mistakes:**
- Thinking library calls and syscalls are identical; libraries may call syscalls internally.
- Ignoring failure codes and partial reads/writes.
- Assuming all syscalls are slow equally.

**Follow-ups:**
- *Why are syscalls controlled?* → To protect hardware and OS resources.
- *What is blocking syscall?* → A syscall that sleeps until I/O/resource is ready.

### Q6. What are process states?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** It checks scheduling basics.

**Beginner lens:** Process states are queue labels in the operating-system traffic office. New means being
created, ready means eligible for CPU, running means currently executing, waiting or blocked means sleeping
for an event, and terminated means finished. The key distinction is ready versus waiting: ready work lacks
CPU time; waiting work lacks an event such as Input and Output completion or lock availability. Thread dumps
use similar reasoning.

**Short answer (say this first, 20-40 sec):**
Typical process states are new, ready, running, waiting or blocked, and terminated. The scheduler moves processes between ready and running; I/O moves running processes to waiting; completion moves them to terminated.

**Detailed answer:**
```mermaid
stateDiagram-v2
    [*] --> New
    New --> Ready
    Ready --> Running
    Running --> Ready: preempted
    Running --> Waiting: I/O or event wait
    Waiting --> Ready: event complete
    Running --> Terminated
    Terminated --> [*]
```
- Ready: can run but waiting for CPU.
- Waiting: cannot run until event/I/O completes.
- Running: currently on CPU.

**Common mistakes:**
- Confusing ready with waiting.
- Forgetting preemption from running to ready.
- Assuming a waiting process consumes CPU.

**Follow-ups:**
- *What creates ready queue?* → Processes ready for CPU scheduling.
- *What causes blocked to ready?* → I/O completion, signal, lock availability.

## Scheduling

### Q7. Explain FCFS, SJF, Round Robin, and Priority scheduling.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Scheduling appears in OS rounds and aptitude-style examples.

**Beginner lens:** Treat scheduling algorithms as different rules for choosing the next customer.
First-Come, First-Served is simple but can trap short jobs behind long ones; Shortest Job First reduces
average wait when bursts are known; Round Robin rotates time slices for responsiveness; Priority favors
important work but risks starvation. The interviewer is testing trade-offs, not memorized definitions, so
always mention fairness, response time, and context-switch overhead.

**Short answer (say this first, 20-40 sec):**
FCFS runs jobs in arrival order, SJF runs the shortest burst first, Round Robin gives each process a time quantum, and Priority scheduling runs higher-priority processes first. Each optimizes different trade-offs like fairness, waiting time, and response time.

**Detailed answer:**
| Algorithm | Idea | Strength | Weakness |
|---|---|---|---|
| FCFS | arrival order | simple | convoy effect |
| SJF | shortest CPU burst | minimum average waiting if known | starvation, needs prediction |
| RR | time quantum rotation | fair, interactive | too-small quantum causes overhead |
| Priority | highest priority first | important work first | starvation |
- Backend analogy: thread pools schedule tasks; long blocking tasks can delay short requests.

**Common mistakes:**
- Saying SJF is always practical; exact future burst is usually unknown.
- Ignoring context-switch overhead in Round Robin.
- Ignoring starvation in priority scheduling.

**Follow-ups:**
- *What is preemptive SJF called?* → Shortest Remaining Time First.
- *What is convoy effect?* → Short jobs wait behind a long job in FCFS.

### Q8. Work a scheduling Gantt example.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** They test if you can compute, not just define.

**Beginner lens:** A Gantt example is just a timeline of who holds the CPU and when. Draw start and finish
times first, then compute waiting time as time spent ready but not running, and turnaround as completion
minus arrival. The common beginner error is doing arithmetic before drawing the order. Recognize whether the
algorithm is preemptive; preemption can split a process into multiple timeline segments.

**Short answer (say this first, 20-40 sec):**
For processes P1=5, P2=3, P3=1 all arriving at time 0, FCFS order P1-P2-P3 gives waiting times 0, 5, 8 and average 4.33. SJF order P3-P2-P1 gives waiting times 0, 1, 4 and average 1.67.

**Detailed answer:**
| Algorithm | Timeline | Waiting times | Average waiting |
|---|---|---|---|
| FCFS | `0 P1 5 P2 8 P3 9` | P1=0, P2=5, P3=8 | 4.33 |
| SJF | `0 P3 1 P2 4 P1 9` | P3=0, P2=1, P1=4 | 1.67 |
- Waiting time = start time - arrival time for non-preemptive cases.
- Turnaround time = completion time - arrival time.

**Common mistakes:**
- Mixing waiting time and turnaround time.
- Forgetting arrival times when not all are zero.
- Ignoring preemption in preemptive algorithms.

**Follow-ups:**
- *Why SJF wins here?* → It reduces time short jobs spend waiting.
- *What if P1 arrives earlier?* → With non-preemptive SJF, already-running process continues.

### Q9. What are starvation and aging?
`🟡 Intermediate` `Medium` `Must-know SDE-1`

**Why they ask:** It checks fairness in scheduling and locks.

**Beginner lens:** Starvation is a priority line where one customer keeps getting skipped forever even
though the shop is open. Aging fixes this by gradually boosting waiting work, like moving a long-waiting
customer closer to the front. Recognize starvation in priority scheduling, reader-heavy locks, and unfair
queues. The interviewer is testing fairness and bounded waiting, plus the distinction from deadlock where a
cycle prevents progress.

**Short answer (say this first, 20-40 sec):**
Starvation occurs when a process waits indefinitely because others keep getting resources first. Aging prevents starvation by gradually increasing the priority of waiting processes.

**Detailed answer:**
- Can happen in priority scheduling, SJF, reader-writer locks, and unfair queues.
- Aging example: increase priority by 1 every second spent waiting.
- In backend systems, starvation can appear when high-priority queues never allow low-priority jobs to run.

**Common mistakes:**
- Confusing starvation with deadlock.
- Ignoring fairness in locks and thread pools.
- Making aging too aggressive and destroying priority semantics.

**Follow-ups:**
- *Deadlock vs starvation?* → Deadlock is circular waiting; starvation is indefinite postponement.
- *How else prevent starvation?* → Fair queues, quotas, bounded priority boosts.

### Q10. How does Round Robin quantum affect performance?
`🟡 Intermediate` `Medium` `Must-know SDE-1`

**Why they ask:** It tests trade-off thinking.

**Beginner lens:** The quantum is a timer slice. If it is tiny, the system feels responsive but wastes time
switching books too often; if it is huge, Round Robin behaves like First-Come, First-Served and interactive
tasks wait. Recognize this as a trade-off between response time and overhead. A strong answer also mentions
cache disruption and workload type, because batch and interactive systems prefer different balances.

**Short answer (say this first, 20-40 sec):**
A small quantum improves responsiveness but increases context-switch overhead. A large quantum reduces overhead but behaves like FCFS and may hurt interactive response time.

**Detailed answer:**
- Quantum should be large compared with context-switch time.
- Interactive systems favor responsiveness.
- Batch systems may use larger quanta.
- If quantum is 10 ms and context switch is 1 ms, overhead can be significant under load.

**Common mistakes:**
- Saying smaller quantum is always better.
- Ignoring CPU cache effects.
- Not considering workload type.

**Follow-ups:**
- *What happens when quantum approaches infinity?* → Round Robin becomes similar to FCFS.
- *What happens with quantum near zero?* → Excessive context switching.

## Synchronization and Deadlocks

### Q11. What is a critical section?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** It is the foundation of locks.

**Beginner lens:** A critical section is the one-person-at-a-time part of code, like a cash drawer that
multiple clerks must not update simultaneously. The important question is not where the synchronized keyword
appears, but which shared mutable state and invariant it protects. Recognize this whenever counters, queues,
maps, buffers, or file metadata are shared. Correctness needs mutual exclusion, progress, and bounded
waiting, not just a lock somewhere nearby.

**Short answer (say this first, 20-40 sec):**
A critical section is code that accesses shared mutable state and must not be executed by multiple threads at the same time unless the state is protected. Correct solutions require mutual exclusion, progress, and bounded waiting.

**Detailed answer:**
- Example: incrementing a shared counter is not atomic.
- Protection tools: mutex, semaphore, monitor, atomic variables, message passing.
- In Java, `synchronized`, `ReentrantLock`, `AtomicInteger`, and concurrent collections are common.

**Common mistakes:**
- Synchronizing too little and leaving races.
- Synchronizing too much and killing concurrency.
- Assuming `volatile` makes compound operations atomic.

**Follow-ups:**
- *What is race condition?* → Result depends on unpredictable interleaving.
- *What is mutual exclusion?* → Only one thread enters the protected section at a time.

### Q12. Mutex vs semaphore?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** This is a frequent OS synchronization question.

**Beginner lens:** A mutex is one key for one room; a semaphore is a bucket of permits for N identical
resources. Use a mutex when ownership and exclusive access matter, and a semaphore when you want to limit
how many threads may enter or consume resources. Recognize this in connection pools, rate limits, and
producer-consumer problems. Always release in finally, and do not use a semaphore when lock ownership
semantics are required.

**Short answer (say this first, 20-40 sec):**
A mutex allows one owner to enter a critical section. A semaphore is a counter that allows up to N permits and can coordinate access to a pool of resources. Binary semaphores resemble mutexes but ownership semantics differ.

**Detailed answer:**
| Feature | Mutex | Semaphore |
|---|---|---|
| Count | 1 | 0 to N |
| Ownership | Usually owner releases | Any thread may signal in many models |
| Use | Protect critical section | Limit resource count, producer-consumer |
- Java: `synchronized`/`ReentrantLock` for mutex; `Semaphore` for permits.

**Common mistakes:**
- Using semaphore when lock ownership matters.
- Forgetting to release in `finally`.
- Deadlocking by acquiring multiple locks inconsistently.

**Follow-ups:**
- *What is binary semaphore?* → Semaphore with one permit.
- *What is counting semaphore?* → Semaphore with multiple permits.

### Q13. What is a monitor?
`🟡 Intermediate` `Medium` `Must-know SDE-1`

**Why they ask:** Java's `synchronized` is monitor-based.

**Beginner lens:** A monitor combines a lock with a waiting area for conditions. In Java, synchronized gives
the mutual-exclusion part, while wait, notify, and notifyAll let threads sleep until a condition changes.
The mental model is a guarded room with a bench outside: only the lock holder checks or changes the
condition. Recognize monitor questions when bounded buffers, producer-consumer, or spurious wakeups appear.

**Short answer (say this first, 20-40 sec):**
A monitor combines mutual exclusion with condition synchronization. In Java, every object can act as a monitor using `synchronized`, `wait`, `notify`, and `notifyAll`.

**Detailed answer:**
```java
class BoundedBuffer<T> {
    private final Queue<T> q = new ArrayDeque<>();
    private final int capacity = 10;
    synchronized void put(T item) throws InterruptedException {
        while (q.size() == capacity) wait();
        q.add(item);
        notifyAll();
    }
    synchronized T take() throws InterruptedException {
        while (q.isEmpty()) wait();
        T item = q.remove();
        notifyAll();
        return item;
    }
}
```
- Use `while`, not `if`, around waits due to spurious wakeups.

**Common mistakes:**
- Calling `wait` without holding the monitor.
- Using `if` instead of `while`.
- Calling `notify` when multiple condition types exist.

**Follow-ups:**
- *Why `notifyAll`?* → Safer when multiple conditions may be waiting.
- *What modern Java class helps?* → `BlockingQueue`.

### Q14. Explain producer-consumer.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** It checks practical synchronization patterns.

**Beginner lens:** Producer-consumer is a conveyor belt between workers who create items and workers who
process them. The buffer must handle empty and full cases safely, so producers wait or slow down when full
and consumers wait when empty. This tests synchronization and backpressure together. In Java, BlockingQueue
usually expresses the pattern more safely than manual wait and notify, especially with bounded capacity.

**Short answer (say this first, 20-40 sec):**
Producer-consumer has producers adding items to a bounded buffer and consumers removing them. Synchronization must prevent producers from overflowing the buffer and consumers from reading empty buffer.

**Detailed answer:**
- Semaphores: `empty` initialized to capacity, `full` to 0, mutex for queue access.
- Java production code: prefer `ArrayBlockingQueue` or `LinkedBlockingQueue`.
```java
BlockingQueue<Job> q = new ArrayBlockingQueue<>(100);
q.put(job);      // producer blocks if full
Job job = q.take(); // consumer blocks if empty
```

**Common mistakes:**
- Busy waiting instead of blocking.
- Not bounding the queue, causing memory pressure.
- Forgetting interruption handling.

**Follow-ups:**
- *Why bounded queue?* → Backpressure.
- *Where used in backend?* → Async jobs, logging, event processing.

### Q15. Explain readers-writers and dining philosophers.
`🟡 Intermediate` `Medium` `Must-know SDE-1`

**Why they ask:** Classic problems reveal concurrency design trade-offs.

**Beginner lens:** Readers-writers is a library rule: many people may read together, but writing needs
exclusive access. Dining philosophers is a shared-forks story that exposes deadlock, ordering, and
starvation. Both are pattern questions about resource coordination. The interviewer wants you to name the
trade-off: more concurrency for reads or independent thinkers, while still guaranteeing progress through
ordering, fairness, try-locks, or limiting contenders.

**Short answer (say this first, 20-40 sec):**
Readers-writers allows many concurrent readers but exclusive writers. Dining philosophers models deadlock risk when multiple actors acquire multiple resources. Both show fairness and deadlock issues.

**Detailed answer:**
- Readers-writers risk: writer starvation if readers keep arriving.
- Dining philosophers deadlock: each philosopher holds one fork and waits for the next.
- Fixes: resource ordering, waiter/arbitrator, try-lock with backoff, allowing at most N-1 philosophers to compete.
```mermaid
flowchart LR
    P1["Thread A holds lock 1"] --> P2["Thread A waits lock 2"]
    P3["Thread B holds lock 2"] --> P4["Thread B waits lock 1"]
    P2 --> DEAD["Circular wait deadlock"]
    P4 --> DEAD
```

**Common mistakes:**
- Ignoring starvation while preventing deadlock.
- Taking locks in inconsistent order.
- Thinking read locks always improve performance.

**Follow-ups:**
- *How Java supports read/write locks?* → `ReentrantReadWriteLock` and `StampedLock`.
- *Simplest dining philosopher fix?* → Total order resources and acquire in order.

### Q16. What are the four necessary conditions for deadlock?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Deadlock prevention depends on these conditions.

**Beginner lens:** Deadlock is four cars each holding one lane of an intersection and waiting for another
lane. The four conditions are mutual exclusion, hold-and-wait, no preemption, and circular wait. If all hold
at once, the system can get stuck. Recognize this in multiple locks, resource allocation, database locks,
and file handles. To prevent deadlock, break at least one condition, often circular wait through global
ordering.

**Short answer (say this first, 20-40 sec):**
Deadlock requires mutual exclusion, hold and wait, no preemption, and circular wait. Break any one condition to prevent deadlock.

**Detailed answer:**
- Mutual exclusion: resource cannot be shared.
- Hold and wait: process holds one resource while waiting for another.
- No preemption: resource cannot be forcibly taken.
- Circular wait: chain of processes each waiting for the next.

**Common mistakes:**
- Listing starvation as a deadlock condition.
- Forgetting no preemption.
- Saying all four are sufficient in every informal scenario; they are necessary for deadlock.

**Follow-ups:**
- *Most practical prevention?* → Lock ordering breaks circular wait.
- *Can databases deadlock?* → Yes, transactions can wait on locks cyclically.

### Q17. Compare deadlock prevention, avoidance, detection, and recovery.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** It tests a complete view of deadlock handling.

**Beginner lens:** Think of four safety strategies. Prevention changes road rules so the crash pattern
cannot occur; avoidance checks each allocation before entering a risky state; detection lets traffic jam and
then finds it; recovery breaks the jam by aborting or preempting work. The interviewer is testing when each
is practical. Real backend systems often combine timeouts, lock ordering, monitoring, and transaction
aborts.

**Short answer (say this first, 20-40 sec):**
Prevention structurally breaks deadlock conditions, avoidance grants resources only if the system remains safe, detection finds deadlocks after they occur, and recovery kills/rolls back/preempts work.

**Detailed answer:**
| Strategy | Idea | Example |
|---|---|---|
| Prevention | break one condition | global lock ordering |
| Avoidance | safe-state check | Banker's algorithm |
| Detection | wait-for graph cycle | DB deadlock detector |
| Recovery | resolve deadlock | abort transaction |

**Common mistakes:**
- Calling timeout prevention; it is detection/recovery-ish.
- Using avoidance without knowing max resource needs.
- Ignoring rollback cost.

**Follow-ups:**
- *What do databases often use?* → Detection plus victim rollback.
- *Why not always use Banker's?* → It needs known maximum resource demands.

### Q18. Work a Banker's algorithm example.
`🔴 Advanced` `Medium` `Stretch`

**Why they ask:** It checks resource-allocation reasoning.

**Beginner lens:** Banker's algorithm is a cautious loan officer. Before granting more resources, it asks
whether there is still some order in which every process could finish and return what it holds. The mental
model is safe sequence, not current completion. Recognize this when maximum demand, allocation, need, and
available resources are given. The common mistake is checking only the next request instead of proving a
full safe path.

**Short answer (say this first, 20-40 sec):**
Banker's algorithm checks whether granting a request leaves at least one safe sequence where every process can finish. If no safe sequence exists, the request is delayed.

**Detailed answer:**
Available `A B C = 3 3 2`

| P | Allocation | Max | Need |
|---|---|---|---|
| P1 | 0 1 0 | 7 5 3 | 7 4 3 |
| P2 | 2 0 0 | 3 2 2 | 1 2 2 |
| P3 | 3 0 2 | 9 0 2 | 6 0 0 |
| P4 | 2 1 1 | 2 2 2 | 0 1 1 |
| P5 | 0 0 2 | 4 3 3 | 4 3 1 |

Safe sequence exists:
1. P2 need `1 2 2` ≤ available `3 3 2`; finish, available becomes `5 3 2`.
2. P4 need `0 1 1`; finish, available `7 4 3`.
3. P5 need `4 3 1`; finish, available `7 4 5`.
4. P1 need `7 4 3`; finish.
5. P3 finish.

**Common mistakes:**
- Comparing Max to Available instead of Need.
- Forgetting to add released Allocation after finish.
- Thinking safe means no waiting; safe means eventual completion is possible.

**Follow-ups:**
- *What is unsafe state?* → Not necessarily deadlocked, but deadlock may occur.
- *Where used today?* → Mostly teaching; real systems often use simpler policies.

## Memory Management

### Q19. What is paging?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Paging underlies virtual memory.

**Beginner lens:** Paging is cutting every process address space into equal-size pages and physical memory
into matching frames, like book pages placed on any shelf slot. The page table tells where each virtual page
lives. This removes external fragmentation and supports isolation, but adds translation overhead and
possible internal waste in the last page. Recognize paging questions when virtual addresses, page tables,
frames, offsets, or page faults appear.

**Short answer (say this first, 20-40 sec):**
Paging divides virtual memory into fixed-size pages and physical memory into frames. The page table maps virtual pages to physical frames, allowing non-contiguous allocation and process isolation.

**Detailed answer:**
- Virtual address = page number + offset.
- Physical address = frame number + offset.
- Eliminates external fragmentation but can cause internal fragmentation in the last page.
- Page tables are per process and protected by the OS.

**Common mistakes:**
- Saying pages and frames are different sizes; they are same size.
- Confusing paging with swapping.
- Ignoring page-table memory overhead.

**Follow-ups:**
- *Why fixed size?* → Simpler allocation and mapping.
- *What is page size?* → Commonly 4 KB, but systems also support huge pages.

### Q20. What is segmentation?
`🟢 Beginner` `Medium` `Must-know SDE-1`

**Why they ask:** It contrasts logical memory division with paging.

**Beginner lens:** Segmentation divides memory by logical chapters such as code, stack, heap, and data, with
each segment having a base and limit. Unlike paging, segments are variable-sized, so they match how
programmers think but can leave scattered holes in memory. Recognize this when questions contrast logical
organization with fixed-size allocation. The key distinction is external fragmentation: enough total free
memory may exist, but not as one contiguous segment.

**Short answer (say this first, 20-40 sec):**
Segmentation divides memory into variable-sized logical segments such as code, stack, heap, and data. It matches programmer view but can suffer external fragmentation.

**Detailed answer:**
- Address = segment number + offset.
- Segment table stores base and limit.
- Easy to protect/share logical units.
- Modern systems often combine paging concepts with segmentation-like protection abstractions.

**Common mistakes:**
- Saying segmentation uses fixed-size blocks.
- Forgetting external fragmentation.
- Confusing segment fault cause with only segmentation; invalid memory access can fault in paged systems too.

**Follow-ups:**
- *Paging vs segmentation?* → Paging is fixed-size physical management; segmentation is variable-size logical management.
- *Which has external fragmentation?* → Segmentation.

### Q21. What is virtual memory?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** It explains why programs can use more memory than physical RAM.

**Beginner lens:** Virtual memory gives each process its own private street map, even though all maps
eventually point to shared physical Random Access Memory or disk-backed pages. This makes isolation and
simpler programming possible, and lets the operating system load pages on demand. Recognize it when programs
appear to use more memory than installed RAM. The production connection is memory pressure: too large a
working set causes page faults, thrashing, or Out Of Memory failures.

**Short answer (say this first, 20-40 sec):**
Virtual memory gives each process a large private address space and maps only needed pages to physical memory. It provides isolation, simpler programming, demand paging, and the ability to use disk as backing store.

**Detailed answer:**
- Each process sees addresses starting from its own virtual space.
- OS maps virtual pages to RAM frames or disk.
- Enables memory protection between processes.
- Too much working set can cause thrashing.

**Common mistakes:**
- Saying virtual memory is only swap space.
- Ignoring process isolation benefits.
- Assuming virtual memory has no performance cost.

**Follow-ups:**
- *What is demand paging?* → Load pages only when referenced.
- *What is working set?* → Pages actively used by a process over a time window.

### Q22. What happens during a page fault?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** It connects memory and OS exception handling.

**Beginner lens:** A page fault is a missing-page interrupt, like asking for a book page that is not
currently on the desk. The Central Processing Unit traps to the kernel, which checks whether the access is
valid, loads or maps the page if possible, updates tables and the Translation Lookaside Buffer, then
restarts the instruction. Recognize that not every page fault is fatal; major faults are costly because they
need disk Input and Output.

**Short answer (say this first, 20-40 sec):**
A page fault occurs when a referenced virtual page is not currently mapped in physical memory or violates permissions. The CPU traps to the OS, which validates access, loads the page if valid, updates page tables, and resumes the instruction.

**Detailed answer:**
```mermaid
flowchart TD
    A["Process accesses virtual address"] --> B{"Page present and allowed?"}
    B -->|"yes"| C["Translate and continue"]
    B -->|"no"| D["Trap to kernel page fault handler"]
    D --> E{"Valid access?"}
    E -->|"no"| F["Terminate or signal fault"]
    E -->|"yes"| G["Load page or choose victim frame"]
    G --> H["Update page table and TLB"]
    H --> I["Restart instruction"]
```

**Common mistakes:**
- Treating every page fault as an error.
- Forgetting permission faults.
- Ignoring disk I/O cost for major page faults.

**Follow-ups:**
- *Minor vs major page fault?* → Minor does not need disk I/O; major does.
- *Why restart instruction?* → The original memory access must complete after mapping is fixed.

### Q23. What is a TLB?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** It explains address-translation performance.

**Beginner lens:** The Translation Lookaside Buffer is a tiny cheat sheet for address translation. Instead
of walking page tables for every memory access, the processor remembers recent virtual-page to
physical-frame mappings. A hit is fast; a miss requires more work. Recognize Translation Lookaside Buffer
questions when context switches, huge pages, page-table walks, or memory locality appear. The practical idea
is that memory performance depends on translation locality, not only data-cache locality.

**Short answer (say this first, 20-40 sec):**
The Translation Lookaside Buffer is a small fast cache of recent virtual-page to physical-frame translations. It avoids expensive page-table walks on every memory access.

**Detailed answer:**
- TLB hit: translation found quickly.
- TLB miss: hardware/OS walks page table and fills TLB.
- Context switches may flush or tag TLB entries.
- Large working sets can reduce TLB locality.

**Common mistakes:**
- Calling TLB a data cache; it caches translations.
- Ignoring context-switch impact.
- Assuming page table lookup is free.

**Follow-ups:**
- *What are huge pages good for?* → Fewer TLB entries needed for large memory regions.
- *What is TLB reach?* → Amount of memory covered by TLB entries.

### Q24. What is thrashing?
`🟡 Intermediate` `Medium` `Must-know SDE-1`

**Why they ask:** It connects memory pressure to system performance.

**Beginner lens:** Thrashing is a worker spending all day moving boxes in and out of storage instead of
doing the real job. The active pages of running processes exceed physical memory, so the operating system
keeps swapping and faulting. Symptoms can be high disk Input and Output, low useful CPU work, and terrible
latency. Recognize this when adding concurrency makes a memory-pressure problem worse rather than faster.

**Short answer (say this first, 20-40 sec):**
Thrashing happens when the system spends more time swapping pages in and out than doing useful work because active working sets exceed physical memory.

**Detailed answer:**
- Symptoms: high disk I/O, low CPU utilization, slow response.
- Causes: too many processes, memory leaks, oversized heaps, poor locality.
- Fixes: reduce degree of multiprogramming, add memory, tune heap, optimize data structures, improve locality.
- Java example: oversized JVM heaps or many services on one host can pressure memory.

**Common mistakes:**
- Blaming CPU when disk paging is the bottleneck.
- Increasing concurrency under memory pressure.
- Ignoring memory leaks.

**Follow-ups:**
- *What metric hints thrashing?* → High page fault rate and swap I/O.
- *How can containers worsen this?* → Memory limits can cause OOM or heavy reclaim if not sized correctly.

### Q25. Work a page replacement example for FIFO, LRU, and Optimal.
`🔴 Advanced` `🔥` `Stretch`

**Why they ask:** It tests algorithmic precision.

**Beginner lens:** Page replacement is choosing which book page leaves the small desk when a new one must
come in. First-In, First-Out evicts the oldest loaded page, Least Recently Used evicts the page unused
longest, and Optimal evicts the page needed farthest in the future. For examples, maintain the frame
contents after each reference. The interviewer tests careful simulation and awareness that Optimal is a
benchmark, not an implementable online policy.

**Short answer (say this first, 20-40 sec):**
For reference string `1 2 3 4 1 2 5 1 2 3 4 5` with 3 frames, FIFO has 9 faults, LRU has 10 faults, and Optimal has 7 faults. Optimal is theoretical because it needs future knowledge.

**Detailed answer:**
| Algorithm | Fault count | Key idea |
|---|---:|---|
| FIFO | 9 | evict oldest loaded page |
| LRU | 10 | evict least recently used page |
| Optimal | 7 | evict page used farthest in future |
- FIFO can suffer Belady's anomaly.
- LRU approximations are used in real systems.

**Common mistakes:**
- Updating LRU order on load but not on hit.
- Believing FIFO always improves with more frames.
- Treating Optimal as implementable in production.

**Follow-ups:**
- *What is Belady's anomaly?* → More frames can cause more FIFO faults for some strings.
- *Why use LRU?* → It exploits temporal locality without future knowledge.

### Q26. Internal vs external fragmentation?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** It is a standard memory-management concept.

**Beginner lens:** Fragmentation is wasted memory shape. Internal fragmentation is unused space inside an
allocated block, like renting a whole locker for a half locker of items. External fragmentation is free
space scattered in small holes, like many tiny empty lockers when you need one large room. Recognize paging
for internal waste and variable-size allocation or segmentation for external holes. Fixes trade memory waste
against allocation and translation overhead.

**Short answer (say this first, 20-40 sec):**
Internal fragmentation is wasted space inside allocated blocks. External fragmentation is free memory split into small non-contiguous holes, making large allocation difficult despite enough total free memory.

**Detailed answer:**
- Paging can have internal fragmentation in the last page.
- Segmentation and variable-size allocation can have external fragmentation.
- Compaction can reduce external fragmentation but is expensive.
- Buddy allocators and slab allocators manage fragmentation trade-offs.

**Common mistakes:**
- Mixing internal and external definitions.
- Saying paging causes external fragmentation.
- Ignoring allocation metadata overhead.

**Follow-ups:**
- *How fix external fragmentation?* → Compaction or fixed-size paging.
- *How reduce internal fragmentation?* → Smaller block size, but with more metadata/translation overhead.

## IPC, Files, and Java Thread Models

### Q27. What are common IPC mechanisms?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Processes need communication despite isolation.

**Beginner lens:** Inter-Process Communication is how separate houses pass messages without sharing all
rooms. Pipes, sockets, shared memory, message queues, signals, and files are different channels with
different cost and synchronization needs. Recognize this when processes are isolated but must coordinate.
The backend connection is microservices, local sidecars, database clients, and Redis clients: communication
requires serialization, buffering, permissions, and failure handling.

**Short answer (say this first, 20-40 sec):**
Common IPC mechanisms are pipes, message queues, shared memory, signals, sockets, and memory-mapped files. Choice depends on speed, structure, host boundary, and synchronization needs.

**Detailed answer:**
| IPC | Best for |
|---|---|
| Pipe | parent-child byte stream |
| Message queue | structured async messages |
| Shared memory | fastest same-machine sharing |
| Socket | local or network communication |
| Signal | small notifications |
- Shared memory needs synchronization because both processes can access same region.

**Common mistakes:**
- Using shared memory without locks.
- Thinking sockets are only remote; Unix domain sockets are local.
- Using signals for large data.

**Follow-ups:**
- *What IPC does HTTP use?* → TCP sockets.
- *Fastest IPC?* → Often shared memory, but complexity increases.

### Q28. What are `fork` and `exec`?
`🟡 Intermediate` `Medium` `Must-know SDE-1`

**Why they ask:** It is classic Unix process creation.

**Beginner lens:** fork is photocopying the current process; exec is replacing the copied process image with
a new program. Unix-like systems combine them so a parent can create a child, then the child loads another
executable. Recognize this when shells, ProcessBuilder, or worker processes appear. The key distinction is
lifecycle: fork preserves much of the current context, while exec keeps the process identity but changes the
running program.

**Short answer (say this first, 20-40 sec):**
`fork` creates a new child process as a copy of the parent. `exec` replaces the current process image with a new program. Shells often fork, then the child execs the requested command.

**Detailed answer:**
- Modern OS uses copy-on-write so fork does not immediately copy all memory.
- Parent and child get different return values from `fork`.
- `exec` keeps PID but replaces code/data/stack with new program.
- Java `ProcessBuilder` eventually relies on OS process creation primitives.

**Common mistakes:**
- Saying exec creates a new process; it replaces current image.
- Ignoring copy-on-write optimization.
- Forgetting file descriptors may be inherited.

**Follow-ups:**
- *Why fork then exec?* → Lets child set up file descriptors/env before running new program.
- *What is copy-on-write?* → Pages are copied only when modified.

### Q29. Zombie vs orphan process?
`🟢 Beginner` `Medium` `Must-know SDE-1`

**Why they ask:** It checks process lifecycle details.

**Beginner lens:** A zombie is a finished child whose exit record still waits for the parent to collect it,
like a completed order ticket left on the counter. An orphan is a still-running child whose parent exited,
so the system adopts it. Recognize this in process lifecycle questions. Zombies waste process-table entries,
not CPU; orphans are not automatically bad, but they need a responsible parent or supervisor.

**Short answer (say this first, 20-40 sec):**
A zombie process has finished execution but still has an entry in the process table because the parent has not collected its exit status. An orphan process is still running after its parent exits and is adopted by init/system process.

**Detailed answer:**
- Zombie: terminated but not reaped with `wait`.
- Orphan: running child whose parent died.
- Many zombies can exhaust process table entries.
- Daemon/service managers handle reaping and adoption.

**Common mistakes:**
- Thinking zombies consume CPU; they are already terminated.
- Confusing orphan with zombie.
- Ignoring parent responsibility to wait on children.

**Follow-ups:**
- *How remove zombie?* → Parent calls `wait`, or parent dies and init adopts/reaps.
- *Is orphan always bad?* → Not necessarily; daemons may intentionally detach.

### Q30. Green threads vs OS threads vs Java virtual threads?
`🔴 Advanced` `🔥` `Stretch`

**Why they ask:** Java 21 virtual threads are important for backend interviews.

**Beginner lens:** This question asks who schedules the work. Operating-system threads are scheduled by the
OS and map to kernel resources. Green threads are scheduled in user space by a runtime. Java virtual threads
are lightweight Java threads that usually mount on carrier OS threads when running and unmount when blocked.
Recognize the performance distinction: virtual threads help blocking concurrency, not CPU-bound speed or
shared-state safety.

**Short answer (say this first, 20-40 sec):**
OS threads are scheduled by the kernel. Green threads are user-level threads scheduled by a runtime and invisible to the OS. Java virtual threads are lightweight JVM-managed threads that run on carrier OS threads and are excellent for many blocking I/O tasks.

**Detailed answer:**
```mermaid
flowchart TD
    APP["Java tasks"] --> VT["Many virtual threads"]
    VT --> JVM["JVM scheduler"]
    JVM --> OS1["Carrier OS thread 1"]
    JVM --> OS2["Carrier OS thread 2"]
    OS1 --> CPU["CPU cores"]
    OS2 --> CPU
```
- Virtual threads reduce thread-per-request overhead for blocking I/O.
- They do not make CPU-bound code faster.
- Avoid pinning carrier threads with long synchronized/native blocking sections.

**Common mistakes:**
- Calling virtual threads the same as old green threads.
- Using virtual threads to solve CPU bottlenecks.
- Keeping huge platform-thread pools and adding virtual threads blindly.

**Follow-ups:**
- *Best use case?* → High-concurrency blocking I/O services.
- *Do virtual threads need synchronization?* → Yes, shared state rules remain.

### Q31. What are inodes and basic file-system metadata?
`🟡 Intermediate` `Medium` `Must-know SDE-1`

**Why they ask:** File storage questions appear in OS and systems rounds.

**Beginner lens:** An inode is like a library catalog card for file contents and metadata. File names live
in directories and point to inodes; the inode tracks ownership, permissions, size, timestamps, and block
locations. Recognize this when hard links, deletes, file descriptors, or metadata questions appear. In
backend work, inode and file-descriptor limits can affect logs, uploads, temporary files, and sockets.

**Short answer (say this first, 20-40 sec):**
An inode is a Unix-like file-system structure storing metadata and pointers to file data blocks. It stores permissions, owner, size, timestamps, and block references, but not usually the filename.

**Detailed answer:**
- Directories map names to inode numbers.
- Hard links are multiple directory entries pointing to same inode.
- File descriptors reference open file descriptions.
- Metadata operations can be expensive at scale.

**Common mistakes:**
- Saying inode stores filename.
- Confusing hard links and symbolic links.
- Ignoring permissions and ownership metadata.

**Follow-ups:**
- *What happens when deleting a file?* → Directory entry removed; data freed when link count and open references drop.
- *What is symlink?* → Separate file containing path to target.

### Q32. How do OS concepts affect Java backend performance?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** They want theory connected to real backend engineering.

**Beginner lens:** Map backend symptoms to operating-system resources. High runnable thread count points to
scheduling and context switches; blocked threads point to Input and Output or locks; high page faults point
to memory pressure; file descriptor exhaustion breaks sockets and files; container limits change CPU and
memory behavior. The interviewer is testing whether you debug below the framework layer instead of blaming
Spring Boot or the database blindly.

**Short answer (say this first, 20-40 sec):**
OS concepts affect backend latency through thread scheduling, context switches, blocking I/O, page faults, file descriptors, socket buffers, and memory pressure. A good Java backend engineer tunes thread pools, timeouts, heap, connection pools, and resource limits.

**Detailed answer:**
- Too many platform threads → context switching and memory overhead.
- Too small DB pool → queueing; too large → DB overload.
- Memory leaks → GC pressure, paging, OOM.
- Unbounded queues → latency spikes and memory growth.
- File descriptor leaks → cannot accept connections/files.

**Common mistakes:**
- Debugging only application code and ignoring OS metrics.
- Increasing every pool size during incidents.
- No timeouts on sockets and external calls.

**Follow-ups:**
- *What metrics matter?* → CPU run queue, context switches, memory/swap, open FDs, network errors, disk I/O.
- *How tie to containers?* → CPU/memory limits change scheduling, GC, and OOM behavior.

## Revision Cheat Sheet
- Process = isolated program; thread = execution path sharing process memory.
- PCB stores process state, registers, scheduling, memory, and resource info.
- Context switches enable multitasking but cost CPU/cache/TLB locality.
- User mode is restricted; kernel mode manages hardware and privileged operations.
- Syscalls bridge user programs to kernel services.
- Process states: new, ready, running, waiting, terminated.
- FCFS is simple but convoy-prone; SJF minimizes waiting if burst known; RR is fair; priority can starve.
- Mutex protects one critical section; semaphore counts permits.
- Use `while` around Java monitor waits.
- Deadlock requires mutual exclusion, hold-and-wait, no preemption, circular wait.
- Banker's algorithm checks safe sequences before granting resources.
- Paging maps fixed virtual pages to physical frames; segmentation maps logical variable-size regions.
- Page faults trap to OS; TLB caches translations.
- Thrashing means page I/O dominates useful work.
- Virtual threads help blocking I/O concurrency, not CPU-bound speed.

## Flashcards
- Q: Process vs thread? → A: Process has isolated address space; thread shares process memory.
- Q: PCB stores? → A: PID, state, registers, scheduling info, memory info, files.
- Q: Context switch cost? → A: Save/load context plus cache/TLB disruption.
- Q: Ready vs waiting? → A: Ready can run; waiting needs an event.
- Q: Convoy effect? → A: Short jobs wait behind a long FCFS job.
- Q: Starvation fix? → A: Aging or fair scheduling.
- Q: Mutex vs semaphore? → A: Lock ownership vs counted permits.
- Q: Deadlock conditions? → A: Mutual exclusion, hold-and-wait, no preemption, circular wait.
- Q: Page table role? → A: Maps virtual pages to physical frames.
- Q: TLB role? → A: Caches address translations.
- Q: Thrashing? → A: Excessive paging due to memory pressure.
- Q: FIFO anomaly? → A: More frames can cause more page faults.
- Q: Zombie process? → A: Terminated child not reaped by parent.
- Q: `fork` vs `exec`? → A: Copy process vs replace process image.
- Q: Virtual threads best for? → A: Many blocking I/O tasks.
