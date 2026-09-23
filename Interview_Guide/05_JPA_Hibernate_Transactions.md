# 05 · JPA, Hibernate, and Transactions

A complete interview study guide for JPA, Hibernate, Spring Data JPA, entity lifecycle, persistence context, fetching, locking, transactions, isolation, and production failure debugging. Answers emphasize precise behavior, generated SQL, and Spring proxy realities.

🟢 Beginner · 🟡 Intermediate · 🔴 Advanced · ⚫ FAANG · 🔥 = very frequently asked

## Table of Contents
- [Real Interview Questions](#real-interview-questions)
- [JPA, Hibernate, and Entity Lifecycle](#jpa-hibernate-and-entity-lifecycle)
- [Fetching, Relationships, and Query Performance](#fetching-relationships-and-query-performance)
- [Spring Transactions](#spring-transactions)
- [Locking, Isolation, and Production Scenarios](#locking-isolation-and-production-scenarios)
- [Revision Cheat Sheet](#revision-cheat-sheet)
- [Flashcards](#flashcards)

## Real Interview Questions

### Q1. Explain `CascadeType` and `orphanRemoval`. How is `orphanRemoval` different from `CascadeType.REMOVE`?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Parent-child entity mapping is a very common Hibernate interview area.

**Short answer (say this first, 20-40 sec):**
Cascade controls which entity operations propagate from parent to child, such as persist, merge, or remove. `CascadeType.REMOVE` deletes children when the parent is removed. `orphanRemoval=true` deletes a child when it is removed from the parent's collection, even if the parent itself is not deleted.

**Detailed answer:**
Entity code:
```java
import jakarta.persistence.*;
import java.util.*;

@Entity
class OrderEntity {
  @Id @GeneratedValue Long id;
  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private final List<OrderLine> lines = new ArrayList<>();
  void addLine(OrderLine line){ lines.add(line); line.order = this; }
  void removeLine(OrderLine line){ lines.remove(line); line.order = null; }
}
@Entity
class OrderLine {
  @Id @GeneratedValue Long id;
  @ManyToOne(fetch = FetchType.LAZY) OrderEntity order;
  String sku;
}
```
Resulting SQL examples:
```sql
-- parent persist with CascadeType.PERSIST or ALL
insert into order_entity (id) values (?);
insert into order_line (order_id, sku, id) values (?, ?, ?);

-- remove one line from collection with orphanRemoval=true
delete from order_line where id = ?;

-- delete parent with CascadeType.REMOVE or ALL
delete from order_line where id = ?;
delete from order_entity where id = ?;
```
Difference table:
| Feature | `CascadeType.REMOVE` | `orphanRemoval=true` |
|---|---|---|
| Trigger | Parent is removed | Child is removed from parent association |
| Use case | Delete aggregate with parent | Child cannot exist without parent |
| SQL | Child deletes during parent delete | Child delete during collection change |

**Common mistakes:**
- Using `CascadeType.ALL` on `@ManyToMany`, accidentally deleting shared entities.
- Removing child from collection but not maintaining both sides of a bidirectional relationship.
- Expecting cascade to run for JPQL bulk deletes.

**Follow-ups:**
- *Should `orphanRemoval` be used for shared children?* → No, only for true aggregate children owned by one parent.
- *Does `CascadeType.REMOVE` remove orphans when parent stays?* → No, that is `orphanRemoval`.

### Q2. Have you used locking? Explain optimistic, pessimistic, `SELECT FOR UPDATE`, and `SKIP LOCKED`.
`🔴 Advanced` `🔥` `Must-know SDE-2`

**Why they ask:** They want real DB-level concurrency understanding.

**Short answer (say this first, 20-40 sec):**
Optimistic locking uses a `@Version` column and fails on commit if another transaction changed the row. Pessimistic locking acquires a DB row lock, commonly translated to `SELECT ... FOR UPDATE`. `SKIP LOCKED` lets workers skip already locked rows, useful for job queues.

**Detailed answer:**
Optimistic entity and retry:
```java
import jakarta.persistence.*;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@Entity
class Product {
  @Id Long id;
  @Version long version;
  int stock;
}

class ProductService {
  String reserveWithRetry(Long id) {
    for (int attempt = 1; attempt <= 3; attempt++) {
      try { reserveOnce(id); return "reserved"; }
      catch (ObjectOptimisticLockingFailureException ex) { if (attempt == 3) throw ex; }
    }
    throw new IllegalStateException();
  }
  @org.springframework.transaction.annotation.Transactional
  void reserveOnce(Long id) { /* load Product, decrement stock, commit checks version */ }
}
```
Pessimistic repository:
```java
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

interface ProductRepository extends JpaRepository<Product, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from Product p where p.id = :id")
  Product lockById(@Param("id") Long id);

  @Query(value = "select * from job where status='READY' order by id for update skip locked limit 10", nativeQuery = true)
  java.util.List<Job> claimJobs();
}
```
Concurrent update flow:
```mermaid
sequenceDiagram
  participant T1 as Tx1
  participant T2 as Tx2
  participant DB as Database
  T1->>DB: Read product version 1
  T2->>DB: Read product version 1
  T1->>DB: Update stock and version to 2
  DB-->>T1: Commit OK
  T2->>DB: Update where version = 1
  DB-->>T2: 0 rows updated
  T2-->>T2: OptimisticLockException and retry
```

**Common mistakes:**
- Using Java `synchronized` instead of DB lock for multi-pod row updates.
- Holding pessimistic locks while calling external APIs.
- Retrying optimistic conflicts without a retry limit.

**Follow-ups:**
- *When optimistic locking?* → Low contention, user can retry or merge.
- *When `SKIP LOCKED`?* → Multiple workers claim queue rows without waiting on each other.

## JPA, Hibernate, and Entity Lifecycle

### Q3. JPA vs Hibernate vs Spring Data JPA?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Candidates often mix these names.

**Short answer (say this first, 20-40 sec):**
JPA is the specification for ORM APIs and annotations. Hibernate is a JPA implementation with extra features. Spring Data JPA sits on top and generates repository implementations, query methods, and integration with Spring transactions.

**Detailed answer:**
| Term | Meaning |
|---|---|
| JPA | Specification: `EntityManager`, annotations, JPQL |
| Hibernate | ORM provider implementing JPA |
| Spring Data JPA | Repository abstraction over JPA provider |

Example:
```java
interface UserRepository extends JpaRepository<User, Long> {
  java.util.Optional<User> findByEmail(String email);
}
```

**Common mistakes:**
- Saying Spring Data JPA is the ORM provider.
- Using Hibernate-specific annotations without realizing portability trade-off.
- Assuming repository `save` always means SQL insert immediately.

**Follow-ups:**
- *Can JPA work without Hibernate?* → Yes, with another provider like EclipseLink.
- *Does Spring Data replace transactions?* → No, it integrates with Spring transaction management.

### Q4. What is an entity and what rules should it follow?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Entity basics affect every Hibernate issue.

**Short answer (say this first, 20-40 sec):**
An entity is a persistent domain object mapped to a database table. It needs an identifier, a no-arg constructor, and should avoid final classes/methods that prevent proxying. Business logic can exist in entities, but relationships and equality must be designed carefully.

**Detailed answer:**
```java
import jakarta.persistence.*;
@Entity
class Customer {
  @Id @GeneratedValue private Long id;
  private String email;
  protected Customer() { }
  Customer(String email){ this.email = email; }
}
```
Use `jakarta.persistence` with Spring Boot 3 and `javax.persistence` with older Spring Boot 2 apps.

**Common mistakes:**
- No no-arg constructor.
- Public mutable collections without helper methods.
- Lombok `@Data` generating risky `toString`, equals, and hashCode.

**Follow-ups:**
- *Can an entity be immutable?* → Mostly, but JPA still needs construction/proxy support.
- *Why protected constructor?* → JPA can use it, but application code avoids invalid construction.

### Q5. Explain the JPA entity lifecycle.
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Lifecycle explains persist, merge, dirty checking, and detach behavior.

**Short answer (say this first, 20-40 sec):**
Entities move through transient, managed, detached, and removed states. Managed entities are tracked by the persistence context and dirty-checked. Detached entities are no longer tracked; changes are not flushed unless merged.

**Detailed answer:**
```mermaid
stateDiagram-v2
  [*] --> Transient
  Transient --> Managed: persist
  Managed --> Removed: remove
  Managed --> Detached: detach or close
  Detached --> Managed: merge
  Removed --> [*]: flush delete
  Managed --> [*]: transaction commit
```
```java
entityManager.persist(user);      // transient to managed
entityManager.detach(user);       // managed to detached
User managedCopy = entityManager.merge(user); // detached to managed copy
```

**Common mistakes:**
- Editing detached entity and expecting automatic SQL.
- Thinking `merge` reattaches the same object; it returns a managed copy.
- Calling `remove` on detached entities.

**Follow-ups:**
- *Which state gets dirty checked?* → Managed.
- *What is transient?* → New object not associated with persistence context.

### Q6. What is the persistence context and dirty checking?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** This is core Hibernate behavior.

**Short answer (say this first, 20-40 sec):**
The persistence context is the first-level cache and unit of work for managed entities. Hibernate stores snapshots of managed entities and compares them at flush time. If a managed entity changed, Hibernate generates SQL automatically; no explicit save is needed inside the transaction.

**Detailed answer:**
```mermaid
flowchart TD
  A["Load entity"] --> B["Persistence context stores managed instance and snapshot"]
  B --> C["Application changes field"]
  C --> D["Flush time dirty check"]
  D --> E{"Changed?"}
  E -->|"yes"| F["Generate update SQL"]
  E -->|"no"| G["No SQL"]
```
```java
@Transactional
void changeName(Long id){ User u = em.find(User.class, id); u.setName("Azam"); } // update at flush
```

**Common mistakes:**
- Calling repository `save` unnecessarily for managed entities.
- Expecting dirty checking outside a transaction/persistence context.
- Mutating detached objects.

**Follow-ups:**
- *What is first-level cache scope?* → Persistence context, usually one transaction.
- *When does SQL run?* → On flush, often before commit or before queries.

### Q7. What are flush modes?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** Flush timing explains surprising SQL execution.

**Short answer (say this first, 20-40 sec):**
Flush synchronizes persistence context changes to the database without committing the transaction. `AUTO` flushes before commit and before queries that need consistent results. `COMMIT` delays flush until commit when possible.

**Detailed answer:**
```java
entityManager.setFlushMode(FlushModeType.COMMIT);
entityManager.persist(order);
entityManager.flush(); // forces insert now, transaction still open
```
Flush can reveal constraint violations before commit.

**Common mistakes:**
- Thinking flush commits.
- Assuming no SQL before transaction end.
- Using `saveAndFlush` everywhere.

**Follow-ups:**
- *Flush vs commit?* → Flush sends SQL; commit makes it durable and visible per isolation.
- *Why flush before query?* → Query should see pending changes when needed.

### Q8. `persist` vs `merge`?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** This prevents detached entity bugs.

**Short answer (say this first, 20-40 sec):**
`persist` makes a new transient entity managed and schedules insert. `merge` copies state from a detached entity into a managed instance and returns that managed instance. The object passed to `merge` remains detached.

**Detailed answer:**
```java
User u = new User("a@x.com");
em.persist(u); // u becomes managed

User detached = getDetachedUser();
User managed = em.merge(detached); // use managed from now on
```

**Common mistakes:**
- Ignoring the return value of `merge`.
- Using `merge` for every save and overwriting fields with stale detached data.
- Passing detached children accidentally.

**Follow-ups:**
- *Which one for new entity?* → `persist`.
- *Can merge insert?* → Yes, if no existing row is found depending on identifier state.

### Q9. `save` vs `saveAndFlush` in Spring Data JPA?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Many candidates misunderstand repository methods.

**Short answer (say this first, 20-40 sec):**
`save` persists or merges the entity and SQL may be delayed until flush/commit. `saveAndFlush` saves and immediately flushes pending changes to the database within the same transaction. Use `saveAndFlush` only when you need early constraint detection or generated DB effects before continuing.

**Detailed answer:**
```java
repo.save(order);          // may flush later
repo.saveAndFlush(order);  // forces flush now, not commit
```
Immediate flush can hurt batching and performance.

**Common mistakes:**
- Thinking `saveAndFlush` commits.
- Using it to fix visibility issues caused by wrong transaction design.
- Overusing it and disabling batch efficiency.

**Follow-ups:**
- *Does `save` always insert?* → No, it may merge/update depending on entity state.
- *When useful?* → Need generated DB constraint error before making next decision.

### Q10. `findById` vs `getReference`?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Lazy proxies are common in associations.

**Short answer (say this first, 20-40 sec):**
`findById` hits the database immediately and returns the entity if present. `getReference` returns a lazy proxy without querying immediately, useful when you only need a reference for a foreign key. Accessing non-id fields triggers loading and can throw if the row does not exist.

**Detailed answer:**
```java
Order order = new Order();
Customer customerRef = entityManager.getReference(Customer.class, customerId);
order.setCustomer(customerRef); // can set FK without loading customer now
```
Spring Data's `getReferenceById` maps to this behavior.

**Common mistakes:**
- Accessing proxy outside transaction and getting `LazyInitializationException`.
- Using getReference when existence must be validated immediately.
- Serializing proxies directly.

**Follow-ups:**
- *When prefer find?* → When you need data or existence validation now.
- *Can getReference throw?* → Yes, when proxy initializes and row is absent.

### Q11. Explain owning side and `mappedBy`.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Relationship bugs often come from wrong ownership.

**Short answer (say this first, 20-40 sec):**
The owning side is the side with the foreign key and controls database updates. In a bidirectional association, `mappedBy` marks the inverse side and points to the field on the owning side. You must keep both Java sides consistent for correct in-memory behavior.

**Detailed answer:**
```java
@Entity class Post { @OneToMany(mappedBy="post") List<Comment> comments = new ArrayList<>(); void add(Comment c){ comments.add(c); c.post=this; } }
@Entity class Comment { @ManyToOne(fetch=FetchType.LAZY) Post post; }
```
`Comment.post` owns the FK column.

**Common mistakes:**
- Updating only inverse side and expecting FK update.
- Wrong `mappedBy` name.
- Missing helper methods.

**Follow-ups:**
- *Which side has FK in many-to-one?* → The many side.
- *Does mappedBy create a column?* → No, it references the owning field.

### Q12. What are default fetch types and LAZY vs EAGER?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Fetch defaults cause performance and serialization issues.

**Short answer (say this first, 20-40 sec):**
`@ManyToOne` and `@OneToOne` default to EAGER. `@OneToMany` and `@ManyToMany` default to LAZY. Prefer LAZY by default and fetch explicitly per use case with join fetch, entity graph, or DTO projection.

**Detailed answer:**
```java
@ManyToOne(fetch = FetchType.LAZY)
private Customer customer;
@OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
private List<OrderLine> lines;
```
EAGER can load huge object graphs and cause N+1 surprises.

**Common mistakes:**
- Leaving to-one associations EAGER by default.
- Returning entities directly from REST controllers.
- Using EAGER to hide LazyInitializationException.

**Follow-ups:**
- *Best default?* → Usually LAZY.
- *How fetch for one query?* → `JOIN FETCH`, `@EntityGraph`, or DTO.

### Q13. What is the N+1 problem? Show SQL and fixes.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** This is among the most common Hibernate performance bugs.

**Short answer (say this first, 20-40 sec):**
N+1 means one query loads parent rows and then one additional query runs per parent to load a relationship. For 100 orders, you may get 101 queries. Fix it with `JOIN FETCH`, `@EntityGraph`, `@BatchSize`, or DTO projection depending on pagination and payload needs.

**Detailed answer:**
SQL log:
```sql
select o.id, o.customer_id from orders o;
select c.id, c.name from customer c where c.id = ?;
select c.id, c.name from customer c where c.id = ?;
-- repeated N times
```
Fix with join fetch:
```java
@Query("select o from Order o join fetch o.customer")
List<Order> findAllWithCustomer();
```
Entity graph:
```java
@EntityGraph(attributePaths = "customer")
List<Order> findByStatus(OrderStatus status);
```
Batch size:
```java
@org.hibernate.annotations.BatchSize(size = 50)
@ManyToOne(fetch = FetchType.LAZY) Customer customer;
```
DTO projection:
```java
@Query("select new com.acme.OrderDto(o.id, c.name) from Order o join o.customer c")
List<OrderDto> listDtos();
```

**Common mistakes:**
- Fixing by making everything EAGER.
- Combining collection fetch join with pagination blindly.
- Not enabling SQL logs in investigation.

**Follow-ups:**
- *Best fix for API list?* → Often DTO projection.
- *When use `@BatchSize`?* → When lazy loading many proxies in batches is acceptable.

### Q14. What is `LazyInitializationException`?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Every Hibernate user hits it.

**Short answer (say this first, 20-40 sec):**
It occurs when code accesses a lazy association after the persistence context/session is closed. Hibernate needs the session to load the proxy, but it is gone. Fix by fetching required data inside the transaction and returning DTOs, not by making everything EAGER.

**Detailed answer:**
Bug:
```java
Order o = repo.findById(id).orElseThrow();
return o.getLines().size(); // outside transaction may throw
```
Fix:
```java
@Query("select o from Order o left join fetch o.lines where o.id = :id")
Optional<Order> findWithLines(Long id);
```
Or map to DTO inside a transactional service.

**Common mistakes:**
- Serializing entities in controllers.
- Enabling Open Session In View as a hidden fix.
- Changing all relationships to EAGER.

**Follow-ups:**
- *Best API pattern?* → Fetch and map DTO in service transaction.
- *Can OSIV hide it?* → Yes, but may cause queries during view rendering.

### Q15. What is Open Session In View?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** It is a common Spring Boot production setting.

**Short answer (say this first, 20-40 sec):**
Open Session In View keeps the Hibernate session open through web view/controller rendering, allowing lazy loading after the service transaction. It avoids LazyInitializationException but can cause hidden queries, N+1 in controllers, and DB work outside clear service boundaries. Many teams disable it and use DTOs.

**Detailed answer:**
```mermaid
flowchart TD
  A["HTTP request"] --> B["Open Hibernate session"]
  B --> C["Service transaction runs"]
  C --> D["Controller serializes response"]
  D --> E["Lazy association may query DB"]
  E --> F["Close session"]
```
Spring Boot property:
```properties
spring.jpa.open-in-view=false
```

**Common mistakes:**
- Letting JSON serialization trigger lazy queries.
- Treating OSIV as performance-neutral.
- Disabling OSIV without adding fetch plans.

**Follow-ups:**
- *Why disable it?* → Clear transaction boundaries and predictable SQL.
- *What replaces it?* → DTO projections or explicit fetch joins/entity graphs.

### Q16. First-level vs second-level cache?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** Cache scope matters for correctness and performance.

**Short answer (say this first, 20-40 sec):**
First-level cache is the persistence context and is always enabled; it guarantees one managed instance per entity identity in that context. Second-level cache is optional, shared across sessions, and must be configured with a provider. Query cache is separate and caches query results/IDs.

**Detailed answer:**
```java
User a = em.find(User.class, 1L);
User b = em.find(User.class, 1L);
System.out.println(a == b); // true in same persistence context
```
Second-level cache needs careful invalidation and is best for mostly-read reference data.

**Common mistakes:**
- Expecting first-level cache across transactions.
- Enabling second-level cache for highly mutable data.
- Confusing query cache with entity cache.

**Follow-ups:**
- *Is first-level optional?* → No, it is core JPA behavior.
- *When use L2 cache?* → Read-mostly data with clear invalidation tolerance.

### Q17. ID generation strategies and batch inserts?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** ID choice affects insert performance.

**Short answer (say this first, 20-40 sec):**
`IDENTITY` uses database auto-increment and usually requires immediate insert to get the ID, which can reduce batching. `SEQUENCE` can allocate IDs before insert and works better with batching, especially with allocation size. UUIDs avoid coordination but can hurt index locality if random.

**Detailed answer:**
```java
@Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq")
@SequenceGenerator(name = "product_seq", sequenceName = "product_seq", allocationSize = 50)
private Long id;
```
Batch config:
```properties
spring.jpa.properties.hibernate.jdbc.batch_size=50
spring.jpa.properties.hibernate.order_inserts=true
```

**Common mistakes:**
- Using `IDENTITY` and expecting efficient batching.
- Allocation size mismatch with DB sequence increment.
- Random UUID primary keys on write-heavy tables without considering index cost.

**Follow-ups:**
- *Best for PostgreSQL batching?* → Sequence with allocation size.
- *Does batching work with flush per row?* → No, frequent flush reduces batching.

### Q18. JPQL vs Criteria vs native SQL?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Query choice is everyday backend work.

**Short answer (say this first, 20-40 sec):**
JPQL queries entities and relationships using object names. Criteria builds type-safe dynamic queries in Java but is verbose. Native SQL uses database-specific SQL and is best for vendor features, complex queries, or performance-critical cases.

**Detailed answer:**
```java
@Query("select u from User u where u.email = :email")
Optional<User> findByEmail(String email);

@Query(value = "select * from users where email = :email", nativeQuery = true)
Optional<User> findNative(String email);
```

**Common mistakes:**
- Writing table names in JPQL instead of entity names.
- Using native SQL and expecting automatic portability.
- Building query strings by concatenating user input.

**Follow-ups:**
- *When Criteria?* → Many optional filters and dynamic predicates.
- *When native?* → Window functions, CTEs, database-specific locking.

### Q19. What are pagination pitfalls with fetch joins?
`🔴 Advanced` `🔥` `Must-know SDE-2`

**Why they ask:** This causes memory and correctness issues.

**Short answer (say this first, 20-40 sec):**
Paginating with a collection fetch join can duplicate parent rows and force Hibernate to paginate in memory or produce wrong results. Use two-step pagination: page parent IDs first, then fetch associations by IDs, or use DTO projections. To-one fetch joins are usually safer.

**Detailed answer:**
Risky:
```java
@Query("select p from Post p join fetch p.comments")
Page<Post> findPage(Pageable pageable);
```
Safer:
```java
@Query("select p.id from Post p where p.status=:s order by p.id")
Page<Long> pageIds(Status s, Pageable pageable);
@Query("select distinct p from Post p left join fetch p.comments where p.id in :ids")
List<Post> fetchByIds(List<Long> ids);
```

**Common mistakes:**
- Collection fetch join plus `Pageable` without checking logs.
- Ignoring duplicate parent rows.
- Missing deterministic ordering.

**Follow-ups:**
- *To-one fetch join with pagination?* → Usually okay.
- *Best API list approach?* → DTO projection with exact fields.

### Q20. Bulk updates bypass persistence context. Why does it matter?
`🔴 Advanced` `🔥` `Must-know SDE-2`

**Why they ask:** Bulk JPQL surprises many developers.

**Short answer (say this first, 20-40 sec):**
JPQL bulk update/delete executes directly in the database and bypasses managed entities, dirty checking, cascades, and entity callbacks. The persistence context may now contain stale entities. Clear or synchronize the persistence context after bulk operations.

**Detailed answer:**
```java
@Modifying(clearAutomatically = true, flushAutomatically = true)
@Query("update User u set u.active = false where u.lastLogin < :cutoff")
int deactivateOldUsers(Instant cutoff);
```
Without clearing, a previously loaded `User` may still show old `active` value.

**Common mistakes:**
- Expecting `@PreUpdate` callbacks.
- Keeping stale managed entities.
- Forgetting version columns may not be incremented unless explicitly updated.

**Follow-ups:**
- *Does bulk delete cascade?* → Not like entity remove cascades.
- *How fix stale context?* → Flush then clear or use `clearAutomatically`.
## Spring Transactions

### Q21. How does `@Transactional` work internally?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** Transaction bugs often come from proxy misunderstanding.

**Short answer (say this first, 20-40 sec):**
Spring usually applies transactions using AOP proxies. A call enters the proxy, the transaction interceptor opens or joins a transaction, invokes the target method, then commits or rolls back based on the outcome. Calls that bypass the proxy, such as self-invocation, do not trigger transaction advice.

**Detailed answer:**
```mermaid
flowchart TD
  A["Client bean calls service method"] --> B["Spring proxy"]
  B --> C["Transaction interceptor"]
  C --> D["Begin or join transaction"]
  D --> E["Invoke target method"]
  E --> F{"Exception requiring rollback?"}
  F -->|"yes"| G["Rollback"]
  F -->|"no"| H["Commit"]
```
`@Transactional` is effective on public methods called through the Spring proxy.

**Common mistakes:**
- Calling transactional method from another method in the same class.
- Expecting private methods to be transactional.
- Catching exceptions and hiding rollback conditions.

**Follow-ups:**
- *Proxy type?* → JDK dynamic proxy for interfaces or CGLIB subclass proxy for classes.
- *When transaction starts?* → When proxy intercepts the method call.

### Q22. What is the self-invocation pitfall?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** It is a very common Spring transaction bug.

**Short answer (say this first, 20-40 sec):**
Self-invocation means one method in a class calls another method in the same class. The call uses `this`, bypassing the Spring proxy, so `@Transactional` on the called method is not applied. Move the method to another bean or call through a proxied collaborator.

**Detailed answer:**
Bug:
```java
@Service
class OrderService {
  void outer(){ inner(); }
  @Transactional void inner(){ }
}
```
Fix:
```java
@Service class OrderWriter { @Transactional void inner(){ } }
@Service class OrderService { private final OrderWriter writer; OrderService(OrderWriter writer){this.writer=writer;} void outer(){ writer.inner(); } }
```

**Common mistakes:**
- Adding `@Transactional` and not checking call path.
- Making the method private.
- Assuming annotations work like language keywords.

**Follow-ups:**
- *Does AspectJ mode help?* → It can, but proxy mode is the common default.
- *Best fix?* → Refactor transactional boundary to another Spring bean.

### Q23. Do `@Transactional` private methods work?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** It checks proxy basics.

**Short answer (say this first, 20-40 sec):**
In standard Spring proxy-based AOP, private methods are not transactional because proxies cannot intercept private method calls. Put `@Transactional` on public service methods that are called from outside the bean through the proxy.

**Detailed answer:**
```java
@Service
class PaymentService {
  public void pay(){ savePayment(); }
  @Transactional private void savePayment(){ }
}
```
Make the transactional boundary public on a proxied bean.

**Common mistakes:**
- Annotating private helper methods.
- Calling transactional method from constructor.
- Testing with no assertion on transaction activity.

**Follow-ups:**
- *Protected/package methods?* → Proxy limitations still matter; public service methods are safest.
- *How verify?* → Enable transaction logs or check transaction active in a test.

### Q24. What are Spring rollback rules for checked exceptions?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Many candidates expect all exceptions to roll back.

**Short answer (say this first, 20-40 sec):**
By default, Spring rolls back on unchecked exceptions (`RuntimeException`) and `Error`, but not checked exceptions. For checked exceptions, configure `rollbackFor`. If you catch an exception and do not rethrow or mark rollback-only, Spring may commit.

**Detailed answer:**
```java
@Transactional(rollbackFor = IOException.class)
void importFile() throws IOException {
  repository.save(row);
  throw new IOException("bad file");
}
```
If catching, rethrow or call `TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()`.

**Common mistakes:**
- Expecting checked exceptions to roll back by default.
- Catching and swallowing runtime exceptions.
- Returning error objects while transaction commits.

**Follow-ups:**
- *Does `RuntimeException` roll back?* → Yes by default.
- *Does no exception mean commit?* → Usually yes, unless rollback-only is set.

### Q25. Explain transaction propagation levels with scenarios.
`🔴 Advanced` `🔥` `Must-know SDE-2`

**Why they ask:** Propagation affects nested service behavior.

**Short answer (say this first, 20-40 sec):**
Propagation defines how a method behaves when a transaction already exists. `REQUIRED` joins or creates, `REQUIRES_NEW` suspends the current and starts a new one, and `NESTED` uses a savepoint when supported. Choose based on whether work must commit independently or roll back together.

**Detailed answer:**
| Propagation | Behavior | Scenario |
|---|---|---|
| REQUIRED | Join existing or create | Normal service method |
| REQUIRES_NEW | Suspend existing, new transaction | Audit log commits independently |
| NESTED | Savepoint inside existing transaction | Partial rollback in batch |
| SUPPORTS | Join if exists, else none | Read helper |
| NOT_SUPPORTED | Suspend transaction | Long non-DB operation |
| MANDATORY | Fail if no transaction | Must be inside workflow |
| NEVER | Fail if transaction exists | Must not run in transaction |

```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
void writeAudit(String msg){ auditRepo.save(new Audit(msg)); }
```

**Common mistakes:**
- Using `REQUIRES_NEW` everywhere.
- Assuming `NESTED` always works.
- Forgetting self-invocation bypasses propagation.

**Follow-ups:**
- *Audit log propagation?* → Often `REQUIRES_NEW`.
- *Batch item rollback?* → `NESTED` if savepoints are supported.

### Q26. Explain database isolation levels.
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** Isolation controls read phenomena and performance.

**Short answer (say this first, 20-40 sec):**
Isolation defines how transactions see each other's changes. Read Uncommitted allows dirty reads, Read Committed prevents dirty reads, Repeatable Read prevents non-repeatable reads, and Serializable gives strongest isolation but can reduce concurrency. Actual behavior differs by database MVCC implementation.

**Detailed answer:**
| Isolation | Prevents | Notes |
|---|---|---|
| READ_UNCOMMITTED | Almost nothing | Dirty reads possible |
| READ_COMMITTED | Dirty reads | PostgreSQL default |
| REPEATABLE_READ | Dirty and non-repeatable reads | MySQL InnoDB default |
| SERIALIZABLE | Strongest anomalies | May abort and require retry |

```java
@Transactional(isolation = Isolation.SERIALIZABLE)
void criticalWorkflow(){ }
```

**Common mistakes:**
- Assuming all databases implement levels identically.
- Raising isolation instead of adding correct locks/constraints.
- Ignoring retry requirement under Serializable.

**Follow-ups:**
- *PostgreSQL default?* → Read Committed.
- *MySQL InnoDB default?* → Repeatable Read.

### Q27. What does `@Transactional(readOnly = true)` do?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** It is common but often overstated.

**Short answer (say this first, 20-40 sec):**
`readOnly=true` is a hint to Spring, Hibernate, and sometimes the database that the transaction is read-only. Hibernate may optimize dirty checking/flush behavior. It is not a universal security guarantee; some databases may still allow writes.

**Detailed answer:**
```java
@Transactional(readOnly = true)
public List<UserDto> listUsers(){ return userRepo.findDtos(); }
```
Use for query service methods. Do not mutate managed entities inside read-only transactions.

**Common mistakes:**
- Thinking it always prevents writes.
- Marking write methods read-only accidentally.
- Expecting huge performance gains for every query.

**Follow-ups:**
- *Should reads be transactional?* → Often yes for consistent session/fetch behavior.
- *Does it change isolation?* → No, isolation is separate.

### Q28. Why is calling an external API inside a transaction an anti-pattern?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** It causes lock waits and inconsistent side effects.

**Short answer (say this first, 20-40 sec):**
External calls inside DB transactions keep locks and connections open while waiting on an unreliable network. If the transaction rolls back after the external call succeeds, side effects are inconsistent. Prefer commit first then publish an event/outbox, or use idempotency and compensation.

**Detailed answer:**
Bad:
```java
@Transactional
void placeOrder(){ orderRepo.save(order); paymentClient.charge(); inventoryRepo.decrement(); }
```
Better: persist order and outbox event in one transaction, commit, then async worker calls payment API idempotently.

**Common mistakes:**
- Holding row locks during HTTP calls.
- No timeout or idempotency key for external API.
- Assuming rollback undoes external side effects.

**Follow-ups:**
- *Pattern to use?* → Transactional outbox.
- *Payment API requirement?* → Idempotency key.

### Q29. What are transaction timeouts?
`🟡 Intermediate` `Stretch`

**Why they ask:** Long transactions hurt DB health.

**Short answer (say this first, 20-40 sec):**
A transaction timeout limits how long a transaction may run before Spring marks it rollback-only or the database cancels work. It protects connections and locks from being held indefinitely. It should be paired with query and HTTP timeouts.

**Detailed answer:**
```java
@Transactional(timeout = 5)
void updateInventory(){ }
```
Timeout behavior depends on transaction manager and database driver support.

**Common mistakes:**
- Relying only on transaction timeout while queries have no timeout.
- Setting very high timeouts to hide slowness.
- Ignoring lock wait timeout at DB level.

**Follow-ups:**
- *Does timeout commit partial work?* → No, transaction should roll back.
- *Where else set timeout?* → JDBC query, connection pool, HTTP clients.

### Q30. What is `TransactionRequiredException`?
`🟢 Beginner` `Stretch`

**Why they ask:** It exposes missing transaction boundaries.

**Short answer (say this first, 20-40 sec):**
`TransactionRequiredException` occurs when an operation requiring a transaction runs without one, such as modifying queries or pessimistic locks. Add a transactional service boundary and call it through the Spring proxy.

**Detailed answer:**
```java
@Modifying
@Query("update User u set u.active=false where u.id=:id")
int deactivate(Long id);
```
Service:
```java
@Transactional
void deactivateUser(Long id){ repo.deactivate(id); }
```

**Common mistakes:**
- Putting transaction boundary in the wrong place.
- Self-invocation bypass.
- Running write query in a read-only transaction.

**Follow-ups:**
- *Where should transaction boundary be?* → Service layer.
- *Do all repository methods need explicit transaction?* → Some CRUD methods have defaults, but service boundaries are clearer.

## Locking, Isolation, and Production Scenarios

### Q31. What is optimistic locking with `@Version`?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** It prevents lost updates without blocking reads.

**Short answer (say this first, 20-40 sec):**
Optimistic locking adds a version column to the update predicate. If another transaction changed the row, the version no longer matches and Hibernate throws an optimistic lock exception. The application can retry or show a conflict.

**Detailed answer:**
```java
@Entity class Document { @Id Long id; @Version long version; String content; }
```
SQL shape:
```sql
update document set content=?, version=? where id=? and version=?;
```
Affected rows zero means conflict.

**Common mistakes:**
- Missing version on concurrently edited rows.
- Infinite retries.
- Retrying non-idempotent side effects.

**Follow-ups:**
- *Does it block?* → No, conflict is detected at update/flush.
- *Best for high contention?* → Pessimistic lock or atomic SQL may be better.

### Q32. What is pessimistic locking in JPA?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** Some workflows need row-level serialization.

**Short answer (say this first, 20-40 sec):**
Pessimistic locking asks the database to lock rows, preventing other transactions from updating them until commit or rollback. In JPA use `LockModeType.PESSIMISTIC_WRITE` or lock query methods. It is useful for high-contention critical rows but can cause lock waits and deadlocks.

**Detailed answer:**
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select a from Account a where a.id = :id")
Account lockAccount(Long id);
```
SQL often becomes `select ... for update` depending on database.

**Common mistakes:**
- Holding lock during external API call.
- No lock timeout.
- Locking rows in inconsistent order.

**Follow-ups:**
- *What exception on timeout?* → Provider-specific wrapped in a Spring data access exception.
- *Read lock?* → `PESSIMISTIC_READ`, database behavior varies.

### Q33. What is `SKIP LOCKED` and a job queue use case?
`🔴 Advanced` `🔥` `Stretch`

**Why they ask:** It is a practical DB concurrency pattern.

**Short answer (say this first, 20-40 sec):**
`SKIP LOCKED` makes a transaction skip rows already locked by another transaction instead of waiting. Multiple workers can claim different jobs concurrently. It is useful for DB-backed queues but must be paired with status updates and retry handling.

**Detailed answer:**
```sql
-- PostgreSQL
BEGIN;
SELECT id FROM job WHERE status = 'READY' ORDER BY id FOR UPDATE SKIP LOCKED LIMIT 10;
UPDATE job SET status = 'RUNNING' WHERE id IN (...);
COMMIT;
```

**Common mistakes:**
- Not marking claimed rows in the same transaction.
- No retry for stuck RUNNING jobs.
- Assuming global fairness.

**Follow-ups:**
- *What problem does it avoid?* → Workers waiting on locked rows.
- *Can rows starve?* → Yes, design retry and ordering carefully.

### Q34. How should entities implement equals and hashCode?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** Bad equality breaks sets and Hibernate proxies.

**Short answer (say this first, 20-40 sec):**
Use stable business keys when available. If using generated IDs, be careful because ID is null before persist; changing hashCode after adding to a `HashSet` breaks lookup. Avoid Lombok `@Data` on entities because it includes relationships and can trigger lazy loading or recursion.

**Detailed answer:**
```java
@Entity
class User {
  @Id @GeneratedValue Long id;
  @Column(nullable=false, unique=true) String email;
  public boolean equals(Object o){ return o instanceof User other && email.equals(other.email); }
  public int hashCode(){ return email.hashCode(); }
}
```

**Common mistakes:**
- Including lazy collections in equals/hashCode.
- HashCode changes after ID assignment.
- `@Data` causing recursive `toString`.

**Follow-ups:**
- *Can proxies affect equals?* → Yes, class checks must account for proxy behavior.
- *Best unique business key?* → Immutable natural key like email if guaranteed.

### Q35. What Hibernate performance tuning knobs do you know?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** They want production tuning awareness.

**Short answer (say this first, 20-40 sec):**
Tune fetch plans, use DTO projections, batch inserts/updates, batch lazy loading, proper indexes, pagination, and avoid unnecessary flushes. Measure SQL count, query plans, p95 latency, and DB CPU. Do not tune blindly.

**Detailed answer:**
```properties
spring.jpa.properties.hibernate.jdbc.batch_size=50
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.default_batch_fetch_size=50
spring.jpa.properties.hibernate.generate_statistics=true
```
Techniques: `JOIN FETCH`, `@EntityGraph`, `@BatchSize`, DTO projections, database indexes, and connection pool sizing.

**Common mistakes:**
- Fixing N+1 with EAGER everywhere.
- Missing DB indexes.
- Overusing second-level cache for mutable data.

**Follow-ups:**
- *First thing to inspect?* → SQL logs and execution plan.
- *Best for read-only list API?* → DTO projection.
### Q36. What is connection pool sizing with JPA?
`🟡 Intermediate` `Stretch`

**Why they ask:** Thread pools and DB pools must align.

**Short answer (say this first, 20-40 sec):**
The DB connection pool limits concurrent database work. If app threads greatly exceed DB connections, many threads wait and latency rises. Size based on database capacity, query time, throughput goals, and load tests, not just CPU count.

**Detailed answer:**
```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.connection-timeout=30000
```
Watch active connections, pending threads, acquisition time, query latency, and DB CPU.

**Common mistakes:**
- Setting pool to hundreds without DB capacity.
- One transaction per slow external call holding a connection.
- No metric for connection acquisition time.

**Follow-ups:**
- *Symptom of small pool?* → Threads waiting for connections.
- *Symptom of huge pool?* → DB saturation and context overhead.

### Q37. How do indexes relate to JPA performance?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** ORM does not remove database fundamentals.

**Short answer (say this first, 20-40 sec):**
JPA generates SQL, and the database still needs indexes for filters, joins, ordering, and unique constraints. Add indexes based on query patterns and verify with execution plans. Too many indexes slow writes.

**Detailed answer:**
```java
@Table(name="orders", indexes = @Index(name="idx_orders_customer_status", columnList="customer_id,status"))
@Entity class Order { @Id Long id; }
```
For performance stories, mention execution plan, scans, join columns, and measured before/after numbers only when real.

**Common mistakes:**
- Indexing every column.
- Missing FK indexes used in joins.
- Ignoring composite index order.

**Follow-ups:**
- *How verify index use?* → `EXPLAIN` / execution plan.
- *Can index hurt?* → Yes, extra write cost and storage.

### Q38. How do DTO projections improve performance?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** APIs rarely need full entity graphs.

**Short answer (say this first, 20-40 sec):**
DTO projections fetch only required columns and avoid loading managed entity graphs. They reduce memory, SQL result size, serialization risk, and LazyInitializationException. They are ideal for read-only API list screens.

**Detailed answer:**
```java
record OrderSummary(Long id, String customerName) {}
@Query("select new com.acme.OrderSummary(o.id, c.name) from Order o join o.customer c")
List<OrderSummary> summaries();
```

**Common mistakes:**
- Returning entities from controllers.
- Fetching full aggregates for list pages.
- Updating DTOs expecting dirty checking.

**Follow-ups:**
- *Are DTOs managed?* → No.
- *Best for write operations?* → Load managed entity and apply changes.

### Q39. Entity graph vs join fetch?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** Both solve fetch planning but differently.

**Short answer (say this first, 20-40 sec):**
`JOIN FETCH` is written directly in JPQL for a specific query. `@EntityGraph` declares fetch paths separately and can be attached to repository methods. Both reduce lazy queries; DTO projections may still be better for read-only APIs.

**Detailed answer:**
```java
@Query("select o from Order o join fetch o.customer where o.id=:id")
Optional<Order> findWithCustomer(Long id);

@EntityGraph(attributePaths = {"customer", "lines"})
Optional<Order> findById(Long id);
```

**Common mistakes:**
- Fetching too many collections at once.
- Expecting entity graph to change every query automatically.
- Using collection fetch joins with pagination carelessly.

**Follow-ups:**
- *Which is more explicit?* → JPQL join fetch.
- *Which is declarative?* → Entity graph.

### Q40. How do you handle `@ManyToMany` safely?
`🟡 Intermediate` `Stretch`

**Why they ask:** Cascading deletes with many-to-many can be dangerous.

**Short answer (say this first, 20-40 sec):**
Avoid `CascadeType.REMOVE` on many-to-many because the other side may be shared. Often model the join table as an entity if it has attributes or lifecycle. Use explicit add/remove helper methods and delete join rows, not shared target rows.

**Detailed answer:**
```java
@Entity class Student { @ManyToMany Set<Course> courses = new HashSet<>(); }
@Entity class Course { @ManyToMany(mappedBy="courses") Set<Student> students = new HashSet<>(); }
```
If enrollment has grade/date, create `Enrollment` with two many-to-one relationships.

**Common mistakes:**
- `cascade = CascadeType.ALL` on many-to-many.
- No unique constraint on join table.
- Returning many-to-many graphs directly.

**Follow-ups:**
- *When join entity?* → When relationship has attributes or lifecycle.
- *Should deleting student delete course?* → Usually no.

### Q41. `remove` vs JPQL bulk delete?
`🔴 Advanced` `Stretch`

**Why they ask:** Cascades and callbacks differ.

**Short answer (say this first, 20-40 sec):**
`entityManager.remove(entity)` operates on managed entities and triggers cascades, orphan removal, and callbacks. JPQL bulk delete executes directly in the database, bypassing the persistence context and entity lifecycle callbacks. Bulk delete is faster but less object-aware.

**Detailed answer:**
```java
em.remove(order); // cascades according to mapping
```
```java
@Modifying(clearAutomatically = true)
@Query("delete from Order o where o.createdAt < :cutoff")
int deleteOld(Instant cutoff);
```

**Common mistakes:**
- Expecting orphan removal in bulk delete.
- Not clearing stale persistence context.
- Bulk deleting parent rows before child constraints.

**Follow-ups:**
- *Which is faster for many rows?* → Bulk delete.
- *Which respects callbacks?* → `remove`.

### Q42. What is a transactional outbox?
`🔴 Advanced` `🔥` `Stretch`

**Why they ask:** It solves DB plus message/API consistency.

**Short answer (say this first, 20-40 sec):**
Transactional outbox stores a business change and an event record in the same DB transaction. A separate worker publishes the event and marks it sent. This avoids losing events when the app crashes after commit or publishing events for rolled-back transactions.

**Detailed answer:**
```mermaid
sequenceDiagram
  participant API as Service
  participant DB as Database
  participant W as OutboxWorker
  participant MQ as MessageBroker
  API->>DB: Save order and outbox row in one transaction
  DB-->>API: Commit
  W->>DB: Read unsent outbox rows
  W->>MQ: Publish event
  W->>DB: Mark sent
```

**Common mistakes:**
- Publishing message before DB commit.
- No idempotent consumer.
- No retry or dead-letter strategy.

**Follow-ups:**
- *Does outbox need polling?* → Polling or CDC are common.
- *Do consumers need idempotency?* → Yes.

### Q43. How do you structure service and repository layers for transactions?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Correct boundaries prevent many bugs.

**Short answer (say this first, 20-40 sec):**
Put transaction boundaries at the service layer, where a business use case starts and ends. Repositories should focus on data access. This lets one transaction cover multiple repository calls and keeps controllers thin.

**Detailed answer:**
```java
@Service
class OrderService {
  @Transactional
  void placeOrder(CreateOrderCommand cmd){ orderRepo.save(order); inventoryRepo.decrement(cmd.sku()); }
}
```
The controller calls the service; repository methods are implementation details.

**Common mistakes:**
- Transaction per repository call for one workflow.
- Business logic in controllers.
- Self-invocation in service methods.

**Follow-ups:**
- *Read-only query boundary?* → Service method with `@Transactional(readOnly=true)`.
- *Multiple DBs?* → Requires careful transaction manager design.

### Q44. What SQL logs should you enable while debugging Hibernate?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** SQL visibility is essential.

**Short answer (say this first, 20-40 sec):**
Enable SQL statements, bind parameter logs, and Hibernate statistics in lower environments. Count queries per request and inspect slow SQL with execution plans. Avoid verbose bind logs in production if they may expose sensitive data.

**Detailed answer:**
```properties
spring.jpa.show-sql=false
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.orm.jdbc.bind=TRACE
spring.jpa.properties.hibernate.generate_statistics=true
```
Use datasource proxy or p6spy for request-level query counting.

**Common mistakes:**
- Debugging N+1 without SQL logs.
- Logging secrets in production.
- Looking at ORM code but not DB execution plan.

**Follow-ups:**
- *What confirms N+1?* → One parent query plus repeated child queries.
- *What confirms index issue?* → Execution plan with scan/high cost.

### Q45. How do you map enums?
`🟢 Beginner` `Stretch`

**Why they ask:** Enum persistence mistakes cause data corruption.

**Short answer (say this first, 20-40 sec):**
Use `@Enumerated(EnumType.STRING)` for readability and safety against enum reordering. Avoid `ORDINAL` unless you fully control ordering forever. For custom database values, use an `AttributeConverter`.

**Detailed answer:**
```java
@Enumerated(EnumType.STRING)
private OrderStatus status;
```
Custom converter:
```java
@Converter(autoApply = true)
class StatusConverter implements AttributeConverter<OrderStatus,String> {
  public String convertToDatabaseColumn(OrderStatus s){ return s.name(); }
  public OrderStatus convertToEntityAttribute(String v){ return OrderStatus.valueOf(v); }
}
```

**Common mistakes:**
- Using ordinal and then reordering enum constants.
- Renaming enum without data migration.
- Missing DB check constraints for allowed values.

**Follow-ups:**
- *Why string?* → Stable against reordering and readable.
- *Downside?* → More storage and rename migrations.

### Q46. Production scenario: 101 queries for 100 rows. What do you do?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** This is the classic N+1 production scenario.

**Short answer (say this first, 20-40 sec):**
First confirm with SQL logs or query count metrics. Identify the lazy association causing one query per row. Fix with DTO projection, join fetch, entity graph, or batch fetching, then verify query count and response time.

**Detailed answer:**
```mermaid
flowchart TD
  A["API list endpoint slow"] --> B["Enable SQL/query count"]
  B --> C["Find repeated child query"]
  C --> D{"Need full entity graph?"}
  D -->|"no"| E["Use DTO projection"]
  D -->|"yes"| F["Use join fetch or entity graph"]
  F --> G["Verify query count"]
  E --> G
```
For 100 rows, target one query or a small fixed number, not 101.

**Common mistakes:**
- Making association EAGER globally.
- Breaking pagination with collection fetch join.
- Not adding regression query-count tests.

**Follow-ups:**
- *Best for list pages?* → DTO projection.
- *When batch size?* → Multiple lazy proxies where fetch join is unsuitable.

### Q47. Production scenario: `@Transactional` did not roll back. Why?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** Rollback surprises are common.

**Short answer (say this first, 20-40 sec):**
Common causes are checked exception without `rollbackFor`, exception caught and swallowed, self-invocation bypassing proxy, private method annotation, method not called from Spring bean, or wrong transaction manager. Check logs for transaction creation and whether rollback-only was set.

**Detailed answer:**
Bug:
```java
@Transactional
void importData() throws IOException { repo.save(row); throw new IOException(); }
```
Fix:
```java
@Transactional(rollbackFor = IOException.class)
void importData() throws IOException { repo.save(row); throw new IOException(); }
```

**Common mistakes:**
- Catching exception and returning error response.
- Same-class method calls.
- Assuming checked exceptions roll back.

**Follow-ups:**
- *How debug?* → Enable `org.springframework.transaction` DEBUG logs.
- *How force rollback after catch?* → Rethrow or mark rollback-only.

### Q48. Production scenario: `LazyInitializationException` in controller.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** It reveals poor API boundaries.

**Short answer (say this first, 20-40 sec):**
The controller is accessing a lazy association after the transaction/session ended. Fetch required data in the service transaction and return a DTO. Do not solve it by returning entities, enabling OSIV blindly, or making all associations EAGER.

**Detailed answer:**
```java
@Transactional(readOnly = true)
OrderDto getOrder(Long id){ Order o = repo.findWithLines(id).orElseThrow(); return mapper.toDto(o); }
```
Controller returns `OrderDto`, not `Order`.

**Common mistakes:**
- JSON serialization of entity graphs.
- EAGER fetch everywhere.
- Ignoring transaction boundary.

**Follow-ups:**
- *Can OSIV fix symptom?* → Yes, but may hide N+1 and DB work in web layer.
- *Best long-term fix?* → DTO and explicit fetch plan.

### Q49. Production scenario: deadlock under load.
`🔴 Advanced` `🔥` `Must-know SDE-2`

**Why they ask:** DB deadlocks happen in high-concurrency apps.

**Short answer (say this first, 20-40 sec):**
Collect DB deadlock logs and application stack traces, identify transactions and lock order, then make all code update rows in a consistent order. Keep transactions short, add proper indexes, and retry deadlock victims because databases may abort one transaction.

**Detailed answer:**
Bad pattern: transaction A updates account 1 then 2; transaction B updates 2 then 1. Fix by sorting IDs and locking in ascending order.
```java
List<Long> ids = Stream.of(fromId, toId).sorted().toList();
List<Account> locked = repo.lockAllByIdInOrder(ids);
```
```sql
select * from account where id in (?, ?) order by id for update;
```

**Common mistakes:**
- Long transactions with external calls.
- Missing indexes causing broad lock scans.
- No retry for deadlock loser.

**Follow-ups:**
- *Can isolation increase deadlocks?* → Broader locks can increase risk.
- *Should you retry?* → Yes, bounded retry if operation is idempotent.

### Q50. Production scenario: lost update.
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** It is a core transaction correctness bug.

**Short answer (say this first, 20-40 sec):**
Lost update happens when two transactions read the same row, compute new values, and one overwrites the other's update. Fix with optimistic `@Version`, pessimistic locks, or atomic SQL updates. Validate by running two concurrent transactions in a test.

**Detailed answer:**
Bug SQL shape:
```sql
-- both read stock = 10
update product set stock = 9 where id = 1;
update product set stock = 9 where id = 1;
```
Fixes:
```sql
update product set stock = 9, version = version + 1 where id = 1 and version = 5;
update product set stock = stock - 1 where id = 1 and stock > 0;
```

**Common mistakes:**
- Calculating new value in Java without version/lock.
- No check on affected row count.
- Assuming local synchronization protects DB rows across pods.

**Follow-ups:**
- *Best for inventory decrement?* → Atomic conditional update or pessimistic lock under high contention.
- *Best for profile edit?* → Optimistic version conflict.

## Revision Cheat Sheet

- JPA is a spec; Hibernate is an implementation; Spring Data JPA is repository abstraction.
- Managed entities are dirty-checked; detached entities are not.
- Flush sends SQL but does not commit.
- `persist` makes new entity managed; `merge` returns a managed copy.
- `saveAndFlush` forces flush, not commit.
- Owning side controls FK updates; `mappedBy` marks inverse side.
- Defaults: to-one EAGER, to-many LAZY; prefer explicit LAZY and fetch plans.
- N+1 is one parent query plus N child queries; fix with DTO, join fetch, entity graph, or batch size.
- LazyInitializationException means lazy proxy accessed after session close.
- OSIV hides lazy errors but can cause controller-level queries.
- First-level cache is persistence context; second-level cache is optional and shared.
- `IDENTITY` can reduce insert batching; sequences with allocation size are batch-friendly.
- Bulk JPQL bypasses persistence context, cascades, and callbacks.
- `@Transactional` works through proxies; self-invocation/private methods do not apply in proxy mode.
- Default rollback: runtime exceptions and errors; checked exceptions need `rollbackFor`.
- `REQUIRED` joins/creates; `REQUIRES_NEW` suspends and starts new; `NESTED` uses savepoints.
- Optimistic locking uses `@Version`; pessimistic locking uses DB row locks.
- Do not call external APIs while holding DB transactions; use outbox/idempotency.
- Entity equals/hashCode should avoid lazy relationships and unstable generated IDs.
- Production debugging starts with SQL logs, query counts, execution plans, transaction logs, and DB lock diagnostics.

## Flashcards

- Q → JPA vs Hibernate? A → JPA is spec; Hibernate implements it.
- Q → Spring Data JPA? A → Repository abstraction over JPA.
- Q → Managed entity update needs save? A → No, dirty checking flushes changes.
- Q → Flush vs commit? A → Flush sends SQL; commit finalizes transaction.
- Q → `merge` return value? A → Managed copy; original remains detached.
- Q → Default to-one fetch? A → EAGER.
- Q → Default to-many fetch? A → LAZY.
- Q → N+1 fix for list API? A → DTO projection or explicit fetch plan.
- Q → LazyInitializationException cause? A → Lazy association accessed after session closed.
- Q → Default Spring rollback? A → RuntimeException and Error.
- Q → Checked exception rollback? A → Add `rollbackFor` or rethrow runtime exception.
- Q → Self-invocation issue? A → It bypasses transactional proxy.
- Q → Optimistic lock annotation? A → `@Version`.
- Q → Pessimistic write SQL usually? A → `SELECT ... FOR UPDATE`.
- Q → Orphan removal trigger? A → Removing child from parent collection.