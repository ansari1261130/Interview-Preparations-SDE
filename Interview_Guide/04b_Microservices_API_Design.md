# 04b · Microservices & API Design

This part covers microservices, resilience, distributed data patterns, observability, and API design. Examples connect to the AI Email Reply Assistant and its Spring Boot backend calling Gemini API.

**Legend:** 🟢 Beginner · 🟡 Intermediate · 🔴 Advanced · ⚫ FAANG · 🔥 = very frequently asked

## Table of Contents
- [Microservices Fundamentals](#microservices-fundamentals)
- [Resilience, Consistency & Observability](#resilience-consistency--observability)
- [API Design](#api-design)
- [Email Reply Assistant API Exercise](#email-reply-assistant-api-exercise)
- [Production Scenarios](#production-scenarios)
- [Revision Cheat Sheet](#revision-cheat-sheet)
- [Flashcards](#flashcards)

## Microservices Fundamentals

### Q45. Monolith vs microservices: how do you decide?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** They want architectural judgment, not hype.

**Short answer (say this first, 20-40 sec):**
A monolith is one deployable application; microservices split a system into independently deployable services around business capabilities. Start with a modular monolith when the team/domain is small. Use microservices when independent scaling, deployment, ownership, and fault isolation outweigh distributed complexity.

**Detailed answer:**
| Factor | Monolith | Microservices |
|---|---|---|
| Deployment | one unit | many units |
| Complexity | lower ops | network/data/observability complexity |
| Scaling | whole app | per service |
| Team fit | small team | multiple autonomous teams |
For Email Reply Assistant, begin as modular monolith; split later if auth, generation, billing/quota, and history need independent scaling.

**Common mistakes:**
- Choosing microservices for a small CRUD app.
- Sharing one database and calling it microservices.

**Follow-ups:**
- *When not microservices?* → Small team, unclear domain, weak DevOps, strong transaction needs.
- *What is modular monolith?* → One deployable app with strongly separated modules.

### Q46. How do you choose microservice boundaries?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Bad boundaries create distributed monoliths.

**Short answer (say this first, 20-40 sec):**
Boundaries should follow business capabilities and data ownership, not technical layers. Each service should own its data and expose behavior through APIs/events. Avoid splitting controller, service, and repository layers into separate services.

**Detailed answer:**
Potential Email Reply boundaries:
- Identity service: users and roles.
- Reply generation service: prompts and Gemini call.
- History service: generated replies and search.
- Quota/billing service: usage limits.
```mermaid
flowchart LR
    A["API Gateway"] --> B["Identity Service"]
    A --> C["Reply Generation Service"]
    A --> D["History Service"]
    C --> E["Gemini API"]
    C --> F["Quota Service"]
```

**Common mistakes:**
- Splitting by technical layers.
- Cross-service database joins.

**Follow-ups:**
- *What is bounded context?* → A domain boundary where terms/rules have clear meaning.
- *Can code be shared?* → Small utility libraries yes; shared domain models should be avoided.

### Q47. What is an API gateway?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Gateways are common in microservice architectures.

**Short answer (say this first, 20-40 sec):**
An API gateway is the single external entry point that routes requests to internal services. It can handle authentication, rate limiting, TLS termination, routing, and correlation IDs, but business logic should remain in services.

**Detailed answer:**
```mermaid
flowchart LR
    A["Client"] --> B["API Gateway"]
    B --> C["Auth Service"]
    B --> D["Reply Service"]
    B --> E["History Service"]
    D --> F["Gemini API"]
```
Gateway concerns: route `/api/replies`, validate token, apply quotas, add `X-Correlation-Id`, hide internal topology.

**Common mistakes:**
- Putting domain logic in the gateway.
- Letting clients call internal services directly.

**Follow-ups:**
- *Gateway vs load balancer?* → Gateway understands API policies; load balancer mainly distributes traffic.
- *Can gateway aggregate?* → Yes, but avoid making it a mini-monolith.

### Q48. What is service discovery?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Service locations change dynamically in cloud systems.

**Short answer (say this first, 20-40 sec):**
Service discovery lets callers find healthy service instances without hardcoded hostnames/ports. It can be client-side, server-side, or platform-native through Kubernetes Services and DNS.

**Detailed answer:**
```mermaid
sequenceDiagram
    participant ReplyService
    participant Registry
    participant Gateway
    ReplyService->>Registry: register healthy instance
    Gateway->>Registry: discover ReplyService
    Registry-->>Gateway: instance list
    Gateway->>ReplyService: route request
```
Examples: Eureka, Consul, Kubernetes DNS/Service, cloud load balancers.

**Common mistakes:**
- Hardcoding IP addresses.
- Sending traffic to unhealthy instances.

**Follow-ups:**
- *Need Eureka on Kubernetes?* → Usually not; Kubernetes has service discovery.
- *Discovery vs config?* → Discovery finds instances; config stores settings.

### Q49. What is a config server?
`🟡 Intermediate` `Medium` `Must-know SDE-1`

**Why they ask:** Distributed config must be consistent and secure.

**Short answer (say this first, 20-40 sec):**
A config server centralizes external config for many services and environments. It helps with versioning, audit, dynamic refresh, and consistency, but secrets should integrate with a secure secret store. For small apps, environment variables may be enough.

**Detailed answer:**
- Useful for feature flags, service URLs, and resilience thresholds.
- Risks: config server outage, accidental global changes, secret leakage.
- Use Vault/Azure Key Vault/Kubernetes Secrets for sensitive values.
- Email Reply Assistant can start with env vars; add central config when services multiply.

**Common mistakes:**
- Storing secrets as plain Git config.
- Adding config server for one simple service.

**Follow-ups:**
- *Can config refresh live?* → Yes with Spring Cloud patterns, but test carefully.
- *What config should be packaged?* → Safe defaults only.

### Q50. Sync vs async inter-service communication?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Communication style affects coupling and resilience.

**Short answer (say this first, 20-40 sec):**
Synchronous communication waits for a response, such as REST or gRPC. Asynchronous communication uses messages/events, decouples services, and absorbs spikes but introduces eventual consistency and duplicate handling.

**Detailed answer:**
| Style | Pros | Cons | Example |
|---|---|---|---|
| Sync | simple, immediate | cascading failure, latency coupling | `Reply -> Quota` check |
| Async | decoupled, resilient | eventual consistency, duplicates | `ReplyCreated` event |
For AI replies: synchronous for short generation; async job for long Gemini calls.

**Common mistakes:**
- Long synchronous chains across many services.
- Assuming async removes failures.

**Follow-ups:**
- *What is backpressure?* → Slowing intake to protect downstream systems.
- *What do async consumers need?* → Idempotency and retry/dead-letter strategy.

### Q51. How should services own data?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Data ownership distinguishes real microservices from distributed monoliths.

**Short answer (say this first, 20-40 sec):**
Each service should own its data and expose it through APIs or events. Other services should not directly read/write its tables. This preserves independent deployment and domain ownership.

**Detailed answer:**
```mermaid
flowchart TD
    A["Reply Service"] --> D1["Reply DB"]
    B["History Service"] --> D2["History DB"]
    C["Quota Service"] --> D3["Quota DB"]
    A -->|"ReplyCreated event"| B
    A -->|"UsageRecorded event"| C
```
- Reporting can use event-built read models.
- Strong consistency may mean keeping data in one service boundary.

**Common mistakes:**
- Cross-service database joins.
- Sharing JPA entities across services.

**Follow-ups:**
- *Can same DB server host multiple schemas?* → Physically yes, but ownership/access must be separate.
- *How build analytics?* → Events/ETL into read or warehouse models.

### Q52. Explain Resilience4j circuit breaker states.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Circuit breakers prevent cascading failures.

**Short answer (say this first, 20-40 sec):**
Circuit breaker is closed when calls are allowed. If failures exceed threshold, it opens and fails fast. After a wait duration it becomes half-open, allows trial calls, then closes on success or reopens on failure.

**Detailed answer:**
```mermaid
stateDiagram-v2
    [*] --> CLOSED
    CLOSED --> OPEN: failure rate high
    OPEN --> HALF_OPEN: wait elapsed
    HALF_OPEN --> CLOSED: trial success
    HALF_OPEN --> OPEN: trial failure
```
```java
@CircuitBreaker(name="gemini", fallbackMethod="fallback")
Reply generate(ReplyRequest req) { return geminiClient.generate(req); }
Reply fallback(ReplyRequest req, Throwable ex) { return Reply.unavailable(); }
```

**Common mistakes:**
- Circuit breaker without timeouts.
- Returning misleading fallback data.

**Follow-ups:**
- *What is fail fast?* → Do not call known-unhealthy dependency.
- *What threshold matters?* → Failure rate, slow-call rate, and window size.

### Q53. How do retries with backoff and jitter work?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Bad retries can amplify outages.

**Short answer (say this first, 20-40 sec):**
Retries repeat transient failures. Exponential backoff increases wait after each failure, and jitter randomizes waits to avoid retry storms. Retry only safe operations and only for retryable errors.

**Detailed answer:**
```java
IntervalFunction f = IntervalFunction.ofExponentialRandomBackoff(Duration.ofMillis(200), 2.0, 0.5);
RetryConfig cfg = RetryConfig.custom().maxAttempts(3).intervalFunction(f)
    .retryOnException(ex -> ex instanceof SocketTimeoutException).build();
```
- Retry 429/502/503/504 when safe.
- Do not retry 400/401/403.
- Keep total retry time inside user SLA.

**Common mistakes:**
- Retrying at every layer.
- No jitter.

**Follow-ups:**
- *What is retry amplification?* → Multiple layers multiply requests.
- *Where retry?* → At the boundary to the unreliable dependency.

### Q54. What are bulkheads and timeouts?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** They show whether you can isolate failures.

**Short answer (say this first, 20-40 sec):**
A timeout bounds waiting time. A bulkhead limits concurrent calls or threads for a dependency. Together they prevent a slow Gemini API from consuming every servlet thread.

**Detailed answer:**
```yaml
resilience4j:
  bulkhead:
    instances:
      gemini:
        max-concurrent-calls: 25
        max-wait-duration: 0
  timelimiter:
    instances:
      gemini:
        timeout-duration: 3s
```
- Semaphore bulkhead limits concurrency.
- Thread-pool bulkhead isolates execution threads.

**Common mistakes:**
- Timeout greater than gateway/client timeout.
- Unbounded queues.

**Follow-ups:**
- *Status when bulkhead full?* → Often 503.
- *Why not just add threads?* → It increases memory and queueing, not dependency health.

### Q55. What are idempotency keys?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Retries can create duplicate writes.

**Short answer (say this first, 20-40 sec):**
An idempotency key is a unique client-provided key for a write operation. The server stores the key with request hash and result, then returns the same result for duplicate retries. It is useful for costly or chargeable AI generation.

**Detailed answer:**
```mermaid
flowchart TD
    A["POST with Idempotency-Key"] --> B{"Key exists?"}
    B -->|"yes"| C["Return stored response"]
    B -->|"no"| D["Process in transaction"]
    D --> E["Store key and result"]
```
```sql
CREATE TABLE idempotency_keys (
  key TEXT PRIMARY KEY,
  request_hash TEXT NOT NULL,
  response_body JSONB NOT NULL,
  status_code INT NOT NULL
);
```

**Common mistakes:**
- Not comparing request hash.
- In-memory dedupe only.

**Follow-ups:**
- *Who generates key?* → Client for one user intent.
- *Same key different body?* → 409 Conflict.

### Q56. Saga choreography vs orchestration?
`🔴 Advanced` `🔥` `Stretch`

**Why they ask:** Distributed transactions need business-level handling.

**Short answer (say this first, 20-40 sec):**
A saga splits a distributed business process into local transactions with compensating actions. Choreography uses events between services. Orchestration uses a central coordinator to command steps and handle compensation.

**Detailed answer:**
Choreography:
```mermaid
sequenceDiagram
    participant Reply
    participant Billing
    participant History
    Reply->>Billing: UsageRecorded event
    Billing-->>Reply: UsageAccepted event
    Reply->>History: ReplyCreated event
```
Orchestration:
```mermaid
sequenceDiagram
    participant Orchestrator
    participant Billing
    participant Reply
    participant History
    Orchestrator->>Billing: reserve quota
    Orchestrator->>Reply: generate reply
    Orchestrator->>History: save history
```

**Common mistakes:**
- No compensation path.
- Non-idempotent saga steps.

**Follow-ups:**
- *Which is easier to debug?* → Orchestration often is.
- *What is compensation?* → Business undo like release quota.

### Q57. What is the Outbox pattern?
`🔴 Advanced` `🔥` `Stretch`

**Why they ask:** It solves reliable event publishing.

**Short answer (say this first, 20-40 sec):**
Outbox writes business data and an event record in the same database transaction. A publisher later reads the outbox and sends events to the broker. This avoids losing events when DB commit succeeds but message publish fails.

**Detailed answer:**
```mermaid
flowchart TD
    A["Handle command"] --> B["Begin transaction"]
    B --> C["Update business table"]
    C --> D["Insert outbox event"]
    D --> E["Commit"]
    E --> F["Publisher sends event"]
```
```sql
CREATE TABLE outbox_events (
  id UUID PRIMARY KEY,
  event_type TEXT NOT NULL,
  payload JSONB NOT NULL,
  sent_at TIMESTAMPTZ
);
```

**Common mistakes:**
- Publishing event before transaction commits.
- Consumers not idempotent.

**Follow-ups:**
- *What is CDC?* → Streaming DB changes, often used to publish outbox rows.
- *Can duplicates happen?* → Yes, consumers must handle them.

### Q58. What is 2PC and why avoid it in microservices?
`🔴 Advanced` `Medium` `Stretch`

**Why they ask:** They compare classic distributed transactions with sagas.

**Short answer (say this first, 20-40 sec):**
Two-phase commit coordinates multiple resources using prepare and commit phases. It gives atomicity but can block, reduce availability, and tightly couple services. Microservices often prefer local transactions plus saga/outbox patterns.

**Detailed answer:**
- Prepare: coordinator asks every participant if it can commit.
- Commit/rollback: all commit if everyone voted yes.
- Problems: coordinator failure, locks held over network, poor autonomy.
- If strong consistency is mandatory, keep data inside one service/database boundary.

**Common mistakes:**
- Saying 2PC is always impossible.
- Using sagas without compensation.

**Follow-ups:**
- *2PC vs saga?* → Technical atomicity vs business eventual consistency.
- *When use 2PC?* → Controlled environments with compatible resources and strict atomicity.

### Q59. What is distributed tracing?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Microservice debugging needs request-level visibility.

**Short answer (say this first, 20-40 sec):**
Distributed tracing follows one request across services using trace IDs and span IDs. Each service creates spans for operations and propagates context downstream. Tools include Zipkin, Jaeger, Grafana Tempo, and cloud tracing systems.

**Detailed answer:**
```mermaid
sequenceDiagram
    participant Client
    participant Gateway
    participant ReplyService
    participant Gemini
    Client->>Gateway: request with trace
    Gateway->>ReplyService: propagate context
    ReplyService->>Gemini: external call span
    Gemini-->>ReplyService: response
    ReplyService-->>Client: response path
```
- Boot 3 uses Micrometer observation/tracing ecosystem.
- Include trace/correlation IDs in logs.

**Common mistakes:**
- Not propagating trace headers.
- No spans around external calls.

**Follow-ups:**
- *Trace vs span?* → Trace is whole request; span is one operation.
- *Why for Gemini?* → It shows provider latency separately from app latency.

### Q60. What is Strangler Fig migration?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** It is practical monolith-to-microservices migration.

**Short answer (say this first, 20-40 sec):**
Strangler Fig gradually replaces parts of a monolith by routing selected features to new services through a gateway/facade. It avoids big-bang rewrites and allows rollback.

**Detailed answer:**
```mermaid
flowchart LR
    A["Client"] --> B["Gateway facade"]
    B --> C["Old monolith"]
    B --> D["New History Service"]
    B --> E["New Generation Service"]
```
Steps: add facade, extract low-risk feature, route traffic, migrate data ownership, repeat.

**Common mistakes:**
- Big-bang rewrite.
- No rollback path.

**Follow-ups:**
- *First feature to extract?* → Low-coupling, well-tested capability.
- *How avoid dual-write bugs?* → Clear ownership or outbox/events.

### Q61. CAP and PACELC in simple terms?
`⚫ FAANG` `Medium` `Stretch`

**Why they ask:** Distributed system tradeoffs matter at top companies.

**Short answer (say this first, 20-40 sec):**
CAP says during a network partition, a distributed system must choose between consistency and availability. PACELC adds that else, during normal operation, systems still trade latency and consistency.

**Detailed answer:**
- Consistency: reads see latest write.
- Availability: every request gets non-error response.
- Partition tolerance: system handles network splits.
- Email history can be eventual; billing/quota should be stronger.

**Common mistakes:**
- Confusing CAP consistency with ACID consistency.
- Applying CAP to a single-node app.

**Follow-ups:**
- *What is eventual consistency?* → Replicas converge if writes stop.
- *Quota consistency?* → Prefer strong per-user consistency.

### Q62. What consistency model fits Email Reply Assistant features?
`⚫ FAANG` `Medium` `Stretch`

**Why they ask:** They want applied tradeoff reasoning.

**Short answer (say this first, 20-40 sec):**
Reply generation response needs immediate consistency for the user. History/search can be eventually consistent. Quota, billing, and idempotency records need strong consistency to avoid overuse, duplicate charges, or duplicate generation.

**Detailed answer:**
| Feature | Consistency | Why |
|---|---|---|
| Generated reply response | immediate | user waits for result |
| Reply history | eventual acceptable | can appear seconds later |
| Quota/idempotency | strong | avoid duplicate cost |
| Analytics | eventual | aggregate delay OK |

**Common mistakes:**
- Making everything strongly consistent.
- Making billing eventually consistent without safeguards.

**Follow-ups:**
- *How get read-your-write?* → Return written data or read from primary.
- *How show stale history?* → Use pending state or refresh.

## API Design

### Q63. What are REST principles?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** REST is a backend interview baseline.

**Short answer (say this first, 20-40 sec):**
REST models APIs as resources identified by URLs and manipulated using standard HTTP methods. It should be stateless, use proper status codes, expose representations like JSON, and respect HTTP semantics such as caching and idempotency.

**Detailed answer:**
- Use nouns: `/api/replies`, not `/generateReply`.
- Use methods: GET, POST, PUT, PATCH, DELETE.
- Each request carries auth/context; do not depend on server conversational state.
- Return DTOs, not database entities.

**Common mistakes:**
- Verb-based endpoints everywhere.
- Always returning 200.

**Follow-ups:**
- *Is REST always best?* → No; gRPC/GraphQL can fit specific needs.
- *What is safe method?* → GET/HEAD should not change state.

### Q64. How should REST resources be named?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Good naming reveals API maturity.

**Short answer (say this first, 20-40 sec):**
Use plural nouns for collections, path parameters for identity, and query parameters for filtering/sorting. Keep paths lowercase and stable; nest only when ownership is real.

**Detailed answer:**
Good:
- `GET /api/v1/replies/{replyId}`
- `POST /api/v1/replies`
- `GET /api/v1/users/{userId}/replies?tone=formal`
Avoid:
- `/getAllReplies`
- `/createNewReply`
- deep nesting like `/users/{u}/mailboxes/{m}/threads/{t}/replies/{r}` unless necessary.

**Common mistakes:**
- Filters in path segments.
- Exposing table names directly.

**Follow-ups:**
- *Where put search?* → Query params or `/search` resource for complex search.
- *When nest?* → When child belongs to parent.

### Q65. Give a practical HTTP status code table.
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Status code choices affect clients.

**Short answer (say this first, 20-40 sec):**
Use 2xx for success, 4xx for client errors, and 5xx for server/upstream failures. Be consistent and specific enough for clients to react.

**Detailed answer:**
| Status | Use |
|---:|---|
| 200 | successful GET/sync operation |
| 201 | resource created |
| 202 | async job accepted |
| 204 | success with no body |
| 400 | validation/malformed request |
| 401 | missing/invalid auth |
| 403 | authenticated but forbidden |
| 404 | not found |
| 409 | conflict/idempotency mismatch |
| 429 | rate limited |
| 500 | unexpected bug |
| 503 | overloaded/circuit open |
| 504 | upstream timeout |

**Common mistakes:**
- 500 for validation errors.
- 200 with error body.

**Follow-ups:**
- *Duplicate create?* → 409 or same response with idempotency key.
- *Long AI job?* → 202 Accepted.

### Q66. PUT vs PATCH vs POST idempotency?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Idempotency is core API design.

**Short answer (say this first, 20-40 sec):**
POST usually creates/processes and is not idempotent by default. PUT replaces or creates a resource at a known URI and is idempotent. PATCH partially updates; it can be idempotent depending on patch semantics.

**Detailed answer:**
| Method | Semantics | Idempotent |
|---|---|---|
| POST | create/action under collection | no by default |
| PUT | replace at URI | yes |
| PATCH | partial update | depends |
| DELETE | delete | yes effect |
Example: `PATCH` setting tone is idempotent; incrementing usage is not.

**Common mistakes:**
- Using POST for every update.
- Thinking idempotent means same response status every time.

**Follow-ups:**
- *Can POST be idempotent?* → Yes with an idempotency key.
- *Is DELETE idempotent?* → Effect is idempotent even if second response is 404.

### Q67. What API versioning strategies exist?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** APIs evolve while clients remain deployed.

**Short answer (say this first, 20-40 sec):**
Strategies include URI versioning, header versioning, media type versioning, and query parameter versioning. URI versioning is simple and visible; header/media type versioning is cleaner but less obvious. Prefer backward-compatible changes first.

**Detailed answer:**
| Strategy | Example | Tradeoff |
|---|---|---|
| URI | `/api/v1/replies` | simple, visible |
| Header | `API-Version: 1` | clean URL, harder manual testing |
| Media type | `application/vnd.app.v1+json` | precise, complex |
| Query | `?version=1` | easy, less preferred |
Breaking changes: remove/rename fields, change type, make optional field required.

**Common mistakes:**
- Versioning every small addition.
- Removing v1 without usage monitoring.

**Follow-ups:**
- *Backward compatible addition?* → Optional response/request field.
- *How deprecate?* → Announce timeline, monitor, add deprecation headers.

### Q68. Offset vs cursor pagination with SQL?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Pagination affects performance and correctness.

**Short answer (say this first, 20-40 sec):**
Offset pagination uses `LIMIT/OFFSET`, simple but slow and unstable for deep pages. Cursor pagination uses the last seen sort key, is stable and efficient for feeds/history, but cannot jump to arbitrary pages easily.

**Detailed answer:**
PostgreSQL offset:
```sql
SELECT id, subject, created_at FROM replies
WHERE user_id = $1 ORDER BY created_at DESC, id DESC
LIMIT 20 OFFSET 1000;
```
PostgreSQL cursor:
```sql
SELECT id, subject, created_at FROM replies
WHERE user_id = $1 AND (created_at, id) < ($2, $3)
ORDER BY created_at DESC, id DESC LIMIT 20;
```
Use deterministic order and opaque cursor tokens.

**Common mistakes:**
- Cursor on non-unique column only.
- No max page size.

**Follow-ups:**
- *Which for history feed?* → Cursor.
- *Which for small admin pages?* → Offset is acceptable.

### Q69. How should filtering and sorting work?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Listing APIs almost always need filters and sort.

**Short answer (say this first, 20-40 sec):**
Use query parameters for filters and sorting, validate allowed fields, and map them safely to repository or SQL queries. Never concatenate raw client input into SQL.

**Detailed answer:**
Example: `GET /api/v1/replies?tone=formal&status=completed&sort=-createdAt&limit=20`.
- Whitelist fields: `createdAt`, `tone`, `status`.
- Cap `limit` to prevent abuse.
- Add indexes for common filter/sort combinations.

**Common mistakes:**
- Allowing arbitrary SQL in query params.
- Sorting huge tables by unindexed fields.

**Follow-ups:**
- *Invalid sort field status?* → 400 Bad Request.
- *Where document filters?* → OpenAPI spec.

### Q70. What are rate limiting headers?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Public APIs need quotas and client feedback.

**Short answer (say this first, 20-40 sec):**
Rate limiting restricts requests per user, token, tenant, or IP. When exceeded, return 429 and headers that show limit, remaining quota, reset time, and retry guidance.

**Detailed answer:**
| Header | Meaning |
|---|---|
| `RateLimit-Limit` | max in window |
| `RateLimit-Remaining` | remaining requests |
| `RateLimit-Reset` | reset time/seconds |
| `Retry-After` | when to retry after 429 |
Algorithms: fixed window, sliding window, token bucket, leaky bucket. Redis is common for distributed counters.

**Common mistakes:**
- Limiting only by IP behind NAT.
- Returning 429 without retry guidance.

**Follow-ups:**
- *Where enforce?* → Gateway plus service for business quotas.
- *Which endpoint stricter?* → Gemini generation, because it costs money/latency.

### Q71. REST vs GraphQL vs gRPC?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** API style should fit the use case.

**Short answer (say this first, 20-40 sec):**
REST is resource-oriented and simple for public CRUD APIs. GraphQL lets clients request exact fields and aggregate data flexibly. gRPC is schema-first, binary, and efficient for internal service-to-service calls.

**Detailed answer:**
| Style | Best for | Tradeoff |
|---|---|---|
| REST | public resource APIs | simple, cacheable, possible over-fetch |
| GraphQL | frontend aggregation | flexible, harder caching/auth/N+1 |
| gRPC | internal low-latency RPC | efficient, browser/public friction |
Email Reply Assistant public frontend fits REST; internal quota/generation calls could later use gRPC.

**Common mistakes:**
- Picking GraphQL to avoid API design.
- Exposing gRPC directly to browsers without tooling plan.

**Follow-ups:**
- *Does GraphQL replace auth?* → No, field/object authorization still required.
- *Is gRPC always faster?* → Often efficient, but design and payloads matter.

### Q72. WebSockets vs Server-Sent Events?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Realtime updates need the right transport.

**Short answer (say this first, 20-40 sec):**
WebSockets are bidirectional long-lived connections. Server-Sent Events are one-way server-to-client streams over HTTP. Use SSE for AI generation progress if the client only receives updates; use WebSockets for interactive bidirectional sessions.

**Detailed answer:**
| Feature | WebSocket | SSE |
|---|---|---|
| Direction | bidirectional | server to client |
| Protocol | WebSocket | HTTP stream |
| Browser retry | app-managed | built-in EventSource retry |
| Use | chat/collab | progress/notifications |

**Common mistakes:**
- Using WebSockets for simple notifications.
- No auth/heartbeat for long-lived connections.

**Follow-ups:**
- *Can SSE stream binary?* → Not directly; it is text/event-stream.
- *Simpler for token streaming?* → SSE is often simpler.

### Q73. How do you maintain backward compatibility?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** APIs must evolve without breaking deployed clients.

**Short answer (say this first, 20-40 sec):**
Prefer additive changes, preserve old fields/semantics, deprecate before removal, and use contract tests. Create a new version for breaking changes. Monitor client usage before sunsetting old versions.

**Detailed answer:**
Compatible: add optional field, add endpoint, add optional request parameter.
Breaking: rename field, change type, remove enum behavior, make optional field required.
Useful headers:
```http
Deprecation: true
Sunset: Wed, 31 Dec 2026 23:59:59 GMT
```

**Common mistakes:**
- Assuming mobile clients update immediately.
- Reusing an old field with new meaning.

**Follow-ups:**
- *Consumer-driven contracts?* → Consumers define expectations providers must satisfy.
- *Can adding enum break?* → Yes, strict clients may reject unknown values.

### Q74. What should an API error response include?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Good errors reduce support and client bugs.

**Short answer (say this first, 20-40 sec):**
Include status, stable error type/code, message/detail, correlation ID, and field errors when relevant. Never expose stack traces, secrets, or internal class names.

**Detailed answer:**
```json
{
  "type": "https://api.emailreply.com/problems/validation",
  "title": "Validation failed",
  "status": 400,
  "detail": "Invalid request fields",
  "correlationId": "abc-123",
  "errors": [{ "field": "emailBody", "message": "must not be blank" }]
}
```
Use one error shape across endpoints, ideally ProblemDetail-compatible.

**Common mistakes:**
- Different error formats per controller.
- Leaking Java exception names.

**Follow-ups:**
- *Downstream timeout error?* → 504 with provider-timeout type.
- *Human message vs machine code?* → Keep both: stable code/type and readable detail.

### Q75. How do you design API authentication and authorization?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** API security is broader than adding JWT.

**Short answer (say this first, 20-40 sec):**
Use HTTPS, OAuth2/OIDC for user login, JWT resource server for stateless APIs, API keys for simple server integrations, and mTLS for high-trust service-to-service. Authorization should use scopes/roles plus resource ownership checks.

**Detailed answer:**
Example scopes: `email:generate`, `reply:read`, `admin:metrics`.
Controls:
- Short-lived access tokens.
- Method security for ownership.
- Audit sensitive actions.
- Rate limit by user/client.
- Do not expose Gemini API key to frontend.

**Common mistakes:**
- Treating API key as user identity.
- Tokens with no expiry.

**Follow-ups:**
- *API key vs JWT?* → API key identifies app/client; JWT often carries user claims.
- *Zero trust idea?* → Verify identity and authorization across boundaries.

### Q76. What is HATEOAS and do you need it?
`⚫ FAANG` `Medium` `Stretch`

**Why they ask:** It checks deeper REST theory and pragmatic judgment.

**Short answer (say this first, 20-40 sec):**
HATEOAS means responses include links to valid next actions. It improves discoverability but many practical JSON APIs do not fully implement it. Use it when workflow navigation matters; otherwise documented endpoints may be enough.

**Detailed answer:**
```json
{
  "id": "rpl_123",
  "status": "completed",
  "links": [
    { "rel": "self", "href": "/api/v1/replies/rpl_123" },
    { "rel": "feedback", "href": "/api/v1/replies/rpl_123/feedback" }
  ]
}
```

**Common mistakes:**
- Adding links inconsistently.
- Exposing unauthorized actions as links.

**Follow-ups:**
- *Richardson maturity?* → Resources, verbs, hypermedia levels.
- *Use here?* → Useful for async job status/next actions, not mandatory everywhere.

### Q77. How should OpenAPI documentation be used?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** API docs are part of production delivery.

**Short answer (say this first, 20-40 sec):**
OpenAPI documents endpoints, schemas, auth, status codes, errors, pagination, rate limits, and examples. Keep it generated or validated in CI so docs do not drift from implementation.

**Detailed answer:**
Document:
- `Authorization` and idempotency headers.
- Request/response examples.
- ProblemDetail error schema.
- 400/401/403/429/5xx responses.
- Cursor pagination and rate-limit headers.
Springdoc can generate Swagger UI, but secure/disable it in production if needed.

**Common mistakes:**
- Only documenting happy path.
- Missing error and rate-limit responses.

**Follow-ups:**
- *Contract-first?* → Define OpenAPI first, then implement/validate.
- *Can clients be generated?* → Yes, SDKs can be generated from spec.

### Q78. How do you design caching for APIs?
`🟡 Intermediate` `Medium` `Must-know SDE-1`

**Why they ask:** Caching improves performance but can create stale/unsafe data.

**Short answer (say this first, 20-40 sec):**
Cache safe, repeatable reads; avoid caching user-private or rapidly changing data unless keys and invalidation are correct. Use HTTP cache headers for clients/CDNs and server caches like Redis for expensive backend reads.

**Detailed answer:**
- Templates/config can be cached.
- Generated reply history per user needs user-scoped cache keys.
- Never cache responses containing another user's data.
- Use `ETag`, `Cache-Control`, and invalidation rules.

**Common mistakes:**
- Cache key missing user/tenant.
- Caching errors too long.

**Follow-ups:**
- *What is ETag?* → Version identifier for conditional GET.
- *Should Gemini output be cached?* → Only if product accepts same prompt/tone returning same result and privacy is handled.

### Q79. How do you handle partial failures in API responses?
`🔴 Advanced` `Medium` `Stretch`

**Why they ask:** Aggregating APIs often have partial dependency failures.

**Short answer (say this first, 20-40 sec):**
Define whether the endpoint is all-or-nothing or can return partial data. If partial is allowed, include clear status per component and avoid hiding critical failures. For writes, prefer clear failure over partial success unless workflow state is persisted.

**Detailed answer:**
Example aggregation: reply details plus analytics.
- Reply missing: 404.
- Analytics unavailable: return reply with `analyticsStatus: unavailable` if non-critical.
- Generation write failed after quota reserve: saga compensation needed.

**Common mistakes:**
- Returning 200 while critical operation failed.
- No machine-readable partial status.

**Follow-ups:**
- *HTTP 207?* → Possible for multi-status, but not common in JSON APIs.
- *Better for writes?* → Persist job state and expose status endpoint.

### Q80. How do you design API observability?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Production APIs need measurable behavior.

**Short answer (say this first, 20-40 sec):**
Add structured logs, metrics, traces, correlation IDs, and meaningful dashboards. Track request rate, error rate, latency percentiles, dependency latency, saturation, and business metrics like generated replies and Gemini failures.

**Detailed answer:**
Golden signals:
- Latency p50/p95/p99.
- Traffic by route/status.
- Errors by type.
- Saturation: Tomcat threads, DB pool, bulkheads.
- Dependency metrics: Gemini timeout/429/5xx.

**Common mistakes:**
- Only average latency.
- Logs without request IDs.

**Follow-ups:**
- *RED metrics?* → Rate, Errors, Duration.
- *USE metrics?* → Utilization, Saturation, Errors.

## Email Reply Assistant API Exercise

### Q81. Design the REST API for the Email Reply Assistant.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** This ties API design to your resume project.

**Short answer (say this first, 20-40 sec):**
Expose versioned endpoints for generating replies, checking reply status/history, managing templates, and submitting feedback. Validate request length and tone, use idempotency for generation, and return consistent ProblemDetail errors.

**Detailed answer:**
| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/replies` | generate or create generation job |
| GET | `/api/v1/replies/{id}` | fetch reply |
| GET | `/api/v1/replies` | list history |
| POST | `/api/v1/replies/{id}/feedback` | capture feedback |
| GET | `/api/v1/templates` | list prompt templates |
Request:
```json
{ "emailBody": "Can we reschedule?", "tone": "formal", "maxWords": 120 }
```

**Common mistakes:**
- `/generateEmailReply` verb endpoint.
- No idempotency or validation.

**Follow-ups:**
- *Sync or async?* → Sync for short SLA; async 202 for long generation.
- *Where store Gemini key?* → Server-side secret/env, never frontend.

### Q82. Design async generation for slow Gemini responses.
`🔴 Advanced` `🔥` `Must-know SDE-1`

**Why they ask:** It tests latency-aware API design.

**Short answer (say this first, 20-40 sec):**
Return `202 Accepted` with a job/reply ID and `Location` header, enqueue work, process it in a worker, and let the client poll `GET /replies/{id}` or subscribe through SSE. This avoids holding HTTP threads for long AI calls.

**Detailed answer:**
```mermaid
sequenceDiagram
    participant Client
    participant API
    participant Queue
    participant Worker
    participant Gemini
    Client->>API: POST /api/v1/replies
    API->>Queue: enqueue job
    API-->>Client: 202 Location /replies/rpl_123
    Worker->>Gemini: generate
    Gemini-->>Worker: text
    Worker->>API: save completed result
    Client->>API: GET /replies/rpl_123
```
Statuses: `queued`, `processing`, `completed`, `failed`, `expired`.

**Common mistakes:**
- Holding request open for minutes.
- No cleanup for old jobs.

**Follow-ups:**
- *How notify without polling?* → SSE or WebSocket.
- *What about retries?* → Worker retries with backoff and dead-letter on repeated failure.

### Q83. How would you add idempotency to `POST /api/v1/replies`?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** AI generation can be retried and may cost money.

**Short answer (say this first, 20-40 sec):**
Accept `Idempotency-Key` per user intent. Store user ID, key, request hash, status, and response/job ID with a unique constraint. Duplicate same request returns same result; same key with different body returns 409.

**Detailed answer:**
```sql
ALTER TABLE reply_requests
ADD CONSTRAINT uq_user_idempotency UNIQUE (user_id, idempotency_key);
```
Behavior:
- New key: process.
- Same key + same hash: return stored response/job.
- Same key + different hash: 409 Conflict.
- In-progress: return same 202 Location.

**Common mistakes:**
- Dedupe only by body text.
- No database unique constraint.

**Follow-ups:**
- *How long store keys?* → At least retry window, e.g., 24 hours or business-defined.
- *Who generates key?* → Client per button click/user intent.

### Q84. How do you secure and rate-limit this API?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** The API uses a paid external AI dependency.

**Short answer (say this first, 20-40 sec):**
Use HTTPS, JWT/OAuth2 for users, server-side Gemini secrets, scope-based authorization, and rate limits by user/tenant. Generation endpoints should have stricter quotas than read endpoints. Return 429 with retry headers.

**Detailed answer:**
- Scope: `email:generate` for POST replies.
- Limit: e.g., per minute and per day per user plan.
- Protect actuator/admin endpoints separately.
- Audit generation and failed auth attempts.
- Never expose Gemini API key to browser.

**Common mistakes:**
- Frontend calls Gemini directly with API key.
- One global IP-only limit.

**Follow-ups:**
- *Where implement quota?* → Gateway for coarse limits, service for business quota.
- *What status over quota?* → 429 Too Many Requests or 402/403 for plan restrictions depending product policy.

## Production Scenarios

### Q85. Scenario: duplicate replies are created after frontend retries.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** This tests idempotency and transaction thinking.

**Short answer (say this first, 20-40 sec):**
Add idempotency keys, store them with a unique constraint, and return the previous result for duplicate retries. Fix frontend retry logic so one user action reuses one key.

**Detailed answer:**
Investigation: compare timestamps, request bodies, user ID, frontend timeout, and network errors. Fix with `(user_id, idempotency_key)` uniqueness and atomic transaction around key/result creation.

**Common mistakes:**
- In-memory dedupe only, failing across replicas.
- New idempotency key on every retry.

**Follow-ups:**
- *Same key different request?* → 409 Conflict.
- *Async duplicate?* → Return same job URL.

### Q86. Scenario: gateway returns 502 or 504.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Gateway failures require layered debugging.

**Short answer (say this first, 20-40 sec):**
502 means bad upstream response or connection failure; 504 means upstream timeout. Check route config, service health, discovery/DNS, readiness, TLS, gateway timeout, and traces from gateway to service.

**Detailed answer:**
- If Gemini generation is long, use async API rather than increasing timeout blindly.
- Check whether service receives request via logs/traces.
- Confirm no circuit breaker/bulkhead rejection.
- Validate readiness probes during deployment.

**Common mistakes:**
- Only increasing gateway timeout.
- No correlation ID across gateway and service.

**Follow-ups:**
- *502 vs 503?* → 502 bad upstream; 503 unavailable/no healthy capacity.
- *What is readiness?* → Instance is ready to receive traffic.

### Q87. Scenario: distributed workflow fails after quota reserved but before reply saved.
`🔴 Advanced` `🔥` `Stretch`

**Why they ask:** They test saga and compensation design.

**Short answer (say this first, 20-40 sec):**
Model it as a saga with persisted state. Reserve quota, generate reply, save reply, then confirm usage. If generation or save fails, run compensation to release quota. Make every step idempotent and retryable.

**Detailed answer:**
```sql
CREATE TABLE generation_saga (
  id UUID PRIMARY KEY,
  user_id TEXT NOT NULL,
  state TEXT NOT NULL,
  retry_count INT NOT NULL DEFAULT 0
);
```
States: `QUOTA_RESERVED`, `GENERATING`, `SAVED`, `CONFIRMED`, `COMPENSATING`, `FAILED`.

**Common mistakes:**
- Assuming remote calls roll back like DB writes.
- No persisted workflow state.

**Follow-ups:**
- *When avoid saga?* → Keep strongly consistent data inside one service transaction.
- *How handle repeated compensation?* → Make compensation idempotent.

### Q88. Scenario: traces show high latency but CPU is low.
`🔴 Advanced` `🔥` `Stretch`

**Why they ask:** Latency often comes from waiting, not CPU.

**Short answer (say this first, 20-40 sec):**
Low CPU with high latency usually means waiting on network, database locks, connection pools, thread pools, queues, or downstream APIs. Use traces to find slow spans and metrics to check saturation.

**Detailed answer:**
Check p95/p99, Gemini span duration, Hikari pool pending threads, Tomcat busy threads, queue lag, DNS/TLS overhead, and circuit breaker slow-call metrics.

**Common mistakes:**
- Scaling CPU without finding wait source.
- Looking only at average latency.

**Follow-ups:**
- *DB pool starvation metric?* → Active connections near max and pending threads high.
- *Tail latency?* → p95/p99, not average.

### Q89. Scenario: message consumer processes duplicate events.
`🔴 Advanced` `Medium` `Stretch`

**Why they ask:** At-least-once delivery is common.

**Short answer (say this first, 20-40 sec):**
Assume duplicate events happen. Make consumers idempotent by storing processed event IDs, using unique constraints, and making business updates safe to repeat.

**Detailed answer:**
```sql
CREATE TABLE processed_events (
  event_id UUID PRIMARY KEY,
  processed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```
Consumer transaction: insert event ID; if conflict, skip; apply update; commit; then acknowledge.

**Common mistakes:**
- Assuming exactly-once business effects automatically.
- Acknowledging before durable processing.

**Follow-ups:**
- *At-least-once means?* → Delivered one or more times until acked.
- *Does ordering remove duplicates?* → No.

### Q90. Scenario: OpenAPI clients break after adding a response field.
`🟡 Intermediate` `Medium` `Must-know SDE-1`

**Why they ask:** Even additive changes can break strict clients.

**Short answer (say this first, 20-40 sec):**
Adding a field is compatible only if clients ignore unknown fields. Some generated or strict clients fail. Communicate schema evolution rules, add consumer contract tests, and version if the contract forbids extra fields.

**Detailed answer:**
- Check client JSON settings such as ignoring unknown properties.
- Update OpenAPI and SDKs.
- Avoid changing field meaning.
- Add staging validation for important consumers.

**Common mistakes:**
- Assuming every client is tolerant.
- No changelog or deprecation policy.

**Follow-ups:**
- *Jackson tolerant client?* → `@JsonIgnoreProperties(ignoreUnknown = true)`.
- *Can enum addition break?* → Yes for strict switches.

### Q91. Scenario: migrating Email Reply Assistant from monolith to microservices.
`⚫ FAANG` `🔥` `Stretch`

**Why they ask:** Migration requires strategy, not big-bang rewriting.

**Short answer (say this first, 20-40 sec):**
First modularize the monolith, add tests and observability, identify bounded contexts, then use Strangler Fig through a gateway. Extract low-risk capabilities like templates/history before high-risk generation/quota.

**Detailed answer:**
```mermaid
flowchart TD
    A["Modular monolith"] --> B["Gateway facade"]
    B --> C["Extract History Service"]
    C --> D["Outbox events"]
    D --> E["Extract Template Service"]
    E --> F["Extract Generation Service"]
```
Success metrics: independent deployment, stable latency/error rate, clear data ownership, and no data drift.

**Common mistakes:**
- Big-bang rewrite.
- Extracting before tests and tracing.

**Follow-ups:**
- *What first service?* → Low-coupling history/templates.
- *How avoid dual writes?* → Outbox/events and clear ownership transitions.

### Q92. Scenario: memory and cost spike after enabling AI streaming.
`🔴 Advanced` `Medium` `Stretch`

**Why they ask:** Streaming changes resource and cost profiles.

**Short answer (say this first, 20-40 sec):**
Limit concurrent streams, enforce per-user quotas, use backpressure, timeouts, and cancellation when clients disconnect. Track token usage/cost and ensure streaming responses do not buffer entire outputs in memory.

**Detailed answer:**
- Use SSE for one-way streaming and close resources on disconnect.
- Apply rate limits and max generation length.
- Monitor active streams, bytes sent, Gemini token usage, and error rate.
- Prefer bounded buffers; avoid storing every token unless needed.

**Common mistakes:**
- No cancellation propagation to Gemini call.
- Unlimited streams per user.

**Follow-ups:**
- *Why backpressure?* → Prevent producer from overwhelming consumers/resources.
- *What if client disconnects?* → Cancel downstream call and release resources.

## Revision Cheat Sheet

- Prefer modular monolith until microservices are justified by scale, teams, deployment independence, or fault isolation.
- Boundaries follow business capabilities and data ownership, not technical layers.
- API gateway routes and enforces cross-cutting policies; keep business logic in services.
- Service discovery avoids hardcoded instance locations.
- Sync is simple but latency-coupled; async decouples but adds duplicates and eventual consistency.
- Each service owns its data; avoid cross-service DB joins.
- Circuit breaker states: closed, open, half-open.
- Retry only transient safe failures with backoff and jitter.
- Bulkheads and timeouts stop one dependency from consuming all resources.
- Idempotency keys make write retries safe.
- Saga uses local transactions plus compensation; outbox reliably publishes events after DB commit.
- 2PC gives atomicity but often hurts autonomy and availability.
- Distributed tracing uses trace IDs and spans across services.
- REST uses nouns/resources, HTTP methods, statelessness, and meaningful status codes.
- PUT is idempotent replace; PATCH partial update; POST create/action unless idempotency key is used.
- Cursor pagination is best for deep/changing feeds; offset is simpler for small pages.
- Rate limiting should return 429 plus `Retry-After` and rate-limit headers.
- REST suits public APIs; GraphQL suits flexible frontend aggregation; gRPC suits internal efficient RPC.
- Email Reply Assistant API should use `/api/v1/replies`, validation, idempotency, async 202 option, and ProblemDetail errors.

## Flashcards

- Q → When not microservices? A → Small team, unclear domain, weak ops, strong transaction needs.
- Q → Gateway role? A → External routing plus auth/rate-limit/correlation policies.
- Q → Service discovery? A → Dynamic lookup of healthy service instances.
- Q → Circuit states? A → Closed, open, half-open.
- Q → Why jitter? A → Avoid synchronized retry storms.
- Q → Bulkhead? A → Resource isolation per dependency/operation.
- Q → Idempotency key? A → Client intent key for safe write retries.
- Q → Saga? A → Local transactions with compensating actions.
- Q → Outbox solves? A → Atomic DB write plus reliable event publishing.
- Q → 2PC downside? A → Blocking, coupling, lower availability.
- Q → Cursor pagination condition? A → Fetch after last stable sort key.
- Q → Rate limit status? A → 429 Too Many Requests.
- Q → SSE vs WebSocket? A → One-way server stream vs bidirectional channel.
- Q → Async creation status? A → 202 Accepted with Location.
- Q → Best API shape for generation? A → `POST /api/v1/replies` with validation and idempotency.
