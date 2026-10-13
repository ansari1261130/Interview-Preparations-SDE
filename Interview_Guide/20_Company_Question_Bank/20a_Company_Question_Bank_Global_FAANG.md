# 20a · Company Question Bank — Global and FAANG-style loops

This company question bank is rebuilt from public interview-prep sources and intentionally hedged: processes change by recruiter, team, location, level, and hiring market. Treat every company section as a focused practice map, then verify the latest round plan before scheduling.

🟢 Beginner · 🟡 Intermediate · 🔴 Advanced · ⚫ FAANG · 🔥 = very frequently asked

## Table of Contents
- [Beginner guide to this file](#beginner-guide-to-this-file)
- [How to verify public company claims](#q1-how-should-mohd-verify-public-company-interview-claims)
- [Q2. Google](#q2-how-should-mohd-prepare-for-google)
- [Q3. Amazon](#q3-how-should-mohd-prepare-for-amazon)
- [Q4. Microsoft](#q4-how-should-mohd-prepare-for-microsoft)
- [Q5. Meta](#q5-how-should-mohd-prepare-for-meta)
- [Q6. Apple](#q6-how-should-mohd-prepare-for-apple)
- [Q7. Netflix](#q7-how-should-mohd-prepare-for-netflix)
- [Q8. Uber](#q8-how-should-mohd-prepare-for-uber)
- [Q9. Airbnb](#q9-how-should-mohd-prepare-for-airbnb)
- [Q10. Adobe](#q10-how-should-mohd-prepare-for-adobe)
- [Q11. Atlassian](#q11-how-should-mohd-prepare-for-atlassian)
- [Revision Cheat Sheet](#revision-cheat-sheet)
- [Flashcards](#flashcards)

## Beginner guide to this file

### What this file teaches

This file teaches how to convert public reports about Facebook/Meta, Amazon, Apple, Netflix, and Google
(FAANG-style) and other global company loops into a practical preparation plan. Interviewers ask company-fit
questions because they want to see whether you understand the role, level, and evaluation signals instead of
blindly copying a stale interview post. In backend work, this same skill appears when you translate changing
requirements, public docs, or incident reports into safe engineering priorities without overclaiming.

### Mental model

Treat public interview reports like a weather forecast, not a contract: they suggest where to prepare, but the
recruiter and current job description decide the actual route.

- Source quality tells you how much confidence to place in a report.
- Level matters: Software Development Engineer level 1 (SDE-1) signals differ from level 2 (SDE-2).
- Data Structures and Algorithms (DSA) patterns are practice priorities, not guaranteed questions.
- Low-Level Design (LLD) and High-Level Design (HLD) scope depends heavily on role and team.
- Behavioral rubrics are company-specific, but honest Situation, Task, Action, Result (STAR) evidence travels.
- Verification keeps you from claiming a company process, compensation rule, or outcome as fact.

### How to think through any question here

1. Start with the official recruiter packet, job description, and role level; those override public reports.
2. Cluster public reports by same country, level, role family, and recency before drawing a pattern.
3. Translate each signal into practice: coding pattern, design topic, project story, or behavioral story.
4. Separate certainty from preparation value: say what is reported, then what you will practice.
5. Ask the recruiter verification questions about round names, tools, design scope, and evaluation rubric.
6. For SDE-1, protect fundamentals first; for SDE-2, add design, ownership, and trade-off depth.
7. Keep a change log so new public information updates priorities without rewriting your whole plan.

### Pattern recognition cues

| If the question mentions... | Think about... | Why it fits |
|---|---|---|
| Recent public reports | Verify recency, country, level, and role | Reports are useful but changeable |
| Coding-heavy company | DSA speed, edge cases, and complexity talk | Coding rounds reward reliable execution |
| Values or culture loop | STAR story mapped to the company rubric | Behavioral evidence can decide close calls |
| SDE-2 or experienced role | LLD, HLD, project depth, and trade-offs | Higher levels test ownership, not only syntax |
| Team-specific company | Product/domain research plus fundamentals | Round content can vary by team |
| Marketplace or consumer product | Data model, trust, ranking, and reliability | Product systems have user and business trade-offs |
| Enterprise platform | APIs, permissions, integration, and supportability | Enterprise users need stable contracts |
| Recruiter conflict with public post | Follow the recruiter and record the change | Current process beats stale anecdotes |

### Abbreviations and key terms

| Short form | Expanded form | Plain meaning |
|---|---|---|
| A/B | A/B test | Controlled comparison between two product or system variants |
| ACID | Atomicity, Consistency, Isolation, Durability | Transaction guarantees for relational databases |
| AI | Artificial Intelligence | Software behavior that reasons, predicts, or generates |
| API | Application Programming Interface | Contract for software-to-software communication |
| BFS | Breadth-First Search | Graph or tree traversal level by level |
| BI | Business Intelligence | Reporting and analytics work |
| CAP | Consistency, Availability, Partition tolerance | Distributed-system trade-off framework |
| CIDR | Classless Inter-Domain Routing | Network address range notation |
| CRUD | Create, Read, Update, Delete | Basic data operations in applications |
| CS | Computer Science | Core computing fundamentals |
| DB | Database | Persistent data store |
| DBMS | Database Management System | Software that manages databases |
| DFS | Depth-First Search | Graph or tree traversal going deep before wide |
| DP | Dynamic Programming | Solving overlapping subproblems with stored results |
| DSA | Data Structures and Algorithms | Coding-interview problem-solving toolkit |
| ETA | Estimated Time of Arrival | Expected completion time |
| ETag | Entity Tag | HTTP caching/version identifier |
| FAANG | Facebook/Meta, Amazon, Apple, Netflix, Google | Shorthand for large selective technology companies |
| GC | Garbage Collection | Automatic memory cleanup in managed runtimes |
| GenAI | Generative Artificial Intelligence | AI that creates content such as text or code |
| HLD | High-Level Design | System architecture and scale design |
| HR | Human Resources | Recruiting and employment-process function |
| HTTP | Hypertext Transfer Protocol | Protocol used by web APIs |
| IDE | Integrated Development Environment | Coding tool with editor, build, and debugging support |
| IP | Internet Protocol | Network addressing protocol |
| JPA | Java Persistence API | Java specification for object-relational persistence |
| JVM | Java Virtual Machine | Runtime that executes Java bytecode |
| L3, L4, E4 | Level 3, Level 4, Engineer 4 | Company-specific level labels; verify with recruiter |
| LLD | Low-Level Design | Class, object, and component-level design |
| LP | Leadership Principle | Culture rubric item, especially in Amazon-style loops |
| LRU | Least Recently Used | Cache eviction rule removing the least recently used item |
| MAQ | MAQ Software | Employer name in the resume context |
| ML | Machine Learning | Systems that learn patterns from data |
| NoSQL | Not Only SQL | Non-relational database family |
| OA | Online Assessment | Timed remote coding or aptitude assessment |
| OOP | Object-Oriented Programming | Modeling behavior with classes and objects |
| OS | Operating System | Software managing processes, memory, files, and devices |
| PDF | Portable Document Format | Common document file format |
| Qn. | Question number | Numbered question heading |
| RAG | Retrieval-Augmented Generation | AI pattern that retrieves context before generating output |
| REST | Representational State Transfer | Resource-oriented API style using HTTP |
| SaaS | Software as a Service | Hosted product delivered over the internet |
| SDK | Software Development Kit | Library and tools for building on a platform |
| SDE | Software Development Engineer | Engineering role title |
| SLO | Service-Level Objective | Reliability target for a service |
| SOLID | Single responsibility, Open-closed, Liskov substitution, Interface segregation, Dependency inversion | Object-oriented design principles |
| SQL | Structured Query Language | Relational database query language |
| STAR | Situation, Task, Action, Result | Behavioral answer structure |
| TCP | Transmission Control Protocol | Reliable transport protocol |
| TLS | Transport Layer Security | Encryption protocol for network traffic |
| TTL | Time To Live | Expiration time for cached or networked data |
| UDP | User Datagram Protocol | Lightweight transport protocol without delivery guarantees |
| URL | Uniform Resource Locator | Address of a web resource |
| US | United States | Country reference; location affects process reports |
| UX | User Experience | How a user experiences a product |
| YOE | Years of Experience | Work-experience count |

### Common beginner traps

- Saying a company always asks a topic because one public post mentioned it.
- Practicing only company-tagged problems and skipping the underlying pattern.
- Ignoring recruiter guidance when it conflicts with online anecdotes.
- Preparing senior system design depth while SDE-1 coding fundamentals are still weak.
- Copying culture language without an honest STAR story behind it.
- Turning public compensation or process rumors into claims instead of verification questions.

### How to study this file

1. Read the short answer to understand the recommended priority for each company.
2. Use the detailed answer to build a practice checklist: DSA, design, project, and behavioral items.
3. Turn every public-report statement into a verification question for the recruiter.
4. Practice the listed problem families, then explain why the pattern fits instead of memorizing titles.
5. Use flashcards for recall and active mocks for timing, communication, and follow-up depth.
6. Recheck sources before a real loop because public reports and company processes change.
7. Apply one structure and one level rule to every Coach's note: answer with source signal, role level,
   practice priority, and recruiter verification question; for Software Development Engineer level 1 (SDE-1),
   protect coding and fundamentals; for SDE-2, add Low-Level Design (LLD) and High-Level Design (HLD) scope,
   ownership stories, and trade-off depth without claiming any process is guaranteed.

## How to verify public company claims

### Q1. How should Mohd verify public company interview claims?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Company processes change quickly; accuracy matters more than copying stale interview-experience posts.

**Coach's note:** Interviewers evaluate source judgment: can you use public reports without treating them as
official policy? Use structure: what the report says, why it may vary, what to practice, and what to verify
with the recruiter. Beginner mistake: copying a stale post into a claim about the company. For SDE-1, verify
coding format and tools; for SDE-2, also verify LLD/HLD depth, project expectations, and behavioral rubric.

**Short answer (say this first, 20-40 sec):**
Use this guide as a preparation map, not a guarantee. Before each interview, ask the recruiter for round names, expected topics, coding environment, design scope, and behavioral rubric. Then cross-check public reports across LeetCode company tags, LeetCode Discuss, Glassdoor, GeeksforGeeks interview experiences, Interviewing.io/company guides, and official recruiter prep docs.

**Detailed answer:**
- Start with official recruiter material and job-description signals; those override public posts.
- Use LeetCode company tags only to identify patterns; premium tags can be stale or level-mixed.
- Read 5-10 recent LeetCode Discuss / Glassdoor / GeeksforGeeks experiences for the same location and level.
- Prefer recent India SDE-1/SDE-2 reports over US senior-staff reports.
- If a source says "asked frequently", translate that into "practice this pattern", not a probability claim.
- Verify whether LLD/HLD is actually in scope; many junior FAANG loops remain coding-heavy.

**Round-type comparison:**

| Company | Coding/OA | LLD / machine coding | HLD / system design | Behavioral signal |
|---|---|---|---|---|
| Google | Coding-heavy | Usually no for L3; possible L4+ | Rare for L3 | Googleyness, collaboration |
| Amazon | OA + coding + LP loop | SDE2+ or basic OOP for SDE1 | Possible/project-dependent | 16 Leadership Principles; bar raiser |
| Microsoft | Coding + fundamentals + as-appropriate | Team/level dependent | Possible | Growth mindset, customer focus |
| Meta | Fast coding; often 2 problems/round | E4+ more likely | Less common than coding for junior | Impact, move fast; verify AI-tool policy |
| Apple | Team-specific technical depth | Team-dependent | Possible | Craftsmanship, privacy, product quality |
| Netflix | Experienced-hire heavy | Often practical design | Strong for many roles | Freedom/responsibility culture |
| Uber | Coding + machine coding | Common in India reports | SDE2+ | Marketplace/logistics ownership |
| Airbnb | Coding + product/culture | Possible | Possible | Empathy, trust, marketplace thinking |
| Adobe | DSA + Java/CS | Possible | SDE2+ | Creative product quality |
| Atlassian | Coding + code design + values | Common | Possible | Public values interview |
| Salesforce | Java/backend + DSA | Possible | SDE2+ | Customer success, enterprise trust |
| Walmart Global Tech | DSA + machine coding | Common | SDE2+ | Retail-scale practicality |
| Mastercard | Java/DB/security fundamentals | Possible | Less for SDE1 | Payments correctness/trust |
| Visa | Java/SQL/security | Possible | Possible | Secure reliable payments |
| American Express | OA + CS fundamentals | Less common for SDE1 | Possible for experienced | Communication + logic |
| LinkedIn | Coding + graph/search design | Possible | SDE2+ | Professional-network impact |
| Flipkart | DSA + machine coding | Very common | SDE2+ | E-commerce scale |
| Swiggy | DSA + machine coding | Common | SDE2+ | Real-time operations |
| Zomato | DSA + backend/machine coding | Common | SDE2+ | Fast execution + ownership |

**Common mistakes:**
- Assuming one viral interview experience represents the whole company.
- Listing LeetCode numbers from memory when unsure; this guide includes numbers only for well-known titles.
- Ignoring behavioral prep at companies where values interviews can decide the outcome.

**Follow-ups:**
- *What if recruiter guidance conflicts with this guide?* → Follow the recruiter guidance.
- *How recent should public reports be?* → Prefer the last 6-18 months, same country, same level, same role family.

## Google

### Q2. How should Mohd prepare for Google?
`⚫ FAANG` `🔥` `Must-know SDE-1/SDE-2`

**Why they ask:** Google prep is easy to dilute with design and trivia, but junior loops are usually coding-heavy.

**Coach's note:** Interviewers evaluate whether you can turn Google public reports into a focused, verified
practice plan. For this section, translate reports into DSA speed, edge-case discipline, complexity
explanation, and Googleyness-style collaboration stories. Beginner mistake: saying Google always asks one
fixed list of problems or ignoring recruiter guidance.

**Short answer (say this first, 20-40 sec):**
Commonly reported: recruiter screen, coding phone screen or OA, multiple coding-heavy interviews, and Googleyness. System design is more common at L4+/SDE-2-like loops and uncommon for pure L3 new-grad loops. Mohd should prioritize DSA speed, crisp complexity analysis, and STAR stories about SQL tuning, automation, and RAG search.

**Detailed answer:**

| Round | Commonly reported format | Mohd's angle |
|---|---|---|
| Recruiter | Role fit, level, timeline, process packet | Ask whether design is in scope |
| Coding screen/OA | One or two algorithmic problems | Use clean Java 17 and edge-case tests |
| Virtual onsite coding | Arrays, graphs, trees, DP, recursion, binary search | Derive brute force, then optimize |
| Googleyness | Collaboration, ambiguity, humility, impact | STAR stories from MAQ and projects |
| Design if level-appropriate | Practical scalable design for L4+ | Clarify SLOs, APIs, scale, consistency |

**Commonly reported DSA practice set (LeetCode tags/discuss lists; verify recency):** Longest Substring Without Repeating Characters (3), Median of Two Sorted Arrays (4), Regular Expression Matching (10), Trapping Rain Water (42), Merge Intervals (56), Minimum Window Substring (76), Word Ladder (127), Number of Islands (200), Serialize and Deserialize Binary Tree (297), Decode String (394), Evaluate Division (399), Alien Dictionary (269).

**LLD / machine-coding prompts:** autocomplete trie with ranking; calendar booking conflict checker; in-memory document index; rate limiter library; URL shortener classes.

**HLD prompts:** Google Docs collaboration core; search autocomplete service; YouTube-like video metadata; geo-distributed cache invalidation; RAG semantic search service.

**Java/backend/CS questions to rehearse:**
- Explain HashMap collision handling and resizing in Java.
- Compare BFS, DFS, Dijkstra, and union-find.
- How would you debug a Java service with p99 latency spikes?
- JVM heap vs stack, GC roots, and common leaks.
- SQL index selection for prefix search vs full-text search.
- How do you make a REST endpoint idempotent?
- Concurrency: happens-before, volatile, synchronized.
- How would you test algorithmic edge cases?

**Behavioral focus:** Googleyness: structured collaboration, learning from feedback, user focus, and principled trade-offs.

**What they value:** Clear thinking under ambiguity, optimal algorithms, readable code, humility, and scale-aware engineering.

**5 preparation tips:**
1. Do 45-minute timed DSA with verbal reasoning.
2. Practice trees, graphs, and DP until complexity analysis is automatic.
3. Confirm if LLD/HLD is in scope; do not over-prepare design for L3.
4. Use examples before code; Google interviewers value clean reasoning.
5. Prepare one STAR each for ambiguity, conflict, failure, and user impact.

**Tailored "why this company" line for Mohd:** I want Google because my Java backend, SQL-performance, and RAG search work map to products where clean algorithms and scalable information retrieval affect billions of users.

**Common mistakes:**
- Treating public tagged problems as guaranteed interview questions.
- Over-indexing on HLD for junior loops.
- Giving vague behavioral answers without metrics or trade-offs.

**Follow-ups:**
- *Should Mohd memorize these exact DSA problems?* → No; use them to learn patterns.
- *What should Mohd verify?* → Round count, coding platform, design scope, and behavioral rubric.

## Amazon

### Q3. How should Mohd prepare for Amazon?
`⚫ FAANG` `🔥` `Must-know SDE-1/SDE-2`

**Why they ask:** Amazon is one of the clearest examples where behavioral evidence can weigh as much as coding.

**Coach's note:** Interviewers evaluate whether you can turn Amazon public reports into a focused, verified
practice plan. For this section, translate reports into DSA plus Leadership Principle stories with verified
STAR evidence from SQL tuning, automation, or projects. Beginner mistake: reciting principles without a story
or inventing metrics for impact.

**Short answer (say this first, 20-40 sec):**
Commonly reported: recruiter/OA, coding rounds, technical design or project deep dive depending on level, and a loop anchored in Leadership Principles. A bar-raiser-style round is often behavioral-heavy. Mohd should prepare DSA plus 8-10 STAR stories mapped to the 16 LPs.

**Detailed answer:**

| Round | Commonly reported format | Mohd's angle |
|---|---|---|
| Recruiter | Leveling, OA expectations, LP prep | Ask how many coding vs LP rounds |
| Online assessment | DSA, debugging/work simulation, work-style questions | Optimize, test, and document edge cases |
| Coding interviews | Strings, trees, heaps, graphs, data structures | State time/space and production edge cases |
| Design/project | SDE2 system design; SDE1 OOP/project scale-up | Tie SQL/RAG designs to availability and cost |
| Bar raiser/behavioral | 16 Leadership Principles using STAR | Use metrics, trade-offs, failures, ownership |

**Commonly reported DSA practice set:** Two Sum (1), Merge k Sorted Lists (23), Copy List with Random Pointer (138), LRU Cache (146), Number of Islands (200), Serialize and Deserialize Binary Tree (297), Top K Frequent Elements (347), K Closest Points to Origin (973), Reorder Data in Log Files (937), Search Suggestions System (1268), Critical Connections in a Network (1192), Word Ladder (127).

**LLD / machine-coding prompts:** locker/package pickup system; parking lot with pricing and tickets; API-gateway rate limiter; shopping cart and coupon engine; in-memory cache with TTL and eviction; Splitwise-style expense sharing.

**HLD prompts:** Amazon product search; order management and inventory reservation; Prime Video watch history; shipment notification service; multi-region cart service.

**Java/backend/CS questions:**
- How does ConcurrentHashMap differ from HashMap?
- Design an idempotent order API with retries.
- Explain optimistic vs pessimistic locking in JPA.
- SQL isolation levels and lost-update prevention.
- Trace a 5xx spike in a Spring Boot service.
- What happens when an index is added to a high-write table?
- Queue vs stream vs pub/sub.
- Handling partial failure in a microservice call.

**Behavioral focus:** Customer Obsession, Ownership, Dive Deep, Bias for Action, Learn and Be Curious, Earn Trust, Deliver Results; know all 16 LPs.

**What they value:** Ownership, measurable impact, frugality, customer-first decisions, and strong behavioral evidence.

**5 preparation tips:**
1. Prepare 8-10 STAR stories with metrics and failure learnings.
2. For every code answer, discuss edge cases before submitting.
3. Practice one retail/order/search design weekly.
4. Do not give vague behavioral answers; follow-ups dig deeply.
5. Link Mohd's 70% SQL tuning and 90% automation impact to LPs.

**Tailored "why this company" line for Mohd:** I want Amazon because its ownership culture matches my SQL optimization and automation work, where backend changes directly improve customer-facing reliability and operational speed.

**Common mistakes:**
- Preparing only DSA and neglecting LP stories.
- Describing team achievements without personal ownership.
- Forgetting to explain failure, learning, and measurable result.

**Follow-ups:**
- *Which LP stories should Mohd prioritize?* → Customer Obsession, Ownership, Dive Deep, Bias for Action, Deliver Results.
- *Is HLD guaranteed?* → No; verify level and recruiter packet.

## Microsoft

### Q4. How should Mohd prepare for Microsoft?
`⚫ FAANG` `🔥` `Must-know SDE-1/SDE-2`

**Why they ask:** Microsoft loops vary by team, so preparation must cover coding, fundamentals, project depth, and values.

**Coach's note:** Interviewers evaluate whether you can turn Microsoft public reports into a focused, verified
practice plan. For this section, translate reports into coding, fundamentals, project depth, and
growth-mindset stories matched to the specific team. Beginner mistake: assuming every Microsoft loop is
identical across teams and locations.

**Short answer (say this first, 20-40 sec):**
Commonly reported: recruiter, OA/phone screen, technical interviews covering coding and CS fundamentals, design or project discussion as appropriate, and a final manager/as-appropriate conversation. Mohd should connect Java/Spring, SQL, Azure/Fabric, and automation work to customer impact.

**Detailed answer:**

| Round | Commonly reported format | Mohd's angle |
|---|---|---|
| Recruiter | Team, role, stack, process | Ask if an as-appropriate or design round is planned |
| OA/screen | DSA plus debugging or language fundamentals | Code in Java and explain trade-offs |
| Technical 1 | Arrays, strings, linked lists, trees, recursion | Use whiteboard-friendly structure |
| Technical 2 | CS fundamentals, OOP, APIs, project | Discuss Spring Boot, SQL, tests, observability |
| As appropriate/manager | Growth mindset, collaboration, customer focus | STAR stories with learning and impact |

**Commonly reported DSA practice set:** Add Two Numbers (2), Valid Parentheses (20), Spiral Matrix (54), Merge Intervals (56), Binary Tree Zigzag Level Order Traversal (103), Clone Graph (133), Word Break (139), LRU Cache (146), Reverse Linked List (206), Course Schedule (207), Kth Largest Element in an Array (215), Design Tic-Tac-Toe (348).

**LLD prompts:** meeting scheduler with room availability; file system abstraction; library management system; elevator controller; tic-tac-toe/game engine; API rate limiter.

**HLD prompts:** Teams chat service; OneDrive file-sync metadata; notification platform; feature flag service; telemetry ingestion pipeline.

**Java/backend/CS questions:** interface vs abstract class; thread-pool sizing; Spring bean lifecycle; SQL execution plan basics; REST status codes; service-layer tests; OS process vs thread; TCP vs UDP and TLS basics.

**Behavioral focus:** Growth mindset, collaboration, customer empathy, inclusion, and learning from mistakes.

**What they value:** Pragmatic engineering, clear communication, reliable services, and team fit.

**5 preparation tips:**
1. Prepare project deep dives with simple architecture diagrams.
2. Review Java, Spring, SQL, OS, and networks.
3. Practice explaining trade-offs, not just final code.
4. Polish "why Microsoft" and "why this team".
5. Use MAQ dashboards, Power BI, Azure, and automation examples.

**Tailored "why this company" line for Mohd:** I want Microsoft because my Java backend, Azure/Fabric, Power BI, and automation experience align with building developer and enterprise products at large scale.

**Common mistakes:**
- Assuming Microsoft is only DSA.
- Not preparing a clean project architecture narrative.
- Giving behavioral answers without customer impact.

**Follow-ups:**
- *What does "as appropriate" mean?* → A later-stage round that can test fit, judgment, design, and team-level readiness.
- *Should Mohd discuss Power BI?* → Yes, when tying data performance and customer value to backend decisions.

## Meta

### Q5. How should Mohd prepare for Meta?
`⚫ FAANG` `🔥` `Stretch SDE-1/SDE-2`

**Why they ask:** Meta coding rounds are commonly reported as fast-paced and require high accuracy under time pressure.

**Coach's note:** Interviewers evaluate whether you can turn Meta public reports into a focused, verified
practice plan. For this section, translate reports into fast coding practice, clean communication, and
verified impact stories while checking current AI-tool policy. Beginner mistake: training only on speed and
skipping correctness, tests, or behavioral evidence.

**Short answer (say this first, 20-40 sec):**
Commonly reported: recruiter, coding screen, onsite coding rounds often expecting two problems per round, and behavioral. Product/system design is common at E4+ and above; AI-enabled coding interviews have been reported, so verify current rules before practicing.

**Detailed answer:**

| Round | Commonly reported format | Mohd's angle |
|---|---|---|
| Recruiter | Level, packet, coding expectations | Ask language, AI-tool policy, and design scope |
| Coding screen | Fast LeetCode-style problems | Target 20-25 minutes per problem |
| Onsite coding | Multiple rounds; bug-free implementation matters | Drill Meta-tagged mediums |
| Product/system design | Usually E4+; product sense or HLD | Clarify users, APIs, feed ranking, privacy |
| Behavioral | Impact, conflict, moving fast, ownership | Concise STAR with metrics |

**Commonly reported DSA practice set:** Merge Intervals (56), Simplify Path (71), Lowest Common Ancestor of a Binary Tree (236), Basic Calculator II (227), Random Pick with Weight (528), Subarray Sum Equals K (560), Valid Palindrome II (680), Accounts Merge (721), Minimum Remove to Make Valid Parentheses (1249), Dot Product of Two Sparse Vectors (1570), Binary Tree Vertical Order Traversal (314), Kth Largest Element in an Array (215).

**LLD prompts:** news-feed ranking component; comment moderation workflow; privacy-aware photo album sharing; Messenger retry queue; experiment assignment service.

**HLD prompts:** Facebook News Feed; Instagram Stories; Messenger presence; realtime comments/likes; ads event ingestion.

**Java/backend/CS questions:** hash table vs tree map; avoiding races in counters; consistent hashing; privacy-aware API design; p50/p95/p99 latency; Java memory model; feature rollout; SQL vs NoSQL for social graph queries.

**Behavioral focus:** Move fast with responsible judgment, focus on impact, direct communication, and learning from ambiguous product problems.

**What they value:** Speed plus correctness, strong coding throughput, scalable product thinking, and measurable impact.

**5 preparation tips:**
1. Solve two mediums in 45 minutes with no IDE crutches.
2. Drill parentheses, intervals, trees, arrays, hash maps.
3. Verify current AI-enabled coding rules before the interview.
4. Prepare concise impact stories; Meta rounds move quickly.
5. Do one product/HLD mock for feeds, messaging, or experimentation.

**Tailored "why this company" line for Mohd:** I want Meta because my GenAI/RAG and backend API work can contribute to large-scale personalized products where fast iteration and measurable impact matter.

**Common mistakes:** Spending too long on one problem; over-explaining without coding; ignoring recruiter AI-tool rules.

**Follow-ups:**
- *Is system design mandatory for SDE-1?* → Usually no; verify level-specific expectations.
- *How should Mohd handle speed pressure?* → State approach quickly, code cleanly, test with focused examples.

## Apple

### Q6. How should Mohd prepare for Apple?
`⚫ FAANG` `🔥` `Stretch`

**Why they ask:** Apple interviews are team-specific, so generic prep must be supplemented by product and domain research.

**Coach's note:** Interviewers evaluate whether you can turn Apple public reports into a focused, verified
practice plan. For this section, translate reports into team-specific domain research, fundamentals, privacy
awareness, and craftsmanship stories. Beginner mistake: giving generic FAANG prep without studying the product
or team area.

**Short answer (say this first, 20-40 sec):**
Commonly reported: recruiter, technical screens, team-specific coding/deep technical rounds, design or domain-specific engineering discussion, and behavioral. Mohd should prepare robust code, privacy/quality trade-offs, and precise backend fundamentals.

**Detailed answer:**

| Round | Commonly reported format | Mohd's angle |
|---|---|---|
| Recruiter | Team specificity: services, platform, data, ML, tools | Ask exact stack and domain depth |
| Coding screen | DSA plus clean implementation | Keep code simple and robust |
| Team technical | APIs, DBs, concurrency, debugging | Tie examples to reliability and UX |
| Design/domain | System or component design depending team | Show privacy, edge cases, quality |
| Behavioral | Product passion, craftsmanship, ownership | Explain why quality matters |

**Commonly reported DSA practice set:** Decode Ways (91), LRU Cache (146), Implement Trie (208), Reverse Linked List (206), Product of Array Except Self (238), Find Median from Data Stream (295), Design Hit Counter (362), Word Search (79), Valid Anagram (242), Alien Dictionary (269), Merge Intervals (56), Valid Parentheses (20).

**LLD prompts:** device notification preference manager; media playlist/library service; offline sync conflict resolver; feature-toggle client SDK; App Store review queue model.

**HLD prompts:** iCloud file metadata sync; push notification service; App Store search/ranking; crash analytics pipeline; privacy-preserving telemetry.

**Java/backend/CS questions:** reducing Java memory use; immutability and thread safety; zero-downtime schema migration; REST API versioning; intermittent bug debugging; encryption in transit vs at rest; cache eviction policies; JVM GC overview.

**Behavioral focus:** Craftsmanship, attention to detail, product ownership, confidentiality, and customer experience.

**What they value:** Quality, privacy, elegant design, and deep technical ownership.

**5 preparation tips:** Research the exact Apple team; prepare polished project stories; practice boundary checks; revise backend fundamentals beyond DSA; avoid generic product-fan answers.

**Tailored "why this company" line for Mohd:** I want Apple because my backend reliability, SQL tuning, and AI application work fit a culture that values polished systems and privacy-conscious user experiences.

**Common mistakes:** Not researching the team; hand-wavy privacy answers; untested code.

**Follow-ups:**
- *Should Mohd mention GenAI?* → Yes, but emphasize product quality, safety, privacy, and measurable utility.
- *What if the team is not Java?* → Translate principles: APIs, data modeling, performance, concurrency.

## Netflix

### Q7. How should Mohd prepare for Netflix?
`⚫ FAANG` `🔥` `Stretch`

**Why they ask:** Netflix often expects mature ownership and design judgment; junior-style preparation may be insufficient.

**Coach's note:** Interviewers evaluate whether you can turn Netflix public reports into a focused, verified
practice plan. For this section, translate reports into ownership, design maturity, reliability trade-offs,
and candid communication practice for experienced-style loops. Beginner mistake: claiming Netflix process
details as fixed or preparing only junior coding basics.

**Short answer (say this first, 20-40 sec):**
Commonly reported: recruiter, technical screens, coding/design/project rounds, and culture interview. Netflix tends to hire experienced engineers, so SDE-1-style openings are less common; Mohd should verify level expectations and prepare high-ownership stories.

**Detailed answer:**

| Round | Commonly reported format | Mohd's angle |
|---|---|---|
| Recruiter | Level, seniority, culture bar | Ask whether coding, design, or domain deep dive dominates |
| Technical screen | Coding or practical backend exercise | Show production thinking |
| Design | Distributed systems, reliability, service ownership | Discuss failure modes |
| Project deep dive | Past impact and decisions | Use SQL tuning/RAG ownership examples |
| Culture | Judgment, feedback, autonomy | Give mature examples of independent decisions |

**Commonly reported DSA practice set:** Maximum Subarray (53), LRU Cache (146), Binary Tree Right Side View (199), Course Schedule II (210), Serialize and Deserialize Binary Tree (297), Design Twitter (355), Insert Delete GetRandom O(1) (380), All O(1) Data Structure (432), Logger Rate Limiter (359), Design In-Memory File System (588), Top K Frequent Words (692), Word Break (139).

**LLD prompts:** streaming playback session tracker; content rating/review service; experiment configuration SDK; user watchlist service; rate limiter/logger service.

**HLD prompts:** video streaming metadata service; recommendation event pipeline; playback analytics ingestion; content delivery/cache invalidation; A/B testing platform.

**Java/backend/CS questions:** graceful degradation; Kafka consumer lag; cache-aside vs write-through; Java backpressure; database hot keys; observability; CAP trade-offs; validating ML/RAG features in production.

**Behavioral focus:** Judgment, feedback, autonomy, high ownership, and comfort with candid culture conversations.

**What they value:** Senior ownership, reliability, direct communication, and product-scale thinking.

**5 preparation tips:** Verify role level; prepare streaming/recommendation trade-offs; show mature ownership; practice failure-mode analysis; be candid but professional.

**Tailored "why this company" line for Mohd:** I want Netflix because my RAG and backend performance work align with data-driven content experiences and high-reliability distributed systems.

**Common mistakes:** Underestimating culture; weak design trade-offs; overselling experience beyond evidence.

**Follow-ups:**
- *Is Netflix realistic for ~1 YOE?* → Possible only for specific openings; verify level and expectations.
- *What should Mohd highlight?* → Reliability, measurable performance work, and learning speed.

## Uber

### Q8. How should Mohd prepare for Uber?
`🔴 Advanced` `🔥` `Must-know product-company`

**Why they ask:** Uber-style India prep often combines DSA, practical OOP, marketplace thinking, and real-time systems.

**Coach's note:** Interviewers evaluate whether you can turn Uber public reports into a focused, verified
practice plan. For this section, translate reports into marketplace, real-time systems, DSA, object modeling,
and logistics trade-offs. Beginner mistake: treating Uber-style prep as only LeetCode without marketplace
constraints.

**Short answer (say this first, 20-40 sec):**
Commonly reported: recruiter, coding screen/OA, DSA, machine coding or LLD, system design for SDE2+, and behavioral/project deep dive. Public India experiences mention OOP machine-coding rounds, so Mohd should practice timed Java designs.

**Detailed answer:**

| Round | Commonly reported format | Mohd's angle |
|---|---|---|
| Recruiter | Role, level, onsite structure | Ask if LLD machine coding is live and timed |
| Coding | Graphs, heaps, intervals, strings, geo-flavored variants | Explain test cases and optimize iteratively |
| Machine coding/LLD | Working OOP design in 60-90 minutes | Start with requirements, interfaces, tests |
| System design | SDE2+ marketplace/geospatial/realtime systems | Focus on matching, dispatch, latency |
| Behavioral | Projects, team conflict, ownership | Use MAQ and RAG trade-off stories |

**Commonly reported DSA practice set:** Merge Intervals (56), Word Break (139), LRU Cache (146), Word Search II (212), Meeting Rooms II (253), Reconstruct Itinerary (332), Design Snake Game (353), Find K Closest Elements (658), Bus Routes (815), Network Delay Time (743), Minimum Cost to Connect Sticks (1167), Design Underground System (1396).

**LLD prompts:** ride matching engine classes; cab booking fare calculator; device command controller; Splitwise/expense manager; parking lot or locker service; rate limiter.

**HLD prompts:** Uber ride matching; ETA service; surge pricing event pipeline; trip tracking; driver location ingestion.

**Java/backend/CS questions:** geohash/quadtree basics; thread-safe maps; booking idempotency; trip/rider/driver SQL schema; Kafka vs REST for location; duplicate events; executor service/backpressure; debugging delayed ride assignment.

**Behavioral focus:** Ownership, hustle with quality, stakeholder communication, conflict handling, and operational awareness.

**What they value:** Pragmatic coding, scalable marketplaces, real-time systems, and clear product trade-offs.

**5 preparation tips:** Practice 90-minute machine coding; clarify matching rules; review heaps/graphs/intervals/geospatial basics; build unit-testable services; prepare ambiguity-under-time-pressure story.

**Tailored "why this company" line for Mohd:** I want Uber because my backend and SQL optimization strengths fit real-time marketplace systems where latency, correctness, and operational metrics matter.

**Common mistakes:** Coding before clarifying requirements; ignoring concurrency; weak tests in machine coding.

**Follow-ups:**
- *What is the best Uber LLD drill?* → Ride matching with fare calculation and driver state transitions.
- *What backend topic matters most?* → Idempotency and realtime event handling.

## Airbnb

### Q9. How should Mohd prepare for Airbnb?
`🔴 Advanced` `🔥` `Stretch`

**Why they ask:** Airbnb-style prep blends DSA, product empathy, marketplace trust, and booking correctness.

**Coach's note:** Interviewers evaluate whether you can turn Airbnb public reports into a focused, verified
practice plan. For this section, translate reports into marketplace trust, booking correctness, product
empathy, DSA, and design trade-offs. Beginner mistake: discussing travel-product ideas without backend
correctness and failure cases.

**Short answer (say this first, 20-40 sec):**
Commonly reported: recruiter, coding phone screen, virtual onsite with coding, design/architecture, and culture. Mohd should practice intervals/date ranges, marketplace search, trust/safety, and customer-empathy stories.

**Detailed answer:**

| Round | Commonly reported format | Mohd's angle |
|---|---|---|
| Recruiter | Level and team fit | Ask whether take-home or host/product discussion appears |
| Coding screen | Algorithmic problem in shared editor | Communicate examples and edge cases |
| Coding onsite | Harder DSA, sometimes parsing/ranking | Focus on correctness and readability |
| Design/architecture | Marketplace/trust/search | Discuss users, abuse, ranking, availability |
| Values/culture | Belonging, empathy, teamwork | Use customer-impact stories |

**Commonly reported DSA practice set:** Regular Expression Matching (10), Text Justification (68), Minimum Window Substring (76), Meeting Rooms II (253), Alien Dictionary (269), Serialize and Deserialize Binary Tree (297), Nested List Weight Sum (339), Palindrome Pairs (336), Queue Reconstruction by Height (406), IP to CIDR (751), Pour Water (755), Design File System (1166).

**LLD prompts:** booking calendar availability engine; review/rating moderation model; coupon/promotion engine; search filter builder; reservation state machine.

**HLD prompts:** Airbnb booking platform; listing search/ranking; trust and safety review pipeline; pricing recommendation service; guest-host messaging.

**Java/backend/CS questions:** preventing double booking; indexing date ranges; reservation eventual consistency; immutable money/date value objects; pagination and filtering APIs; fraud/abuse rate limiting; SQL constraints for booking overlap; testing time-zone logic.

**Behavioral focus:** Hospitality mindset, empathy, inclusive collaboration, product taste, and trust trade-offs.

**What they value:** User empathy, clean design, marketplace correctness, and thoughtful product engineering.

**5 preparation tips:** Practice interval/date problems; prepare double-booking designs; use careful domain modeling; have one empathy story; review marketplace search/ranking/trust.

**Tailored "why this company" line for Mohd:** I want Airbnb because my backend, SQL, and RAG search experience can help build trustworthy marketplace discovery and booking systems.

**Common mistakes:** Ignoring time zones; failing to discuss trust/abuse; treating booking like a simple CRUD app.

**Follow-ups:**
- *What SQL concept is most useful?* → Range conflicts, constraints, and transaction isolation.
- *What behavioral story works well?* → A user-impact story where Mohd improved reliability or reduced manual work.

## Adobe

### Q10. How should Mohd prepare for Adobe?
`🟡 Intermediate` `🔥` `Must-know SDE-1/SDE-2`

**Why they ask:** Adobe India reports commonly mix DSA with Java/OOP, CS fundamentals, and project depth.

**Coach's note:** Interviewers evaluate whether you can turn Adobe public reports into a focused, verified
practice plan. For this section, translate reports into Java, object-oriented programming, CS fundamentals,
project depth, and product-quality stories. Beginner mistake: assuming Adobe is only design-brand knowledge
and skipping backend fundamentals.

**Short answer (say this first, 20-40 sec):**
Commonly reported: recruiter/OA, coding technical rounds, Java/OOP/CS fundamentals, LLD or project/design for experienced roles, and managerial/HR. Mohd should prepare DSA plus Java, DBMS, OS, networks, Spring, and a polished GenAI/API project deep dive.

**Detailed answer:**

| Round | Commonly reported format | Mohd's angle |
|---|---|---|
| OA | DSA, debugging, aptitude depending path | Prioritize medium accuracy |
| Technical 1 | Coding plus data structures | Explain approach and complexity |
| Technical 2 | Java, OOP, DBMS, OS, networks, project | Use backend project examples |
| Design/LLD | Component design or machine coding for SDE2/team | Write extensible Java classes |
| Managerial/HR | Team fit and project ownership | Prepare concise motivation |

**Commonly reported DSA practice set:** Valid Parentheses (20), Search in Rotated Sorted Array (33), Group Anagrams (49), Merge Intervals (56), Decode Ways (91), LRU Cache (146), Number of Islands (200), Course Schedule (207), Kth Largest Element in an Array (215), Implement Trie (208), Longest Increasing Subsequence (300), Word Break (139).

**LLD prompts:** document versioning service; PDF annotation/comment system; image-processing job queue; license/subscription manager; notification preference service.

**HLD prompts:** Creative Cloud asset sync; PDF conversion service; product-usage analytics ingestion; template search service; collaborative editing metadata.

**Java/backend/CS questions:** Java collections internals; JVM GC; ACID and isolation; Spring REST exception handling; SOLID; process vs thread/deadlock; HTTP caching/ETag; composite indexes.

**Behavioral focus:** Creative ownership, collaboration, product quality, and ability to explain technical choices.

**What they value:** Strong fundamentals, Java/OOP clarity, customer-focused quality, and project depth.

**5 preparation tips:** Revise Java + DBMS + OS; prepare document/media design; explain projects at code/DB/deployment levels; show clean OO design; use AI Email Reply Assistant for API/error handling.

**Tailored "why this company" line for Mohd:** I want Adobe because my Java backend and GenAI projects fit creative-product workflows where reliable APIs and intelligent content features matter.

**Common mistakes:** Only practicing DSA; weak Java fundamentals; shallow project explanations.

**Follow-ups:**
- *What should Mohd revise first?* → Java collections, OOP, SQL indexes, and Spring REST.
- *Is LLD guaranteed?* → No; verify by team and level.

## Atlassian

### Q11. How should Mohd prepare for Atlassian?
`🔴 Advanced` `🔥` `Must-know product-company`

**Why they ask:** Atlassian is known publicly for values-focused hiring and practical code/design discussions.

**Coach's note:** Interviewers evaluate whether you can turn Atlassian public reports into a focused, verified
practice plan. For this section, translate reports into values evidence, practical code design, collaboration,
and maintainable engineering trade-offs. Beginner mistake: quoting values without showing a verified behavior
from your own work.

**Short answer (say this first, 20-40 sec):**
Commonly reported: recruiter, coding screen, code design/LLD or practical coding, system/design collaboration, and values interview. Mohd should prepare maintainable Java designs plus stories mapped to Atlassian values.

**Detailed answer:**

| Round | Commonly reported format | Mohd's angle |
|---|---|---|
| Recruiter | Role, team, values round format | Ask whether code design, bug bash, or values round appears |
| Coding | DSA or practical coding | Narrate choices and tests |
| Code design/LLD | Build/extend a component | Favor small classes and interfaces |
| Design | SaaS collaboration tools, notifications, permissions | Discuss multi-tenancy and reliability |
| Values | Open company, build with heart, customer focus, team play, change | Use candid team-first STAR stories |

**Commonly reported DSA practice set:** Design Hit Counter (362), Find Duplicate File in System (609), Design HashMap (706), Subdomain Visit Count (811), Time Based Key-Value Store (981), Rank Teams by Votes (1366), LRU Cache (146), Merge Intervals (56), Top K Frequent Elements (347), Design File System (1166), Design Snake Game (353), Logger Rate Limiter (359).

**LLD prompts:** rate limiter library; feature flag evaluator; notification batching system; Jira ticket workflow engine; Confluence page permission model; code-review comment service.

**HLD prompts:** Jira issue tracker; Confluence collaborative pages; notification fanout; multi-tenant audit logging; marketplace app integration.

**Java/backend/CS questions:** multi-tenant schema design; strategy pattern for workflow states; REST pagination/filtering; caching permissions safely; collaborative edit concurrency; SaaS migrations; unit vs integration tests; respectful code review.

**Behavioral focus:** Atlassian values: openness, customer focus, teamwork, balance, and proactive change.

**What they value:** Collaborative design, maintainable code, customer empathy, and values alignment.

**5 preparation tips:** Read Atlassian values; practice evolving requirements; emphasize tests/refactoring; study multi-tenancy/permissions; communicate trade-offs openly.

**Tailored "why this company" line for Mohd:** I want Atlassian because my backend automation and data-quality work align with collaboration products where maintainable services improve team productivity.

**Common mistakes:** Ignoring values prep; writing monolithic machine-coding solutions; not explaining customer impact.

**Follow-ups:**
- *Which values are public?* → Open company, no bullshit; build with heart and balance; don't #@!% the customer; play as a team; be the change you seek.
- *What is the best LLD drill?* → Feature flag evaluator or Jira workflow engine with extensible rules.

## Revision Cheat Sheet

- Google/Meta: coding speed and algorithmic clarity dominate; verify design scope by level.
- Amazon: DSA plus Leadership Principles; STAR stories need metrics and deep follow-up readiness.
- Microsoft/Adobe: expect a blend of DSA, Java/backend, CS fundamentals, and project discussion.
- Apple/Netflix: team-specific loops; prepare quality, privacy, reliability, and mature ownership examples.
- Uber/Airbnb/Atlassian: combine DSA with practical design, product empathy, and machine-coding/code-design readiness.
- Always ask the recruiter: round names, coding platform, AI policy, LLD/HLD scope, and behavioral rubric.

## Flashcards

1. Q → What is the safest phrase for public process claims? A → "Commonly reported — verify with recruiter."
2. Q → Which Amazon dimension can override coding? A → Leadership Principles / bar-raiser evidence.
3. Q → What is usually most important for Google L3? A → DSA coding clarity and Googleyness; design is less common.
4. Q → Meta coding pacing target? A → Often two problems per round, so practice fast medium solutions.
5. Q → Atlassian's values round checks what? A → Openness, customer focus, balance, teamwork, and proactive change.
6. Q → Uber India reports often include what? A → Machine coding / LLD with OOP implementation.
7. Q → Airbnb design themes? A → Marketplace trust, booking correctness, search, and empathy.
8. Q → Adobe preparation mix? A → DSA plus Java, OOP, DBMS, OS, and project depth.
9. Q → Apple differentiator? A → Team-specific depth, quality, privacy, and product craftsmanship.
10. Q → Netflix caveat? A → Often experienced-hire heavy; verify level and culture expectations.
