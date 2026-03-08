# MetricHive: Decision Log & Interview Prep

## Part 1: Architecture Decision Records (ADRs)

*One entry per meaningful technical choice. This shifts the focus from "what I did" to "why I did it" and shows engineering maturity.*

### 1. Global Exception Handling over Controller-Level Try-Catch
- **What problem I was hitting:** The core endpoint logic was cluttered with repetitive error-handling blocks. It made the business intent hard to read, and different exceptions were resulting in inconsistent API response formats.
- **Options I considered:**
    1. Manual `try-catch` blocks inside the controller methods.
    2. Spring `@ControllerAdvice` for global, centralized exception handling.
- **Why I chose what I chose:** `@ControllerAdvice` abstracts the error formatting away from the business logic. It guarantees a consistent API response structure (e.g., standard JSON with timestamp, error code, and message) across any future endpoints without duplicating code.
- **What I had to give up:** A bit of local control. It’s slightly harder to trace the exact origin of the error if logging isn't set up perfectly.
- **What I'd change if I did it again:** I would define a strict taxonomy of custom business exceptions (e.g., `MetricValidationException`, `AuthException`) on day one to make the global handler even more granular and precise.

### 2. Authentication Strategy
- **What problem I was hitting:** Needed to secure the endpoint without overwhelming the primary database on every single request just to validate tokens or sessions.
- **Options I considered:**
    1. Querying the primary database (PostgreSQL) on every request.
    2. Purely stateless JWTs.
    3. Session/Token-based Auth backed by Redis.
- **Why I chose what I chose:** I went with Redis-backed authentication. While JWTs are great and stateless, they cannot be instantly revoked before they expire. Redis allows for instant token invalidation and extremely fast, in-memory validation, completely protecting the primary DB from auth-related load.
- **What I had to give up:** Infrastructure simplicity. I took on the operational complexity of running and maintaining a Redis instance.
- **What I'd change if I did it again:** I would add tiered caching—doing a local memory check before the Redis network hop—or implement detailed metrics to track exactly how much latency the Redis hop is adding.

### 3. Database Schema Management with Flyway
- **What problem I was hitting:** Manually managing `.sql` scripts was causing schema drift. Testing on a clean database was annoying, and "it works on my machine" issues were bound to happen as the schema evolved.
- **Options I considered:**
    1. Hibernate `hbm2ddl.auto=update` (auto-generation).
    2. Manual SQL script execution.
    3. Flyway DB migration tool.
- **Why I chose what I chose:** Flyway enforces an ordered, reproducible schema state. Migrations are version-controlled alongside the application code. Hibernate auto-update is unpredictable and dangerous for production workloads.
- **What I had to give up:** Upfront setup overhead, and the strictness that you can't just silently edit an old table; you must write a new migration script to alter it.
- **What I'd change if I did it again:** I would clearly separate DDL (schema changes) from DML (data changes) into separate migration streams to make rollbacks cleaner.

### 4. Integrating Redis (Caching/Lookups)
- **What problem I was hitting:** Repeated lookups for configurations or session states were unnecessarily hitting the database, threatening to become a bottleneck under load.
- **Options I considered:**
    1. In-memory local cache (like Guava/Caffeine).
    2. Redis distributed cache.
- **Why I chose what I chose:** Redis allows the application to be horizontally scaled. If I spin up multiple backend instances, an in-memory cache results in fragmented, out-of-sync states. Redis provides a centralized, blazing-fast single source of truth.
- **What I had to give up:** The latency of an extra network hop (compared to local RAM) and the cognitive overhead of cache invalidation strategies (cache stampedes, stale data).
- **What I'd change if I did it again:** I would implement a strict TTL (Time to Live) policy on all cached keys from the beginning to prevent out-of-memory (OOM) errors over time.

---

## Part 2: The Interview Simulator

*Read these questions and answers OUT LOUD. This encodes the reasoning in your memory and helps highlight naturally flowing conversational answers.*

**Interviewer:** "I see you used Flyway for your database migrations on MetricHive. Given that this is a relatively small project right now with basically just one core endpoint, wasn't that over-engineering? Why not just use JPA auto-generation to move faster?"

**Candidate (You):** "It might look like over-engineering for day one, but I chose Flyway precisely to set a foundational standard. JPA auto-update is notorious for dropping constraints or failing subtly when column types change. By using Flyway, every schema change is version-controlled and 100% reproducible across environments. The trade-off is a bit of setup time, but it completely eliminated the risk of schema drift. If I had to do it again, I'd separate data migrations from schema migrations, but even for a small project, predictability is way more valuable than short-term speed."

**Interviewer:** "Interesting. Talk to me about your authentication flow. You brought in Redis. Why take on the complexity of Redis instead of just sticking a fast index on a Postgres table and querying that for tokens?"

**Candidate:** "That was a deliberate architectural choice regarding resource contention. I wanted to protect the primary DB connections. If I queried Postgres for auth checking on every request, under high load, I risk exhausting the database connection pool merely on authentication before any actual business logic even executes. Redis handles key-value lookups entirely in memory and supports an order of magnitude more operations per second. I traded infrastructure simplicity for high-throughput auth checking. Next time, I plan to measure the exact latency P99 difference between Postgres and Redis for this specific workload so I have the hard baseline numbers."

**Interviewer:** "You mentioned implementing Global Exception Handling. What was the specific pain point that led you to do that, and what was the downside?"

**Candidate:** "Before the global handler, the controller was flooded with try-catch blocks just to map different exceptions to HTTP 400s or 500s. The core business intent was buried under boilerplate. Abstracting this using a global controller advice guaranteed a standardized JSON error format for API consumers, no matter where the code failed. The downside is that sometimes you want highly specific fallback logic rather than just tossing an error up the stack, and a global handler can obscure where an error originated. To mitigate that, I made sure the global handler logs the full stack trace with a trace ID before returning the sanitized response to the client."

**Interviewer:** "Let's say MetricHive gets featured somewhere and traffic spikes. If you had to scale this single endpoint to 10,000 requests per second tomorrow, what breaks first, and how would you fix it?"

**Candidate:** "Right now, the bottleneck would likely shift back to the primary database for the actual data writes, or Tomcat's default thread pool would get exhausted. Since Auth is handled by Redis, we save some DB hits upfront, but every real metric operation still touches Postgres synchronously. At 10,000 req/s, Postgres would choke. To handle that, I'd introduce an async queue—like RabbitMQ or Kafka. The controller would just validate the payload, push it to the queue, and return a '202 Accepted'. The trade-off is giving up immediate consistency for eventual consistency, which is almost always necessary for high-throughput ingestion."
