# 18 · Power BI, Microsoft Fabric & Data Engineering (Current Work Experience)

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

My current job at MAQ Software is Power BI / Microsoft Fabric / SQL focused. Interviewers will ask about it, and backend interviewers will test **how it transfers to software engineering**. Prepare me for both.

> **Keep this SDE-focused:** I am applying for backend SDE roles, not BI/data roles. Spend ~60% on the transferable parts (SQL performance tuning, indexing, data validation, automation, the "BI → backend" story) and ~40% on the Power BI / Fabric concepts I must be able to explain about my own work. Tag deep DAX / Fabric internals as [Stretch].

### Topics

- Power BI architecture: Power Query (M), data model, DAX, reports, service, gateways
- Star schema vs snowflake schema, fact vs dimension tables, relationships and cardinality
- Import vs DirectQuery vs Direct Lake (Fabric), composite models
- VertiPaq engine, columnar storage, compression
- DAX: CALCULATE, filter context vs row context, context transition, iterators (SUMX), time intelligence
- Performance Analyzer, DAX Studio, query folding, reducing model size
- Incremental refresh, aggregations, partitions
- Row-Level Security (RLS)
- Microsoft Fabric: Lakehouse, Warehouse, OneLake, Delta tables, Notebooks (PySpark), Data Pipelines, Dataflows Gen2
- Power Automate: triggers, actions, error handling, retries, approvals
- Data validation and integrity checks: what to validate, where (source / pipeline / model), how to alert
- ETL vs ELT, data quality, idempotent pipelines

### Resume-Specific Deep Dive

- "You improved dashboard and query performance by 70%. Walk me through exactly what you changed. Before/after numbers?"
- "Which indexes did you add and why? How did you verify them?"
- "What workflows did you automate with Power Automate, and how did you measure the 90% reduction?"
- "What data defects did your validation checks catch? Give an example."
- "Why do you want to move from BI to backend engineering?" (craft a strong, honest answer)
- "How does your BI experience make you a better backend engineer?"

### Provide

- 30+ Interview Questions (🟢 → ⚫) with full Output Format
- STAR-format stories for each work bullet
- Sample SQL and DAX snippets for the optimizations I claimed
