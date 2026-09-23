# 25 · MCP (Model Context Protocol) & Agentic AI

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

Prepare me for MCP and Agentic AI interview questions. My projects already use LangChain, RAG, Gemini, and Nemotron, so interviewers at AI-focused teams will ask about agents, tool calling, and MCP. Cover the **latest MCP specification version** and explicitly call out what changed between spec versions. If you are unsure about a spec detail, say so and point me to the official docs at modelcontextprotocol.io.

---

### Part A: MCP Fundamentals

- What problem MCP solves (the M × N integration problem), and the "USB-C for AI" analogy
- Architecture: **Host**, **Client**, **Server**, and how they relate
- Protocol layer: JSON-RPC 2.0 messages (requests, responses, notifications)
- Lifecycle: initialize → capability negotiation → operation → shutdown
- Transports: **stdio** vs **Streamable HTTP** (and the older HTTP + SSE transport it replaced); when to use each
- **Server primitives:** Tools, Resources (and resource templates), Prompts
- **Client primitives:** Sampling, Roots, Elicitation
- Tool definitions: name, description, input schema (JSON Schema), structured output, annotations
- Pagination, progress notifications, cancellation, logging
- Authorization: OAuth 2.1 for remote servers, protected resource metadata, token audience
- MCP vs plain function calling vs OpenAPI plugins vs A2A (Agent-to-Agent protocol)

### Part B: MCP Security (asked often)

- Prompt injection through tool results and resources
- Tool poisoning, rug-pull attacks (tool definitions that change after approval)
- Confused deputy problem, token passthrough anti-pattern
- Least privilege, human-in-the-loop approval, sandboxing local servers
- Secrets handling and auditing

### Part C: Building MCP Servers & Clients

- Official SDKs: Java (and **Spring AI MCP** starters), Python (FastMCP), TypeScript
- Build a Java / Spring Boot MCP server that exposes:
  - a tool: `search_poetry(query, top_k)` backed by my Lafz pgvector retrieval
  - a resource: poetry chunk by ID
  - a prompt: "write a poem in the style of <poet>"
- Build a Python MCP server with FastMCP for the same thing
- Connect it to a host (VS Code / Claude Desktop / MCP Inspector) and test it
- Error handling, input validation, timeouts, logging
- Deploying a remote MCP server (Docker, Streamable HTTP, auth)

### Part D: Agentic AI

- What is an AI agent? Agent vs workflow vs chain
- Tool / function calling internals (how the LLM decides to call a tool)
- Agent patterns: ReAct, Plan-and-Execute, Reflection, Router, Orchestrator-Workers, Evaluator-Optimizer
- Memory: short-term (context), long-term (vector store), episodic
- Multi-agent systems: supervisor vs peer-to-peer, handoffs
- Frameworks: LangChain agents, **LangGraph** (state graphs, checkpoints), LlamaIndex, Spring AI, Semantic Kernel, CrewAI, AutoGen / Microsoft Agent Framework: when to use which
- Guardrails, loop limits, cost / token budgets, human-in-the-loop
- Evaluating agents: task success rate, tool-call accuracy, trajectory evaluation, LLM-as-judge
- Observability for agents: tracing tool calls (LangSmith, OpenTelemetry)
- Failure modes: hallucinated tool calls, infinite loops, context overflow, wrong tool selection

### Part E: Design Questions

- Design an MCP server platform for a company (registry, auth, multi-tenant, rate limits, audit)
- Design an AI coding assistant that uses MCP tools
- Design a customer-support agent with RAG + tools + human escalation
- Upgrade my Email Reply Assistant into an agent (read inbox via tool, draft reply, ask for approval)

### Part F: Resume Upgrade

- Suggest how to add an MCP server to Lafz or the Email Reply Assistant, and write the resume bullet for it (XYZ format)

### Provide

- 40+ Interview Questions (🟢 → ⚫) with full Output Format
- Mermaid diagrams: MCP architecture, request lifecycle sequence, agent loop
- Java and Python code for Part C
- One-page MCP cheat sheet
