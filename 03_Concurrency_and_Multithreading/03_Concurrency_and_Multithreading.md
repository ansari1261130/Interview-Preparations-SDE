# 03 · Concurrency & Multithreading (Theory + Production)

<!-- HEADER:START -->
> **How to use:** Copy everything below the line into your AI assistant (ChatGPT / Claude / Copilot). Attach your resume PDF if the tool supports it. Type **NEXT** to continue after every part.

---

## Role

Act as a Senior FAANG Interview Coach, Hiring Manager, Staff Software Engineer, System Design Interviewer, and Technical Mentor.

## Candidate Profile (from my resume)

- **Mohd Azam Ansari**, Associate Software Engineer at MAQ Software (Nov 2025 – Present), ~1 YOE
- B.Tech CSE, Galgotias University (2022 – 2026), CGPA 8.63
- **Stack:** Java, Python, Spring Boot, Spring MVC, REST APIs, Hibernate, JPA, MySQL, PostgreSQL, pgvector, Redis, LangChain, RAG, Embeddings, JUnit, Postman, Git, GitHub, Docker, Azure, Microsoft Fabric, Power BI, Power Automate
- **Work:** Optimized SQL queries and semantic data models (70% faster via indexing, query tuning, Performance Analyzer); automated workflows with Power Automate (90% less manual effort); built data validation and integrity checks
- **Project 1, Lafz: Mirage of Words:** RAG poetry generator using LangChain, PostgreSQL + pgvector, NVIDIA 2048-dim embeddings, Nemotron; Top-K semantic retrieval with sub-second latency
- **Project 2, AI Email Reply Assistant:** Spring Boot REST backend + Gemini API, frontend integration, JSON flows, exception handling, Docker, env-var secrets management
- **Target:** SDE-1 / SDE-2 Backend (Java) at Google, Amazon, Microsoft, Meta, Apple, Netflix, Uber, Airbnb, Walmart Global Tech, Salesforce, Adobe, Atlassian, Visa, Mastercard, American Express, LinkedIn, Flipkart, Swiggy, Zomato

## Rules

- **Target depth is SDE-1 / SDE-2.** Spend ~70% of content on 🟢🟡 questions (what SDE-1 loops actually ask), ~20% on 🔴, and ~10% on ⚫ as stretch.
- Tag every question **[Must-know SDE-1]** or **[Stretch]** so I know what to skip when short on time.
- Tie answers back to MY resume (my projects, my tech, my numbers) wherever possible. No generic answers.
- **Accuracy first:** do not invent company-specific "most asked" claims, frequencies, or LeetCode IDs. If unsure, say so and point me to where to verify (LeetCode company tags, NeetCode 150, Striver SDE Sheet, official docs).
- Output is large: deliver in parts. End each part with a progress tracker and wait for me to type **NEXT**.
- Use tables, Mermaid diagrams (architecture, sequence, ER, class), and Java code blocks.
- If behaviour changed across versions (Java 7 / 8 / 17 / 21, Spring Boot 2 / 3), call out the difference explicitly.
- End the subject with: 10-line Revision Summary + 10 Flashcards + "Top 5 things to say in the interview".

## Output Format (for every question)

- Question
- Why Interviewer Asks This
- Expected Answer
- Optimal Answer
- Common Mistakes
- Follow-Up Questions
- Interview Tips

**Difficulty:** 🟢 Beginner · 🟡 Intermediate · 🔴 Advanced · ⚫ FAANG Level
**Frequency:** 🔥 High · Medium · Low

---
<!-- HEADER:END -->

## Task

Prepare me for concurrency interviews, both theory and real production scenarios.

### Theory Topics

- Thread lifecycle, Runnable vs Callable, ExecutorService
- Multithreading
- Synchronization (`synchronized` block vs method, instance vs static lock)
- Locks (ReentrantLock, ReadWriteLock, StampedLock, fairness, tryLock)
- `volatile`
- Atomic Classes (CAS, AtomicInteger, LongAdder)
- Java Memory Model, happens-before, visibility vs atomicity
- Concurrent Collections (ConcurrentHashMap internals Java 7 vs Java 8, CopyOnWriteArrayList, BlockingQueue)
- wait / notify, CountDownLatch, CyclicBarrier, Semaphore, Phaser
- CompletableFuture, ForkJoinPool, parallel streams pitfalls

### Production Topics

- Check-then-act, read-modify-write, lost updates, double-checked locking
- ExecutorService / thread pool sizing (CPU-bound vs IO-bound)
- Virtual Threads (Java 21), pinning, ThreadLocal leaks in thread pools
- Deadlock, livelock, starvation: how to detect (jstack, thread dumps) and fix
- Concurrency across multiple pods: why `synchronized` is useless there, and the alternatives (DB locks, Redis / Redisson locks, idempotency, queues)
- How to TEST concurrent code (CountDownLatch, ExecutorService stress tests, jcstress)

## Real Interview Seed Questions (MUST ANSWER)

These were asked in real interviews. Answer each in full Output Format, then generate **30 more** in the same production-scenario style.

1. What is a race condition and when does it occur?
2. How do you prevent race conditions?
3. `putIfAbsent` vs `computeIfAbsent` in ConcurrentHashMap?
4. Two methods are synchronized but we still get a race condition. Why, and how would you investigate? (different lock objects, static vs instance lock, check-then-act across two calls, multiple JVM instances/pods, leaked references)
5. In which scenario would you use a big HashMap? (memory, load factor, resizing, initial capacity, off-heap / Redis alternatives)
6. Can HashMap cause a deadlock / infinite loop? (Java 7 resize cycle, Java 8 lost updates / data corruption, treeification)
7. Does ConcurrentHashMap allow null keys/values? Why not? (ambiguity of `get() == null`)
8. A shared HashMap works in testing but fails in production. Why?
9. Why would requests time out in an application using a shared HashMap across threads, even though the app is not crashing?
10. In which scenarios do you get concurrency issues? (in-JVM, DB, distributed)

For each: include Java code that **reproduces** the bug and code that **fixes** it.
