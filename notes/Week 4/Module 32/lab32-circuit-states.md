# Lab 32 — Circuit States

## Step 1 — Closed

Normal calls flow; failures counted.

Every call goes through to Account Profile and the breaker records the outcome.
It is counting the failure rate over a sliding window, not tripping on a single
failure, so one 503 in a healthy window changes nothing. When the failure rate
crosses the threshold the breaker moves to open.

## Step 2 — Open

Calls fail fast / use fallback; Account Profile is not hammered.

No call leaves the CRM. The breaker rejects immediately with
CallNotPermittedException and the fallback answers instead, so Amina's page
comes back in milliseconds rather than after a 30 second hang. Account Profile
gets no traffic at all, which is the point, it is already failing and a
dependency that is down does not recover faster for being called more. The
breaker stays open for waitDurationInOpenState, then moves to half-open.

## Step 3 — Half-open

Trial calls probe recovery; success → closed, failure → open.

A small fixed number of calls are let through as probes while everything else
still fails fast. If they succeed the breaker closes and normal traffic resumes.
If they fail it goes straight back to open and waits again. This is what stops
the breaker flapping the whole load back onto a dependency that is only half
recovered.

## Step 4 — Draw

Sketch a tiny state diagram (boxes + arrows) in markdown.

                  failure rate >= threshold
        +---------+ --------------------------> +---------+
        | CLOSED  |                             |  OPEN   |
        +---------+ <-------------------------- +---------+
             ^        probe calls succeeded          |
             |                                       | waitDurationInOpenState
             |                                       | elapsed
             |            +-----------+              |
             +----------- | HALF-OPEN | <------------+
                          +-----------+
                                |
                                | probe calls failed
                                +--------> back to OPEN

CLOSED calls the dependency and counts outcomes. OPEN calls nothing and answers
from the fallback. HALF-OPEN calls a limited number of probes.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab32-circuit-states.md`
- [ x ] Three states described
- [ x ] Transitions named
- [ x ] Diagram drawn
