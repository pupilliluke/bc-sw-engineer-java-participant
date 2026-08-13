# Lab 32 — Fallback Contract

## Step 1 — Fields kept

List fields still shown: customerId, displayName maybe, status UNKNOWN.

customerId, because the CRM already knows it, it came in on the request and did
not come from Account Profile. displayName, for the same reason, it is Amina
Khan from the CRM's own store. status as UNKNOWN rather than a guessed value.
And a degraded flag saying this response is partial.

The rule is that a fallback may only return what the CRM already had. Anything
that would have come from Account Profile is not known and must not be invented.

## Step 2 — Fields dropped

List fields omitted: balance, tier, lastLogin.

balance, tier and lastLogin. All three come from Account Profile and there is no
local copy, so they are omitted or explicitly null, never zero and never a
default. A balance of 0.00 on Amina's page is worse than no balance, it looks
like an answer and someone will act on it.

## Step 3 — API signal

Decide: HTTP 200 with `degraded=true` vs 503 — pick one and justify.

200 with degraded=true, because the CRM did answer, the response is real and
usable, and only the enriched fields are missing. A 503 says the CRM is down,
which is not true and would take the whole customer detail page out for a
dependency that only supplies part of it.

The condition is that degraded must be honest and machine readable. A 200 that
looks identical to a healthy response is the wrong answer, the caller cannot
tell it is missing data and neither can a dashboard. On a write path the choice
flips, see step 4 of the pattern map.

## Step 4 — User message

Draft one UI string: *Account details temporarily limited.*

Account details temporarily limited. Balance and tier are unavailable right now,
the rest of this customer is up to date.

It names what is missing rather than showing a generic error, so the user knows
which parts of the page they can trust.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab32-fallback-contract.md`
- [ x ] Fields kept listed
- [ x ] Fields dropped listed
- [ x ] API signal chosen and justified
- [ x ] User message drafted
