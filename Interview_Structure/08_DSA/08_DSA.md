# 08 · Data Structures & Algorithms

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

Generate FAANG-level DSA preparation in Java.

> **DSA is ~50% of the SDE-1 outcome** (online assessment + 2–3 coding rounds). Order the content by SDE-1 priority:
> - **Tier 1 (must master first):** Arrays, Strings, HashMap, Two Pointer, Sliding Window, Binary Search, Linked List, Stack, Queue, Trees, BST, Heap, Graph BFS/DFS, 1D/2D DP basics, Recursion/Backtracking basics
> - **Tier 2 (next):** Topological Sort, Greedy, Intervals, Prefix Sum, Monotonic Stack, Trie, Union Find
> - **Tier 3 [Stretch]:** Advanced DP (bitmask, digit, interval), Segment Tree / Fenwick Tree, advanced graph (Dijkstra variants, MST, bridges)
>
> Anchor problem lists to well-known sheets (NeetCode 150, Striver SDE Sheet, LeetCode Top Interview 150) instead of inventing lists.

### Topics

1. Arrays
2. Strings
3. HashMap
4. Linked List
5. Stack
6. Queue
7. Heap
8. Tree
9. BST
10. Graph
11. Trie
12. Union Find
13. Dynamic Programming
14. Backtracking
15. Sliding Window
16. Two Pointer
17. Binary Search
18. Greedy
19. Bit Manipulation
20. Prefix Sum / Intervals / Monotonic Stack / Topological Sort

### For EACH problem provide

- Problem Statement
- Brute Force
- Better Approach
- Optimal Approach
- Java Solution
- Time Complexity
- Space Complexity
- Pattern Recognition (how to identify this pattern from the problem statement)
- Edge cases and dry run

### Java Toolkit for Coding Rounds

- Must-know APIs: `ArrayList`, `HashMap` / `getOrDefault` / `merge`, `HashSet`, `TreeMap` (`floorKey`, `ceilingKey`), `ArrayDeque` (as stack and queue), `PriorityQueue` with custom `Comparator`, `Collections`, `Arrays.sort` / `Arrays.fill`, `StringBuilder`, `Character` helpers
- Why `ArrayDeque` over `Stack`, `int` overflow and `long`, `Integer` caching pitfall with `==`
- Fast input / output for online assessments (`BufferedReader`, `StringTokenizer`, `PrintWriter`)
- Recursion depth limits and converting recursion to iteration

### Also list

- **Top 200 FAANG Problems** as a table: # | Problem | LeetCode ID | Pattern | Company Tags | Difficulty
- Pattern cheat sheet: "If you see X in the problem, think Y"
- How to communicate while solving (clarify → brute force → optimize → code → test)
