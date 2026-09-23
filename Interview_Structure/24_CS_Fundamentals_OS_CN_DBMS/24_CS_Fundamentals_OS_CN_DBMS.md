# 24 · CS Fundamentals: Operating Systems, Computer Networks & DBMS Theory

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

Prepare me for CS fundamentals rounds. Microsoft, Amazon, Adobe, Walmart, Visa, Mastercard, and most Indian product companies ask these of candidates with 0–2 YOE. Keep answers crisp (interview-length), with diagrams where useful, and connect to Java/backend wherever possible (e.g., threads → Java threads, TCP → HTTP clients and connection pools).

### Operating Systems

- Process vs thread, PCB, context switching, user vs kernel mode, system calls
- CPU scheduling (FCFS, SJF, Round Robin, Priority), starvation, aging
- Process synchronization: critical section, mutex vs semaphore, monitors, producer-consumer, readers-writers, dining philosophers
- Deadlock: four conditions, prevention, avoidance (Banker's algorithm), detection, recovery
- Memory management: paging, segmentation, virtual memory, page faults, TLB, thrashing
- Page replacement (FIFO, LRU, Optimal)
- Fragmentation (internal vs external)
- IPC (pipes, shared memory, message queues, sockets)
- File systems basics, inodes
- Multithreading models, green threads vs OS threads (relate to Java virtual threads)

### Computer Networks

- OSI vs TCP/IP model, what happens at each layer
- TCP vs UDP, 3-way handshake, 4-way termination, flow control vs congestion control
- HTTP/1.1 vs HTTP/2 vs HTTP/3, keep-alive, HTTP methods and status codes
- HTTPS and TLS handshake, certificates
- DNS resolution flow
- **"What happens when you type a URL in the browser?"** (full end-to-end answer)
- IP addressing, subnetting basics, NAT, ports
- Load balancers, proxies vs reverse proxies, CDN
- WebSockets, long polling, SSE
- Cookies vs sessions vs tokens, CORS

### DBMS Theory

- ACID, transactions, schedules, serializability
- Normalization (1NF → BCNF) with examples, when to denormalize
- Keys (primary, candidate, super, foreign, composite)
- ER modelling
- Indexing theory (B-Tree / B+ Tree, clustered vs non-clustered)
- Concurrency control (2PL, timestamp ordering, MVCC)
- Recovery (write-ahead logging, checkpoints)
- SQL vs NoSQL, CAP theorem

### Provide

- Interview Questions (🟢 → 🔴) with full Output Format
- "Explain like an interviewer wants it" 30-second answers for the top 50 questions
- One-page cheat sheet per subject
