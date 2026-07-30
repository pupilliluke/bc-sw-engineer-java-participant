# Before / after — Lab 12

Refactor of `com.northstar.crm.service.CustomerService`, 2026-07-29.
Windows PowerShell, Temurin JDK 21, Maven 3.9.9.

Frozen baseline: `src/main/java/com/northstar/crm/service/CustomerService.before.java.txt`
(68 lines). Refactored: `CustomerService.java` (86 lines). The snapshot is a real
file, not a description — every claim below can be diffed against it.

## 1. Smell → fix mapping

Ten smells catalogued in [`smells.md`](smells.md); each row here is the fix.

| # | Smell | Fix |
| - | ----- | --- |
| 1 | `doStuff`, `data`, params `a`–`e` | `createCustomer` / `getCustomer` / `updateStatus`; field `customersById`; every parameter named for what it holds |
| 2 | Raw `List` + casts | `Map<String, Customer> customersById` — typed, no cast anywhere in the class |
| 3 | 39-line method, five jobs | Three public methods, one job each, plus three private helpers |
| 4 | Stringly-typed status + else-if chain | `CustomerStatus` at the API. The whole chain is deleted; a typo is now a compile error, not a silent `PROSPECT` |
| 5 | `==` on IDs (line 58) | `Map` keyed by `customerId`, so lookup is `hashCode`/`equals` on the value. Proven by `lookupByEqualValueNotSameReference` |
| 6 | `a == ""` blank check (line 17) | `requireNonBlank` using `isBlank()` |
| 7 | `null` returned for three different failures | `IllegalArgumentException` for blank/unknown, `IllegalStateException` for duplicate. No method returns `null` |
| 8 | `System.out.println("bad"/"dup"/"ok"/"upd")` | Removed. Failure information travels in exception messages, each carrying `correlationId=lab-request-001` |
| 9 | Magic `"UPDATE"` branch keyed off the name | Deleted. Status changes go only through `updateStatus`. Experiment 5 below shows what it cost |
| 10 | Dead second scan of the list | Gone with the branch |

## 2. Method list

**Before**

```
Object doStuff(String a, String b, String c, String d, String e)
Object get(String id)
CustomerResponse create(CustomerRequest request)   // Lab 8 stub
CustomerResponse getById(String customerId)        // Lab 8 stub
```

**After**

```
Customer createCustomer(String customerId, String fullName, String email,
                        String phone, CustomerStatus status)
Customer getCustomer(String customerId)
Customer updateStatus(String customerId, CustomerStatus newStatus)

private void requireNonBlank(String value, String fieldName)
private void requireUniqueId(String customerId)
private Customer requireExisting(String customerId)
private String correlationSuffix()

CustomerResponse create(CustomerRequest request)   // Lab 8 stub, unchanged
CustomerResponse getById(String customerId)        // Lab 8 stub, unchanged
```

The two Lab 8 DTO stubs are carried through untouched because
`CustomerController` calls them. They are not part of the messy teaching
artifact and were present in the frozen snapshot too.

Note on the helpers: `getCustomer` is a one-line delegate to `requireExisting`,
and `requireExisting` calls `requireNonBlank`. The blank-customerId rule
therefore exists in exactly one place. A first pass at this refactor had it in
three — inline in `getCustomer`, in `requireNonBlank`, and again in
`requireExisting` — which is the duplicated-validation smell reappearing inside
the cleanup. Caught by grepping for the check rather than by reading.

## 3. Test output

```
mvn -B clean verify

[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0 -- in com.northstar.crm.entity.CustomerTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0 -- in com.northstar.crm.service.CustomerServiceTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
[INFO] Building jar: target\customer-service.jar
[INFO] BUILD SUCCESS
```

Step 3 red run, before the refactor existed: the target-API tests did not fail,
they did not compile.

```
[ERROR] CustomerServiceTest.java:[23,35] cannot find symbol
  symbol:   method createCustomer(String,String,String,String,CustomerStatus)
  location: variable service of type com.northstar.crm.service.CustomerService
```

Worth recording precisely, because "red" and "does not compile" are different
states. Nothing about behaviour was proven at that point — the tests only became
evidence once Steps 4–5 made them runnable.

The six service tests: create-then-get, duplicate rejected, unknown fails with
id **and** correlation id in the message, Ravi PROSPECT → ACTIVE, blank id
rejected, and lookup by an equal-but-not-identical String.

## 4. Manual demo transcript

Real output, `java -cp target/classes com.northstar.crm.Main`.

**Before** (frozen baseline, harness driving `doStuff`/`get`):

```
ok CUS-1001
ok CUS-1002
get literal      -> Customer{customerId='CUS-1001', fullName='Amina Khan', status=ACTIVE}
get new String   -> null
dup
duplicate 1001   -> null
bad
blank id         -> null
ok CUS-1003
typo status      -> Customer{customerId='CUS-1003', fullName='Priya Patel', status=PROSPECT}
unknown id       -> null
```

Three things to read out of that. `get new String` lost Amina even though she
was created one line earlier — the `==` bug, which is the support ticket in the
lab's business scenario. `get new String` and `unknown id` are both `null`, so a
caller cannot tell "lookup is broken" from "no such customer". And `typo status`
was called with `"AKTIVE"` and returned a customer with status `PROSPECT` and a
cheerful `ok` — a typo became valid-looking data with nothing reported.

**After** (refactored API):

```
get literal      -> Customer{customerId='CUS-1001', fullName='Amina Khan', status=ACTIVE}
get new String   -> Customer{customerId='CUS-1001', fullName='Amina Khan', status=ACTIVE}
duplicate 1001   -> EX: Customer id already exists: CUS-1001 correlationId=lab-request-001
blank id         -> EX: customerId must be provided correlationId=lab-request-001
activate 1002    -> Customer{customerId='CUS-1002', fullName='Ravi Singh', status=ACTIVE}
null status      -> defaults to PROSPECT: Customer{customerId='CUS-1003', fullName='Priya Patel', status=PROSPECT}
unknown id       -> EX: Customer not found: CUS-9999 correlationId=lab-request-001
```

`get new String` now returns Amina. The three failures are distinguishable by
type and message, and each carries the correlation id. The `"AKTIVE"` case is
absent from the after transcript because it can no longer be written — the
parameter is `CustomerStatus`, so the typo is a compile error. The remaining
`null status` line shows the one defaulting behaviour deliberately preserved
from the baseline.

## 5. Sizing

```
CustomerService.before.java.txt    68 lines
CustomerService.java               86 lines
```

The refactor is **longer**, and that is the honest result. It buys three public
methods with stated contracts, three named validation helpers, and javadoc, in
place of one 39-line method that silently returned `null` three different ways.
Lines are not the metric; the before file's worst method had five
responsibilities and the after file's worst has one.

## 6. Behaviour preserved vs deliberately changed

| Behaviour | Before | After | Verdict |
| --- | --- | --- | --- |
| Reject blank id / name | yes, returns `null` | yes, throws | preserved, contract improved |
| Reject duplicate id | yes, returns `null` | yes, throws | preserved, contract improved |
| Default missing status to PROSPECT | yes | yes | preserved deliberately |
| Set `createdAt` on create | yes | yes | preserved |
| Look up by id | broken under `==` | works | **fixed** |
| Update status via name containing "UPDATE" | yes | no | **removed** — see experiment 5 |
| Print to stdout on every path | yes | no | **removed** |

The `"UPDATE"` removal is the only intentional loss of behaviour, and it is the
point of the lab rather than an accident.

## 7. Failure experiments

| # | Experiment | Observed | Restored |
| - | ---------- | -------- | -------- |
| 1 | Removed the `Customer` import from `CustomerService` | `cannot find symbol: class Customer` at lines 23, 34, 52, 56 — four sites from one deleted line | import restored, `verify` green |
| 2 | Blank `customerId` on create | `IllegalArgumentException: customerId must be provided correlationId=lab-request-001` from `requireNonBlank` | helper kept; locked by `blankCustomerIdRejected` |
| 3 | Created `CUS-1001` twice | `IllegalStateException: Customer id already exists: CUS-1001 correlationId=lab-request-001` | detection kept; locked by `duplicateIdRejected` |
| 4 | `getCustomer(new String("CUS-1001"))` after create | returns Amina. The frozen `get()` returned `null` for this exact call — both transcripts above | Map keying kept; locked by `lookupByEqualValueNotSameReference` |
| 5 | Reintroduced the `"UPDATE"` branch briefly | see below | branch deleted again, `verify` green at 8 |

Experiment 5 in full. The branch was re-added to `createCustomer`, then two
customers were created differing **only** in name:

```
plain name    -> PROSPECT
upd
name w/UPDATE -> ACTIVE
```

Same method, same requested status, different result, decided by whether the
customer's name happened to contain a keyword. Amina Khan gets `PROSPECT`;
"Amina UPDATE Khan" gets `ACTIVE`. No signature, no test, and no document could
tell a caller that. This is why the branch is gone rather than tidied.

## 8. Concepts to discuss

Answered in [`../../../notes/Week 2/Module 12/lab12-answers.md`](../../../notes/Week%202/Module%2012/lab12-answers.md),
alongside the reflection questions.
