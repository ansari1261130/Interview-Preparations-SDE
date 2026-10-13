# 10b · Low-Level Design (LLD) Problems Tier 1: Machine-Coding Study Guide

Interview-ready Java 17 designs for five high-frequency low-level design rounds. Each problem focuses on
clear requirements, extensible objects, readable code, deterministic tests, and concrete concurrency notes.
Code is shown as package sections; each public top-level type belongs in its own `.java` file with that name.

**Table of Contents**
- [Beginner reading rule for every problem](#beginner-reading-rule-for-every-problem)
- [How to present an LLD in 45-90 minutes](#how-to-present-an-lld-in-45-90-minutes)
- [Q1. Design Parking Lot](#q1-design-parking-lot)
- [Q2. Design Tic-Tac-Toe](#q2-design-tic-tac-toe)
- [Q3. Design Snake & Ladder](#q3-design-snake--ladder)
- [Q4. Design LRU Cache and LFU Cache](#q4-design-lru-cache-and-lfu-cache)
- [Q5. Design Rate Limiter](#q5-design-rate-limiter)
- [Revision Cheat Sheet](#revision-cheat-sheet)
- [Flashcards](#flashcards)

🟢 Beginner · 🟡 Intermediate · 🔴 Advanced · ⚫ FAANG · 🔥 = very frequently asked

## Beginner reading rule for every problem

Each solution has two layers. The **beginner primer** explains the real-world problem, mental model,
responsibility split, pattern-recognition signals, flow, alternatives, and scale path. The existing detailed
answer then provides diagrams, Java code, tests, concurrency notes, and interview follow-ups.

Expand these terms when speaking: Least Recently Used (LRU), Least Frequently Used (LFU), Electric Vehicle
(EV), Automated Number Plate Recognition (ANPR), Unified Payments Interface (UPI), Application Programming
Interface (API), database (DB), and Software Development Engineer level 1 or 2 (SDE-1 or SDE-2).

### Mental model

Treat each problem as a small backend product with one central workflow and a few stateful objects. A parking
lot allocates scarce spots, a board game validates moves, a cache evicts memory entries, and a rate limiter
spends request allowance. The names change, but the design rhythm is similar:

- A public service or facade owns the use case and keeps multi-object updates atomic.
- Entities own local state and invariants, such as spot occupancy, board cells, cache nodes, or token counts.
- Interfaces represent policies that may change: allocation, pricing, dice, cache behavior, or time source.
- Records and enums carry immutable facts, request results, statuses, and supported modes.
- Tests and synchronization prove that invalid moves, duplicate allocation, and concurrent overspending fail safely.

### How to think through any question here

1. Restate the user action in plain language: park, move, roll, get or put, or ask for permission.
2. Identify the scarce or protected state: one spot, one cell, one turn, limited cache capacity, or limited tokens.
3. Write the invariant before naming classes, for example no two vehicles in one spot or no request consumes a
   missing token.
4. Split responsibilities into models, the coordinating service, and replaceable policy interfaces.
5. Define the main success flow and the exact point where state changes.
6. Say what happens on failure: throw, return empty, return false, keep old state, or compensate.
7. Add concurrency only around shared mutation; avoid making every object globally synchronized.
8. End with the scale path: indexed lookup, database transactions, Redis, cleanup, metrics, or production libraries.

### Pattern recognition cues

| If the question mentions... | Think about... | Why it fits |
|---|---|---|
| Nearest, cheapest, compatible, or first available spot | Strategy for allocation | The selection rule changes without changing ticket lifecycle code. |
| Payment, ticket status, and releasing a resource | Transaction boundary and compensation | Payment failure must not release the parking spot. |
| A board, turns, illegal moves, and winner detection | Referee service plus board invariants | One object should validate order while the board protects cells or positions. |
| Random dice or bot moves | Injectable strategy | Tests need deterministic behavior while production can use randomness. |
| Least recently used | Hash map plus doubly linked list | The map finds nodes and the list tracks recency in constant time. |
| Least frequently used | Frequency buckets plus tie-breaking recency | Eviction depends on count first and age within the count second. |
| Burst plus steady average request rate | Token bucket | Capacity allows burst; refill rate controls long-term throughput. |
| Exact recent request count | Sliding-window log | Timestamps can answer whether requests are inside the current window. |
| Multiple servers enforcing one decision | External atomic authority | A single Java lock does not protect state across processes. |
| Time-dependent behavior | Inject `Clock` or `TimeProvider` | Tests become deterministic and production can use system time. |

### Abbreviations and key terms

| Short form | Expanded form | Plain meaning |
|---|---|---|
| AI | Artificial Intelligence | Computer logic that chooses moves or decisions, such as a future game bot. |
| ANPR | Automated Number Plate Recognition | Camera-based vehicle number detection at a parking gate. |
| API | Application Programming Interface | The methods or endpoints callers use, such as `enter`, `exit`, `get`, or `allow`. |
| DB | Database | Durable storage that can replace in-memory maps for tickets, games, or limiter state. |
| EV | Electric Vehicle | A vehicle type that may need a compatible charging spot. |
| FAANG | Facebook, Amazon, Apple, Netflix, Google | Shorthand for large product-company interview style. |
| HTTP | Hypertext Transfer Protocol | Web request protocol that often sits in front of a rate limiter. |
| ID | Identifier | A stable value that names an entity, ticket, player, key, client, or request. |
| IP | Internet Protocol | Network address often used as a simple rate-limiter client key. |
| JVM | Java Virtual Machine | The runtime process where Java locks protect only in-process state. |
| LFU | Least Frequently Used | Cache policy that evicts the item with the smallest access count. |
| LLD | Low-Level Design | Class, interface, method, and object-interaction design. |
| LRU | Least Recently Used | Cache policy that evicts the item not touched for the longest time. |
| O(1) | Constant time | Operation time stays roughly the same as data size grows. |
| SDE-1 | Software Development Engineer level 1 | Entry-level engineering interview expectation. |
| SDE-2 | Software Development Engineer level 2 | Mid-level expectation with more trade-off and scale discussion. |
| TTL | Time To Live | Expiry duration after which cache or limiter state can be removed. |
| UI | User Interface | Screen or console layer that should stay outside core design logic. |
| UPI | Unified Payments Interface | Indian digital payment mode that can be modeled as a payment strategy. |
| UTC | Coordinated Universal Time | Time standard used for deterministic timestamps across systems. |
| UUID | Universally Unique Identifier | Generated unique identifier for tickets, payments, or loans. |
| Invariant | Invariant | Rule that must always stay true, such as one vehicle per spot. |
| Strategy | Strategy pattern | Interface-based way to swap allocation, pricing, dice, cache, or time behavior. |
| Atomic update | Atomic update | A group of state changes that must succeed or fail together. |
| Token bucket | Token bucket algorithm | Rate-limit algorithm where time refills tokens and requests spend them. |
| Sliding window | Sliding-window algorithm | Rate-limit algorithm that counts only requests inside a recent time interval. |

### Common beginner traps

- Starting with design patterns before writing the invariant and failure cases.
- Putting all logic in one manager class, which hides where state actually belongs.
- Treating `get` as read-only in cache designs even though it updates recency or frequency metadata.
- Forgetting that a single Java lock does not protect multiple application instances.
- Returning success-shaped values after invalid moves or payment failures instead of failing explicitly.
- Using system time or randomness directly in tests, making behavior non-deterministic.

### How to study this file

1. Read each beginner primer first and explain the simple problem aloud in one minute.
2. Draw the responsibility map from memory, then compare it with the code and class diagram.
3. Read the short answer until you can deliver it without memorizing exact words.
4. Walk through the detailed flow and say which object changes state at each step.
5. Use the tests as active practice: predict the result, then explain the failure behavior.
6. Finish with the flashcards and invent one follow-up change, such as Redis rate limiting or cache Time To Live.

## How to present an LLD in 45-90 minutes

1. Clarify scope: actors, APIs, scale, concurrency, persistence, and what is explicitly out of scope.
2. Write 5-8 concrete functional requirements and 3-4 non-functional requirements.
3. Identify entities first, then relationships, then lifecycle/state transitions.
4. Draw class and sequence diagrams before coding; keep diagrams small and explain trade-offs.
5. Code the domain model and service APIs first; keep I/O and console output outside core logic.
6. State design patterns only where they remove real change pressure.
7. Add validation, deterministic tests, and concurrency boundaries.
8. End with extensions: "If we add X, I change these exact classes."

---

### Q1. Design Parking Lot
`🟡 Intermediate` `🔥` `Must-know SDE-1`

#### Beginner primer

**Simple problem:** A driver enters, receives one compatible free parking spot and a ticket, then later pays
and leaves. The system must never allocate the same spot to two vehicles.

**Mental model:** This is a **resource-allocation system** plus a **ticket lifecycle**. Floors contain spots;
the ticket proves that one vehicle currently owns one spot. `ParkingLot` coordinates the use case,
`ParkingSpot` protects occupancy rules, allocation and pricing interfaces hold replaceable policies, and the
payment processor isolates an external boundary.

**Thought process:** Identify the scarce resource (`ParkingSpot`), write the invariant (one vehicle per spot),
model the ownership record (`Ticket`), then separate policies that can change. Recognize **Strategy** when
the prompt says nearest/cheapest allocation or hourly/dynamic pricing. Recognize **Repository** when
in-memory ticket storage may later become database storage.

**Flow:** validate vehicle → atomically select and occupy a compatible spot → create active ticket → later
load ticket → calculate fee → collect payment → mark ticket paid → release spot → return receipt. If
allocation fails, create no ticket. If payment fails, do not release the spot.

**Alternatives and scale:** A small lot can scan every spot; a larger lot keeps free-spot indexes per type and
floor. One process can use a lock around allocation. Multiple servers require a database transaction,
conditional update, or unique active-allocation constraint. A single large `ParkingLot` class was rejected
because pricing, allocation, payment, and persistence change for different reasons.

**Responsibility map:**

| Class or interface | Responsibility | Key methods and contract | Relationships |
|---|---|---|---|
| `VehicleType` enum | Names supported vehicle categories. | Constants are read-only; no inputs, mutation, return value, or failure behavior. | Used by `Vehicle`, `SpotType.fits`, and `HourlyPricingStrategy`. |
| `SpotType` enum | Names spot categories and compatibility rules. | `fits(VehicleType)` takes a vehicle type, changes no state, returns whether it fits; no explicit validation. | Owned by `ParkingSpot`; depends on `VehicleType`. |
| `TicketStatus` enum | Tracks ticket lifecycle. | Constants represent active, paid, or lost; no methods or mutation. | Stored in `Ticket` and checked by `ParkingLot.exit`. |
| `PaymentMode` enum | Lists accepted payment modes. | Constants are selected by the caller; no methods or mutation. | Passed to `PaymentProcessor.pay` and stored in `Payment`. |
| `Vehicle` record | Carries vehicle number and type. | Canonical constructor stores inputs immutably; accessors return fields; no validation. | Passed into `ParkingLot.enter` and `ParkingSpot.canFit`. |
| `Payment` record | Captures payment result. | Canonical constructor stores id, amount, mode, and success flag; no mutation or validation. | Returned by `PaymentProcessor` and embedded in `ExitReceipt`. |
| `ExitReceipt` record | Returns exit outcome. | Canonical constructor stores ticket id, amount, and payment; no mutation or validation. | Created by `ParkingLot.exit` after successful payment and release. |
| `ParkingSpot` class | Owns one physical spot and its occupied vehicle. | Constructor stores ids and type; `id` returns id; `canFit` reads occupancy and type; `park` sets vehicle or throws `IllegalStateException`; `release` clears vehicle; `toString` returns display text. | Belongs to `ParkingFloor`; referenced by `Ticket` and allocation strategy. |
| `ParkingFloor` record | Groups spots under a floor id. | Canonical constructor stores id and spot list; accessors return them; no defensive copy or validation. | Stored in `ParkingLot`; scanned by `FirstFitAllocationStrategy`. |
| `Ticket` class | Records active ownership of one spot by one vehicle. | Constructor stores immutable details and starts active; accessors return fields; `markPaid` changes status to paid; no validation. | Created and stored by `ParkingLot`; read by pricing and exit flow. |
| `SpotAllocationStrategy` interface | Defines how a compatible spot is selected. | `allocate(Collection<ParkingFloor>, Vehicle)` returns a spot or fails according to implementation; it should not mutate maps itself. | Implemented by `FirstFitAllocationStrategy` and injected into `ParkingLot`. |
| `FirstFitAllocationStrategy` class | Picks the first compatible spot by floor order. | `allocate` scans floors and spots, changes no state directly, returns a free spot, or throws `IllegalStateException` when full. | Uses `ParkingFloor.spots` and `ParkingSpot.canFit`. |
| `PricingStrategy` interface | Defines fee calculation. | `price(Ticket, Instant)` returns amount in integer units; implementation decides validation and rounding. | Implemented by `HourlyPricingStrategy`; used by `ParkingLot.exit`. |
| `HourlyPricingStrategy` class | Calculates hourly fee by vehicle type. | `price` reads ticket and exit time, mutates nothing, returns rounded-up fee with at least one minute; no explicit invalid-time failure. | Depends on `Ticket.entryTime` and `VehicleType`. |
| `PaymentProcessor` interface | Abstracts external payment. | `pay(String, long, PaymentMode)` returns a `Payment` or throws on invalid or failed processing. | Implemented by `InMemoryPaymentProcessor`; called by `ParkingLot.exit`. |
| `InMemoryPaymentProcessor` class | Simulates successful payment. | `pay` takes ticket id, amount, and mode, returns successful `Payment`, and throws `IllegalArgumentException` for non-positive amount. | Creates `Payment` with `UUID`; has no stored state. |
| `ParkingLot` class | Coordinates atomic enter and exit workflows. | Constructor registers floors and policies; `enter` locks, rejects duplicate active vehicle, parks spot, stores and returns ticket; `exit` locks, validates ticket, prices, pays, marks paid, releases spot, removes ticket, and returns receipt; throws on invalid ticket, full lot, duplicate vehicle, or payment failure. | Owns floors and active tickets; uses all strategies, `Clock`, `Ticket`, and `ParkingSpot`. |
| `Main` class | Demonstrates one parking flow. | `main(String[])` builds sample objects, calls `enter` and `exit`, prints receipt; mutates only demo state; exceptions bubble up. | Wires model and service classes for a runnable example. |
| `ParkingLotTest` class | Verifies core parking behavior. | Helper builds one-spot lot; tests call service methods and use assertions; failures surface as JUnit assertion errors or expected exceptions. | Exercises `ParkingLot`, strategies, records, and enums. |

**Why they ask:** It tests entity modeling, allocation, payments, and atomic updates under contention.

**Short answer (say this first, 20-40 sec):**
Model a parking lot as floors containing typed spots. An entry gate atomically allocates a compatible spot
and issues a ticket; an exit gate calculates fee, collects payment, and releases the spot. Allocation,
pricing, and payment are strategies so the design can add nearest-spot, EV-only, or dynamic pricing.

**Detailed answer:**

#### 1. Clarifying questions & requirements
- Questions: How many floors and gates? Which vehicle and spot types? Is EV charging a separate spot type?
  Should allocation prefer nearest floor or any free spot? Which payment modes and pricing rules are needed?
- Functional: park BIKE/CAR/TRUCK/EV; support floors; spot sizes BIKE/COMPACT/LARGE/EV; entry and exit gates;
  issue one active ticket per parked vehicle; calculate fee; accept payment; release spot after payment.
- Non-functional: atomic allocation for the last spot; O(number of candidate spots) or better lookup; readable
  audit trail through ticket/payment IDs; deterministic tests by injecting `Clock`.
- Out of scope: reservations, camera/ANPR integration, distributed multi-city parking, persisted database.

#### 2. Entities & relationships
- `ParkingLot` owns floors, active tickets, allocation strategy, pricing strategy, and payment processor.
- `ParkingFloor` owns many `ParkingSpot` objects.
- `ParkingSpot` has a `SpotType`, occupied vehicle, and compatibility rule.
- `Ticket` links vehicle, spot, entry gate, entry time, and status.
- `ExitReceipt` records amount and payment status.

#### 3. Class diagram
```mermaid
classDiagram
class ParkingLot {
  -Map~String,ParkingFloor~ floors
  -Map~String,Ticket~ activeTickets
  -SpotAllocationStrategy allocator
  -PricingStrategy pricing
  +enter(String,Vehicle,String) Ticket
  +exit(String,String,PaymentMode) ExitReceipt
}
class ParkingFloor {
  -String id
  -List~ParkingSpot~ spots
  +spots() List~ParkingSpot~
}
class ParkingSpot {
  -String id
  -SpotType type
  -Vehicle vehicle
  +canFit(Vehicle) boolean
  +park(Vehicle) void
  +release() void
}
class Vehicle
class Ticket
class ExitReceipt
class SpotAllocationStrategy {
  <<interface>>
  +allocate(Collection~ParkingFloor~,Vehicle) ParkingSpot
}
class PricingStrategy {
  <<interface>>
  +price(Ticket,Instant) long
}
class PaymentProcessor {
  <<interface>>
  +pay(String,long,PaymentMode) Payment
}
ParkingLot *-- "1..*" ParkingFloor
ParkingFloor *-- "0..*" ParkingSpot
ParkingLot --> SpotAllocationStrategy
ParkingLot --> PricingStrategy
ParkingLot --> PaymentProcessor
Ticket --> Vehicle
Ticket --> ParkingSpot
ExitReceipt --> Ticket
```

#### 4. Main-flow sequence diagram
```mermaid
sequenceDiagram
actor Driver
participant Entry as EntryGate
participant Lot as ParkingLot
participant Alloc as SpotAllocationStrategy
participant Spot as ParkingSpot
participant Exit as ExitGate
participant Price as PricingStrategy
participant Pay as PaymentProcessor
Driver->>Entry: scan vehicle
Entry->>Lot: enter(vehicle, entryGateId)
Lot->>Alloc: allocate(floors, vehicle)
Alloc-->>Lot: compatible free spot
Lot->>Spot: park(vehicle)
Lot-->>Driver: ticket
Driver->>Exit: submit ticket
Exit->>Lot: exit(ticketId, exitGateId, mode)
Lot->>Price: price(ticket, exitTime)
Lot->>Pay: pay(ticketId, amount, mode)
Lot->>Spot: release()
Lot-->>Driver: receipt
```

#### 5. Design patterns used & why
- Strategy: `SpotAllocationStrategy`, `PricingStrategy`, and `PaymentProcessor` change independently.
- Factory: useful for creating floors/spots from configuration, not hard-coded constructors.
- Repository style: active ticket map hides storage and can later become a DB repository.

#### 6. Complete, compilable Java 17 code
```java
// parkinglot/model/Models.java
package parkinglot.model;

import java.time.Instant;

public enum VehicleType { BIKE, CAR, TRUCK, EV }

public enum SpotType {
    BIKE, COMPACT, LARGE, EV;

    public boolean fits(VehicleType type) {
        return switch (this) {
            case BIKE -> type == VehicleType.BIKE;
            case COMPACT -> type == VehicleType.BIKE || type == VehicleType.CAR;
            case LARGE -> type != VehicleType.EV;
            case EV -> type == VehicleType.EV;
        };
    }
}

public enum TicketStatus { ACTIVE, PAID, LOST }

public enum PaymentMode { CASH, CARD, UPI }

public record Vehicle(String number, VehicleType type) { }

public record Payment(String id, long amount, PaymentMode mode, boolean success) { }

public record ExitReceipt(String ticketId, long amount, Payment payment) { }

public final class ParkingSpot {
    private final String id;
    private final String floorId;
    private final SpotType type;
    private Vehicle vehicle;

    public ParkingSpot(String id, String floorId, SpotType type) {
        this.id = id;
        this.floorId = floorId;
        this.type = type;
    }

    public String id() {
        return id;
    }

    public boolean canFit(Vehicle candidate) {
        return vehicle == null && type.fits(candidate.type());
    }

    public void park(Vehicle candidate) {
        if (!canFit(candidate)) {
            throw new IllegalStateException("spot not available: " + id);
        }
        vehicle = candidate;
    }

    public void release() {
        vehicle = null;
    }

    @Override
    public String toString() {
        return floorId + "-" + id + "(" + type + ")";
    }
}

public record ParkingFloor(String id, java.util.List<ParkingSpot> spots) { }

public final class Ticket {
    private final String id;
    private final Vehicle vehicle;
    private final ParkingSpot spot;
    private final String entryGateId;
    private final Instant entryTime;
    private TicketStatus status = TicketStatus.ACTIVE;

    public Ticket(String id, Vehicle vehicle, ParkingSpot spot, String entryGateId, Instant entryTime) {
        this.id = id;
        this.vehicle = vehicle;
        this.spot = spot;
        this.entryGateId = entryGateId;
        this.entryTime = entryTime;
    }

    public String id() {
        return id;
    }

    public Vehicle vehicle() {
        return vehicle;
    }

    public ParkingSpot spot() {
        return spot;
    }

    public Instant entryTime() {
        return entryTime;
    }

    public TicketStatus status() {
        return status;
    }

    public void markPaid() {
        status = TicketStatus.PAID;
    }
}
```

```java
// parkinglot/service/ParkingLot.java
package parkinglot.service;

import parkinglot.model.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

public interface SpotAllocationStrategy {
    ParkingSpot allocate(Collection<ParkingFloor> floors, Vehicle vehicle);
}

public final class FirstFitAllocationStrategy implements SpotAllocationStrategy {
    @Override
    public ParkingSpot allocate(Collection<ParkingFloor> floors, Vehicle vehicle) {
        return floors.stream()
                .sorted(Comparator.comparing(ParkingFloor::id))
                .flatMap(floor -> floor.spots().stream())
                .filter(spot -> spot.canFit(vehicle))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("parking lot full for " + vehicle.type()));
    }
}

public interface PricingStrategy {
    long price(Ticket ticket, Instant exitTime);
}

public final class HourlyPricingStrategy implements PricingStrategy {
    @Override
    public long price(Ticket ticket, Instant exitTime) {
        long minutes = Math.max(1, Duration.between(ticket.entryTime(), exitTime).toMinutes());
        long hours = (minutes + 59) / 60;
        long rate = switch (ticket.vehicle().type()) {
            case BIKE -> 20;
            case CAR, EV -> 40;
            case TRUCK -> 80;
        };
        return hours * rate;
    }
}

public interface PaymentProcessor {
    Payment pay(String ticketId, long amount, PaymentMode mode);
}

public final class InMemoryPaymentProcessor implements PaymentProcessor {
    @Override
    public Payment pay(String ticketId, long amount, PaymentMode mode) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        return new Payment(UUID.randomUUID().toString(), amount, mode, true);
    }
}

public final class ParkingLot {
    private final Map<String, ParkingFloor> floors = new LinkedHashMap<>();
    private final Map<String, Ticket> activeTickets = new LinkedHashMap<>();
    private final SpotAllocationStrategy allocator;
    private final PricingStrategy pricing;
    private final PaymentProcessor payments;
    private final Clock clock;
    private final ReentrantLock lock = new ReentrantLock();

    public ParkingLot(
            Collection<ParkingFloor> floors,
            SpotAllocationStrategy allocator,
            PricingStrategy pricing,
            PaymentProcessor payments,
            Clock clock) {
        floors.forEach(floor -> this.floors.put(floor.id(), floor));
        this.allocator = allocator;
        this.pricing = pricing;
        this.payments = payments;
        this.clock = clock;
    }

    public Ticket enter(String entryGateId, Vehicle vehicle) {
        lock.lock();
        try {
            boolean alreadyParked = activeTickets.values().stream()
                    .anyMatch(ticket -> ticket.vehicle().number().equals(vehicle.number()));
            if (alreadyParked) {
                throw new IllegalStateException("vehicle already parked");
            }
            ParkingSpot spot = allocator.allocate(floors.values(), vehicle);
            spot.park(vehicle);
            Ticket ticket = new Ticket(
                    UUID.randomUUID().toString(),
                    vehicle,
                    spot,
                    entryGateId,
                    clock.instant());
            activeTickets.put(ticket.id(), ticket);
            return ticket;
        } finally {
            lock.unlock();
        }
    }

    public ExitReceipt exit(String ticketId, String exitGateId, PaymentMode mode) {
        lock.lock();
        try {
            Ticket ticket = activeTickets.get(ticketId);
            if (ticket == null || ticket.status() != TicketStatus.ACTIVE) {
                throw new IllegalArgumentException("invalid active ticket");
            }
            long amount = pricing.price(ticket, clock.instant());
            Payment payment = payments.pay(ticket.id(), amount, mode);
            if (!payment.success()) {
                throw new IllegalStateException("payment failed");
            }
            ticket.markPaid();
            ticket.spot().release();
            activeTickets.remove(ticket.id());
            return new ExitReceipt(ticket.id(), amount, payment);
        } finally {
            lock.unlock();
        }
    }
}
```

```java
// parkinglot/Main.java
package parkinglot;

import parkinglot.model.*;
import parkinglot.service.*;

import java.time.Clock;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        ParkingFloor floor = new ParkingFloor("F1", List.of(
                new ParkingSpot("B1", "F1", SpotType.BIKE),
                new ParkingSpot("C1", "F1", SpotType.COMPACT),
                new ParkingSpot("L1", "F1", SpotType.LARGE),
                new ParkingSpot("E1", "F1", SpotType.EV)));
        ParkingLot lot = new ParkingLot(
                List.of(floor),
                new FirstFitAllocationStrategy(),
                new HourlyPricingStrategy(),
                new InMemoryPaymentProcessor(),
                Clock.systemUTC());

        Ticket ticket = lot.enter("ENTRY-1", new Vehicle("UP32AB1234", VehicleType.CAR));
        ExitReceipt receipt = lot.exit(ticket.id(), "EXIT-1", PaymentMode.UPI);
        System.out.println(receipt);
    }
}
```

#### 7. Thread-safety considerations
- `ParkingLot.enter` and `ParkingLot.exit` use one `ReentrantLock` because ticket map and spot occupancy must
  change atomically; otherwise two cars can get the same last spot.
- Higher throughput version: lock per floor or per spot plus a concurrent active-ticket repository.
- Payment should be idempotent with a transaction ID if external payment calls can retry.

#### 8. Extensibility
- Add nearest-gate allocation: implement `SpotAllocationStrategy` using gate-to-spot distance metadata.
- Add reservations: add `Reservation`, block reserved spots in `ParkingSpot.canFit`, expire via scheduler.
- Add EV charging fee: add `ChargingSession` and decorate `PricingStrategy` with charging duration.
- Add database: replace active ticket map with `TicketRepository`; keep service API unchanged.

#### 9. JUnit 5 tests
```java
package parkinglot;

import org.junit.jupiter.api.Test;
import parkinglot.model.*;
import parkinglot.service.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParkingLotTest {
    private ParkingLot lotWithOneCompactSpot() {
        ParkingFloor floor = new ParkingFloor("F1", List.of(
                new ParkingSpot("C1", "F1", SpotType.COMPACT)));
        return new ParkingLot(
                List.of(floor),
                new FirstFitAllocationStrategy(),
                new HourlyPricingStrategy(),
                new InMemoryPaymentProcessor(),
                Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void parksAndUnparksCar() {
        ParkingLot lot = lotWithOneCompactSpot();
        Ticket ticket = lot.enter("E1", new Vehicle("KA01", VehicleType.CAR));
        ExitReceipt receipt = lot.exit(ticket.id(), "X1", PaymentMode.CARD);

        assertEquals(40, receipt.amount());
        assertTrue(receipt.payment().success());
    }

    @Test
    void rejectsWhenNoCompatibleSpotExists() {
        ParkingLot lot = lotWithOneCompactSpot();

        assertThrows(IllegalStateException.class,
                () -> lot.enter("E1", new Vehicle("TRUCK-1", VehicleType.TRUCK)));
    }

    @Test
    void rejectsDuplicateActiveVehicle() {
        ParkingFloor floor = new ParkingFloor("F1", List.of(
                new ParkingSpot("C1", "F1", SpotType.COMPACT),
                new ParkingSpot("C2", "F1", SpotType.COMPACT)));
        ParkingLot lot = new ParkingLot(
                List.of(floor),
                new FirstFitAllocationStrategy(),
                new HourlyPricingStrategy(),
                new InMemoryPaymentProcessor(),
                Clock.systemUTC());

        lot.enter("E1", new Vehicle("KA01", VehicleType.CAR));

        assertThrows(IllegalStateException.class,
                () -> lot.enter("E2", new Vehicle("KA01", VehicleType.CAR)));
    }
}
```

**Common mistakes:**
- Using one `SpotType.CAR` only; no floor, gate, ticket status, or payment lifecycle.
- Freeing a spot before payment succeeds.
- Hard-coding pricing in `ParkingLot` instead of a pricing strategy.

**Follow-ups:**
- *How do you prevent double allocation in DB?* → Use a transaction with row lock or optimistic version on spot.
- *How do you show free count per floor?* → Maintain counters by `floorId + spotType`, update inside same lock.
- *How do you support lost tickets?* → Mark ticket lost, charge maximum daily fee, then release spot manually.

---

### Q2. Design Tic-Tac-Toe
`🟢 Beginner` `🔥` `Must-know SDE-1`

#### Beginner primer

**Simple problem:** Two players alternately place marks on a board. Reject illegal moves and announce a win
or draw. The game must never accept two moves for one turn or overwrite an occupied cell.

**Mental model:** `Game` is the referee, `Board` owns cells, `Player` owns a mark, and `Move` records one
action. Start with a board scan after every move because it is easiest to prove correct. The row, column,
and diagonal counters are an optimization: add `+1` for one mark and `-1` for the other; an absolute total
equal to board size means one player filled the whole line.

**Pattern recognition and flow:** A fixed two-player game needs no heavy pattern. A **Strategy** becomes
useful only when human and bot algorithms choose moves differently. Verify active game and turn → verify
bounds and empty cell → place mark → update win counters → declare win/draw or switch turn.

**Alternatives and scale:** Scanning the board is simpler; counters make win checks constant time but add
state that must stay consistent with the board. A State-pattern class per status is over-engineering for
three simple statuses. Extensions include arbitrary board size, multiple players, undo, replay, and bots;
network play additionally needs authoritative turn ordering.

**Responsibility map:**

| Class or interface | Responsibility | Key methods and contract | Relationships |
|---|---|---|---|
| `Mark` enum | Represents X, O, and empty cells with scoring values. | Constructor stores score; `score()` returns integer contribution; no mutation or failure. | Used by `Board.place`, `Player`, and `MoveResult`. |
| `GameStatus` enum | Represents game lifecycle outcome. | Constants are read-only; no inputs, mutation, return value, or failure behavior. | Stored in `Game` and returned by `MoveResult`. |
| `Player` record | Carries player id, display name, and mark. | Canonical constructor stores fields immutably; accessors return values; no validation. | Passed to `Game` and compared by player id during moves. |
| `MoveResult` record | Reports status and next turn after a move. | Canonical constructor stores status and next mark; accessors return fields; no mutation or validation. | Returned by `Game.move`. |
| `Board` class | Owns cells, counters, and fullness checks. | Constructor creates empty board; `place(Mark,int,int)` validates bounds and vacancy, mutates cell and counters, returns win boolean, and throws `IllegalArgumentException` for invalid moves; `full()` returns whether all cells are used. | Hidden inside `Game`; depends on `Mark.score`. |
| `Game` class | Coordinates turns and lifecycle. | Constructor validates X and O players, creates board, and sets first turn; `move(String,int,int)` is synchronized, validates active game and turn, delegates placement, updates status or turn, returns `MoveResult`, and throws for finished or illegal moves. | Owns `Board`, two `Player` records, and `GameStatus`. |
| `Main` class | Demonstrates a small game. | `main(String[])` creates players and game, performs moves, and prints results; exceptions bubble up. | Uses public model records, enums, and `Game`. |
| `TicTacToeTest` class | Verifies win, occupancy, and turn validation. | Test methods call `move` and assertions; expected invalid operations throw `IllegalArgumentException`; assertion failures fail the test. | Exercises `Game`, `Player`, `MoveResult`, and `GameStatus`. |

**Why they ask:** It checks clean validation, turn handling, and O(1) win detection without over-engineering.

**Short answer (say this first, 20-40 sec):**
The `Game` owns players, turn, status, and a `Board`. `Board.place` validates moves and updates row, column,
and diagonal counters using +1 for X and -1 for O, so each move checks win in O(1).

**Detailed answer:**

#### 1. Clarifying questions & requirements
- Questions: fixed 3x3 or NxN? Two players only? Need bot? Need undo or replay? Should API reject out-of-turn moves?
- Functional: create game; validate bounds, occupancy, and turn; place marks; detect X/O win and draw; expose status.
- Non-functional: O(1) winner check per move; deterministic service methods; no console input in game logic.
- Out of scope: online matchmaking, persistence, AI minimax, graphical UI.

#### 2. Entities & relationships
- `Game` coordinates turns and status.
- `Board` stores marks and O(1) counters.
- `Player` is immutable and owns one `Mark`.
- `MoveResult` is returned after each valid move.

#### 3. Class diagram
```mermaid
classDiagram
class Game {
  -Board board
  -Player x
  -Player o
  -Player turn
  -GameStatus status
  +move(String,int,int) MoveResult
}
class Board {
  -Mark[][] cells
  -int[] rows
  -int[] cols
  -int diagonal
  -int antiDiagonal
  +place(Mark,int,int) boolean
}
class Player
class MoveResult
Game *-- Board
Game --> "2" Player
Game --> MoveResult
```

#### 4. Main-flow sequence diagram
```mermaid
sequenceDiagram
actor Player
participant Game
participant Board
Player->>Game: move(playerId, row, col)
Game->>Game: validate turn and status
Game->>Board: place(mark, row, col)
Board-->>Game: winningMove?
Game-->>Player: MoveResult
```

#### 5. Design patterns used & why
- Encapsulation: `Board` owns cell and counter mutation.
- Strategy can be added later for bot moves; not needed for two human players.
- State enum is enough; a State pattern would be overkill for this lifecycle.

#### 6. Complete, compilable Java 17 code
```java
// tictactoe/model/Models.java
package tictactoe.model;

public enum Mark {
    X(1), O(-1), EMPTY(0);

    private final int score;

    Mark(int score) {
        this.score = score;
    }

    public int score() {
        return score;
    }
}

public enum GameStatus { IN_PROGRESS, X_WON, O_WON, DRAW }

public record Player(String id, String name, Mark mark) { }

public record MoveResult(GameStatus status, Mark nextTurn) { }
```

```java
// tictactoe/service/Game.java
package tictactoe.service;

import tictactoe.model.*;

import java.util.Arrays;

final class Board {
    private final int size;
    private final Mark[][] cells;
    private final int[] rows;
    private final int[] cols;
    private int diagonal;
    private int antiDiagonal;
    private int moves;

    Board(int size) {
        this.size = size;
        this.cells = new Mark[size][size];
        this.rows = new int[size];
        this.cols = new int[size];
        for (Mark[] row : cells) {
            Arrays.fill(row, Mark.EMPTY);
        }
    }

    boolean place(Mark mark, int row, int col) {
        if (row < 0 || row >= size || col < 0 || col >= size) {
            throw new IllegalArgumentException("move outside board");
        }
        if (cells[row][col] != Mark.EMPTY) {
            throw new IllegalArgumentException("cell already occupied");
        }
        cells[row][col] = mark;
        moves++;
        int score = mark.score();
        rows[row] += score;
        cols[col] += score;
        if (row == col) {
            diagonal += score;
        }
        if (row + col == size - 1) {
            antiDiagonal += score;
        }
        return Math.abs(rows[row]) == size
                || Math.abs(cols[col]) == size
                || Math.abs(diagonal) == size
                || Math.abs(antiDiagonal) == size;
    }

    boolean full() {
        return moves == size * size;
    }
}

public final class Game {
    private final Board board;
    private final Player x;
    private final Player o;
    private Player turn;
    private GameStatus status = GameStatus.IN_PROGRESS;

    public Game(int size, Player x, Player o) {
        if (x.mark() != Mark.X || o.mark() != Mark.O) {
            throw new IllegalArgumentException("players must be X and O");
        }
        this.board = new Board(size);
        this.x = x;
        this.o = o;
        this.turn = x;
    }

    public synchronized MoveResult move(String playerId, int row, int col) {
        if (status != GameStatus.IN_PROGRESS) {
            throw new IllegalStateException("game already finished");
        }
        if (!turn.id().equals(playerId)) {
            throw new IllegalArgumentException("not player's turn");
        }
        boolean won = board.place(turn.mark(), row, col);
        if (won) {
            status = turn.mark() == Mark.X ? GameStatus.X_WON : GameStatus.O_WON;
        } else if (board.full()) {
            status = GameStatus.DRAW;
        } else {
            turn = turn == x ? o : x;
        }
        return new MoveResult(status, status == GameStatus.IN_PROGRESS ? turn.mark() : Mark.EMPTY);
    }
}
```

```java
// tictactoe/Main.java
package tictactoe;

import tictactoe.model.*;
import tictactoe.service.Game;

public class Main {
    public static void main(String[] args) {
        Player x = new Player("p1", "Azam", Mark.X);
        Player o = new Player("p2", "Interviewer", Mark.O);
        Game game = new Game(3, x, o);

        System.out.println(game.move("p1", 0, 0));
        System.out.println(game.move("p2", 1, 0));
    }
}
```

#### 7. Thread-safety considerations
- `Game.move` is synchronized because status, turn, and board counters mutate together.
- In an online game, process moves through one game actor/queue to preserve order.
- Never expose the mutable `cells` array; return a defensive copy if board display is needed.

#### 8. Extensibility
- Add bot: introduce `MoveStrategy` with `nextMove(BoardView, Mark)`.
- Add undo: keep a stack of `Move` commands and reverse counters.
- Add tournament: create `GameRepository` and `ScoreboardService`.
- Add arbitrary win length: replace row counters with directional streak checking.

#### 9. JUnit 5 tests
```java
package tictactoe;

import org.junit.jupiter.api.Test;
import tictactoe.model.*;
import tictactoe.service.Game;

import static org.junit.jupiter.api.Assertions.*;

class TicTacToeTest {
    private final Player x = new Player("x", "X", Mark.X);
    private final Player o = new Player("o", "O", Mark.O);

    @Test
    void detectsRowWinInConstantTime() {
        Game game = new Game(3, x, o);

        game.move("x", 0, 0);
        game.move("o", 1, 0);
        game.move("x", 0, 1);
        game.move("o", 1, 1);
        MoveResult result = game.move("x", 0, 2);

        assertEquals(GameStatus.X_WON, result.status());
    }

    @Test
    void rejectsOccupiedCell() {
        Game game = new Game(3, x, o);
        game.move("x", 0, 0);

        assertThrows(IllegalArgumentException.class, () -> game.move("o", 0, 0));
    }

    @Test
    void rejectsOutOfTurnMove() {
        Game game = new Game(3, x, o);

        assertThrows(IllegalArgumentException.class, () -> game.move("o", 0, 0));
    }
}
```

**Common mistakes:**
- Scanning the full board after every move when counters give O(1).
- Forgetting turn validation or accepting moves after a terminal state.
- Mixing scanner/console UI with the core game service.

**Follow-ups:**
- *How to support NxN?* → Constructor accepts size; counters already work for N in a row.
- *How to support AI?* → Add `MoveStrategy`; `Game` asks strategy for bot move.
- *How to support replay?* → Persist immutable `Move` events with timestamp and player ID.

---

### Q3. Design Snake & Ladder
`🟢 Beginner` `🔥` `Must-know SDE-1`

#### Beginner primer

**Simple problem:** Players take turns rolling a die and moving on a numbered board. Landing on a ladder
moves up; landing on a snake moves down. The first player to reach the final cell wins.

**Mental model:** `Game` controls turns, `Board` answers “where do I finally land?”, `Dice` generates a move,
and each `Player` has one position. Snakes and ladders are both a jump from one cell to another, so one map
models both.

**Thought process and patterns:** Define ambiguous rules first: must the player land exactly on the last cell,
can jumps chain, and are cycles forbidden? Use **Strategy** for dice or winning rules only if variants are
required. Inject randomness so tests can use a deterministic die.

**Flow and alternatives:** current player rolls → calculate tentative cell → handle overshoot according to
the chosen rule → follow jump map → update position → declare winner or advance turn. A chain of jump
objects is more object-oriented but a map is clearer for fixed jumps. Validate unique sources, in-range
destinations, snake/ladder direction, and absence of cycles.

**Responsibility map:**

| Class or interface | Responsibility | Key methods and contract | Relationships |
|---|---|---|---|
| `Player` record | Identifies one participant. | Canonical constructor stores id and name immutably; accessors return fields; no validation. | Used as queue element and key in `Game.positions`. |
| `Jump` record | Represents one snake or ladder endpoint pair. | Compact constructor validates positive, different endpoints, stores fields, and throws `IllegalArgumentException` for invalid jumps. | Passed to `Board`; read by `Board` constructor. |
| `TurnResult` record | Reports one completed turn. | Canonical constructor stores player, roll, from, to, and won flag; no mutation or validation. | Returned by `Game.playTurn` and used by demo/tests. |
| `Dice` interface | Abstracts dice rolling. | `roll()` takes no input, mutates according to implementation, returns roll value; failures are implementation-defined. | Implemented by `RandomDice`; injected into `Game` and test lambdas. |
| `RandomDice` class | Produces a random six-sided roll. | `roll()` reads random generator, returns 1 through 6, and has no explicit failure behavior. | Implements `Dice` for production-like play. |
| `Board` class | Applies board size, overshoot, and jump rules. | Constructor validates jumps against final cell and throws `IllegalArgumentException` for outside jumps; `nextPosition(int,int)` returns current cell on overshoot or jump destination; `finalCell()` returns target cell. | Used by `Game`; owns jump map from `Jump` records. |
| `Game` class | Owns turn queue, positions, and finished flag. | Constructor requires at least two players, initializes positions, and throws `IllegalArgumentException` otherwise; `playTurn()` is synchronized, rolls, moves, updates position or finished state, returns `TurnResult`, and throws `IllegalStateException` after finish; `position(Player)` returns stored cell. | Coordinates `Board`, `Dice`, `Player`, and `TurnResult`. |
| `Main` class | Demonstrates a playable loop. | `main(String[])` builds board, game, and players, loops until a winning turn, and prints progress; exceptions bubble up. | Wires `Board`, `Jump`, `RandomDice`, and `Game`. |
| `SnakeLadderTest` class | Verifies jumps, overshoot, and win behavior. | Test methods use deterministic dice lambdas and assertions; invalid post-win turn throws `IllegalStateException`; assertion failures fail tests. | Exercises records, `Board`, and `Game`. |

**Why they ask:** It checks turn queues, testable randomness, board rules, and clean separation of model from game loop.

**Short answer (say this first, 20-40 sec):**
Represent snakes and ladders as a map from source cell to destination cell. The `Game` keeps player positions
and a turn queue; a `Dice` strategy is injected so tests use fixed rolls and production uses random rolls.

**Detailed answer:**

#### 1. Clarifying questions & requirements
- Questions: Board size? Exact win or allow overshoot? One dice or multiple dice? Extra turn on six? Multiple players?
- Functional: create board with snakes/ladders; validate jumps; roll dice; move current player; apply jump;
  rotate turn; declare winner on exact final cell.
- Non-functional: deterministic tests; O(1) jump lookup; no random object hidden inside `Game`.
- Out of scope: UI animation, network multiplayer, persistence, anti-cheat.

#### 2. Entities & relationships
- `Board` owns final cell and jump map.
- `Jump` represents snake or ladder from start to end.
- `Dice` is a strategy.
- `Game` owns player queue and position map.

#### 3. Class diagram
```mermaid
classDiagram
class Game {
  -Board board
  -Dice dice
  -Queue~Player~ turns
  -Map~Player,Integer~ positions
  +playTurn() TurnResult
}
class Board {
  -int finalCell
  -Map~Integer,Integer~ jumps
  +nextPosition(int,int) int
}
class Dice {
  <<interface>>
  +roll() int
}
class RandomDice
class Player
class TurnResult
Game --> Board
Game --> Dice
Dice <|.. RandomDice
Game --> "2..*" Player
Board --> "0..*" Jump
```

#### 4. Main-flow sequence diagram
```mermaid
sequenceDiagram
participant Game
participant Dice
participant Board
Game->>Dice: roll()
Dice-->>Game: steps
Game->>Board: nextPosition(current, steps)
Board-->>Game: normal or jump destination
Game->>Game: update position
Game->>Game: winner or rotate queue
```

#### 5. Design patterns used & why
- Strategy: `Dice` supports random, fixed, loaded, or multi-dice behavior.
- Value objects: `Jump`, `Player`, and `TurnResult` are immutable records.
- Factory can build a standard board from configuration.

#### 6. Complete, compilable Java 17 code
```java
// snake/model/Models.java
package snake.model;

public record Player(String id, String name) { }

public record Jump(int from, int to) {
    public Jump {
        if (from <= 0 || to <= 0 || from == to) {
            throw new IllegalArgumentException("invalid jump");
        }
    }
}

public record TurnResult(Player player, int roll, int from, int to, boolean won) { }
```

```java
// snake/service/Game.java
package snake.service;

import snake.model.*;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

public interface Dice {
    int roll();
}

public final class RandomDice implements Dice {
    private final Random random = new Random();

    @Override
    public int roll() {
        return 1 + random.nextInt(6);
    }
}

public final class Board {
    private final int finalCell;
    private final Map<Integer, Integer> jumps = new HashMap<>();

    public Board(int finalCell, List<Jump> jumps) {
        this.finalCell = finalCell;
        for (Jump jump : jumps) {
            if (jump.from() >= finalCell || jump.to() > finalCell) {
                throw new IllegalArgumentException("jump outside board");
            }
            this.jumps.put(jump.from(), jump.to());
        }
    }

    public int nextPosition(int current, int roll) {
        int candidate = current + roll;
        if (candidate > finalCell) {
            return current;
        }
        return jumps.getOrDefault(candidate, candidate);
    }

    public int finalCell() {
        return finalCell;
    }
}

public final class Game {
    private final Board board;
    private final Dice dice;
    private final Queue<Player> turns = new ArrayDeque<>();
    private final Map<Player, Integer> positions = new HashMap<>();
    private boolean finished;

    public Game(Board board, Dice dice, List<Player> players) {
        if (players.size() < 2) {
            throw new IllegalArgumentException("need at least two players");
        }
        this.board = board;
        this.dice = dice;
        turns.addAll(players);
        players.forEach(player -> positions.put(player, 0));
    }

    public synchronized TurnResult playTurn() {
        if (finished) {
            throw new IllegalStateException("game already finished");
        }
        Player player = turns.remove();
        int from = positions.get(player);
        int roll = dice.roll();
        int to = board.nextPosition(from, roll);
        positions.put(player, to);
        boolean won = to == board.finalCell();
        if (won) {
            finished = true;
        } else {
            turns.add(player);
        }
        return new TurnResult(player, roll, from, to, won);
    }

    public int position(Player player) {
        return positions.get(player);
    }
}
```

```java
// snake/Main.java
package snake;

import snake.model.*;
import snake.service.*;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        Board board = new Board(30, List.of(new Jump(3, 22), new Jump(27, 1)));
        Game game = new Game(
                board,
                new RandomDice(),
                List.of(new Player("p1", "Azam"), new Player("p2", "Bob")));

        while (!game.playTurn().won()) {
            System.out.println("next turn");
        }
    }
}
```

#### 7. Thread-safety considerations
- `playTurn` is synchronized because queue rotation and position update must be atomic.
- In a web game, store each game instance behind a per-game lock rather than one global lock.
- Dice strategy should be thread-safe or used only inside the synchronized method.

#### 8. Extensibility
- Add extra turn on six: `Game.playTurn` checks roll and skips queue rotation.
- Add multiple dice: implement `Dice` that sums multiple dice.
- Add board generator: `BoardFactory` validates jump endpoints and no duplicate sources.
- Add move history: append each `TurnResult` to an immutable event list.

#### 9. JUnit 5 tests
```java
package snake;

import org.junit.jupiter.api.Test;
import snake.model.*;
import snake.service.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SnakeLadderTest {
    @Test
    void ladderMovesPlayerToDestination() {
        Player a = new Player("a", "A");
        Player b = new Player("b", "B");
        Game game = new Game(new Board(30, List.of(new Jump(3, 22))), () -> 3, List.of(a, b));

        TurnResult result = game.playTurn();

        assertEquals(22, result.to());
        assertEquals(22, game.position(a));
    }

    @Test
    void overshootKeepsSamePosition() {
        Player a = new Player("a", "A");
        Player b = new Player("b", "B");
        Game game = new Game(new Board(5, List.of()), () -> 6, List.of(a, b));

        assertEquals(0, game.playTurn().to());
    }

    @Test
    void exactFinalCellWins() {
        Player a = new Player("a", "A");
        Player b = new Player("b", "B");
        Game game = new Game(new Board(3, List.of()), () -> 3, List.of(a, b));

        assertTrue(game.playTurn().won());
        assertThrows(IllegalStateException.class, game::playTurn);
    }
}
```

**Common mistakes:**
- Creating `new Random()` directly inside `Game`, making tests flaky.
- Forgetting overshoot behavior.
- Modeling every cell as an object when a jump map is enough.

**Follow-ups:**
- *How do you avoid snake-ladder cycles?* → Validate the directed jump graph or trust configured boards.
- *How do you rank all players?* → Continue after first winner and record finish order.
- *How do you support custom rules?* → Add `RuleSet` strategy for extra turns, overshoot, and win policy.

---

### Q4. Design LRU Cache and LFU Cache
`🟡 Intermediate` `🔥` `Must-know SDE-1`

#### Beginner primer

**Terminology:** Least Recently Used (LRU) evicts the item not accessed for the longest time. Least
Frequently Used (LFU) evicts the item with the smallest access count; recency usually breaks frequency ties.

**Mental model:** A hash map answers “where is this key?” quickly. A doubly linked list answers “which item
is oldest?” quickly. LRU combines them, so each access moves one node to the front and eviction removes the
tail. LFU adds frequency buckets and tracks the smallest non-empty frequency.

**Responsibilities and invariants:** `Node` stores key/value and links; the cache owns the map, sentinel
nodes, capacity, and mutation methods. For LFU, the key map, frequency lists, and `minFrequency` must agree
after every operation. `get` both returns a value and updates usage; `put` inserts/updates and may evict.

**Alternatives and scale:** This is primarily a data-structure design, so do not force a Gang of Four
pattern. Java `LinkedHashMap` is best when library use is allowed; manual nodes demonstrate constant-time
behavior. One lock is clearer than lock-free pointer updates. A distributed cache adds partitioning,
replication, expiry, and consistency and belongs in High-Level Design.

**Responsibility map:**

| Class or interface | Responsibility | Key methods and contract | Relationships |
|---|---|---|---|
| `Cache<K,V>` interface | Defines common cache operations. | `get(K)` returns `Optional<V>` and may update policy metadata; `put(K,V)` inserts or updates and may evict; failure behavior is implementation-defined. | Implemented by `LruCache` and `LfuCache`. |
| `LruCache.Node<K,V>` class | Stores one LRU entry and list links. | Private constructor stores key and value; fields are mutated by list helpers; no public methods or explicit failures. | Owned only by `LruCache`; linked between sentinel nodes. |
| `LruCache<K,V>` class | Implements least-recently-used eviction. | Constructor validates non-negative capacity; synchronized `get` returns optional value and moves hit to front; synchronized `put` ignores capacity zero, updates or inserts, evicts tail when full; helpers mutate links; negative capacity throws `IllegalArgumentException`. | Implements `Cache`; owns map, sentinel nodes, and `Node` objects. |
| `LfuCache<K,V>` class | Implements least-frequently-used eviction with recency tie-break. | Constructor validates non-negative capacity; synchronized `get` returns optional value and increases frequency; synchronized `put` ignores zero capacity, updates or inserts, evicts oldest key in min-frequency bucket; negative capacity throws `IllegalArgumentException`. | Implements `Cache`; owns value map, frequency map, buckets, and `minFrequency`. |
| `Main` class | Demonstrates LRU usage. | `main(String[])` creates cache, calls `put` and `get`, prints optional result; exceptions bubble up. | Uses `Cache` interface with `LruCache` implementation. |
| `CacheTest` class | Verifies LRU, LFU, and LFU tie-breaking. | Test methods perform cache operations and assert expected empties or values; assertion failures fail tests. | Exercises both `Cache` implementations through the interface. |

**Why they ask:** It validates O(1) data-structure design, generic APIs, edge cases, and eviction correctness.

**Short answer (say this first, 20-40 sec):**
LRU uses a hash map from key to doubly-linked-list node; get and put move the node to the head in O(1),
and eviction removes the tail. LFU uses key-to-node maps plus frequency buckets; each bucket keeps recency
with a linked set so eviction removes the oldest key in the minimum frequency bucket in O(1).

**Detailed answer:**

#### 1. Clarifying questions & requirements
- Questions: What is capacity zero behavior? Should `get` update recency/frequency? How to break LFU ties?
  Need TTL, metrics, or thread safety?
- Functional: generic `get`/`put`; O(1) average operations; LRU evicts least recently used; LFU evicts lowest
  frequency and oldest within that frequency; update existing key.
- Non-functional: synchronized public methods for thread safety; no use of `LinkedHashMap` for LRU if asked to
  implement nodes; tests for capacity zero and tie-breaks.
- Out of scope: distributed cache, persistence, expiration, admission policies.

#### 2. Entities & relationships
- `Cache<K,V>` is the common interface.
- `LruCache` owns a map and doubly linked list.
- `LfuCache` owns value map, frequency map, frequency buckets, and `minFrequency`.

#### 3. Class diagram
```mermaid
classDiagram
class Cache~K,V~ {
  <<interface>>
  +get(K) Optional~V~
  +put(K,V) void
}
class LruCache~K,V~ {
  -Map~K,Node~ map
  -Node head
  -Node tail
}
class LfuCache~K,V~ {
  -Map~K,V~ values
  -Map~K,Integer~ frequencies
  -Map~Integer,LinkedHashSet~ buckets
  -int minFrequency
}
class Node
Cache <|.. LruCache
Cache <|.. LfuCache
LruCache *-- Node
```

#### 4. Main-flow sequence diagram
```mermaid
sequenceDiagram
actor Client
participant Cache
Client->>Cache: put(1, A)
Client->>Cache: put(2, B)
Client->>Cache: get(1)
Cache-->>Client: A and update policy metadata
Client->>Cache: put(3, C)
Cache->>Cache: evict according to LRU or LFU
```

#### 5. Design patterns used & why
- Strategy: both implementations satisfy the same `Cache<K,V>` contract.
- Encapsulation: policy metadata is hidden behind `get`/`put`.
- Decorator can add metrics or synchronization around any cache.

#### 6. Complete, compilable Java 17 code
```java
// cache/Cache.java
package cache;

import java.util.Optional;

public interface Cache<K, V> {
    Optional<V> get(K key);

    void put(K key, V value);
}
```

```java
// cache/LruCache.java
package cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class LruCache<K, V> implements Cache<K, V> {
    private static final class Node<K, V> {
        private final K key;
        private V value;
        private Node<K, V> prev;
        private Node<K, V> next;

        private Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private final int capacity;
    private final Map<K, Node<K, V>> map = new HashMap<>();
    private final Node<K, V> head = new Node<>(null, null);
    private final Node<K, V> tail = new Node<>(null, null);

    public LruCache(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity cannot be negative");
        }
        this.capacity = capacity;
        head.next = tail;
        tail.prev = head;
    }

    @Override
    public synchronized Optional<V> get(K key) {
        Node<K, V> node = map.get(key);
        if (node == null) {
            return Optional.empty();
        }
        moveToFront(node);
        return Optional.ofNullable(node.value);
    }

    @Override
    public synchronized void put(K key, V value) {
        if (capacity == 0) {
            return;
        }
        Node<K, V> existing = map.get(key);
        if (existing != null) {
            existing.value = value;
            moveToFront(existing);
            return;
        }
        if (map.size() == capacity) {
            Node<K, V> victim = tail.prev;
            remove(victim);
            map.remove(victim.key);
        }
        Node<K, V> node = new Node<>(key, value);
        map.put(key, node);
        addAfterHead(node);
    }

    private void moveToFront(Node<K, V> node) {
        remove(node);
        addAfterHead(node);
    }

    private void addAfterHead(Node<K, V> node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }

    private void remove(Node<K, V> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }
}
```

```java
// cache/LfuCache.java
package cache;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;

public final class LfuCache<K, V> implements Cache<K, V> {
    private final int capacity;
    private int minFrequency;
    private final Map<K, V> values = new HashMap<>();
    private final Map<K, Integer> frequencies = new HashMap<>();
    private final Map<Integer, LinkedHashSet<K>> buckets = new HashMap<>();

    public LfuCache(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity cannot be negative");
        }
        this.capacity = capacity;
    }

    @Override
    public synchronized Optional<V> get(K key) {
        if (!values.containsKey(key)) {
            return Optional.empty();
        }
        increaseFrequency(key);
        return Optional.ofNullable(values.get(key));
    }

    @Override
    public synchronized void put(K key, V value) {
        if (capacity == 0) {
            return;
        }
        if (values.containsKey(key)) {
            values.put(key, value);
            increaseFrequency(key);
            return;
        }
        if (values.size() == capacity) {
            LinkedHashSet<K> minBucket = buckets.get(minFrequency);
            K victim = minBucket.iterator().next();
            minBucket.remove(victim);
            values.remove(victim);
            frequencies.remove(victim);
        }
        values.put(key, value);
        frequencies.put(key, 1);
        buckets.computeIfAbsent(1, ignored -> new LinkedHashSet<>()).add(key);
        minFrequency = 1;
    }

    private void increaseFrequency(K key) {
        int oldFrequency = frequencies.get(key);
        LinkedHashSet<K> oldBucket = buckets.get(oldFrequency);
        oldBucket.remove(key);
        if (oldFrequency == minFrequency && oldBucket.isEmpty()) {
            minFrequency++;
        }
        int newFrequency = oldFrequency + 1;
        frequencies.put(key, newFrequency);
        buckets.computeIfAbsent(newFrequency, ignored -> new LinkedHashSet<>()).add(key);
    }
}
```

```java
// cache/Main.java
package cache;

public class Main {
    public static void main(String[] args) {
        Cache<Integer, String> cache = new LruCache<>(2);
        cache.put(1, "one");
        cache.put(2, "two");
        cache.get(1);
        cache.put(3, "three");

        System.out.println(cache.get(2)); // Optional.empty
    }
}
```

#### 7. Thread-safety considerations
- Public `get` and `put` are synchronized because they mutate order/frequency metadata.
- For high throughput, use segmented locks or rely on production libraries like Caffeine.
- Avoid returning internal node/bucket references.

#### 8. Extensibility
- Add TTL: wrap value in `CacheEntry` with `expiresAt`; purge lazily on access.
- Add metrics: decorate `Cache` and increment hit/miss/eviction counters.
- Add max weight: replace count capacity with `Weigher<K,V>` and current weight.
- Add write-through: decorate `put` to also update a repository.

#### 9. JUnit 5 tests
```java
package cache;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CacheTest {
    @Test
    void lruEvictsLeastRecentlyUsed() {
        Cache<Integer, String> cache = new LruCache<>(2);
        cache.put(1, "one");
        cache.put(2, "two");
        cache.get(1);
        cache.put(3, "three");

        assertTrue(cache.get(2).isEmpty());
        assertEquals("one", cache.get(1).orElseThrow());
    }

    @Test
    void lfuEvictsLowestFrequency() {
        Cache<Integer, String> cache = new LfuCache<>(2);
        cache.put(1, "one");
        cache.put(2, "two");
        cache.get(1);
        cache.put(3, "three");

        assertTrue(cache.get(2).isEmpty());
        assertEquals("one", cache.get(1).orElseThrow());
    }

    @Test
    void lfuBreaksTieByOldestWithinFrequency() {
        Cache<Integer, String> cache = new LfuCache<>(2);
        cache.put(1, "one");
        cache.put(2, "two");
        cache.put(3, "three");

        assertTrue(cache.get(1).isEmpty());
        assertEquals("two", cache.get(2).orElseThrow());
    }
}
```

**Common mistakes:**
- Updating LRU order on `put` but not on `get`.
- LFU without recency tie-break inside a frequency bucket.
- Forgetting capacity zero.

**Follow-ups:**
- *Why not just use `LinkedHashMap`?* → It is fine in practice, but interviews test node/list mechanics.
- *How do you make it distributed?* → Put policy state in Redis or use a dedicated distributed cache.
- *What is LFU aging?* → Periodically decay frequencies so old hot keys do not dominate forever.

---

### Q5. Design Rate Limiter
`🟡 Intermediate` `🔥` `Must-know SDE-1`

#### Beginner primer

**Simple problem:** Before an operation runs, decide whether a client is within an allowed request rate.
Concurrent requests must never overspend the same allowance.

**Mental model:** A token bucket has fixed capacity. Time adds tokens up to that capacity; each accepted
request removes one. Capacity permits a burst, while refill rate controls the long-term average.
`RateLimiter` locates a client bucket, `Bucket` owns token math, and `TimeProvider` isolates the clock.

**Pattern recognition and flow:** Use **Strategy** when plans may select token bucket, fixed window, or
sliding window. Find bucket → calculate elapsed time → add `elapsed × refillRate`, capped at capacity → if
one token remains, subtract and allow; otherwise reject.

**Alternatives and scale:** Fixed window is simplest but allows boundary bursts. Sliding log is accurate but
stores timestamps. Token bucket balances memory and burst support. A synchronized bucket works in one Java
Virtual Machine (JVM). Multiple instances need one atomic authority, often a Redis script or database
update, plus Time To Live (TTL) cleanup. Decide whether shared-limiter failure fails open or closed.

**Responsibility map:**

| Class or interface | Responsibility | Key methods and contract | Relationships |
|---|---|---|---|
| `RateLimiter` interface | Defines the allow-or-reject decision. | `allow(String)` takes a client id, may mutate per-client state, returns true for accepted and false for rejected; validation is implementation-defined. | Implemented by token bucket and sliding-window limiters. |
| `TimeProvider` interface | Abstracts current time. | `millis()` takes no input, changes no required state, returns current time in milliseconds; failure behavior is implementation-defined. | Implemented by `SystemTimeProvider` and test fake clock. |
| `SystemTimeProvider` class | Uses system wall-clock time. | `millis()` returns `System.currentTimeMillis()` and has no explicit failure behavior. | Injected into production limiter examples. |
| `TokenBucketRateLimiter.Bucket` class | Holds mutable token state for one client. | Private constructor stores token count and last refill time; fields mutate inside synchronized bucket block; no public methods. | Stored in `TokenBucketRateLimiter.buckets`. |
| `TokenBucketRateLimiter` class | Enforces burst plus refill limits per client. | Constructor validates positive capacity and refill; `allow` creates or locks a bucket, refills tokens by elapsed time, subtracts one token on success, returns false when empty, and guards negative elapsed time with zero. | Implements `RateLimiter`; depends on `TimeProvider` and `Bucket`. |
| `SlidingWindowLogRateLimiter` class | Enforces exact recent request count per client. | Constructor validates positive limit and window; `allow` creates or locks a timestamp deque, removes expired entries, returns false at limit, otherwise appends current time and returns true. | Implements `RateLimiter`; depends on `TimeProvider` and per-client deques. |
| `Main` class | Demonstrates token bucket decisions. | `main(String[])` builds limiter, calls `allow`, and prints booleans; constructor or runtime exceptions bubble up. | Uses `RateLimiter`, `TokenBucketRateLimiter`, and `SystemTimeProvider`. |
| `RateLimiterTest.FakeClock` class | Provides deterministic test time. | `millis()` returns stored time; `advance(long)` mutates time by input milliseconds and returns void; no explicit validation. | Implements `TimeProvider` for tests. |
| `RateLimiterTest` class | Verifies burst, refill, strict window, and client isolation. | Test methods call `allow`, advance fake time, and assert booleans; assertion failures fail tests. | Exercises both limiter implementations and `FakeClock`. |

**Why they ask:** It tests algorithm choice, per-client state, time handling, and thread-safe mutation.

**Short answer (say this first, 20-40 sec):**
Expose a `RateLimiter.allow(clientId)` interface. Token bucket keeps refillable tokens per client and allows
bursts up to capacity; sliding-window-log keeps recent timestamps per client and enforces a strict count in
the last window. Both use a concurrent map and per-client synchronization.

**Detailed answer:**

#### 1. Clarifying questions & requirements
- Questions: Limit per user, IP, API key, or endpoint? Burst allowed? Single JVM or distributed? What response
  metadata is needed? What time precision?
- Functional: support token bucket and sliding-window-log limiters; per-client state; deterministic clock;
  thread-safe `allow`; reject invalid capacities.
- Non-functional: no global lock for all clients; O(1) token bucket; sliding window proportional to hits in window.
- Out of scope: Redis cluster implementation, HTTP middleware, persistent analytics.

#### 2. Entities & relationships
- `RateLimiter` is the strategy interface.
- `TokenBucketRateLimiter` stores one `Bucket` per client.
- `SlidingWindowLogRateLimiter` stores one timestamp deque per client.
- `TimeProvider` makes tests deterministic.

#### 3. Class diagram
```mermaid
classDiagram
class RateLimiter {
  <<interface>>
  +allow(String) boolean
}
class TokenBucketRateLimiter {
  -ConcurrentHashMap~String,Bucket~ buckets
  +allow(String) boolean
}
class SlidingWindowLogRateLimiter {
  -ConcurrentHashMap~String,Deque~Long~~ windows
  +allow(String) boolean
}
class TimeProvider {
  <<interface>>
  +millis() long
}
class Bucket {
  -double tokens
  -long lastRefillMillis
}
RateLimiter <|.. TokenBucketRateLimiter
RateLimiter <|.. SlidingWindowLogRateLimiter
TokenBucketRateLimiter --> TimeProvider
SlidingWindowLogRateLimiter --> TimeProvider
TokenBucketRateLimiter *-- Bucket
```

#### 4. Main-flow sequence diagram
```mermaid
sequenceDiagram
actor Client
participant Limiter as RateLimiter
participant Clock as TimeProvider
Client->>Limiter: allow(clientId)
Limiter->>Clock: millis()
Limiter->>Limiter: refill or trim old entries
Limiter-->>Client: allowed or rejected
```

#### 5. Design patterns used & why
- Strategy: token bucket and sliding window are interchangeable.
- Dependency Injection: `TimeProvider` avoids sleeping in tests.
- Factory: API gateway can choose limiter by tenant plan.

#### 6. Complete, compilable Java 17 code
```java
// ratelimiter/RateLimiter.java
package ratelimiter;

public interface RateLimiter {
    boolean allow(String clientId);
}

public interface TimeProvider {
    long millis();
}

public final class SystemTimeProvider implements TimeProvider {
    @Override
    public long millis() {
        return System.currentTimeMillis();
    }
}
```

```java
// ratelimiter/TokenBucketRateLimiter.java
package ratelimiter;

import java.util.concurrent.ConcurrentHashMap;

public final class TokenBucketRateLimiter implements RateLimiter {
    private static final class Bucket {
        private double tokens;
        private long lastRefillMillis;

        private Bucket(double tokens, long lastRefillMillis) {
            this.tokens = tokens;
            this.lastRefillMillis = lastRefillMillis;
        }
    }

    private final int capacity;
    private final double refillTokensPerMillis;
    private final TimeProvider clock;
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public TokenBucketRateLimiter(int capacity, double refillTokensPerSecond, TimeProvider clock) {
        if (capacity <= 0 || refillTokensPerSecond <= 0) {
            throw new IllegalArgumentException("capacity and refill must be positive");
        }
        this.capacity = capacity;
        this.refillTokensPerMillis = refillTokensPerSecond / 1000.0;
        this.clock = clock;
    }

    @Override
    public boolean allow(String clientId) {
        Bucket bucket = buckets.computeIfAbsent(
                clientId,
                ignored -> new Bucket(capacity, clock.millis()));
        synchronized (bucket) {
            long now = clock.millis();
            long elapsed = Math.max(0, now - bucket.lastRefillMillis);
            bucket.tokens = Math.min(capacity, bucket.tokens + elapsed * refillTokensPerMillis);
            bucket.lastRefillMillis = now;
            if (bucket.tokens < 1.0) {
                return false;
            }
            bucket.tokens -= 1.0;
            return true;
        }
    }
}
```

```java
// ratelimiter/SlidingWindowLogRateLimiter.java
package ratelimiter;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

public final class SlidingWindowLogRateLimiter implements RateLimiter {
    private final int limit;
    private final long windowMillis;
    private final TimeProvider clock;
    private final ConcurrentHashMap<String, Deque<Long>> windows = new ConcurrentHashMap<>();

    public SlidingWindowLogRateLimiter(int limit, long windowMillis, TimeProvider clock) {
        if (limit <= 0 || windowMillis <= 0) {
            throw new IllegalArgumentException("limit and window must be positive");
        }
        this.limit = limit;
        this.windowMillis = windowMillis;
        this.clock = clock;
    }

    @Override
    public boolean allow(String clientId) {
        Deque<Long> timestamps = windows.computeIfAbsent(clientId, ignored -> new ArrayDeque<>());
        synchronized (timestamps) {
            long now = clock.millis();
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() >= windowMillis) {
                timestamps.removeFirst();
            }
            if (timestamps.size() >= limit) {
                return false;
            }
            timestamps.addLast(now);
            return true;
        }
    }
}
```

```java
// ratelimiter/Main.java
package ratelimiter;

public class Main {
    public static void main(String[] args) {
        RateLimiter limiter = new TokenBucketRateLimiter(2, 1.0, new SystemTimeProvider());

        System.out.println(limiter.allow("azam"));
        System.out.println(limiter.allow("azam"));
        System.out.println(limiter.allow("azam"));
    }
}
```

#### 7. Thread-safety considerations
- `ConcurrentHashMap` isolates client state lookup; synchronizing on each bucket/window avoids global blocking.
- Token refill and token consumption are inside the same monitor.
- Sliding-window trim, size check, and append are inside the same monitor.
- Production cleanup should remove idle clients to avoid unbounded memory.

#### 8. Extensibility
- Add fixed window: implement `RateLimiter` with counter and window start.
- Add tier limits: create `PolicyResolver` returning capacity/window per client.
- Add distributed Redis: use a Lua script to atomically refill/check/update.
- Add HTTP headers: return `RateLimitDecision` instead of boolean.

#### 9. JUnit 5 tests
```java
package ratelimiter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTest {
    private static final class FakeClock implements TimeProvider {
        private long now;

        @Override
        public long millis() {
            return now;
        }

        void advance(long millis) {
            now += millis;
        }
    }

    @Test
    void tokenBucketAllowsBurstThenRefills() {
        FakeClock clock = new FakeClock();
        RateLimiter limiter = new TokenBucketRateLimiter(2, 1.0, clock);

        assertTrue(limiter.allow("u"));
        assertTrue(limiter.allow("u"));
        assertFalse(limiter.allow("u"));
        clock.advance(1000);
        assertTrue(limiter.allow("u"));
    }

    @Test
    void slidingWindowRejectsStrictRecentCount() {
        FakeClock clock = new FakeClock();
        RateLimiter limiter = new SlidingWindowLogRateLimiter(2, 1000, clock);

        assertTrue(limiter.allow("u"));
        assertTrue(limiter.allow("u"));
        assertFalse(limiter.allow("u"));
        clock.advance(1000);
        assertTrue(limiter.allow("u"));
    }

    @Test
    void clientsHaveIndependentBudgets() {
        FakeClock clock = new FakeClock();
        RateLimiter limiter = new TokenBucketRateLimiter(1, 1.0, clock);

        assertTrue(limiter.allow("a"));
        assertFalse(limiter.allow("a"));
        assertTrue(limiter.allow("b"));
    }
}
```

**Common mistakes:**
- Global lock around all clients.
- Integer refill that loses fractional tokens.
- Tests using `Thread.sleep` instead of an injected clock.

**Follow-ups:**
- *Token bucket vs sliding window?* → Token bucket allows burst; sliding log is stricter but stores timestamps.
- *How to distribute it?* → Use Redis atomic Lua or API gateway rate-limit primitives.
- *How to avoid memory leak?* → Evict client state after idle TTL or run periodic cleanup.

## Revision Cheat Sheet

- Parking Lot: floor → spot → ticket → payment; lock allocation and release; strategies for allocation/pricing.
- Tic-Tac-Toe: use row/column/diagonal counters for O(1) win detection.
- Snake & Ladder: jump map plus injected dice; exact win keeps overshoot at current cell.
- LRU: hash map plus doubly linked list; get and put both update recency.
- LFU: frequency buckets plus `minFrequency`; ties evict oldest in min bucket.
- Rate Limiter: token bucket permits bursts; sliding-window-log enforces recent request count.
- In machine coding, deterministic tests and clean service APIs matter more than console UI.

## Flashcards

1. Parking allocation race? → Lock allocation and spot occupancy update atomically.
2. Parking EV support? → Add `SpotType.EV` and pricing/charging strategy.
3. Tic-Tac-Toe O(1) win? → Maintain row, column, diagonal counters.
4. Tic-Tac-Toe invalid move? → Reject out-of-bounds, occupied, wrong turn, and finished game.
5. Snake & Ladder testability? → Inject `Dice`.
6. Snake overshoot rule? → Keep current position unless problem says otherwise.
7. LRU core structure? → Hash map from key to node plus doubly linked list.
8. LFU O(1) tie-break? → `LinkedHashSet` per frequency.
9. Cache capacity zero? → `put` is no-op and `get` is empty.
10. Token bucket state? → Capacity, tokens, refill rate, last refill time.
11. Sliding-window-log state? → Deque of timestamps per client.
12. Rate limiter concurrency? → Concurrent map plus per-client monitor.
