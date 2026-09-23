# 26 · AI-Assisted Coding Interviews

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

Prepare me for the new **AI-assisted coding round**. In this format the candidate may use an AI assistant (e.g., Copilot, Claude, ChatGPT, Cursor, or a built-in assistant in CoderPad / HackerRank) and is judged on **how** they use it: breaking down the problem, prompting, checking and debugging AI output, testing, and explaining decisions. Meta and others have reported rounds like this, and interview-trend reports (e.g., Karat 2026) describe the same shift.

> **Accuracy rule:** Formats differ by company and change often. Don't claim a specific company's exact format unless it's publicly documented; label anything else "typical format". Remind me to **ask the recruiter** which tools are allowed in each round.
>
> **Integrity rule:** Only use AI in rounds where it is explicitly allowed. Using it in a traditional round is usually detected and disqualifying. Cover how to keep my **unassisted** coding sharp too, since most DSA rounds are still AI-free.

---

### Part A: Understand the Format

- Typical setups: multi-file codebase to extend, bug-fix tasks, progressive multi-level problems (requirements added in stages), AI-allowed take-homes
- What interviewers evaluate (build a scoring rubric):
  - Understanding the problem and codebase **before** prompting
  - Breaking work into small, verifiable steps
  - Prompt quality (context, constraints, expected output)
  - Critical review of AI output (catching bugs, bad complexity, invented APIs)
  - Testing and edge cases
  - Debugging skill when AI code fails
  - Communication: narrating decisions, explaining why they accept or reject AI suggestions
  - Judgment: knowing when **not** to use AI
- How this round differs from a traditional DSA round, and what gets candidates rejected (blindly accepting output, not reading code, not testing, over-prompting)

### Part B: A Winning Workflow

Give me a step-by-step routine to follow in every AI-assisted round:

1. Read the problem / codebase and restate it in my own words
2. Clarify requirements and edge cases with the interviewer
3. Plan the approach and complexity **myself** first
4. Prompt for small, well-scoped pieces with clear context and constraints
5. Read **every line** the AI produces; explain it out loud
6. Write / generate tests, including edge cases, and run them
7. Debug failures; don't just re-prompt blindly
8. Refactor for readability and state the final complexity
9. Summarize the tradeoffs and what I'd improve with more time

### Part C: Prompting for Code

- Anatomy of a good coding prompt: context, goal, constraints (language, version, complexity, libraries), input/output examples, what NOT to change
- Asking for tests, explanations, alternatives, and complexity analysis
- Iterating: narrowing a bad answer instead of starting over
- Anti-patterns: pasting the whole problem, vague prompts, accepting large unreviewed diffs
- 10 bad prompt → good prompt examples for Java / Spring Boot tasks

### Part D: Verifying AI-Generated Code

Common AI mistakes and how to catch each quickly:

- Off-by-one and boundary errors, null handling
- Invented or outdated APIs / library versions (e.g., Spring Boot 2 vs 3, `javax` vs `jakarta`)
- Wrong or worse-than-claimed time / space complexity
- Missed edge cases (empty input, duplicates, overflow, Unicode)
- Concurrency bugs (race conditions, non-thread-safe collections)
- Security issues (SQL injection, hard-coded secrets, missing validation)
- Silently changing existing behaviour or breaking other tests
- Code that passes the given example but fails hidden tests

Give me a **60-second review checklist** I can run on any AI-generated snippet.

### Part E: Reading & Debugging Unfamiliar Code

- How to navigate a new repo fast (entry points, tests, package structure)
- Reading stack traces and failing tests
- Using the debugger, logs, and small experiments instead of guessing
- Explaining an unfamiliar codebase to the interviewer

### Part F: Practice Rounds (Java-focused)

Run each as a realistic 45–60 minute exercise. **Simulation mode:** you play both the interviewer AND the AI assistant. As the assistant, sometimes give subtly buggy, inefficient, or insecure code so I have to catch it. Score me at the end using the Part A rubric.

1. AI-generated LRU cache with a hidden bug: find and fix it
2. Extend a small Spring Boot REST service: new endpoint + validation + exception handling + tests
3. AI-generated code with a race condition on a shared `HashMap`: detect and fix it
4. AI gives an O(n²) solution: recognise it and drive it to optimal
5. Review an AI-generated SQL / JPA query for N+1, missing index, and injection risk
6. Progressive problem: an in-memory key-value store built up over 4 levels (basic ops → TTL → transactions → persistence)
7. Refactor a messy AI-generated class into clean, testable code
8. Write unit tests for an untested legacy class using AI, then find the gaps it missed
9. Debug a failing test suite in an unfamiliar codebase
10. Fix a retrieval bug in a Python RAG pipeline (connects to my Lafz project)

### Part G: Discussion Questions About AI Usage

Answer these in full Output Format (they come up in HM, behavioral, and technical rounds):

- How do you use AI tools in your daily work?
- How do you make sure AI-generated code is correct and secure?
- Tell me about a time an AI tool gave you wrong code. How did you catch it?
- What are the risks of AI-generated code in production?
- When would you NOT use an AI assistant?
- How do you handle licensing, secrets, and company-policy concerns with AI tools?
- How has AI changed the way you debug, test, and review code?
- Will AI replace software engineers? (give a thoughtful, balanced answer)

### Provide

- A scoring rubric table for AI-assisted rounds
- The 60-second AI-code review checklist
- 25+ Interview Questions (🟢 → ⚫) with full Output Format
- A weekly practice plan that balances AI-assisted and unassisted coding
- A one-page cheat sheet
