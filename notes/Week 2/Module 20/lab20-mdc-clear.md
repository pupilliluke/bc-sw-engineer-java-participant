# Lab 20 — Clear MDC Finally Drill

## Bug story
Request A arrives and the filter runs MDC.put("correlationId", "lab-request-001"), then never clears it.
The thread goes back to the pool still holding the key.
Request B is handled on that same thread, so B's log lines come out tagged corr=lab-request-001 and B's work reads as if it belonged to A.

## Fix
The filter clears the MDC in a finally block, on every request, including the ones that throw.
finally { MDC.clear(); }

## Test idea
After a request completes, assert the MDC is empty, so MDC.get("correlationId") returns null.
Run two requests back to back on the same thread and assert the second one does not carry the first one's correlation ID.

## Scope
Pre-lab only.

If an exception skips the happy path return, which block still must clear MDC?

The finally block. It runs whether the request returns normally or throws, so it is the only place where the clear is guaranteed.
Clearing at the end of the try block only works on the happy path, and a failed request is exactly the one whose ID leaks into the next request.

Static String CORRELATION = lastSeen — why is that worse than MDC?

A static field is one value shared by the whole JVM. Every thread handling a request overwrites it, so any read gives whichever request wrote last, not the request doing the reading.
The MDC is per thread, so each request keeps its own value. The logback pattern also reads the MDC, %X only looks there, so a static field would never reach the log line anyway.

- [ X ] File exists at `notes/lab20-mdc-clear.md`
- [ X ] Bug story
- [ X ] Fix noted
- [ X ] Test idea present
