# 10c · LLD Problems Tier-1 Continued: Machine Coding Study Guide

Continuation of Tier-1 LLD machine-coding problems. Code is Java 17, intentionally in-memory, and written
as package sections; each public top-level type should be placed in its own `.java` file with the same name.

**Table of Contents**
- [Q1. Design Logger](#q1-design-logger)
- [Q2. Design Vending Machine](#q2-design-vending-machine)
- [Q3. Design ATM](#q3-design-atm)
- [Q4. Design Library Management](#q4-design-library-management)
- [Q5. Design Elevator System](#q5-design-elevator-system)
- [Revision Cheat Sheet](#revision-cheat-sheet)
- [Flashcards](#flashcards)

🟢 Beginner · 🟡 Intermediate · 🔴 Advanced · ⚫ FAANG · 🔥 = very frequently asked

---

### Q1. Design Logger
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Logger checks Chain of Responsibility, formatting, output abstraction, and safe concurrent writes.

**Short answer (say this first, 20-40 sec):**
Expose a `Logger` facade that creates `LogEvent` objects. A level-filtering handler chain decides whether an
event should be written, then forwards it to sink strategies like console or memory. Formatter and sink are
injected so JSON, file, async, or multi-sink logging can be added without changing logger APIs.

**Detailed answer:**

#### 1. Clarifying questions & requirements
- Questions: Which levels? Should one event go to multiple sinks? Sync or async writes? Need timestamps,
  correlation IDs, file rotation, or dynamic level changes?
- Functional: log DEBUG/INFO/WARN/ERROR; apply threshold; format message; write to one or more sinks; support
  in-memory sink for tests.
- Non-functional: thread-safe sink writes; no global hidden singleton in core tests; bounded async queue if added.
- Out of scope: distributed tracing, log shipping, log retention policies.

#### 2. Entities & relationships
- `Logger` facade receives application calls.
- `LogEvent` carries immutable level, message, and timestamp.
- `LogHandler` forms a chain.
- `LogSink` writes formatted events.
- `LogFormatter` controls output shape.

#### 3. Class diagram
```mermaid
classDiagram
class Logger {
  -LogHandler handler
  +info(String) void
  +error(String) void
  +log(Level,String) void
}
class LogHandler {
  <<abstract>>
  -LogHandler next
  +handle(LogEvent) void
}
class ThresholdHandler
class LogSink {
  <<interface>>
  +write(String) void
}
class LogFormatter {
  <<interface>>
  +format(LogEvent) String
}
class LogEvent
Logger --> LogHandler
LogHandler --> LogHandler : next
ThresholdHandler --> LogSink
ThresholdHandler --> LogFormatter
LogSink <|.. ConsoleSink
LogSink <|.. MemorySink
```

#### 4. Main-flow sequence diagram
```mermaid
sequenceDiagram
actor App
participant Logger
participant Handler as ThresholdHandler
participant Formatter
participant Sink
App->>Logger: error(message)
Logger->>Handler: handle(LogEvent)
Handler->>Formatter: format(event)
Formatter-->>Handler: line
Handler->>Sink: write(line)
```

#### 5. Design patterns used & why
- Chain of Responsibility: multiple handlers can filter or route by level/module.
- Strategy: `LogSink` and `LogFormatter` vary independently.
- Facade: callers use `Logger.info` instead of knowing handlers/sinks.
- Singleton is optional for app configuration; avoid hard-coded static singleton in tests.

#### 6. Complete, compilable Java 17 code
```java
// logger/model package
package logger.model;

import java.time.Instant;

public enum Level { DEBUG, INFO, WARN, ERROR }

public record LogEvent(Level level, String message, Instant timestamp) { }
```

```java
// logger/service package
package logger.service;

import logger.model.*;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

public interface LogSink {
    void write(String line);
}

public final class ConsoleSink implements LogSink {
    @Override
    public synchronized void write(String line) {
        System.out.println(line);
    }
}

public final class MemorySink implements LogSink {
    private final List<String> lines = new ArrayList<>();

    @Override
    public synchronized void write(String line) {
        lines.add(line);
    }

    public synchronized List<String> lines() {
        return List.copyOf(lines);
    }
}

public interface LogFormatter {
    String format(LogEvent event);
}

public final class TextFormatter implements LogFormatter {
    @Override
    public String format(LogEvent event) {
        return event.timestamp() + " " + event.level() + " " + event.message();
    }
}

public abstract class LogHandler {
    private LogHandler next;

    public LogHandler link(LogHandler handler) {
        this.next = handler;
        return handler;
    }

    public final void handle(LogEvent event) {
        process(event);
        if (next != null) {
            next.handle(event);
        }
    }

    protected abstract void process(LogEvent event);
}

public final class ThresholdHandler extends LogHandler {
    private final Level threshold;
    private final LogSink sink;
    private final LogFormatter formatter;

    public ThresholdHandler(Level threshold, LogSink sink, LogFormatter formatter) {
        this.threshold = threshold;
        this.sink = sink;
        this.formatter = formatter;
    }

    @Override
    protected void process(LogEvent event) {
        if (event.level().ordinal() >= threshold.ordinal()) {
            sink.write(formatter.format(event));
        }
    }
}

public final class Logger {
    private final LogHandler handler;
    private final Clock clock;

    public Logger(LogHandler handler, Clock clock) {
        this.handler = handler;
        this.clock = clock;
    }

    public void debug(String message) {
        log(Level.DEBUG, message);
    }

    public void info(String message) {
        log(Level.INFO, message);
    }

    public void error(String message) {
        log(Level.ERROR, message);
    }

    public void log(Level level, String message) {
        handler.handle(new LogEvent(level, message, clock.instant()));
    }
}
```

```java
// logger/Main.java
package logger;

import logger.model.Level;
import logger.service.*;

import java.time.Clock;

public class Main {
    public static void main(String[] args) {
        Logger logger = new Logger(
                new ThresholdHandler(Level.INFO, new ConsoleSink(), new TextFormatter()),
                Clock.systemUTC());

        logger.info("application started");
        logger.debug("hidden debug line");
    }
}
```

#### 7. Thread-safety considerations
- `MemorySink.lines` and `ConsoleSink.write` are synchronized because sinks are shared mutable resources.
- If async logging is added, use a bounded `BlockingQueue` and a shutdown/flush method.
- Dynamic threshold updates should use `volatile` or an atomic reference.

#### 8. Extensibility
- Add file sink: implement `LogSink` with rotation lock around file handle.
- Add JSON output: implement `LogFormatter`.
- Add async logging: `AsyncSink` decorates another sink and drains a queue.
- Add per-class log levels: add `LoggerFactory` that maps logger name to threshold.

#### 9. JUnit 5 tests
```java
package logger;

import logger.model.Level;
import logger.service.*;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class LoggerTest {
    @Test
    void thresholdSuppressesDebug() {
        MemorySink sink = new MemorySink();
        Logger logger = new Logger(
                new ThresholdHandler(Level.INFO, sink, new TextFormatter()),
                Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));

        logger.debug("debug");
        logger.info("info");

        assertEquals(1, sink.lines().size());
        assertTrue(sink.lines().get(0).contains("INFO"));
    }

    @Test
    void errorPassesInfoThreshold() {
        MemorySink sink = new MemorySink();
        Logger logger = new Logger(
                new ThresholdHandler(Level.INFO, sink, new TextFormatter()),
                Clock.systemUTC());

        logger.error("boom");

        assertTrue(sink.lines().get(0).contains("boom"));
    }
}
```

**Common mistakes:**
- Hard-coding `System.out.println` in every caller.
- Treating Singleton as the whole design.
- Ignoring file/memory sink thread safety.

**Follow-ups:**
- *How do you log to console and file?* → Chain two handlers or use a composite sink.
- *How do you avoid blocking request threads?* → Async sink with bounded queue and back-pressure policy.
- *How do you add trace ID?* → Add context fields to `LogEvent` or use mapped diagnostic context.

---

### Q2. Design Vending Machine
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** It tests state transitions, inventory, payment/change handling, and invalid operation behavior.

**Short answer (say this first, 20-40 sec):**
Use a State pattern. The machine has inventory, cash box, selected slot, paid amount, and states such as idle,
selected, and paid. Inventory and cash operations are synchronized through the machine so selection, payment,
dispense, and refund remain consistent.

**Detailed answer:**

#### 1. Clarifying questions & requirements
- Questions: Cash only or card/UPI? Should machine return exact change? Can user cancel? Multiple units per slot?
  What happens when selected item goes out of stock mid-flow?
- Functional: add products; select item; insert coins/notes; reject invalid actions; dispense item; return change;
  refund before dispense.
- Non-functional: one customer session per machine; deterministic money calculations in integer paise/cents;
  state-specific errors.
- Out of scope: hardware drivers, remote inventory sync, QR payment callbacks.

#### 2. Entities & relationships
- `VendingMachine` owns `Inventory`, `CashBox`, current `MachineState`, selection, and paid amount.
- `Inventory` maps code to product and stock.
- `CashBox` tracks accepted cash and makes change.
- `MachineState` validates allowed operations.

#### 3. Class diagram
```mermaid
classDiagram
class VendingMachine {
  -MachineState state
  -Inventory inventory
  -CashBox cashBox
  -String selectedCode
  -int paid
  +select(String) void
  +insert(Money) void
  +dispense() DispenseResult
  +cancel() int
}
class MachineState {
  <<interface>>
  +select(VendingMachine,String) void
  +insert(VendingMachine,Money) void
  +dispense(VendingMachine) DispenseResult
  +cancel(VendingMachine) int
}
class IdleState
class SelectedState
class PaidState
class Inventory
class CashBox
class Product
VendingMachine *-- Inventory
VendingMachine *-- CashBox
VendingMachine --> MachineState
MachineState <|.. IdleState
MachineState <|.. SelectedState
MachineState <|.. PaidState
Inventory --> Product
```

#### 4. Main-flow sequence diagram
```mermaid
sequenceDiagram
actor Customer
participant VM as VendingMachine
participant State as MachineState
participant Inv as Inventory
participant Cash as CashBox
Customer->>VM: select(A1)
VM->>State: select
State->>Inv: requireAvailable(A1)
Customer->>VM: insert(50)
VM->>State: insert
State->>Cash: accept(50)
Customer->>VM: dispense()
VM->>State: dispense
State->>Inv: decrement(A1)
State->>Cash: change(paid - price)
VM-->>Customer: product and change
```

#### 4b. State diagram
```mermaid
stateDiagram-v2
[*] --> Idle
Idle --> Selected: select available product
Selected --> Selected: insert insufficient cash
Selected --> Paid: insert reaches price
Selected --> Idle: cancel refund
Paid --> Idle: dispense product and change
Paid --> Idle: cancel refund
```

#### 5. Design patterns used & why
- State: each state defines legal operations and avoids a giant switch.
- Strategy: change-making can become `ChangeStrategy`.
- Facade: `VendingMachine` exposes simple methods to UI/hardware adapters.

#### 6. Complete, compilable Java 17 code
```java
// vending/model package
package vending.model;

public enum Money {
    TEN(10), TWENTY(20), FIFTY(50), HUNDRED(100);

    private final int amount;

    Money(int amount) {
        this.amount = amount;
    }

    public int amount() {
        return amount;
    }
}

public record Product(String code, String name, int price) { }

public record DispenseResult(Product product, int change) { }
```

```java
// vending/service package
package vending.service;

import vending.model.*;

import java.util.HashMap;
import java.util.Map;

public final class Inventory {
    private final Map<String, Product> products = new HashMap<>();
    private final Map<String, Integer> quantities = new HashMap<>();

    public void add(Product product, int quantity) {
        products.put(product.code(), product);
        quantities.merge(product.code(), quantity, Integer::sum);
    }

    public Product requireAvailable(String code) {
        Product product = products.get(code);
        if (product == null || quantities.getOrDefault(code, 0) <= 0) {
            throw new IllegalStateException("product unavailable");
        }
        return product;
    }

    public void decrement(String code) {
        requireAvailable(code);
        quantities.computeIfPresent(code, (ignored, quantity) -> quantity - 1);
    }
}

public final class CashBox {
    private int cash;

    public void accept(Money money) {
        cash += money.amount();
    }

    public int returnChange(int amount) {
        if (amount < 0 || cash < amount) {
            throw new IllegalStateException("cannot return change");
        }
        cash -= amount;
        return amount;
    }
}

interface MachineState {
    void select(VendingMachine machine, String code);

    void insert(VendingMachine machine, Money money);

    DispenseResult dispense(VendingMachine machine);

    int cancel(VendingMachine machine);
}

final class IdleState implements MachineState {
    @Override
    public void select(VendingMachine machine, String code) {
        machine.inventory().requireAvailable(code);
        machine.setSelectedCode(code);
        machine.setState(new SelectedState());
    }

    @Override
    public void insert(VendingMachine machine, Money money) {
        throw new IllegalStateException("select product first");
    }

    @Override
    public DispenseResult dispense(VendingMachine machine) {
        throw new IllegalStateException("pay first");
    }

    @Override
    public int cancel(VendingMachine machine) {
        return 0;
    }
}

final class SelectedState implements MachineState {
    @Override
    public void select(VendingMachine machine, String code) {
        machine.inventory().requireAvailable(code);
        machine.setSelectedCode(code);
    }

    @Override
    public void insert(VendingMachine machine, Money money) {
        machine.cashBox().accept(money);
        machine.addPaid(money.amount());
        Product product = machine.inventory().requireAvailable(machine.selectedCode());
        if (machine.paid() >= product.price()) {
            machine.setState(new PaidState());
        }
    }

    @Override
    public DispenseResult dispense(VendingMachine machine) {
        throw new IllegalStateException("insufficient payment");
    }

    @Override
    public int cancel(VendingMachine machine) {
        return machine.resetAndRefund();
    }
}

final class PaidState implements MachineState {
    @Override
    public void select(VendingMachine machine, String code) {
        throw new IllegalStateException("dispense or cancel current selection");
    }

    @Override
    public void insert(VendingMachine machine, Money money) {
        machine.cashBox().accept(money);
        machine.addPaid(money.amount());
    }

    @Override
    public DispenseResult dispense(VendingMachine machine) {
        Product product = machine.inventory().requireAvailable(machine.selectedCode());
        machine.inventory().decrement(product.code());
        int change = machine.cashBox().returnChange(machine.paid() - product.price());
        machine.resetWithoutRefund();
        return new DispenseResult(product, change);
    }

    @Override
    public int cancel(VendingMachine machine) {
        return machine.resetAndRefund();
    }
}

public final class VendingMachine {
    private final Inventory inventory;
    private final CashBox cashBox;
    private MachineState state = new IdleState();
    private String selectedCode;
    private int paid;

    public VendingMachine(Inventory inventory, CashBox cashBox) {
        this.inventory = inventory;
        this.cashBox = cashBox;
    }

    public synchronized void select(String code) {
        state.select(this, code);
    }

    public synchronized void insert(Money money) {
        state.insert(this, money);
    }

    public synchronized DispenseResult dispense() {
        return state.dispense(this);
    }

    public synchronized int cancel() {
        return state.cancel(this);
    }

    Inventory inventory() {
        return inventory;
    }

    CashBox cashBox() {
        return cashBox;
    }

    String selectedCode() {
        return selectedCode;
    }

    int paid() {
        return paid;
    }

    void addPaid(int amount) {
        paid += amount;
    }

    void setSelectedCode(String selectedCode) {
        this.selectedCode = selectedCode;
    }

    void setState(MachineState state) {
        this.state = state;
    }

    int resetAndRefund() {
        int refund = paid;
        resetWithoutRefund();
        return refund;
    }

    void resetWithoutRefund() {
        selectedCode = null;
        paid = 0;
        state = new IdleState();
    }
}
```

```java
// vending/Main.java
package vending;

import vending.model.*;
import vending.service.*;

public class Main {
    public static void main(String[] args) {
        Inventory inventory = new Inventory();
        inventory.add(new Product("A1", "Water", 30), 2);

        VendingMachine machine = new VendingMachine(inventory, new CashBox());
        machine.select("A1");
        machine.insert(Money.FIFTY);

        System.out.println(machine.dispense());
    }
}
```

#### 7. Thread-safety considerations
- Public operations are synchronized to ensure one customer session mutates selection, paid amount, stock, and
  cash atomically.
- In a fleet, each machine has its own lock; admin restock should also acquire the same machine lock.
- Payment callbacks must include idempotency keys if external payments are introduced.

#### 8. Extensibility
- Add card/UPI: add `PaymentStrategy` and a `PaymentState` waiting for callback.
- Add exact coin inventory: make `CashBox` store denomination counts and inject `ChangeStrategy`.
- Add cancel timeout: scheduler calls `cancel` after inactivity.
- Add admin restock: `InventoryService.restock` with authorization around `Inventory.add`.

#### 9. JUnit 5 tests
```java
package vending;

import org.junit.jupiter.api.Test;
import vending.model.*;
import vending.service.*;

import static org.junit.jupiter.api.Assertions.*;

class VendingMachineTest {
    @Test
    void dispensesAfterEnoughPayment() {
        Inventory inventory = new Inventory();
        inventory.add(new Product("A1", "Water", 30), 1);
        VendingMachine machine = new VendingMachine(inventory, new CashBox());

        machine.select("A1");
        machine.insert(Money.FIFTY);
        DispenseResult result = machine.dispense();

        assertEquals("A1", result.product().code());
        assertEquals(20, result.change());
    }

    @Test
    void cannotInsertBeforeSelect() {
        VendingMachine machine = new VendingMachine(new Inventory(), new CashBox());

        assertThrows(IllegalStateException.class, () -> machine.insert(Money.TEN));
    }

    @Test
    void cancelRefundsPaidAmount() {
        Inventory inventory = new Inventory();
        inventory.add(new Product("A1", "Water", 30), 1);
        VendingMachine machine = new VendingMachine(inventory, new CashBox());

        machine.select("A1");
        machine.insert(Money.TWENTY);

        assertEquals(20, machine.cancel());
    }
}
```

**Common mistakes:**
- One giant `switch` on state with duplicated validations.
- No refund/change path.
- Decrementing inventory before payment is complete.

**Follow-ups:**
- *How do you support exact change?* → `CashBox` stores denomination counts and `ChangeStrategy` computes change.
- *How do you handle card failure?* → Keep item selected, move back to selected state, and allow retry/cancel.
- *How do you manage multiple machines?* → `MachineRepository` with one lock per machine.

---

### Q3. Design ATM
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** ATM combines state transitions, authentication, cash inventory, and transaction consistency.

**Short answer (say this first, 20-40 sec):**
The ATM is a stateful facade over bank authorization and cash dispensing. Withdrawal authenticates the card,
debits the account, dispenses cash by denomination, and reverses the debit if dispensing fails. State pattern
keeps card-inserted, authenticated, and idle transitions explicit.

**Detailed answer:**

#### 1. Clarifying questions & requirements
- Questions: Withdraw only or balance/deposit too? Denominations? Daily limits? Online bank service or local mock?
  What if cash is unavailable after debit?
- Functional: insert card; authenticate PIN; withdraw valid amount; check account balance; check ATM cash;
  dispense denominations; reverse debit on dispense failure; eject card.
- Non-functional: transactional consistency; synchronized ATM session; no duplicate withdrawal on retry.
- Out of scope: EMV hardware, fraud detection, inter-bank settlement, receipt printing.

#### 2. Entities & relationships
- `Atm` owns state, bank service, dispenser, and current card.
- `BankService` owns account authentication and balances.
- `CashDispenser` owns cassette counts and dispense plan.
- `AtmState` controls valid operations.

#### 3. Class diagram
```mermaid
classDiagram
class Atm {
  -AtmState state
  -BankService bank
  -CashDispenser dispenser
  -String currentCard
  +insertCard(String) void
  +authenticate(String) void
  +withdraw(int) Map~Integer,Integer~
  +eject() void
}
class AtmState {
  <<interface>>
  +insertCard(Atm,String) void
  +authenticate(Atm,String) void
  +withdraw(Atm,int) Map~Integer,Integer~
  +eject(Atm) void
}
class BankService
class CashDispenser
class Account
Atm --> AtmState
Atm --> BankService
Atm --> CashDispenser
BankService --> Account
```

#### 4. Main-flow sequence diagram
```mermaid
sequenceDiagram
actor User
participant ATM as Atm
participant Bank as BankService
participant Cash as CashDispenser
User->>ATM: insertCard(card)
User->>ATM: authenticate(pin)
ATM->>Bank: authenticate(card, pin)
User->>ATM: withdraw(amount)
ATM->>Bank: debit(card, amount)
ATM->>Cash: dispense(amount)
alt dispenser fails
  ATM->>Bank: credit(card, amount)
end
ATM-->>User: notes
```

#### 4b. State diagram
```mermaid
stateDiagram-v2
[*] --> Idle
Idle --> CardInserted: insert card
CardInserted --> Authenticated: valid PIN
CardInserted --> Idle: eject
Authenticated --> Authenticated: balance or withdraw
Authenticated --> Idle: eject
Authenticated --> Idle: timeout
```

#### 5. Design patterns used & why
- State: legal operations differ before/after card insertion and authentication.
- Chain of Responsibility: denomination handlers can dispense cash from largest to smallest.
- Facade: `Atm` hides bank and dispenser internals.

#### 6. Complete, compilable Java 17 code
```java
// atm/service package
package atm.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

final class Account {
    private final String card;
    private final String pin;
    private int balance;

    Account(String card, String pin, int balance) {
        this.card = card;
        this.pin = pin;
        this.balance = balance;
    }

    boolean matchesPin(String candidate) {
        return pin.equals(candidate);
    }

    int balance() {
        return balance;
    }

    void debit(int amount) {
        if (balance < amount) {
            throw new IllegalStateException("insufficient funds");
        }
        balance -= amount;
    }

    void credit(int amount) {
        balance += amount;
    }
}

public final class BankService {
    private final Map<String, Account> accounts = new HashMap<>();

    public void addAccount(String card, String pin, int balance) {
        accounts.put(card, new Account(card, pin, balance));
    }

    public synchronized boolean authenticate(String card, String pin) {
        Account account = accounts.get(card);
        return account != null && account.matchesPin(pin);
    }

    public synchronized void debit(String card, int amount) {
        account(card).debit(amount);
    }

    public synchronized void credit(String card, int amount) {
        account(card).credit(amount);
    }

    public synchronized int balance(String card) {
        return account(card).balance();
    }

    private Account account(String card) {
        Account account = accounts.get(card);
        if (account == null) {
            throw new IllegalArgumentException("unknown card");
        }
        return account;
    }
}

public final class CashDispenser {
    private final LinkedHashMap<Integer, Integer> notes = new LinkedHashMap<>();

    public CashDispenser(Map<Integer, Integer> noteCountsDescending) {
        notes.putAll(noteCountsDescending);
    }

    public synchronized Map<Integer, Integer> dispense(int amount) {
        if (amount <= 0 || amount % 100 != 0) {
            throw new IllegalArgumentException("invalid amount");
        }
        int remaining = amount;
        Map<Integer, Integer> plan = new LinkedHashMap<>();
        for (Map.Entry<Integer, Integer> entry : notes.entrySet()) {
            int denomination = entry.getKey();
            int count = Math.min(remaining / denomination, entry.getValue());
            if (count > 0) {
                plan.put(denomination, count);
                remaining -= denomination * count;
            }
        }
        if (remaining != 0) {
            throw new IllegalStateException("atm cannot dispense exact amount");
        }
        plan.forEach((denomination, count) -> notes.merge(denomination, -count, Integer::sum));
        return plan;
    }
}
```

```java
// atm/service/Atm.java
package atm.service;

import java.util.Map;

interface AtmState {
    void insertCard(Atm atm, String card);

    void authenticate(Atm atm, String pin);

    Map<Integer, Integer> withdraw(Atm atm, int amount);

    void eject(Atm atm);
}

final class IdleState implements AtmState {
    @Override
    public void insertCard(Atm atm, String card) {
        atm.setCurrentCard(card);
        atm.setState(new CardInsertedState());
    }

    @Override
    public void authenticate(Atm atm, String pin) {
        throw new IllegalStateException("insert card first");
    }

    @Override
    public Map<Integer, Integer> withdraw(Atm atm, int amount) {
        throw new IllegalStateException("authenticate first");
    }

    @Override
    public void eject(Atm atm) {
        // already idle
    }
}

final class CardInsertedState implements AtmState {
    @Override
    public void insertCard(Atm atm, String card) {
        throw new IllegalStateException("card already inserted");
    }

    @Override
    public void authenticate(Atm atm, String pin) {
        if (!atm.bank().authenticate(atm.currentCard(), pin)) {
            throw new SecurityException("invalid pin");
        }
        atm.setState(new AuthenticatedState());
    }

    @Override
    public Map<Integer, Integer> withdraw(Atm atm, int amount) {
        throw new IllegalStateException("authenticate first");
    }

    @Override
    public void eject(Atm atm) {
        atm.reset();
    }
}

final class AuthenticatedState implements AtmState {
    @Override
    public void insertCard(Atm atm, String card) {
        throw new IllegalStateException("session already active");
    }

    @Override
    public void authenticate(Atm atm, String pin) {
        // already authenticated
    }

    @Override
    public Map<Integer, Integer> withdraw(Atm atm, int amount) {
        atm.bank().debit(atm.currentCard(), amount);
        try {
            return atm.dispenser().dispense(amount);
        } catch (RuntimeException ex) {
            atm.bank().credit(atm.currentCard(), amount);
            throw ex;
        }
    }

    @Override
    public void eject(Atm atm) {
        atm.reset();
    }
}

public final class Atm {
    private final BankService bank;
    private final CashDispenser dispenser;
    private AtmState state = new IdleState();
    private String currentCard;

    public Atm(BankService bank, CashDispenser dispenser) {
        this.bank = bank;
        this.dispenser = dispenser;
    }

    public synchronized void insertCard(String card) {
        state.insertCard(this, card);
    }

    public synchronized void authenticate(String pin) {
        state.authenticate(this, pin);
    }

    public synchronized Map<Integer, Integer> withdraw(int amount) {
        return state.withdraw(this, amount);
    }

    public synchronized void eject() {
        state.eject(this);
    }

    BankService bank() {
        return bank;
    }

    CashDispenser dispenser() {
        return dispenser;
    }

    String currentCard() {
        return currentCard;
    }

    void setCurrentCard(String currentCard) {
        this.currentCard = currentCard;
    }

    void setState(AtmState state) {
        this.state = state;
    }

    void reset() {
        currentCard = null;
        state = new IdleState();
    }
}
```

```java
// atm/Main.java
package atm;

import atm.service.*;

import java.util.Map;

public class Main {
    public static void main(String[] args) {
        BankService bank = new BankService();
        bank.addAccount("card1", "1234", 1000);
        CashDispenser dispenser = new CashDispenser(Map.of(500, 4, 100, 10));
        Atm atm = new Atm(bank, dispenser);

        atm.insertCard("card1");
        atm.authenticate("1234");
        System.out.println(atm.withdraw(600));
    }
}
```

#### 7. Thread-safety considerations
- `Atm` public methods are synchronized because one physical ATM serves one session at a time.
- `BankService` synchronizes account mutations; production would use database transactions and transaction IDs.
- Debit and dispense need a journal/reversal flow; this sample credits back on dispenser failure.

#### 8. Extensibility
- Add deposit: add `DepositAcceptor` and `deposit(amount)` in authenticated state.
- Add daily limit: decorate `BankService.debit` with `LimitService`.
- Add receipt: return `TransactionReceipt` from withdraw.
- Add denomination chain: replace greedy loop with handlers per cassette.

#### 9. JUnit 5 tests
```java
package atm;

import atm.service.*;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AtmTest {
    @Test
    void withdrawDebitsAccountAndReturnsNotes() {
        BankService bank = new BankService();
        bank.addAccount("c", "p", 1000);
        Atm atm = new Atm(bank, new CashDispenser(Map.of(500, 2, 100, 10)));

        atm.insertCard("c");
        atm.authenticate("p");
        Map<Integer, Integer> notes = atm.withdraw(600);

        assertEquals(400, bank.balance("c"));
        assertEquals(1, notes.get(500));
        assertEquals(1, notes.get(100));
    }

    @Test
    void badPinRejected() {
        BankService bank = new BankService();
        bank.addAccount("c", "p", 1000);
        Atm atm = new Atm(bank, new CashDispenser(Map.of(500, 2)));

        atm.insertCard("c");

        assertThrows(SecurityException.class, () -> atm.authenticate("wrong"));
    }

    @Test
    void debitIsReversedWhenCashCannotBeDispensed() {
        BankService bank = new BankService();
        bank.addAccount("c", "p", 1000);
        Atm atm = new Atm(bank, new CashDispenser(Map.of(500, 1)));

        atm.insertCard("c");
        atm.authenticate("p");

        assertThrows(IllegalStateException.class, () -> atm.withdraw(600));
        assertEquals(1000, bank.balance("c"));
    }
}
```

**Common mistakes:**
- Debiting account without handling dispenser failure.
- No state model for card inserted/authenticated.
- Ignoring denomination availability.

**Follow-ups:**
- *How do you avoid duplicate withdrawal on retry?* → Use idempotency key and transaction journal.
- *How do you support multiple ATMs?* → Central bank service with transactional account rows.
- *How do you choose denominations?* → Greedy for canonical denominations; DP/backtracking for arbitrary cassettes.

---

### Q4. Design Library Management
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** It tests entity boundaries, copy-vs-book modeling, search, loans, and double-borrow prevention.

**Short answer (say this first, 20-40 sec):**
Separate `Book` metadata from `BookCopy`, because users borrow physical copies, not titles. `LibraryService`
coordinates search, borrow, return, and reservations; borrow atomically marks a copy unavailable and creates
a `Loan` with due date.

**Detailed answer:**

#### 1. Clarifying questions & requirements
- Questions: Physical books only or e-books? How many active loans per member? Reservations? Fine calculation?
  Search by title, author, ISBN, or category?
- Functional: add books/copies/members; search; borrow available copy; return copy; prevent double borrow;
  track due date and active loans.
- Non-functional: borrow/return atomic per copy; search can be eventually indexed; clear model for future DB.
- Out of scope: payment gateway for fines, barcode hardware, recommendation engine.

#### 2. Entities & relationships
- `Book` is bibliographic metadata.
- `BookCopy` is the lendable unit.
- `Member` borrows copies through `Loan`.
- `LibraryService` acts as application service over repositories/maps.

#### 3. Class diagram
```mermaid
classDiagram
class LibraryService {
  -Map~String,Book~ books
  -Map~String,BookCopy~ copies
  -Map~String,Loan~ activeLoansByCopy
  +search(String) List~Book~
  +borrow(String,String) Loan
  +returnCopy(String) void
}
class Book
class BookCopy {
  -CopyStatus status
  +markBorrowed() void
  +markAvailable() void
}
class Member
class Loan
LibraryService --> Book
LibraryService --> BookCopy
LibraryService --> Member
Loan --> BookCopy
Loan --> Member
BookCopy --> Book
```

#### 4. Main-flow sequence diagram
```mermaid
sequenceDiagram
actor Member
participant Library as LibraryService
participant Copy as BookCopy
Member->>Library: search(query)
Library-->>Member: matching books
Member->>Library: borrow(memberId, copyId)
Library->>Copy: markBorrowed()
Library-->>Member: loan with due date
Member->>Library: returnCopy(copyId)
Library->>Copy: markAvailable()
```

#### 5. Design patterns used & why
- Repository style: in-memory maps can be swapped with DB repositories.
- Strategy: `FineStrategy` or `SearchStrategy` can be added later.
- Observer: notifications for due-date reminders or reservation availability.

#### 6. Complete, compilable Java 17 code
```java
// library/model package
package library.model;

import java.time.LocalDate;

public enum CopyStatus { AVAILABLE, BORROWED, LOST }

public record Book(String isbn, String title, String author) { }

public record Member(String id, String name) { }

public record Loan(String id, String memberId, String copyId, LocalDate dueDate) { }

public final class BookCopy {
    private final String id;
    private final Book book;
    private CopyStatus status = CopyStatus.AVAILABLE;

    public BookCopy(String id, Book book) {
        this.id = id;
        this.book = book;
    }

    public String id() {
        return id;
    }

    public Book book() {
        return book;
    }

    public CopyStatus status() {
        return status;
    }

    public void markBorrowed() {
        if (status != CopyStatus.AVAILABLE) {
            throw new IllegalStateException("copy unavailable");
        }
        status = CopyStatus.BORROWED;
    }

    public void markAvailable() {
        if (status != CopyStatus.BORROWED) {
            throw new IllegalStateException("copy is not borrowed");
        }
        status = CopyStatus.AVAILABLE;
    }
}
```

```java
// library/service/LibraryService.java
package library.service;

import library.model.*;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class LibraryService {
    private final Map<String, Book> booksByIsbn = new HashMap<>();
    private final Map<String, BookCopy> copiesById = new HashMap<>();
    private final Map<String, Member> membersById = new HashMap<>();
    private final Map<String, Loan> activeLoansByCopy = new HashMap<>();
    private final Clock clock;

    public LibraryService(Clock clock) {
        this.clock = clock;
    }

    public void addBook(Book book) {
        booksByIsbn.put(book.isbn(), book);
    }

    public void addCopy(BookCopy copy) {
        copiesById.put(copy.id(), copy);
    }

    public void addMember(Member member) {
        membersById.put(member.id(), member);
    }

    public List<Book> search(String query) {
        String normalized = query.toLowerCase();
        return booksByIsbn.values().stream()
                .filter(book -> book.title().toLowerCase().contains(normalized)
                        || book.author().toLowerCase().contains(normalized)
                        || book.isbn().equalsIgnoreCase(query))
                .toList();
    }

    public synchronized Loan borrow(String memberId, String copyId) {
        if (!membersById.containsKey(memberId)) {
            throw new IllegalArgumentException("unknown member");
        }
        BookCopy copy = requireCopy(copyId);
        copy.markBorrowed();
        Loan loan = new Loan(
                UUID.randomUUID().toString(),
                memberId,
                copyId,
                LocalDate.now(clock).plusDays(14));
        activeLoansByCopy.put(copyId, loan);
        return loan;
    }

    public synchronized void returnCopy(String copyId) {
        BookCopy copy = requireCopy(copyId);
        copy.markAvailable();
        activeLoansByCopy.remove(copyId);
    }

    public synchronized boolean hasActiveLoan(String copyId) {
        return activeLoansByCopy.containsKey(copyId);
    }

    private BookCopy requireCopy(String copyId) {
        BookCopy copy = copiesById.get(copyId);
        if (copy == null) {
            throw new IllegalArgumentException("unknown copy");
        }
        return copy;
    }
}
```

```java
// library/Main.java
package library;

import library.model.*;
import library.service.LibraryService;

import java.time.Clock;

public class Main {
    public static void main(String[] args) {
        LibraryService library = new LibraryService(Clock.systemUTC());
        Book book = new Book("978013", "Effective Java", "Joshua Bloch");
        library.addBook(book);
        library.addCopy(new BookCopy("copy-1", book));
        library.addMember(new Member("m1", "Azam"));

        System.out.println(library.borrow("m1", "copy-1"));
    }
}
```

#### 7. Thread-safety considerations
- `borrow` and `returnCopy` are synchronized because copy status and active-loan map must change atomically.
- Production DB design should enforce one active loan per copy with a unique filtered index.
- Search can use immutable indexes or read-write locks if writes are frequent.

#### 8. Extensibility
- Add reservations: maintain queue per ISBN and assign returned copy to next member.
- Add fines: inject `FineStrategy` based on due date and return date.
- Add categories: add fields and secondary indexes by author/category/title.
- Add notifications: publish domain events on borrow, return, and due-date reminders.

#### 9. JUnit 5 tests
```java
package library;

import library.model.*;
import library.service.LibraryService;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class LibraryServiceTest {
    @Test
    void borrowCreatesLoanAndPreventsDoubleBorrow() {
        LibraryService library = new LibraryService(Clock.systemUTC());
        Book book = new Book("1", "Java", "Author");
        library.addBook(book);
        library.addCopy(new BookCopy("c1", book));
        library.addMember(new Member("m1", "Member"));

        library.borrow("m1", "c1");

        assertTrue(library.hasActiveLoan("c1"));
        assertThrows(IllegalStateException.class, () -> library.borrow("m1", "c1"));
    }

    @Test
    void returnMakesCopyBorrowableAgain() {
        LibraryService library = new LibraryService(Clock.systemUTC());
        Book book = new Book("1", "Java", "Author");
        library.addBook(book);
        library.addCopy(new BookCopy("c1", book));
        library.addMember(new Member("m1", "Member"));

        library.borrow("m1", "c1");
        library.returnCopy("c1");

        assertDoesNotThrow(() -> library.borrow("m1", "c1"));
    }

    @Test
    void dueDateIsFourteenDaysLater() {
        Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        LibraryService library = new LibraryService(clock);
        Book book = new Book("1", "Java", "Author");
        library.addBook(book);
        library.addCopy(new BookCopy("c1", book));
        library.addMember(new Member("m1", "Member"));

        Loan loan = library.borrow("m1", "c1");

        assertEquals("2026-01-15", loan.dueDate().toString());
    }
}
```

**Common mistakes:**
- Borrowing `Book` instead of `BookCopy`.
- Not preventing double borrow.
- No due date or active-loan lifecycle.

**Follow-ups:**
- *How do reservations work?* → Queue member IDs per ISBN; when a copy returns, assign to queue head.
- *What tables would you create?* → `book`, `book_copy`, `member`, `loan`, and `reservation`.
- *How do you compute fines?* → `FineStrategy.calculate(loan, returnedAt)`.

---

### Q5. Design Elevator System
`🔴 Advanced` `🔥` `Must-know SDE-1/SDE-2`

**Why they ask:** Elevator combines scheduling, state machines, queues, direction, and concurrent requests.

**Short answer (say this first, 20-40 sec):**
Model each elevator as an actor-like object with current floor, direction, and two sorted stop sets. The
dispatcher uses a scheduling strategy to assign hall calls. LOOK/SCAN-style movement serves all stops in the
current direction before reversing, avoiding random oscillation.

**Detailed answer:**

#### 1. Clarifying questions & requirements
- Questions: Number of elevators/floors? Hall calls only or destination control? Capacity? Emergency mode?
  Should scheduler be nearest, SCAN, or LOOK?
- Functional: receive hall request; assign an elevator; add car destination; move one tick at a time; open door
  at requested floors; use LOOK/SCAN direction behavior.
- Non-functional: thread-safe dispatcher; no starvation for opposite direction; strategy-swappable scheduling.
- Out of scope: motor physics, sensor hardware, fire-service integration, predictive traffic learning.

#### 2. Entities & relationships
- `ElevatorSystem` receives requests and owns elevators.
- `Elevator` owns movement state and stop sets.
- `SchedulingStrategy` chooses elevator.
- `HallRequest` captures floor and direction.

#### 3. Class diagram
```mermaid
classDiagram
class ElevatorSystem {
  -List~Elevator~ elevators
  -SchedulingStrategy strategy
  +requestPickup(int,Direction) int
  +requestDestination(int,int) void
  +stepAll() void
}
class Elevator {
  -int id
  -int currentFloor
  -Direction direction
  -TreeSet~Integer~ upStops
  -TreeSet~Integer~ downStops
  +addStop(int) void
  +step() void
}
class SchedulingStrategy {
  <<interface>>
  +choose(List~Elevator~,HallRequest) Elevator
}
class LookSchedulingStrategy
class HallRequest
ElevatorSystem *-- "1..*" Elevator
ElevatorSystem --> SchedulingStrategy
SchedulingStrategy <|.. LookSchedulingStrategy
```

#### 4. Main-flow sequence diagram
```mermaid
sequenceDiagram
actor Rider
participant System as ElevatorSystem
participant Strategy as SchedulingStrategy
participant Elevator
Rider->>System: requestPickup(floor, UP)
System->>Strategy: choose(elevators, request)
Strategy-->>System: elevator
System->>Elevator: addStop(floor)
Rider->>System: requestDestination(elevatorId, dest)
System->>Elevator: addStop(dest)
System->>Elevator: step()
Elevator-->>System: updated floor and direction
```

#### 4b. State diagram
```mermaid
stateDiagram-v2
[*] --> Idle
Idle --> MovingUp: higher stop added
Idle --> MovingDown: lower stop added
MovingUp --> DoorOpen: stop at current floor
MovingDown --> DoorOpen: stop at current floor
DoorOpen --> MovingUp: pending higher stops
DoorOpen --> MovingDown: pending lower stops
DoorOpen --> Idle: no pending stops
MovingUp --> MovingDown: no higher stops
MovingDown --> MovingUp: no lower stops
```

#### 5. Design patterns used & why
- Strategy: scheduling can be LOOK, nearest-car, capacity-aware, or destination-control.
- State via enum: direction and door status are simple enough for enums; full State can be added for hardware.
- Mediator: `ElevatorSystem` coordinates riders, strategy, and elevators.

#### 6. Complete, compilable Java 17 code
```java
// elevator/model package
package elevator.model;

public enum Direction { UP, DOWN, IDLE }

public enum DoorState { OPEN, CLOSED }

public record HallRequest(int floor, Direction direction) { }
```

```java
// elevator/service package
package elevator.service;

import elevator.model.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

public final class Elevator {
    private final int id;
    private int currentFloor;
    private Direction direction = Direction.IDLE;
    private DoorState door = DoorState.CLOSED;
    private final TreeSet<Integer> upStops = new TreeSet<>();
    private final TreeSet<Integer> downStops = new TreeSet<>(Comparator.reverseOrder());

    public Elevator(int id, int startFloor) {
        this.id = id;
        this.currentFloor = startFloor;
    }

    public synchronized void addStop(int floor) {
        if (floor > currentFloor) {
            upStops.add(floor);
            if (direction == Direction.IDLE) {
                direction = Direction.UP;
            }
        } else if (floor < currentFloor) {
            downStops.add(floor);
            if (direction == Direction.IDLE) {
                direction = Direction.DOWN;
            }
        } else {
            door = DoorState.OPEN;
        }
    }

    public synchronized void step() {
        door = DoorState.CLOSED;
        chooseDirectionIfNeeded();
        if (direction == Direction.UP) {
            currentFloor++;
            if (upStops.remove(currentFloor)) {
                door = DoorState.OPEN;
            }
            if (upStops.isEmpty() && !downStops.isEmpty()) {
                direction = Direction.DOWN;
            }
        } else if (direction == Direction.DOWN) {
            currentFloor--;
            if (downStops.remove(currentFloor)) {
                door = DoorState.OPEN;
            }
            if (downStops.isEmpty() && !upStops.isEmpty()) {
                direction = Direction.UP;
            }
        }
        if (upStops.isEmpty() && downStops.isEmpty() && door == DoorState.CLOSED) {
            direction = Direction.IDLE;
        }
    }

    private void chooseDirectionIfNeeded() {
        if (direction != Direction.IDLE) {
            return;
        }
        if (!upStops.isEmpty()) {
            direction = Direction.UP;
        } else if (!downStops.isEmpty()) {
            direction = Direction.DOWN;
        }
    }

    public synchronized int id() {
        return id;
    }

    public synchronized int currentFloor() {
        return currentFloor;
    }

    public synchronized Direction direction() {
        return direction;
    }

    public synchronized DoorState door() {
        return door;
    }

    public synchronized int pendingStops() {
        return upStops.size() + downStops.size();
    }
}

public interface SchedulingStrategy {
    Elevator choose(List<Elevator> elevators, HallRequest request);
}

public final class LookSchedulingStrategy implements SchedulingStrategy {
    @Override
    public Elevator choose(List<Elevator> elevators, HallRequest request) {
        return elevators.stream()
                .min(Comparator
                        .comparingInt((Elevator elevator) -> cost(elevator, request))
                        .thenComparingInt(Elevator::pendingStops)
                        .thenComparingInt(Elevator::id))
                .orElseThrow();
    }

    private int cost(Elevator elevator, HallRequest request) {
        Direction direction = elevator.direction();
        int distance = Math.abs(elevator.currentFloor() - request.floor());
        if (direction == Direction.IDLE) {
            return distance;
        }
        boolean sameDirection = direction == request.direction();
        boolean onTheWay = direction == Direction.UP
                ? request.floor() >= elevator.currentFloor()
                : request.floor() <= elevator.currentFloor();
        return sameDirection && onTheWay ? distance : distance + 100;
    }
}

public final class ElevatorSystem {
    private final List<Elevator> elevators = new ArrayList<>();
    private final SchedulingStrategy strategy;

    public ElevatorSystem(int elevatorCount, SchedulingStrategy strategy) {
        this.strategy = strategy;
        for (int i = 0; i < elevatorCount; i++) {
            elevators.add(new Elevator(i, 0));
        }
    }

    public synchronized int requestPickup(int floor, Direction direction) {
        Elevator elevator = strategy.choose(elevators, new HallRequest(floor, direction));
        elevator.addStop(floor);
        return elevator.id();
    }

    public void requestDestination(int elevatorId, int destinationFloor) {
        elevators.get(elevatorId).addStop(destinationFloor);
    }

    public void stepAll() {
        elevators.forEach(Elevator::step);
    }

    public Elevator elevator(int id) {
        return elevators.get(id);
    }
}
```

```java
// elevator/Main.java
package elevator;

import elevator.model.Direction;
import elevator.service.*;

public class Main {
    public static void main(String[] args) {
        ElevatorSystem system = new ElevatorSystem(2, new LookSchedulingStrategy());
        int elevatorId = system.requestPickup(5, Direction.UP);

        for (int i = 0; i < 5; i++) {
            system.stepAll();
        }

        system.requestDestination(elevatorId, 9);
        System.out.println(system.elevator(elevatorId).currentFloor());
    }
}
```

#### 7. Thread-safety considerations
- Dispatcher assignment is synchronized so one hall request is assigned once.
- Each `Elevator` synchronizes stop mutation and movement because scheduler and motor loop may run concurrently.
- Actor model is cleaner in production: one queue per elevator plus one dispatcher queue.

#### 8. Extensibility
- Add capacity: add `currentLoad/maxLoad` to `Elevator`; scheduler skips full elevators.
- Add emergency mode: add `available` flag; scheduler filters unavailable elevators.
- Add destination control: replace hall request with passenger destination and group by floor.
- Add door timing: separate `DoorController` and state machine for open/closing/obstructed.

#### 9. JUnit 5 tests
```java
package elevator;

import elevator.model.*;
import elevator.service.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ElevatorSystemTest {
    @Test
    void elevatorMovesTowardStopAndOpensDoor() {
        Elevator elevator = new Elevator(1, 0);
        elevator.addStop(2);

        elevator.step();
        elevator.step();

        assertEquals(2, elevator.currentFloor());
        assertEquals(DoorState.OPEN, elevator.door());
    }

    @Test
    void systemAssignsPickup() {
        ElevatorSystem system = new ElevatorSystem(1, new LookSchedulingStrategy());

        int id = system.requestPickup(3, Direction.UP);
        system.stepAll();

        assertEquals(0, id);
        assertEquals(1, system.elevator(0).currentFloor());
    }

    @Test
    void lookStrategyPrefersIdleNearestElevator() {
        Elevator idle = new Elevator(1, 0);
        Elevator busy = new Elevator(2, 0);
        busy.addStop(10);
        SchedulingStrategy strategy = new LookSchedulingStrategy();

        Elevator chosen = strategy.choose(java.util.List.of(busy, idle), new HallRequest(1, Direction.UP));

        assertEquals(1, chosen.id());
    }
}
```

**Common mistakes:**
- Assigning purely by nearest floor without considering direction and pending stops.
- No target queues or stop sets.
- Treating elevator movement as immediate teleportation.

**Follow-ups:**
- *SCAN vs LOOK?* → SCAN goes to physical end before reversing; LOOK reverses at furthest pending stop.
- *How to avoid starvation?* → Aging priority for old requests or periodic global rebalance.
- *How do you scale to many elevators?* → Dispatcher queue, per-elevator actor, strategy with cost function.

## Revision Cheat Sheet

- Logger: facade + handler chain + sink/formatter strategies; sinks need synchronization.
- Vending Machine: State pattern for Idle/Selected/Paid; inventory and cash mutate under one machine lock.
- ATM: authenticate, debit, dispense, reverse on failure; state machine prevents invalid operations.
- Library: Book metadata differs from BookCopy; borrow creates active loan atomically.
- Elevator: LOOK/SCAN scheduling uses direction and sorted stop sets; dispatcher assignment must be synchronized.
- For every LLD, state the exact lock boundary and the extension class you would change.

## Flashcards

1. Logger output extension? → Implement `LogSink`.
2. Logger async design? → Bounded queue plus worker and flush/shutdown.
3. Vending state pattern benefit? → Invalid actions are localized per state.
4. Vending exact change? → Denomination-aware `CashBox` plus `ChangeStrategy`.
5. ATM dispense failure after debit? → Reverse debit using transaction journal.
6. ATM denomination logic? → Greedy for canonical notes, DP for arbitrary counts.
7. Library Book vs Copy? → Book is metadata; Copy is lendable physical item.
8. Library double borrow prevention? → Atomic copy status update and active-loan constraint.
9. Elevator LOOK? → Continue current direction until no stops remain that way, then reverse.
10. Elevator scheduler input? → Current floor, direction, pending stops, capacity, and request direction.
11. Shared theme? → Keep core services independent of UI and console input.
12. Best tests? → Deterministic, service-level, and focused on lifecycle edges.
