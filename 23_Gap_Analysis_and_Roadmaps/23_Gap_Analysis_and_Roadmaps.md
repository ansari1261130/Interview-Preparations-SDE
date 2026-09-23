# 23 · Gap Analysis, Study Plan & Roadmaps

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

Before generating, ask me for: target companies (priority order), interview dates, hours available per day, and self-ratings (1–5) for DSA, LLD, HLD, Java, Spring, DB, Concurrency, CS Fundamentals (OS / CN / DBMS), GenAI / RAG, MCP & Agentic AI, AI-Assisted Coding, Behavioral.

> **Time allocation for SDE-1 (use this as the default split, adjust by my self-ratings):**
>
> | Area | Share of time |
> |------|---------------|
> | DSA | 50% |
> | Java + Concurrency + Spring Boot + JPA + SQL | 20% |
> | LLD / Machine Coding | 10% |
> | Resume + Projects + GenAI/RAG | 10% |
> | CS Fundamentals (OS / CN / DBMS) | 5% |
> | Behavioral + HLD basics + everything else | 5% |

### Generate

1. **Resume Gap Analysis:** skill × target-company heatmap table (e.g., missing Kafka, microservices, a pure Java backend project, Redis usage in projects)
2. **Personalized Study Plan** based on my self-ratings
3. **30-Day FAANG Roadmap:** daily tasks + hours + LeetCode problem IDs
4. **60-Day Product Company Roadmap**
5. **High Priority Topics:** 🔥 must-know, ranked by frequency × my weakness
6. **Project Upgrade Plan:** concrete features to add to my projects (Redis caching, Kafka queue, JWT auth, tests, CI/CD, metrics) to close resume gaps
7. **Last 24-Hour / Last 1-Hour Revision Cheat Sheet**
8. **Weekly Mock Interview Schedule + Self-Scoring Rubric** (Problem Solving, Coding, Communication, Design, Behavioral: 1–5 each)
9. **Progress Tracker** table I can update after every session
