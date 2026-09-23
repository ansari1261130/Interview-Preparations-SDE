# 14a · Docker, Kubernetes, and Cloud

This part focuses on container fundamentals, production-grade Dockerfiles, Docker Compose, JVM behavior in
containers, debugging, and the first half of Kubernetes. Examples are tied to a Spring Boot AI Email Reply
Assistant using PostgreSQL with pgvector, Redis, and a Gemini API key supplied securely at runtime.

**Legend:** 🟢 Beginner · 🟡 Intermediate · 🔴 Advanced · ⚫ FAANG · 🔥 = very frequently asked

## Table of Contents
- [Section 1 · Docker fundamentals](#section-1--docker-fundamentals)
- [Section 2 · Production Docker for Spring Boot](#section-2--production-docker-for-spring-boot)
- [Section 3 · Docker Compose and debugging](#section-3--docker-compose-and-debugging)
- [Section 4 · Kubernetes foundations](#section-4--kubernetes-foundations)
- [Revision Cheat Sheet](#revision-cheat-sheet)
- [Flashcards](#flashcards)

## Section 1 · Docker fundamentals

### Q1. What is the difference between a Docker image and a container?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** This is the base mental model for every Docker deployment question.

**Short answer (say this first, 20-40 sec):**
An image is an immutable packaged filesystem plus metadata. A container is a running instance of an image
with its own writable layer, process namespace, network namespace, environment, and resource limits.

**Detailed answer:**
Think of an image as a class and a container as an object instance, but with operating-system isolation.
An image for the AI Email Reply Assistant may contain a JRE, the Spring Boot application, and an entrypoint.
When you run it, Docker creates a container with a writable layer for logs or temporary files, connects it
to a network, injects environment variables such as `GEMINI_API_KEY`, and starts PID 1.

```powershell
docker build -t email-reply-api:dev .
docker run --rm -p 8080:8080 `
  -e GEMINI_API_KEY=replace-at-runtime `
  email-reply-api:dev
docker ps
# CONTAINER ID   IMAGE                 PORTS                    NAMES
# a12bc34d56ef   email-reply-api:dev   0.0.0.0:8080->8080/tcp   upbeat_api
```

Deleting a container does not delete the image. Rebuilding an image does not automatically update already
running containers; you must recreate them. Store application state in databases, volumes, or external
services, not in the container writable layer.

**Common mistakes:**
- Treating containers as lightweight virtual machines and SSHing into them for permanent changes.
- Baking runtime secrets into the image.
- Expecting a running container to update after `docker build`.

**Follow-ups:**
- *Is a container immutable?* → The image is immutable; the running container has a temporary writable layer.
- *What is PID 1?* → The first process in the container; its signal handling affects graceful shutdown.

### Q2. How do Docker layers and build cache work?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Good Dockerfiles build faster and produce repeatable artifacts.

**Short answer (say this first, 20-40 sec):**
Each Dockerfile instruction creates a layer or metadata step. Docker reuses cached layers when the instruction
and files it depends on have not changed, so copy dependency manifests before frequently changing source code.

**Detailed answer:**
For Maven, copying `pom.xml` before `src` lets dependency download cache survive code edits:

```dockerfile
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN ./mvnw -B -q dependency:go-offline
COPY src src
RUN ./mvnw -B package -DskipTests
```

If you instead `COPY . .` before dependency download, every source edit invalidates the dependency layer.
Layer order should go from least frequently changing to most frequently changing: base image, OS packages,
dependency descriptors, source, build command. Cache is content-based for copied files, not just timestamp.

```mermaid
flowchart TD
  base["Base image layer"] --> tools["Maven wrapper and pom layer"]
  tools --> deps["Downloaded dependencies cache"]
  deps --> source["Application source layer"]
  source --> jar["Built JAR layer"]
```

Use `.dockerignore` to keep `target/`, `.git/`, and secrets out of build context; otherwise unrelated files
can invalidate cache and leak data to the daemon. In CI, BuildKit cache exporters can persist cache across runs.

**Common mistakes:**
- Copying the whole repository before resolving dependencies.
- Letting `.git` and `target` enter the build context.
- Using `latest` base images without controlling rebuild timing.

**Follow-ups:**
- *Does `RUN apt-get update` cache forever?* → It can; combine update and install, and rebuild intentionally.
- *What invalidates a `COPY` layer?* → Changes in any file included by that `COPY` instruction.

### Q3. What should a good `.dockerignore` contain?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Build context mistakes cause slow builds and accidental secret exposure.

**Short answer (say this first, 20-40 sec):**
`.dockerignore` excludes files from the build context before Docker sends them to the daemon. Ignore build
outputs, VCS metadata, IDE files, local environment files, logs, and large datasets not needed to build.

**Detailed answer:**
Example for a Spring Boot repository:

```dockerignore
.git
.github
.idea
.vscode
target
*.log
.env
.env.*
!.env.example
node_modules
coverage
Dockerfile*
docker-compose*.yml
README.md
```

Be careful with excluding `Dockerfile` or Compose files if your build process actually copies them for
documentation; otherwise it is usually fine. The critical entries are `.env`, `.git`, and `target`. `.env`
may contain the Gemini API key; `.git` can include secret history; `target` makes context huge and may hide
whether the Dockerfile really builds from source. Verify context size:

```powershell
docker build --progress=plain -t email-reply-api:dev .
# transferring context: 45.2kB
```

`.dockerignore` syntax is similar to `.gitignore` but evaluated by Docker for build context only. It does
not stop files from being copied if they are generated inside the image during `RUN`.

**Common mistakes:**
- Assuming `.gitignore` automatically applies to Docker builds.
- Sending `.env` to the build context and then relying on not copying it.
- Ignoring `mvnw` or `.mvn` and breaking reproducible builds.

**Follow-ups:**
- *Does `.dockerignore` affect runtime mounts?* → No, it only affects build context.
- *Why ignore `target`?* → Build inside the image should be clean and cache-controlled.

### Q4. What is the difference between `ARG` and `ENV`?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Misusing build-time and runtime configuration often leaks secrets.

**Short answer (say this first, 20-40 sec):**
`ARG` is available only during image build unless copied into `ENV`. `ENV` becomes part of the image metadata
and is available at container runtime, so neither should be used for sensitive secrets.

**Detailed answer:**
Example:

```dockerfile
ARG APP_VERSION=dev
ENV SPRING_PROFILES_ACTIVE=prod
LABEL org.opencontainers.image.version=$APP_VERSION
```

Build:

```powershell
docker build --build-arg APP_VERSION=1.4.0 -t email-reply-api:1.4.0 .
docker inspect email-reply-api:1.4.0
```

Use `ARG` for non-secret build choices such as application version, optional module, or package mirror. Use
`ENV` for safe defaults such as `SERVER_PORT=8080` or `JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=75`. Do not
pass `GEMINI_API_KEY` as `ARG`; build args may appear in build history or provenance. Do not bake it as
`ENV`; it becomes visible through image inspection or derived images. Inject secrets at runtime through
orchestrator secrets, environment variables from a secure store, or mounted files.

**Common mistakes:**
- Treating `ARG` as secret because it disappears after build stages.
- Setting production credentials with `ENV` in the Dockerfile.
- Expecting `ARG` values to be available when the container starts.

**Follow-ups:**
- *Can `ENV` use `ARG`?* → Yes, but then the value persists in the final image.
- *Where put runtime config?* → In deployment manifests, secret stores, or environment-specific config.

### Q5. How should secrets be handled in Docker for the Gemini API key?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Container images are widely copied, so secret leakage becomes severe.

**Short answer (say this first, 20-40 sec):**
Never put the Gemini API key in the Dockerfile, image layers, Compose file committed to Git, or build args.
Inject it at runtime from an environment variable, Docker secret, Kubernetes Secret, Azure Key Vault, or
GitHub Actions environment secret.

**Detailed answer:**
Local development:

```powershell
docker run --rm -p 8080:8080 `
  --env-file .env.local `
  email-reply-api:dev
```

`.env.local` must be ignored and never committed. Docker Swarm secret style:

```powershell
printf "actual-key" | docker secret create gemini_api_key -
```

Compose can reference an external secret:

```yaml
services:
  app:
    image: email-reply-api:dev
    secrets:
      - gemini_api_key
secrets:
  gemini_api_key:
    external: true
```

BuildKit secret for private dependency download, not for application runtime:

```dockerfile
RUN --mount=type=secret,id=maven_settings,target=/root/.m2/settings.xml \
    ./mvnw -B package -DskipTests
```

```powershell
docker build --secret id=maven_settings,src=.m2\settings.xml .
```

If a key was built into an image, rotate it. Deleting the image locally is not enough because registry layers
and CI logs may still contain it.

**Common mistakes:**
- Using `ARG GEMINI_API_KEY` during build.
- Committing `.env` beside `docker-compose.yml`.
- Logging the full environment in startup diagnostics.

**Follow-ups:**
- *Is base64 encryption?* → No, base64 is encoding; use a real secret store and access controls.
- *Can Spring read env vars?* → Yes, map `${GEMINI_API_KEY}` in configuration or read through properties.

### Q6. What is the difference between `CMD` and `ENTRYPOINT`?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Startup command design affects overridability, signal handling, and production operations.

**Short answer (say this first, 20-40 sec):**
`ENTRYPOINT` defines the executable that normally always runs. `CMD` provides default arguments that can be
overridden at `docker run` time. Use exec form to ensure signals reach the Java process.

**Detailed answer:**
Recommended pattern:

```dockerfile
ENTRYPOINT ["java"]
CMD ["-jar", "/app/app.jar"]
```

Or for fixed app startup:

```dockerfile
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

Exec form is important. Shell form like `ENTRYPOINT java -jar app.jar` starts a shell as PID 1, which can
handle signals poorly. Kubernetes sends SIGTERM before killing pods; Java must receive it so Spring Boot can
stop accepting traffic and close resources. Override examples:

```powershell
docker run email-reply-api:dev --spring.profiles.active=local
docker run --entrypoint sh email-reply-api:dev
```

Use `ENTRYPOINT` for the stable executable and `CMD` for defaults. For debug shells, overriding entrypoint
is acceptable locally, but production images should not rely on manual shell fixes.

**Common mistakes:**
- Using shell form and then wondering why graceful shutdown fails.
- Putting environment-specific config into the startup command instead of deployment config.
- Assuming `docker run image echo hi` overrides entrypoint; it overrides CMD arguments.

**Follow-ups:**
- *Can both be used together?* → Yes, CMD becomes default arguments to ENTRYPOINT.
- *Why PID 1 matters?* → It receives signals and handles orphaned child processes.

## Section 2 · Production Docker for Spring Boot

### Q7. Write a production multi-stage Dockerfile for Spring Boot.
`🔴 Advanced` `🔥` `Must-know SDE-2`

**Why they ask:** A production Dockerfile reveals build cache, runtime hardening, health checks, and Java knowledge.

**Short answer (say this first, 20-40 sec):**
Use a JDK build stage and a smaller JRE runtime stage. Resolve dependencies before copying source, extract
Spring Boot layers with layertools, run as a non-root user, expose the service port, and add a health check.

**Detailed answer:**

```dockerfile
# syntax=docker/dockerfile:1.7
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN ./mvnw -B -q dependency:go-offline
COPY src src
RUN ./mvnw -B package -DskipTests
RUN java -Djarmode=layertools -jar target/*.jar extract --destination layers

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
RUN groupadd --system app && useradd --system --gid app app
COPY --from=build /workspace/layers/dependencies/ ./
COPY --from=build /workspace/layers/spring-boot-loader/ ./
COPY --from=build /workspace/layers/snapshot-dependencies/ ./
COPY --from=build /workspace/layers/application/ ./
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
EXPOSE 8080
USER app
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget -qO- http://127.0.0.1:8080/actuator/health/readiness || exit 1
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
```

Spring Boot 3 uses the `org.springframework.boot.loader.launch.JarLauncher` package. Health check requires
Actuator and the readiness endpoint exposed. Some minimal JRE images may not include `wget`; then either
install a tiny client, use a distroless-compatible probe at orchestration level, or rely on Kubernetes probes.

**Common mistakes:**
- Running the app as root.
- Copying a locally built `target/*.jar` instead of building inside the image.
- Using a health check endpoint that requires authentication.

**Follow-ups:**
- *Why layertools?* → It separates stable dependency layers from frequently changing application classes.
- *Why skip tests in Docker build?* → CI should run tests before image build; avoid duplicating expensive checks.

### Q8. How do Spring Boot layered JARs improve container rebuilds?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Java images can be large; layering improves CI speed and registry efficiency.

**Short answer (say this first, 20-40 sec):**
Spring Boot can split an executable JAR into dependency, loader, snapshot-dependency, and application layers.
Docker then reuses unchanged dependency layers when only application code changes.

**Detailed answer:**
Spring Boot's layered JAR metadata is produced by the Maven or Gradle plugin. Extract it:

```powershell
java -Djarmode=layertools -jar target/email-reply-api.jar list
# dependencies
# spring-boot-loader
# snapshot-dependencies
# application
java -Djarmode=layertools -jar target/email-reply-api.jar extract
```

Dockerfile ordering copies stable layers first:

```dockerfile
COPY --from=build /workspace/layers/dependencies/ ./
COPY --from=build /workspace/layers/spring-boot-loader/ ./
COPY --from=build /workspace/layers/snapshot-dependencies/ ./
COPY --from=build /workspace/layers/application/ ./
```

If you change `EmailReplyController`, only the application layer changes. If you change `pom.xml`, dependency
layers change. This improves local rebuilds, CI cache hits, and registry pushes because unchanged layers are
already present. It also makes image scanning clearer because third-party libraries are separated from app
classes, though scanners usually inspect the final filesystem anyway.

**Common mistakes:**
- Copying the fat JAR as one layer and invalidating everything per code change.
- Forgetting Spring Boot version differences in the launcher class.
- Expecting layering to reduce runtime memory; it mainly affects image distribution and build cache.

**Follow-ups:**
- *Does layering replace multi-stage builds?* → No, use both: build in one stage and run extracted layers in another.
- *What changes dependency layer?* → Dependency version changes, plugin output changes, or `pom.xml` changes.

### Q9. How do you make Java container memory settings safe?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** JVM memory defaults can cause OOMKilled containers if not tuned.

**Short answer (say this first, 20-40 sec):**
Modern Java is container-aware, but you should still set memory limits and configure heap as a percentage
with `-XX:MaxRAMPercentage`. Leave space for metaspace, thread stacks, direct buffers, and native memory.

**Detailed answer:**
Java 10+ detects cgroup limits; Java 8 support depends on update level and flags. For Java 21:

```dockerfile
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:InitialRAMPercentage=25 \
  -XX:+ExitOnOutOfMemoryError"
```

Kubernetes resources:

```yaml
resources:
  requests:
    cpu: "250m"
    memory: "512Mi"
  limits:
    cpu: "1"
    memory: "1Gi"
```

If limit is `1Gi` and max heap is 75%, heap can grow around 768Mi, leaving roughly 256Mi for non-heap. For
high-thread apps, Netty, large TLS buffers, or heavy class loading, reduce percentage. Exit on OOM lets the
orchestrator restart the container instead of limping. Investigate memory with heap dumps only if storage
and privacy are handled; dumps may contain user data or prompts.

**Common mistakes:**
- Setting `-Xmx` equal to the container memory limit.
- Omitting memory limits and letting pods consume node memory unpredictably.
- Ignoring non-heap memory when diagnosing exit code 137.

**Follow-ups:**
- *What is exit code 137?* → Usually SIGKILL, commonly from cgroup OOM kill.
- *What is exit code 143?* → SIGTERM, often normal orchestrator shutdown.

### Q10. How can image size be reduced, and what are Alpine or distroless caveats?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Smaller images improve transfer time and attack surface, but unsafe minimalism hurts debugging.

**Short answer (say this first, 20-40 sec):**
Use multi-stage builds, smaller runtime bases, `.dockerignore`, layered JAR extraction, and remove unnecessary
tools. Alpine and distroless can be useful, but Alpine uses musl libc and distroless lacks a shell.

**Detailed answer:**
Practical reductions:

```dockerfile
FROM eclipse-temurin:21-jdk AS build
# build JAR
FROM eclipse-temurin:21-jre AS runtime
# copy only runtime artifacts
```

Other options: use `jlink` to build a custom runtime, choose a slim JRE image, avoid package managers in the
final stage, and do not copy source, Maven cache, tests, or `target` directory. Distroless images reduce shell
and package attack surface:

```dockerfile
FROM gcr.io/distroless/java21-debian12
COPY --from=build /workspace/app.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

Caveats: no shell for `docker exec sh`, harder ad-hoc debugging, and health checks needing shell tools will
fail. Alpine can be smaller but musl differences may affect native libraries, DNS behavior, fonts, or performance.
For a Java service, Temurin JRE slim is often a balanced default; move to distroless when observability is mature.

**Common mistakes:**
- Optimizing image size before fixing build context and dependency cache.
- Installing debug tools in production final images and forgetting to remove them.
- Assuming Alpine is always safer or faster.

**Follow-ups:**
- *Does smaller image mean less memory?* → Not necessarily; image size and runtime memory are different.
- *How debug distroless?* → Use logs, ephemeral debug containers in Kubernetes, or a separate debug image.

## Section 3 · Docker Compose and debugging

### Q11. Write a Docker Compose file for Spring Boot, PostgreSQL with pgvector, and Redis.
`🔴 Advanced` `🔥` `Must-know SDE-2`

**Why they ask:** Compose shows whether you can run a realistic local dependency stack.

**Short answer (say this first, 20-40 sec):**
Define app, PostgreSQL pgvector, and Redis services on one network. Add health checks, persistent volumes,
runtime environment variables, and `depends_on` with health conditions so the app starts after dependencies.

**Detailed answer:**

```yaml
services:
  app:
    build: .
    image: email-reply-api:local
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/emaildb
      SPRING_DATASOURCE_USERNAME: email
      SPRING_DATASOURCE_PASSWORD: email_pw
      SPRING_DATA_REDIS_HOST: redis
      GEMINI_API_KEY: ${GEMINI_API_KEY}
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy

  postgres:
    image: pgvector/pgvector:pg16
    environment:
      POSTGRES_DB: emaildb
      POSTGRES_USER: email
      POSTGRES_PASSWORD: email_pw
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U email -d emaildb"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    command: ["redis-server", "--appendonly", "yes"]
    volumes:
      - redisdata:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 3s
      retries: 5

volumes:
  pgdata:
  redisdata:
```

Use `.env` locally but never commit real `GEMINI_API_KEY`. `depends_on` health condition helps startup order,
but the application should still retry database connections because dependencies can restart later.

**Common mistakes:**
- Using `localhost` from the app container to reach Postgres; service name `postgres` is the hostname.
- Committing real passwords in Compose files intended for Git.
- Believing `depends_on` guarantees the database remains available forever.

**Follow-ups:**
- *Where enable pgvector extension?* → Run `CREATE EXTENSION vector;` through migration or init script.
- *Does Compose equal Kubernetes?* → No, Compose is local orchestration; Kubernetes has controllers and desired state.

### Q12. What are Docker volumes and bind mounts?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** State management is a key container concept.

**Short answer (say this first, 20-40 sec):**
Volumes are Docker-managed persistent storage. Bind mounts map a host path into a container. Use volumes for
database data and bind mounts for local development source or config.

**Detailed answer:**
Volume:

```powershell
docker volume create pgdata
docker run -v pgdata:/var/lib/postgresql/data postgres:16
```

Bind mount:

```powershell
docker run --rm -v ${PWD}\config:/app/config email-reply-api:dev
```

Volumes are portable across host path layout and managed by Docker; inspect them with `docker volume inspect`.
Bind mounts expose exact host files, so they are convenient for live development but can create permission
and OS path issues. In Compose, named volumes preserve PostgreSQL data across container recreation:

```yaml
volumes:
  pgdata:
services:
  postgres:
    volumes:
      - pgdata:/var/lib/postgresql/data
```

Do not store critical data only in a disposable container layer. For production Kubernetes, use Persistent
Volumes and managed databases rather than local Docker volumes.

**Common mistakes:**
- Deleting a container and expecting database data to remain without a volume.
- Bind-mounting over application directories and hiding files baked into the image.
- Using host-specific absolute paths in team Compose files.

**Follow-ups:**
- *List volumes?* → `docker volume ls`.
- *Remove unused volumes?* → `docker volume prune`, but check data carefully first.

### Q13. How do Docker networks work in Compose?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Many local container bugs are actually hostname and network misunderstandings.

**Short answer (say this first, 20-40 sec):**
Compose creates a default bridge network where services can reach each other by service name. Host port
publishing is only needed for access from your machine, not for service-to-service traffic.

**Detailed answer:**
In Compose, the app reaches PostgreSQL at `postgres:5432`, not `localhost:5432`. Inside the app container,
`localhost` means the app container itself. Example Spring datasource:

```yaml
environment:
  SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/emaildb
```

Port mapping:

```yaml
ports:
  - "8080:8080"
```

This exposes app port 8080 to the host. PostgreSQL does not need a host port if only the app container uses
it; you may add `"5432:5432"` for local tools like DBeaver, but avoid collisions. Inspect:

```powershell
docker network ls
docker network inspect chief_default
docker compose exec app getent hosts postgres
```

Custom networks can separate frontend, backend, and database traffic, but for most local development a
single default network is enough.

**Common mistakes:**
- Configuring container-to-container URLs with `localhost`.
- Publishing every database/cache port unnecessarily.
- Assuming Compose service names exist outside the Docker network.

**Follow-ups:**
- *Can two Compose projects share a network?* → Yes, create an external network and attach both.
- *What is bridge network?* → A virtual network on one Docker host using NAT and service discovery.

### Q14. How do you debug a failing Docker container?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Production support starts with reading container state and logs quickly.

**Short answer (say this first, 20-40 sec):**
Check container status, logs, exit code, environment, ports, health checks, and recent events. Use `exec`
only if the container is running; use `inspect` for stopped containers.

**Detailed answer:**
Command sequence:

```powershell
docker ps -a
docker logs --tail 200 email-reply-api
docker inspect email-reply-api --format '{{.State.ExitCode}}'
docker inspect email-reply-api --format '{{json .State.Health}}'
docker inspect email-reply-api --format '{{json .Config.Env}}'
docker port email-reply-api
```

If running:

```powershell
docker exec -it email-reply-api sh
```

If no shell exists, use logs or rebuild a debug image. Exit codes: `137` usually means SIGKILL, often memory
limit; `143` means SIGTERM, often normal stop or orchestrator termination; `1` is generic application error.
For Spring Boot, look for failed datasource URL, missing `GEMINI_API_KEY`, port binding errors, or actuator
health failures. Compose adds:

```powershell
docker compose ps
docker compose logs -f app
docker compose config
```

`docker compose config` renders interpolated environment values, but avoid printing secrets in shared logs.

**Common mistakes:**
- Trying to `exec` into a container that already exited.
- Debugging app code before checking missing environment variables.
- Ignoring health check failures because the process is still running.

**Follow-ups:**
- *What does 137 mean?* → SIGKILL; investigate OOM and memory limits.
- *How see restart policy?* → `docker inspect` under `HostConfig.RestartPolicy`.

### Q15. How do you troubleshoot Docker image pull and registry issues?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** CI/CD and Kubernetes frequently fail before the application even starts.

**Short answer (say this first, 20-40 sec):**
Verify image name, tag, registry authentication, platform architecture, and network access. Pull manually
from the same environment and prefer immutable tags or digests.

**Detailed answer:**
Commands:

```powershell
docker pull ghcr.io/org/email-reply-api:sha-6f3a9c2
docker image inspect ghcr.io/org/email-reply-api:sha-6f3a9c2
docker login ghcr.io
docker manifest inspect ghcr.io/org/email-reply-api:sha-6f3a9c2
```

Common problems: tag was never pushed, registry is private, token lacks `read:packages`, wrong repository
case, image built only for `linux/amd64` while running on `arm64`, or corporate network blocks registry
access. In GitHub Actions, ensure `packages: write` for push and appropriate package visibility for pull.
In Kubernetes, use `imagePullSecrets` or managed identity integration depending on registry. Avoid deploying
`latest`; if the pull succeeds on one node and fails on another, check node architecture and registry rate limits.

**Common mistakes:**
- Confusing Docker Hub names with GHCR names.
- Pushing `email-reply-api:dev` locally and expecting the cluster to see it.
- Forgetting private package permissions after moving a repository to an organization.

**Follow-ups:**
- *How prove exact image?* → Use digest, e.g. `image@sha256:...`.
- *What is multi-arch build?* → A manifest list pointing to images for different CPU architectures.

## Section 4 · Kubernetes foundations

### Q16. Explain Kubernetes architecture.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** You need to know who stores desired state, schedules pods, and runs containers.

**Short answer (say this first, 20-40 sec):**
The control plane exposes the API server, stores state in etcd, schedules pods, and runs controllers. Worker
nodes run kubelet, kube-proxy, and a container runtime to keep pods matching the desired state.

**Detailed answer:**

```mermaid
flowchart TD
  user["kubectl or controller"] --> api["API server"]
  api --> etcd["etcd state store"]
  api --> scheduler["Scheduler"]
  api --> controller["Controller manager"]
  scheduler --> api
  controller --> api
  api --> kubelet["Kubelet on worker node"]
  kubelet --> runtime["Container runtime"]
  kubelet --> pod["Pods"]
  proxy["kube-proxy"] --> service["Service networking"]
```

The API server is the front door; every `kubectl apply` writes desired state through it. etcd is the strongly
consistent key-value store for cluster state. The scheduler assigns unscheduled pods to nodes based on
resources, constraints, taints, and affinity. Controllers reconcile desired and actual state: Deployment
controller creates ReplicaSets, ReplicaSet controller creates Pods, node controller notices node health.
Kubelet runs on each node and starts containers through containerd or another runtime. kube-proxy programs
network rules for Services.

**Common mistakes:**
- Thinking `kubectl` directly starts containers on nodes.
- Calling etcd a relational database.
- Ignoring controllers and describing Kubernetes as only a scheduler.

**Follow-ups:**
- *What happens if scheduler dies?* → Existing pods keep running; new unscheduled pods wait.
- *What is reconciliation?* → Controllers continuously move actual state toward desired state.

### Q17. What is a Pod?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Pod is the smallest deployable unit in Kubernetes.

**Short answer (say this first, 20-40 sec):**
A Pod is one or more containers that share network namespace, storage volumes, and lifecycle. Usually one
application container runs per pod, with optional sidecars for supporting behavior.

**Detailed answer:**
Minimal pod:

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: email-reply-api
spec:
  containers:
    - name: app
      image: ghcr.io/org/email-reply-api:sha-6f3a9c2
      ports:
        - containerPort: 8080
```

Containers in the same pod share `localhost`; a sidecar proxy can call the app at `localhost:8080`. Pods are
ephemeral; if a node dies, the pod is replaced by a controller, not resurrected with the same identity unless
using StatefulSet semantics. You normally do not create naked pods in production; use Deployments, Jobs, or
StatefulSets so controllers handle replacement. Pod IPs are not stable enough for clients, so expose pods
through Services.

**Common mistakes:**
- Deploying a standalone Pod for production and wondering why it is not recreated.
- Putting unrelated services into one pod because they communicate.
- Depending on pod IPs directly from clients.

**Follow-ups:**
- *When use multiple containers in one pod?* → Sidecars such as log shippers, service mesh proxies, or adapters.
- *Do pod containers share filesystem?* → Only through explicitly mounted volumes, not by default.

### Q18. What are ReplicaSet and Deployment?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** This is the standard way stateless services run in Kubernetes.

**Short answer (say this first, 20-40 sec):**
A ReplicaSet keeps a specified number of matching pods running. A Deployment manages ReplicaSets and provides
declarative rolling updates, rollback history, and scaling for stateless applications.

**Detailed answer:**
Deployment example:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: email-reply-api
spec:
  replicas: 3
  selector:
    matchLabels:
      app: email-reply-api
  template:
    metadata:
      labels:
        app: email-reply-api
    spec:
      containers:
        - name: app
          image: ghcr.io/org/email-reply-api:sha-6f3a9c2
```

When you change the image, the Deployment creates a new ReplicaSet and gradually scales it up while scaling
the old one down. The selector must match the pod template labels; if not, the Deployment may not manage
its pods correctly. Use Deployments for stateless Spring Boot APIs; keep PostgreSQL in a managed database
or StatefulSet if self-hosted. Rollback:

```powershell
kubectl rollout history deployment/email-reply-api
kubectl rollout undo deployment/email-reply-api
```

**Common mistakes:**
- Editing ReplicaSets directly instead of the Deployment.
- Mismatching selector and template labels.
- Running stateful databases as Deployments without stable identity or storage planning.

**Follow-ups:**
- *Can Deployment scale to zero?* → Yes, set replicas to 0.
- *Who creates pods?* → Deployment creates ReplicaSets; ReplicaSets create Pods.

### Q19. What are Kubernetes Service types?
`🟢 Beginner` `🔥` `Must-know SDE-1`

**Why they ask:** Services decouple clients from changing pod IPs.

**Short answer (say this first, 20-40 sec):**
A Service gives stable virtual networking over selected pods. Types include ClusterIP for internal access,
NodePort for node-level exposure, LoadBalancer for cloud load balancers, and ExternalName for DNS aliasing.

**Detailed answer:**
ClusterIP example:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: email-reply-api
spec:
  type: ClusterIP
  selector:
    app: email-reply-api
  ports:
    - port: 80
      targetPort: 8080
```

Service types:

| Type | Scope | Use case |
|---|---|---|
| ClusterIP | Inside cluster | Service-to-service calls |
| NodePort | Each node IP and static port | Simple demos or special networking |
| LoadBalancer | Cloud external LB | Public TCP/HTTP entry without Ingress |
| ExternalName | DNS CNAME | Alias external service |

The selector chooses pods by labels. If selector labels do not match, endpoints are empty and traffic fails.
For HTTP routing across many services, use Ingress or Gateway API rather than one LoadBalancer per service.

**Common mistakes:**
- Setting `port` and `targetPort` backwards.
- Debugging app code when Service has no endpoints.
- Using NodePort as default production exposure in cloud environments.

**Follow-ups:**
- *Check endpoints?* → `kubectl get endpoints email-reply-api`.
- *Can Services load balance?* → Yes, kube-proxy or dataplane routes to ready endpoints.

### Q20. What is Ingress?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Most HTTP services enter Kubernetes through Ingress or a gateway.

**Short answer (say this first, 20-40 sec):**
Ingress defines HTTP and HTTPS routing rules from outside the cluster to Services. It requires an Ingress
controller such as NGINX, Azure Application Gateway Ingress Controller, or a cloud load balancer controller.

**Detailed answer:**
Example:

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: email-reply-api
spec:
  ingressClassName: nginx
  rules:
    - host: email-reply.example.com
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: email-reply-api
                port:
                  number: 80
```

Ingress is only a resource definition; the controller watches it and configures actual proxy/load balancer
infrastructure. TLS is usually configured with a Secret containing a certificate or via cert-manager. For
Azure AKS, common choices include NGINX Ingress Controller or Application Gateway Ingress Controller. For
newer designs, Gateway API provides more expressive routing, but Ingress remains widely asked and used.

**Common mistakes:**
- Creating Ingress without installing an Ingress controller.
- Sending traffic to Service port 8080 when the Service exposes port 80.
- Forgetting DNS must point to the external load balancer.

**Follow-ups:**
- *Ingress vs Service LoadBalancer?* → Ingress routes HTTP by host/path; LoadBalancer exposes one service at L4.
- *Where store TLS cert?* → Kubernetes Secret, often managed by cert-manager.

### Q21. ConfigMap vs Secret: what is the difference?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Configuration and secret handling are frequent production mistakes.

**Short answer (say this first, 20-40 sec):**
ConfigMaps store non-sensitive configuration, while Secrets store sensitive values with different handling
and access controls. Kubernetes Secret data is base64-encoded by default, not automatically encrypted in use.

**Detailed answer:**
ConfigMap:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: email-reply-config
data:
  SPRING_PROFILES_ACTIVE: prod
  REDIS_HOST: redis
```

Secret:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: email-reply-secrets
type: Opaque
stringData:
  GEMINI_API_KEY: replace-through-secret-manager
```

Use them in a Deployment:

```yaml
envFrom:
  - configMapRef:
      name: email-reply-config
  - secretRef:
      name: email-reply-secrets
```

`stringData` is convenient for manifests before the API server stores base64 data. In production, use Azure
Key Vault with Secrets Store CSI Driver or External Secrets instead of committing Secret YAML. Enable etcd
encryption at rest and restrict RBAC because anyone who can read Secrets can usually decode them.

**Common mistakes:**
- Saying base64 is encryption.
- Committing real Secret manifests to Git.
- Giving broad `get secrets` RBAC to application teams or service accounts.

**Follow-ups:**
- *Do env var secret changes restart pods?* → Not automatically; rollout restart or use mounted volumes with app reload support.
- *Can ConfigMap hold JSON?* → Yes, as string data or file-like keys.

### Q22. Explain liveness, readiness, and startup probes.
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Probe mistakes cause traffic to broken pods or restart healthy slow-starting apps.

**Short answer (say this first, 20-40 sec):**
Readiness decides whether a pod receives traffic, liveness decides whether it should be restarted, and startup
protects slow-starting containers before liveness begins. Use Spring Boot Actuator health groups.

**Detailed answer:**
Spring Boot properties:

```properties
management.endpoint.health.probes.enabled=true
management.endpoints.web.exposure.include=health,info,prometheus
```

Deployment snippet:

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  periodSeconds: 10
  failureThreshold: 3
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  periodSeconds: 5
  failureThreshold: 2
startupProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  periodSeconds: 5
  failureThreshold: 24
```

Do not put every dependency in liveness; if PostgreSQL is temporarily down, restarting all app pods can make
the incident worse. Readiness can include database connectivity if the app cannot serve without it. Startup
is useful for JVM warmup, migrations, or first-time cache initialization. Tune thresholds based on measured
startup time, not guesses.

**Common mistakes:**
- Using the same deep dependency check for liveness and readiness.
- Setting initial delays too short and causing CrashLoopBackOff.
- Protecting slow startup with huge liveness delay instead of startup probe.

**Follow-ups:**
- *What happens when readiness fails?* → Pod stays running but is removed from Service endpoints.
- *What happens when liveness fails?* → Kubelet restarts the container.

### Q23. What are Kubernetes requests, limits, and QoS classes?
`🟡 Intermediate` `🔥` `Must-know SDE-2`

**Why they ask:** Resource settings determine scheduling, cost, performance, and eviction behavior.

**Short answer (say this first, 20-40 sec):**
Requests are guaranteed scheduling amounts; limits are maximum allowed usage. QoS classes are Guaranteed,
Burstable, and BestEffort, influencing eviction priority under node pressure.

**Detailed answer:**
Example:

```yaml
resources:
  requests:
    cpu: "250m"
    memory: "512Mi"
  limits:
    cpu: "1"
    memory: "1Gi"
```

The scheduler uses requests to place pods. CPU limit throttles; memory limit can kill the container with
OOMKilled. QoS:

| QoS | Requirement | Eviction priority |
|---|---|---|
| Guaranteed | Every container has equal CPU and memory request=limit | Last |
| Burstable | At least one request or limit, not all equal | Middle |
| BestEffort | No requests or limits | First |

For Spring Boot, set memory request based on normal observed usage and limit with JVM percentage settings.
Avoid very low CPU limits because Java startup and GC can be throttled. Monitor actual usage and adjust;
requests that are too high waste cluster capacity, while requests too low cause noisy neighbors.

**Common mistakes:**
- Setting memory limit without tuning JVM heap.
- Leaving requests empty and causing unpredictable scheduling.
- Treating CPU limit as guaranteed CPU; request is the scheduling guarantee.

**Follow-ups:**
- *What is `250m` CPU?* → One quarter of a CPU core.
- *What causes OOMKilled?* → Container exceeded memory cgroup limit or node memory pressure killed it.

### Q24. What is Horizontal Pod Autoscaler and how do you configure it?
`🔴 Advanced` `🔥` `Must-know SDE-2`

**Why they ask:** Autoscaling is a standard cloud-native production topic.

**Short answer (say this first, 20-40 sec):**
HPA adjusts pod replica count based on metrics such as CPU, memory, or custom metrics. It requires resource
requests and a metrics pipeline, commonly metrics-server for CPU and memory.

**Detailed answer:**

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: email-reply-api
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: email-reply-api
  minReplicas: 2
  maxReplicas: 10
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
```

Check:

```powershell
kubectl get hpa
kubectl describe hpa email-reply-api
kubectl top pods
```

HPA calculates desired replicas from current usage divided by requested resources. If requests are missing,
CPU utilization targets cannot work correctly. For APIs dominated by external Gemini latency, CPU may not
reflect load; request rate, queue depth, or p95 latency custom metrics may be better. HPA scales pods, not
nodes; cluster autoscaler may be needed to add nodes when pods cannot schedule.

**Common mistakes:**
- Forgetting to install metrics-server.
- Setting max replicas too low for incidents or too high for database capacity.
- Autoscaling on CPU for an I/O-bound service with slow external calls.

**Follow-ups:**
- *HPA vs VPA?* → HPA changes replicas; VPA recommends or changes pod resource requests.
- *Does HPA scale to zero?* → Native HPA generally does not; KEDA can scale event-driven workloads to zero.

### Q25. How do rolling updates and rollback work in Kubernetes?
`🟡 Intermediate` `🔥` `Must-know SDE-1`

**Why they ask:** Deployments are only safe if you understand surge, unavailable pods, and rollback commands.

**Short answer (say this first, 20-40 sec):**
A Deployment rolling update creates a new ReplicaSet and gradually replaces old pods. `maxSurge` controls
extra pods above desired replicas, `maxUnavailable` controls how many can be down, and rollback restores a
previous ReplicaSet revision.

**Detailed answer:**
Deployment strategy:

```yaml
strategy:
  type: RollingUpdate
  rollingUpdate:
    maxSurge: 1
    maxUnavailable: 0
```

Commands:

```powershell
kubectl set image deployment/email-reply-api `
  app=ghcr.io/org/email-reply-api:sha-9abc123
kubectl rollout status deployment/email-reply-api
kubectl rollout history deployment/email-reply-api
kubectl rollout undo deployment/email-reply-api
kubectl rollout undo deployment/email-reply-api --to-revision=3
```

With replicas `3`, `maxSurge: 1` allows up to 4 pods during rollout, and `maxUnavailable: 0` keeps at least
3 available if readiness probes work. If readiness is wrong, Kubernetes may send traffic too early. Rollback
changes the pod template back; it does not undo database migrations or external side effects. Use immutable
image tags or digests so rollout history points to real artifacts.

**Common mistakes:**
- Using `latest`, making rollback ambiguous.
- Setting `maxUnavailable: 1` on a single-replica critical service.
- Assuming rollback reverses database changes.

**Follow-ups:**
- *Pause rollout?* → `kubectl rollout pause deployment/name`.
- *Resume rollout?* → `kubectl rollout resume deployment/name`.

## Revision Cheat Sheet
- Image is a packaged template; container is a running instance with a writable layer.
- Optimize Docker cache by copying `pom.xml` before `src`; keep build context small with `.dockerignore`.
- Never bake Gemini or cloud secrets into images; inject at runtime from secret stores.
- Use multi-stage Spring Boot Dockerfiles, JRE runtime, non-root user, layertools, and health checks.
- Java containers need memory limits plus `MaxRAMPercentage`; leave non-heap headroom.
- Compose service names are DNS names; use `postgres`, not `localhost`, from app containers.
- Kubernetes control plane stores desired state through API server and etcd; controllers reconcile it.
- Use Deployments for stateless Spring Boot APIs, Services for stable networking, and Ingress for HTTP routing.
- ConfigMap is non-secret config; Secret is sensitive but base64 is not encryption.
- Readiness gates traffic, liveness restarts, startup protects slow boot.
- Requests drive scheduling; limits cap usage; HPA depends on metrics and requests.

## Flashcards
- Q: Docker image vs container? → Image is immutable package; container is running instance.
- Q: Best cache order for Maven Dockerfile? → Copy `pom.xml` and wrapper before `src`.
- Q: Where put Gemini API key? → Runtime secret, not Dockerfile or image layer.
- Q: `CMD` vs `ENTRYPOINT`? → ENTRYPOINT executable; CMD default args.
- Q: Compose hostname for Postgres service? → `postgres`.
- Q: Exit code 137? → Usually SIGKILL from OOM.
- Q: Kubernetes smallest deployable unit? → Pod.
- Q: Deployment manages what? → ReplicaSets and rolling updates.
- Q: ClusterIP use? → Internal stable service access.
- Q: Base64 Secret data safe? → No, it is encoding, not encryption.
- Q: Readiness failure effect? → Pod removed from Service endpoints.
- Q: HPA scales what? → Replica count of a target such as Deployment.
