# 09 · System Design (HLD): Framework → Building Blocks → Case Studies

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

Teach me High-Level Design (HLD) from zero to interview-ready. Cover every building block first, then the case studies. I want **complete coverage with no gaps**.

> **SDE-1 expectation:** Solid fundamentals + a clean, well-reasoned HLD for the Tier 1 case studies. Many SDE-1 loops have no HLD round or only a light one, but SDE-2 loops always do. For each case study, give an **"SDE-1 answer (enough to pass)"** first, then a **"Senior answer"** tagged [Stretch].

---

### Part A: How to Run an HLD Interview

- 45-minute framework with a time split: Requirements (5) → Estimation (5) → API Design (5) → Data Model (5) → High-Level Diagram (10) → Deep Dives (10) → Bottlenecks & Tradeoffs (5)
- Clarifying-question checklist (users, scale, read/write ratio, latency, consistency, availability, geography)
- Functional vs non-functional requirements
- What interviewers score (rubric) at SDE-1 vs SDE-2 vs Senior
- How to discuss tradeoffs out loud ("I chose X over Y because…")
- Common mistakes (jumping to the solution, over-engineering, ignoring failure cases)

### Part B: Back-of-the-Envelope Estimation

- Latency numbers every engineer should know, powers of 2, time conversions
- Formulas for QPS, peak QPS, storage, bandwidth, memory for cache, number of servers
- 5 fully worked estimation examples

### Part C: Building Blocks

For each, cover: what it is, how it works internally, when to use it, tradeoffs, and 5 interview questions.

1. **Networking & Entry:** DNS, CDN (push vs pull), load balancers (L4 vs L7, round robin, least connections, consistent hashing), reverse proxy, API gateway
2. **Scaling:** vertical vs horizontal, stateless services, sticky sessions, autoscaling
3. **Caching:** client / CDN / app / DB caches, cache-aside / write-through / write-back, eviction, invalidation, stampede, hot keys
4. **Databases:** SQL vs NoSQL (key-value, document, wide-column, graph, time-series, search), indexing, denormalization
5. **Replication:** leader-follower, multi-leader, leaderless, replication lag, read-your-writes
6. **Partitioning / Sharding:** range vs hash vs directory, consistent hashing with virtual nodes, hot partitions, resharding
7. **Storage Internals:** B-Tree vs LSM Tree, WAL, SSTables, compaction, Bloom filters
8. **Blob / Object Storage:** S3 / Azure Blob, chunking, deduplication, multipart upload, presigned URLs
9. **Messaging:** queue vs pub/sub vs log (Kafka), delivery semantics, ordering, backpressure, DLQ
10. **Consistency Models:** CAP, PACELC, strong / eventual / causal, quorum (R + W > N), vector clocks, conflict resolution (LWW, CRDTs)
11. **Distributed Coordination:** leader election, consensus (Raft / Paxos basics), ZooKeeper / etcd, distributed locks, gossip, heartbeats, split brain
12. **Distributed Transactions:** 2PC, Saga, Outbox, idempotency keys, exactly-once illusions
13. **Unique ID Generation:** UUID, Snowflake, DB sequences, ticket servers
14. **Rate Limiting:** token bucket, leaky bucket, fixed / sliding window log / sliding window counter, distributed rate limiting
15. **Search:** inverted index, Elasticsearch, ranking basics, typeahead with tries
16. **Geospatial:** geohash, quadtree, S2 / H3
17. **Real-time Communication:** polling, long polling, WebSockets, SSE, presence, fan-out on write vs read
18. **Data Processing:** batch vs stream (Spark, Flink, Kafka Streams), Lambda vs Kappa architecture, data warehouse vs data lake
19. **Reliability:** redundancy, failover, multi-AZ / multi-region, disaster recovery (RPO / RTO), circuit breakers, bulkheads, graceful degradation, chaos testing
20. **Security in Design:** authN / authZ, API keys, OAuth, encryption at rest / in transit, secrets, DDoS protection
21. **Observability in Design:** metrics, logs, traces, SLOs, alerting
22. **Cost Awareness:** storage tiers, compute choices, egress costs

### Part D: Case Studies

**Tier 1: SDE-1 Must-Know**

- URL Shortener (TinyURL / Bitly)
- Pastebin
- Rate Limiter (distributed)
- Unique ID Generator
- Distributed Cache / Key-Value Store
- Notification Service (email / SMS / push)
- Chat Application (WhatsApp)
- News Feed (Twitter / Instagram)
- Search Autocomplete / Typeahead
- Ticket Booking (BookMyShow): seat locking, concurrency

**Tier 2: SDE-2 Must-Know**

- Video Streaming (YouTube / Netflix)
- Ride Hailing (Uber / Ola)
- Food Delivery (Swiggy / Zomato)
- E-Commerce (Flipkart / Amazon), including flash sale / inventory
- Payment Gateway / Digital Wallet / UPI (idempotency, ledger, reconciliation)
- File Storage & Sync (Dropbox / Google Drive)
- Web Crawler
- Proximity Service (Yelp / nearby restaurants)
- Hotel Booking (Airbnb)
- Leaderboard / Top-K (heavy hitters)
- Distributed Job Scheduler (cron at scale)
- Email Service (Gmail)
- Metrics, Logging & Monitoring System

**Tier 3: [Stretch]**

- Collaborative Editing (Google Docs): OT vs CRDT
- Distributed Message Queue (design Kafka)
- Stock Exchange / Order Matching Engine
- Ad Click Aggregation
- Google Maps
- Video Conferencing (Zoom)
- Search Engine

**AI Systems (connect to my projects)**

- RAG / Semantic Search for 100M documents (scaled-up Lafz)
- LLM-powered AI Assistant at scale (scaled-up Email Reply Assistant): rate limits, queuing, streaming, caching, cost control
- MCP-based Tool Platform for AI agents (see file 25)

### For each case study provide

- Functional & non-functional requirements, plus clarifying questions to ask
- Capacity Estimation
- API Design
- Data Model + DB choice with justification
- HLD (Mermaid diagram)
- LLD of the key component (classes / key APIs)
- Deep Dives into the 2–3 hardest parts
- Redis Usage
- Message Queue Usage
- Scaling (10x → 100x)
- High Availability & Fault Tolerance ("What if X goes down?")
- Tradeoffs and alternatives rejected
- SDE-1 answer vs Senior answer
- Follow-up questions the interviewer will ask
- One-page summary for revision

### Part E: Practice

- 10 "design critique" exercises: show me a flawed design and let me find the issues
- For live practice, use the Mock Interview file (22) and pick the "HLD" round
