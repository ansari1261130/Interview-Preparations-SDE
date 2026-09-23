# 11 · Production Debugging, Troubleshooting & Observability

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

Prepare me for "production scenario" questions, the kind that separate candidates who have only studied from those who have debugged real systems.

### Part A: Debugging Runbooks

Give a step-by-step RUNBOOK (**symptom → hypothesis → tool/command → fix → prevention**) for:

- Latency spike, timeouts, high CPU, memory leak, OutOfMemoryError, GC thrashing
- Thread pool / HikariCP connection pool exhaustion, deadlock detection
- Slow queries, missing indexes, lock contention, replication lag
- Works locally but fails in prod; fails only under load; intermittent failures
- Third-party API (e.g., Gemini / LLM API) slow or rate-limited
- Pod restarts / OOMKilled / CrashLoopBackOff

**Tools:** jstack, jmap, jcmd, jstat, JFR, async-profiler, VisualVM, Eclipse MAT, EXPLAIN ANALYZE, pg_stat_statements, slow query log, APM (Datadog / New Relic / AppDynamics / Azure Application Insights).

### Part B: Observability & On-Call

- Logs, metrics, traces: the three pillars
- Correlation IDs, MDC
- Micrometer, Prometheus, Grafana, ELK, OpenTelemetry, Azure Monitor
- SLI / SLO / SLA, error budgets
- Alert design (avoid alert fatigue)
- Incident handling
- Blameless postmortem format (with a sample based on my resume)

## Real Interview Seed Questions (MUST ANSWER)

1. How do you investigate an application that normally responds in 100 ms but is now taking 4–5 seconds? (APM/traces, thread dump, GC logs, connection pool, DB slow query log, EXPLAIN ANALYZE, missing/unused index, locks, downstream calls, network, recent deploy)
2. How do you investigate an API that is responding slowly, including checking the database side and indexes?
3. Why would requests time out even though the application is not crashing? (thread pool / connection pool exhaustion, blocked threads, deadlock, infinite loop, GC pauses, downstream latency)
4. Why are all requests timing out with exceptions even though the application is running on the server?
5. What issues arise when a HashMap is shared by multiple threads in production, even if it works fine in testing?

Then generate **40 more** questions in the same production-scenario style.
