# 02a · Core Java and JVM Fundamentals

This part covers interview-ready Core Java fundamentals, JVM architecture, class loading, memory management, GC, strings, equality, and core collection internals. Use Part B for streams, generics, exceptions, design patterns, modern Java, and tricky output questions.

**Legend:** 🟢 Beginner · 🟡 Intermediate · 🔴 Advanced · ⚫ FAANG · 🔥 = very frequently asked

## Table of Contents
- [Foundations and OOP](#foundations-and-oop)
- [JDK, JRE, JVM and Class Loading](#jdk-jre-jvm-and-class-loading)
- [JVM Memory and Garbage Collection](#jvm-memory-and-garbage-collection)
- [Strings, Equality and Core Collections](#strings-equality-and-core-collections)
- [Revision Cheat Sheet](#revision-cheat-sheet)
- [Flashcards](#flashcards)

## Foundations and OOP

### Q1. What are the four pillars of OOP in Java?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** They check whether you can model backend code beyond syntax.

**Short answer (say this first, 20-40 sec):**
OOP has encapsulation, abstraction, inheritance, and polymorphism. Encapsulation protects state, abstraction exposes only essential behavior, inheritance reuses or specializes behavior, and polymorphism lets one interface have multiple runtime implementations.

**Detailed answer:**
- **Encapsulation:** keep fields private and expose behavior through methods.
- **Abstraction:** hide implementation details behind interfaces or abstract classes.
- **Inheritance:** `extends` for class specialization; Java supports single class inheritance.
- **Polymorphism:** method overloading at compile time and overriding at runtime.

```java
interface PaymentGateway {
    boolean charge(int amountInPaise);
}

final class UpiGateway implements PaymentGateway {
    private final String provider;

    UpiGateway(String provider) {
        this.provider = provider;
    }

    @Override
    public boolean charge(int amountInPaise) {
        return amountInPaise > 0 && provider != null;
    }
}
```

For a Spring Boot REST service, you usually inject an interface like `PaymentGateway`; tests can use a fake implementation and production can use UPI, card, or wallet implementations.

**Common mistakes:**
- Saying abstraction and encapsulation are the same.
- Using inheritance where composition is simpler.
- Forgetting runtime polymorphism requires overriding, not overloading.

**Follow-ups:**
- *Is Java fully object-oriented?* → No. It has primitives like `int`, though wrappers and autoboxing bridge the gap.
- *Why prefer interfaces?* → They decouple callers from implementation and make testing easier.

### Q2. Encapsulation vs abstraction: what is the difference?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Many candidates memorize terms but cannot design classes cleanly.

**Short answer (say this first, 20-40 sec):**
Encapsulation is about hiding and protecting an object's internal state. Abstraction is about exposing a simplified contract while hiding implementation complexity.

**Detailed answer:**
| Concept | Focus | Java tools | Example |
|---|---|---|---|
| Encapsulation | Data safety | `private`, validation methods, immutable classes | `BankAccount.withdraw()` validates balance |
| Abstraction | Simplified contract | `interface`, `abstract class` | `Repository.save()` hides SQL/JPA details |

```java
final class BankAccount {
    private long balance;

    public void deposit(long amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
        balance += amount;
    }

    public long balance() {
        return balance;
    }
}
```

The private field is encapsulation. A service depending on `AccountRepository` instead of a specific SQL class is abstraction.

**Common mistakes:**
- Exposing mutable fields through getters.
- Creating interfaces for every class without a real abstraction.
- Thinking abstraction always means abstract class.

**Follow-ups:**
- *Can a class be encapsulated but not abstracted?* → Yes, a simple value object can hide fields without implementing an interface.
- *Can abstraction leak?* → Yes, for example a repository method exposing SQL-specific exceptions leaks persistence details.

### Q3. Inheritance vs composition: when should you use each?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Good Java design avoids fragile inheritance hierarchies.

**Short answer (say this first, 20-40 sec):**
Use inheritance for a true "is-a" relationship with stable behavior. Prefer composition for "has-a" behavior, optional capabilities, and code that must evolve without breaking subclasses.

**Detailed answer:**
Inheritance couples a subclass to parent implementation details. Composition delegates work to another object and is easier to test or replace.

```java
interface DiscountPolicy {
    long discount(long cartValue);
}

final class CheckoutService {
    private final DiscountPolicy discountPolicy;

    CheckoutService(DiscountPolicy discountPolicy) {
        this.discountPolicy = discountPolicy;
    }

    long payableAmount(long cartValue) {
        return cartValue - discountPolicy.discount(cartValue);
    }
}
```

This is better than `FestivalCheckoutService extends CheckoutService` because policies can change independently. In backend systems, composition also works well with dependency injection.

**Common mistakes:**
- Extending a class just to reuse one method.
- Making deep inheritance trees that are hard to reason about.
- Violating Liskov Substitution by weakening parent behavior.

**Follow-ups:**
- *Where is inheritance useful?* → Framework base classes, sealed hierarchies, and domain types with stable common behavior.
- *What is the fragile base class problem?* → A parent change unexpectedly breaks subclasses that depend on internal behavior.

### Q4. What is polymorphism in Java?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Runtime dispatch appears in almost every Java backend interview.

**Short answer (say this first, 20-40 sec):**
Polymorphism means the same call can execute different behavior based on the object type. Overloading is compile-time polymorphism; overriding is runtime polymorphism using dynamic dispatch.

**Detailed answer:**
```java
class Animal {
    String sound() {
        return "unknown";
    }
}

class Dog extends Animal {
    @Override
    String sound() {
        return "bark";
    }
}

Animal animal = new Dog();
System.out.println(animal.sound()); // bark
```

The variable type is `Animal`, but the object is `Dog`; overridden instance methods dispatch at runtime. Static methods, private methods, and fields are not overridden in the same way.

**Common mistakes:**
- Confusing method overloading with overriding.
- Thinking fields are polymorphic.
- Forgetting static method calls are resolved by reference type.

**Follow-ups:**
- *Can constructors be overridden?* → No. Constructors are not inherited.
- *Can return type differ in overriding?* → Yes, with covariant return types.

### Q5. Overloading vs overriding: how does Java choose a method?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** It is a common source of output-based questions.

**Short answer (say this first, 20-40 sec):**
Overloading is chosen at compile time from the reference type and argument types. Overriding is chosen at runtime from the actual object type.

**Detailed answer:**
```java
class Printer {
    void print(Object value) { System.out.println("object"); }
    void print(String value) { System.out.println("string"); }
}

Printer printer = new Printer();
printer.print(null); // string, because String is more specific than Object
```

Overload resolution prefers exact match, widening, boxing, varargs in that rough order. For overriding, Java uses dynamic dispatch only for instance methods.

```java
class Parent {
    void run() { System.out.println("parent"); }
}
class Child extends Parent {
    @Override void run() { System.out.println("child"); }
}
Parent p = new Child();
p.run(); // child
```

**Common mistakes:**
- Expecting runtime type to affect overload selection.
- Overloading with ambiguous `null` arguments.
- Reducing visibility while overriding; Java forbids it.

**Follow-ups:**
- *Can checked exceptions change in overriding?* → The child cannot throw broader checked exceptions than the parent method.
- *Can overloaded methods differ only by return type?* → No.

### Q6. What is the difference between interface and abstract class?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** It tests API design and Java language basics.

**Short answer (say this first, 20-40 sec):**
Use an interface to define a capability or contract that many unrelated classes can implement. Use an abstract class when implementations share state or common protected behavior.

**Detailed answer:**
| Feature | Interface | Abstract class |
|---|---|---|
| Multiple inheritance | A class can implement many | A class extends only one |
| Fields | `public static final` constants | Instance fields allowed |
| Methods | Abstract, default, static, private helper methods | Abstract and concrete methods |
| Constructor | No instance constructor | Constructor allowed |

```java
interface Auditable {
    default void audit(String action) {
        System.out.println("audit: " + action);
    }
}

abstract class BaseEntity {
    private final long createdAt = System.currentTimeMillis();
    long createdAt() { return createdAt; }
}
```

Since Java 8, interfaces can have `default` methods, but they should not become stateful base classes.

**Common mistakes:**
- Saying interfaces cannot have implemented methods; Java 8 changed that.
- Putting mutable shared state in interfaces.
- Choosing abstract class when multiple capabilities are needed.

**Follow-ups:**
- *What if two interfaces have same default method?* → The implementing class must override and resolve the conflict.
- *Can interfaces have private methods?* → Yes, since Java 9, for helper logic inside default/static methods.

### Q7. What are access modifiers in Java?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Access control affects API boundaries and encapsulation.

**Short answer (say this first, 20-40 sec):**
Java has `private`, package-private, `protected`, and `public`. Use the most restrictive visibility that still supports the design.

**Detailed answer:**
| Modifier | Same class | Same package | Subclass outside package | Everywhere |
|---|---:|---:|---:|---:|
| `private` | Yes | No | No | No |
| package-private | Yes | Yes | No | No |
| `protected` | Yes | Yes | Yes, through inheritance | No |
| `public` | Yes | Yes | Yes | Yes |

Package-private is often useful for testable internal helpers without making them public API. In libraries, every public method becomes a compatibility promise.

**Common mistakes:**
- Making fields `public`.
- Thinking `protected` means package-only; it also allows subclass access.
- Overexposing service internals for tests.

**Follow-ups:**
- *Can top-level classes be private?* → No. Top-level classes can be public or package-private.
- *Can an overriding method reduce visibility?* → No, it can only keep or widen visibility.

### Q8. What are `final`, `finally`, and `finalize()`?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Similar names cause confusion in interviews.

**Short answer (say this first, 20-40 sec):**
`final` is a modifier for variables, methods, or classes. `finally` is an exception-handling block. `finalize()` was a cleanup hook but is deprecated and should not be used.

**Detailed answer:**
- `final` variable: cannot be reassigned after initialization.
- `final` method: cannot be overridden.
- `final` class: cannot be extended.
- `finally`: usually runs after `try`/`catch`, even when an exception is thrown.
- `finalize()`: unreliable, deprecated for removal; use `try-with-resources`, cleaners only when unavoidable.

```java
try (var stream = java.nio.file.Files.lines(java.nio.file.Path.of("data.txt"))) {
    stream.limit(1).forEach(System.out::println);
}
```

**Common mistakes:**
- Using `finalize()` for file or socket cleanup.
- Thinking `final` makes an object immutable; it only prevents reassignment.
- Returning from `finally`, which can suppress exceptions or earlier returns.

**Follow-ups:**
- *Does `finally` always run?* → Not after JVM crash, `System.exit`, process kill, or infinite loop before the block.
- *What makes an object immutable?* → No observable state changes, defensive copies, and no leaked mutable internals.

### Q9. Is Java pass-by-value or pass-by-reference?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** This is a classic misconception.

**Short answer (say this first, 20-40 sec):**
Java is always pass-by-value. For objects, the value passed is a copy of the reference, so methods can mutate the object but cannot reassign the caller's reference.

**Detailed answer:**
```java
static void change(StringBuilder sb) {
    sb.append("X");               // mutates same object
    sb = new StringBuilder("Y");   // local reference only
}

StringBuilder value = new StringBuilder("A");
change(value);
System.out.println(value); // AX
```

The reference copy points to the same object initially. Reassigning the parameter changes only the local copy.

**Common mistakes:**
- Saying Java is pass-by-reference for objects.
- Confusing object mutation with reference reassignment.
- Assuming immutable objects like `String` can be changed by a method.

**Follow-ups:**
- *Can a method swap two caller variables?* → Not directly, because references are passed by value.
- *How can mutation be avoided?* → Use immutable classes or defensive copies.

### Q10. What is object lifecycle in Java?
`🟡 Intermediate` `Must-know SDE-1`

**Why they ask:** It connects constructors, references, GC, and resource management.

**Short answer (say this first, 20-40 sec):**
An object is allocated, initialized by constructors, used while reachable, becomes eligible for GC when unreachable, and is eventually reclaimed by the garbage collector. Non-memory resources must be closed explicitly.

**Detailed answer:**
Typical lifecycle:
1. Class is loaded and initialized if needed.
2. Memory is allocated on heap, or optimized away by JIT escape analysis.
3. Fields get default values, then initializers and constructors run.
4. Object is used while reachable from GC roots.
5. When unreachable, memory can be reclaimed.

```java
try (DatabaseConnection connection = pool.getConnection()) {
    connection.query("select 1");
}
```

GC handles heap memory, not timely release of database connections, files, locks, or sockets. Use `AutoCloseable` and `try-with-resources`.

**Common mistakes:**
- Assuming GC runs immediately when an object becomes unreachable.
- Using GC for resource cleanup.
- Leaking `this` from a constructor before the object is fully initialized.

**Follow-ups:**
- *What are GC roots?* → Active thread stacks, static fields, JNI references, and some VM internals.
- *What is escape analysis?* → JIT analysis that can allocate objects on stack or eliminate allocation if object does not escape.

## JDK, JRE, JVM and Class Loading

### Q11. What is the difference between JDK, JRE, and JVM?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** It verifies basic platform understanding.

**Short answer (say this first, 20-40 sec):**
JVM runs bytecode, JRE provides JVM plus runtime libraries, and JDK provides JRE plus developer tools like `javac`, `jar`, `jdb`, and `jlink`.

**Detailed answer:**
| Term | Contains | Used by |
|---|---|---|
| JVM | Class loader, runtime memory, execution engine, GC | Running Java bytecode |
| JRE | JVM plus standard libraries | End users running apps |
| JDK | JRE plus compilers and tools | Developers building apps |

Since Java 9 modularization, the old standalone JRE distribution model changed; many deployments build custom runtimes with `jlink`. In interviews, still explain the conceptual layering.

**Common mistakes:**
- Saying JDK and JVM are the same.
- Saying JVM compiles `.java`; `javac` does that.
- Ignoring Java 9 module/runtime image changes.

**Follow-ups:**
- *What does `javac` produce?* → Platform-independent bytecode in `.class` files.
- *Why is Java called platform independent?* → Bytecode runs on any compatible JVM, but native libraries still depend on OS/CPU.

### Q12. Explain JVM architecture.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** JVM architecture is the base for class loading, GC, JIT, and performance tuning.

**Short answer (say this first, 20-40 sec):**
The JVM loads class files, verifies bytecode, stores runtime data in memory areas, executes code through interpreter and JIT compiler, and manages memory through garbage collection.

**Detailed answer:**
```mermaid
flowchart TD
    A["Java source files"] --> B["javac compiler"]
    B --> C["Class files bytecode"]
    C --> D["Class loader subsystem"]
    D --> E["Bytecode verifier"]
    E --> F["Runtime data areas"]
    F --> G["Execution engine"]
    G --> H["Interpreter"]
    G --> I["JIT compiler"]
    G --> J["Garbage collector"]
    G --> K["Native method interface"]
    K --> L["Native libraries"]
```

Runtime data areas include heap, stacks, metaspace, PC registers, and native method stacks. HotSpot first interprets bytecode, profiles hot methods, then JIT-compiles them to optimized machine code.

**Common mistakes:**
- Thinking Java is only interpreted.
- Ignoring bytecode verification and security checks.
- Treating heap and stack as the only JVM memory areas.

**Follow-ups:**
- *What is HotSpot?* → The most commonly used OpenJDK JVM implementation.
- *What is JIT?* → Just-in-time compilation of hot bytecode to native code at runtime.

### Q13. What happens when you run `java Main`?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** It checks end-to-end understanding from classpath to execution.

**Short answer (say this first, 20-40 sec):**
The JVM starts, locates `Main`, loads and verifies classes, initializes static state, finds `public static void main(String[] args)`, and executes bytecode using interpreter/JIT while GC manages memory.

**Detailed answer:**
Steps:
1. Launcher starts JVM with classpath or module path.
2. Application class loader finds `Main.class`.
3. JVM verifies bytecode for type safety and stack correctness.
4. Class preparation creates static fields with default values.
5. Class initialization runs static initializers.
6. JVM invokes `main`.
7. Execution engine interprets/JIT-compiles hot code.

```java
public class Main {
    static { System.out.println("init"); }
    public static void main(String[] args) {
        System.out.println("run");
    }
}
```

Output is `init` then `run`, because class initialization happens before `main`.

**Common mistakes:**
- Forgetting class initialization before `main`.
- Thinking every referenced class loads at startup.
- Confusing class loading with object creation.

**Follow-ups:**
- *When is a class initialized?* → On active use, such as `new`, static method call, or non-constant static field access.
- *What is lazy loading?* → Classes are loaded when needed, not necessarily all at startup.

### Q14. Explain class loading phases.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Class loading appears in framework, reflection, and classpath issues.

**Short answer (say this first, 20-40 sec):**
Class loading has loading, linking, and initialization. Linking includes verification, preparation, and optional resolution.

**Detailed answer:**
- **Loading:** find class bytes and create `Class<?>` metadata.
- **Verification:** ensure bytecode safety.
- **Preparation:** allocate static fields and set default values.
- **Resolution:** convert symbolic references to direct references as needed.
- **Initialization:** run static field initializers and static blocks in textual order.

```java
class Demo {
    static int x = value();
    static { System.out.println("block " + x); }
    static int value() {
        System.out.println("field");
        return 10;
    }
}
```

Initialization order matters and is a frequent output-question target.

**Common mistakes:**
- Saying static fields get explicit values during preparation; explicit values happen during initialization.
- Assuming resolution always happens eagerly.
- Ignoring parent class initialization before child initialization.

**Follow-ups:**
- *What is `ClassNotFoundException`?* → Checked exception usually from dynamic loading by name.
- *What is `NoClassDefFoundError`?* → Runtime linkage error when a class was available at compile time but missing or failed at runtime.

### Q15. What are Bootstrap, Platform, and Application class loaders?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** It tests how Java finds classes and avoids conflicts.

**Short answer (say this first, 20-40 sec):**
Bootstrap loads core Java classes, Platform loads platform modules, and Application loads application classpath or module-path classes. They normally follow parent-first delegation.

**Detailed answer:**
| Loader | Java 9+ role | Examples |
|---|---|---|
| Bootstrap | Core modules | `java.lang.String`, `java.util.List` |
| Platform | Platform modules outside core | Some JDK service/provider APIs |
| Application | App classes and dependencies | Your Spring Boot code, library jars |

In Java 8 and earlier, you often hear Extension loader instead of Platform loader. Java 9 modules changed the exact implementation, but parent delegation remains the interview model.

**Common mistakes:**
- Saying application loader loads `String`.
- Using Java 8 Extension loader terminology without noting Java 9 changes.
- Assuming custom class loaders always use parent-first; some frameworks use child-first.

**Follow-ups:**
- *Can two classes with same name coexist?* → Yes, if loaded by different class loaders; identity is class name plus defining loader.
- *Why cannot we easily override `java.lang.String`?* → Parent delegation and JVM protections prevent normal app code from replacing core classes.

### Q16. Explain parent delegation model in class loading.
`🔴 Advanced` `🔥` `Must-know SDE-1`

**Why they ask:** It is crucial for classpath conflicts and framework behavior.

**Short answer (say this first, 20-40 sec):**
In parent delegation, a class loader asks its parent to load a class first. Only if the parent cannot load it does the child try, which protects core Java classes and reduces duplicate loading.

**Detailed answer:**
```mermaid
flowchart TD
    A["Request to load class com.app.User"] --> B["Application class loader"]
    B --> C{"Already loaded?"}
    C -->|"yes"| D["Return cached Class object"]
    C -->|"no"| E["Delegate to Platform loader"]
    E --> F["Delegate to Bootstrap loader"]
    F --> G{"Bootstrap finds class?"}
    G -->|"yes"| H["Return core class"]
    G -->|"no"| I{"Platform finds class?"}
    I -->|"yes"| J["Return platform class"]
    I -->|"no"| K{"Application finds class?"}
    K -->|"yes"| L["Define and return app class"]
    K -->|"no"| M["Throw ClassNotFoundException"]
```

Custom loaders may intentionally break this model for plugins, app servers, OSGi, or hot reload. That adds flexibility but can create class-cast problems because the same class name loaded twice is not the same type.

**Common mistakes:**
- Saying delegation starts from Bootstrap; the request starts at a child and delegates upward.
- Forgetting the loaded class cache.
- Ignoring custom class loader exceptions.

**Follow-ups:**
- *Why parent-first?* → Security, consistency, and avoiding duplicate core classes.
- *What is class loader leak?* → A long-lived reference prevents an old loader and all its classes from being GCed.

### Q17. What is JIT compilation?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** It connects Java performance with runtime optimization.

**Short answer (say this first, 20-40 sec):**
JIT compiles frequently executed bytecode into optimized native machine code at runtime. The JVM profiles code first, then optimizes hot paths using techniques like inlining and escape analysis.

**Detailed answer:**
HotSpot starts with interpretation, gathers profiling data, and compiles hot methods. Optimizations include:
- method inlining,
- dead-code elimination,
- lock elision,
- escape analysis and scalar replacement,
- deoptimization if assumptions become invalid.

This is why Java programs may warm up: performance can improve after hot code is compiled. For latency-sensitive services, warm-up and GC behavior matter.

**Common mistakes:**
- Saying Java is slower because it is interpreted only.
- Benchmarking Java without warm-up.
- Ignoring deoptimization and tiered compilation.

**Follow-ups:**
- *What is escape analysis?* → It determines whether an object escapes a method/thread; non-escaping allocations can be optimized.
- *What is AOT?* → Ahead-of-time compilation; used by tools like GraalVM Native Image with different trade-offs.

### Q18. Explain JVM memory areas.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Memory questions are common in Java backend interviews.

**Short answer (say this first, 20-40 sec):**
The JVM has heap for objects, thread stacks for frames and local variables, metaspace for class metadata, PC registers for current bytecode position, and native method stacks for native calls.

**Detailed answer:**
```mermaid
flowchart TD
    A["JVM Runtime Data Areas"] --> B["Heap shared by threads"]
    B --> C["Young generation"]
    C --> D["Eden"]
    C --> E["Survivor S0"]
    C --> F["Survivor S1"]
    B --> G["Old generation"]
    A --> H["Metaspace native memory"]
    A --> I["Java stack per thread"]
    I --> J["Stack frames"]
    J --> K["Local variables and operand stack"]
    A --> L["PC register per thread"]
    A --> M["Native method stack"]
```

Heap and metaspace are shared across threads. Each thread has its own stack; stack overflow usually means deep recursion, while heap OOM means object allocation pressure or leaks.

**Common mistakes:**
- Saying metaspace is inside heap; since Java 8 it uses native memory.
- Thinking local primitive variables always live on stack; JIT may optimize differently.
- Ignoring per-thread stack memory in highly threaded apps.

**Follow-ups:**
- *Where are static fields stored?* → The field values are associated with class metadata; referenced objects are still on heap.
- *Where is the string pool?* → Since Java 7, the string intern pool is in the heap.

### Q19. What are heap generations?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Most GC explanations start with generational behavior.

**Short answer (say this first, 20-40 sec):**
Generational collectors divide heap into young and old regions because most objects die young. New objects usually start in Eden, surviving objects move through survivor spaces, and long-lived objects are promoted to old generation.

**Detailed answer:**
The generational hypothesis says:
- most objects become unreachable quickly,
- a smaller set lives much longer.

Young GC collects Eden and survivor spaces frequently. Objects that survive enough young collections, or are too large for young space, may be promoted to old generation. G1 uses regions rather than fixed contiguous young/old spaces, but still applies generational ideas. ZGC in Java 21 supports generational ZGC.

**Common mistakes:**
- Thinking every collector has the same physical generation layout.
- Saying old generation is never collected.
- Ignoring large object allocation behavior.

**Follow-ups:**
- *What is promotion failure?* → A young GC cannot promote survivors because old gen lacks space, often triggering a more expensive collection.
- *What is survivor ratio?* → A tuning parameter that affects Eden vs survivor sizing for some collectors.

### Q20. What is stack memory and what causes `StackOverflowError`?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Stack vs heap is a must-know distinction.

**Short answer (say this first, 20-40 sec):**
Each Java thread has its own stack containing method call frames, local variables, operand stack, and return information. `StackOverflowError` usually happens from very deep or infinite recursion.

**Detailed answer:**
```java
static int factorial(int n) {
    if (n <= 1) return 1;
    return n * factorial(n - 1);
}
```

Every recursive call adds a frame. With a huge `n`, the stack can overflow. Stack size is controlled with `-Xss`, but increasing it is not a substitute for fixing unbounded recursion.

**Common mistakes:**
- Calling all local objects stack-allocated; object instances are conceptually heap objects unless optimized.
- Catching `StackOverflowError` as normal control flow.
- Ignoring thread count; many threads multiply stack memory use.

**Follow-ups:**
- *Can Java objects be stack-allocated?* → JIT may eliminate or scalar-replace allocations if escape analysis proves safety.
- *What causes `OutOfMemoryError: unable to create native thread`?* → Too many threads or insufficient native memory for stacks.

### Q21. What is metaspace?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** It replaced PermGen and appears in production OOMs.

**Short answer (say this first, 20-40 sec):**
Metaspace stores class metadata in native memory. It replaced PermGen in Java 8, grows by default up to available native memory, and can be capped with `-XX:MaxMetaspaceSize`.

**Detailed answer:**
Metaspace contains class metadata such as method metadata, constant pool information, and runtime class structures. It can leak when class loaders cannot be garbage collected, common in hot-redeploy systems, plugin systems, or apps that generate many classes.

Useful flags:
- `-XX:MaxMetaspaceSize=256m`
- `-XX:+TraceClassLoading` was older; modern JDKs use unified logging like `-Xlog:class+load`.

**Common mistakes:**
- Saying PermGen exists in Java 8+ HotSpot.
- Setting metaspace too low and causing startup failures.
- Ignoring class loader leaks.

**Follow-ups:**
- *Can classes be unloaded?* → Yes, when their defining class loader becomes unreachable.
- *What is CompressedClassSpace?* → A separate area related to compressed class pointers in HotSpot.

### Q22. How does garbage collection work at a high level?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** GC is essential for diagnosing latency and memory problems.

**Short answer (say this first, 20-40 sec):**
GC finds objects reachable from roots, marks reachable objects, reclaims unreachable ones, and may compact or relocate memory to reduce fragmentation. Some phases can stop application threads.

**Detailed answer:**
```mermaid
flowchart TD
    A["Allocation request"] --> B{"Enough free space?"}
    B -->|"yes"| C["Allocate object"]
    B -->|"no"| D["Trigger GC"]
    D --> E["Find GC roots"]
    E --> F["Mark reachable objects"]
    F --> G["Reclaim unreachable objects"]
    G --> H{"Compaction needed?"}
    H -->|"yes"| I["Move live objects and update references"]
    H -->|"no"| J["Return free memory"]
    I --> J
    J --> K{"Allocation now fits?"}
    K -->|"yes"| C
    K -->|"no"| L["Throw OutOfMemoryError"]
```

GC roots include thread stacks, static references, JNI references, and JVM internals. Modern collectors try to reduce stop-the-world pauses using concurrent and parallel phases.

**Common mistakes:**
- Thinking GC prevents all memory leaks.
- Equating unreachable with immediately collected.
- Ignoring pause-time vs throughput trade-offs.

**Follow-ups:**
- *What is stop-the-world?* → Application threads pause while GC performs a phase requiring a stable object graph.
- *What is compaction?* → Moving live objects together to create contiguous free space.

### Q23. Compare Serial, Parallel, G1, ZGC, and Shenandoah.
`🔴 Advanced` `🔥` `Must-know SDE-1`

**Why they ask:** They want practical collector selection, not just names.

**Short answer (say this first, 20-40 sec):**
Parallel GC maximizes throughput with longer pauses, G1 balances throughput and predictable pauses, and ZGC/Shenandoah target very low pauses using concurrent relocation. Serial is mainly for small heaps or single-core environments.

**Detailed answer:**
| Collector | Main goal | Strength | Trade-off | Typical use |
|---|---|---|---|---|
| Serial | Simplicity | Low overhead on tiny apps | Long pauses | Small CLI tools |
| Parallel | Throughput | Uses multiple GC threads | Higher pause times | Batch jobs |
| G1 | Balanced latency | Region-based, pause target | More overhead than Parallel | Default server choice in many JDKs |
| ZGC | Very low pauses | Concurrent, colored pointers/load barriers | More CPU overhead, newer behavior | Large heaps, latency-sensitive services |
| Shenandoah | Very low pauses | Concurrent compaction | Availability depends on JDK distribution | Low-latency workloads |

Java version matters: G1 became default in JDK 9. ZGC became production-ready in JDK 15, and Java 21 includes generational ZGC.

**Common mistakes:**
- Claiming one collector is always best.
- Tuning collector before measuring GC logs.
- Using low-pause collectors for CPU-starved services without testing overhead.

**Follow-ups:**
- *Which for API latency?* → Start with G1; evaluate ZGC/Shenandoah for strict tail-latency and large heaps.
- *Which for batch throughput?* → Parallel can be strong if pauses are acceptable.

### Q24. How do you read basic GC logs?
`🔴 Advanced` `Must-know SDE-1`

**Why they ask:** Production Java roles require diagnosis, not only theory.

**Short answer (say this first, 20-40 sec):**
GC logs show when collections happen, why they happen, pause duration, heap before/after, and collector phases. Look for frequent collections, long pauses, promotion failures, allocation pressure, and whether heap returns to a stable baseline.

**Detailed answer:**
For modern JDKs:
```bash
java -Xlog:gc*,safepoint:file=gc.log:time,uptime,level,tags -jar app.jar
```

Read:
- **Cause:** allocation failure, metadata threshold, humongous allocation.
- **Before/after heap:** if after-GC keeps rising, suspect leak.
- **Pause time:** compare with SLA.
- **Frequency:** too frequent young GC may mean high allocation rate.
- **Old gen trend:** steady growth suggests retained objects.

For a Spring Boot API going from 100 ms to 5 s, check p95/p99 latency, GC pause timestamps, heap usage, thread dumps, and recent traffic or deployment changes.

**Common mistakes:**
- Looking only at average pause, not tail latency.
- Treating one full GC as proof of a leak.
- Ignoring allocation rate and object churn.

**Follow-ups:**
- *What tool can inspect heap dumps?* → Eclipse MAT, VisualVM, JDK Mission Control, or IntelliJ profiler.
- *What is a humongous object in G1?* → An object larger than half a G1 region, handled specially.

### Q25. What JVM flags are commonly used for heap sizing?
`🟡 Intermediate` `Must-know SDE-1`

**Why they ask:** Backend engineers often configure JVMs in Docker or servers.

**Short answer (say this first, 20-40 sec):**
Use `-Xms` for initial heap, `-Xmx` for max heap, and collector-specific flags only after measurement. In containers, use modern JDK container awareness and consider percentage-based heap flags.

**Detailed answer:**
Common options:
- `-Xms512m`
- `-Xmx2g`
- `-XX:MaxRAMPercentage=75`
- `-XX:+UseG1GC`
- `-XX:MaxGCPauseMillis=200`
- `-Xlog:gc*`

Set heap too low and GC becomes frequent. Set heap too high and pauses or memory pressure can increase. In Docker/Kubernetes, leave memory for metaspace, thread stacks, direct buffers, native libraries, and OS overhead.

**Common mistakes:**
- Setting `-Xmx` equal to container memory limit.
- Copy-pasting GC flags without logs.
- Over-tuning before fixing object churn.

**Follow-ups:**
- *Should `Xms` equal `Xmx`?* → Sometimes for predictable latency, but it commits memory earlier; test for your workload.
- *What else uses memory besides heap?* → Metaspace, stacks, direct buffers, code cache, native allocations.

### Q26. What are minor, major, and full GC?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** These terms appear in logs and production incidents.

**Short answer (say this first, 20-40 sec):**
Minor GC usually collects young generation. Major GC usually refers to old generation collection, and Full GC often means collecting the entire heap plus class metadata, but terminology varies by collector and log format.

**Detailed answer:**
Use careful wording because JVM logs and collectors differ. In a generational collector:
- **Young/minor GC:** collects young generation.
- **Mixed GC in G1:** collects young regions plus selected old regions.
- **Full GC:** stop-the-world fallback that compacts or collects broad heap areas.

The important interview answer is not the label but impact: pause duration, frequency, cause, and whether memory after GC drops.

**Common mistakes:**
- Treating major and full GC as universally identical.
- Ignoring collector-specific terminology.
- Saying Full GC always means leak.

**Follow-ups:**
- *Why is Full GC bad?* → It often has longer stop-the-world pauses and indicates pressure or fallback.
- *Can young GC be expensive?* → Yes, if live set is large or promotion is difficult.

### Q27. What are common Java memory leak patterns?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** GC does not prevent leaks from unintended strong references.

**Short answer (say this first, 20-40 sec):**
Java leaks happen when objects are no longer useful but remain strongly reachable. Common sources are static collections, unbounded caches, listeners not removed, `ThreadLocal`, unclosed resources, and class loader leaks.

**Detailed answer:**
Examples:
- `static Map` grows forever.
- Cache has no eviction or max size.
- Event listener registered but never unregistered.
- `ThreadLocal` value not removed in pooled threads.
- JDBC/file resources not closed.
- Class loader retained by static references during redeploy.

```java
private static final ThreadLocal<byte[]> BUFFER = new ThreadLocal<>();

void handle() {
    try {
        BUFFER.set(new byte[1024 * 1024]);
        // work
    } finally {
        BUFFER.remove();
    }
}
```

**Common mistakes:**
- Saying Java cannot leak memory.
- Relying on finalizers for cleanup.
- Adding caches without size or TTL.

**Follow-ups:**
- *How do you confirm a leak?* → Heap dump comparison, retained-size analysis, and after-GC baseline trend.
- *What is retained size?* → Memory that would be freed if a given object were collected.

## Strings, Equality and Core Collections

### Q28. Why is `String` immutable in Java?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** It connects security, caching, class loading, and hash-based collections.

**Short answer (say this first, 20-40 sec):**
`String` is immutable so it is safe to share, cache, intern, use as a map key, and pass to security-sensitive APIs. Once created, its content cannot change.

**Detailed answer:**
Benefits:
- string pool sharing,
- cached hash code,
- thread safety,
- safe file paths, class names, URLs, and SQL identifiers,
- predictable `HashMap` keys.

```java
String a = "java";
String b = a.toUpperCase();
System.out.println(a); // java
System.out.println(b); // JAVA
```

Modern Java stores `String` internally as compact byte arrays plus coder flag since Java 9, not always `char[]`; immutability is the important concept.

**Common mistakes:**
- Saying `String` methods modify the original object.
- Assuming immutability means no internal optimization.
- Confusing `final` reference with immutable object.

**Follow-ups:**
- *When use `StringBuilder`?* → For repeated mutation in a single thread.
- *When use `StringBuffer`?* → Rarely; it is synchronized and older.

### Q29. What is the String pool?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** It is a frequent output and memory question.

**Short answer (say this first, 20-40 sec):**
The String pool stores interned string instances so identical literals can share one object. String literals are automatically interned; `new String("x")` creates a separate object unless interned.

**Detailed answer:**
```java
String a = "hello";
String b = "hello";
String c = new String("hello");

System.out.println(a == b);      // true
System.out.println(a == c);      // false
System.out.println(a == c.intern()); // true
```

Since Java 7, the intern pool is in the heap. Use `equals()` for content comparison; use `==` only for reference identity.

**Common mistakes:**
- Comparing strings with `==`.
- Interning arbitrary user data and growing memory pressure.
- Thinking every runtime concatenation is automatically pooled.

**Follow-ups:**
- *Are compile-time constants pooled?* → Yes, constant expressions like `"a" + "b"` become interned literals.
- *Is `intern()` always good?* → No. It can increase pool pressure and should be used deliberately.

### Q30. Explain `equals()` and `hashCode()` contract.
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** It is essential for hash-based collections.

**Short answer (say this first, 20-40 sec):**
If two objects are equal by `equals()`, they must have the same `hashCode()`. Equal objects must remain equal while used as keys in hash-based collections.

**Detailed answer:**
Contract:
- `equals` is reflexive, symmetric, transitive, consistent, and handles `null`.
- Equal objects must have equal hash codes.
- Unequal objects may have same hash code, causing collisions.

```java
import java.util.Objects;

final class UserKey {
    private final long id;
    private final String tenant;

    UserKey(long id, String tenant) {
        this.id = id;
        this.tenant = tenant;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserKey other)) return false;
        return id == other.id && Objects.equals(tenant, other.tenant);
    }

    @Override public int hashCode() {
        return Objects.hash(id, tenant);
    }
}
```

Records generate correct `equals` and `hashCode` for components.

**Common mistakes:**
- Overriding `equals` but not `hashCode`.
- Using mutable fields in hash keys.
- Using `getClass()` vs `instanceof` without understanding inheritance implications.

**Follow-ups:**
- *Can two unequal objects have same hash code?* → Yes, collision is allowed.
- *Why are mutable keys dangerous?* → If hash-relevant fields change, the object may become unreachable in the map bucket.

### Q31. How does `HashMap` work internally?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** It is one of the most common Java collection questions.

**Short answer (say this first, 20-40 sec):**
`HashMap` stores entries in an array of buckets. It hashes the key, maps the hash to a bucket, resolves collisions using linked lists or red-black trees, and resizes when size exceeds capacity times load factor.

**Detailed answer:**
Default initial capacity is 16 and default load factor is 0.75. Java 8+ treeifies long collision chains when a bucket reaches 8 entries and the table is at least 64; it untreeifies when entries shrink to 6.

```mermaid
flowchart TD
    A["put key value"] --> B["Compute spread hash"]
    B --> C["Index equals hash and capacity minus one"]
    C --> D{"Bucket empty?"}
    D -->|"yes"| E["Insert new node"]
    D -->|"no"| F{"Same key found?"}
    F -->|"yes"| G["Replace value"]
    F -->|"no"| H{"Collision chain length at least 8?"}
    H -->|"yes"| I{"Table capacity at least 64?"}
    I -->|"yes"| J["Treeify bucket"]
    I -->|"no"| K["Resize table"]
    H -->|"no"| L["Append node"]
    E --> M{"Size greater than capacity times 0.75?"}
    G --> M
    J --> M
    K --> M
    L --> M
    M -->|"yes"| N["Resize and rehash bins"]
    M -->|"no"| O["Done"]
    N --> O
```

Average `get`/`put` is O(1), worst case improved to O(log n) for treeified buckets with comparable keys.

**Common mistakes:**
- Saying HashMap always uses linked lists.
- Forgetting resize threshold uses load factor.
- Assuming HashMap is thread-safe.

**Follow-ups:**
- *Can HashMap store null?* → One null key and multiple null values.
- *What changed in Java 8?* → Buckets can become red-black trees under high collision.

### Q32. Why should keys in `HashMap` be immutable?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** It tests practical bug awareness.

**Short answer (say this first, 20-40 sec):**
If a key's hash-relevant fields change after insertion, the map may search the wrong bucket and fail to find the entry. Immutable keys prevent that class of bug.

**Detailed answer:**
```java
import java.util.*;

class Key {
    int id;
    Key(int id) { this.id = id; }
    @Override public boolean equals(Object o) {
        return o instanceof Key other && id == other.id;
    }
    @Override public int hashCode() { return id; }
}

Map<Key, String> map = new HashMap<>();
Key key = new Key(1);
map.put(key, "value");
key.id = 2;
System.out.println(map.get(key)); // often null
```

The entry remains in the bucket for hash `1`, but lookup uses hash `2`.

**Common mistakes:**
- Mutating fields used in `equals`/`hashCode`.
- Using arrays directly as keys without wrapping.
- Assuming `final` reference means key state cannot mutate.

**Follow-ups:**
- *Are records good map keys?* → Yes, if their components are themselves immutable or treated immutably.
- *Can `StringBuilder` be a good key?* → Usually no, because it is mutable and does not override `equals` by content.

### Q33. How does `HashMap` resizing work?
`🔴 Advanced` `🔥` `Must-know SDE-1`

**Why they ask:** It checks beyond the basic hash-bucket answer.

**Short answer (say this first, 20-40 sec):**
When size exceeds threshold, HashMap doubles capacity and redistributes buckets. Since capacities are powers of two, Java 8 can split nodes into same index or old index plus old capacity based on one hash bit.

**Detailed answer:**
Threshold is `capacity * loadFactor`. With default capacity 16 and load factor 0.75, first threshold is 12. Resizing is expensive because bucket structure must be moved, so choose initial capacity if expected size is known.

```java
int expected = 10_000;
int capacity = (int) (expected / 0.75f) + 1;
Map<String, Integer> counts = new HashMap<>(capacity);
```

This reduces repeated resizes during bulk loading. Very high load factors save memory but increase collision probability.

**Common mistakes:**
- Thinking resize happens at 75 percent of current size rather than capacity.
- Forgetting capacity is rounded to a power of two.
- Oversizing maps unnecessarily and wasting memory.

**Follow-ups:**
- *Does resize recompute full hashes?* → Hash is stored in nodes; redistribution uses hash bits.
- *Why power-of-two capacity?* → It enables fast index calculation with `hash & (n - 1)`.

### Q34. Is `HashMap` thread-safe?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Collections and concurrency often meet in backend systems.

**Short answer (say this first, 20-40 sec):**
No, `HashMap` is not thread-safe for concurrent modification. Use `ConcurrentHashMap`, synchronization, or immutable maps depending on the use case.

**Detailed answer:**
Concurrent modification can cause lost updates, inconsistent reads, or structural corruption in older implementations. Even if corruption is less likely in modern JDKs, data races are still incorrect.

```java
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

ConcurrentMap<String, Integer> counts = new ConcurrentHashMap<>();
counts.merge("java", 1, Integer::sum);
```

For read-only data, build a map safely and publish it as immutable using `Map.copyOf`.

**Common mistakes:**
- Wrapping with `Collections.synchronizedMap` but iterating without external synchronization.
- Using `volatile HashMap` and assuming internal operations become thread-safe.
- Using `ConcurrentHashMap` with non-atomic get-then-put sequences.

**Follow-ups:**
- *Does `ConcurrentHashMap` allow null?* → No null keys or values.
- *What is fail-fast?* → Iterators may throw `ConcurrentModificationException` on structural modification.

### Q35. Compare `ArrayList` and `LinkedList`.
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** It tests Big-O plus real-world performance intuition.

**Short answer (say this first, 20-40 sec):**
`ArrayList` uses a resizable array and is usually better for random access and iteration. `LinkedList` uses nodes and is better only for frequent insert/delete when you already have the node position, but it has poor cache locality.

**Detailed answer:**
| Operation | ArrayList | LinkedList |
|---|---:|---:|
| Get by index | O(1) | O(n) |
| Append | Amortized O(1) | O(1) |
| Insert middle by index | O(n) shift | O(n) traversal plus O(1) link |
| Memory | Lower | Higher due to nodes |
| Iteration | Cache-friendly | Pointer chasing |

In most backend code, prefer `ArrayList` unless you specifically need queue/deque behavior; then consider `ArrayDeque`.

**Common mistakes:**
- Saying LinkedList is always faster for insertion.
- Ignoring traversal cost.
- Using `LinkedList` as a stack instead of `ArrayDeque`.

**Follow-ups:**
- *What is ArrayList growth policy?* → Implementation detail, but it grows by allocating a larger array and copying elements.
- *Is ArrayList synchronized?* → No.

### Q36. What is `TreeMap` and when is it used?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Ordered maps are common in range-query problems.

**Short answer (say this first, 20-40 sec):**
`TreeMap` is a sorted map implemented using a red-black tree. It gives O(log n) get, put, remove, and supports ordered operations like floor, ceiling, first, last, and range views.

**Detailed answer:**
```java
import java.util.*;

TreeMap<Integer, String> map = new TreeMap<>();
map.put(10, "ten");
map.put(20, "twenty");
System.out.println(map.floorKey(15));   // 10
System.out.println(map.ceilingKey(15)); // 20
```

Use it when sorted order or range queries matter. Use `HashMap` when only key lookup matters and order is irrelevant.

**Common mistakes:**
- Expecting O(1) lookup.
- Mutating keys after insertion.
- Providing a comparator inconsistent with equals and getting surprising map behavior.

**Follow-ups:**
- *Can TreeMap have null keys?* → Natural ordering does not allow null; a custom comparator may handle null but avoid it.
- *What is `subMap`?* → A view over a key range, backed by the original map.

### Q37. How does `LinkedHashMap` work and how can it implement LRU?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** It appears in cache design and LLD warm-ups.

**Short answer (say this first, 20-40 sec):**
`LinkedHashMap` combines hash table lookup with a doubly linked list for predictable iteration order. With `accessOrder=true` and `removeEldestEntry`, it can implement a simple LRU cache.

**Detailed answer:**
```java
import java.util.LinkedHashMap;
import java.util.Map;

class LruCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;

    LruCache(int capacity) {
        super(capacity, 0.75f, true);
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }
}
```

`accessOrder=false` preserves insertion order. `accessOrder=true` moves entries to the end when accessed.

**Common mistakes:**
- Forgetting `accessOrder=true` for LRU.
- Treating this as a production distributed cache.
- Ignoring synchronization for shared access.

**Follow-ups:**
- *What is insertion order?* → Iteration follows first insertion order.
- *What is access order?* → Iteration follows least-recently accessed to most-recently accessed.

### Q38. How does `PriorityQueue` work?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Heap-based priority queues are common in DSA and backend scheduling.

**Short answer (say this first, 20-40 sec):**
Java `PriorityQueue` is a binary heap. The head is the least element by natural order or comparator; offer and poll are O(log n), peek is O(1).

**Detailed answer:**
```java
import java.util.Comparator;
import java.util.PriorityQueue;

PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
maxHeap.offer(3);
maxHeap.offer(10);
maxHeap.offer(5);
System.out.println(maxHeap.poll()); // 10
```

Iteration order is not sorted. To consume in priority order, repeatedly `poll()`. It does not allow null elements and is not thread-safe.

**Common mistakes:**
- Expecting iterator to return sorted order.
- Forgetting Java's default is min-heap.
- Mutating an element's priority while inside the queue.

**Follow-ups:**
- *What for thread-safe priority queue?* → `PriorityBlockingQueue`, with caveats about unbounded capacity by default.
- *Can comparator be inconsistent?* → Yes, but it can cause surprising ordering behavior.

### Q39. What are fail-fast and fail-safe iterators?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** It tests collection behavior under modification.

**Short answer (say this first, 20-40 sec):**
Fail-fast iterators detect structural modification and may throw `ConcurrentModificationException`. Fail-safe is an informal term for iterators that work on a snapshot or weakly consistent view, such as `CopyOnWriteArrayList` or `ConcurrentHashMap`.

**Detailed answer:**
```java
import java.util.*;

List<Integer> list = new ArrayList<>(List.of(1, 2, 3));
for (Integer value : list) {
    if (value == 2) {
        list.remove(value); // may throw ConcurrentModificationException
    }
}
```

Correct removal:
```java
Iterator<Integer> iterator = list.iterator();
while (iterator.hasNext()) {
    if (iterator.next() == 2) {
        iterator.remove();
    }
}
```

Fail-fast is best-effort, not a correctness guarantee.

**Common mistakes:**
- Calling fail-safe an official Java term.
- Depending on `ConcurrentModificationException` for synchronization.
- Modifying a list inside enhanced for loop.

**Follow-ups:**
- *How does CopyOnWriteArrayList iterator behave?* → It iterates over an immutable snapshot.
- *How does ConcurrentHashMap iterator behave?* → It is weakly consistent and does not throw fail-fast exceptions.

### Q40. Comparable vs Comparator?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Sorting custom objects is a basic Java skill.

**Short answer (say this first, 20-40 sec):**
`Comparable` defines natural ordering inside the class using `compareTo`. `Comparator` defines external, possibly multiple orderings.

**Detailed answer:**
```java
import java.util.*;

record Employee(String name, int salary) implements Comparable<Employee> {
    @Override
    public int compareTo(Employee other) {
        return this.name.compareTo(other.name);
    }
}

List<Employee> employees = new ArrayList<>();
employees.sort(Comparator.comparingInt(Employee::salary).reversed());
```

Use `Integer.compare(a, b)` instead of `a - b` to avoid overflow.

**Common mistakes:**
- Returning only `1` or `-1` and never `0`.
- Subtracting ints in comparator and causing overflow.
- Making comparator inconsistent with equals in sorted sets/maps.

**Follow-ups:**
- *Can a class have multiple Comparable orders?* → No, only one natural order; use comparators for alternatives.
- *What does comparator consistent with equals mean?* → `compare(a,b)==0` should generally imply `a.equals(b)` for sorted collections.

## Revision Cheat Sheet

- OOP pillars: encapsulation protects state; abstraction exposes contracts; inheritance specializes; polymorphism dispatches behavior.
- Prefer composition over inheritance unless there is a stable "is-a" relationship.
- Overloading is compile-time; overriding is runtime for instance methods.
- JDK = developer tools plus runtime; JRE = runtime libraries plus JVM; JVM = bytecode execution engine.
- Class loading: loading, linking, initialization; linking includes verification, preparation, resolution.
- Parent delegation protects core classes and reduces duplicate loading.
- JVM memory: heap, stacks, metaspace, PC registers, native stacks.
- Young/old generation exists because most objects die young.
- GC marks reachable objects from roots and reclaims unreachable objects; pause/throughput trade-offs matter.
- G1 is a balanced default; Parallel is throughput-oriented; ZGC/Shenandoah target low pauses.
- `String` is immutable, poolable, thread-safe, and safe as a key.
- `equals` and `hashCode`: equal objects must have equal hash codes.
- `HashMap` uses buckets, collisions, treeification at 8, untreeification at 6, and resize near load factor 0.75.
- Prefer `ArrayList` for most list use; prefer `ArrayDeque` for stack/queue; use `TreeMap` for sorted keys.
- Fail-fast iterators are best-effort bug detectors, not synchronization.

## Flashcards

- Q → What does JVM execute? A → Bytecode from `.class` files.
- Q → Which is bigger conceptually, JDK or JRE? A → JDK includes JRE plus development tools.
- Q → Where are objects allocated? A → Conceptually on heap, unless optimized by JIT.
- Q → What replaced PermGen in Java 8? A → Metaspace.
- Q → What is default HashMap load factor? A → 0.75.
- Q → When does HashMap treeify a bucket? A → Around 8 entries if table capacity is at least 64.
- Q → What is String pool used for? A → Sharing interned string instances.
- Q → Equal objects must have what? A → Same hash code.
- Q → ArrayList random access complexity? A → O(1).
- Q → TreeMap lookup complexity? A → O(log n).
- Q → PriorityQueue default order? A → Min-heap by natural order.
- Q → Is HashMap thread-safe? A → No.
- Q → What causes StackOverflowError? A → Usually unbounded or very deep recursion.
- Q → What is Full GC? A → Broad stop-the-world collection, exact meaning varies by collector.
- Q → What is the safest resource cleanup pattern? A → `try-with-resources`.




