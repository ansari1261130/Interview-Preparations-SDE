# 17 · Generative AI, RAG, LangChain & Vector Databases

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

Both my projects are AI-based (**Lafz**: RAG with LangChain + pgvector + NVIDIA embeddings + Nemotron; **AI Email Reply Assistant**: Gemini API). Interviewers WILL deep-dive here. Prepare me fully.

### Topics

- LLM basics: tokens, context window, temperature, top-p, hallucination
- Embeddings: what they are, dimensionality (why 2048-dim?), cosine vs dot product vs L2 distance, normalization
- Chunking strategies (fixed, recursive, semantic), chunk size and overlap tradeoffs
- RAG pipeline: ingestion → chunking → embedding → indexing → retrieval → re-ranking → prompt augmentation → generation
- pgvector: `vector` type, operators (`<->`, `<=>`, `<#>`), exact vs approximate search, **IVFFlat vs HNSW** indexes, index parameters (lists, probes, m, ef_construction, ef_search), dimension limits for indexing
- pgvector vs Pinecone vs Milvus vs Weaviate vs FAISS vs Azure AI Search: when to use which
- Hybrid search (BM25 + vector), metadata filtering, re-rankers
- LangChain: chains, retrievers, prompt templates, memory, LCEL, agents; LangChain vs LlamaIndex vs Spring AI
- Prompt engineering: system prompts, few-shot, output formatting, guardrails
- Evaluating RAG: retrieval metrics (Recall@K, MRR, nDCG), generation metrics (faithfulness, relevance), RAGAS
- Latency optimization: caching embeddings / responses, batching, streaming, async
- Cost optimization: token usage, model choice, caching
- Security: prompt injection, PII leakage, API key management
- Production concerns: rate limits, retries, fallbacks, timeouts, observability for LLM calls
- Fine-tuning vs RAG vs prompt engineering: when to use each
- Agents, tool calling, and MCP are covered in depth in file 25; give only a short overview here

### Python Essentials (Python is on my resume and Lafz is Python-based)

- Python data types, mutability, list / dict / set comprehensions, generators vs iterators, decorators, context managers
- `*args` / `**kwargs`, shallow vs deep copy, `is` vs `==`
- GIL, threading vs multiprocessing vs asyncio (and why it matters for LLM API calls)
- Virtual environments, `pip`, `requirements.txt` / `pyproject.toml`
- Type hints, Pydantic (used heavily by LangChain)
- Python vs Java: when I would pick each for a backend / AI service

### Resume-Specific Deep Dive

- "How did you measure sub-second retrieval latency? p50 / p95? How many vectors?"
- "What happens to latency at 1M / 100M vectors?"
- "How do you keep embeddings in sync when source data changes?"
- "How do you prevent the model from copying retrieved poetry verbatim?"
- "Why Nemotron / NVIDIA embeddings instead of OpenAI / open-source models?"
- "How would you implement the Email Reply Assistant backend in Spring AI?"
- "How do you handle Gemini API failures, timeouts, and rate limits in Spring Boot?"

### Provide

- 40+ Interview Questions (🟢 → ⚫) with full Output Format
- Architecture diagrams (Mermaid) for both projects
- SQL for pgvector table creation, HNSW index, and Top-K query
- System Design: "Design a RAG-based semantic search system for 100M documents"
