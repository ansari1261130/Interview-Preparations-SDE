# 10 · Low-Level Design (LLD), Object-Oriented Design & Machine Coding

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

Prepare me completely for LLD, object-oriented design, and machine coding rounds. These are very common for SDE-1 / SDE-2 at Flipkart, Swiggy, Zomato, Uber, Atlassian, Walmart, Amazon, Microsoft, and PhonePe. I want **complete coverage with no gaps**.

---

### Part A: Fundamentals

- OOP pillars applied to design (encapsulation, abstraction, inheritance, polymorphism)
- Composition over inheritance, interfaces vs abstract classes
- SOLID with a bad → good code example for each principle
- DRY, KISS, YAGNI, Law of Demeter, high cohesion / low coupling
- UML class diagrams: association, aggregation, composition, dependency, inheritance, multiplicity
- Sequence diagrams for key flows
- Enums, immutability, value objects vs entities
- Exception design and validation in LLD
- Designing for testability (dependency injection, interfaces, mocks)

### Part B: Design Patterns (GoF + Modern)

For each: intent, when to use, Java code, a real LLD problem where it fits, and common misuse.

- **Creational:** Singleton (thread-safe variants, enum singleton), Factory Method, Abstract Factory, Builder, Prototype
- **Structural:** Adapter, Decorator, Facade, Proxy, Composite, Bridge, Flyweight
- **Behavioral:** Strategy, Observer, State, Command, Chain of Responsibility, Template Method, Iterator, Mediator, Memento, Visitor
- **Modern / Enterprise:** Repository, Dependency Injection, Null Object, Specification, Event Bus, Pipeline

### Part C: Machine Coding Round Strategy

- 90–120 minute plan: clarify (10) → entities & relationships (10) → interfaces (10) → core code (50) → demo / driver + tests (15) → extensions (10)
- What evaluators score: working code, modularity, extensibility, naming, patterns, thread-safety, error handling, tests
- Package structure (model / service / repository / strategy / exception)
- In-memory storage design, how to handle "no DB allowed"
- Common reasons candidates fail machine coding rounds

### Part D: LLD Problems

**Tier 1: SDE-1 Must-Know**

- Parking Lot
- Tic-Tac-Toe
- Snake & Ladder
- LRU Cache (and LFU)
- Rate Limiter (token bucket, sliding window)
- Logger (levels, sinks, Chain of Responsibility)
- Vending Machine (State pattern)
- ATM
- Library Management
- Elevator System

**Tier 2: SDE-2 Must-Know**

- BookMyShow (seat locking + concurrency)
- Splitwise (expense sharing + simplify debts)
- Ride Booking / Uber (driver matching strategy)
- Food Delivery order lifecycle (Swiggy / Zomato)
- Hotel / Meeting Room Booking & Calendar Scheduler
- Inventory & Order Management (Amazon / Flipkart)
- Shopping Cart + Coupon / Discount Engine
- Notification System (multi-channel, retries, templates)
- Digital Wallet / Payment System
- In-Memory Key-Value Store (with TTL, transactions)
- Pub-Sub / Message Queue
- Task / Job Scheduler
- Car Rental System
- Stack Overflow / Q&A platform
- Chess

**Tier 3: [Stretch]**

- In-Memory File System (mkdir, ls, cd, rm)
- Text Editor with Undo / Redo (Command + Memento)
- Rule Engine / Workflow Engine
- Online Auction System
- Amazon Locker
- Cricbuzz / live score system
- Twitter / Instagram LLD (post, follow, feed)

### Part E: Concurrency-Focused LLD (very common follow-up)

- Thread-safe Singleton
- Bounded Blocking Queue (producer-consumer)
- Thread-safe LRU Cache
- Concurrent Rate Limiter
- Connection Pool
- Thread Pool (build a mini ExecutorService)
- Seat booking without double booking (in-memory lock vs DB lock vs optimistic locking)
- Read-Write Lock based cache

### Part F: Schema Design for LLD

- For each Tier 1 and Tier 2 problem, also give the relational schema (tables, keys, indexes) and explain how the classes map to tables (JPA entities)

### Part G: Code Quality / Refactoring Round

(Atlassian "Code Design", Uber / Google code review style)

- Give me 5 messy code snippets to refactor, then show the ideal refactor
- Code review checklist: naming, SRP, error handling, magic numbers, tests, concurrency bugs

### For each LLD problem provide

- Requirements (functional + clarifying questions to ask)
- Entities and relationships
- Class Diagram (Mermaid)
- Sequence diagram for the main flow
- Design Patterns used and why
- Thread-safety considerations
- Extensibility ("How would you add X?")
- Full, runnable Java code (with a driver / demo class)
- Unit tests for the core logic
- Follow-up questions the interviewer will ask
- Common mistakes
