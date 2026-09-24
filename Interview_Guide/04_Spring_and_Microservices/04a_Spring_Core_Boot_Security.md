# 04a · Spring Core, Boot & Security

This part covers Spring Core, Boot internals, MVC, validation, logging, API clients, and Security for SDE-1/SDE-2 interviews. Examples connect to the AI Email Reply Assistant Spring Boot backend that calls Gemini API.

**Legend:** 🟢 Beginner · 🟡 Intermediate · 🔴 Advanced · ⚫ FAANG · 🔥 = very frequently asked

## Table of Contents
- [Spring Core](#spring-core)
- [Spring Boot](#spring-boot)
- [MVC, Validation, Logging, AOP & API Clients](#mvc-validation-logging-aop--api-clients)
- [Spring Security](#spring-security)
- [Production Scenarios](#production-scenarios)
- [Revision Cheat Sheet](#revision-cheat-sheet)
- [Flashcards](#flashcards)

## Spring Core

### Q1. What is Spring IoC?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** It is the foundation behind every Spring feature.

**Short answer (say this first, 20-40 sec):**
IoC means the container controls object creation and wiring instead of application classes manually creating dependencies. Classes declare dependencies, and Spring provides configured beans. This improves loose coupling, testing, lifecycle handling, and configuration management.

**Detailed answer:**
- Without IoC, `EmailController` creates `EmailService` and Gemini clients with `new`, tightly coupling code.
- With IoC, the controller asks for `EmailReplyService` in its constructor and Spring injects it.
- The container uses component scanning, Java config, auto-configuration, and bean definitions.
```mermaid
flowchart LR
    A["Class declares dependency"] --> B["Spring container creates beans"]
    B --> C["Dependencies injected"]
    C --> D["Application uses ready objects"]
```

**Common mistakes:**
- Saying IoC and DI are exactly the same; DI is a way to implement IoC.
- Manually instantiating Spring services.

**Follow-ups:**
- *Why is IoC testable?* → Tests can pass mocks or fakes without changing production code.
- *Can IoC exist without Spring?* → Yes, factories or manual composition can invert control too.

### Q2. What are the dependency injection types, and why prefer constructor injection?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** DI style affects maintainability and startup safety.

**Short answer (say this first, 20-40 sec):**
Spring supports constructor, setter, and field injection. Constructor injection is preferred for required dependencies because dependencies are explicit, can be `final`, are easy to test, and fail fast at startup. Setter injection fits optional dependencies; field injection hides dependencies and makes tests harder.

**Detailed answer:**
```java
@Service
class EmailReplyService {
    private final GeminiClient geminiClient;
    EmailReplyService(GeminiClient geminiClient) { this.geminiClient = geminiClient; }
}
```
| Type | Use | Risk |
|---|---|---|
| Constructor | Required dependencies | Reveals circular dependencies |
| Setter | Optional dependencies | Partially initialized object |
| Field | Legacy/demo | Hidden, reflection-based, not `final` |

**Common mistakes:**
- Using field injection because it is shorter.
- Making mandatory collaborators optional.

**Follow-ups:**
- *Do you need `@Autowired` on one constructor?* → No, modern Spring infers it.
- *What does many constructor args suggest?* → The class may have too many responsibilities.

### Q3. How does Spring handle circular dependencies in Boot 2.6+?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Circular dependencies appear often in service-layer designs.

**Short answer (say this first, 20-40 sec):**
A circular dependency means bean A needs B and B needs A. Constructor cycles cannot be resolved safely. Since Spring Boot 2.6, circular references are disabled by default, so the app fails fast unless `spring.main.allow-circular-references=true` is set; the right fix is usually redesign.

**Detailed answer:**
```mermaid
flowchart LR
    A["EmailService"] --> B["TemplateService"]
    B --> A
    A --> C["Boot 2.6 plus fails by default"]
```
Fixes:
- Extract shared logic to a third bean.
- Publish application events instead of direct callbacks.
- Use `@Lazy` only as a temporary workaround.

**Common mistakes:**
- Enabling circular references instead of removing the cycle.
- Hiding cycles with field injection.

**Follow-ups:**
- *Why do setter cycles sometimes work?* → Spring may expose early references, but it is fragile.
- *Why constructor cycles fail?* → Neither object can be constructed first.

### Q4. What are Spring bean scopes?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Scope affects thread safety, memory, and request behavior.

**Short answer (say this first, 20-40 sec):**
Bean scope defines how many instances Spring creates and how long they live. Default scope is singleton: one bean per application context. Other common scopes are prototype, request, session, application, and websocket.

**Detailed answer:**
| Scope | Meaning | Typical use |
|---|---|---|
| singleton | One per context | Stateless services |
| prototype | New per lookup | Stateful helper |
| request | One per HTTP request | Request data |
| session | One per HTTP session | Web session state |
- A singleton injecting a prototype receives only one prototype instance at construction.
- Use `ObjectProvider<T>` when a singleton needs fresh prototype instances.

**Common mistakes:**
- Storing request-specific state in singleton fields.
- Assuming prototype means per HTTP request.

**Follow-ups:**
- *Is singleton thread-safe?* → Not automatically; stateless design makes it safe.
- *Who destroys prototype beans?* → The caller, after Spring initializes them.

### Q5. Explain the Spring bean lifecycle.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Lifecycle explains proxies, initialization bugs, and shutdown cleanup.

**Short answer (say this first, 20-40 sec):**
Spring instantiates a bean, populates dependencies, calls aware callbacks, applies BeanPostProcessor before initialization, runs init callbacks, applies BeanPostProcessor after initialization, then the bean is ready. On shutdown, destroy callbacks run for managed singletons.

**Detailed answer:**
```mermaid
flowchart TD
    A["instantiate"] --> B["populate dependencies"]
    B --> C["Aware callbacks"]
    C --> D["BeanPostProcessor before"]
    D --> E["@PostConstruct afterPropertiesSet init"]
    E --> F["BeanPostProcessor after"]
    F --> G["ready"]
    G --> H["@PreDestroy destroy"]
```
- `@PostConstruct`: validate config or initialize lightweight resources.
- BPP after initialization often creates proxies for AOP, transactions, security.
- `@PreDestroy`: close resources gracefully.

**Common mistakes:**
- Doing slow network calls during startup.
- Expecting destroy callbacks for prototype beans.

**Follow-ups:**
- *Where do proxies appear?* → Usually after initialization via post-processors.
- *What is `InitializingBean`?* → Interface alternative to `@PostConstruct`.

### Q6. `@Component` vs `@Bean`: what is the difference?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Both create beans but are used differently.

**Short answer (say this first, 20-40 sec):**
`@Component` marks a class for component scanning. `@Bean` marks a method inside configuration that explicitly creates an object. Use `@Component` for your own classes and `@Bean` for third-party classes or custom construction logic.

**Detailed answer:**
```java
@Component
class PromptBuilder { }

@Configuration
class ClientConfig {
    @Bean RestClient geminiRestClient(RestClient.Builder builder) {
        return builder.baseUrl("https://generativelanguage.googleapis.com").build();
    }
}
```
- `@Service`, `@Repository`, `@Controller` are specialized components.
- `@Bean` gives full control over constructor args, builder calls, and conditions.

**Common mistakes:**
- Creating duplicate beans with both styles.
- Trying to annotate third-party library classes.

**Follow-ups:**
- *Why use `@Repository`?* → It documents persistence role and enables exception translation.
- *Can `@Bean` method take parameters?* → Yes, Spring injects dependencies into method parameters.

### Q7. How do `@Primary` and `@Qualifier` work?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Ambiguous beans are common in real apps.

**Short answer (say this first, 20-40 sec):**
When multiple beans match a type, Spring needs a tie-breaker. `@Primary` marks the default candidate, while `@Qualifier` selects a specific bean at injection point. Use `@Primary` for common default and qualifiers for explicit special cases.

**Detailed answer:**
```java
interface AiClient { String generate(String prompt); }
@Component @Primary class GeminiClient implements AiClient { public String generate(String p){return p;} }
@Component("mockAiClient") class MockAiClient implements AiClient { public String generate(String p){return "mock";} }
@Service class ReplyService { ReplyService(@Qualifier("mockAiClient") AiClient client) {} }
```
- Missing tie-breaker causes `NoUniqueBeanDefinitionException`.
- Qualifiers are useful for Gemini vs mock/local LLM clients.

**Common mistakes:**
- Overusing bean names instead of clear qualifiers.
- Declaring multiple primary beans of same type.

**Follow-ups:**
- *Can qualifiers be custom annotations?* → Yes, for type-safe selection.
- *What if no bean exists?* → `NoSuchBeanDefinitionException`.

### Q8. ApplicationContext vs BeanFactory?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** It checks core container knowledge.

**Short answer (say this first, 20-40 sec):**
`BeanFactory` is the basic IoC container. `ApplicationContext` extends it with events, resources, i18n, environment, web integration, and automatic post-processor registration. Spring Boot applications use `ApplicationContext`.

**Detailed answer:**
| Feature | BeanFactory | ApplicationContext |
|---|---:|---:|
| Bean wiring | yes | yes |
| Events | limited | yes |
| Environment/profiles | no | yes |
| Web support | no | yes |
| Boot default | no | yes |
- `ApplicationContext` usually pre-instantiates singleton beans, giving fail-fast startup.

**Common mistakes:**
- Thinking they are unrelated.
- Using BeanFactory directly in Boot apps.

**Follow-ups:**
- *Why fail fast?* → Broken config appears during deployment, not first request.
- *What is web context?* → An ApplicationContext integrated with servlet/reactive web runtime.

### Q9. What are BeanPostProcessors?
`🔴 Advanced` `Medium` `Stretch`

**Why they ask:** Many Spring features are built through post-processing.

**Short answer (say this first, 20-40 sec):**
A BeanPostProcessor can modify or wrap beans before and after initialization. Spring uses them for annotations, AOP proxies, transactions, validation, and injection behavior. Application code rarely needs custom BPPs unless building framework infrastructure.

**Detailed answer:**
```java
@Component
class LoggingBeanPostProcessor implements BeanPostProcessor {
    public Object postProcessAfterInitialization(Object bean, String name) {
        return bean;
    }
}
```
- Runs for many beans, so keep it fast and safe.
- Can return a proxy instead of the original bean.
- Incorrect BPPs can break container startup.

**Common mistakes:**
- Putting business logic in BPP.
- Assuming it runs for objects not managed by Spring.

**Follow-ups:**
- *How is AOP related?* → Auto-proxy creators are BeanPostProcessors.
- *Can BPP change bean instance?* → Yes, it can wrap or replace it.

## Spring Boot

### Q10. What are Spring Boot starters?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Starters explain why Boot setup is fast.

**Short answer (say this first, 20-40 sec):**
Starters are dependency bundles for capabilities like web, security, JPA, actuator, and validation. They bring compatible libraries through Boot dependency management. For example, web starter includes MVC, embedded Tomcat, Jackson, and related web dependencies.

**Detailed answer:**
- Email Reply Assistant likely uses `spring-boot-starter-web`, `validation`, `actuator`, `security`, and `test`.
- Starters reduce manual dependency version conflicts.
- They are mostly dependency descriptors, not magic runtime code.

**Common mistakes:**
- Adding both servlet web and reactive web starters accidentally.
- Overriding managed versions without reason.

**Follow-ups:**
- *Where are versions managed?* → In Spring Boot dependency BOM.
- *Can teams create custom starters?* → Yes, with auto-configuration and metadata.

### Q11. What does `@SpringBootApplication` include?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** It is the main Boot annotation.

**Short answer (say this first, 20-40 sec):**
It combines `@SpringBootConfiguration`, `@EnableAutoConfiguration`, and `@ComponentScan`. It marks the main configuration class, enables auto-configuration, and scans components from its package downward.

**Detailed answer:**
```java
@SpringBootApplication
public class EmailReplyApplication {
    public static void main(String[] args) { SpringApplication.run(EmailReplyApplication.class, args); }
}
```
```mermaid
flowchart TD
    A["@SpringBootApplication"] --> B["configuration"]
    A --> C["auto configuration"]
    A --> D["component scan"]
```
Place the main class at the root package so controllers, services, and repositories are scanned.

**Common mistakes:**
- Main class in a deep package, missing sibling packages.
- Thinking component scan covers every classpath jar.

**Follow-ups:**
- *Can scan base package be changed?* → Yes, but root package layout is cleaner.
- *Can auto-config be excluded?* → Yes with `exclude` or properties.

### Q12. How does auto-configuration work in Boot 2 vs Boot 3?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** It tests Boot internals and version awareness.

**Short answer (say this first, 20-40 sec):**
Auto-configuration imports configuration classes based on classpath, properties, existing beans, and environment. Boot 2 commonly used `META-INF/spring.factories`; Boot 3 uses `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`. Conditions like `@ConditionalOnClass` and `@ConditionalOnMissingBean` decide what applies.

**Detailed answer:**
```mermaid
flowchart TD
    A["EnableAutoConfiguration"] --> B["Read imports metadata"]
    B --> C["Evaluate @ConditionalOnClass"]
    C --> D["Evaluate @ConditionalOnMissingBean"]
    D --> E["Register matching beans"]
```
Common conditions: `@ConditionalOnProperty`, `@ConditionalOnWebApplication`, `@ConditionalOnBean`, `@ConditionalOnMissingBean`.

**Common mistakes:**
- Thinking auto-config ignores user-defined beans.
- Mixing Boot 2 and Boot 3 metadata files.

**Follow-ups:**
- *How debug decisions?* → Use `--debug` or Actuator conditions endpoint.
- *Why did adding a starter change behavior?* → It added classes that matched conditions.

### Q13. Describe Spring Boot startup flow.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Startup understanding helps debug failures.

**Short answer (say this first, 20-40 sec):**
Boot creates `SpringApplication`, prepares environment and profiles, creates the application context, loads bean definitions, refreshes the context, starts the embedded server, then runs application runners and publishes ready events.

**Detailed answer:**
```mermaid
flowchart TD
    A["main"] --> B["prepare environment"]
    B --> C["create ApplicationContext"]
    C --> D["load bean definitions"]
    D --> E["refresh context and create beans"]
    E --> F["start embedded server"]
    F --> G["run runners"]
    G --> H["ready"]
```
- Config errors happen early.
- Bean creation errors happen during refresh.
- Port conflicts happen when server starts.

**Common mistakes:**
- Heavy external network checks in startup.
- Ignoring active profile logs.

**Follow-ups:**
- *When do CommandLineRunner beans run?* → After context startup.
- *What is context refresh?* → Processing bean definitions, creating singletons, lifecycle callbacks.

### Q14. How do profiles work?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Profiles are essential for local, Docker, test, and prod configs.

**Short answer (say this first, 20-40 sec):**
Profiles activate environment-specific properties and beans. You can use `application-dev.yml`, profile documents, `@Profile`, and `SPRING_PROFILES_ACTIVE`. Multiple profiles can be active together.

**Detailed answer:**
```yaml
spring:
  config:
    activate:
      on-profile: prod
gemini:
  base-url: https://generativelanguage.googleapis.com
```
```java
@Bean @Profile("local") AiClient mockAiClient() { return p -> "mock"; }
```
- Use env vars for Docker: `SPRING_PROFILES_ACTIVE=prod`.
- Do not store secrets in profile files.

**Common mistakes:**
- Expecting inactive profile config to load.
- Committing API keys in profile YAML.

**Follow-ups:**
- *Can profiles be combined?* → Yes, e.g., `prod,azure`.
- *How inspect active profiles?* → Startup logs or secured Actuator env.

### Q15. Explain externalized config precedence.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Many production bugs are config precedence issues.

**Short answer (say this first, 20-40 sec):**
Boot reads properties from many sources; higher-precedence sources override lower ones. Command-line args, system properties, environment variables, external files, profile files, packaged defaults, and code defaults are common sources. This lets one jar run across environments.

**Detailed answer:**
Practical high-to-low order:
1. Command line `--server.port=9090`.
2. JVM system properties.
3. OS environment variables.
4. External config files.
5. Packaged profile-specific config.
6. Packaged `application.yml`.
Relaxed binding maps `GEMINI_API_KEY` to `gemini.api-key`.

**Common mistakes:**
- Debugging YAML while an env var overrides it.
- Printing secrets to confirm config.

**Follow-ups:**
- *How inspect property sources?* → Secured Actuator env endpoint.
- *Why env vars in Docker?* → Immutable image, runtime-specific config.

### Q16. Why use `@ConfigurationProperties`?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Typed config is production-grade Spring Boot practice.

**Short answer (say this first, 20-40 sec):**
`@ConfigurationProperties` binds related config into a typed object and supports validation. It is cleaner and safer than scattered `@Value` fields, especially for URLs, API keys, timeouts, and limits.

**Detailed answer:**
```java
@Validated
@ConfigurationProperties(prefix = "gemini")
public record GeminiProperties(
    @NotBlank String apiKey,
    @NotBlank String baseUrl,
    @Positive int readTimeoutMs) { }
```
```java
@SpringBootApplication
@ConfigurationPropertiesScan
class EmailReplyApplication { }
```
- Keeps Gemini settings centralized.
- Validation fails fast if required config is missing.

**Common mistakes:**
- Forgetting `@ConfigurationPropertiesScan`.
- Using primitive defaults that hide missing properties.

**Follow-ups:**
- *Can records be used?* → Yes in modern Boot.
- *Which validation package in Boot 3?* → `jakarta.validation`.

### Q17. What are Actuator endpoints and how should they be secured?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Actuator is useful but can leak sensitive data.

**Short answer (say this first, 20-40 sec):**
Actuator exposes operational endpoints like health, metrics, info, env, beans, loggers, and prometheus. Expose only what is needed; allow basic health if required by load balancers, and protect sensitive endpoints with authentication and network controls.

**Detailed answer:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when_authorized
```
```java
@Bean SecurityFilterChain actuator(HttpSecurity http) throws Exception {
    return http.securityMatcher("/actuator/**")
        .authorizeHttpRequests(a -> a.requestMatchers("/actuator/health").permitAll().anyRequest().hasRole("OPS"))
        .httpBasic(Customizer.withDefaults()).build();
}
```

**Common mistakes:**
- Exposing `/actuator/env` publicly.
- Showing health details to anonymous users.

**Follow-ups:**
- *Readiness vs liveness?* → Ready means can receive traffic; live means should not be restarted.
- *Which endpoint debugs auto-config?* → `/actuator/conditions`.

### Q18. Spring Boot 2 vs 3 differences?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Many teams are migrating to Boot 3.

**Short answer (say this first, 20-40 sec):**
Boot 3 uses Spring Framework 6, requires Java 17+, and moves from `javax.*` to `jakarta.*`. It also uses new auto-configuration imports, supports `ProblemDetail`, and favors modern Spring Security `SecurityFilterChain` configuration.

**Detailed answer:**
| Area | Boot 2 | Boot 3 |
|---|---|---|
| Java | 8 or 11 depending version | 17+ |
| EE APIs | `javax.*` | `jakarta.*` |
| Auto-config metadata | `spring.factories` | `AutoConfiguration.imports` |
| Security style | adapter often seen | `SecurityFilterChain` bean |
| Errors | custom DTO common | `ProblemDetail` available |

**Common mistakes:**
- Upgrading Boot without upgrading Java runtime.
- Leaving old `javax.servlet` or `javax.validation` imports.

**Follow-ups:**
- *Why dependencies break?* → Libraries must support Jakarta namespaces.
- *Is `WebSecurityConfigurerAdapter` used in Boot 3?* → No, use component-based config.

### Q19. How do you structure packages in a Boot app?
`🟢 Beginner` `Medium` `Must-know SDE-1`

**Why they ask:** Bad package structure causes scanning and maintainability issues.

**Short answer (say this first, 20-40 sec):**
Put the main class in the root package and group code by feature or layer consistently. For small apps, layer packages are fine; for growing apps, feature packages reduce coupling and make extraction easier.

**Detailed answer:**
Example feature-oriented layout:
```text
com.azam.emailreply
  auth
  reply
    ReplyController
    ReplyService
    GeminiClient
  config
  common.error
```
- Main class at `com.azam.emailreply` scans all children.
- Keep DTOs separate from entities.

**Common mistakes:**
- Main class below service packages.
- Putting all classes in one package.

**Follow-ups:**
- *Layer vs feature package?* → Layer is simple; feature scales better for bounded contexts.
- *Where put shared exceptions?* → A small `common` or `error` package.

### Q20. What is conditional bean creation?
`🟡 Intermediate` `Medium` `Must-know SDE-1`

**Why they ask:** It is central to auto-configuration and environment-specific beans.

**Short answer (say this first, 20-40 sec):**
Conditional annotations create beans only when conditions match, such as a property being enabled, a class being present, or another bean missing. This powers Boot auto-configuration and is useful for optional features like mock Gemini clients.

**Detailed answer:**
```java
@Bean
@ConditionalOnProperty(name = "gemini.mock", havingValue = "true")
AiClient mockAiClient() { return p -> "mock reply"; }

@Bean
@ConditionalOnMissingBean(AiClient.class)
AiClient geminiAiClient() { return new GeminiAiClient(); }
```
- Conditions are evaluated during context startup.
- User-defined beans often override defaults with `@ConditionalOnMissingBean`.

**Common mistakes:**
- Creating overlapping beans accidentally.
- Not checking condition report when bean is missing.

**Follow-ups:**
- *Where see condition outcome?* → `--debug` or Actuator conditions.
- *Difference from profiles?* → Profiles are environment labels; conditions are arbitrary rules.

### Q21. How does `@Valid` request validation work?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Every REST API needs boundary validation.

**Short answer (say this first, 20-40 sec):**
`@Valid` triggers Bean Validation on request DTOs before controller logic runs. Invalid DTOs throw validation exceptions that should be mapped to 400 responses. In Boot 3, use `jakarta.validation` annotations.

**Detailed answer:**
```java
public record EmailReplyRequest(
    @NotBlank String emailBody,
    @Size(max = 40) String tone) { }

@PostMapping("/api/replies")
ResponseEntity<EmailReplyResponse> create(@Valid @RequestBody EmailReplyRequest request) {
    return ResponseEntity.ok(service.generate(request));
}
```
- Use validation groups for create vs update.
- Validate DTOs, not JPA entities, at API boundary.

**Common mistakes:**
- Forgetting `@Valid`.
- Missing `spring-boot-starter-validation`.

**Follow-ups:**
- *Status for validation failure?* → 400 Bad Request.
- *How validate nested objects?* → Put `@Valid` on nested DTO field.

### Q22. How do you create a custom validator?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Business validation often goes beyond built-in annotations.

**Short answer (say this first, 20-40 sec):**
Create a constraint annotation and implement `ConstraintValidator`. Use it for reusable rules like allowed email reply tones. Keep slow database or network checks in the service layer unless carefully designed.

**Detailed answer:**
```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ToneValidator.class)
public @interface ValidTone {
    String message() default "Unsupported tone";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
public class ToneValidator implements ConstraintValidator<ValidTone, String> {
    private static final Set<String> ALLOWED = Set.of("formal", "friendly", "concise");
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        return value == null || ALLOWED.contains(value.toLowerCase());
    }
}
```

**Common mistakes:**
- Failing null values when `@NotNull` should handle null.
- Calling external APIs in validators.

**Follow-ups:**
- *Can validators inject beans?* → Yes in Spring Boot, but avoid heavy logic.
- *Where place cross-field validation?* → Class-level constraint or service layer.

## MVC, Validation, Logging, AOP & API Clients

### Q23. How do SLF4J, Logback, and MDC work together?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Production debugging depends on good logs.

**Short answer (say this first, 20-40 sec):**
SLF4J is a logging facade; Logback is Boot's common logging implementation. MDC stores per-thread context such as correlation ID so every log line for one request can be traced. Always clear MDC in a `finally` block.

**Detailed answer:**
```java
@Component
class CorrelationIdFilter implements Filter {
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest r = (HttpServletRequest) req;
    String id = Optional.ofNullable(r.getHeader("X-Correlation-Id")).orElse(UUID.randomUUID().toString());
    try { MDC.put("correlationId", id); chain.doFilter(req, res); }
    finally { MDC.clear(); }
  }
}
```
- Add `%X{correlationId}` to Logback pattern.
- Never log Gemini API keys, JWTs, or passwords.

**Common mistakes:**
- Using `System.out.println`.
- Forgetting to clear MDC on pooled threads.

**Follow-ups:**
- *Does MDC cross async threads automatically?* → Usually no; context propagation is needed.
- *Why useful for Gemini?* → It links frontend request, backend logs, and provider call.

### Q24. Explain Spring AOP, proxy types, advice, and self-invocation.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Transactions, retries, security, and caching rely on proxies.

**Short answer (say this first, 20-40 sec):**
Spring AOP wraps beans in proxies. JDK proxies implement interfaces; CGLIB creates subclass proxies. Advice can run before, after, after-returning, after-throwing, or around a method. Self-invocation bypasses the proxy, so annotations may not apply.

**Detailed answer:**
```mermaid
flowchart LR
    A["Client bean"] --> B["AOP proxy"]
    B --> C["Advice"]
    C --> D["Target service"]
```
```java
@Aspect @Component
class TimingAspect {
  @Around("execution(* com.azam.email..service..*(..))")
  Object time(ProceedingJoinPoint pjp) throws Throwable { return pjp.proceed(); }
}
```
- Private/final methods are not good proxy targets.
- `@Transactional` on an internally called method can be skipped.

**Common mistakes:**
- Expecting AOP to intercept private methods.
- Forgetting to call `proceed()` in around advice.

**Follow-ups:**
- *Which proxy if interface exists?* → JDK proxy is common; CGLIB proxies classes.
- *How fix self-invocation?* → Move method to another bean or call through proxy carefully.

### Q25. Explain DispatcherServlet request flow.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** It helps debug routing, validation, and serialization.

**Short answer (say this first, 20-40 sec):**
`DispatcherServlet` is Spring MVC's front controller. It receives requests, finds a handler, binds and validates inputs, invokes the controller through an adapter, serializes the response, and delegates errors to exception resolvers.

**Detailed answer:**
```mermaid
sequenceDiagram
    participant Client
    participant Filter
    participant DispatcherServlet
    participant HandlerMapping
    participant Controller
    participant Service
    Client->>Filter: HTTP request
    Filter->>DispatcherServlet: pass request
    DispatcherServlet->>HandlerMapping: find handler
    DispatcherServlet->>Controller: bind validate invoke
    Controller->>Service: business logic
    Service-->>Controller: DTO
    Controller-->>DispatcherServlet: response
    DispatcherServlet-->>Client: JSON
```
- JSON conversion uses `HttpMessageConverter`, usually Jackson.
- Validation occurs before controller body executes.

**Common mistakes:**
- Thinking Tomcat directly calls controller methods.
- Returning JPA entities directly.

**Follow-ups:**
- *Where are exceptions handled?* → HandlerExceptionResolver and `@RestControllerAdvice`.
- *What maps URL to method?* → Handler mappings.

### Q26. Filters vs interceptors vs AOP?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Cross-cutting concerns must be placed at the right layer.

**Short answer (say this first, 20-40 sec):**
Filters are servlet-level and run before MVC. Interceptors are Spring MVC-level and run around handler execution. AOP wraps Spring bean method calls. Use filters for security/MDC/CORS, interceptors for MVC request concerns, and AOP for service-layer concerns.

**Detailed answer:**
| Tool | Layer | Good use |
|---|---|---|
| Filter | Servlet | JWT, CORS, correlation ID |
| Interceptor | MVC | request timing, locale, audit |
| AOP | Bean methods | transactions, retries, metrics |
```mermaid
flowchart LR
    A["Client"] --> B["Filter"] --> C["DispatcherServlet"] --> D["Interceptor"] --> E["Controller"] --> F["AOP service proxy"]
```

**Common mistakes:**
- Implementing authentication in controllers.
- Expecting interceptor on non-MVC calls.

**Follow-ups:**
- *Where is Spring Security?* → Mostly filters.
- *Can AOP see HTTP headers?* → Not naturally; pass context or use request APIs carefully.

### Q27. What are `@RestController` and `ResponseEntity`?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** REST controller basics are required.

**Short answer (say this first, 20-40 sec):**
`@RestController` combines `@Controller` and `@ResponseBody`, so return values are written directly as response bodies. `ResponseEntity` lets you control status, headers, and body explicitly.

**Detailed answer:**
```java
@RestController
@RequestMapping("/api/replies")
class ReplyController {
  @PostMapping
  ResponseEntity<ReplyResponse> create(@Valid @RequestBody ReplyRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.generate(req));
  }
}
```
- Return DTO directly for simple 200 responses.
- Use `ResponseEntity` for 201, 202, 204, headers, cache, and Location.

**Common mistakes:**
- Returning 200 for every outcome.
- Exposing internal entities as API responses.

**Follow-ups:**
- *What writes JSON?* → Jackson message converter.
- *When use 201?* → New resource created.

### Q28. How do you implement global exception handling with ProblemDetail in Boot 3?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** APIs need consistent errors.

**Short answer (say this first, 20-40 sec):**
Use `@RestControllerAdvice` and `@ExceptionHandler` to map exceptions centrally. Boot 3/Spring 6 provides `ProblemDetail` for structured errors with status, title, detail, type, and custom fields.

**Detailed answer:**
```java
@RestControllerAdvice
class ApiErrors {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ProblemDetail> validation(MethodArgumentNotValidException ex) {
    ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    pd.setTitle("Validation failed");
    pd.setDetail("Invalid request fields");
    pd.setProperty("errors", ex.getFieldErrors().stream().map(e -> e.getField()+": "+e.getDefaultMessage()).toList());
    return ResponseEntity.badRequest().body(pd);
  }
  @ExceptionHandler(GeminiTimeoutException.class)
  ResponseEntity<ProblemDetail> timeout(GeminiTimeoutException ex) {
    ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.GATEWAY_TIMEOUT);
    pd.setTitle("AI provider timeout");
    return ResponseEntity.status(504).body(pd);
  }
}
```

**Common mistakes:**
- Returning stack traces.
- Handling errors separately in every controller.

**Follow-ups:**
- *Status for downstream timeout?* → 504 Gateway Timeout.
- *Boot 2 equivalent?* → Use custom error DTOs.

### Q29. RestTemplate vs WebClient vs RestClient?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** External API calls are common in backend systems.

**Short answer (say this first, 20-40 sec):**
`RestTemplate` is older blocking client in maintenance mode. `WebClient` is reactive/non-blocking and supports streaming. `RestClient` is modern synchronous fluent client in Spring Framework 6.1 and is a good default for blocking MVC apps.

**Detailed answer:**
| Client | Style | Best use |
|---|---|---|
| RestTemplate | blocking legacy | existing apps |
| WebClient | reactive | high concurrency, streaming |
| RestClient | blocking modern | Boot 3 MVC APIs |
```java
@Bean RestClient geminiRestClient(RestClient.Builder b, GeminiProperties p) {
  return b.baseUrl(p.baseUrl()).defaultHeader("x-goog-api-key", p.apiKey()).build();
}
```

**Common mistakes:**
- No timeouts.
- Blocking heavily inside reactive pipelines.

**Follow-ups:**
- *Is RestTemplate removed?* → No, but not preferred for new code.
- *Can WebClient be used in MVC?* → Yes, but understand blocking and thread use.

### Q30. How do you set timeouts and retries for Gemini API calls?
`🔴 Advanced` `🔥` `Must-know SDE-1`

**Why they ask:** External dependency failure can take down your API.

**Short answer (say this first, 20-40 sec):**
Set connect/read timeouts, retry only transient errors, use exponential backoff with jitter, and combine with circuit breaker and bulkhead. Do not retry 400/401/403; consider retrying 429 and 5xx if safe and within latency budget.

**Detailed answer:**
```yaml
resilience4j:
  retry:
    instances:
      gemini:
        max-attempts: 3
        wait-duration: 300ms
  circuitbreaker:
    instances:
      gemini:
        sliding-window-size: 20
        failure-rate-threshold: 50
```
```java
@Retry(name="gemini")
@CircuitBreaker(name="gemini", fallbackMethod="fallback")
String generate(String prompt) { return client.call(prompt); }
String fallback(String prompt, Throwable ex) { return "AI service temporarily unavailable"; }
```

**Common mistakes:**
- Infinite retries.
- Timeout longer than user SLA.

**Follow-ups:**
- *Why jitter?* → Prevents retry storms.
- *What status on timeout?* → 504 or a product-defined fallback.

### Q31. What is Spring Data JPA's role here?
`🟢 Beginner` `Medium` `Must-know SDE-1`

**Why they ask:** Even if deep JPA is separate, you should know integration basics.

**Short answer (say this first, 20-40 sec):**
Spring Data JPA provides repository abstractions over JPA/Hibernate for database access. In Email Reply Assistant, it could store users, reply history, idempotency keys, and feedback. Keep entities internal and expose DTOs through controllers.

**Detailed answer:**
```java
interface ReplyRepository extends JpaRepository<ReplyEntity, UUID> {
  Page<ReplyEntity> findByUserId(String userId, Pageable pageable);
}
```
- Use repositories in service layer, not controllers.
- Use transactions for write operations.
- Avoid lazy-loading surprises in API serialization.

**Common mistakes:**
- Returning entities directly.
- Putting business logic in repositories.

**Follow-ups:**
- *Where put `@Transactional`?* → Service layer.
- *Why DTOs?* → Stable API contract and less accidental data exposure.

### Q32. What are Spring transactions at a high level?
`🟡 Intermediate` `Medium` `Must-know SDE-1`

**Why they ask:** Service methods often require atomic database changes.

**Short answer (say this first, 20-40 sec):**
`@Transactional` wraps a method in a database transaction through AOP proxy. If the method succeeds, it commits; if a runtime exception occurs, it rolls back by default. Use it at service boundaries for operations that must be atomic.

**Detailed answer:**
```java
@Service
class ReplyCommandService {
  @Transactional
  Reply createReply(ReplyRequest req) {
    Reply r = repository.save(map(req));
    idempotencyStore.save(req.key(), r.id());
    return r;
  }
}
```
- Self-invocation can bypass `@Transactional`.
- Checked exceptions do not roll back by default unless configured.

**Common mistakes:**
- Annotating private methods.
- Performing slow external API calls inside long DB transactions.

**Follow-ups:**
- *Rollback default?* → Runtime exceptions and errors.
- *Should Gemini call be inside transaction?* → Usually no; keep DB transaction short.

## Spring Security

### Q33. Explain the Spring Security filter chain.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Spring Security is filter-based.

**Short answer (say this first, 20-40 sec):**
Security filters run before Spring MVC and handle CORS, CSRF, authentication, exception handling, session management, and authorization. A request must pass the chain before reaching `DispatcherServlet` and controllers.

**Detailed answer:**
```mermaid
flowchart LR
    A["HTTP request"] --> B["CORS and CSRF filters"] --> C["Authentication filter"] --> D["Authorization filter"] --> E["DispatcherServlet"] --> F["Controller"]
```
- Multiple `SecurityFilterChain` beans can match different paths.
- Custom JWT filters should be placed at the correct point.

**Common mistakes:**
- Doing auth in controllers.
- Disabling all security to fix one CORS issue.

**Follow-ups:**
- *Where is user stored?* → `SecurityContextHolder`.
- *Can actuator have separate security?* → Yes, with a separate chain/matcher.

### Q34. Authentication vs authorization?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Core security terms must be precise.

**Short answer (say this first, 20-40 sec):**
Authentication verifies who the user is; authorization checks what they can access. JWT validation is authentication. Checking `email:write` scope or admin role is authorization.

**Detailed answer:**
```mermaid
flowchart TD
    A["Request"] --> B{"Authenticated?"}
    B -->|"no"| C["401"]
    B -->|"yes"| D{"Allowed?"}
    D -->|"no"| E["403"]
    D -->|"yes"| F["Controller"]
```
| Failure | Status |
|---|---|
| Missing/invalid credentials | 401 |
| Valid user lacks permission | 403 |

**Common mistakes:**
- Returning 403 for unauthenticated users.
- Treating token parsing as permission checking.

**Follow-ups:**
- *What is principal?* → Authenticated identity.
- *What is authority?* → Granted permission or role.

### Q35. Show Boot 3 style `SecurityFilterChain` config.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Old adapter-based examples are obsolete.

**Short answer (say this first, 20-40 sec):**
Define a `SecurityFilterChain` bean. For stateless APIs, disable CSRF if using bearer tokens, set stateless sessions, permit public endpoints, protect private endpoints, and configure JWT resource server or custom authentication.

**Detailed answer:**
```java
@Configuration
@EnableMethodSecurity
class SecurityConfig {
  @Bean SecurityFilterChain api(HttpSecurity http) throws Exception {
    return http.csrf(csrf -> csrf.disable())
      .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(a -> a
        .requestMatchers("/actuator/health", "/api/auth/**").permitAll()
        .requestMatchers("/api/admin/**").hasRole("ADMIN")
        .anyRequest().authenticated())
      .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()))
      .build();
  }
}
```

**Common mistakes:**
- Extending `WebSecurityConfigurerAdapter` in Boot 3.
- Disabling CSRF blindly for cookie session apps.

**Follow-ups:**
- *Why stateless?* → JWT APIs should not use server sessions.
- *How enable method security?* → `@EnableMethodSecurity`.

### Q36. Why is password encoding required?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Password handling is a baseline security requirement.

**Short answer (say this first, 20-40 sec):**
Passwords must be stored as salted slow hashes, never plain text or reversible encryption. Spring recommends a `PasswordEncoder`, commonly BCrypt via `PasswordEncoderFactories.createDelegatingPasswordEncoder()` for algorithm migration.

**Detailed answer:**
```java
@Bean PasswordEncoder passwordEncoder() {
  return PasswordEncoderFactories.createDelegatingPasswordEncoder();
}
boolean ok = passwordEncoder.matches(rawPassword, storedHash);
```
- Stored hash may look like `{bcrypt}$2a$10$...`.
- Slow hashing makes brute force expensive.

**Common mistakes:**
- Using MD5/SHA-256 directly.
- Logging raw passwords.

**Follow-ups:**
- *What is salt?* → Random data so same passwords get different hashes.
- *Can you decrypt BCrypt?* → No; you verify by matching.

### Q37. Explain JWT authentication flow.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** JWT is common for stateless APIs.

**Short answer (say this first, 20-40 sec):**
User logs in, server validates credentials, creates a signed access token, and the client sends it as `Authorization: Bearer`. A filter validates signature, expiry, issuer, audience, and claims, then sets authentication in the security context.

**Detailed answer:**
```mermaid
sequenceDiagram
    participant Client
    participant AuthAPI
    participant JwtService
    participant JwtFilter
    participant Controller
    Client->>AuthAPI: login credentials
    AuthAPI->>JwtService: create signed token
    JwtService-->>Client: access token
    Client->>JwtFilter: API request with Bearer token
    JwtFilter->>JwtService: validate token
    JwtFilter->>Controller: authenticated request
```
- Keep claims minimal; do not store secrets in JWT payload.
- Use short expiry and refresh-token strategy if needed.

**Common mistakes:**
- Not checking expiry/audience.
- Putting PII or secrets in token.

**Follow-ups:**
- *Can JWT be revoked?* → Hard unless short-lived, denylisted, or backed by refresh sessions.
- *Where store browser token?* → Prefer secure HttpOnly cookie or memory based on threat model.

### Q38. Show a JWT filter at a high level.
`🔴 Advanced` `🔥` `Must-know SDE-1`

**Why they ask:** It checks practical filter implementation.

**Short answer (say this first, 20-40 sec):**
A JWT filter extends `OncePerRequestFilter`, extracts bearer token, validates it, creates an `Authentication`, and stores it in `SecurityContextHolder`. Invalid tokens should not create partial authentication.

**Detailed answer:**
```java
@Component
class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtService jwtService;
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    String h = req.getHeader(HttpHeaders.AUTHORIZATION);
    if (h == null || !h.startsWith("Bearer ")) { chain.doFilter(req, res); return; }
    JwtUser u = jwtService.validate(h.substring(7));
    var auth = new UsernamePasswordAuthenticationToken(u.username(), null, u.authorities());
    SecurityContextHolder.getContext().setAuthentication(auth);
    chain.doFilter(req, res);
  }
}
```
Prefer OAuth2 resource server for standards-based JWT validation.

**Common mistakes:**
- Accepting unsigned tokens.
- Continuing with stale security context after failure.

**Follow-ups:**
- *Why OncePerRequestFilter?* → Prevents duplicate execution in one request.
- *Where register it?* → Before username/password auth filter if custom.

### Q39. How do refresh tokens work?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Access token expiry and UX require a refresh design.

**Short answer (say this first, 20-40 sec):**
Access tokens are short-lived. Refresh tokens are longer-lived credentials used only to obtain new access tokens. Store refresh tokens securely, rotate them on use, and revoke sessions on reuse detection.

**Detailed answer:**
```mermaid
flowchart TD
    A["Access token expired"] --> B["Send refresh token"]
    B --> C{"Valid and not reused?"}
    C -->|"no"| D["Revoke session and 401"]
    C -->|"yes"| E["Issue new access token"]
    E --> F["Rotate refresh token"]
```
- Store refresh token server-side hashed or as secure opaque token.
- Use Secure, HttpOnly, SameSite cookies for browser flows when appropriate.

**Common mistakes:**
- Long-lived access tokens.
- No refresh-token rotation.

**Follow-ups:**
- *JWT or opaque refresh token?* → Opaque is easier to revoke.
- *What if old refresh token is reused?* → Treat as theft and revoke session.

### Q40. Explain OAuth2 Authorization Code with PKCE.
`🔴 Advanced` `🔥` `Stretch`

**Why they ask:** Modern login flows use OAuth2/OIDC.

**Short answer (say this first, 20-40 sec):**
Authorization Code with PKCE lets public clients obtain tokens without storing a client secret. The client sends a code challenge during authorization and later proves it has the original verifier while exchanging the authorization code.

**Detailed answer:**
```mermaid
sequenceDiagram
    participant Browser
    participant ClientApp
    participant AuthServer
    participant ResourceServer
    Browser->>ClientApp: login
    ClientApp->>AuthServer: authorize with code challenge
    AuthServer-->>ClientApp: authorization code
    ClientApp->>AuthServer: code plus verifier
    AuthServer-->>ClientApp: tokens
    ClientApp->>ResourceServer: access token
```
- PKCE protects intercepted codes.
- OIDC adds identity and ID token on top of OAuth2.

**Common mistakes:**
- Using implicit flow for modern SPAs.
- Confusing OAuth authorization with authentication.

**Follow-ups:**
- *Why PKCE for public clients?* → They cannot keep secrets.
- *What validates API token?* → Resource server.

### Q41. How do resource server, CORS, CSRF, and method security fit together?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Real API security combines multiple controls.

**Short answer (say this first, 20-40 sec):**
A resource server validates bearer tokens for protected APIs. CORS controls browser cross-origin access. CSRF protects cookie-authenticated browser apps from unwanted actions. Method security applies business authorization at service/controller methods.

**Detailed answer:**
```java
@Bean CorsConfigurationSource cors() {
  CorsConfiguration c = new CorsConfiguration();
  c.setAllowedOrigins(List.of("https://email-ui.example.com"));
  c.setAllowedMethods(List.of("GET","POST","PUT","DELETE"));
  c.setAllowedHeaders(List.of("Authorization","Content-Type"));
  UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource();
  s.registerCorsConfiguration("/**", c); return s;
}
@PreAuthorize("hasAuthority('SCOPE_email:write')")
ReplyResponse generate(ReplyRequest request) { return service.generate(request); }
```
- Disable CSRF only when appropriate, e.g., stateless bearer APIs.

**Common mistakes:**
- `*` origin with credentials.
- Thinking CORS protects server-to-server calls.

**Follow-ups:**
- *Who enforces CORS?* → Browsers.
- *401 vs 403?* → Missing auth vs insufficient permission.

## Production Scenarios

### Q42. Scenario: Gemini API is slow and threads pile up. What do you do?
`🔴 Advanced` `🔥` `Must-know SDE-1`

**Why they ask:** This directly matches the Email Reply Assistant project.

**Short answer (say this first, 20-40 sec):**
Protect the app with strict timeouts, bounded pools, bulkheads, limited retries with jitter, and circuit breakers. Add metrics/tracing and consider async job processing if generation can take long. Do not simply increase Tomcat threads.

**Detailed answer:**
```mermaid
flowchart TD
    A["Request"] --> B{"Bulkhead capacity?"}
    B -->|"no"| C["503"]
    B -->|"yes"| D["Gemini call with timeout"]
    D --> E{"Done before deadline?"}
    E -->|"yes"| F["Return reply"]
    E -->|"no"| G["Fallback or 504"]
```
Check Tomcat busy threads, HTTP client latency, p95/p99, Gemini 429/5xx, and circuit state.

**Common mistakes:**
- Infinite retries during outage.
- No read timeout.

**Follow-ups:**
- *What is bulkhead?* → Limits resource use by one dependency.
- *When use async?* → If generation exceeds normal HTTP SLA.

### Q43. Scenario: app fails to start after adding a dependency.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Starters can trigger new auto-configuration.

**Short answer (say this first, 20-40 sec):**
Read the root cause, check dependency conflicts, new auto-config conditions, missing properties, duplicate beans, and Boot version compatibility. Use `--debug` for the condition evaluation report.

**Detailed answer:**
- Security starter may secure all endpoints until you add a filter chain.
- JPA starter may require a datasource.
- Boot 3 dependency may fail if it still uses `javax.*`.
- Use dependency tree and avoid overriding Boot BOM randomly.

**Common mistakes:**
- Reading only `Application run failed`.
- Removing dependency without understanding the condition that activated.

**Follow-ups:**
- *What tool shows auto-config?* → Actuator conditions or `--debug`.
- *What is root cause?* → The earliest meaningful caused-by in stack trace.

### Q44. Scenario: config not picked up in Docker, bean not found, or prototype leaks memory.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** These are common production Spring issues.

**Short answer (say this first, 20-40 sec):**
For Docker config, verify active profiles, env var names, and precedence. For missing beans, check annotations, scan package, profiles, and conditions. For prototype leaks, remember prototype is per lookup, not per request, and singleton references can retain state.

**Detailed answer:**
```yaml
environment:
  SPRING_PROFILES_ACTIVE: prod
  GEMINI_API_KEY: ${GEMINI_API_KEY}
  GEMINI_READ_TIMEOUT_MS: 3000
```
Debug checklist:
- `GEMINI_API_KEY` maps to `gemini.api-key`.
- Main class must be above packages being scanned.
- `@Profile` beans require active profile.
- Use `ObjectProvider<PrototypeBean>` for fresh prototype; never store request state in singleton fields.

**Common mistakes:**
- Using `gemini.api-key` as env var name.
- Assuming prototype beans are automatically destroyed.

**Follow-ups:**
- *How inspect beans safely?* → Secured `/actuator/beans` or tests.
- *What causes memory leak?* → Singleton collections or references retaining per-request objects.

## Revision Cheat Sheet

- IoC means Spring controls object creation and wiring; DI is the main mechanism.
- Constructor injection is preferred: explicit, immutable, testable, fail-fast.
- Boot 2.6+ disables circular references by default; fix design.
- Singleton is one per context; prototype is per lookup, not per request.
- Lifecycle: instantiate → populate → aware → BPP before → init → BPP after → ready → destroy.
- `@Component` scans your class; `@Bean` explicitly creates objects, often third-party ones.
- `@SpringBootApplication` = config + auto-config + component scan.
- Boot 3 means Java 17, Spring 6, Jakarta, `ProblemDetail`, new auto-config imports.
- Use `@ConfigurationProperties` for typed validated config.
- Secure Actuator; never expose env/beans publicly.
- DispatcherServlet handles MVC routing, binding, validation, serialization, and exception delegation.
- Filters run before MVC; interceptors around MVC handlers; AOP around bean methods.
- Use `ProblemDetail` and `@RestControllerAdvice` for clean errors.
- RestClient is modern sync, WebClient is reactive, RestTemplate is legacy.
- Security runs in filter chain; authn identifies user, authz checks permissions.
- JWTs need signature, expiry, issuer, audience, and minimal claims.
- Refresh tokens should rotate and be revocable.
- CORS is browser access control; CSRF matters mainly for cookie-authenticated state-changing requests.
- Gemini calls need timeouts, bounded retries, circuit breaker, and bulkhead.

## Flashcards

- Q → Best DI style? A → Constructor injection.
- Q → Boot 2.6 circular default? A → Fails by default.
- Q → Default bean scope? A → Singleton.
- Q → Boot 3 namespace? A → `jakarta.*`.
- Q → Boot 3 Java baseline? A → Java 17.
- Q → Auto-config file in Boot 3? A → `AutoConfiguration.imports`.
- Q → MVC front controller? A → `DispatcherServlet`.
- Q → Structured error class in Spring 6? A → `ProblemDetail`.
- Q → Modern sync HTTP client? A → `RestClient`.
- Q → Where does security run? A → Servlet filter chain.
- Q → 401 vs 403? A → Unauthenticated vs forbidden.
- Q → JWT header prefix? A → `Bearer`.
- Q → Browser cross-origin mechanism? A → CORS.
- Q → Password storage? A → Salted slow hash, e.g., BCrypt.
- Q → Main defense for slow Gemini? A → Timeout, bulkhead, circuit breaker, limited retries.
