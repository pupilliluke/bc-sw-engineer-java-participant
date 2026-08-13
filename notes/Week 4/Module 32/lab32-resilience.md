# Lab 32 — Why Resilience

## Step 1 — Scenario

Customer detail for `CUS-1001` Amina calls Account Profile. The dependency hangs 30s. List **three** user-visible or thread-pool effects.

1. The customer detail page for Amina spins for 30 seconds and then most likely
   errors anyway. Nothing tells the user it is the Account Profile call that is
   slow, the whole page is just late.
2. The request thread is blocked for those 30 seconds and cannot serve anyone
   else. Enough concurrent customer detail calls and the pool is exhausted, so
   requests that never touch Account Profile start queueing and timing out too.
3. Health checks and anything else sharing that pool start failing, so one slow
   outbound dependency makes the CRM itself look down.

## Step 2 — Pattern names

Write the four Resilience4j ideas: retry, circuit breaker, time limiter, fallback.

Retry, circuit breaker, time limiter, fallback.

Time limiter bounds the wait so the 30 seconds cannot happen. Retry gives a
transient blip a second attempt. Circuit breaker stops calling a dependency that
is already failing instead of burning a thread on every request to learn the same
thing. Fallback decides what the caller gets when the call did not happen.

## Step 3 — Not a substitute

One sentence: resilience wraps calls; it does not fix a permanently wrong URL.

Resilience wraps the call, it does not fix a permanently wrong URL or broken
configuration, it just makes the failure fast and contained instead of slow and
spreading.

## Step 4 — Notes file

Saved as `notes/lab32-resilience.md`.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab32-resilience.md`
- [ x ] Three hang effects
- [ x ] Four patterns named
- [ x ] Limitation sentence written
