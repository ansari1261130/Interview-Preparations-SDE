# 06a · SQL Concepts and Database Internals

A complete SDE-1/SDE-2 interview guide for SQL, query performance, transactions, and database internals. It is tuned to Mohd Azam Ansari's Java backend profile, PostgreSQL/MySQL experience, pgvector project, and 70% query optimization resume bullet.

**Legend:** 🟢 Beginner · 🟡 Intermediate · 🔴 Advanced · ⚫ FAANG · 🔥 = very frequently asked

## Table of Contents
- [Beginner guide to this file](#beginner-guide-to-this-file)
- [Section 1 · Real Interview Questions](#section-1--real-interview-questions)
- [Section 2 · SQL Core Concepts](#section-2--sql-core-concepts)
- [Section 3 · Indexes and Query Optimization](#section-3--indexes-and-query-optimization)
- [Section 4 · Transactions, Isolation, Locks, and Deadlocks](#section-4--transactions-isolation-locks-and-deadlocks)
- [Section 5 · Schema Design, Scaling, and Operations](#section-5--schema-design-scaling-and-operations)
- [Section 6 · Backend Interview Edge Cases](#section-6--backend-interview-edge-cases)
- [Revision Cheat Sheet](#revision-cheat-sheet)
- [Flashcards](#flashcards)

## Beginner guide to this file

### What this file teaches
This file teaches Structured Query Language (SQL), query execution, indexes, transactions, locks, schema
design, and database operations from the point of view of a Java backend service. Interviews ask these
topics because real APIs fail when a request reads the wrong rows, scans too much data, waits on a lock,
changes a schema unsafely, or reads a stale replica.

### Mental model
A database is like a busy warehouse: requests are shoppers, tables are aisles, indexes are labeled maps,
transactions are checkout baskets, locks are temporary lane closures, and logs are the security camera used
after a crash.
- Tables store facts, and constraints keep impossible facts out.
- Query execution chooses how to find rows: scan, index lookup, join, sort, aggregate, and limit.
- Indexes speed selected reads but add storage and write cost.
- Multiversion Concurrency Control (MVCC), isolation, and locks decide what concurrent requests see or wait for.
- Write-Ahead Logging (WAL), recovery, replication, partitioning, and migrations keep data safe while traffic continues.

### How to think through any question here
1. Start with a user request, such as GET /customers/42/orders?status=PAID or POST /payments/17/capture.
2. Name the output grain: one row per order, customer, job, day, partition, or API response item.
3. Identify tables, predicates, joins, grouping, ordering, and limits.
4. Predict the execution path: scan type, index, join, sort, aggregate, and rows read versus returned.
5. Ask what concurrent requests can change the same rows and whether MVCC, isolation, optimistic locking, or row locks are needed.
6. Check write safety: transaction boundary, constraints, WAL durability, rollback, and recovery.
7. Add production checks: 95th/99th percentile latency (p95/p99), lock waits, replica lag, index write cost, partition pruning, and migration rollback.

### Pattern recognition cues
| If the question mentions... | Think about... | Why it fits |
|---|---|---|
| Slow endpoint or high p95 | EXPLAIN ANALYZE, rows read, indexes, locks | The bottleneck may be SQL shape, missing index, or waiting transaction |
| customer_id, status, ORDER BY, LIMIT | Composite index order | Equality columns then ordering columns can avoid scans and sorts |
| Same row updated by two requests | Isolation, locks, optimistic version | Lost updates happen in read-modify-write races |
| Long report on PostgreSQL | MVCC snapshots and VACUUM | Old row versions remain while snapshots need them |
| Reports by month or tenant | Partitioning or sharding | Large data needs pruning or distribution by a stable key |
| Read replica returns old data | Replication lag | A committed primary write may not be visible on the replica yet |
| Add column or index in production | Safe schema migration | Live traffic must avoid long locks and incompatible deploy order |
| NULL, missing value, optional field | Three-valued logic | SQL treats unknown differently from false |

### Abbreviations and key terms
| Short form | Expanded form | Plain meaning |
|---|---|---|
| ACID | Atomicity, Consistency, Isolation, Durability | Transaction guarantees for correct and durable writes |
| API | Application Programming Interface | Service endpoint whose request becomes database work |
| APM | Application Performance Monitoring | Tooling that traces latency across app, cache, and database calls |
| B+Tree / B-Tree | Balanced tree index family | Ordered index structure used for equality, range, and sort-friendly lookups |
| BCNF | Boyce-Codd Normal Form | Strong normalization rule for dependencies and candidate keys |
| CRUD | Create, Read, Update, Delete | Basic operations most APIs perform on data |
| CTE | Common Table Expression | Named subquery introduced with WITH |
| DB / DBMS | Database / Database Management System | Persistent store and the software managing it |
| DDL | Data Definition Language | SQL that changes schema, such as CREATE, ALTER, DROP, or TRUNCATE |
| GIN / GiST | Generalized Inverted Index / Generalized Search Tree | PostgreSQL index families for specialized data types |
| HNSW | Hierarchical Navigable Small World | Approximate-nearest-neighbor graph for vector search |
| I/O | Input/Output | Disk or network reads and writes |
| JPA / ORM | Java Persistence API / Object-Relational Mapping | Java persistence layer, commonly Hibernate |
| JSON / JSONB | JavaScript Object Notation / binary JSON | Document-shaped data; JSONB supports PostgreSQL indexing |
| JVM | Java Virtual Machine | Runtime that executes Java applications |
| KNN | K-Nearest Neighbors | Search for closest vectors or points |
| LAG / LEAD | Previous-row / next-row window functions | Compare a row with its neighbor |
| LLM | Large Language Model | AI model often paired with retrieval systems |
| MVCC | Multiversion Concurrency Control | Row-versioning technique for concurrent reads and writes |
| N+1 | One query plus one query per parent row | ORM query explosion pattern |
| OLAP / OLTP | Online Analytical Processing / Online Transaction Processing | Analytics scans versus request-time transactions |
| GET / POST | Hypertext Transfer Protocol request methods | Read and create/change style API operations used in examples |
| P95 / P99 | 95th / 99th percentile latency | Tail latency showing how slow the slowest normal requests are |
| RAG | Retrieval-Augmented Generation | Retrieve relevant data, then generate an answer |
| SDE-1 / SDE-2 | Software Development Engineer level 1 / level 2 | Interview seniority levels |
| SQL | Structured Query Language | Language for querying and changing relational data |
| SSI | Serializable Snapshot Isolation | PostgreSQL technique for serializable transactions |
| UTC | Coordinated Universal Time | Time standard for storing instants |
| UUID / ULID | Universally Unique Identifier / Universally Unique Lexicographically Sortable Identifier | Globally unique identifiers |
| VACUUM | PostgreSQL cleanup process | Removes dead tuples left by MVCC updates and deletes |
| WAL | Write-Ahead Logging | Durable log written before data pages so recovery can replay changes |

### Common beginner traps
- Saying an index always makes a query faster; it may be unused, low-selectivity, or too expensive on writes.
- Explaining SQL in written order instead of logical execution order.
- Treating MVCC as no locking; writes still lock rows and schema changes can take stronger locks.
- Assuming a Spring transaction automatically fixes every race condition.
- Adding replicas or partitions before proving the query shape, index, and data model are correct.
- Running schema changes as one risky step instead of expand, backfill, validate, switch, and clean up.

### How to study this file
1. Read the short answer first and say it aloud in 30 seconds.
2. Read the detailed answer with a tiny data set: three customers, six orders, and two concurrent payments.
3. For every performance question, sketch the likely plan and index.
4. For every transaction question, name the concurrent requests and the bad outcome you are preventing.
5. Use flashcards for recall, then write one SQL or Spring example for each weak topic.
6. Practice production trade-offs: write cost, lock duration, replica lag, rollback, and observability.

### Scalability and production lens
A toy answer assumes one small table and one user. A production answer starts with traffic and data shape:
for example, 2,000 requests per second reading latest paid orders for customer 42, 200 writes per second
creating payments, and a 400-million-row orders table. Query execution needs selective composite indexes,
partition pruning, and EXPLAIN ANALYZE evidence. MVCC keeps readers from blocking writers, but long reports
can delay VACUUM and increase bloat. Replicas help reads, but read-after-write requests may need the primary
because of lag. Safe schema changes use backward-compatible deploys, concurrent indexes where supported,
batched backfills, monitoring, and rollback plans.

## Section 1 · Real Interview Questions

### Q1. What databases have you worked with, and how does PostgreSQL MVCC differ from MySQL InnoDB MVCC?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** They want to know whether your resume's PostgreSQL/MySQL claims go beyond CRUD.

**Beginner lens:** Think of Multiversion Concurrency Control (MVCC) as each reader seeing a photo of rows
while writers keep working. PostgreSQL stores old row versions in the table itself; cleanup by VACUUM matters.
MySQL InnoDB keeps current clustered-index records and uses undo logs to reconstruct older versions, then
purge removes old undo. The key distinction is where versions live and how old versions are cleaned.
Interviewers are testing whether database experience includes concurrency, bloat, long transactions, and
InnoDB gap or next-key locking, not only Create, Read, Update, Delete (CRUD) queries.

**Short answer (say this first, 20-40 sec):**
I have worked with PostgreSQL, MySQL, and pgvector on backend/RAG-style projects. PostgreSQL keeps old row versions directly in table tuples using `xmin` and `xmax`, so dead tuples must be cleaned by VACUUM and can cause table bloat. InnoDB stores the current row in clustered indexes and reconstructs older snapshots using undo logs; old undo versions are removed by purge. InnoDB also has gap and next-key locks under repeatable read to prevent phantom ranges.

**Detailed answer:**
| Area | PostgreSQL MVCC | MySQL InnoDB MVCC |
|---|---|---|
| Version storage | Multiple tuple versions in heap table | Current record plus undo log chain |
| Visibility metadata | `xmin`, `xmax`, command id, transaction snapshot | transaction id and rollback pointer to undo |
| Cleanup | `VACUUM` removes dead tuples; autovacuum is critical | purge thread removes obsolete undo records |
| Space issue | table and index bloat if vacuum lags | long transactions grow undo and history list |
| Default isolation | READ COMMITTED | REPEATABLE READ |
| Phantom handling | Serializable Snapshot Isolation for serializable | gap and next-key locks in locking reads |
| Range locks | no general gap locks in normal reads | gap locks and next-key locks are common |

```mermaid
flowchart LR
  A["PostgreSQL UPDATE"] --> B["New tuple version in table"]
  B --> C["Old tuple has xmax"]
  C --> D["Readers choose tuple by snapshot"]
  D --> E["VACUUM removes dead tuples"]
  F["InnoDB UPDATE"] --> G["Clustered index row changes"]
  G --> H["Old image written to undo log"]
  H --> I["Readers reconstruct snapshot"]
  I --> J["Purge removes old undo"]
```

Use this in your answer: *In Lafz, I used PostgreSQL with pgvector, so I care about MVCC because long-running reads and slow VACUUM can affect table/index bloat and retrieval latency.*

**Common mistakes:**
- Saying MVCC means no locks; writes still lock rows.
- Ignoring VACUUM in PostgreSQL or purge in InnoDB.
- Claiming MySQL repeatable read behaves exactly like PostgreSQL repeatable read.

**Follow-ups:**
- *Why can PostgreSQL bloat happen?* → UPDATE/DELETE create dead tuples; if VACUUM cannot remove them, table and index pages grow.
- *Why does InnoDB use next-key locks?* → To lock both existing index records and gaps so range predicates do not get phantoms in locking operations.

### Q2. How do you investigate a slow API, including database checks and indexes?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** This tests production debugging, not just SQL syntax.

**Beginner lens:** Treat a slow Application Programming Interface (API) as a stopwatch problem: controller
code, network, cache, pool wait, locks, and queries each get a lap time. If an orders endpoint returns 20 rows
but the plan scans millions, the database is the bottleneck. The mental model is measured evidence before
remedies: exact statement, parameters, execution plan, index fit, and before/after latency. The interviewer
wants production debugging discipline. A common trap is adding a single-column index without checking
selectivity, composite order, write overhead, or whether time was really spent outside the database.

**Short answer (say this first, 20-40 sec):**
I first isolate whether time is spent in network, application, cache, or database. On the DB side I identify the exact SQL, measure with `EXPLAIN ANALYZE`, check cardinality, indexes, lock waits, and slow-query statistics. I compare expected versus actual rows, look for sequential scans, bad joins, missing or unused indexes, and confirm with before/after metrics. For my resume, I would explain the 70% improvement as a measured query/index tuning outcome, not just adding indexes blindly.

**Detailed answer:**
```mermaid
flowchart TD
  A["Slow API observed"] --> B["Check p95 latency and error rate"]
  B --> C{"DB time high?"}
  C -->|"no"| D["Inspect app code, remote calls, cache, serialization"]
  C -->|"yes"| E["Find exact SQL and parameters"]
  E --> F["Run EXPLAIN or EXPLAIN ANALYZE"]
  F --> G["Compare estimated rows and actual rows"]
  G --> H{"Plan issue?"}
  H -->|"missing index"| I["Design selective composite or covering index"]
  H -->|"bad join"| J["Fix predicate, stats, join order, or query shape"]
  H -->|"lock wait"| K["Inspect blockers and transaction length"]
  I --> L["Benchmark before and after"]
  J --> L
  K --> L
```

PostgreSQL checks:
```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT * FROM orders WHERE customer_id = 42 AND status = 'PAID' ORDER BY created_at DESC LIMIT 20;

SELECT query, calls, mean_exec_time, rows
FROM pg_stat_statements
ORDER BY mean_exec_time DESC
LIMIT 10;
```

Sample plan:
```text
Limit  (actual time=0.120..0.180 rows=20)
  -> Index Scan using idx_orders_customer_status_created
     Index Cond: ((customer_id = 42) AND (status = 'PAID'))
     Buffers: shared hit=8
```
Good signs: index scan, low actual time, few buffers. Bad signs: huge `Seq Scan`, bad row estimates, sort spilling to disk, nested loop over many rows.

MySQL checks:
```sql
EXPLAIN ANALYZE SELECT * FROM orders WHERE customer_id = 42 AND status = 'PAID';
SHOW VARIABLES LIKE 'slow_query_log';
SHOW ENGINE INNODB STATUS;
```

70% improvement STAR template:
- **Situation:** `[API/report/query name]` had high p95 latency because `[large table scan / bad join / no composite index]`.
- **Task:** Reduce latency without changing business output.
- **Action:** Captured baseline, read plan, found `[problem]`, added `[index/query rewrite]`, verified with `EXPLAIN ANALYZE`, and checked no regression on writes.
- **Result:** Query latency improved by about 70%, from `[before]` to `[after]`, with monitored CPU/IO stable.

**Common mistakes:**
- Starting by adding random indexes.
- Reading estimated cost only and ignoring actual rows/time.
- Forgetting lock waits and connection pool exhaustion.

**Follow-ups:**
- *How do you find unused indexes?* → PostgreSQL `pg_stat_user_indexes`; MySQL `performance_schema` and `sys.schema_unused_indexes` where available.
- *What if EXPLAIN uses the wrong index?* → Check stats freshness, selectivity, casts, functions, parameter values, and composite column order.

### Q3. Have you used database-level locking in code?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Backend systems fail when concurrent requests update the same row incorrectly.

**Beginner lens:** Database-level locking is a turnstile for data that cannot be changed by two requests at
once. For a job queue, one worker can select a ready job FOR UPDATE so another worker cannot claim the same
row. The main distinction is pessimistic locking, which blocks first, versus optimistic locking, which checks
a version later. The interviewer is probing whether you can place locks inside a short transaction. Avoid
holding row locks while calling remote services, and prefer optimistic versions when conflicts are rare.

**Short answer (say this first, 20-40 sec):**
Yes, I would use pessimistic locking when only one worker must update a row now, for example `SELECT ... FOR UPDATE` inside a transaction. I would use optimistic locking with a version column when conflicts are rare, such as editing user settings. For job queues, `FOR UPDATE SKIP LOCKED` lets multiple workers safely claim different rows.

**Detailed answer:**
PostgreSQL job claim pattern:
```sql
BEGIN;
WITH job AS (
  SELECT id
  FROM jobs
  WHERE status = 'READY'
  ORDER BY created_at
  FOR UPDATE SKIP LOCKED
  LIMIT 1
)
UPDATE jobs j
SET status = 'RUNNING', started_at = now()
FROM job
WHERE j.id = job.id
RETURNING j.*;
COMMIT;
```

JPA optimistic locking:
```java
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
class Account {
    @Id Long id;
    long balance;
    @Version long version;
}
```

**Common mistakes:**
- Using locks outside an explicit transaction.
- Holding locks while calling external APIs.
- Using pessimistic locks for all updates even when optimistic locking is cheaper.

**Follow-ups:**
- *What does SKIP LOCKED do?* → It skips rows currently locked by another transaction, useful for work queues.
- *Can SELECT FOR UPDATE block?* → Yes, unless you use `NOWAIT`, `SKIP LOCKED`, or a lock timeout.

## Section 2 · SQL Core Concepts

### Q4. Explain INNER, LEFT, RIGHT, FULL, CROSS, and SELF joins.
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Joins appear in almost every SQL interview problem.

**Beginner lens:** Join types decide which rows survive when tables meet. With customers C1 and C2 and orders
only for C1, INNER returns C1's matching orders; LEFT keeps C2 with missing order fields; RIGHT is the mirror;
FULL keeps unmatched rows from both sides; CROSS makes every pair; SELF compares rows in the same table. The
mental model is match condition plus row-preservation side. They want you to predict output cardinality, not
recite names. MySQL has no native FULL OUTER JOIN, so emulate it carefully when needed.

**Short answer (say this first, 20-40 sec):**
An inner join returns matching rows from both tables. A left join returns all left rows plus matching right rows, with NULLs when unmatched. Right join is symmetric, full join keeps unmatched rows from both sides, cross join returns a Cartesian product, and self join joins a table to itself.

**Detailed answer:**
```mermaid
flowchart LR
  A["employees"] --> B{"join condition"}
  C["departments"] --> B
  B --> D["INNER keeps matches"]
  B --> E["LEFT keeps all employees"]
  B --> F["FULL keeps both sides"]
  B --> G["CROSS creates combinations"]
```
```sql
SELECT e.name, d.name AS department
FROM employees e
LEFT JOIN departments d ON d.id = e.department_id;

SELECT e.name AS employee, m.name AS manager
FROM employees e
LEFT JOIN employees m ON m.id = e.manager_id;
```

**Common mistakes:**
- Putting right-table filters in `WHERE` after a `LEFT JOIN`, accidentally turning it into an inner join.
- Forgetting join predicates and creating a Cartesian product.

**Follow-ups:**
- *When use FULL OUTER JOIN?* → Reconciliation between source and target systems.
- *Does MySQL support FULL OUTER JOIN?* → Not directly; emulate with `LEFT JOIN UNION RIGHT JOIN`.

### Q5. What is the difference between WHERE and HAVING?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** It checks whether you understand filtering before versus after aggregation.

**Beginner lens:** WHERE is the bouncer before grouping; HAVING is the judge after groups exist. If employees
have departments, WHERE active = true removes inactive rows before counts are built, while HAVING count >= 5
keeps only departments with enough active people. The key distinction is row filter versus group filter. This
question checks whether aggregates live at the right stage. A common error is putting count conditions in
WHERE, or using HAVING for ordinary base-column filters and forcing extra work.

**Short answer (say this first, 20-40 sec):**
`WHERE` filters rows before grouping. `HAVING` filters groups after `GROUP BY` and aggregate calculation. Use `WHERE` for base columns and `HAVING` for aggregate conditions like `COUNT(*) > 5`.

**Detailed answer:**
```sql
SELECT department_id, COUNT(*) AS employee_count
FROM employees
WHERE active = true
GROUP BY department_id
HAVING COUNT(*) >= 5;
```
`WHERE active = true` reduces rows first; `HAVING COUNT(*) >= 5` keeps only departments with at least five active employees.

**Common mistakes:**
- Using `HAVING` for non-aggregate filters, making the query do more work.
- Trying to use aggregate functions in `WHERE`.

**Follow-ups:**
- *Can HAVING be used without GROUP BY?* → Yes, the whole result can be treated as one group in many databases.
- *Can WHERE use SELECT aliases?* → Usually no because `WHERE` runs before SELECT logically.

### Q6. What is the logical execution order of a SQL query?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** It explains many alias, filter, and aggregate mistakes.

**Beginner lens:** A query's logical order is like assembling a report: choose tables, join them, filter rows,
form groups, compute aggregates, filter groups, calculate window values, project columns, sort, then limit.
The optimizer may use a different physical plan, but the meaning follows this order. That explains why a
SELECT alias is usually not visible in WHERE and why window functions often need a subquery before filtering.
Interviewers listenfor semantic reasoning. The production trap is debugging syntax by trial instead of knowing
which step creates each value.

**Short answer (say this first, 20-40 sec):**
The logical order is `FROM/JOIN`, `WHERE`, `GROUP BY`, aggregates, `HAVING`, window functions, `SELECT`, `DISTINCT`, `ORDER BY`, then `LIMIT/OFFSET`. The optimizer can physically execute differently, but the logical semantics follow this order.

**Detailed answer:**
```mermaid
flowchart TD
  A["FROM and JOIN"] --> B["WHERE"]
  B --> C["GROUP BY"]
  C --> D["Aggregates"]
  D --> E["HAVING"]
  E --> F["Window functions"]
  F --> G["SELECT"]
  G --> H["DISTINCT"]
  H --> I["ORDER BY"]
  I --> J["LIMIT or OFFSET"]
```
This is why a `WHERE` clause cannot reference a window function directly; wrap it in a subquery or CTE.

**Common mistakes:**
- Thinking `SELECT` happens first because it is written first.
- Filtering window-function output in `WHERE` instead of an outer query.

**Follow-ups:**
- *Can ORDER BY use SELECT aliases?* → Usually yes because it is logically after SELECT.
- *Is physical execution always this order?* → No, optimizers reorder operations if semantics are preserved.

### Q7. How does GROUP BY work, and what columns can appear in SELECT?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Many candidates write nondeterministic aggregate queries.

**Beginner lens:** GROUP BY makes one output row per group, like sorting receipts into envelopes by department
and then totaling each envelope. After grouping, the SELECT list may show the group key, such as department,
and aggregate values, such as count or sum. A non-grouped employee name is ambiguous because many names may be
inside one envelope. The interviewer is checking deterministic thinking. PostgreSQL enforces this strictly;
MySQL allows unsaferesults if ONLY_FULL_GROUP_BY is disabled, so do not rely on that behavior.

**Short answer (say this first, 20-40 sec):**
`GROUP BY` collapses rows into groups. The SELECT list can contain grouped columns and aggregates over each group. PostgreSQL enforces this strictly; MySQL may allow non-grouped columns depending on `ONLY_FULL_GROUP_BY`, but relying on that is unsafe.

**Detailed answer:**
```sql
SELECT department_id, AVG(salary) AS avg_salary
FROM employees
GROUP BY department_id;
```
Invalid in standard SQL:
```sql
SELECT department_id, name, AVG(salary)
FROM employees
GROUP BY department_id;
```
`name` is ambiguous because a department has many employees.

**Common mistakes:**
- Selecting non-grouped, non-aggregated columns.
- Grouping by too many columns and changing the intended granularity.

**Follow-ups:**
- *How to get employee with max salary per department?* → Use `ROW_NUMBER()` or join to grouped max result.
- *What is aggregation granularity?* → The level at which one output row is produced.

### Q8. Explain SQL NULL semantics.
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** NULL bugs cause subtle production filtering errors.

**Beginner lens:** NULL means unknown or missing, not zero, false, or an empty string. If a customer's
middle_name is NULL, middle_name = NULL is not true; the result is UNKNOWN, so use IS NULL. The mental model
is three-valued logic: true, false, and unknown. This question hunts for filtering bugs around NOT IN,
aggregates, and optional columns. Remember that count(column) ignores NULL values while count(*) counts rows.
Prefer NOT EXISTS when a subquery might return NULL.

**Short answer (say this first, 20-40 sec):**
NULL means unknown or missing, not zero or empty string. Comparisons with NULL return UNKNOWN, so use `IS NULL` or `IS NOT NULL`. Aggregates usually ignore NULL except `COUNT(*)`, which counts rows.

**Detailed answer:**
```sql
SELECT * FROM users WHERE email IS NULL;       -- correct
SELECT * FROM users WHERE email = NULL;        -- wrong
SELECT COUNT(*), COUNT(email) FROM users;      -- rows vs non-null emails
```
`NOT IN` is dangerous if the subquery returns NULL; prefer `NOT EXISTS`.

**Common mistakes:**
- Comparing with `= NULL`.
- Assuming `NULL <> 5` is true; it is UNKNOWN.
- Using `NOT IN` against nullable columns.

**Follow-ups:**
- *How do you replace NULL?* → `COALESCE(value, fallback)`.
- *Can unique indexes contain NULLs?* → Behavior differs; PostgreSQL allows multiple NULLs in a normal unique index.

### Q9. What is the difference between UNION and UNION ALL?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** It checks correctness and performance awareness.

**Beginner lens:** UNION ALL stacks two result sets and keeps every row, like pouring two bags of tickets into
one box. UNION addsa duplicate-removal step, so equal rows collapse to one ticket. The distinction is bag
semantics versus set semantics, and it affects both correctness and cost. Interviewers expect you to choose
UNION ALL when duplicates are impossible or acceptable. The trap is using UNION as a reflex, causing an
unnecessary sort orhash distinct and sometimes hiding data-quality duplicates you should investigate.

**Short answer (say this first, 20-40 sec):**
`UNION ALL` concatenates results and keeps duplicates. `UNION` removes duplicates, usually requiring sort or hash distinct. Prefer `UNION ALL` when duplicates are acceptable or impossible.

**Detailed answer:**
```sql
SELECT email FROM customers
UNION ALL
SELECT email FROM newsletter_signups;
```
Use `UNION` only when you need set semantics.

**Common mistakes:**
- Using `UNION` by default and paying unnecessary distinct cost.
- Forgetting both queries must return compatible column counts and types.

**Follow-ups:**
- *Which is faster?* → Usually `UNION ALL` because it avoids duplicate elimination.
- *Does UNION sort final output?* → Do not rely on it; use `ORDER BY` explicitly.

### Q10. Compare DELETE, TRUNCATE, and DROP.
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** They test operational safety.

**Beginner lens:** DELETE, TRUNCATE, and DROP remove different things. DELETE is a row operation and can
remove only cancelled orders with a WHERE clause. TRUNCATE empties the whole table faster but is more
operationally heavy. DROP removes the table definition itself. The mental model is rows versus all rows versus
object. PostgreSQL TRUNCATE is transactional, while MySQL often treats TRUNCATE as Data Definition Language
(DDL) with an implicit commit. Interviewers are testing safety: never run a broad destructive command without
backup, scope, and permissions.

**Short answer (say this first, 20-40 sec):**
`DELETE` removes selected rows and can use `WHERE`. `TRUNCATE` quickly removes all rows from a table and may reset identity depending on DB/options. `DROP` removes the table definition itself.

**Detailed answer:**
| Command | Removes | WHERE? | Object remains? |
|---|---:|---:|---:|
| DELETE | rows | yes | yes |
| TRUNCATE | all rows | no | yes |
| DROP | table object | no | no |
```sql
DELETE FROM orders WHERE created_at < CURRENT_DATE - INTERVAL '2 years';
TRUNCATE TABLE staging_orders;
DROP TABLE old_staging_orders;
```

**Common mistakes:**
- Running DELETE without WHERE.
- Using TRUNCATE where foreign keys or audit triggers matter.

**Follow-ups:**
- *Can DELETE be rolled back?* → Yes inside a transaction in transactional engines.
- *Is TRUNCATE always rollback-safe?* → Depends on database; PostgreSQL supports transactional TRUNCATE.

### Q11. What are window functions?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Window functions solve ranking, running totals, and analytics problems cleanly.

**Beginner lens:** Window functions add a calculation beside each row instead of collapsing rows. For every
employee row, avg salary over department can show that employee and the department average together. PARTITION
BY chooses the mini-table, ORDER BY sets sequence, and the frame chooses nearby rows. The important
distinction from GROUP BY is that original rows remain visible. The signal is ranking, running total,
top-per-group, or comparison-with-neighbor wording. Watch the default frame when ORDER BY is present; it may
surprise running-total answers.

**Short answer (say this first, 20-40 sec):**
A window function calculates a value across related rows while keeping each original row. It uses `OVER`, optional `PARTITION BY`, optional `ORDER BY`, and optional frame clauses. Unlike `GROUP BY`, it does not collapse rows.

**Detailed answer:**
```sql
SELECT employee_id, department_id, salary,
       AVG(salary) OVER (PARTITION BY department_id) AS dept_avg
FROM employees;
```
Each employee row remains visible, with the department average attached.

**Common mistakes:**
- Expecting window functions to reduce rows.
- Forgetting deterministic `ORDER BY` in ranking functions.

**Follow-ups:**
- *Can window functions be used in WHERE?* → Not directly; use a subquery or CTE.
- *What is PARTITION BY?* → It splits the result into independent windows.

### Q12. Compare ROW_NUMBER, RANK, and DENSE_RANK.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Top-N-per-group questions depend on choosing the correct ranking function.

**Beginner lens:** ROW_NUMBER, RANK, and DENSE_RANK answer tie handling. For scores 100, 100, and 90,
ROW_NUMBER still produces 1, 2, 3; RANK gives 1, 1, 3; DENSE_RANK gives 1, 1, 2. The mental model is whether
ties share a label and whether gaps appear afterward. Interviewers use this for top-N-per-group problems. A
trap is choosing ROW_NUMBER when all tied winners should be returned, or DENSE_RANK when business rules
require Olympic-style gaps.

**Short answer (say this first, 20-40 sec):**
`ROW_NUMBER` gives a unique sequence even for ties. `RANK` gives same rank to ties and leaves gaps. `DENSE_RANK` gives same rank to ties without gaps.

**Detailed answer:**
```sql
SELECT name, salary,
       ROW_NUMBER() OVER (ORDER BY salary DESC) AS rn,
       RANK()       OVER (ORDER BY salary DESC) AS rnk,
       DENSE_RANK() OVER (ORDER BY salary DESC) AS drnk
FROM employees;
```
For salaries `100, 100, 90`, row numbers are `1,2,3`, rank is `1,1,3`, dense rank is `1,1,2`.

**Common mistakes:**
- Using `ROW_NUMBER` when ties should be included.
- Not adding a secondary sort for deterministic results.

**Follow-ups:**
- *Top 3 salaries with ties?* → Use `DENSE_RANK() <= 3`.
- *Exactly 3 rows per department?* → Use `ROW_NUMBER() <= 3`.

### Q13. What are LAG and LEAD used for?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** They appear in trend, retention, and consecutive-day problems.

**Beginner lens:** LAG and LEAD look to a neighboring row inside an ordered window. In monthly revenue,
LAG(revenue) can place last month's value next to this month's value so growth is a subtraction, not a self
join. The mental model is previous or next after partitioning and ordering. Interviewers are testing whether
you can express trends, gaps, churn, or before-after comparisons cleanly. The common bug is omitting a stable
ORDER BY, which makes previous row meaningless when timestamps tie.

**Short answer (say this first, 20-40 sec):**
`LAG` reads a previous row's value and `LEAD` reads a following row's value within a window. They are useful for comparing current versus previous values, detecting gaps, and calculating changes over time.

**Detailed answer:**
```sql
SELECT user_id, login_date,
       LAG(login_date) OVER (PARTITION BY user_id ORDER BY login_date) AS prev_login
FROM logins;
```
For month-over-month growth:
```sql
SELECT month, revenue,
       revenue - LAG(revenue) OVER (ORDER BY month) AS delta
FROM monthly_revenue;
```

**Common mistakes:**
- Omitting `ORDER BY`, making previous row undefined.
- Assuming `LAG` skips NULL automatically; it does not in most DBs.

**Follow-ups:**
- *How to supply a default?* → `LAG(value, 1, 0)`.
- *Can it cross users?* → Not if you use `PARTITION BY user_id`.

### Q14. Explain running totals and window frames.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Running totals are common in analytics interviews.

**Beginner lens:** A running total is a windowed sum whose frame grows as the ordered rows advance. For daily
sales of 5, 7, and 3, the running totals are 5, 12, and 15. The key idea is that ORDER BY gives sequence,
while the frame says which rows count for each current row. ROWS means physical row positions; RANGE groups
peers with equal sort values. Interviewers are checking frame awareness. In PostgreSQL and MySQL 8, default
RANGE behavior can duplicate totals across same-date rows unless you specify ROWS.

**Short answer (say this first, 20-40 sec):**
A running total uses an ordered window and a frame from the beginning to the current row. Frames control which rows participate in the calculation. Use `ROWS` for physical row counts and `RANGE` for peer groups with equal sort values.

**Detailed answer:**
```sql
SELECT order_date, amount,
       SUM(amount) OVER (
         ORDER BY order_date
         ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
       ) AS running_total
FROM orders;
```
Moving average: `AVG(amount) OVER (ORDER BY order_date ROWS BETWEEN 6 PRECEDING AND CURRENT ROW)`.

**Common mistakes:**
- Forgetting frame behavior differs by DB and function.
- Using `RANGE` when duplicate dates should not be grouped together.

**Follow-ups:**
- *What is a frame?* → The subset of rows inside a partition used by the current window calculation.
- *Why add a tiebreaker to ORDER BY?* → To make running totals deterministic.

### Q15. What are CTEs and recursive CTEs?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** CTEs improve readability and recursive CTEs solve hierarchy questions.

**Beginner lens:** A Common Table Expression (CTE) is a named result used only by the following query, like
labeling a subquery as active_customers before joining it. A recursive CTE has an anchor row set and a
recursive step, useful for employee-manager trees. The distinction is readability helper versus loop-like
hierarchy traversal. The interviewer wants to see termination thinking and optimizer awareness. PostgreSQL 12
and newer may inline non-recursive CTEs unless materialization is requested, so do not assume every CTE is an
optimization fence.

**Short answer (say this first, 20-40 sec):**
A CTE is a named temporary result inside one query, defined with `WITH`. A recursive CTE has an anchor query and a recursive query, useful for trees like employee-manager hierarchies.

**Detailed answer:**
```sql
WITH RECURSIVE org AS (
  SELECT id, name, manager_id, 1 AS level
  FROM employees
  WHERE manager_id IS NULL
  UNION ALL
  SELECT e.id, e.name, e.manager_id, org.level + 1
  FROM employees e
  JOIN org ON e.manager_id = org.id
)
SELECT * FROM org ORDER BY level, id;
```

**Common mistakes:**
- Creating infinite recursion by missing termination conditions.
- Assuming CTEs are always materialized; PostgreSQL 12+ can inline non-recursive CTEs unless forced.

**Follow-ups:**
- *CTE vs subquery?* → CTE is often more readable; optimizer behavior differs by DB/version.
- *How prevent cycles?* → Track visited ids or enforce acyclic constraints.

## Section 3 · Indexes and Query Optimization

### Q16. How does a B+Tree index work?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Most OLTP indexes are B+Trees, so internals explain performance.

**Beginner lens:** A B+Tree index is like a balanced directory where internal pages guide you to sorted leaf
pages. To find customer 42, the database walks a few pages, lands near the key, and then scans adjacent leaves
for ranges or ordering. The model is logarithmic seek plus cheap ordered walk. Interviewers want to know why
equality, range, and ORDER BY can be fast. Production cost matters: every insert or key update must maintain
the tree, and low-selectivity predicates may still prefer a table scan.

**Short answer (say this first, 20-40 sec):**
A B+Tree stores sorted keys in a balanced tree. Internal pages guide search, and leaf pages contain key entries pointing to rows or clustered records. It supports equality, range scans, ordering, and prefix lookups efficiently.

**Detailed answer:**
```mermaid
flowchart TD
  R["Root page keys 30 and 70"] --> L["Leaf keys 1 to 29"]
  R --> M["Leaf keys 30 to 69"]
  R --> N["Leaf keys 70 to 99"]
  L -->|"next leaf"| M
  M -->|"next leaf"| N
```
Search is roughly `O(log n)` page reads; range scan finds the first key then walks linked leaves.

**Common mistakes:**
- Saying B+Tree is only for equality.
- Forgetting it can avoid sorting when `ORDER BY` matches index order.

**Follow-ups:**
- *Why are leaves linked?* → Efficient range scans.
- *Why not hash for ranges?* → Hash indexes do equality only.

### Q17. What is the difference between clustered and non-clustered indexes?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** MySQL InnoDB performance depends heavily on clustered primary keys.

**Beginner lens:** In a clustered index, the table rows live in the index order. MySQL InnoDB clusters rows by
the primary key, so a secondary index lookup may first find the primary key and then fetch the clustered row. A
non-clustered index is a separate structure pointing to rows. PostgreSQL stores heap rows separately; its
CLUSTER command rewrites a table but does not keep that order forever. The recognition signal is primary-key
choice and lookup path. A random large primary key can hurt InnoDB locality.

**Short answer (say this first, 20-40 sec):**
A clustered index defines table row storage order; InnoDB clusters data by primary key. A non-clustered secondary index stores key values plus a row locator. PostgreSQL does not maintain permanent clustered order, though `CLUSTER` can rewrite a table once.

**Detailed answer:**
In InnoDB, primary key leaf pages contain full rows; secondary index leaf pages contain secondary key plus primary key, so a secondary lookup may require another primary-key lookup. PostgreSQL indexes point to heap tuple locations.

**Common mistakes:**
- Assuming every database has one clustered index like SQL Server/InnoDB.
- Choosing random UUID primary keys without considering locality and page splits.

**Follow-ups:**
- *What is a covering index?* → An index containing all columns needed by a query.
- *Is PostgreSQL primary key clustered?* → No, not automatically.

### Q18. Explain composite indexes and the leftmost-prefix rule.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Composite index order is a frequent real-world tuning topic.

**Beginner lens:** A composite index sorts by the first column, then the second within each first-column
value, and so on. For an index on customer_id, status, created_at, a query with customer_id and status can
jump to a tight ordered slice; a query with only status cannot use the leading order as well. The mental model
is phone book sorting by last name before first name. Interviewers are testing index order, not just index
presence. Equality columns usually lead, then range or ordering columns, but workload decides.

**Short answer (say this first, 20-40 sec):**
A composite index stores multiple columns in sorted order. It is most useful when predicates use the leftmost columns. Equality columns usually come first, then range/order columns, but selectivity and query patterns matter.

**Detailed answer:**
For index `(customer_id, status, created_at)`:
```sql
WHERE customer_id = 1
WHERE customer_id = 1 AND status = 'PAID'
WHERE status = 'PAID' -- usually not left-prefix usable
WHERE customer_id = 1 ORDER BY status, created_at
```

**Common mistakes:**
- Creating separate single-column indexes instead of one useful composite index.
- Putting a column first without checking workload.

**Follow-ups:**
- *Where should range columns go?* → Usually after equality columns because range limits use of later keys.
- *Can optimizers combine indexes?* → Sometimes, but a good composite index is often better.

### Q19. What is a covering index and an index-only scan?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** It connects index design to fewer table reads.

**Beginner lens:** A covering index contains every column a query needs, so the engine may avoid visiting the
base table. For an order list showing id, status, and created_at, an index that includes those values can
answer the page from the index alone. PostgreSQL still needs the visibility map to allow an index-only scan
without heap checks; MySQL InnoDB can avoid the extra clustered lookup when a secondary index covers the
query. The interviewer is testing read path depth. Over-covering indexes increases storage and write
maintenance.

**Short answer (say this first, 20-40 sec):**
A covering index contains all columns needed by a query, so the engine can answer from the index. PostgreSQL can do an index-only scan only when the visibility map says heap rows are visible enough; otherwise it still checks heap tuples.

**Detailed answer:**
```sql
CREATE INDEX idx_orders_cover
ON orders(customer_id, status, created_at DESC)
INCLUDE (total_amount);
```
This can serve:
```sql
SELECT created_at, total_amount
FROM orders
WHERE customer_id = 42 AND status = 'PAID'
ORDER BY created_at DESC
LIMIT 10;
```

**Common mistakes:**
- Adding too many columns and hurting write performance.
- Expecting PostgreSQL index-only scans before VACUUM updates visibility information.

**Follow-ups:**
- *What is INCLUDE?* → Non-key columns stored for coverage, not search order.
- *Why help APIs?* → Fewer random table reads and less IO.

### Q20. When will a database not use an index?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** This is a core query-tuning interview trap.

**Beginner lens:** An index is useful only when the optimizer believes it reduces work. If most rows have
status = ACTIVE, reading the table may be cheaper than bouncing through an index. Functions on indexed
columns, mismatchedtypes, leading wildcard searches, stale statistics, and missing leftmost composite columns
can also block efficient use. The mental model is search-argument-able predicates plus selectivity. This
question checks whether you know that indexes are optional tools, not promises. The trap is forcing an index
instead of fixing query shape or statistics.

**Short answer (say this first, 20-40 sec):**
An index may not be used when the predicate is not sargable, selectivity is low, types are implicitly cast, a leading wildcard is used, OR conditions are poorly structured, stats are stale, or the optimizer estimates a scan is cheaper.

**Detailed answer:**
```sql
WHERE LOWER(email) = 'a@x.com'   -- needs expression index or normalized column
WHERE name LIKE '%sam%'          -- leading wildcard blocks B+Tree seek
WHERE numeric_col = '42'         -- implicit cast may hurt
WHERE status = 'ACTIVE'          -- low selectivity may prefer scan
WHERE a = 1 OR b = 2             -- may need rewrite or separate indexes
```

**Common mistakes:**
- Thinking index existence guarantees usage.
- Ignoring selectivity and table size.

**Follow-ups:**
- *What is sargable?* → Search-argument-able; the engine can use an index seek/range scan.
- *How handle `LOWER(email)`?* → Store normalized email or create an expression index.

### Q21. Compare B-Tree, hash, GIN, and GiST indexes.
`🔴 Advanced` `Stretch`

**Why they ask:** PostgreSQL-heavy roles may ask index-type tradeoffs.

**Beginner lens:** B-Tree, hash, Generalized Inverted Index (GIN), and Generalized Search Tree (GiST) indexes
serve different operators. B-Tree handles equality, ranges, and ordering. Hash is narrow equality lookup. GIN
is good when one row contains many searchable items, such as arrays, JavaScript Object Notation (JSONB), or
full-text tokens. GiST supports ranges, geometry, and nearest-neighbor style operators. The interviewer is
checking operator-to-index matching. For vector search, pgvector commonly uses specialized approximate
indexes, not an ordinary B-Tree over the vector.

**Short answer (say this first, 20-40 sec):**
B-Tree is the default for equality, range, and ordering. Hash is mainly equality-only. GIN indexes composite values like arrays, JSONB, and full-text tokens. GiST is a framework for geometry, range, nearest-neighbor, and custom operator classes.

**Detailed answer:**
| Index | Best for | Example |
|---|---|---|
| B-Tree | `=`, ranges, `ORDER BY` | `email`, `created_at` |
| Hash | equality only | exact lookup |
| GIN | contains/search inside values | `jsonb`, arrays, full text |
| GiST | ranges, geometry, KNN | PostGIS, range overlap |
For pgvector, index choice depends on extension version and metric, commonly HNSW/IVFFlat rather than ordinary B-Tree.

**Common mistakes:**
- Creating B-Tree on JSONB and expecting containment queries to be fast.
- Treating all indexes as equivalent.

**Follow-ups:**
- *What index for `jsonb @>`?* → Usually GIN.
- *What index for text contains search?* → Full-text GIN or trigram GIN/GiST depending query.

### Q22. How do you read EXPLAIN and EXPLAIN ANALYZE?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** This validates your 70% optimization story.

**Beginner lens:** EXPLAIN is the map; EXPLAIN ANALYZE is the trip with a stopwatch. A plan node that expected
100 rows but processed 1,000,000 points to bad statistics or a poor predicate. Look at scan type, join type,
loops, actual time, buffers, and sorts spilling to disk. PostgreSQL EXPLAIN ANALYZE runs the query, so avoid it
casually on writes unless wrapped safely. MySQL has EXPLAIN ANALYZE in modern 8.0 releases. Interviewers want
evidence-based tuning rather than guessing from the query text.

**Short answer (say this first, 20-40 sec):**
`EXPLAIN` shows the planned execution; `EXPLAIN ANALYZE` actually runs the query and shows real time and rows. I compare estimated rows to actual rows, scan type, join type, sort/hash memory, buffers, and whether expensive nodes process too many rows.

**Detailed answer:**
Checklist: scan type, join type, estimated vs actual rows, loops, buffer reads/hits, sort spill, and where the most time is spent.
```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT c.id, SUM(o.total_amount)
FROM customers c
JOIN orders o ON o.customer_id = c.id
WHERE o.created_at >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY c.id;
```

**Common mistakes:**
- Running `EXPLAIN ANALYZE` on dangerous writes without wrapping safely.
- Looking only at the top cost and not child nodes.

**Follow-ups:**
- *What does row misestimate mean?* → Stats/correlation may be wrong, leading to bad plans.
- *What is a bitmap scan?* → Build bitmap of matching heap pages, then fetch them efficiently.

### Q23. How do you find missing and unused indexes?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** They want practical tuning, not just syntax.

**Beginner lens:** Missing indexes are found from pain: slow statements, high-frequency paths, and plans that
scan too much. Unused indexes are found from usage statistics over a real workload window, not from a quiet
afternoon. The key tradeoff is read speed versus write cost, disk, cache pressure, and migration risk.
Interviewers are checking operational judgment. A production concern is seasonal or rare reporting queries: an
index that looks unused this week may be critical at month end, so review ownership before dropping it.

**Short answer (say this first, 20-40 sec):**
For missing indexes, start from slow queries and execution plans. For unused indexes, check index usage stats over a meaningful time window. Remove unused indexes carefully because indexes help reads but slow writes and consume space.

**Detailed answer:**
```sql
-- PostgreSQL
SELECT relname, seq_scan, idx_scan FROM pg_stat_user_tables ORDER BY seq_scan DESC;
SELECT indexrelname, idx_scan FROM pg_stat_user_indexes ORDER BY idx_scan ASC;

-- MySQL
SELECT * FROM sys.schema_unused_indexes;
```

**Common mistakes:**
- Dropping an index after observing a quiet period.
- Ignoring indexes used for constraints.

**Follow-ups:**
- *How validate a new index?* → Compare plans and latency before/after in staging or safe rollout.
- *Can too many indexes hurt?* → Yes, every insert/update/delete maintains them.

### Q24. What is table partitioning and when is it useful?
`🔴 Advanced` `Stretch`

**Why they ask:** Large-table design appears in backend and data roles.

**Beginner lens:** Partitioning keeps one logical table while splitting storage into child pieces, such as
orders_2026_01 and orders_2026_02. If queries include the partition key, the database can prune irrelevant
partitions and makemaintenance or archiving easier. The mental model is manageability and pruning, not magic
speed. Interviewerslook for range, list, or hash partition choices tied to access patterns. A trap is
partitioning without filtering on the partition key; another is forgetting unique constraints and foreign keys
have database-specific limitations.

**Short answer (say this first, 20-40 sec):**
Partitioning splits one logical table into physical partitions by range, list, or hash. It helps pruning, maintenance, archiving, and sometimes write/read scalability. It is not a substitute for good indexes.

**Detailed answer:**
```sql
CREATE TABLE events (
  id bigint,
  event_time timestamp not null,
  payload jsonb
) PARTITION BY RANGE (event_time);
CREATE TABLE events_2026_09 PARTITION OF events
FOR VALUES FROM ('2026-09-01') TO ('2026-10-01');
```
Use for time-series logs, monthly orders, or tenant splits when queries include partition key.

**Common mistakes:**
- Partitioning small tables.
- Querying without partition key and scanning all partitions.

**Follow-ups:**
- *Partitioning vs sharding?* → Partitioning is usually within one DB; sharding distributes across nodes.
- *What is pruning?* → Optimizer skips impossible partitions.

### Q25. Give a query optimization checklist.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** They want a structured debugging approach.

**Beginner lens:** A query optimization checklist is a disciplined loop: measure the slow path, capture the
exact statement andparameters, read the execution plan, reduce rows early, choose indexes that match filters
and ordering, fetchfewer columns, and remeasure. Think of a grocery checkout line: moving work earlier only
helps if it removesreal items from the belt. The interviewer is testing structure under pressure. Do not
ignore lock waits, connection pool saturation, stale statistics, or Object-Relational Mapping (ORM) N+1
behavior outside the single statement.

**Short answer (say this first, 20-40 sec):**
Measure first, inspect the plan, reduce rows early, use correct indexes, avoid non-sargable predicates, fetch only needed columns, fix N+1 queries, check locks, and verify with before/after metrics.

**Detailed answer:**
1. Capture SQL, params, frequency, and p95.
2. Run `EXPLAIN ANALYZE`.
3. Check row estimates and scan/join types.
4. Add/reorder composite indexes based on predicates and ordering.
5. Avoid `SELECT *`, functions/casts on indexed columns, and accidental cross joins.
6. Check connection pool, lock waits, transaction time, stats, and vacuum/analyze.
7. Benchmark before/after and monitor write overhead.

**Common mistakes:**
- Optimizing rare queries while ignoring high-frequency medium queries.
- Measuring locally on tiny data.

**Follow-ups:**
- *What is selectivity?* → Fraction of rows matching a predicate; high selectivity usually benefits indexes.
- *How can an index hurt?* → More write cost, memory, storage, and optimizer complexity.

### Q26. How would you tune `WHERE customer_id = ? AND status = ? ORDER BY created_at DESC LIMIT 20`?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** It tests composite index design with filtering and ordering.

**Beginner lens:** For customer_id, status, created_at descending, the useful index usually starts with the
equality filters and ends with the ordering column. The database can seek to one customer and status, then
read newest orders already sorted for LIMIT 20. The mental model is a narrow ordered slice, not separate
indexes glued together. Interviewers are testing composite index design. If status is optional for many
requests, a second index such as customer_id, created_at may win. Add a deterministic tie breaker like id for
pagination.

**Short answer (say this first, 20-40 sec):**
I would consider a composite index on `(customer_id, status, created_at DESC)`. Equality predicates come first, then the ordering column, allowing the database to seek to a customer/status range and return latest rows without sorting.

**Detailed answer:**
```sql
CREATE INDEX idx_orders_customer_status_created
ON orders(customer_id, status, created_at DESC);
```
If the API selects only a few columns, use a covering index. If many queries omit status, evaluate `(customer_id, created_at DESC)` too.

**Common mistakes:**
- Indexing only `created_at`.
- Creating three separate indexes and expecting them to behave like one ordered composite index.

**Follow-ups:**
- *Should status be first?* → Usually no if every query filters customer first; index order follows workload.
- *What if the API pages deeply?* → Prefer keyset pagination using last `(created_at, id)`.

## Section 4 · Transactions, Isolation, Locks, and Deadlocks

### Q27. Explain ACID properties.
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** ACID is foundational for backend consistency.

**Beginner lens:** Atomicity, Consistency, Isolation, and Durability (ACID) describe what a transaction
promises. In a banktransfer, atomicity means debit and credit both happen or neither does; consistency means
constraints still hold; isolation controls what concurrent transfers can see; durability means a committed
transfer survives acrash through log recovery. The mental model is correctness around failures and
concurrency. Interviewers are checking whether you can connect terms to real invariants. A trap is saying
consistency means replicas are instantly synchronized; here it means database rules remain valid.

**Short answer (say this first, 20-40 sec):**
Atomicity means all-or-nothing. Consistency means constraints and invariants remain valid. Isolation means concurrent transactions behave according to an isolation level. Durability means committed data survives crashes.

**Detailed answer:**
| Property | Meaning | Example |
|---|---|---|
| Atomicity | rollback if any step fails | transfer debit and credit together |
| Consistency | rules remain valid | balance cannot go below allowed limit |
| Isolation | concurrent transactions controlled | two purchases do not oversell stock |
| Durability | commit persists | WAL/redo log replay after crash |

**Common mistakes:**
- Defining consistency as eventual consistency.
- Assuming isolation always means serial execution.

**Follow-ups:**
- *Which property uses WAL?* → Durability and crash recovery.
- *What breaks atomicity?* → DB update plus external API call without compensation/outbox.

### Q28. Explain isolation levels and anomalies.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Isolation appears in payment, inventory, and booking systems.

**Beginner lens:** Isolation levels decide which concurrent effects a transaction may observe. Read Committed
blocks dirty reads but can see a changed row on a later statement. Repeatable Read often gives a stable
snapshot, but details differ. Serializable aims to behave like one-at-a-time execution, sometimes by aborting
transactions that must be retried. The interviewer is testing anomaly vocabulary: dirty read, non-repeatable
read, phantom, and write skew. PostgreSQL Repeatable Read is snapshot isolation and can allow write skew;
MySQL InnoDB uses next-key locks for some locking range reads.

**Short answer (say this first, 20-40 sec):**
Isolation levels trade correctness for concurrency. Read Committed avoids dirty reads, Repeatable Read avoids non-repeatable reads in many databases, and Serializable aims to behave as if transactions ran one at a time. Snapshot-based systems can still have write skew unless serializable isolation detects it.

**Detailed answer:**
| Anomaly | Meaning | Read Uncommitted | Read Committed | Repeatable Read | Serializable |
|---|---|---:|---:|---:|---:|
| Dirty read | read uncommitted data | possible | prevented | prevented | prevented |
| Non-repeatable read | same row changes between reads | possible | possible | prevented in snapshot/RR | prevented |
| Phantom | new matching row appears | possible | possible | DB-specific | prevented |
| Write skew | different rows break invariant | possible | possible | possible in snapshot | prevented/detected |
PostgreSQL default is READ COMMITTED. MySQL InnoDB default is REPEATABLE READ.

**Common mistakes:**
- Assuming names mean identical behavior across databases.
- Ignoring write skew under snapshot isolation.

**Follow-ups:**
- *What is a dirty read?* → Reading data another transaction has not committed.
- *What is write skew?* → Transactions read same invariant then update different rows, violating it together.

### Q29. Compare MySQL default REPEATABLE READ and PostgreSQL default READ COMMITTED.
`🔴 Advanced` `Stretch`

**Why they ask:** This tests database-specific accuracy.

**Beginner lens:** PostgreSQL's default Read Committed gives each statement a fresh snapshot, so two selects
in one transaction can see different committed data. MySQL InnoDB's default Repeatable Read gives ordinary
consistent reads a transaction-level snapshot. However, InnoDB locking reads such as SELECT FOR UPDATE use
current reads and may take record, gap, or next-key locks. The key distinction is statement snapshot versus
transaction snapshot plus InnoDB range locking. Interviewers ask this to catch generic answers. Always name
the engine, becauseMySQL behavior means InnoDB here.

**Short answer (say this first, 20-40 sec):**
PostgreSQL defaults to READ COMMITTED, where each statement gets a new snapshot. MySQL InnoDB defaults to REPEATABLE READ, where a consistent-read transaction sees a stable snapshot. InnoDB also uses next-key locks for locking reads to prevent phantoms in ranges.

**Detailed answer:**
PostgreSQL READ COMMITTED: each `SELECT` sees rows committed before that statement starts, so two SELECTs in one transaction can see different committed versions. MySQL InnoDB REPEATABLE READ: consistent reads see a transaction-level snapshot, while locking reads such as `SELECT ... FOR UPDATE` can take record/gap/next-key locks.

**Common mistakes:**
- Saying PostgreSQL READ COMMITTED allows dirty reads; it does not.
- Ignoring InnoDB gap locks when explaining blocked inserts.

**Follow-ups:**
- *How to get stable snapshot in PostgreSQL?* → Use REPEATABLE READ transaction.
- *Can MySQL READ COMMITTED reduce gap locks?* → Often for some cases, but behavior is nuanced.

### Q30. What lock types should a backend engineer know?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Locks explain blocking, deadlocks, and correctness.

**Beginner lens:** Locks are traffic signals for data changes. A row lock protects one account row; a table
lock affects the whole table; shared locks allow compatible readers; exclusive locks block conflicting
writers. MySQL InnoDB also has gap and next-key locks around index ranges, while PostgreSQL uses different
mechanisms and predicate locks at Serializable isolation. The mental model is scope plus compatibility.
Interviewers want you to explain blocking and deadlocks. A common production issue is missing indexes causing
updates to scan andlock far more rows than expected.

**Short answer (say this first, 20-40 sec):**
Know row locks, table locks, shared locks, exclusive locks, gap locks, and next-key locks. Shared locks allow reads but block conflicting writes. Exclusive locks protect updates. Gap/next-key locks are important in InnoDB range locking.

**Detailed answer:**
| Lock | Meaning | Example |
|---|---|---|
| Row lock | lock specific row | `UPDATE account SET ... WHERE id=1` |
| Table lock | lock table-level operation | DDL, bulk ops |
| Shared | read lock | `SELECT ... FOR SHARE` |
| Exclusive | write lock | `SELECT ... FOR UPDATE` |
| Gap | lock space between records | InnoDB range protection |
| Next-key | record plus gap | InnoDB phantom prevention |

**Common mistakes:**
- Thinking normal SELECT always blocks writers under MVCC.
- Holding locks during slow network calls.

**Follow-ups:**
- *What lock does UPDATE take?* → Row-level exclusive lock on modified rows, plus DB-specific index/range locks.
- *What is lock escalation?* → Some DBs convert many fine locks to coarser locks; behavior varies.

### Q31. Explain gap locks and next-key locks.
`🔴 Advanced` `Stretch`

**Why they ask:** InnoDB deadlocks and blocked inserts often involve range locks.

**Beginner lens:** A gap lock protects the space between index records, and a next-key lock protects a record
plus the gap before it. If values are 10 and 20, a locked range may block inserting 15 even though row 15 does
not exist yet. This is mainly a MySQL InnoDB Repeatable Read locking-read topic used to prevent phantoms. The
interviewer is testing range-concurrency accuracy. PostgreSQL normal reads do not use gap locks; Serializable
isolation instead detects dangerous patterns and may abort transactions.

**Short answer (say this first, 20-40 sec):**
A gap lock locks the gap between index records, not a specific row. A next-key lock locks an index record plus the gap before it. InnoDB uses these in locking reads under repeatable read to prevent phantom rows in searched ranges.

**Detailed answer:**
If index values are `10, 20, 30`, `WHERE value BETWEEN 10 AND 20 FOR UPDATE` can lock records `10`, `20`, and gaps around them, blocking another transaction from inserting `15`.
```sql
-- MySQL InnoDB
START TRANSACTION;
SELECT * FROM coupons WHERE code BETWEEN 'A' AND 'M' FOR UPDATE;
```

**Common mistakes:**
- Expecting only existing rows to be locked.
- Running range locking queries without an index, causing broader locks.

**Follow-ups:**
- *Are gap locks in PostgreSQL normal reads?* → No; PostgreSQL uses different MVCC/SSI mechanisms.
- *How reduce gap-lock contention?* → Precise indexed predicates, shorter transactions, or lower isolation if safe.

### Q32. Show a deadlock example and how to prevent it.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Deadlocks are common in concurrent services.

**Beginner lens:** A deadlock is a waiting cycle: transaction A holds account 1 and wants account 2, while
transaction B holdsaccount 2 and wants account 1. The database detects the cycle and cancels one participant.
The mental model isnot total lock count but circular dependency. Interviewers are checking whether you design
for prevention and retry. Lock resources in a consistent order, keep transactions short, index predicates so
fewer rows are touched, and treat deadlock errors as expected transient failures in concurrent systems.

**Short answer (say this first, 20-40 sec):**
A deadlock happens when transactions wait on each other in a cycle. Databases detect it and abort one transaction. Prevent it by locking resources in a consistent order, keeping transactions short, using proper indexes, and retrying aborted transactions.

**Detailed answer:**
```mermaid
sequenceDiagram
  participant T1 as Transaction 1
  participant T2 as Transaction 2
  T1->>T1: lock account 1
  T2->>T2: lock account 2
  T1->>T2: waits for account 2
  T2->>T1: waits for account 1
  Note over T1,T2: database detects cycle and aborts one
```
```sql
-- Session 1
BEGIN;
UPDATE accounts SET balance = balance - 100 WHERE id = 1;
UPDATE accounts SET balance = balance + 100 WHERE id = 2;
-- Session 2 does ids in reverse order and deadlocks
```
Prevent by always locking lower account id first.

**Common mistakes:**
- Treating deadlocks as impossible bugs instead of retryable conflicts.
- Ignoring missing indexes that make updates scan and lock many rows.

**Follow-ups:**
- *What should application do on deadlock?* → Roll back and retry the whole transaction with backoff.
- *Where see deadlocks?* → MySQL `SHOW ENGINE INNODB STATUS`; PostgreSQL logs deadlock details.

### Q33. Compare optimistic and pessimistic locking.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** This is central to designing concurrent writes.

**Beginner lens:** Optimistic locking is a handshake at commit time: update the row only if version is still
7, otherwise someone changed it and you retry or report a conflict. Pessimistic locking is reserving the row
first with SELECT FOR UPDATE. The distinction is rare-conflict throughput versus immediate exclusion.
Interviewers are testing fit to workload: profile edits often use optimistic versions, while job claims or
inventory decrements may need pessimistic control. Trap: pessimistic locks require an active transaction and
should not cover slow external calls.

**Short answer (say this first, 20-40 sec):**
Optimistic locking assumes conflicts are rare and checks a version at update time. Pessimistic locking assumes conflicts are likely and locks rows before changes. Optimistic gives higher concurrency; pessimistic gives immediate exclusion.

**Detailed answer:**
| Approach | Use when | Mechanism |
|---|---|---|
| Optimistic | rare conflicts, user edits | version column, compare-and-swap |
| Pessimistic | inventory/job claims/money | `SELECT ... FOR UPDATE` |
```sql
UPDATE products
SET stock = stock - 1, version = version + 1
WHERE id = 10 AND version = 7 AND stock > 0;
```
If affected rows = 0, retry or report conflict.

**Common mistakes:**
- Using optimistic locking without checking affected rows.
- Holding pessimistic locks while doing slow work.

**Follow-ups:**
- *What does JPA `@Version` do?* → Adds version check to updates and throws on conflict.
- *Which for seat booking?* → Pessimistic or carefully designed atomic update, depending contention.

### Q34. How does SELECT FOR UPDATE SKIP LOCKED help job queues?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** It is a practical concurrency pattern.

**Beginner lens:** SELECT FOR UPDATE SKIP LOCKED turns a jobs table into a safe work queue. Worker A locks job
101; worker B skipsthat locked row and claims job 102 instead of waiting. The mental model is parallel
consumers taking different unlocked rows. Interviewers are checking practical concurrency and fairness
awareness. PostgreSQL and MySQL 8.0 support this pattern, but it can starve old locked or repeatedly failing
jobs. Keep claims short, mark attempts visibly, and have retry or timeout handling for crashed workers.

**Short answer (say this first, 20-40 sec):**
`FOR UPDATE` locks selected job rows so another worker cannot claim them. `SKIP LOCKED` makes other workers skip locked jobs instead of blocking. Together they allow parallel workers to claim distinct jobs safely.

**Detailed answer:**
```mermaid
flowchart LR
  A["Worker 1 selects job 101 FOR UPDATE"] --> B["Job 101 locked"]
  C["Worker 2 selects ready jobs"] --> D["SKIP LOCKED ignores job 101"]
  D --> E["Worker 2 claims job 102"]
```
```sql
WITH claimed AS (
  SELECT id FROM jobs
  WHERE status = 'READY'
  ORDER BY priority DESC, created_at
  FOR UPDATE SKIP LOCKED
  LIMIT 10
)
UPDATE jobs SET status = 'RUNNING'
WHERE id IN (SELECT id FROM claimed)
RETURNING *;
```

**Common mistakes:**
- Forgetting this must run inside a transaction.
- Not handling crashed workers with timeout/retry logic.

**Follow-ups:**
- *What if no rows returned?* → No available unlocked jobs; sleep/backoff.
- *Does SKIP LOCKED guarantee fairness?* → Not fully; design ordering and retries carefully.

### Q35. What are advisory locks?
`🔴 Advanced` `Stretch`

**Why they ask:** Advisory locks are useful for coarse application-level coordination.

**Beginner lens:** Advisory locks are database-managed mutexes whose meaning is defined by the application.
For example, hash tenant 42 into a lock key so only one billing rebuild for that tenant runs at a time. Unlike
row locks, the database does not know which rows the key protects. The mental model is coordination outside
normal foreign-key or row relationships. Interviewers are checking disciplined usage. PostgreSQL offers
session-level and transaction-level advisory locks; prefer transaction-scoped locks when possible so cleanup
follows rollback orcommit.

**Short answer (say this first, 20-40 sec):**
Advisory locks are application-defined locks managed by the database but not tied to a row automatically. PostgreSQL supports session-level and transaction-level advisory locks. They are useful for single-flight jobs, migrations, and tenant-level coordination.

**Detailed answer:**
```sql
BEGIN;
SELECT pg_advisory_xact_lock(12345);
-- do one-at-a-time work for resource 12345
COMMIT;
```
They require discipline because the database does not know what data the key represents.

**Common mistakes:**
- Using session locks and leaking them through pooled connections.
- Replacing proper constraints with informal locks.

**Follow-ups:**
- *Transaction vs session advisory lock?* → Transaction lock releases on commit/rollback; session lock releases when session ends.
- *Use advisory locks for inventory?* → Usually row locks or atomic updates are clearer.

### Q36. How do you investigate lock waits?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Slow APIs are often blocked, not CPU-bound.

**Beginner lens:** A lock wait investigation asks who is waiting, who is blocking, and how long the blocker
has been in a transaction. In PostgreSQL, pg_stat_activity and pg_locks reveal wait events, blocking process
identifiers, and query text. In MySQL InnoDB, performance_schema lock tables and SHOW ENGINE INNODB STATUS are
common tools. The mental model is blocked time, not slow computation. Interviewers want a safe response plan:
identify the blocker, understand business impact, then fix indexes, transaction boundaries, or code paths
rather than killingsessions blindly.

**Short answer (say this first, 20-40 sec):**
I check active sessions, wait events, blocking PIDs, query text, transaction age, and locks held. Then I identify the blocker and fix the code path, index, or transaction boundary.

**Detailed answer:**
PostgreSQL: inspect `pg_stat_activity`, `pg_locks`, `wait_event`, and blocking PIDs. MySQL: inspect `performance_schema.data_locks`, `data_lock_waits`, and `SHOW ENGINE INNODB STATUS`.
```sql
SELECT pid, wait_event_type, wait_event, state, query
FROM pg_stat_activity
WHERE wait_event_type IS NOT NULL;
```

**Common mistakes:**
- Killing sessions before understanding root cause.
- Ignoring long idle-in-transaction connections.

**Follow-ups:**
- *What is idle in transaction?* → A session opened a transaction and stopped, holding locks/snapshot.
- *How prevent lock waits?* → Short transactions, proper indexes, deterministic lock order, timeouts.

### Q37. What Spring transaction pitfalls affect databases?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Java backend interviews connect SQL to Spring behavior.

**Beginner lens:** Spring transactions are controlled by proxies, so a method calling another method on the
same object may bypass @Transactional. The database effect is real: locks may be missing, commits may happen
earlier than expected, or a connection may be held while remote work runs. The mental model is transaction
boundary equals database resource lifetime. Interviewers are testing Java backend realism. By default, Spring
rolls back on unchecked exceptions; checked exceptions need configuration. Keep transactions short and around
database work, not network calls.

**Short answer (say this first, 20-40 sec):**
`@Transactional` applies through Spring proxies, so self-invocation can bypass it. Long transactions hold locks and connections. By default, Spring rolls back on unchecked exceptions, not checked exceptions unless configured.

**Detailed answer:**
```java
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class TransferService {
    @Transactional
    public void transfer(long from, long to, long amount) {
        debit(from, amount);
        credit(to, amount);
    }
}
```
Keep transactions around database work, not remote API calls.

**Common mistakes:**
- Calling a transactional method from another method in the same class.
- Doing network calls while holding DB locks.
- Assuming checked exceptions roll back automatically.

**Follow-ups:**
- *How set isolation?* → `@Transactional(isolation = Isolation.REPEATABLE_READ)` when justified.
- *What is propagation?* → How transactions nest or join existing transactions, such as `REQUIRED` or `REQUIRES_NEW`.

### Q38. What is write skew, and how do you prevent it?
`🔴 Advanced` `Stretch`

**Why they ask:** It separates basic isolation knowledge from real concurrency correctness.

**Beginner lens:** Write skew happens when transactions read the same rule but update different rows. Two
doctors both see someone else on call, each marks themselves off, and the rule at least one doctor stays on
call is broken. No single row conflict catches it. The mental model is cross-row invariant, not lost update.
Interviewers use this to separate snapshot isolation from true serializable behavior. Prevent it with a
constraint when possible, a single summary row, explicit locks over the invariant set, or Serializable
isolation with retry logic.

**Short answer (say this first, 20-40 sec):**
Write skew occurs when two transactions read the same invariant and update different rows, so neither conflicts directly but together they violate the rule. Prevent it with serializable isolation, explicit locks on the invariant set, constraints, or redesign as a single row/counter update.

**Detailed answer:**
Example: at least one doctor must be on call. T1 sees A and B on call and sets A off. T2 sees the same snapshot and sets B off. Both commit under snapshot isolation; invariant breaks.
```sql
SELECT * FROM doctor_on_call WHERE shift_id = 7 FOR UPDATE;
```
Or use SERIALIZABLE and retry serialization failures.

**Common mistakes:**
- Believing row-level locks on updated rows solve cross-row invariants.
- Not retrying serialization failures.

**Follow-ups:**
- *Is write skew a phantom?* → Related but distinct; it is an invariant violation from concurrent writes after shared reads.
- *Best fix?* → Prefer constraints when expressible; otherwise serializable or explicit locking.

## Section 5 · Schema Design, Scaling, and Operations

### Q39. Explain normalization from 1NF to BCNF with examples.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Schema quality affects correctness and query complexity.

**Beginner lens:** Normalization is about removing repeated facts so updates cannot disagree. First Normal
Form (1NF) avoids lists inside a cell; Second Normal Form (2NF) removes dependency on only part of a composite
key; Third Normal Form (3NF) removes facts depending on non-key facts; Boyce-Codd Normal Form (BCNF) says
every determinant should be a candidate key. With orders, customer phone belongs in customers, not repeated on
every order. Interviewers are testing anomaly awareness. The trap is over-normalizing read-heavy paths without
a clear query plan.

**Short answer (say this first, 20-40 sec):**
Normalization reduces redundancy and update anomalies. 1NF requires atomic values, 2NF removes partial dependency on composite keys, 3NF removes transitive dependencies, and BCNF requires every determinant to be a candidate key.

**Detailed answer:**
Bad table: `order_id, customer_name, customer_phone, product_ids, product_names`. Normalized design:
```sql
customers(id, name, phone)
orders(id, customer_id, order_date)
products(id, name, price)
order_items(order_id, product_id, quantity)
```

**Common mistakes:**
- Over-normalizing read-heavy reporting schemas.
- Treating normalization as only academic.

**Follow-ups:**
- *What anomaly does normalization prevent?* → Insert, update, and delete anomalies.
- *When denormalize?* → When measured read performance justifies controlled duplication.

### Q40. What are denormalization tradeoffs?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Real systems often denormalize carefully.

**Beginner lens:** Denormalization deliberately stores a repeated or precomputed value, such as order_total on
an order instead ofsumming items every page load. The benefit is faster reads or simpler reports; the cost is
keeping copies correct when writes happen. The mental model is buying latency with consistency work.
Interviewers want areasoned tradeoff, not normalization dogma. Production guardrails include transactions,
triggers, events, reconciliation jobs, and clear ownership. Never denormalize unless you know the read pattern
and the update path.

**Short answer (say this first, 20-40 sec):**
Denormalization duplicates data to reduce joins or precompute results. It can improve read latency but increases write complexity, storage, and inconsistency risk. Use it when reads dominate and you have a reliable sync/update strategy.

**Detailed answer:**
Examples: store `order_total`, maintain `customer_order_count`, or use materialized views for dashboards. Guardrails include transactions, triggers, outbox/event updates, reconciliation jobs, and tests.

**Common mistakes:**
- Denormalizing before measuring.
- Not documenting the source of truth.

**Follow-ups:**
- *How detect inconsistency?* → Periodic reconciliation queries and constraints where possible.
- *Is caching denormalization?* → It is related duplication, but usually outside the DB.

### Q41. How do you model many-to-many relationships?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Backend schema design requires relationship modeling.

**Beginner lens:** A many-to-many relationship needs a junction table because one student can take many
courses and one course can have many students. The enrollment table holds student_id and course_id, usually
with a composite primary key or unique constraint on the pair. Extra facts about the relationship, such as
enrolled_at or grade, live there too. The mental model is turning a relationship into its own entity.
Interviewers are checking constraints and cardinality. A trap is storing comma-separated identifiers, which
breaks joins, indexing, and referential integrity.

**Short answer (say this first, 20-40 sec):**
Use a junction table with foreign keys to both entities. Put a composite primary key or unique constraint on the pair. Store relationship attributes on the junction table.

**Detailed answer:**
```sql
CREATE TABLE enrollments (
  student_id bigint REFERENCES students(id),
  course_id bigint REFERENCES courses(id),
  enrolled_at timestamp NOT NULL,
  PRIMARY KEY (student_id, course_id)
);
```

**Common mistakes:**
- Storing comma-separated ids in one column.
- Forgetting uniqueness and allowing duplicate relationships.

**Follow-ups:**
- *Where store grade?* → In `enrollments`, because grade belongs to the relationship.
- *How index reverse lookup?* → Add `(course_id, student_id)` if querying course students.

### Q42. Why are constraints important?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Constraints are the database's last line of defense.

**Beginner lens:** Constraints are the database's seat belts. Application validation may miss a path, but a
NOT NULL, foreign key, unique constraint, check constraint, or primary key still protects the table. For
payments, a unique idempotency key can prevent charging twice even if two requests race. The key model is
invariant enforcement at the lowest shared layer. Interviewers are testing whether you trust the database for
correctness. Production concern: add constraints carefully on large tables, often with validation phases or
online methods.

**Short answer (say this first, 20-40 sec):**
Constraints enforce correctness even if multiple services or scripts write data. Primary keys, foreign keys, unique constraints, check constraints, and not-null constraints prevent invalid states. Application validation is useful but not enough.

**Detailed answer:**
```sql
CREATE TABLE users (
  id bigint PRIMARY KEY,
  email text NOT NULL UNIQUE,
  age int CHECK (age >= 0)
);
```
Unique constraints can also support idempotency, such as one payment per idempotency key.

**Common mistakes:**
- Relying only on application checks.
- Avoiding foreign keys without a real reason.

**Follow-ups:**
- *Can constraints hurt performance?* → They add write checks but prevent expensive corruption.
- *What is deferred constraint?* → Checked at transaction commit instead of statement end, where supported.

### Q43. Stored procedures vs application logic: how do you choose?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Teams differ on where business logic should live.

**Beginner lens:** Stored procedures keep logic close to data; application code keeps logic close to tests,
version control, observability, and service calls. A batch recalculation touching millions of rows may fit a
procedure, while order-domain rules that call other services usually belong in Java or another application
layer. The mental model is data locality versus maintainability. Interviewers are checking balanced judgment.
Traps include hiding business rules in database-specific code no one reviews, or forcing chatty application
loops where one set-based database operation would be safer.

**Short answer (say this first, 20-40 sec):**
Stored procedures are good for data-heavy operations close to the DB, batch jobs, and controlled access. Application logic is better for domain behavior, version control, testing, observability, and service integration. I prefer Java for business logic unless DB locality strongly wins.

**Detailed answer:**
| Stored procedure | Application logic |
|---|---|
| fewer round trips | easier unit testing |
| close to data | normal code review/CI |
| centralized DB permissions | easier service integration |
| DB-specific | portable and observable |

**Common mistakes:**
- Putting all logic in stored procedures and losing testability.
- Pulling millions of rows into app memory instead of aggregating in SQL.

**Follow-ups:**
- *When definitely use SQL?* → Filtering, joining, aggregation, set-based updates.
- *When avoid stored procs?* → Logic that calls many external services or changes frequently.

### Q44. What are views and materialized views?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Views appear in reporting and API read models.

**Beginner lens:** A view is a saved query name, like active_customers, that runs when selected. It simplifies
permissions and repeated joins but usually does not store rows. A materialized view stores the result
physically, so an expensive dashboard query can read precomputed data. The key distinction is freshness versus
speed. Interviewersare testing whether you know refresh and invalidation costs. PostgreSQL has materialized
views; MySQL commonly uses ordinary views and emulates materialization with tables or scheduled refresh jobs.

**Short answer (say this first, 20-40 sec):**
A view is a saved query definition; data is computed when queried. A materialized view stores query results physically and must be refreshed. Views simplify access; materialized views speed repeated expensive reads at the cost of freshness.

**Detailed answer:**
```sql
CREATE VIEW active_customers AS SELECT * FROM customers WHERE active = true;
CREATE MATERIALIZED VIEW monthly_sales AS
SELECT date_trunc('month', order_date) AS month, SUM(total_amount) AS revenue
FROM orders GROUP BY 1;
REFRESH MATERIALIZED VIEW CONCURRENTLY monthly_sales;
```

**Common mistakes:**
- Expecting normal views to automatically improve performance.
- Forgetting refresh strategy for materialized views.

**Follow-ups:**
- *Can you index a materialized view?* → In PostgreSQL, yes.
- *What is staleness?* → Difference between materialized view data and base tables.

### Q45. Explain replication, read replicas, and replication lag.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Backend systems often scale reads with replicas.

**Beginner lens:** Replication copies changes from a primary database to replicas. Read replicas can take
reporting or read traffic, but they may be seconds behind. If a user creates an order and the next page reads
from a lagging replica, the order may seem missing. The mental model is eventual copy, not instant shared
memory. Interviewers are checking read-after-write consistency awareness. Mitigations include reading recent
user actions from the primary, lag-aware routing, sticky sessions, or making stale reads visible in product
behavior.

**Short answer (say this first, 20-40 sec):**
Replication copies changes from a primary to replicas. Read replicas scale read traffic and improve availability. Replication lag means replicas may be behind the primary, so read-after-write consistency can break.

**Detailed answer:**
```mermaid
flowchart LR
  A["App write"] --> B["Primary database"]
  B --> C["WAL or binlog stream"]
  C --> D["Read replica 1"]
  C --> E["Read replica 2"]
  F["App read"] --> D
```
Mitigations: read your writes from primary, session stickiness, lag-aware routing, retry after commit, or expose eventual consistency.

**Common mistakes:**
- Sending all reads to replicas after writes.
- Ignoring lag during incidents or heavy writes.

**Follow-ups:**
- *Sync vs async replication?* → Sync waits for replica acknowledgment; async is faster but can lose recent commits on failover.
- *How measure lag?* → DB-specific replay/binlog metrics or seconds-behind-source.

### Q46. What is sharding, and when should you use it?
`🔴 Advanced` `Stretch`

**Why they ask:** Sharding tests distributed data design.

**Beginner lens:** Sharding splits one logical dataset across database nodes, often by tenant_id or
customer_id. If customer 42's orders live on shard 7, that shard can serve most customer-specific requests
independently. The mental model is horizontal capacity in exchange for distributed-system complexity.
Interviewers want restraint: try indexes, caching, replicas, partitioning, and query fixes first. Hard parts
include cross-shardjoins, global uniqueness, rebalancing hot tenants, backups, and transactions that touch
multiple shards. A poor shard key creates uneven load.

**Short answer (say this first, 20-40 sec):**
Sharding splits data across multiple database nodes, usually by a shard key like tenant or user id. It increases capacity but makes joins, transactions, rebalancing, and operations harder. Use it only after indexing, replicas, partitioning, and caching are not enough.

**Detailed answer:**
Good shard key qualities: high cardinality, even distribution, matches access patterns, and minimizes cross-shard transactions. Example: `orders` sharded by `customer_id` keeps a customer's orders colocated.

**Common mistakes:**
- Sharding too early.
- Choosing a hot key like country or status.

**Follow-ups:**
- *How handle global unique ids?* → UUID/ULID/Snowflake-style ids or central allocator.
- *What is resharding?* → Moving data when shard count or key distribution changes.

### Q47. How do you size a HikariCP connection pool?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Pool misconfiguration can create slow APIs even with good SQL.

**Beginner lens:** A HikariCP pool is a limited set of database connections shared by an application instance.
Ten pods with pool size 30 can create 300 database sessions, which may overload the server even though each
pod looks reasonable. The model is matching concurrency to database capacity and query time, not maximizing
the number. Interviewers are testing production tuning. Watch active, idle, pending, timeout, and database CPU
metrics under load.Too small queues requests; too large increases contention, memory, and context switching.

**Short answer (say this first, 20-40 sec):**
Do not set huge pools blindly. Size based on DB capacity, CPU cores, query latency, and number of app instances. A pool should keep the DB busy without causing connection thrashing or lock contention.

**Detailed answer:**
```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.max-lifetime=1800000
```
If 10 pods each have pool 30, the DB may see 300 connections. Start modest, load test, and tune active/idle/pending metrics.

**Common mistakes:**
- Increasing pool size to hide slow queries.
- Forgetting total connections across pods.

**Follow-ups:**
- *Symptom of too-small pool?* → Threads wait for connections; Hikari timeouts.
- *Symptom of too-large pool?* → DB CPU/context switching rises and latency worsens.

### Q48. SQL vs NoSQL: how do you choose?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Backend engineers must justify storage choices.

**Beginner lens:** Structured Query Language (SQL) databases are strongest when relationships, constraints,
joins, and transactions are central, such as orders and payments. Not Only SQL (NoSQL) stores can fit known
access patterns that need document flexibility, key-value speed, or wide horizontal writes. The mental model
is consistency and query flexibility versus scale and shape specialization. Interviewers are testing choice
justification, not tribal preference. A trap is choosing a document store to avoid schema design, then needing
cross-document transactions and ad hoc joins later.

**Short answer (say this first, 20-40 sec):**
Choose SQL when you need strong consistency, joins, constraints, transactions, and flexible querying. Choose NoSQL when access patterns are known and you need horizontal scale, flexible documents, high write throughput, or key-value latency.

**Detailed answer:**
| Need | Better fit |
|---|---|
| payments/orders with constraints | SQL |
| flexible profile document | document DB |
| huge write key-value access | Cassandra/DynamoDB style |
| graph traversals | graph DB |
| semantic vector search | pgvector or vector DB depending scale |

**Common mistakes:**
- Saying NoSQL means no schema; schemas move to application/query design.
- Using MongoDB to avoid learning joins when data is relational.

**Follow-ups:**
- *Can SQL scale?* → Yes, with indexing, replicas, partitioning, sharding, and caching.
- *Can NoSQL be consistent?* → Some offer tunable/transactional consistency, but tradeoffs differ.

### Q49. What is the role of pgvector in a PostgreSQL-backed RAG system?
`🟡 Intermediate` `Must-know SDE-1`

**Why they ask:** It connects your Lafz project to database internals.

**Beginner lens:** In Retrieval-Augmented Generation (RAG), pgvector lets PostgreSQL store embedding vectors
and search for chunks similar to a user's question. The application embeds the question, asks for nearest
chunks, and sends those chunks to the Large Language Model (LLM) as context. The mental model is semantic
lookup beside relational filters such as tenant or document type. Interviewers connect this to your project
experience. Production concerns include embedding dimension consistency, approximate-index recall, top-K
latency, vacuum effects, and whether scale justifies a separate vector database.

**Short answer (say this first, 20-40 sec):**
pgvector stores embedding vectors in PostgreSQL and supports similarity search using distance operators. In RAG, the app embeds a query, retrieves top-K nearest chunks, and sends them to the LLM as context. Index choice and filtering affect latency and recall.

**Detailed answer:**
```sql
SELECT id, content
FROM poem_chunks
ORDER BY embedding <-> $1
LIMIT 5;
```
Benchmark HNSW/IVFFlat settings, recall, and latency; keep embedding dimensions consistent.

**Common mistakes:**
- Claiming approximate vector indexes always return exact nearest neighbors.
- Ignoring metadata filters and tenant isolation.

**Follow-ups:**
- *What is top-K retrieval?* → Return the K nearest vectors by chosen distance metric.
- *Why store metadata in PostgreSQL?* → Transactional filters and joins with relational data.

### Q50. Compare OLTP and OLAP workloads.
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** It clarifies why API databases and analytics systems are tuned differently.

**Beginner lens:** Online Transaction Processing (OLTP) is the checkout counter: many small reads and writes,
strict correctness, and low latency. Online Analytical Processing (OLAP) is the analyst's warehouse: large
scans, joins, aggregations, and reports over history. The mental model is operational state versus analytical
questions. Interviewers want you to explain why normalized row-store tables suit services, while columnar
storage, star schemas, and pre-aggregation help dashboards. A trap is running heavy reports on the primary
service database and hurting customer-facing transactions.

**Short answer (say this first, 20-40 sec):**
OLTP handles many small transactional reads/writes with low latency. OLAP handles large analytical scans, aggregations, and reports. OLTP favors normalized schemas and indexes; OLAP often uses columnar storage, star schemas, and pre-aggregations.

**Detailed answer:**
| Area | OLTP | OLAP |
|---|---|---|
| Example | checkout API | sales dashboard |
| Query | point lookup/update | large scan/aggregate |
| Schema | normalized | dimensional/star |
| Latency | milliseconds | seconds/minutes acceptable |
| Writes | frequent | batch/stream loads |
Your Power BI/semantic model optimization is closer to OLAP; Spring Boot APIs are OLTP.

**Common mistakes:**
- Running heavy analytics on primary OLTP during peak traffic.
- Over-indexing OLTP tables for every report.

**Follow-ups:**
- *How separate workloads?* → Replicas, ETL/ELT, warehouse/lakehouse, materialized views.
- *Why columnar for OLAP?* → Reads needed columns and compresses well.

## Section 6 · Backend Interview Edge Cases

### Q51. How do you perform safe schema migrations?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Production migrations can break deployments.

**Beginner lens:** A safe schema migration is a sequence, not a single dramatic change. To add required
time_zone, first add it nullable, deploy code that tolerates both shapes, backfill in batches, validate, then
enforce NOT NULL and remove old paths later. The mental model is backward and forward compatibility across
rolling deploys. Interviewers are testing production caution. Watch locks, table rewrites, long transactions,
replicas, and rollback plans. PostgreSQL and MySQL have different online Data Definition Language (DDL)
capabilities, so check version-specific behavior.

**Short answer (say this first, 20-40 sec):**
Use backward-compatible, incremental migrations. Add nullable columns first, backfill in batches, deploy code that writes both old and new fields if needed, validate, then enforce constraints and remove old columns later. Avoid long locks on large tables.

**Detailed answer:**
Safe add-not-null: add nullable column → backfill in small batches → add default for new writes → validate no NULLs → add NOT NULL during a safe window or online method.

**Common mistakes:**
- Adding a blocking constraint or index on a huge table during peak traffic.
- Deploying code that requires a column before migration reaches all environments.

**Follow-ups:**
- *PostgreSQL online index?* → `CREATE INDEX CONCURRENTLY` reduces blocking but cannot run inside a normal transaction block.
- *How rollback?* → Prefer expand-contract so old and new code coexist.

### Q52. Offset vs keyset pagination: which is better?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Pagination affects API latency and correctness.

**Beginner lens:** Offset pagination says skip the first 10,000 rows, then return 20, so deep pages get slower
and concurrent inserts can move rows between pages. Keyset pagination says continue after the last seen
created_at and id, letting the index resume from a known position. The mental model is counting past rows
versus seeking froma cursor. Interviewers test performance and consistency. Offset is fine for small admin
pages; keyset is better for infinite scroll, feeds, and large ordered tables, but it needs a stable unique
ordering.

**Short answer (say this first, 20-40 sec):**
Offset pagination is simple but gets slower for deep pages and can skip/duplicate rows under concurrent changes. Keyset pagination uses the last seen sort key, making deep pagination efficient and stable. Use keyset for infinite scroll and large ordered datasets.

**Detailed answer:**
```sql
-- Offset
SELECT * FROM orders ORDER BY created_at DESC LIMIT 20 OFFSET 100000;
-- Keyset
SELECT * FROM orders
WHERE (created_at, id) < (:lastCreatedAt, :lastId)
ORDER BY created_at DESC, id DESC
LIMIT 20;
```

**Common mistakes:**
- Using offset for very deep pages.
- Missing unique tie-breaker in keyset ordering.

**Follow-ups:**
- *Can keyset jump to page 50?* → Not easily; it is cursor-based.
- *What cursor expose?* → Encode last sort key values, not raw SQL state.

### Q53. What is the N+1 query problem in Hibernate/JPA?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Java backend performance often fails at ORM boundaries.

**Beginner lens:** The N+1 problem is an Object-Relational Mapping (ORM) loop: one query loads 50 authors,
then 50 more queries load each author's books. The result is 51 round trips instead of one planned fetch. In
Hibernate and Java Persistence API (JPA), lazy associations often cause this when rendering responses. The
mental model is hidden query multiplication at entity boundaries. Interviewers are testing whether Java code
and database behavior are connected. Fixes include fetch joins, entity graphs, batch fetching, Data Transfer
Object (DTO) projections, or explicit queries.

**Short answer (say this first, 20-40 sec):**
N+1 occurs when one query loads parent rows and then one extra query loads children for each parent. It causes many round trips and slow APIs. Fix it with fetch joins, entity graphs, batch fetching, DTO queries, or explicit joins.

**Detailed answer:**
```java
@Query("select o from Order o join fetch o.customer where o.status = :status")
List<Order> findWithCustomer(String status);
```
Use DTO projections for API responses when full entities are unnecessary.

**Common mistakes:**
- Solving N+1 by making everything EAGER.
- Not enabling SQL logging/metrics in development.

**Follow-ups:**
- *How detect N+1?* → Query logs, Hibernate statistics, APM traces.
- *Fetch join pagination issue?* → Joining collections can duplicate parent rows and break pagination.

### Q54. What are prepared statements and plan caching?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Prepared statements affect security and performance.

**Beginner lens:** Prepared statements separate the statement shape from values: select by email where email =
? instead of concatenating user input. That protects against injection and can reduce parse or planning
overhead through reuse. The mental model is stable template plus bound parameters. Interviewers are checking
security and performance nuance. PostgreSQL may choose custom or generic plans for prepared statements; MySQL
uses prepared execution differently by protocol and cache layer. A trap is assuming one cached plan is always
fastest when data distribution is highly skewed.

**Short answer (say this first, 20-40 sec):**
Prepared statements separate SQL structure from parameter values, preventing SQL injection and allowing statement/plan reuse. Databases may cache generic or custom plans. Parameterized queries are the default expectation in Java backends.

**Detailed answer:**
```java
var ps = connection.prepareStatement("SELECT * FROM users WHERE email = ?");
ps.setString(1, email);
```
Benefits: safer escaping, less parse overhead, possible plan cache reuse. Watch for parameter-sensitive plans where one generic plan is bad for skewed values.

**Common mistakes:**
- Building SQL with string concatenation from user input.
- Assuming cached plans are always optimal.

**Follow-ups:**
- *Do prepared statements eliminate all injection?* → Only for values, not dynamic identifiers like table names.
- *What is bind peeking?* → Optimizer behavior that considers parameter values in some DBs.

### Q55. What data type and time-zone choices matter in database design?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Production bugs often come from wrong types, money, and time zones.

**Beginner lens:** Type choices encode business rules. Money should use decimal or numeric types and
BigDecimal in Java, not floating point. Instants should use a clear Coordinated Universal Time (UTC) convention
or timestamp-with-time-zone semantics, with local dates stored separately when the calendar date matters. The
mental model is preserving meaning, precision, and comparison behavior. Interviewers are testing production
bug prevention. Also consider collation for case or accent comparisons, integer ranges, identifier size, and
avoiding numbers or dates hidden inside text fields.

**Short answer (say this first, 20-40 sec):**
Use precise types: `decimal/numeric` for money, not floating point; `timestamp with time zone` or a clear UTC convention for instants; proper text collation for case/accent behavior; and matching application types. Avoid storing dates, numbers, or JSON as strings unless justified.

**Detailed answer:**
```sql
amount numeric(12,2) NOT NULL,
created_at timestamptz NOT NULL DEFAULT now(),
email text NOT NULL
```
Java mapping: use `BigDecimal` for money and `Instant`/`OffsetDateTime` for instants.

**Common mistakes:**
- Using float/double for currency.
- Storing local time without zone for global events.
- Comparing text without understanding collation/case sensitivity.

**Follow-ups:**
- *What is `timestamptz` in PostgreSQL?* → It stores an instant normalized internally and displays in session time zone.
- *When use JSONB?* → Flexible attributes, but keep core queryable fields as columns.

## Revision Cheat Sheet

- PostgreSQL MVCC stores row versions in heap tuples using `xmin/xmax`; VACUUM cleans dead tuples and controls bloat.
- InnoDB uses undo logs for old versions and purge cleanup; default isolation is REPEATABLE READ with next-key/gap locks for locking ranges.
- Slow API flow: isolate DB time, find SQL, run `EXPLAIN ANALYZE`, check rows/scans/joins/sorts/locks, add targeted index or rewrite, benchmark.
- Joins: INNER keeps matches; LEFT keeps all left rows; FULL keeps both; SELF joins a table to itself.
- Logical SQL order: FROM/JOIN → WHERE → GROUP BY → aggregates → HAVING → windows → SELECT → DISTINCT → ORDER BY → LIMIT.
- Window functions keep rows; `ROW_NUMBER`, `RANK`, `DENSE_RANK`, `LAG`, `LEAD`, running totals are staples.
- Composite indexes follow workload; equality predicates first, range/order next; leftmost prefix matters.
- Index may not be used because of functions, leading wildcard, implicit casts, low selectivity, OR, stale stats, or small table scans.
- Isolation levels prevent different anomalies; database-specific behavior matters.
- Deadlocks are detected by DB; prevent with consistent lock order, short transactions, good indexes, and retry logic.
- Optimistic locking uses version checks; pessimistic locking uses row locks like `FOR UPDATE`.
- `SKIP LOCKED` is useful for parallel job queues.
- Normalize for correctness; denormalize only with measured read benefit and consistency strategy.
- Read replicas scale reads but can be stale because of replication lag.
- HikariCP pool size must consider total app instances and DB capacity, not just one JVM.

## Flashcards

- Q → PostgreSQL MVCC cleanup mechanism? A → VACUUM/autovacuum removes dead tuples and updates visibility information.
- Q → InnoDB old row versions live where? A → Undo logs, later removed by purge.
- Q → Best first step for slow SQL? A → Capture exact query and run `EXPLAIN ANALYZE` with real parameters.
- Q → WHERE vs HAVING? A → WHERE filters rows before grouping; HAVING filters groups after aggregation.
- Q → `ROW_NUMBER` vs `DENSE_RANK`? A → ROW_NUMBER is unique per row; DENSE_RANK ties share rank without gaps.
- Q → Leftmost prefix rule? A → Composite index is most usable from its leading columns onward.
- Q → Covering index? A → Index containing all columns needed by a query.
- Q → Dirty read? A → Reading uncommitted data from another transaction.
- Q → Write skew? A → Concurrent transactions update different rows after reading same invariant and break it.
- Q → Deadlock prevention? A → Consistent lock order, short transactions, good indexes, and retries.
- Q → `FOR UPDATE SKIP LOCKED` use case? A → Multiple workers claiming jobs without blocking each other.
- Q → Normalization goal? A → Reduce redundancy and update anomalies.
- Q → Replication lag risk? A → Read-after-write may return stale data from replicas.
- Q → HikariCP too large symptom? A → DB overload, context switching, higher latency.
- Q → Money data type? A → `numeric/decimal` in SQL and `BigDecimal` in Java.
