# Lab 39 — Paging and Locking Notes

## Step 1 — Page request

```java
Pageable page = PageRequest.of(0, 20, Sort.by("customerId"));
Page<CustomerEntity> result = customerRepository.findByStatus("ACTIVE", page);
```

page numbers are zero-based, so `of(0, 20, ...)` is the first page of
twenty. the deck states what this compiles to, OFFSET = page * size and
LIMIT = size, which means every Pageable in this application is the
construct lab 38 measured. page 0 was 23 buffers and position 5,000 was
5,047, so the cost of a Pageable is not flat and the page number is what
moves it.

the size has to be bounded before it reaches PageRequest:

```java
int safeSize = Math.min(Math.max(size, 1), 100);
```

a client asking for size=1000000 otherwise gets an unbounded query with
a page number painted on it. the floor matters too, size=0 is not a
smaller page, it is a different failure.

sort needs an allow-list, not whatever column arrives in the query
string. `customerId`, `createdAt`, `fullName` and `status` are the
defensible ones and anything else is rejected rather than passed through.
two reasons: a client that can sort by any field can name a field that
is not a column, and lab 38 showed sorting by an unindexed column means
a Seq Scan plus a sort of 35,001 rows, 1,006 buffers against 23. an
open sort parameter is a performance switch handed to the caller.

the tiebreak is the real content of step 1. `Sort.by("customerId")` on
its own is already the tiebreak; sorting the list the CRM actually shows
is `Sort.by(Order.desc("createdAt")).and(Sort.by(Order.desc("customerId")))`,
newest first with the id settling ties. without the id, rows sharing a
created_at have no defined order, they can come back differently between
two plans, and a row can appear on page 1 and again on page 2 while
another is never shown at all. lab 38 proved this is not hypothetical
here: Amina and Ravi share a created_at to the microsecond because the
lab 37 seed inserted them in one transaction and CURRENT_TIMESTAMP is
transaction start time.

that sort order also has to match `ix_customer_status_created`, which is
`(status, created_at DESC, customer_id DESC)`. matching it is what keeps
the list an Index Scan instead of a sort.

## Step 2 — Response

return `totalElements` plus the content slice, so the UI can render both
the rows and how many there are.

| Page<T> gives | Used for |
| --- | --- |
| `getContent()` | the twenty rows |
| `getTotalElements()` | the count the UI shows |
| `getTotalPages()` | how many pages the pager draws |
| `getNumber()` / `getSize()` | echoing the request back |

`totalElements` is not free. Page runs a second COUNT query against the
same predicate, so a paged endpoint is two round trips, and the count
has to examine every matching row even though the page returns twenty.
`Slice<T>` is the version that drops the count and only answers whether
there is a next page, which is enough for an infinite-scroll UI and
cheaper. Page is the right default here because the CRM list shows a
total.

the content is DTOs by the time it reaches the client, not
CustomerEntity. the entity carries `version_no` and the surrogate
`customer_id`, neither of which the API has ever exposed, and labs 34 to
36 have carried a UI shape of their own since before this schema existed.

## Step 3 — Optimistic lock

a second writer on Amina fails if the version is stale, and the user
retries.

concretely: two requests load Amina at `version_no = 3`. the first saves,
Hibernate issues `UPDATE ... SET ... version_no = 4 WHERE customer_id = ?
AND version_no = 3`, one row matches, done. the second saves with the
same WHERE, zero rows match because the column is now 4, and Hibernate
raises `OptimisticLockingFailureException` rather than reporting success.

nothing is locked while the user is thinking, which is the point. no row
is held between the read and the write, so a long-open edit form costs
nobody anything, and the conflict is detected at save time instead of
prevented by waiting.

the service maps it to HTTP 409, the same status the duplicate email
gets from 23505. 409 rather than 500 because nothing is broken, the
request simply lost a race, and rather than 422 because the payload was
valid when it was written. the client's move is to reload and reapply,
and the response should say so, since a retry of the identical stale
payload fails identically.

`@Version` is what Hibernate reads on the way in and writes on the way
out. no code increments it.

## Step 4 — Correlation

log `lab-request-001` on every lock failure.

```
WARN  optimistic lock conflict publicId=CUS-1002 expectedVersion=3
      correlationId=lab-request-001
```

a 409 is the one error a user can legitimately cause without doing
anything wrong, so it will be reported to support, and the report will
be "it said conflict" with no timestamp. the correlation id is what
connects that sentence to the two requests in the log. `lab-request-001`
is the fixture value the labs carry, and it is already the
`correlation_id` column on `customer_status_history` from lab 37, so the
same value spans the log line and the row.

what to log: the correlation id, the public id, the version that was
expected. not the entity, not the payload, and not the email, since a
conflict warning is not a place to spill customer data.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab39-paging-locking.md`
- [ x ] PageRequest example
- [ x ] Stale version behavior
- [ x ] Correlation logging note
