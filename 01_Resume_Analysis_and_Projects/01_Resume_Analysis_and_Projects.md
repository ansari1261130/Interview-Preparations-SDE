# 01 · Resume Analysis, Projects & Cross-Examination

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

Perform a complete analysis of my resume and prepare me for every question that can be asked from it.

### Part A: Resume Analysis

For every **Project, Skill, Technology, Internship, Achievement, Certification, and Work Experience** on my resume, generate:

1. Likely Interview Questions
2. Deep Dive Questions
3. Follow-Up Questions
4. Cross Questions
5. Scenario-Based Questions
6. Why Questions
7. Optimization Questions

Also identify:

- Strengths
- Weak Areas
- Missing Concepts
- Resume Improvements
- Potential Red Flags (e.g., full-time job from Nov 2025 overlaps with graduation in June 2026; Redis listed in skills but not used in any project; Hibernate/JPA listed but not shown in projects; mostly BI work experience for a backend role)

### Part B: Project-Wise Preparation

For **Lafz: Mirage of Words**, **AI Email Reply Assistant**, and **MAQ Software work (SQL optimization, Power Automate, data validation)**, generate:

- How to Explain the Project in an Interview (30-second, 2-minute, and 5-minute versions)
- Project Architecture (Mermaid diagram)
- Business Problem
- Why this Technology was chosen (and why not alternatives)
- Database Design (ER diagram, tables, indexes)
- Scaling Strategy (10x, 100x, 1000x users)
- Challenges Faced
- Production Issues (realistic ones I should be ready to discuss)
- Optimization Done

Then generate **20–30 expected interview questions per project**. For each question provide:

- Answer
- Thought Process
- Follow-up Questions
- Interview Tips

### Part C: Resume Cross-Examination (Trap Questions)

Pick EVERY bullet on my resume and grill me:

- "You wrote 70% / 90% improvement: how was it measured? Baseline? Tooling?"
- "What exactly was YOUR contribution vs the team's?"
- "What would you do differently today?" "What broke in production?"
- "Why not <alternative tech>?" "Explain this on a whiteboard."
- "How did you achieve sub-second retrieval latency? What was p50/p95/p99?"
- "Why 2048-dimensional embeddings? What is the storage/latency cost?"

Flag any bullet I cannot defend deeply and suggest a safer rewrite.

### Part D: Resume ATS Optimization

- ATS score estimate and missing keywords for Java Backend SDE roles
- Rewrite every bullet using the Google XYZ formula ("Accomplished X, measured by Y, by doing Z")
- Suggest what to add (e.g., a Java/Spring Boot + Redis + Kafka backend project) to close gaps
- Ordering, one-page advice, and a final rewritten resume
