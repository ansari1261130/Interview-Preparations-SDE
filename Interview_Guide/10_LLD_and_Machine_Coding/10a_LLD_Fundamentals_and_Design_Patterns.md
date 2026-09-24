# 10a · LLD Fundamentals and Design Patterns

A compact, interview-ready Java 17 guide for object-oriented design, SOLID, GoF patterns, and machine-coding strategy. Read the short answer first, then draw, code, and defend trade-offs.

**Table of Contents**
- [Core OOD Fundamentals](#core-ood-fundamentals)
- [SOLID and Design Principles](#solid-and-design-principles)
- [GoF and Enterprise Patterns](#gof-and-enterprise-patterns)
- [Machine Coding Round Strategy](#machine-coding-round-strategy)
- [Revision Cheat Sheet](#revision-cheat-sheet)
- [Flashcards](#flashcards)

🟢 Beginner · 🟡 Intermediate · 🔴 Advanced · ⚫ FAANG · 🔥 = very frequently asked

## Core OOD Fundamentals

### Q1. How do you apply the four OOP pillars in an LLD problem?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** They check whether you can convert requirements into maintainable classes, not just recite definitions.

**Short answer (say this first, 20-40 sec):**
Encapsulation protects invariants, abstraction exposes only required behavior, inheritance models true is-a reuse, and polymorphism lets clients depend on contracts. Start from use cases, identify entities/services, keep state private, and introduce interfaces where variation is expected.

**Detailed answer:**
| Pillar | Interview use | Java cue |
|---|---|---|
| Encapsulation | protect balance, spot count, state transitions | private fields + validation |
| Abstraction | hide payment, storage, matching, rate limit algorithm | interface boundary |
| Inheritance | stable is-a taxonomy only | Car extends Vehicle |
| Polymorphism | swap pricing/notification/scheduling | Strategy.calculate() |

```mermaid
classDiagram
class PaymentGateway { <<interface>> +charge(Money) Receipt }
class StripeGateway
class RazorpayGateway
PaymentGateway <|.. StripeGateway
PaymentGateway <|.. RazorpayGateway
CheckoutService --> PaymentGateway
```
Resume hook: frame your Spring Boot/RAG projects as controllers depending on services/repositories, not concrete DB or LLM details.

**Common mistakes:**
- Creating one god class.
- Exposing mutable collections directly.
- Using inheritance only for code reuse.

**Follow-ups:**
- *When avoid inheritance?* → When behavior varies independently.
- *Where is polymorphism useful?* → Pricing, rate limiting, scheduling, payment modes.

### Q2. Why prefer composition over inheritance?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Deep inheritance hierarchies make machine-coding designs rigid.

**Short answer (say this first, 20-40 sec):**
Composition gives behavior by holding collaborators; inheritance gives behavior from a parent. Prefer composition when behavior changes independently.

**Detailed answer:**
```java
// Bad
class Bird { void fly() {} }
class Penguin extends Bird { @Override void fly(){ throw new UnsupportedOperationException(); } }
// Good
interface Movement { void move(); }
final class Flying implements Movement { public void move(){ System.out.println("fly"); } }
final class Bird2 {
    private final Movement movement;

    Bird2(Movement movement) {
        this.movement = movement;
    }

    void move() {
        movement.move();
    }
}
```
Use inheritance for stable taxonomy; use composition for strategies, policies, and capabilities.

**Common mistakes:**
- Overriding methods to throw exceptions.
- Deep Vehicle to Car to ElectricCar trees.
- Not naming collaborators by role.

**Follow-ups:**
- *Example?* → Parking fee policy composed into ParkingLot.
- *JDK?* → Collections use Comparator composition.

### Q3. Interface vs abstract class: how do you choose?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** They want practical Java design judgment.

**Short answer (say this first, 20-40 sec):**
Use an interface for a capability or boundary with multiple implementations. Use an abstract class when closely related classes share state, protected helpers, or a fixed template algorithm.

**Detailed answer:**
```java
interface Sink { void write(String msg); }
abstract class AbstractLogger {
  protected final Sink sink;
  protected AbstractLogger(Sink sink){ this.sink = sink; }
  public final void log(String msg){ if (enabled(msg)) sink.write(format(msg)); }
  protected abstract boolean enabled(String msg);
  protected String format(String msg){ return java.time.Instant.now() + " " + msg; }
}
```
Java 8 added default interface methods; Java 9 added private helpers. Still avoid stateful fat interfaces.

**Common mistakes:**
- Using abstract classes only to avoid duplication.
- Forgetting Java single class inheritance.
- Making interfaces too broad.

**Follow-ups:**
- *Testing impact?* → Interfaces are easier to fake.
- *Can abstract class implement interface?* → Yes.

## SOLID and Design Principles

### Q4. Explain Single Responsibility Principle with bad to good Java code.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** SOLID quickly reveals whether your design survives extensions.

**Short answer (say this first, 20-40 sec):**
A class should have one reason to change. Split orchestration, persistence, formatting, and policy.

**Detailed answer:**
```java
// Bad
class InvoiceService {
    void save(Invoice invoice) {}

    String print(Invoice invoice) {
        return invoice.toString();
    }

    double tax(Invoice invoice) {
        return invoice.total() * 0.18;
    }
}
// Good
class InvoiceRepository { void save(Invoice i){} }
class InvoicePrinter { String print(Invoice i){ return i.toString(); } }
class TaxCalculator { double tax(Invoice i){ return i.total()*0.18; } }
```
In interviews, name the likely future change: new vehicle type, sink, payment mode, or repository.

**Common mistakes:**
- Quoting the principle without code.
- Overengineering every small class.
- Depending on concrete classes inside services.

**Follow-ups:**
- *Is it mandatory everywhere?* → No; apply where change/testing pressure exists.
- *Spring help?* → DI injects abstractions and lets tests replace beans.

### Q5. Explain Open Closed Principle with bad to good Java code.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** SOLID quickly reveals whether your design survives extensions.

**Short answer (say this first, 20-40 sec):**
Code should be open for extension and closed for modification. Add variants through interfaces, strategies, or registration.

**Detailed answer:**
```java
// Bad
class Fee { int fee(String type){ return type.equals("CAR") ? 40 : 20; } }
// Good
interface FeePolicy { int fee(); }
class CarFee implements FeePolicy { public int fee(){ return 40; } }
class BikeFee implements FeePolicy { public int fee(){ return 20; } }
```
In interviews, name the likely future change: new vehicle type, sink, payment mode, or repository.

**Common mistakes:**
- Quoting the principle without code.
- Overengineering every small class.
- Depending on concrete classes inside services.

**Follow-ups:**
- *Is it mandatory everywhere?* → No; apply where change/testing pressure exists.
- *Spring help?* → DI injects abstractions and lets tests replace beans.

### Q6. Explain Liskov Substitution Principle with bad to good Java code.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** SOLID quickly reveals whether your design survives extensions.

**Short answer (say this first, 20-40 sec):**
A subtype must be usable wherever its parent is expected without weakening behavior or surprising callers.

**Detailed answer:**
```java
// Bad
class Rectangle { int w,h; void setW(int w){this.w=w;} void setH(int h){this.h=h;} }
class Square extends Rectangle { @Override void setW(int w){this.w=this.h=w;} }
// Good
interface Shape { int area(); }
record Rect(int w, int h) implements Shape { public int area(){ return w*h; } }
record Square2(int side) implements Shape { public int area(){ return side*side; } }
```
In interviews, name the likely future change: new vehicle type, sink, payment mode, or repository.

**Common mistakes:**
- Quoting the principle without code.
- Overengineering every small class.
- Depending on concrete classes inside services.

**Follow-ups:**
- *Is it mandatory everywhere?* → No; apply where change/testing pressure exists.
- *Spring help?* → DI injects abstractions and lets tests replace beans.

### Q7. Explain Interface Segregation Principle with bad to good Java code.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** SOLID quickly reveals whether your design survives extensions.

**Short answer (say this first, 20-40 sec):**
Clients should not depend on methods they do not use. Prefer small role-specific interfaces.

**Detailed answer:**
```java
// Bad
interface Machine { void print(); void scan(); void fax(); }
// Good
interface Printer { void print(); } interface Scanner { void scan(); }
class SimplePrinter implements Printer { public void print(){} }
```
In interviews, name the likely future change: new vehicle type, sink, payment mode, or repository.

**Common mistakes:**
- Quoting the principle without code.
- Overengineering every small class.
- Depending on concrete classes inside services.

**Follow-ups:**
- *Is it mandatory everywhere?* → No; apply where change/testing pressure exists.
- *Spring help?* → DI injects abstractions and lets tests replace beans.

### Q8. Explain Dependency Inversion Principle with bad to good Java code.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** SOLID quickly reveals whether your design survives extensions.

**Short answer (say this first, 20-40 sec):**
High-level modules should depend on abstractions, not low-level concretions. Pass dependencies through constructors.

**Detailed answer:**
```java
// Bad
class OrderService { private final MySqlOrderRepo repo = new MySqlOrderRepo(); }
// Good
interface OrderRepository { void save(Order o); }
class OrderService2 {
    private final OrderRepository repo;

    OrderService2(OrderRepository repo) {
        this.repo = repo;
    }
}
```
In interviews, name the likely future change: new vehicle type, sink, payment mode, or repository.

**Common mistakes:**
- Quoting the principle without code.
- Overengineering every small class.
- Depending on concrete classes inside services.

**Follow-ups:**
- *Is it mandatory everywhere?* → No; apply where change/testing pressure exists.
- *Spring help?* → DI injects abstractions and lets tests replace beans.

### Q9. How do DRY, KISS, YAGNI, Law of Demeter, cohesion, and coupling guide LLD?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** These show practical judgment under time pressure.

**Short answer (say this first, 20-40 sec):**
Keep code simple, avoid premature abstractions, remove duplicated knowledge, and avoid long object chains. High cohesion means one focused class; low coupling means classes know only required collaborators.

**Detailed answer:**
| Principle | Interview sentence |
|---|---|
| DRY | duplicate knowledge is worse than repeated lines |
| KISS | simple code beats clever generic engines |
| YAGNI | do not build DB/UI/distributed locks unless asked |
| Law of Demeter | talk to direct collaborators only |

```java
// Bad train wreck
String city = order.customer().address().city().name();
// Better intent
boolean ok = order.canDeliverTo("Lucknow");
```

**Common mistakes:**
- Utility-class dumping.
- Overengineering.
- Refactoring before understanding variation.

**Follow-ups:**
- *DRY vs readability?* → Prefer readable duplication until abstraction is stable.
- *Cohesion smell?* → Vague Manager classes.

### Q10. Draw UML relationships used in class diagrams.
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** LLD interviews require quick, correct diagrams before coding.

**Short answer (say this first, 20-40 sec):**
Association is knows, aggregation is weak whole-part, composition is strong owned lifecycle, inheritance is is-a, and dependency is temporary use.

**Detailed answer:**
```mermaid
classDiagram
class ParkingLot
class Floor
class Spot
class Vehicle
class Ticket
class PaymentService
class FeePolicy
ParkingLot "1" o-- "many" Floor : aggregation
Floor "1" *-- "many" Spot : composition
Spot --> Vehicle : association
Vehicle <|-- Car : inheritance
PaymentService ..> FeePolicy : dependency
Ticket --> Spot : association
```
Use composition when deleting parent deletes child; use aggregation when part can outlive or be shared. Mermaid generics use List~Spot~.

**Common mistakes:**
- Drawing composition for every relation.
- Missing multiplicity.
- Showing too many fields.

**Follow-ups:**
- *Dependency vs association?* → Method-local use vs stored reference.
- *Aggregation in code?* → Usually references; lifecycle is semantic.

### Q11. What are immutable objects and value objects?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Thread-safety, equality, and domain modeling depend on this.

**Short answer (say this first, 20-40 sec):**
An immutable object cannot change after construction. A value object is identified by values, not database identity. Java records are good for small immutable value objects.

**Detailed answer:**
```java
import java.math.BigDecimal;
public record Money(BigDecimal amount, String currency) {
  public Money {
    if (amount == null || amount.signum() < 0) throw new IllegalArgumentException("amount");
    if (currency == null || currency.length() != 3) throw new IllegalArgumentException("currency");
  }
  public Money add(Money other){
    if (!currency.equals(other.currency)) throw new IllegalArgumentException("currency mismatch");
    return new Money(amount.add(other.amount), currency);
  }
}
```
Entity: BookCopy has identity/lifecycle. Value object: Money, Email, Position.

**Common mistakes:**
- Exposing mutable lists.
- Assuming records are deeply immutable.
- Giving value objects setters.

**Follow-ups:**
- *How copy collections?* → List.copyOf.
- *Where useful?* → Cache keys, money, coordinates, user IDs.

### Q12. How do you design for testability?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Hidden tests and extension requests punish tightly coupled code.

**Short answer (say this first, 20-40 sec):**
Isolate pure logic, inject dependencies, depend on interfaces, and wrap time/random/IO. Services should run without Spring, DB, network, or filesystem.

**Detailed answer:**
```java
interface Clock { long millis(); }
class SystemClock implements Clock { public long millis(){ return System.currentTimeMillis(); } }
class TokenBucket {
  private final Clock clock;
  TokenBucket(Clock clock){ this.clock = clock; }
  boolean allow(String user){ return clock.millis() >= 0; }
}
```
Checklist: constructor injection, deterministic IDs, repository interfaces, no static mutable state, driver separate from logic.

**Common mistakes:**
- Static singletons everywhere.
- Direct time/random in algorithms.
- Tests that inspect only console output.

**Follow-ups:**
- *How test concurrency?* → Latches and small capacities.
- *Need Mockito?* → Hand-written fakes are fine.

### Q13. How should exceptions and validation be modeled?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** They check robust boundaries and clear failure modes.

**Short answer (say this first, 20-40 sec):**
Validate at boundaries and constructors, use meaningful domain exceptions for invalid operations, and fail fast instead of returning null.

**Detailed answer:**
```java
class OutOfStockException extends RuntimeException {
    OutOfStockException(String item) {
        super(item + " out of stock");
    }
}

record SlotId(int floor, int number) {
    SlotId {
        if (floor < 0 || number < 0) {
            throw new IllegalArgumentException("negative slot");
        }
    }
}

class Inventory {
    private int count;

    void dispense(String item) {
        if (count <= 0) {
            throw new OutOfStockException(item);
        }
        count--;
    }
}
```
In REST apps, map domain exceptions to HTTP status in controller advice, not in domain services.

**Common mistakes:**
- Returning null for errors.
- Catching and swallowing exceptions.
- Throwing generic Exception.

**Follow-ups:**
- *Checked or unchecked?* → Domain validation is usually unchecked.
- *Where validate?* → Constructors and workflow service methods.

## GoF and Enterprise Patterns

### Q14. Explain Singleton, five variants, and how it can break.
`🔴 Advanced` `🔥` `Must-know SDE-1`

**Why they ask:** Singleton checks whether you understand global state, JVM class loading, and Java object-creation loopholes.

**Short answer (say this first, 20-40 sec):**
Use Singleton only when exactly one shared coordinator is required. Enum is safest; holder is lazy and simple; double-checked locking needs `volatile`. In Spring, prefer DI-managed singleton beans over manual global access.

**Detailed answer:**
```mermaid
classDiagram
class ConfigRegistry { -ConfigRegistry() +getInstance() ConfigRegistry }
class Holder { +INSTANCE ConfigRegistry }
ConfigRegistry --> Holder
Client ..> ConfigRegistry
```
```java
public enum AppConfig {
    INSTANCE;
    public String get(String key) {
        return System.getProperty(key);
    }
}
public final class HolderSingleton {
    private HolderSingleton() {
    }
    private static class Holder {
        private static final HolderSingleton INSTANCE = new HolderSingleton();
    }
    public static HolderSingleton getInstance() {
        return Holder.INSTANCE;
    }
}
```
one controlled instance. Use: stateless config, logger adapters, or metadata registries. Avoid for request/user state, hidden dependencies, or anything tests must replace. JDK: `Runtime.getRuntime()`. Spring: singleton bean scope is one bean per container, not a GoF singleton. LLD: config registry or logging sink in Parking Lot.

**Common mistakes:**
- Missing volatile in DCL.
- Using Singleton for mutable business state.

**Follow-ups:**
- *How can it break?* → Reflection, serialization, or cloning can create extra instances unless blocked.
- *Enum singleton?* → Safest Java option; JVM protects enum constants during reflection and serialization.

### Q15. Explain the Factory Method pattern.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Factory Method tests whether you can hide creation choices without overengineering every constructor.

**Short answer (say this first, 20-40 sec):**
Factory Method creates a product through a method while clients depend on the product interface. Use when the concrete class depends on input, config, or subtype. Avoid it for one obvious constructor.

**Detailed answer:**
```mermaid
classDiagram
class Vehicle { <<interface>> +type() VehicleType }
class Car
class VehicleFactory { +create(VehicleType) Vehicle }
Vehicle <|.. Car
VehicleFactory --> Vehicle
```
```java
enum VehicleType {
    CAR,
    BIKE
}
interface Vehicle {
    VehicleType type();
}
record Car() implements Vehicle {
    public VehicleType type() {
        return VehicleType.CAR;
    }
}
final class VehicleFactory {
    Vehicle create(VehicleType type) {
        return switch (type) {
            case CAR -> new Car();
            case BIKE -> () -> VehicleType.BIKE;
        };
    }
}
```
centralize product selection. Use when variants should not change clients. Avoid when `new` is stable and readable. JDK/Spring: `Calendar.getInstance()`, `NumberFormat.getCurrencyInstance()`, Spring `FactoryBean`. LLD: creating vehicle, ticket, or fee-policy objects.

**Common mistakes:**
- Mixing creation with business rules.
- Returning concrete types instead of product interfaces.

**Follow-ups:**
- *Factory Method vs Abstract Factory?* → Factory Method creates one product; Abstract Factory creates related products.
- *When avoid it?* → When a direct constructor is stable and readable.

### Q16. Explain the Abstract Factory pattern.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Abstract Factory checks whether you can keep related provider-specific objects consistent.

**Short answer (say this first, 20-40 sec):**
Abstract Factory creates families of related objects without binding clients to concrete classes. Use when products must be compatible, like payment gateway plus refund service. Avoid it for one product type.

**Detailed answer:**
```mermaid
classDiagram
class PaymentFactory { <<interface>> +gateway() Gateway +refunds() RefundService }
class RazorpayFactory
class Gateway { <<interface>> }
class RefundService { <<interface>> }
PaymentFactory <|.. RazorpayFactory
PaymentFactory --> Gateway
PaymentFactory --> RefundService
```
```java
interface Gateway {
    Receipt charge(long paise);
}
interface RefundService {
    void refund(String paymentId);
}
record Receipt(String id) {
}
interface PaymentFactory {
    Gateway gateway();
    RefundService refunds();
}
final class RazorpayFactory implements PaymentFactory {
    public Gateway gateway() {
        return amount -> new Receipt("rzp-" + amount);
    }
    public RefundService refunds() {
        return paymentId -> { };
    }
}
```
create compatible product families. Use: provider suites selected by config. Avoid when one simple factory or DI profile is enough. JDK/Spring: JAXP factories, Spring profiles/config classes. LLD: payment provider family in Food Delivery or BookMyShow.

**Common mistakes:**
- Creating factories before family variation exists.
- Mixing products from different families.

**Follow-ups:**
- *Spring alternative?* → Profiles or conditional beans can select a compatible family.
- *When avoid it?* → When a single factory method is enough.

### Q17. Explain the Builder pattern.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Builder shows whether you can handle optional fields without unreadable constructors.

**Short answer (say this first, 20-40 sec):**
Builder constructs an object step by step and validates at `build()`. Use it for many optional fields, named construction, or immutable commands. Avoid it for two mandatory fields.

**Detailed answer:**
```mermaid
classDiagram
class Order { -customerId String -coupon String }
class Builder { +coupon(String) Builder +build() Order }
Order --> Builder
Client ..> Builder
```
```java
public record Order(String customerId, String coupon, boolean giftWrap) {
    public static Builder builder(String customerId) {
        return new Builder(customerId);
    }
    public static final class Builder {
        private final String customerId;
        private String coupon;
        private boolean giftWrap;
        private Builder(String customerId) {
            this.customerId = java.util.Objects.requireNonNull(customerId);
        }
        public Builder coupon(String coupon) {
            this.coupon = coupon;
            return this;
        }
        public Builder giftWrap(boolean giftWrap) {
            this.giftWrap = giftWrap;
            return this;
        }
        public Order build() {
            return new Order(customerId, coupon, giftWrap);
        }
    }
}
```
readable construction plus validation. Use: immutable requests, notification templates, and search filters. Avoid if fields are few and validation is absent. JDK/Spring: `StringBuilder`, `UriComponentsBuilder`, `ResponseEntity.BodyBuilder`. LLD: order creation request or report filter.

**Common mistakes:**
- Using Builder with no invariants.
- Forgetting mandatory fields or validation.

**Follow-ups:**
- *Builder vs telescoping constructors?* → Builder names optional fields and avoids overload ambiguity.
- *Lombok @Builder?* → It reduces boilerplate, but invariants still need explicit validation.

### Q18. Explain the Prototype pattern.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Prototype reveals whether you understand copying, mutability, and configured templates.

**Short answer (say this first, 20-40 sec):**
Prototype creates new objects by copying an existing configured instance. Use when setup is expensive or templates vary slightly. Avoid shallow copies of mutable fields.

**Detailed answer:**
```mermaid
classDiagram
class CouponRule { +copyFor(String) CouponRule }
class RuleRegistry { +prototype(String) CouponRule }
RuleRegistry --> CouponRule
Client ..> RuleRegistry
```
```java
import java.util.List;
record CouponRule(String name, List<String> actions) {
    CouponRule {
        actions = List.copyOf(actions);
    }
    CouponRule copyFor(String newName) {
        return new CouponRule(newName, actions);
    }
}
```
clone a known-good template. Use: game pieces, coupon rules, or tenant defaults. Prefer explicit copy methods over Java `Cloneable`. JDK/Spring: `Object.clone()` concept, Spring prototype bean scope. LLD: Chess piece prototypes or coupon templates.

**Common mistakes:**
- Sharing mutable collections between copies.
- Copying identity fields that should be regenerated.

**Follow-ups:**
- *Deep vs shallow copy?* → Deep-copy owned mutable state; share immutable values.
- *Why avoid Cloneable?* → It is shallow by default and awkward to make safe.

### Q19. Explain the Adapter pattern.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Adapter checks whether you can integrate external APIs without leaking vendor details.

**Short answer (say this first, 20-40 sec):**
Adapter converts an incompatible interface into the interface your code expects. Use it around SDKs, legacy code, or different data shapes. Avoid it if clients can directly use the dependency cleanly.

**Detailed answer:**
```mermaid
classDiagram
class PaymentGateway { <<interface>> +charge(long) boolean }
class VendorSdk { +pay(long) String }
class VendorAdapter { -sdk VendorSdk +charge(long) boolean }
PaymentGateway <|.. VendorAdapter
VendorAdapter --> VendorSdk
CheckoutService --> PaymentGateway
```
```java
interface PaymentGateway {
    boolean charge(long paise);
}
final class VendorSdk {
    String pay(long paise) {
        return "OK";
    }
}
final class VendorAdapter implements PaymentGateway {
    private final VendorSdk sdk;
    VendorAdapter(VendorSdk sdk) {
        this.sdk = sdk;
    }
    public boolean charge(long paise) {
        return "OK".equals(sdk.pay(paise));
    }
}
```
translate interface, units, exceptions, and models. Use: payments, email providers, or storage clients. Avoid passing vendor DTOs into domain services. JDK/Spring: `InputStreamReader`, `Arrays.asList()`, Spring MVC adapters. LLD: payment gateway in BookMyShow or Food Delivery.

**Common mistakes:**
- Leaking vendor DTOs or exceptions.
- Putting retries, pricing, and persistence inside the adapter.

**Follow-ups:**
- *Adapter vs Proxy?* → Adapter changes an interface; Proxy controls access to the same interface.
- *Adapter vs Facade?* → Adapter translates; Facade simplifies a subsystem.

### Q20. Explain the Decorator pattern.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Decorator tests whether you can add behavior without subclass explosion.

**Short answer (say this first, 20-40 sec):**
Decorator wraps an object with the same interface and adds behavior before or after delegation. Use it for optional, stackable features. Avoid it when one explicit class is clearer.

**Detailed answer:**
```mermaid
classDiagram
class Notifier { <<interface>> +send(String) void }
class EmailNotifier
class AuditingNotifier
Notifier <|.. EmailNotifier
Notifier <|.. AuditingNotifier
AuditingNotifier --> Notifier
```
```java
interface Notifier {
    void send(String message);
}
final class EmailNotifier implements Notifier {
    public void send(String message) {
        System.out.println("email " + message);
    }
}
final class AuditingNotifier implements Notifier {
    private final Notifier delegate;
    AuditingNotifier(Notifier delegate) {
        this.delegate = delegate;
    }
    public void send(String message) {
        System.out.println("audit " + message);
        delegate.send(message);
    }
}
```
add responsibilities dynamically while preserving the interface. Use: caching, logging, metrics, compression, or notification add-ons. Avoid deep wrapper stacks. JDK/Spring: `BufferedInputStream`, `Collections.unmodifiableList()`, bean decorators. LLD: logging/metrics around gateways.

**Common mistakes:**
- Changing the interface while decorating.
- Creating deep wrapper stacks that are hard to debug.

**Follow-ups:**
- *Decorator vs Proxy vs Adapter?* → Decorator adds behavior; Proxy controls access; Adapter changes interface.
- *When avoid it?* → When the behavior is mandatory core behavior.

### Q21. Explain the Facade pattern.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Facade shows whether you can make a subsystem usable without creating a god service.

**Short answer (say this first, 20-40 sec):**
Facade exposes a small use-case API over several collaborating classes. Use it to reduce client coupling to subsystem details. Avoid letting it absorb all business logic.

**Detailed answer:**
```mermaid
classDiagram
class CheckoutFacade { +checkout(Cart) Receipt }
class InventoryService
class PaymentGateway
class OrderRepository
CheckoutFacade --> InventoryService
CheckoutFacade --> PaymentGateway
CheckoutFacade --> OrderRepository
Client ..> CheckoutFacade
```
```java
final class CheckoutFacade {
    private final InventoryService inventory;
    private final PaymentGateway payments;
    private final OrderRepository orders;
    CheckoutFacade(
            InventoryService inventory,
            PaymentGateway payments,
            OrderRepository orders) {
        this.inventory = inventory;
        this.payments = payments;
        this.orders = orders;
    }
    Receipt checkout(Cart cart) {
        inventory.reserve(cart);
        Receipt receipt = payments.charge(cart.totalPaise());
        orders.save(cart, receipt);
        return receipt;
    }
}
```
provide one stable entry point to a complex workflow. Use: checkout, booking, or report generation. Avoid vague `Manager` classes. JDK/Spring: `JdbcTemplate` simplifies JDBC; service layers often act as facades. LLD: BookMyShow booking facade.

**Common mistakes:**
- Turning the facade into a god service.
- Exposing subsystem internals through facade methods.

**Follow-ups:**
- *Facade vs Adapter?* → Facade simplifies a subsystem; Adapter converts an incompatible interface.
- *When avoid it?* → When one direct collaborator is already simple.

### Q22. Explain the Proxy pattern.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Proxy connects LLD to Spring AOP, transactions, caching, and access control.

**Short answer (say this first, 20-40 sec):**
Proxy exposes the same interface as the real object and controls access to it. Use it for lazy loading, authorization, caching, retries, or transactions. Avoid it when it hides remote or expensive work.

**Detailed answer:**
```mermaid
classDiagram
class ReportService { <<interface>> +report(String) String }
class RealReportService
class AuthProxy
ReportService <|.. RealReportService
ReportService <|.. AuthProxy
AuthProxy --> ReportService
Client --> ReportService
```
```java
interface ReportService {
    String report(String userId);
}
final class RealReportService implements ReportService {
    public String report(String userId) {
        return "report for " + userId;
    }
}
final class AuthProxy implements ReportService {
    private final ReportService target;
    AuthProxy(ReportService target) {
        this.target = target;
    }
    public String report(String userId) {
        if (userId == null) {
            throw new SecurityException("anonymous");
        }
        return target.report(userId);
    }
}
```
control access while preserving the target interface. Use: protection, virtual, remote, and smart-reference proxies. JDK/Spring: `java.lang.reflect.Proxy`, Hibernate lazy proxies, Spring AOP `@Transactional` and `@Cacheable`. LLD: access-controlled admin service.

**Common mistakes:**
- Confusing Proxy with Adapter.
- Forgetting Spring self-invocation bypasses proxy advice.

**Follow-ups:**
- *How does @Transactional work?* → Spring wraps the bean and manages transaction boundaries around proxied calls.
- *Proxy vs Decorator?* → Proxy controls access; Decorator adds optional behavior.

### Q23. Explain the Composite pattern.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Composite checks if you can model tree structures without type checks everywhere.

**Short answer (say this first, 20-40 sec):**
Composite lets clients treat leaves and groups through the same interface. Use it for recursive structures like menus, folders, comments, or org charts. Avoid it when the domain is flat.

**Detailed answer:**
```mermaid
classDiagram
class MenuNode { <<interface>> +pricePaise() long }
class MenuItem
class Combo
MenuNode <|.. MenuItem
MenuNode <|.. Combo
Combo o-- MenuNode
```
```java
interface MenuNode {
    long pricePaise();
}
record MenuItem(long pricePaise) implements MenuNode {
}
final class Combo implements MenuNode {
    private final java.util.List<MenuNode> items;
    Combo(java.util.List<MenuNode> items) {
        this.items = java.util.List.copyOf(items);
    }
    public long pricePaise() {
        return items.stream().mapToLong(MenuNode::pricePaise).sum();
    }
}
```
uniform operations over part-whole trees. Use when recursion is natural. Avoid forcing flat data into a tree. JDK/Spring: Swing/AWT component trees, expression trees conceptually. LLD: file system, menu combos, nested comments.

**Common mistakes:**
- Putting child operations on leaves without meaning.
- Forgetting cycle prevention when reparenting.

**Follow-ups:**
- *Transparent vs safe composite?* → Transparent exposes child methods everywhere; safe keeps them on composites.
- *When avoid it?* → When the model is flat and not recursive.

### Q24. Explain the Bridge pattern.
`🔴 Advanced` `🔥` `Stretch`

**Why they ask:** Bridge tests if you can separate two dimensions of variation instead of creating class explosions.

**Short answer (say this first, 20-40 sec):**
Bridge decouples an abstraction from its implementation so both can vary independently. Use when type and platform/channel/provider vary separately. Avoid it when Strategy or simple composition is enough.

**Detailed answer:**
```mermaid
classDiagram
class Notification { -sender MessageSender +send(String) void }
class UrgentNotification
class MessageSender { <<interface>> +send(String) void }
class EmailSender
class SmsSender
Notification --> MessageSender
Notification <|-- UrgentNotification
MessageSender <|.. EmailSender
MessageSender <|.. SmsSender
```
```java
interface MessageSender {
    void send(String text);
}
abstract class Notification {
    private final MessageSender sender;
    protected Notification(MessageSender sender) {
        this.sender = sender;
    }
    public final void send(String text) {
        sender.send(format(text));
    }
    protected abstract String format(String text);
}
final class UrgentNotification extends Notification {
    UrgentNotification(MessageSender sender) {
        super(sender);
    }
    protected String format(String text) {
        return "URGENT: " + text;
    }
}
```
avoid `EmailUrgentNotification`, `SmsUrgentNotification`, etc. Use when abstraction type and implementation channel grow independently. JDK/Spring: JDBC API to drivers; SLF4J to logging implementations. LLD: notification type plus delivery channel.

**Common mistakes:**
- Calling any delegation Bridge.
- Creating two hierarchies before two axes really vary.

**Follow-ups:**
- *Bridge vs Strategy?* → Bridge separates two class hierarchies; Strategy swaps one algorithm.
- *LLD clue?* → Two independent dimensions such as message type and channel both expand.

### Q25. Explain the Flyweight pattern.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Flyweight checks whether you separate shared intrinsic state from per-object extrinsic state.

**Short answer (say this first, 20-40 sec):**
Flyweight shares immutable common state across many small objects. Use when object count is huge and shared state is duplicated. Avoid it if sharing adds complexity without memory pressure.

**Detailed answer:**
```mermaid
classDiagram
class PieceType { +icon String +color String }
class Piece { +row int +col int }
class PieceTypeFactory { +get(String,String) PieceType }
Piece --> PieceType
PieceTypeFactory --> PieceType
```
```java
record PieceType(String icon, String color) {
}
record Piece(int row, int col, PieceType type) {
}
final class PieceTypeFactory {
    private final java.util.Map<String, PieceType> cache = new java.util.HashMap<>();
    PieceType get(String icon, String color) {
        String key = icon + ":" + color;
        return cache.computeIfAbsent(key, ignored -> new PieceType(icon, color));
    }
}
```
share intrinsic immutable data and keep extrinsic coordinates outside. Use: chess pieces, map tiles, text glyphs. Avoid sharing mutable per-object state. JDK/Spring: `Integer` cache, string interning. LLD: Chess board or game map.

**Common mistakes:**
- Sharing mutable state accidentally.
- Adding a factory when object counts are tiny.

**Follow-ups:**
- *Intrinsic vs extrinsic?* → Intrinsic is shared immutable data; extrinsic is per-use context.
- *Thread safety?* → Immutable flyweights are safe; guard a concurrent factory cache.

### Q26. Explain the Strategy pattern.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Strategy checks if you replace conditionals with injectable algorithms only where variation matters.

**Short answer (say this first, 20-40 sec):**
Strategy encapsulates interchangeable algorithms behind one interface. Use when behavior varies by policy, tenant, or runtime configuration. Avoid it for stable two-line conditionals.

**Detailed answer:**
```mermaid
classDiagram
class PricingStrategy { <<interface>> +pricePaise(int) long }
class HourlyPricing
class ParkingService { -pricing PricingStrategy +feePaise(int) long }
PricingStrategy <|.. HourlyPricing
ParkingService --> PricingStrategy
```
```java
interface PricingStrategy {
    long pricePaise(int hours);
}
final class HourlyPricing implements PricingStrategy {
    public long pricePaise(int hours) {
        return hours * 4_000L;
    }
}
final class ParkingService {
    private final PricingStrategy pricing;
    ParkingService(PricingStrategy pricing) {
        this.pricing = pricing;
    }
    long feePaise(int hours) {
        return pricing.pricePaise(hours);
    }
}
```
swap algorithms without changing the context class. Use: pricing, scheduling, matching, or rate limiting. Avoid class-per-enum-value when a switch is clearer. JDK/Spring: `Comparator`, `FileVisitor`, Spring interface injection. LLD: parking fee policy, elevator scheduling, Splitwise split strategy.

**Common mistakes:**
- Creating one class per tiny branch too early.
- Letting strategies mutate context internals.

**Follow-ups:**
- *Strategy vs State?* → Strategy is chosen from outside; State changes with lifecycle.
- *Spring selection?* → Inject a Map of strategy beans or mark a default with @Primary.

### Q27. Explain the Observer pattern.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Observer tests loose coupling, ordering, backpressure, and sync vs async event delivery.

**Short answer (say this first, 20-40 sec):**
Observer lets subscribers react when a subject changes. Use it for in-process notifications where the subject should not know every consumer. Avoid it when reliable distributed delivery needs a broker.

**Detailed answer:**
```mermaid
classDiagram
class OrderListener { <<interface>> +onPaid(String) void }
class EmailListener
class OrderEvents { +subscribe(OrderListener) void +paid(String) void }
OrderListener <|.. EmailListener
OrderEvents o-- OrderListener
```
```java
interface OrderListener {
    void onPaid(String orderId);
}
final class OrderEvents {
    private final java.util.List<OrderListener> listeners = new java.util.ArrayList<>();
    void subscribe(OrderListener listener) {
        listeners.add(listener);
    }
    void paid(String orderId) {
        for (OrderListener listener : java.util.List.copyOf(listeners)) {
            listener.onPaid(orderId);
        }
    }
}
```
notify dependents without hard-coding them. Use: UI/domain events inside one process. Avoid slow synchronous observers on critical paths. JDK/Spring: Swing listeners, `PropertyChangeListener`, Spring `ApplicationEventPublisher`. LLD: stock alerts or order notification fan-out.

**Common mistakes:**
- Forgetting unsubscribe and leaks.
- Letting one observer failure stop all observers accidentally.

**Follow-ups:**
- *Observer vs Pub-Sub?* → Observer is usually in-process; Pub-Sub uses broker topics and durable delivery.
- *Sync or async?* → Async needs queues, retries, ordering, and failure handling.

### Q28. Explain the State pattern.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** State checks if you can model lifecycle behavior without giant transition switches.

**Short answer (say this first, 20-40 sec):**
State delegates behavior to the current state object, and transitions replace the state. Use when allowed operations depend heavily on lifecycle state. Avoid it when an enum transition table is simpler.

**Detailed answer:**
```mermaid
classDiagram
class OrderState { <<interface>> +pay(Order) void +cancel(Order) void }
class CreatedState
class PaidState
class Order { -state OrderState +pay() void +setState(OrderState) void }
OrderState <|.. CreatedState
OrderState <|.. PaidState
Order --> OrderState
```
```java
interface OrderState {
    void pay(Order order);
    void cancel(Order order);
}
final class CreatedState implements OrderState {
    public void pay(Order order) {
        order.setState(new PaidState());
    }
    public void cancel(Order order) {
        order.setState(new CancelledState());
    }
}
final class PaidState implements OrderState {
    public void pay(Order order) {
        throw new IllegalStateException("already paid");
    }
    public void cancel(Order order) {
        throw new IllegalStateException("refund required");
    }
}
```
keep state-specific operations near state-specific rules. Use: vending machine, ATM, order, or booking lifecycle. Avoid simple two-state flags. JDK/Spring: `Thread.State`, Spring StateMachine for heavy workflows. LLD: Vending Machine and BookMyShow booking status.

**Common mistakes:**
- Keeping a giant switch inside each state.
- Persisting state objects instead of stable status values.

**Follow-ups:**
- *Strategy vs State?* → Strategy is a chosen algorithm; State represents current lifecycle behavior.
- *How persist it?* → Store an enum/status and reconstruct behavior in domain code.

### Q29. Explain the Command pattern.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Command checks if you can queue, retry, log, or undo operations cleanly.

**Short answer (say this first, 20-40 sec):**
Command wraps a request as an object with `execute`. Use when requests need queuing, retries, scheduling, logging, or undo. Avoid it when a direct method call is enough.

**Detailed answer:**
```mermaid
classDiagram
class Command { <<interface>> +execute() void }
class TransferCommand
class Invoker { +submit(Command) void }
Command <|.. TransferCommand
Invoker --> Command
TransferCommand --> AccountService
```
```java
interface Command {
    void execute();
}
final class TransferCommand implements Command {
    private final AccountService accounts;
    private final String from;
    private final String to;
    private final long paise;
    TransferCommand(AccountService accounts, String from, String to, long paise) {
        this.accounts = accounts;
        this.from = from;
        this.to = to;
        this.paise = paise;
    }
    public void execute() {
        accounts.transfer(from, to, paise);
    }
}
```
turn requests into data-like objects. Use: job queues, editor undo, ATM transactions, or scheduling. Avoid command classes with no lifecycle benefit. JDK/Spring: `Runnable`, `Callable`, Swing `Action`, Spring Batch tasklets. LLD: ATM transaction commands.

**Common mistakes:**
- Capturing stale mutable objects.
- Forgetting idempotency for queued retries.

**Follow-ups:**
- *How implement undo?* → Store enough before-state or an inverse command.
- *Command vs Strategy?* → Command is a request; Strategy is an algorithm.

### Q30. Explain the Chain of Responsibility pattern.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Chain maps directly to filters, interceptors, and validation pipelines in Java web apps.

**Short answer (say this first, 20-40 sec):**
Chain passes a request through ordered handlers until one handles it or all run. Use it for filters, validation, authorization, or logging steps. Avoid it when order is unclear.

**Detailed answer:**
```mermaid
classDiagram
class Handler { <<interface>> +handle(Request, Chain) void }
class AuthHandler
class Chain { +next(Request) void }
Handler <|.. AuthHandler
Chain o-- Handler
```
```java
interface Handler {
    void handle(Request request, Chain chain);
}
final class Chain {
    private final java.util.List<Handler> handlers;
    private int index;
    Chain(java.util.List<Handler> handlers) {
        this.handlers = java.util.List.copyOf(handlers);
    }
    void next(Request request) {
        if (index < handlers.size()) {
            handlers.get(index++).handle(request, this);
        }
    }
}
```
decouple sender from ordered processing steps. Use: servlet filters, logging, auth, validation, and approvals. Avoid if direct calls are easier to trace. JDK/Spring: Servlet `FilterChain`, Spring Security filter chain, Spring MVC interceptors. LLD: request pipeline or logger chain.

**Common mistakes:**
- Not defining whether the chain stops or continues.
- Hiding important handler order constraints.

**Follow-ups:**
- *Servlet filters?* → Each filter wraps chain.doFilter, so order affects request and response handling.
- *Spring Security?* → Auth, CSRF, session, and authorization filters run as an ordered chain.

### Q31. Explain the Template Method pattern.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Template Method tests whether you can fix algorithm order while letting selected steps vary.

**Short answer (say this first, 20-40 sec):**
Template Method defines an algorithm skeleton in a base class and lets subclasses override steps. Use when order is stable and steps vary. Avoid it when composition avoids inheritance more cleanly.

**Detailed answer:**
```mermaid
classDiagram
class ImportJob { +run() void #read() List~String~ #write(List~String~) void }
class CsvImportJob
ImportJob <|-- CsvImportJob
Client ..> ImportJob
```
```java
abstract class ImportJob {
    public final void run() {
        java.util.List<String> rows = read();
        validate(rows);
        write(rows);
    }
    protected abstract java.util.List<String> read();
    protected void validate(java.util.List<String> rows) {
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("empty import");
        }
    }
    protected abstract void write(java.util.List<String> rows);
}
```
keep invariant algorithm order in one place. Use: import jobs, game turns, or report generation. Avoid if subclasses need to change the whole flow. JDK/Spring: `AbstractList`, `JdbcTemplate` callback flow, `TransactionTemplate`. LLD: ETL/import workflow.

**Common mistakes:**
- Allowing subclasses to override the template method.
- Using inheritance when composition is simpler.

**Follow-ups:**
- *JdbcTemplate?* → It owns JDBC lifecycle and calls callbacks for variable query-mapping steps.
- *Template vs Strategy?* → Template fixes order with inheritance; Strategy swaps behavior via composition.

### Q32. Explain the Iterator pattern.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Iterator checks traversal abstraction and Java collection fail-fast behavior.

**Short answer (say this first, 20-40 sec):**
Iterator exposes sequential traversal without revealing internal structure. Use it for custom collections or paged traversal. Avoid custom iterators when standard collections and streams are enough.

**Detailed answer:**
```mermaid
classDiagram
class Iterator~T~ { <<interface>> +hasNext() boolean +next() T }
class Playlist { +iterator() Iterator~String~ }
class PlaylistIterator
Iterator <|.. PlaylistIterator
Playlist --> PlaylistIterator
```
```java
import java.util.Iterator;
import java.util.List;
final class Playlist implements Iterable<String> {
    private final List<String> songs;
    Playlist(List<String> songs) {
        this.songs = List.copyOf(songs);
    }
    public Iterator<String> iterator() {
        return songs.iterator();
    }
}
```
decouple traversal from storage. Use: custom collections, tree traversal, cursor-like pages. Avoid exposing mutable internals. JDK/Spring: `Iterator`, `Iterable`, `Spliterator`, JDBC `ResultSet` cursor concept. LLD: playlist, catalog pagination, file tree traversal.

**Common mistakes:**
- Mutating a collection while iterating incorrectly.
- Not defining traversal order.

**Follow-ups:**
- *What is fail-fast?* → JDK iterators best-effort throw ConcurrentModificationException after structural changes.
- *Iterator vs Stream?* → Iterator is pull traversal; Stream adds lazy functional operations.

### Q33. Explain the Mediator pattern.
`🔴 Advanced` `🔥` `Stretch`

**Why they ask:** Mediator tests if you can reduce many-to-many object chatter in collaborative workflows.

**Short answer (say this first, 20-40 sec):**
Mediator centralizes communication between components so they do not depend on each other directly. Use when many objects coordinate through rules. Avoid it if the mediator becomes a god object.

**Detailed answer:**
```mermaid
classDiagram
class ChatRoom { +send(User,String) void }
class User { -room ChatRoom +send(String) void +receive(String) void }
ChatRoom o-- User
User --> ChatRoom
```
```java
final class ChatRoom {
    private final java.util.List<User> users = new java.util.ArrayList<>();
    void join(User user) {
        users.add(user);
    }
    void send(User sender, String message) {
        for (User user : users) {
            if (user != sender) {
                user.receive(message);
            }
        }
    }
}
```
move coordination rules out of peer objects. Use: chat rooms, UI dialogs, control towers, or auctions. Avoid when direct dependencies are few and clearer. JDK/Spring: controllers and dispatchers often mediate interactions. LLD: group chat or elevator controller.

**Common mistakes:**
- Moving all business logic into the mediator.
- Using Mediator when Observer or direct calls are simpler.

**Follow-ups:**
- *Mediator vs Observer?* → Mediator coordinates peers; Observer broadcasts changes to subscribers.
- *When avoid it?* → When there is no many-to-many communication problem.

### Q34. Explain the Memento pattern.
`🔴 Advanced` `🔥` `Stretch`

**Why they ask:** Memento checks whether you can support undo without exposing private internals.

**Short answer (say this first, 20-40 sec):**
Memento captures an object's state so it can be restored later. Use it for undo/redo or checkpoints. Avoid it for huge state unless you use diffs or persistence.

**Detailed answer:**
```mermaid
classDiagram
class Editor { +save() Snapshot +restore(Snapshot) void }
class Snapshot { -text String }
class History { +push(Snapshot) void +pop() Snapshot }
Editor --> Snapshot
History o-- Snapshot
```
```java
final class Editor {
    private String text = "";
    void type(String value) {
        text += value;
    }
    Snapshot save() {
        return new Snapshot(text);
    }
    void restore(Snapshot snapshot) {
        text = snapshot.text();
    }
    record Snapshot(String text) {
    }
}
```
restore previous state without exposing mutable internals. Use: text editor undo, game checkpoints, or wizard drafts. Avoid storing huge full snapshots on every change. JDK/Spring: transaction savepoints are a related idea. LLD: text editor or game restore.

**Common mistakes:**
- Capturing mutable references instead of copies.
- Keeping unlimited history in memory.

**Follow-ups:**
- *Full snapshot vs diff?* → Full is simple; diff is cheaper but harder to apply correctly.
- *How limit memory?* → Cap history, compress snapshots, or persist versions.

### Q35. Explain the Visitor pattern.
`🔴 Advanced` `🔥` `Stretch`

**Why they ask:** Visitor tests double dispatch and adding operations across stable class hierarchies.

**Short answer (say this first, 20-40 sec):**
Visitor adds operations to a stable object structure without modifying each class for every operation. Use when element types are stable but operations change. Avoid it when new element types are common.

**Detailed answer:**
```mermaid
classDiagram
class Shape { <<interface>> +accept(ShapeVisitor) void }
class Circle
class RectangleShape
class ShapeVisitor { <<interface>> +visit(Circle) void +visit(RectangleShape) void }
Shape <|.. Circle
Shape <|.. RectangleShape
ShapeVisitor --> Circle
ShapeVisitor --> RectangleShape
```
```java
interface Shape {
    void accept(ShapeVisitor visitor);
}
record Circle(int radius) implements Shape {
    public void accept(ShapeVisitor visitor) {
        visitor.visit(this);
    }
}
record RectangleShape(int width, int height) implements Shape {
    public void accept(ShapeVisitor visitor) {
        visitor.visit(this);
    }
}
interface ShapeVisitor {
    void visit(Circle circle);
    void visit(RectangleShape rectangle);
}
```
separate operations from element classes. Use: ASTs, reports over graphs, or export formats. Avoid it for changing element hierarchies. JDK/Spring: `FileVisitor`, annotation/AST visitors. LLD: file-system reports or expression evaluator.

**Common mistakes:**
- Using Visitor when methods are clearer.
- Forgetting new element types require visitor changes.

**Follow-ups:**
- *Why double dispatch?* → accept picks the runtime element, then visitor overload handles that type.
- *When avoid it?* → When new element types are added more often than operations.

### Q36. Explain the Repository pattern.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Repository checks whether you separate domain logic from persistence and can test without a DB.

**Short answer (say this first, 20-40 sec):**
Repository provides collection-like access to aggregate roots and hides persistence details. Use it behind domain services. Avoid putting workflows or HTTP concerns into repositories.

**Detailed answer:**
```mermaid
classDiagram
class OrderRepository { <<interface>> +save(Order) void +findById(String) Optional~Order~ }
class JpaOrderRepository
class OrderService
OrderRepository <|.. JpaOrderRepository
OrderService --> OrderRepository
JpaOrderRepository ..> Database
```
```java
import java.util.Optional;
interface OrderRepository {
    Optional<Order> findById(String id);
    void save(Order order);
}
final class OrderService {
    private final OrderRepository orders;
    OrderService(OrderRepository orders) {
        this.orders = orders;
    }
    void cancel(String id) {
        Order order = orders.findById(id).orElseThrow();
        order.cancel();
        orders.save(order);
    }
}
```
isolate persistence and keep domain code storage-agnostic. Use: aggregates such as Booking, Order, or Account. Avoid generic repositories that expose every table operation. JDK/Spring: Spring Data and JPA repositories. LLD: BookMyShow, Parking Lot, or Splitwise storage.

**Common mistakes:**
- Putting business decisions in query methods.
- Leaking SQL or JPA APIs into domain services.

**Follow-ups:**
- *DAO vs Repository?* → DAO is table-oriented; Repository is aggregate/domain-oriented.
- *How test?* → Use an in-memory fake implementing the repository interface.

### Q37. Explain the Dependency Injection pattern.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** DI checks whether your design is testable and not hard-wired to concrete infrastructure.

**Short answer (say this first, 20-40 sec):**
Dependency Injection gives an object its collaborators from outside instead of constructing them internally. Prefer constructor injection for required dependencies. Avoid service locators and static globals.

**Detailed answer:**
```mermaid
classDiagram
class EmailSender { <<interface>> +send(String) void }
class SmtpEmailSender
class SignupService { -sender EmailSender +signup(String) void }
EmailSender <|.. SmtpEmailSender
SignupService --> EmailSender
```
```java
interface EmailSender {
    void send(String address);
}
final class SignupService {
    private final EmailSender sender;
    SignupService(EmailSender sender) {
        this.sender = java.util.Objects.requireNonNull(sender);
    }
    void signup(String email) {
        sender.send(email);
    }
}
```
invert object assembly and make dependencies explicit. Use: repositories, gateways, clocks, ID generators, and notifiers. Avoid field injection and hidden lookups. JDK/Spring: Spring constructor injection, CDI/Guice. LLD: testable services in every machine-coding problem.

**Common mistakes:**
- Calling new for infrastructure inside services.
- Using field injection for required dependencies.

**Follow-ups:**
- *Constructor vs setter injection?* → Constructor for required dependencies; setter for optional reconfiguration.
- *DI vs Factory?* → DI supplies collaborators; Factory creates products based on runtime choice.

### Q38. Explain the Null Object pattern.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Null Object checks if you can remove null checks without hiding real absence or errors.

**Short answer (say this first, 20-40 sec):**
Null Object provides a do-nothing implementation of an interface. Use when absence means safe no-op. Avoid it when absence is an error or caller must know no value exists.

**Detailed answer:**
```mermaid
classDiagram
class AuditSink { <<interface>> +record(String) void }
class DbAuditSink
class NoOpAuditSink
AuditSink <|.. DbAuditSink
AuditSink <|.. NoOpAuditSink
OrderService --> AuditSink
```
```java
interface AuditSink {
    void record(String event);
}
final class NoOpAuditSink implements AuditSink {
    public void record(String event) {
        // intentionally no-op
    }
}
final class OrderService {
    private final AuditSink audit;
    OrderService(AuditSink audit) {
        this.audit = audit;
    }
    void place() {
        audit.record("order placed");
    }
}
```
replace repeated null checks with polymorphism. Use: optional logging, metrics, or notifications. Avoid when no-op hides failed payment, missing user, or corrupt state. JDK/Spring: `Collections.emptyList()`, no-op loggers, test fakes. LLD: optional notification/audit sink.

**Common mistakes:**
- Hiding real errors with no-op behavior.
- Using Null Object where Optional or an exception is clearer.

**Follow-ups:**
- *Null Object vs Optional?* → Null Object is behavior; Optional is explicit value absence.
- *When dangerous?* → Payment, security, and persistence failures must not become no-ops.

### Q39. What are Specification, Event Bus, and Pipeline patterns?
`🔴 Advanced` `🔥` `Stretch`

**Why they ask:** These appear when filtering, event routing, or staged processing grows beyond simple methods.

**Short answer (say this first, 20-40 sec):**
Specification composes predicates, Event Bus routes events to handlers, and Pipeline runs ordered transformation/validation stages. Use them when rules, event consumers, or stages grow independently. Avoid them for one condition, one listener, or two fixed steps.

**Detailed answer:**
```mermaid
classDiagram
class Specification~T~ { <<interface>> +isSatisfiedBy(T) boolean }
class EventBus { +publish(Object) void }
class Pipeline~T~ { +run(T) T }
Specification --> Candidate
EventBus o-- EventHandler
Pipeline o-- Stage
```
```java
import java.util.List;
import java.util.function.UnaryOperator;
interface Specification<T> {
    boolean isSatisfiedBy(T value);
    default Specification<T> and(Specification<T> other) {
        return value -> isSatisfiedBy(value) && other.isSatisfiedBy(value);
    }
}
final class Pipeline<T> {
    private final List<UnaryOperator<T>> stages;
    Pipeline(List<UnaryOperator<T>> stages) {
        this.stages = List.copyOf(stages);
    }
    T run(T input) {
        T current = input;
        for (UnaryOperator<T> stage : stages) {
            current = stage.apply(current);
        }
        return current;
    }
}
```
Specification names composable rules; Event Bus decouples event producers and consumers; Pipeline makes staged processing explicit. Use Specification for eligibility/search, Event Bus for in-process notifications, Pipeline for validation/enrichment/imports. JDK/Spring: `Predicate`, Spring events, servlet/filter-style pipelines. LLD: cab matching filters, order events, request validation.

**Common mistakes:**
- Building an Event Bus when Observer is enough.
- Continuing a pipeline after mandatory validation fails.

**Follow-ups:**
- *Specification vs Strategy?* → Specification composes yes/no rules; Strategy performs an algorithm.
- *Pipeline vs Chain?* → Pipeline usually transforms through all stages; Chain may stop when handled.

## Machine Coding Round Strategy

### Q40. How should you approach a 90-120 minute machine-coding LLD round?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** A good plan prevents half-built, untestable systems.

**Short answer (say this first, 20-40 sec):**
Clarify, design the smallest extensible model, code core flows first, then demo and test. State assumptions loudly and defer non-goals like DB/UI unless asked.

**Detailed answer:**
```mermaid
flowchart TD
A["Clarify requirements 10 min"] --> B["Entities and relationships 10 min"]
B --> C["Interfaces and patterns 10 min"]
C --> D["Core code 45-55 min"]
D --> E["Driver demo 10 min"]
E --> F["Focused tests 10-15 min"]
F --> G["Discuss extensions and trade-offs"]
```
| Rubric area | What good looks like |
|---|---|
| Working code | demo covers happy path and edge cases |
| Modularity | model, service, repository, strategy, exception |
| Extensibility | new vehicle/payment/rule added by new class |
| Thread-safety | lock/concurrent collection around shared mutable state |
| Tests | deterministic unit tests, not console-only validation |

```text
com.example.parking
  model/ service/ repository/ strategy/ exception/ Main.java
```
Common failures: coding before requirements, no driver, no tests, god service, null-heavy code, missing concurrency story, overusing patterns.

**Common mistakes:**
- Spending 40 minutes drawing.
- No runnable main method.
- Adding Spring Boot when plain Java was requested.

**Follow-ups:**
- *What code first?* → Domain models and one successful main flow.
- *If time is low?* → Finish demo path, then outline extensions.

## Revision Cheat Sheet

- Encapsulation protects invariants; abstraction hides unstable details.
- Prefer composition for capabilities and strategies; inheritance only for honest is-a.
- Interfaces define roles; abstract classes share algorithm/state among close relatives.
- SOLID is useful when tied to real future changes.
- Mermaid classDiagram generics use List~String~.
- Immutable value objects make equality, caching, and concurrency easier.
- Constructor injection plus interfaces makes code testable without frameworks.
- Singleton: enum safest; DCL needs volatile; Spring singleton is usually better.
- Creational patterns handle object creation; structural patterns adapt/wrap/compose; behavioral patterns manage algorithms and communication.
- Working code + tests + extensibility beats a beautiful incomplete design.

## Flashcards

1. OOP pillars? ? Encapsulation, abstraction, inheritance, polymorphism applied to invariants and extension points.
2. Composition over inheritance? ? Use collaborators when behavior varies independently.
3. Interface vs abstract class? ? Contract vs shared base algorithm/state.
4. SRP? ? One reason to change.
5. OCP? ? Add behavior without modifying tested code.
6. LSP? ? Subtypes preserve parent expectations.
7. DIP? ? Depend on abstractions, not concretions.
8. Value object? ? Equality by fields, usually immutable.
9. Strategy fit? ? Pricing, matching, rate limiting, scheduling.
10. State fit? ? Vending machine, ATM, order lifecycle.
11. Decorator fit? ? Logging, compression, notification channels.
12. Repository fit? ? In-memory or DB storage hidden behind collection-like API.
13. Null Object? ? Safe no-op collaborator when absence is acceptable.
14. Machine-coding flow? ? Clarify, entities, interfaces, core code, demo, tests.
15. Biggest failure? ? Non-runnable code with unclear responsibilities.
