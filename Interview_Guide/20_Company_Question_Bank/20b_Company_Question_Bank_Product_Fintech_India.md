# 20b · Company Question Bank — Product, fintech, and India backend loops

This companion file continues `20a_Company_Question_Bank_Global_FAANG.md`. Processes are public-report summaries, not guarantees; verify with recruiter, job description, and current prep packet before each loop.

🟢 Beginner · 🟡 Intermediate · 🔴 Advanced · ⚫ FAANG · 🔥 = very frequently asked

## Table of Contents
- [Q12. Salesforce](#q12-how-should-mohd-prepare-for-salesforce)
- [Q13. Walmart Global Tech](#q13-how-should-mohd-prepare-for-walmart-global-tech)
- [Q14. Mastercard](#q14-how-should-mohd-prepare-for-mastercard)
- [Q15. Visa](#q15-how-should-mohd-prepare-for-visa)
- [Q16. American Express](#q16-how-should-mohd-prepare-for-american-express)
- [Q17. LinkedIn](#q17-how-should-mohd-prepare-for-linkedin)
- [Q18. Flipkart](#q18-how-should-mohd-prepare-for-flipkart)
- [Q19. Swiggy](#q19-how-should-mohd-prepare-for-swiggy)
- [Q20. Zomato](#q20-how-should-mohd-prepare-for-zomato)
- [Sources used](#sources-used)
- [Revision Cheat Sheet](#revision-cheat-sheet)
- [Flashcards](#flashcards)

## Salesforce

### Q12. How should Mohd prepare for Salesforce?
`🟡 Intermediate` `🔥` `Must-know SDE-1/SDE-2`

**Why they ask:** Salesforce backend loops often test enterprise Java, APIs, databases, and customer-oriented engineering rather than only DSA.

**Short answer (say this first, 20-40 sec):**
Commonly reported: recruiter/OA, coding and Java/backend rounds, design/project deep dive for experienced roles, and manager/HR. Mohd should lean into Java/Spring, SQL tuning, transaction correctness, multi-tenant APIs, and workflow automation examples.

**Detailed answer:**

| Round | Commonly reported format | Mohd's angle |
|---|---|---|
| OA/screen | DSA medium, strings, arrays, hashing | Solve cleanly and test |
| Technical coding | Data structures plus Java implementation | Explain object models |
| Backend round | Spring/JPA/API/DB fundamentals | Use MAQ SQL tuning examples |
| Design/project | SDE2 may get LLD/HLD around CRM/workflows | Discuss metadata, tenants, integrations |
| Managerial/HR | Team fit, customer success, ownership | STAR around supportability and impact |

**Commonly reported DSA practice set:** Group Anagrams (49), Min Stack (155), Clone Graph (133), LRU Cache (146), Number of Islands (200), Course Schedule (207), Product of Array Except Self (238), Meeting Rooms II (253), Insert Delete GetRandom O(1) (380), Decode String (394), String Compression (443), Merge Intervals (56).

**LLD / machine-coding prompts:** CRM lead assignment rules; workflow automation engine; notification template renderer; role/permission model; audit log service.

**HLD prompts:** multi-tenant CRM platform; metadata-driven forms service; event-based workflow automation; search across customer records; integration webhook platform.

**Java/backend/CS questions to rehearse:**
- Multi-tenancy isolation patterns.
- JPA lazy loading and N+1 problem.
- Spring transaction propagation.
- REST API versioning for enterprise clients.
- SQL indexing for CRM filters.
- Java exception hierarchy and checked exceptions.
- OAuth2 basics and JWT validation.
- Queue retries and dead-letter queues.

**Behavioral focus:** Customer success, trust, equality/teamwork, and enterprise reliability.

**What they value:** Enterprise-grade backend quality, API stability, secure data handling, and customer empathy.

**5 preparation tips:**
1. Revise Java, Spring, JPA, SQL thoroughly.
2. Practice multi-tenant and permission LLD.
3. Prepare one integration/API project story.
4. Know transaction boundaries and data consistency.
5. Do one CRM/workflow HLD mock.

**Tailored "why this company" line for Mohd:** I want Salesforce because my Java/Spring and SQL tuning background fits enterprise platforms where reliable APIs and data correctness drive customer success.

**Common mistakes:** Treating Salesforce as only DSA; ignoring enterprise security; weak JPA/transaction answers.

**Follow-ups:**
- *Which guide files help most?* → `02`, `04a`, `04b`, `05`, `06a`, `16`.
- *Should Mohd mention Power Automate?* → Yes, as workflow automation experience relevant to CRM/business processes.

## Walmart Global Tech

### Q13. How should Mohd prepare for Walmart Global Tech?
`🟡 Intermediate` `🔥` `Must-know India product-company`

**Why they ask:** Walmart Global Tech commonly maps backend interviews to retail-scale cart, inventory, pricing, and order workflows.

**Short answer (say this first, 20-40 sec):**
Commonly reported: recruiter/OA, DSA technical rounds, machine coding/LLD for many backend roles, system design for SDE2+, and managerial/HR. Mohd should practice retail LLDs and emphasize SQL performance, correctness under concurrency, and production debugging.

**Detailed answer:**

| Round | Commonly reported format | Mohd's angle |
|---|---|---|
| OA | Arrays, strings, trees, graphs, DP | Time-box and test |
| DSA round | Medium coding with complexity analysis | Use Java collections correctly |
| Machine coding/LLD | Working console/service design | Model entities and write extensible services |
| System design | Inventory/order/search for SDE2+ | Discuss scale, consistency, caching |
| Manager/HR | Ownership, collaboration, customer impact | Tie to performance and operations |

**Commonly reported DSA practice set:** Two Sum (1), 3Sum (15), Valid Parentheses (20), Search in Rotated Sorted Array (33), Rotate Image (48), Merge Intervals (56), Binary Tree Level Order Traversal (102), LRU Cache (146), Number of Islands (200), Kth Largest Element in an Array (215), Lowest Common Ancestor of a Binary Tree (236), Coin Change (322).

**LLD prompts:** inventory reservation system; cart and promotion engine; parking lot or locker; order status state machine; warehouse slot allocator.

**HLD prompts:** e-commerce checkout; inventory availability service; product catalog search; shipment tracking service; pricing and promotions platform.

**Java/backend/CS questions:** inventory consistency under concurrent checkout; SQL unique constraints and transactions; Java BigDecimal for money; cache invalidation for prices; Spring validation and exception handling; message queues for order events; normalization vs denormalization; debugging slow checkout API.

**Behavioral focus:** Customer impact, ownership, frugality/pragmatism, cross-team collaboration.

**What they value:** Practical backend delivery, retail-scale consistency, clean OOP, and operational debugging.

**5 preparation tips:** Practice retail LLDs; review money/date handling; focus on SQL transactions and races; run timed machine-coding mocks; use SQL tuning wins as impact examples.

**Tailored "why this company" line for Mohd:** I want Walmart Global Tech because my SQL optimization and backend automation experience map directly to high-volume retail systems where performance improves customer checkout and operations.

**Common mistakes:** Ignoring oversell race conditions; hard-coding coupon logic; missing tests in machine coding.

**Follow-ups:**
- *Best LLD drill?* → Cart + promotion + inventory reservation.
- *Best HLD drill?* → Checkout with inventory and payment failure handling.

## Mastercard

### Q14. How should Mohd prepare for Mastercard?
`🟡 Intermediate` `🔥` `Must-know fintech`

**Why they ask:** Fintech SDE interviews often reward correctness, Java fundamentals, SQL/security basics, and careful communication.

**Short answer (say this first, 20-40 sec):**
Commonly reported: OA, technical rounds with Java, DSA, DBMS, testing/security, project discussion, and HR. Public India experiences mention Java/JUnit, synchronization, REST, TLS, SQL relationships, indexing, and basic DSA.

**Detailed answer:**

| Round | Commonly reported format | Mohd's angle |
|---|---|---|
| Online test | Basic-to-medium coding, aptitude, debugging | Prioritize correctness |
| Technical 1 | Java, JUnit/testing, synchronization, DSA | Explain fundamentals clearly |
| Technical 2 | Projects, DB relationships, API security, SQL/NoSQL | Use SQL tuning and API examples |
| Managerial | Teamwork, project choices, payments motivation | Show risk awareness |
| HR | Fit, compensation, background | Be concise and professional |

**Commonly reported DSA practice set:** Two Sum (1), Valid Parentheses (20), Maximum Subarray (53), Climbing Stairs (70), Merge Sorted Array (88), Best Time to Buy and Sell Stock (121), Reverse Linked List (206), Contains Duplicate (217), Valid Anagram (242), Missing Number (268), Intersection of Two Arrays II (350), Binary Search (704).

**LLD prompts:** payment authorization workflow; fraud-rule evaluator; transaction reconciliation service; card limit manager; audit log writer.

**HLD prompts:** payment gateway; fraud detection pipeline; transaction ledger service; tokenization service; dispute management workflow.

**Java/backend/CS questions:** TLS handshake at a high level; SQL many-to-many schema and indexing; synchronized vs ReentrantLock; JUnit tests for payment rules; idempotency key for payment retries; ACID and ledger consistency; REST GET vs POST safety/idempotency; partitioning vs indexing.

**Behavioral focus:** Trust, accuracy, risk control, clear communication, and project ownership.

**What they value:** Correctness, security, database fundamentals, and reliable Java services.

**5 preparation tips:** Revise synchronization and JUnit; prepare SQL relationships/indexing; practice payment idempotency; solve easy/medium DSA bug-free; explain security basics without overclaiming.

**Tailored "why this company" line for Mohd:** I want Mastercard because my SQL tuning and backend validation work align with payment systems where correctness, security, and performance are essential.

**Common mistakes:** Casual money handling; vague TLS/security answers; not knowing SQL relationship modeling.

**Follow-ups:**
- *Which story should Mohd use?* → Data validation/integrity checks or SQL tuning with measurable impact.
- *What Java topic matters?* → Thread safety and testing basics.

## Visa

### Q15. How should Mohd prepare for Visa?
`🟡 Intermediate` `🔥` `Must-know fintech`

**Why they ask:** Visa backend interviews often center on secure, reliable payment APIs and strong Java/SQL fundamentals.

**Short answer (say this first, 20-40 sec):**
Commonly reported: recruiter/OA, coding/Java rounds, DBMS/API/security discussion, design or project deep dive, and HR. Mohd should prepare payment idempotency, audit logging, transaction isolation, secure APIs, and SQL indexing.

**Detailed answer:**

| Round | Commonly reported format | Mohd's angle |
|---|---|---|
| OA | Arrays, strings, SQL or aptitude depending role | Avoid careless edge-case misses |
| Coding | DSA medium with Java implementation | Name complexity and tests |
| Backend fundamentals | Java, Spring, DBMS, REST, security | Use payment-safe design language |
| Design/project | Payment APIs, reconciliation, ledger, fraud | Discuss consistency and auditability |
| HR | Motivation, teamwork, integrity | Show customer and risk awareness |

**Commonly reported DSA practice set:** Two Sum (1), Group Anagrams (49), Merge Intervals (56), Binary Tree Level Order Traversal (102), Valid Palindrome (125), Min Stack (155), Number of Islands (200), Course Schedule (207), Kth Largest Element in an Array (215), Product of Array Except Self (238), Find All Anagrams in a String (438), LRU Cache (146).

**LLD prompts:** idempotent payment API; card token vault interface; merchant settlement calculator; fraud alert rule engine; API throttling service.

**HLD prompts:** payment authorization service; merchant onboarding platform; real-time fraud scoring; transaction search/audit service; webhook notification platform.

**Java/backend/CS questions:** idempotency and exactly-once illusions; SQL isolation and double-spend prevention; BigDecimal and rounding; OAuth2/JWT basics; circuit breaker; audit logging and PII masking; indexes for transaction lookup; exponential backoff.

**Behavioral focus:** Integrity, risk management, customer trust, and cross-team execution.

**What they value:** Reliable Java services, secure payments, DB correctness, and operational maturity.

**5 preparation tips:** Practice SQL/payment scenarios; prepare idempotency examples; review `16_Security.md`; use money-safe Java types; prepare defect-prevention stories.

**Tailored "why this company" line for Mohd:** I want Visa because my backend, SQL performance, and data-integrity experience fit payment networks that require reliable, secure, low-latency services.

**Common mistakes:** Claiming exactly-once delivery; using floating point for money; ignoring auditability.

**Follow-ups:**
- *Best design drill?* → Payment authorization with retries, ledger, and fraud scoring.
- *Best CS topic?* → Transaction isolation and idempotency.

## American Express

### Q16. How should Mohd prepare for American Express?
`🟡 Intermediate` `🔥` `Must-know fintech`

**Why they ask:** Public Amex India experiences include HackerRank coding plus CV, networks, OS/DBMS, logic, and communication.

**Short answer (say this first, 20-40 sec):**
Commonly reported: resume shortlist/OA, technical round covering CV, DSA, networks/OS/DBMS, final HR+technical/puzzle. Mohd should be ready to explain projects simply and solve easy-medium coding accurately.

**Detailed answer:**

| Round | Commonly reported format | Mohd's angle |
|---|---|---|
| Resume shortlist | Project and internship relevance | Make Java/RAG/SQL impact visible |
| Coding challenge | Strings, BST/trees, debugging, easy-medium | Submit reliable code |
| Technical | CV, DSA, networks/OS/DBMS, projects | Answer fundamentals precisely |
| Final HR+technical | Puzzle/problem-solving plus behavioral | Communicate assumptions first |
| HR | Motivation and fit | Connect to financial trust and analytics |

**Commonly reported DSA practice set:** Two Sum (1), Longest Common Prefix (14), Valid Parentheses (20), Merge Two Sorted Lists (21), Maximum Subarray (53), Climbing Stairs (70), Binary Tree Inorder Traversal (94), Validate Binary Search Tree (98), Balanced Binary Tree (110), Missing Number (268), First Unique Character in a String (387), Find Second Minimum Value in a Binary Tree (671).

**LLD prompts:** credit card statement generator; reward points calculator; fraud case workflow; expense categorization service; customer notification preference service.

**HLD prompts:** credit card transaction platform; rewards engine; fraud monitoring dashboard; statement generation batch system; customer support case system.

**Java/backend/CS questions:** TCP vs UDP and HTTPS; BST validation; SQL joins and transaction indexes; equals/hashCode contract; REST exception handling; OS deadlock conditions; debugging failing tests; puzzle assumptions.

**Behavioral focus:** Communication, logical reasoning, trust, teamwork, and project clarity.

**What they value:** Financial-data correctness, fundamentals, communication, and reliable delivery.

**5 preparation tips:** Revise networks/OS/DBMS; practice easy-medium trees and strings; prepare project diagrams; do puzzle practice; keep answers concise and non-speculative.

**Tailored "why this company" line for Mohd:** I want American Express because my SQL, analytics, and backend validation work align with financial products where data correctness and customer trust matter.

**Common mistakes:** Overcomplicating easy coding; weak networks basics; not explaining CV claims clearly.

**Follow-ups:**
- *What project should Mohd lead with?* → SQL tuning/semantic-model performance, then RAG project if AI relevance appears.
- *What level of puzzle prep?* → Enough to show structured assumptions and communication.

## LinkedIn

### Q17. How should Mohd prepare for LinkedIn?
`⚫ FAANG` `🔥` `Stretch`

**Why they ask:** LinkedIn blends coding with graph/search/relevance thinking and professional-network product judgment.

**Short answer (say this first, 20-40 sec):**
Commonly reported: recruiter, coding screen, onsite coding rounds, design for experienced roles, and behavioral/culture. Public prep lists emphasize nested structures, trees, maps, graph/search, and product/backend scale.

**Detailed answer:**

| Round | Commonly reported format | Mohd's angle |
|---|---|---|
| Recruiter | Level, team, coding/design split | Ask if product/system design is included |
| Coding screen | One or two medium problems | Explain constraints and tests |
| Onsite coding | Trees, maps, iterators, design-ish coding | Write clean classes when needed |
| Design | Feed/search/messaging/recruiter tools for SDE2+ | Discuss graph, ranking, privacy |
| Behavioral | Professional network mission and collaboration | Tie to helping users and teams |

**Commonly reported DSA practice set:** Two Sum III - Data Structure Design (170), Shortest Word Distance II (244), Factor Combinations (254), Paint House (256), Nested List Weight Sum II (364), Find Leaves of Binary Tree (366), Insert Delete GetRandom O(1) (380), Serialize and Deserialize BST (449), Can Place Flowers (605), Max Stack (716), Merge Intervals (56), Lowest Common Ancestor of a Binary Tree (236).

**LLD prompts:** connection request workflow; profile privacy settings; job alert matching service; in-memory inverted index; rate limiter for messaging.

**HLD prompts:** LinkedIn feed; People You May Know graph service; job search and recommendations; messaging service; profile search indexing.

**Java/backend/CS questions:** graph storage for connections; search indexing and inverted indexes; privacy-aware APIs; Java iterator design; cache consistency for profiles; SQL vs graph DB; ranking feature pipeline; spam prevention in messaging.

**Behavioral focus:** Member value, collaboration, professional impact, and thoughtful product trade-offs.

**What they value:** Graph thinking, search/relevance, professional trust, and clean backend APIs.

**5 preparation tips:** Practice nested iterators and data-structure design; review graph traversal/ranking; design feeds/search/jobs; show empathy for professional users; connect RAG to search/recommendations.

**Tailored "why this company" line for Mohd:** I want LinkedIn because my RAG/search and backend data experience can help build professional discovery systems that connect people to opportunities.

**Common mistakes:** Ignoring privacy; shallow graph modeling; no relevance/ranking discussion.

**Follow-ups:**
- *Best HLD drill?* → People You May Know with privacy and ranking.
- *Best DSA pattern?* → Iterator/nested data structures and tree traversals.

## Flipkart

### Q18. How should Mohd prepare for Flipkart?
`🔴 Advanced` `🔥` `Must-know India product-company`

**Why they ask:** Flipkart-style preparation is a strong proxy for Indian product-company DSA + machine-coding loops.

**Short answer (say this first, 20-40 sec):**
Commonly reported: OA, live DSA, machine coding/LLD, system design for SDE2+, and hiring manager/HR. Mohd should run 90-120 minute Java machine-coding drills around cart, coupon, inventory, order, and payments.

**Detailed answer:**

| Round | Commonly reported format | Mohd's angle |
|---|---|---|
| OA | Medium/hard DSA | Time-box and handle edge cases |
| DSA | Arrays, graphs, DP, heaps | Code optimal Java |
| Machine coding | Working OOP console/service app | Clean packages, services, strategies, tests |
| System design | SDE2+ e-commerce scale | Inventory, catalog, payments, consistency |
| Manager/HR | Ownership, fast execution, customer impact | Use performance/automation stories |

**Commonly reported DSA practice set:** LRU Cache (146), Word Break (139), Kth Largest Element in an Array (215), Number of Islands (200), Design Twitter (355), LFU Cache (460), Merge Intervals (56), Rotting Oranges (994), Shortest Path in Binary Matrix (1091), Snapshot Array (1146), Design Underground System (1396), Design Parking System (1603).

**LLD prompts:** cart and coupon engine; inventory reservation/release; cab/parking-lot style machine coding; order state machine; warehouse/bin allocation; cache/rate limiter.

**HLD prompts:** e-commerce checkout; product catalog search; inventory service for flash sales; order tracking; recommendation service.

**Java/backend/CS questions:** preventing oversell with locks/transactions; SOLID in machine coding; orders/items/payments schema; concurrency in inventory; caching catalog safely; Kafka order lifecycle; testing machine-coded apps; flash-sale capacity estimates.

**Behavioral focus:** Ownership, speed, customer obsession, frugal scalable engineering.

**What they value:** Strong DSA, machine-coding execution, e-commerce domain modeling, and scale trade-offs.

**5 preparation tips:** Do weekly 90-minute Java LLD; practice cart/coupon/inventory/order; review DP/graphs/heaps; use layered code; prepare flash-sale HLD.

**Tailored "why this company" line for Mohd:** I want Flipkart because my Java backend and SQL optimization experience can improve e-commerce systems where latency and inventory correctness directly affect users.

**Common mistakes:** Not producing runnable code; no extensibility; ignoring transaction races.

**Follow-ups:**
- *Best weekend drill?* → Coupon engine with multiple strategies and tests.
- *Which guide files?* → `10a-10f`, `06a`, `08e`, `08f`, `09c`.

## Swiggy

### Q19. How should Mohd prepare for Swiggy?
`🔴 Advanced` `🔥` `Must-know India product-company`

**Why they ask:** Swiggy-style backend interviews combine food-commerce domain logic, real-time logistics, DSA, and LLD.

**Short answer (say this first, 20-40 sec):**
Commonly reported: recruiter/OA, DSA, machine coding/LLD, HLD for SDE2+, and managerial/HR. Mohd should prepare order, restaurant, coupon, delivery assignment, wallet/refund, and realtime tracking designs.

**Detailed answer:**

| Round | Commonly reported format | Mohd's angle |
|---|---|---|
| OA | DSA medium: arrays/graphs/heaps | Optimize and test |
| DSA live | Problem solving with constraints | Explain trade-offs |
| Machine coding | Food ordering, wallet, coupon, Splitwise, rate limiter | Build running Java services |
| HLD | Restaurant search, order tracking, dispatch for SDE2+ | Discuss realtime and consistency |
| Manager/HR | Ownership, ambiguity, operational incidents | Use debugging and automation stories |

**Commonly reported DSA practice set:** Minimum Window Substring (76), LRU Cache (146), Number of Islands (200), Course Schedule (207), Top K Frequent Elements (347), Design In-Memory File System (588), Network Delay Time (743), Time Based Key-Value Store (981), K Closest Points to Origin (973), Rotting Oranges (994), Merge Intervals (56), Food Ratings System (2353).

**LLD prompts:** food ordering cart/checkout; restaurant menu/versioning service; coupon engine; delivery assignment simulator; wallet/refund ledger.

**HLD prompts:** food delivery order tracking; restaurant search/ranking; delivery partner dispatch; realtime location pipeline; promo/coupon service.

**Java/backend/CS questions:** realtime location ingestion; menu/price cache invalidation; orders/restaurants/partners schema; payment success but order failure; Java concurrency for workers; idempotent webhooks; observability for delayed delivery; geo-indexing basics.

**Behavioral focus:** Customer experience, speed, ownership in ambiguity, incident learning, and collaboration.

**What they value:** Practical machine coding, realtime logistics, consistency, and operational excellence.

**5 preparation tips:** Practice food-ordering LLD; review heaps/graphs/intervals/maps; study idempotency/compensation; do dispatch HLD; prepare incident/debugging STAR story.

**Tailored "why this company" line for Mohd:** I want Swiggy because my backend optimization and automation work fits real-time food-commerce systems where fast APIs and reliable order state matter.

**Common mistakes:** Ignoring partial payment/order failures; simplistic dispatch logic; no observability story.

**Follow-ups:**
- *Best HLD drill?* → Order tracking with location ingestion and notifications.
- *Best LLD drill?* → Cart + coupon + checkout with refund compensation.

## Zomato

### Q20. How should Mohd prepare for Zomato?
`🔴 Advanced` `🔥` `Must-know India product-company`

**Why they ask:** Zomato loops often reward fast problem solving, backend depth, ownership, and product sense around restaurant discovery and ordering.

**Short answer (say this first, 20-40 sec):**
Commonly reported: OA/referral screen, DSA, backend/machine coding, system design/project deep dive, and managerial/HR. Mohd should prepare restaurant search, order state, ratings, coupons, payments, and delivery-fee logic.

**Detailed answer:**

| Round | Commonly reported format | Mohd's angle |
|---|---|---|
| OA/screen | Medium/hard DSA and debugging | Practice speed and correctness |
| DSA live | Graphs, heaps, strings, design data structures | Talk through edge cases |
| Machine coding/backend | Restaurant/order/rating/coupon service | Use clean Java OOP and tests |
| HLD/project | Search, ordering, notifications, payments | Discuss scale and failure modes |
| Manager/HR | Ownership, fast execution, customer obsession | Use metrics and trade-offs |

**Commonly reported DSA practice set:** Minimum Window Substring (76), Word Search (79), LRU Cache (146), Find Median from Data Stream (295), Design Twitter (355), Top K Frequent Elements (347), Insert Delete GetRandom O(1) (380), LFU Cache (460), Rotting Oranges (994), Shortest Path in Binary Matrix (1091), Merge Intervals (56), Design Underground System (1396).

**LLD prompts:** restaurant table booking; food ordering workflow; rating/review moderation; coupon and referral engine; delivery fee calculator.

**HLD prompts:** restaurant search/discovery; order placement and tracking; review/rating platform; notification service; payment/refund workflow.

**Java/backend/CS questions:** restaurant ranking with filters; duplicate order/payment events; SQL indexes for geo + cuisine filters; menu cache updates; priority queues for dispatch; order state APIs; downstream payment failures; production debugging for latency.

**Behavioral focus:** Ownership, bias to action, customer experience, and working in high-change environments.

**What they value:** Fast execution, backend correctness, product thinking, and operational troubleshooting.

**5 preparation tips:** Practice restaurant/order LLD under 90 minutes; study retries/idempotency/compensation; review heaps/graphs/caches; prepare latency/reliability story; do restaurant search/ranking HLD.

**Tailored "why this company" line for Mohd:** I want Zomato because my Java backend, SQL tuning, and GenAI search work align with food-tech discovery and ordering systems that require speed and correctness.

**Common mistakes:** Treating ratings/search as simple CRUD; missing refund/compensation flows; weak product metrics.

**Follow-ups:**
- *Best DSA pattern?* → Design data structures, heaps, graph traversal, cache problems.
- *Best project link?* → RAG/search project for discovery, SQL tuning for low-latency filters.

## Sources used

- Amazon Jobs, software development interview preparation: https://www.amazon.jobs/en/software-development-interview-prep
- Google Careers, How we hire / interview preparation: https://careers.google.com/how-we-hire/
- Microsoft Careers, hiring and interview tips: https://careers.microsoft.com/v2/global/en/hiring-tips
- Meta Careers interview preparation pages and candidate-facing interview tips: https://www.metacareers.com/
- Atlassian values: https://www.atlassian.com/company/values
- Uber careers and values pages: https://www.uber.com/global/en/careers/ and https://www.uber.com/global/en/about/values/
- LeetCode company tags and problem lists: https://leetcode.com/company/ and https://leetcode.com/discuss/interview-question
- GeeksforGeeks Google SDE Sheet: https://www.geeksforgeeks.org/dsa/google-sde-sheet-interview-questions-and-answers/
- GeeksforGeeks Amazon SDE Sheet: https://www.geeksforgeeks.org/dsa/amazon-sde-sheet-interview-questions-and-answers/
- GeeksforGeeks interview experiences, including Uber, Mastercard, and American Express SDE reports.
- Glassdoor interview reviews for company-specific round reports: https://www.glassdoor.co.in/Interview/
- Interviewing.io learning center and company interview guides: https://interviewing.io/learn
- Educative and public company-interview guides were used only as secondary triangulation where official details were unavailable.

## Revision Cheat Sheet

- Product companies in India often add LLD/machine-coding rounds; practice 90-minute Java implementations.
- Fintech companies emphasize Java, SQL, API security, idempotency, auditability, and CS fundamentals.
- E-commerce/food-tech loops combine DSA with cart, order, inventory, dispatch, coupon, and payment workflows.
- LinkedIn blends coding with graph/search/relevance thinking.
- For every company, verify current scope with recruiter and treat public reports as pattern practice.
- Mohd's strongest repeated pitch: Java backend + SQL tuning + automation impact + GenAI/RAG project depth.

## Flashcards

1. Q → What should Mohd ask before a machine-coding loop? A → Time limit, expected runnable code, tests, language, and allowed libraries.
2. Q → Fintech backend must always mention what? A → Idempotency, auditability, security, and data consistency.
3. Q → Flipkart-style LLD theme? A → E-commerce entities such as cart, coupon, inventory, and order state.
4. Q → Swiggy/Zomato HLD theme? A → Real-time order tracking, restaurant search, dispatch, payments, and notifications.
5. Q → Mastercard public reports highlighted which fundamentals? A → Java, JUnit/testing, synchronization, REST/TLS, SQL relationships/indexing.
6. Q → American Express public reports highlighted what? A → HackerRank coding, CV questions, networks/CS basics, and logical communication.
7. Q → Salesforce backend angle? A → Multi-tenant enterprise APIs, JPA, transactions, permissions, workflows.
8. Q → Walmart domain angle? A → Retail catalog, checkout, inventory, pricing, shipment, and operational debugging.
9. Q → LinkedIn domain angle? A → Professional graph, feed, jobs, messaging, and search/relevance.
10. Q → Best use of LeetCode company tags? A → Pattern selection, not prediction.
