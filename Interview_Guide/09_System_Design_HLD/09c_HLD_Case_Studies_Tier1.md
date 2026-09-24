# 09c · HLD Case Studies Tier 1 I

This file covers the first five Tier-1 HLD case studies with interview-ready structure: requirements, estimation, APIs, data model, diagrams, deep dives, scaling, failure handling, and revision notes.

**Legend:** 🟢 Beginner · 🟡 Intermediate · 🔴 Advanced · ⚫ FAANG · 🔥 = very frequently asked

## Table of Contents
- [1. Design URL Shortener](#1-design-url-shortener)
- [2. Design Pastebin](#2-design-pastebin)
- [3. Design Distributed Rate Limiter](#3-design-distributed-rate-limiter)
- [4. Design Unique ID Generator](#4-design-unique-id-generator)
- [5. Design Distributed Cache / Key-Value Store](#5-design-distributed-cache--key-value-store)
- [Revision Cheat Sheet](#revision-cheat-sheet)
- [Flashcards](#flashcards)

## 1. Design URL Shortener
Tier 1 · SDE-1 must-know

### 1. Clarifying questions to ask
- Should short links be custom aliases or generated only?
- Do links expire? Do users need analytics?
- Expected redirects per day and new links per day?
- Is low latency more important than strong analytics freshness?
- Are links public, private, or abuse-scanned?

### 2. Functional & Non-functional requirements
Functional: create short URL, redirect to long URL, optional custom alias, expiry, basic click analytics. Non-functional: redirect P99 under 100 ms from cache, high availability, durable mappings, abuse detection, read-heavy scaling.

### 3. Capacity estimation (show the math)
Assume 1M new URLs/day and 100M redirects/day. Write QPS = 1M / 86,400 ≈ 12, peak 60. Read QPS = 100M / 86,400 ≈ 1,160, peak 5,800. Mapping row ≈ 500 B; 5-year raw storage = 1M × 365 × 5 × 500 B ≈ 912 GB before indexes and replication. Analytics event 200 B × 100M/day = 20 GB/day raw; store in log/object storage with aggregation.

### 4. API design (endpoints with request/response)
`POST /api/v1/links`
```json
{"longUrl":"https://example.com/a/b","customAlias":"az-poem","expiresAt":"2027-01-01T00:00:00Z"}
```
Response:
```json
{"shortUrl":"https://sho.rt/az-poem","code":"az-poem","expiresAt":"2027-01-01T00:00:00Z"}
```
`GET /{code}` returns `301` or `302` with `Location`. Use `404` for missing, `410` for expired, `409` for alias conflict.

### 5. Data model + DB choice with justification
Use PostgreSQL/MySQL for canonical mappings because writes are modest and uniqueness constraints matter. Use Redis for hot code-to-longUrl cache. Use Kafka/log storage for click events.

```mermaid
erDiagram
    LINKS {
        string code PK
        string long_url
        string user_id
        datetime expires_at
        datetime created_at
        string status
    }
    CLICK_EVENTS {
        string event_id PK
        string code
        string ip_hash
        string user_agent
        datetime clicked_at
    }
    LINKS ||--o{ CLICK_EVENTS : records
```

### 6. High-level design (Mermaid flowchart LR)
```mermaid
flowchart LR
    U["User browser"] --> CDN["DNS and CDN"]
    CDN --> LB["Load balancer"]
    LB --> API["URL service"]
    API --> R["Redis cache"]
    API --> DB["SQL links DB"]
    API --> Q["Click events queue"]
    Q --> AN["Analytics workers"]
    AN --> DW["Analytics store"]
```

### 7. Key flow sequence diagram(s)
```mermaid
sequenceDiagram
    participant U as User
    participant A as URL Service
    participant R as Redis
    participant D as SQL DB
    participant Q as Queue
    U->>A: GET /abc123
    A->>R: get abc123
    alt cache hit
        R-->>A: long URL
    else cache miss
        A->>D: select long_url where code
        D-->>A: mapping
        A->>R: set mapping with TTL
    end
    A->>Q: async click event
    A-->>U: 302 Location long URL
```

### 8. Deep dives into 2-3 hardest parts
**Short-code generation:** Base62 counter is compact and collision-free with a sequence or Snowflake ID; hash of long URL is deterministic but collision-prone and leaks duplicates; random code needs collision retry. For SDE-1, choose Snowflake/DB sequence → Base62. For custom alias, use DB unique constraint.

**Redirect latency:** Cache hot mappings in Redis with TTL; also use local in-process cache for very hot codes if invalidation is acceptable. Do not synchronously write analytics before redirect.

**Abuse and expiry:** Validate URL scheme, block malware domains, enforce per-user creation limits, and return `410 Gone` for expired links.

### 9. Redis usage · Message queue usage
Redis: `code -> longUrl`, custom alias reservation locks, rate limits. Queue: click analytics events, abuse scans, cache invalidation.

### 10. Scaling 10x -> 100x · High availability & "what if X goes down"
10x: read replicas, Redis cluster, partition analytics by code/date. 100x: shard mapping DB by hash(code), multi-region read cache, asynchronous global replication. If Redis goes down, fall back to DB with circuit breaker and lower TTL after recovery. If DB leader goes down, failover to replica; redirects for cached links still work. If queue is down, buffer best effort or drop analytics, never block redirect.

### 11. Tradeoffs & alternatives rejected
Rejected storing mappings only in Redis because durability and alias uniqueness are critical. Rejected synchronous analytics because it hurts redirect latency. Rejected hash-only code because collision handling and unwanted dedupe complicate behavior.

### 12. "SDE-1 answer (enough to pass)" vs "Senior answer [Stretch]"
- **SDE-1:** LB, URL service, SQL mappings, Redis cache, async analytics, Base62 IDs, unique constraint.
- **Senior [Stretch]:** abuse pipeline, multi-region cache warmup, custom domain support, GDPR deletion, per-tenant quotas, active-active conflict strategy.

### 13. Interviewer follow-up questions with short answers
- *301 or 302?* → 302 by default for analytics/control; 301 for permanent SEO links if allowed.
- *How avoid collisions?* → Generate numeric ID then Base62, or random with DB unique retry.
- *How track analytics?* → Async event queue and aggregate offline.

### 14. One-paragraph revision summary
URL shortener is read-heavy: generate collision-free Base62 codes, store canonical mappings in SQL, cache hot redirects in Redis, and push analytics asynchronously. The main interview tradeoff is redirect latency versus analytics freshness and durability.

## 2. Design Pastebin
Tier 1 · SDE-1 must-know

### 1. Clarifying questions to ask
- Max paste size and retention period?
- Public, unlisted, or private pastes?
- Syntax highlighting required?
- Editable after creation or immutable?
- Expected reads vs writes and abuse moderation needs?

### 2. Functional & Non-functional requirements
Functional: create paste, read paste by key, optional expiry, visibility, raw view, metadata. Non-functional: durable storage, high read availability, large content support, low read latency, spam/malware controls.

### 3. Capacity estimation (show the math)
Assume 500K pastes/day, average 10 KB, 20M reads/day. Write QPS ≈ 6, peak 30. Read QPS ≈ 231, peak 1,200. Storage/day = 500K × 10 KB = 5 GB/day; 1-year raw ≈ 1.8 TB before replication. Metadata row ≈ 1 KB/day = 0.5 GB/day.

### 4. API design (endpoints with request/response)
`POST /api/v1/pastes`
```json
{"content":"public class A {}","language":"java","visibility":"UNLISTED","expiresInSeconds":604800}
```
Response:
```json
{"pasteId":"p9Kx2a","url":"https://paste.io/p9Kx2a","rawUrl":"https://paste.io/raw/p9Kx2a"}
```
`GET /pastes/{pasteId}`, `GET /raw/{pasteId}`, `DELETE /pastes/{pasteId}` for owner/admin.

### 5. Data model + DB choice with justification
Use SQL for metadata and permissions. Store large paste body in object storage for cost and scalability. Small pastes may be stored inline initially, but object storage is cleaner at scale.

```mermaid
erDiagram
    PASTES {
        string paste_id PK
        string owner_id
        string object_key
        string language
        string visibility
        datetime expires_at
        datetime created_at
    }
    PASTE_STATS {
        string paste_id PK
        long read_count
        datetime last_read_at
    }
    PASTES ||--|| PASTE_STATS : has
```

### 6. High-level design (Mermaid flowchart LR)
```mermaid
flowchart LR
    U["Client"] --> LB["Load balancer"]
    LB --> API["Paste API"]
    API --> DB["Metadata SQL DB"]
    API --> OBJ["Object storage"]
    API --> R["Redis cache"]
    API --> Q["Stats and moderation queue"]
    Q --> W["Workers"]
```

### 7. Key flow sequence diagram(s)
```mermaid
sequenceDiagram
    participant C as Client
    participant A as Paste API
    participant O as Object Storage
    participant D as Metadata DB
    participant R as Redis
    C->>A: POST paste
    A->>O: Store content object
    A->>D: Insert metadata and object key
    A->>R: Cache metadata
    A-->>C: paste URL
```

### 8. Deep dives into 2-3 hardest parts
**Content storage:** Object storage handles large blobs better than DB BLOBs. Metadata DB stores owner, visibility, expiry, language, and object key.

**Expiry and deletion:** A background job scans expired metadata and deletes or tombstones content. Object lifecycle policies clean orphaned objects.

**Read path:** Cache metadata and small hot content in Redis/CDN. Raw public pastes can be CDN cached; private pastes require signed access and auth checks.

### 9. Redis usage · Message queue usage
Redis: hot paste metadata/content, rate limits for paste creation, abuse counters. Queue: moderation scan, syntax highlighting precompute, stats aggregation, expiry cleanup.

### 10. Scaling 10x -> 100x · High availability & "what if X goes down"
10x: CDN for public raw reads, object storage multipart uploads, DB read replicas. 100x: shard metadata by pasteId hash, region-local caches, async replication. If object storage is slow, serve cached hot pastes; if metadata DB is down, cached public reads continue; if queue is down, creation still works but moderation/stats lag.

### 11. Tradeoffs & alternatives rejected
Rejected storing all content in SQL because blobs bloat backups and reduce DB cache efficiency. Rejected synchronous syntax highlighting because it slows paste creation. Rejected public sequential IDs because enumeration increases abuse.

### 12. "SDE-1 answer (enough to pass)" vs "Senior answer [Stretch]"
- **SDE-1:** API service, SQL metadata, object storage content, Redis cache, expiry worker.
- **Senior [Stretch]:** malware scanning, privacy model, legal takedown workflow, CDN signed URLs, object lifecycle tiers, regional replication.

### 13. Interviewer follow-up questions with short answers
- *How protect private pastes?* → Auth check metadata, signed short-lived object URL, no CDN public cache.
- *How handle very large pastes?* → Multipart direct upload with size limits.
- *How prevent spam?* → Rate limit, captcha/risk scoring, moderation queue.

### 14. One-paragraph revision summary
Pastebin is mostly metadata plus blob storage. Keep DB for searchable metadata and permissions, store content in object storage, cache hot public reads, and handle moderation, stats, syntax highlighting, and expiry asynchronously.

## 3. Design Distributed Rate Limiter
Tier 1 · SDE-1 must-know

### 1. Clarifying questions to ask
- Limit by user, IP, API key, tenant, endpoint, or all?
- Strict global limits or approximate regional limits?
- Burst allowed or smooth traffic only?
- What happens if limiter store is down: fail-open or fail-closed?
- Required response headers and audit logs?

### 2. Functional & Non-functional requirements
Functional: check and consume quota, configure rules, return allow/deny and retry-after. Non-functional: sub-millisecond to few-ms decision, atomic updates, high availability, bounded memory, distributed correctness good enough for policy.

### 3. Capacity estimation (show the math)
Assume 50K API QPS, one limiter check per request, 1 KB Redis operation response/request overhead approximate. Peak Redis ops ≈ 50K/s. If 10M active keys/day and each key state ≈ 100 B plus overhead 2x, memory ≈ 2 GB; shard Redis for throughput and HA.

### 4. API design (endpoints with request/response)
Internal check:
```http
POST /internal/rate-limit/check
```
```json
{"key":"user:42:/send","limit":100,"windowSeconds":60,"cost":1}
```
Response:
```json
{"allowed":true,"remaining":73,"retryAfterSeconds":0}
```
Admin config: `PUT /rules/{ruleId}` with subject, endpoint, algorithm, rate, burst.

### 5. Data model + DB choice with justification
Rules live in SQL/config store for auditability. Runtime counters live in Redis Cluster for atomic low-latency updates.

```mermaid
erDiagram
    RATE_LIMIT_RULES {
        string rule_id PK
        string subject_type
        string endpoint_pattern
        string algorithm
        int limit_value
        int window_seconds
    }
    LIMIT_AUDIT {
        string event_id PK
        string key
        string decision
        datetime created_at
    }
```

### 6. High-level design (Mermaid flowchart LR)
```mermaid
flowchart LR
    C["Client"] --> G["API Gateway"]
    G --> L["Rate limiter library or service"]
    L --> R["Redis Cluster"]
    L --> CFG["Rule cache"]
    CFG --> DB["Rules DB"]
    G --> S["Backend service"]
    L --> Q["Audit sample queue"]
```

### 7. Key flow sequence diagram(s)
```mermaid
sequenceDiagram
    participant C as Client
    participant G as Gateway
    participant L as Limiter
    participant R as Redis
    participant S as Service
    C->>G: API request
    G->>L: check key and rule
    L->>R: Lua atomic consume token
    R-->>L: allowed remaining retryAfter
    alt allowed
        G->>S: forward
    else denied
        G-->>C: 429 Retry-After
    end
```

### 8. Deep dives into 2-3 hardest parts
**Algorithm choice:** Token bucket for public APIs because it permits bursts while limiting average. Sliding log for strict compliance but memory-heavy. Sliding counter for balanced accuracy and memory.

**Atomicity:** Use Lua script so refill, consume, TTL update, and response are one atomic operation. Avoid GET then SET races.

**Failure mode:** Fail-open for low-risk traffic to protect availability; fail-closed for login, expensive LLM calls, or abuse-prone endpoints. Add local fallback token buckets if Redis is degraded.

### 9. Redis usage · Message queue usage
Redis stores token bucket state and TTL keys. Queue stores sampled audit events, rule changes for cache invalidation, and abuse analytics.

### 10. Scaling 10x -> 100x · High availability & "what if X goes down"
Shard Redis by key hash; keep gateway-local rule cache; use regional limiters plus coarse global quotas. If Redis shard goes down, route to replica or use local emergency limiter. If rules DB goes down, cached rules continue until TTL; if audit queue is down, drop samples, not decisions.

### 11. Tradeoffs & alternatives rejected
Rejected DB counters due to write latency and lock contention. Rejected purely local limiters because limits are bypassed across gateway instances. Accepted small distributed approximation errors for availability.

### 12. "SDE-1 answer (enough to pass)" vs "Senior answer [Stretch]"
- **SDE-1:** Gateway checks Redis token bucket atomically; returns 429; rules cached.
- **Senior [Stretch]:** hierarchical global/regional limits, adaptive abuse detection, local fallback, multi-tenant fairness, Redis hot-key mitigation.

### 13. Interviewer follow-up questions with short answers
- *Why Lua?* → Atomic multi-step update in Redis.
- *How avoid hot keys?* → Split huge tenant counters by shard and aggregate, or enforce per-user plus tenant limits.
- *What headers return?* → `X-RateLimit-Limit`, `Remaining`, `Reset`, `Retry-After`.

### 14. One-paragraph revision summary
A distributed rate limiter sits at the gateway, uses Redis for atomic shared counters, and chooses token bucket or sliding-window algorithms based on burst tolerance. Main tradeoffs are strictness versus latency and availability.

## 4. Design Unique ID Generator
Tier 1 · SDE-1 must-know

### 1. Clarifying questions to ask
- Required throughput and number of regions?
- Should IDs be sortable by time?
- Numeric only or string allowed?
- Can IDs reveal creation time or machine id?
- What is acceptable behavior during clock rollback?

### 2. Functional & Non-functional requirements
Functional: generate globally unique IDs, expose service/library endpoint, monitor worker IDs. Non-functional: high throughput, low latency, no central bottleneck, roughly ordered if needed, resilient to restarts.

### 3. Capacity estimation (show the math)
Assume 100K IDs/sec peak globally, 100 workers. Per worker ≈ 1K IDs/sec. Snowflake with 12-bit sequence gives 4096 IDs/ms/worker ≈ 4M/sec/worker theoretical, far above need. 64-bit IDs store 8 bytes each; 1B rows need 8 GB raw for ID column before indexes.

### 4. API design (endpoints with request/response)
Library method preferred for low latency: `long nextId()`. Service fallback:
```http
POST /internal/ids/batch
```
```json
{"namespace":"orders","count":1000}
```
Response:
```json
{"ids":["7423912340001","7423912340002"],"expiresAt":"2026-09-23T23:00:00Z"}
```

### 5. Data model + DB choice with justification
No DB on hot path for Snowflake. Use coordination/config store for worker assignments and audit.

```mermaid
erDiagram
    WORKERS {
        int worker_id PK
        string region
        string instance_id
        string status
        datetime lease_expires_at
    }
    ID_NAMESPACES {
        string namespace PK
        string strategy
        bigint epoch_ms
    }
```

### 6. High-level design (Mermaid flowchart LR)
```mermaid
flowchart LR
    S1["Service A"] --> G1["ID library"]
    S2["Service B"] --> G2["ID library"]
    G1 --> C["Worker config cache"]
    G2 --> C
    C --> Z["Coordination store"]
    MON["Monitor"] --> Z
```

### 7. Key flow sequence diagram(s)
```mermaid
sequenceDiagram
    participant I as Instance
    participant Z as Coordinator
    participant G as Generator
    I->>Z: Acquire worker id lease
    Z-->>I: worker id 17
    I->>G: nextId
    G->>G: timestamp plus worker plus sequence
    G-->>I: 64-bit ID
```

### 8. Deep dives into 2-3 hardest parts
**Bit layout:** 1 sign bit, 41 timestamp bits, 10 worker bits, 12 sequence bits is common. Timestamp bits define lifetime; worker bits define scale; sequence bits define per-ms throughput.

**Worker assignment:** Use static config for simple systems or coordination leases for dynamic fleets. Never run two active instances with same worker id.

**Clock rollback:** Detect `now < lastTimestamp`. Options: block until clock catches up, fail fast, or use logical sequence extension. For correctness, never generate duplicate timestamp-worker-sequence tuple.

### 9. Redis usage · Message queue usage
Redis optional for worker leases or range allocation but coordination store is safer. Queue optional for audit/monitoring events; not in hot path.

### 10. Scaling 10x -> 100x · High availability & "what if X goes down"
Add worker bits or namespaces, run generator as library to avoid network hop, reserve worker ranges per region. If coordinator goes down, existing leases continue until TTL; new instances cannot start. If clock skew occurs, instance self-protects by pausing or failing.

### 11. Tradeoffs & alternatives rejected
Rejected DB sequence at high scale due to central bottleneck. Rejected UUID v4 when compact sortable IDs are required. Rejected timestamp-only IDs because same-millisecond duplicates are likely.

### 12. "SDE-1 answer (enough to pass)" vs "Senior answer [Stretch]"
- **SDE-1:** Snowflake-style ID with timestamp, worker id, sequence, unique worker assignment.
- **Senior [Stretch]:** region bits, clock rollback strategy, worker lease fencing, privacy encoding, migration from DB IDs.

### 13. Interviewer follow-up questions with short answers
- *How many IDs per ms with 12 bits?* → 4096 per worker.
- *Are IDs globally ordered?* → Roughly time-ordered, not perfectly across skewed machines.
- *Can users infer traffic?* → Yes if exposed; encode or use opaque public IDs if needed.

### 14. One-paragraph revision summary
Use Snowflake-style IDs when you need compact, high-throughput, roughly sortable unique IDs without a DB bottleneck. The critical correctness issues are worker-id uniqueness, sequence overflow, and clock rollback handling.

## 5. Design Distributed Cache / Key-Value Store
Tier 1 · SDE-1 must-know

### 1. Clarifying questions to ask
- Cache only or durable KV store?
- Expected key/value size, QPS, TTL, eviction policy?
- Need replication and consistency level?
- Operations: get, set, delete, compare-and-set, batch?
- Is data loss acceptable?

### 2. Functional & Non-functional requirements
Functional: get/set/delete keys, TTL, eviction, replication, client routing. Non-functional: low latency, horizontal scaling, high availability, hot-key handling, bounded memory, optional persistence.

### 3. Capacity estimation (show the math)
Assume 10M keys, average value 1 KB, metadata overhead 100 B, replication factor 2. Memory ≈ 10M × 1.1 KB × 2 ≈ 22 GB. Peak 200K QPS; with 20K safe QPS/node, need at least 10 nodes plus headroom, so 15-20 nodes.

### 4. API design (endpoints with request/response)
Client library preferred:
```java
Optional<byte[]> get(String key);
void set(String key, byte[] value, Duration ttl);
boolean delete(String key);
boolean compareAndSet(String key, byte[] expected, byte[] next);
```
Wire endpoint if service-based: `GET /kv/{key}`, `PUT /kv/{key}`, `DELETE /kv/{key}`.

### 5. Data model + DB choice with justification
In-memory hash table per node for speed. Optional append-only log and snapshots for recovery. Consistent hashing maps keys to nodes.

```mermaid
erDiagram
    CACHE_ENTRY {
        string key PK
        bytes value
        datetime expires_at
        long version
        string flags
    }
```

### 6. High-level design (Mermaid flowchart LR)
```mermaid
flowchart LR
    C["Client library"] --> R["Routing by consistent hash"]
    R --> N1["Cache node 1 primary"]
    R --> N2["Cache node 2 primary"]
    N1 --> Rep1["Replica node"]
    N2 --> Rep2["Replica node"]
    M["Membership service"] --> R
```

### 7. Key flow sequence diagram(s)
```mermaid
sequenceDiagram
    participant C as Client
    participant L as Client Library
    participant P as Primary Node
    participant R as Replica Node
    C->>L: set key value ttl
    L->>P: route by hash
    P->>P: write memory and AOF
    P->>R: replicate
    R-->>P: ack
    P-->>L: success
```

### 8. Deep dives into 2-3 hardest parts
**Partitioning:** Use consistent hashing with virtual nodes so adding/removing nodes moves limited keys. Replicate to next N distinct nodes on the ring.

**Eviction:** LRU approximations, LFU, TTL-based expiry, and memory pressure eviction. Choose based on workload; Redis often uses approximate policies for speed.

**Consistency:** For cache, eventual replication is usually fine. For durable KV, allow quorum reads/writes and versioning to resolve conflicts.

### 9. Redis usage · Message queue usage
This system is Redis-like. Redis can be used directly in real interviews unless asked to design cache internals. Queue is not hot path; membership changes and async persistence events can be logged.

### 10. Scaling 10x -> 100x · High availability & "what if X goes down"
Add nodes and rebalance virtual nodes; shard metadata; use replicas and automatic failover. If a node dies, client routes to replica and membership updates. If hot key overloads one node, add local cache, key salting for counters, or special replication for hot keys.

### 11. Tradeoffs & alternatives rejected
Rejected centralized router only because it can bottleneck; client-side routing is faster but needs membership updates. Rejected perfect LRU because it is expensive under high concurrency. Accepted eventual consistency for cache use cases.

### 12. "SDE-1 answer (enough to pass)" vs "Senior answer [Stretch]"
- **SDE-1:** Hash keys across nodes, replicate, TTL, eviction, client library routing.
- **Senior [Stretch]:** virtual nodes, quorum mode, anti-entropy repair, AOF/snapshot recovery, hot-key replication, split-brain handling.

### 13. Interviewer follow-up questions with short answers
- *How add a node?* → Add virtual nodes to ring and move only affected key ranges.
- *What if cache misses spike?* → Use request coalescing, backpressure, and warmup.
- *How choose TTL?* → Based on freshness requirement and origin load tolerance.

### 14. One-paragraph revision summary
A distributed cache partitions keys with consistent hashing, stores values in memory with TTL/eviction, and replicates for availability. The main design choices are routing, eviction, replication consistency, persistence, and hot-key protection.

## Revision Cheat Sheet

- URL shortener: SQL mapping, Redis redirect cache, Base62 IDs, async analytics.
- Pastebin: SQL metadata, object storage content, CDN/cache for public raw reads.
- Rate limiter: gateway + Redis atomic token bucket; fail-open or fail-closed by risk.
- Unique ID: Snowflake avoids DB bottleneck; protect worker IDs and clocks.
- Distributed cache: consistent hashing, virtual nodes, TTL, eviction, replicas.
- For read-heavy systems, never put analytics or slow scans in the user critical path.
- For correctness, prefer DB unique constraints and idempotency over best-effort locks.
- For blob-heavy systems, app servers should issue signed URLs rather than proxy all bytes.
- For cache systems, plan for cache miss storms and hot keys.
- For every case, say what happens when Redis, DB, queue, or a node goes down.

## Flashcards

1. Q → URL shortener DB? A → SQL for mappings, Redis for hot redirects.
2. Q → URL analytics path? A → Async queue after redirect decision.
3. Q → Pastebin content store? A → Object storage; DB stores metadata.
4. Q → Paste expiry? A → Metadata TTL plus cleanup worker and object lifecycle.
5. Q → Token bucket store? A → Redis atomic script with TTL.
6. Q → Rate limiter failure choice? A → Fail-open for availability, fail-closed for abuse-sensitive paths.
7. Q → Snowflake risk? A → Clock rollback or duplicate worker id.
8. Q → Snowflake throughput with 12 sequence bits? A → 4096 IDs per ms per worker.
9. Q → Cache partitioning? A → Consistent hashing with virtual nodes.
10. Q → Cache hot key mitigation? A → local cache, replication, salting, request coalescing.
