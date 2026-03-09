MetricHive — Why I Did What I Did

Global Exception Handling
So basically every endpoint was turning into a mess of try-catch blocks and the actual logic was buried under error handling code. Also different endpoints were returning errors in different formats which is just bad for whoever's calling the API.
Fixed it with Spring's @ControllerAdvice — one place handles all exceptions, always returns the same JSON structure. Clean.
What I gave up: if something breaks and logging isn't set up properly it's harder to trace exactly where it came from.
What I'd do differently: define all my custom exception types on day one instead of adding them as I go. Like MetricValidationException, AuthException etc. Makes the handler way more precise.

Auth with Redis instead of hitting the DB
Needed to validate tokens on every request. Options were: hit Postgres every time, use stateless JWTs, or use Redis-backed tokens.
JWTs are fine but you can't revoke them before they expire. If someone's token gets compromised you're stuck waiting. Redis lets me invalidate instantly, and it's just a key lookup in memory so it's way faster than a DB query.
What I gave up: now I'm running Redis as infrastructure. More moving parts.
What I'd do differently: measure the actual latency that Redis hop adds (P99). Also maybe check local memory first before going to Redis — saves a network hop for the most common cases.

Flyway for DB migrations
Was manually running SQL scripts and it was already getting annoying. Schema drift happens fast.
Flyway keeps migrations version controlled alongside the code. Every environment runs the exact same scripts in the exact same order. Hibernate's auto-update is dangerous — it'll silently drop constraints or do weird things when column types change.
What I gave up: you can't just edit a table directly anymore. You have to write a migration script every time. Bit of overhead.
What I'd do differently: keep schema changes (DDL) and data changes (DML) in separate migration files from the start. Makes rollbacks much cleaner.

Redis for caching lookups
Repeated lookups for configs and session state were hitting the DB unnecessarily. Fine at low traffic, becomes a problem fast.
Used Caffeine (local) first and then switched to Redis. The reason: if I run multiple backend instances, local cache means each instance has its own version of the data. They go out of sync. Redis is one central cache that all instances share.
What I gave up: extra network hop on every cache read. Also cache invalidation is genuinely hard — stale data, cache stampedes, all that.
What I'd do differently: set strict TTLs on every cached key from day one. Learned this the hard way — without TTLs you're just waiting for an OOM error eventually.

When it comes to implementing rate limiting on redis. I got few issues. like how am i gonna implement this ?  is it like ok 1 API get 1000 calls per minute if they exceed it then its over. 
But my case was like , what if they actually want to send that much of api calls ?  maybe starbucks might sent over 100,000 api calls per minute from all around the world , around 1pm right ?
So the thing is to rate limit but with IP address as well. So for this IP address and this API only this much of calls can be made for a minute. 
And then... another issue. whats up if i got an DDOS attack  ? So for that i got multiple options and I went with Device fingerprint (canvas, WebGL, screen resolution hashed together) + API. 
But this isnt enough. Why ? Because the point of DDOS attack is never to enter to your application. Its to exhaust your server. So we need something from outside to stop this
so currently im planning to implemmet some cloudflare security feature to stop this. So for now im just gonna implement redis rate limiting with device fingerprint. 
