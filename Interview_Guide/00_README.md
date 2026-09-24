# Interview Guide: Questions, Answers, Code & Diagrams

A study guide for **SDE-1 / SDE-2 Java backend** interviews at FAANG and top product companies. Every file contains real interview questions with full technical answers, working Java / SQL / YAML code, and Mermaid diagrams (flowcharts, sequence, class, ER, state).

> The prompts in [the prompt library](../Interview_Structure/00_README/00_README.md) generate *more* content on demand. This folder is the ready-to-study answer bank.

**Legend:** 🟢 Beginner · 🟡 Intermediate · 🔴 Advanced · ⚫ FAANG · 🔥 very frequently asked · `Must-know SDE-1` vs `Stretch`

**Answer format:** Why they ask → Short answer (say this first) → Detailed answer (code, diagrams) → Common mistakes → Follow-ups

---

## Real interview questions (from your screenshot) and where they're answered

| # | Question | Answer |
|---|----------|--------|
| 1-2 | Race condition, and how to prevent it | [03 · Q1-Q2](./03_Concurrency_and_Multithreading/03_Concurrency_and_Multithreading.md) |
| 3 | `putIfAbsent` vs `computeIfAbsent` | [03 · Q3](./03_Concurrency_and_Multithreading/03_Concurrency_and_Multithreading.md) |
| 4-5 | Two synchronized methods but still a race: why, and how to investigate | [03 · Q4](./03_Concurrency_and_Multithreading/03_Concurrency_and_Multithreading.md) |
| 6 | When to use a big HashMap (sizing, memory, alternatives) | [03 · Q5b](./03_Concurrency_and_Multithreading/03_Concurrency_and_Multithreading.md) |
| 7-8 | HashMap deadlock / infinite loop (Java 7 vs 8) | [03 · Q6](./03_Concurrency_and_Multithreading/03_Concurrency_and_Multithreading.md) |
| 9-10 | ConcurrentHashMap and null keys/values: why not | [03 · Q7](./03_Concurrency_and_Multithreading/03_Concurrency_and_Multithreading.md) |
| 11 | 100 ms → 4-5 s: investigate | [11a · Section 1](./11_Production_Debugging_and_Observability/11a_Production_Debugging_and_Observability.md) |
| 12 | Slow API: database side and indexes | [11a · Section 1](./11_Production_Debugging_and_Observability/11a_Production_Debugging_and_Observability.md), [06a · Section 1](./06_SQL_and_Database_Internals/06a_SQL_Concepts_and_DB_Internals.md) |
| 13-14 | Shared HashMap fails in prod / causes timeouts | [03 · Q8-Q9](./03_Concurrency_and_Multithreading/03_Concurrency_and_Multithreading.md), [11a](./11_Production_Debugging_and_Observability/11a_Production_Debugging_and_Observability.md) |
| 15 | All requests time out but the server is up | [11a · Section 1](./11_Production_Debugging_and_Observability/11a_Production_Debugging_and_Observability.md) |
| 16 | Scenarios that cause concurrency issues | [03 · Q10](./03_Concurrency_and_Multithreading/03_Concurrency_and_Multithreading.md) |
| 17 | GitHub branching strategies and branch protection rules | [13a · Section 1](./13_Git_GitHub_and_CICD/13a_Git_GitHub_CICD.md) |
| 18 | PostgreSQL vs MySQL MVCC | [06a · Section 1](./06_SQL_and_Database_Internals/06a_SQL_Concepts_and_DB_Internals.md) |
| 19 | CascadeType vs orphanRemoval | [05 · Q1](./05_JPA_Hibernate_and_Transactions/05_JPA_Hibernate_Transactions.md) |
| 20 | Database-level locking in code | [05 · Q2](./05_JPA_Hibernate_and_Transactions/05_JPA_Hibernate_Transactions.md), [06a](./06_SQL_and_Database_Internals/06a_SQL_Concepts_and_DB_Internals.md) |

---

## Files

### 🔥 Tier 1: Must-do

| Subject | Files |
|---------|-------|
| Resume & projects (start here) | [01a Resume analysis & projects](./01_Resume_and_Projects/01a_Resume_Analysis_and_Projects.md) · [01b MAQ work, trap questions, resume rewrite](./01_Resume_and_Projects/01b_Resume_Analysis_and_Projects.md) |
| Plan (do second) | [23 Gap analysis, 30/60-day roadmaps, tracker](./23_Gap_Analysis_and_Roadmaps/23_Gap_Analysis_and_Roadmaps.md) |
| DSA (50% of your time) | [08a Arrays/Strings/Hashing](./08_DSA/08a_DSA_Foundations_Arrays_Strings_Hashing.md) · [08b Two Pointers/Sliding Window/Binary Search](./08_DSA/08b_DSA_Two_Pointers_Sliding_Window_Binary_Search.md) · [08c Linked List/Stack/Queue/Heap](./08_DSA/08c_DSA_LinkedList_Stack_Queue_Heap.md) · [08d Trees/BST/Trie](./08_DSA/08d_DSA_Trees_BST_Trie.md) · [08e Graphs/Union-Find](./08_DSA/08e_DSA_Graphs_UnionFind.md) · [08f DP/Backtracking/Greedy/Bits](./08_DSA/08f_DSA_DP_Backtracking_Greedy.md) · [08g Top 200 list](./08_DSA/08g_DSA_Top_200_Problem_List.md) |
| Core Java | [02a JVM, OOP, memory, GC, collections](./02_Core_Java_and_JVM/02a_Core_Java_and_JVM.md) · [02b APIs, modern Java, tricky outputs](./02_Core_Java_and_JVM/02b_Core_Java_and_JVM.md) |
| Concurrency | [03 Concurrency & multithreading](./03_Concurrency_and_Multithreading/03_Concurrency_and_Multithreading.md) |
| Spring Boot | [04a Spring Core, Boot & Security](./04_Spring_and_Microservices/04a_Spring_Core_Boot_Security.md) · [04b Microservices & API design](./04_Spring_and_Microservices/04b_Microservices_API_Design.md) |
| JPA / Hibernate | [05 JPA, Hibernate & transactions](./05_JPA_Hibernate_and_Transactions/05_JPA_Hibernate_Transactions.md) |
| SQL | [06a Concepts & DB internals](./06_SQL_and_Database_Internals/06a_SQL_Concepts_and_DB_Internals.md) · [06b 100 SQL problems, part 1](./06_SQL_and_Database_Internals/06b_SQL_100_Practice_Questions.md) · [06c part 2](./06_SQL_and_Database_Internals/06c_SQL_100_Practice_Questions.md) |
| LLD / machine coding | [10a Fundamentals & all design patterns](./10_LLD_and_Machine_Coding/10a_LLD_Fundamentals_and_Design_Patterns.md) · [10b Tier 1: Parking Lot, Tic-Tac-Toe, Snake & Ladder, LRU/LFU, Rate Limiter](./10_LLD_and_Machine_Coding/10b_LLD_Problems_Tier1.md) · [10c Tier 1: Logger, Vending Machine, ATM, Library, Elevator](./10_LLD_and_Machine_Coding/10c_LLD_Problems_Tier1_Continued.md) · [10d Tier 2 part 1](./10_LLD_and_Machine_Coding/10d_LLD_Problems_Tier2_Part1.md) · [10e Tier 2 part 2](./10_LLD_and_Machine_Coding/10e_LLD_Problems_Tier2_Part2.md) · [10f Concurrency LLD, schema, refactoring](./10_LLD_and_Machine_Coding/10f_LLD_Concurrency_Schema_Refactoring.md) |
| GenAI / RAG (your projects) | [17 GenAI, RAG, LangChain, pgvector, Python](./17_GenAI_RAG_LangChain_and_VectorDB/17_GenAI_RAG_LangChain_VectorDB.md) |
| CS fundamentals | [24a Operating Systems](./24_CS_Fundamentals/24a_OS.md) · [24b Computer Networks](./24_CS_Fundamentals/24b_Networks.md) · [24c DBMS](./24_CS_Fundamentals/24c_DBMS.md) |
| Behavioral | [19 HR & behavioral (STAR, Amazon LPs)](./19_HR_and_Behavioral/19_HR_and_Behavioral.md) |

### 🟡 Tier 2: Important

| Subject | Files |
|---------|-------|
| System design (HLD) | [09a Framework, estimation, building blocks I](./09_System_Design_HLD/09a_HLD_Framework_Estimation_Building_Blocks.md) · [09b Building blocks II](./09_System_Design_HLD/09b_HLD_Building_Blocks_II.md) · [09c Tier 1 case studies](./09_System_Design_HLD/09c_HLD_Case_Studies_Tier1.md) · [09d Tier 1 continued](./09_System_Design_HLD/09d_HLD_Case_Studies_Tier1_Continued.md) · [09e Tier 2 part 1](./09_System_Design_HLD/09e_HLD_Case_Studies_Tier2_Part1.md) · [09f Tier 2 part 2 & AI systems](./09_System_Design_HLD/09f_HLD_Case_Studies_Tier2_Part2_and_AI.md) |
| Production debugging | [11a Seed questions & runbooks](./11_Production_Debugging_and_Observability/11a_Production_Debugging_and_Observability.md) · [11b Evidence reading & observability](./11_Production_Debugging_and_Observability/11b_Production_Debugging_and_Observability.md) |
| Current job | [18 Power BI / Fabric, framed for backend](./18_PowerBI_Fabric_and_Data/18_PowerBI_Fabric_Data.md) |
| Caching | [07 Redis & caching](./07_Redis_and_Caching/07_Redis_and_Caching.md) |
| Git & CI/CD | [13a](./13_Git_GitHub_and_CICD/13a_Git_GitHub_CICD.md) · [13b](./13_Git_GitHub_and_CICD/13b_Git_GitHub_CICD.md) |
| Testing & security | [15 Testing](./15_Testing/15_Testing.md) · [16 Security](./16_Security/16_Security.md) |
| MCP & AI | [25 MCP & agentic AI](./25_MCP_and_Agentic_AI/25_MCP_and_Agentic_AI.md) · [26 AI-assisted coding interviews](./26_AI_Assisted_Coding_Interviews/26_AI_Assisted_Coding_Interviews.md) |
| Companies | [20a Global / FAANG](./20_Company_Question_Bank/20a_Company_Question_Bank_Global_FAANG.md) · [20b Product, fintech, India](./20_Company_Question_Bank/20b_Company_Question_Bank_Product_Fintech_India.md) |

### ⚪ Tier 3: Optional for SDE-1

| Subject | Files |
|---------|-------|
| Messaging | [12 Kafka & RabbitMQ](./12_Messaging_Kafka_RabbitMQ/12_Messaging_Kafka_RabbitMQ.md) |
| Docker / K8s / Cloud | [14a](./14_Docker_Kubernetes_and_Cloud/14a_Docker_Kubernetes_Cloud.md) · [14b](./14_Docker_Kubernetes_and_Cloud/14b_Docker_Kubernetes_Cloud.md) |
| Strategy & negotiation | [21 Interview strategy & negotiation](./21_Interview_Strategy_and_Negotiation/21_Interview_Strategy_and_Negotiation.md) |

**Mock interviews:** use the prompt [22_Mock_Interview.md](../Interview_Structure/22_Mock_Interview/22_Mock_Interview.md) weekly.

---

## How to study

1. **Read the short answer and say it out loud.** Then read the detailed answer, and redraw the diagrams from memory.
2. **DSA and LLD: write the code yourself before reading the solution.** Use a timer (DSA: 25-35 min, machine coding: 90 min).
3. **Fill every `[placeholder]`** in the resume and HR files with your real numbers and details. Never quote a number you can't defend.
4. **Verify company-specific claims** (file 20) on LeetCode company tags, LeetCode Discuss, Glassdoor, and with your recruiter.
5. **Revise weekly** using the Revision Cheat Sheet and Flashcards at the end of each file.

> **Diagrams:** Mermaid renders in VS Code's Markdown preview (built-in in recent versions; otherwise install *Markdown Preview Mermaid Support*) and on GitHub.
