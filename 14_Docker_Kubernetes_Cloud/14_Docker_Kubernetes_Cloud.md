# 14 · Docker, Kubernetes & Cloud (Azure / AWS)

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

Prepare me for Docker, Kubernetes, and Cloud interviews. Relate to my **Dockerized AI Email Reply Assistant** and my **Azure** experience.

> **Priority for SDE-1:** Docker is on my resume, so go deep there. Kubernetes and Cloud are **basics only** (what each component does and why), with advanced topics tagged [Stretch].

### Docker

- Image vs container, layers, caching
- Dockerfile best practices, multi-stage builds, image size reduction
- ENV vs ARG, managing secrets (why not to bake secrets into images)
- Volumes, networks, Docker Compose
- JVM inside containers (`-XX:MaxRAMPercentage`, container awareness)

### Kubernetes Basics

- Pods, ReplicaSets, Deployments, Services (ClusterIP / NodePort / LoadBalancer), Ingress
- ConfigMap vs Secret
- Liveness vs readiness vs startup probes
- HPA, resource requests and limits
- Rolling updates and rollbacks
- Debugging: OOMKilled, CrashLoopBackOff, ImagePullBackOff, Pending pods

### Cloud (Azure first, then AWS equivalents)

- Compute: App Service, AKS, Azure Functions (EC2, EKS, Lambda)
- Storage: Blob Storage (S3)
- Databases: Azure SQL, Azure Database for PostgreSQL, Cosmos DB (RDS, DynamoDB)
- Messaging: Service Bus, Event Hubs (SQS, SNS, Kinesis)
- Security: Key Vault, Managed Identity, Entra ID (Secrets Manager, IAM)
- Monitoring: Azure Monitor, Application Insights (CloudWatch)

### Provide

- Interview Questions (🟢 → ⚫)
- Answers
- Production Scenarios
- Sample Dockerfile (multi-stage, Spring Boot) and Kubernetes Deployment YAML
