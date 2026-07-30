# AI review notes — Lab 12

Assistant used: Claude Code (Opus 5) in the IntelliJ terminal, not Copilot
inline ghost text. Where an entry shows a suggestion block, that block is the
assistant standing in for Copilot Chat on the prompt quoted above it, in the
shape Copilot returns for that prompt. Same rule and same wording as
`copilot-notes/ai-review-notes.md` (Lab 10) and Lab 11's log, so the audit trail
stays consistent about who produced what.

`copilot-notes/ai-review-notes.md` beside this project is Lab 10's review log,
carried across with the tree. Nothing in it is Lab 12 work.

## lab12-001 — extract-method pass over the refactored service

- Date: 2026-07-29
- File: `src/main/java/com/northstar/crm/service/CustomerService.java`
- Verdict: **accept with edits**

Prompt:

    Review CustomerService after the refactor. Suggest extract-method or rename
    improvements. Plain Java 21, no Spring, no JPA.

Suggestion, condensed to the four items it returned:

    1. getCustomer duplicates the blank-id check that requireNonBlank already
       performs, and requireExisting performs it a third time. Delegate.
    2. requireUniqueId could return the existing customer instead of void, so
       createCustomer can upsert when the id is taken.
    3. Extract a CorrelationContext class rather than a field, so the id can be
       set per request later.
    4. createCustomer takes five parameters; consider a CustomerRequest record
       or a builder.

Item 1: **accepted, and it was a real defect.** The blank-customerId rule
existed in three places — inline in `getCustomer`, in `requireNonBlank`, and
again in `requireExisting`. That is precisely the duplicated-validation smell
this lab exists to remove, reintroduced inside the cleanup. `getCustomer` is now
a one-line delegate to `requireExisting`, which calls `requireNonBlank`. Verified
by grep: `isBlank` appears once in the file.

Item 2: **rejected.** This is the silent-upsert the guide's Step 4 warns about,
arriving exactly as predicted. It reads as a small ergonomic improvement and is
a contract change — `createCustomer` would stop rejecting duplicates and start
overwriting Amina's record with whatever the second caller sent. The
`duplicateIdRejected` test would fail, which is the test doing its job. Rejecting
a duplicate is documented behaviour carried from the baseline, and changing it
would need a decision, not a refactor.

Item 3: **rejected for now, recorded as deferred.** A `CorrelationContext` is
sanctioned by the guide's project tree as optional, and the pre-lab correlation
note already reasoned that MDC is where a correlation id belongs. But nothing in
this lab sets a per-request id — there is no request, no HTTP layer until Lab 13,
and no logging framework on the classpath. A context class with one hardcoded
value is ceremony around a constant. The field stays; the reasoning is in
`notes/Week 2/Module 12/lab12-correlation-todos.md`.

Item 4: **rejected, wrong lab.** `CustomerRequest` already exists in
`com.northstar.crm.dto` as a Lab 8 stub, and wiring it into the service API is
the DTO work Lab 14 does. Doing it here would change the API the Step 3 tests
and Checkpoint B are written against.

One risk caught, for the record: item 2. An accepted "upsert on duplicate" would
have compiled, read as an improvement in review, and silently removed a
validation rule that support depends on. It was caught because a test asserted
the rejection, not because the suggestion looked wrong.

## Manual refactor choice, no assistant involved

`getCustomer` returning `Customer` rather than `Optional<Customer>` was decided
by hand. `Optional` is the Lab 10/11 idiom for `findByCustomerId`, and it is the
better signature in general. It is not used here because the guide's Step 3 tests
call `svc.getCustomer("CUS-1001").getFullName()` directly and assert that an
unknown id throws — an `Optional` return makes the unknown-id case a caller
problem again, which is the `null`-return smell wearing a nicer type. Throwing
with a correlation id is the contract this lab asks for. Recorded because it is a
deliberate divergence from the previous two labs.
