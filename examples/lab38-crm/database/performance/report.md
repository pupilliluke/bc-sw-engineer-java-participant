# Lab 38 — Performance report

| Experiment | Plan hash / notes | Buffers | Median time | Write cost |
| ---------- | ----------------- | ------- | ----------- | ---------- |
| lab38-001 baseline email | Index Scan using uk_customer_email, E-rows 1 / A-rows 1 | 4 shared hit | 0.058 ms | none added, index already exists as the UNIQUE constraint |
| lab38-002 after email index | Index Scan using ux_customer_email_norm, E-rows 1 / A-rows 1 | 4 shared (1 hit + 3 read) | 0.114 ms | +2,877 buffers per 5,000 inserts |
| lab38-003 OFFSET deep page | Index Scan + Limit, produces 5,020 rows to keep 20; past-end page flips to Seq Scan + full sort | 5,047 | 8.146 ms, single run | none |
| lab38-004 keyset page | guide's OR form filters, 5,047 buffers; row-value form seeks via Index Cond | 23 | 0.387 ms, single run | none |
| lab38-005 ACTIVE list, before / after ix_customer_status_created | Seq Scan + top-N heapsort → Index Scan, stops at 20 rows | 1,006 → 23 | 10.57 ms → 0.070 ms | one more index maintained per customer write |
| lab38-006 sargable date rewrite | Seq Scan both; wrapped E-rows 250 vs range E-rows 530, A-rows 556 | 1,006 → 1,006 | 10.2 ms → 4.7 ms, single runs | none, query rewrite only |
| lab38-007 join strategies | Nested Loop at rows=1 vs Hash Join at rows=35,065, planner flips on cardinality | 6 vs 1,320 | 0.072 ms vs 51.0 ms, single runs | none, ix_account_customer inherited from Lab 37 |

PostgreSQL has no plan hash equivalent to Oracle's. The operation line plus
the estimated and actual row counts identify the plan instead, and
`pg_stat_statements.queryid` would be the closest hash if the extension is
enabled.

## Data set

| Measure | Value |
| --- | --- |
| customers | 50,002 |
| ACTIVE / PROSPECT | 35,001 / 15,001, 70/30 |
| accounts | 25,001 |
| customers with an account | 25,001 |
| fixtures | CUS-1001 ACTIVE with account, CUS-1002 PROSPECT with none |

`ANALYZE customer; ANALYZE account;` gathered after the load. `reltuples`
reads 50,002 and 25,001, matching the actual counts, so the planner is not
working from pre-load estimates.

## Binds

Fixed for every measurement in this lab, so before and after plans compare.

| Bind | Value |
| --- | --- |
| email | `user000001@example.test` |
| email, fixture | `amina@example.com` |
| public_id | `CUS-1001` |
| status | `ACTIVE` |
| day | `2026-07-01` |

## Baseline

Email lookup, captured with `EXPLAIN (ANALYZE, BUFFERS)`. Oracle's
`DBMS_XPLAN.DISPLAY_CURSOR` with `ALLSTATS LAST` does not exist in
PostgreSQL; `ANALYZE` supplies the actual rows and time, `BUFFERS` the block
counts.

```
Index Scan using uk_customer_email on customer
  (cost=0.41..8.43 rows=1 width=46) (actual time=0.017..0.018 rows=1 loops=1)
  Index Cond: ((email_normalized)::text = 'user000001@example.test'::text)
  Buffers: shared hit=4
Planning:
  Buffers: shared hit=15
Planning Time: 0.067 ms
Execution Time: 0.037 ms
```

| Field | Value |
| --- | --- |
| operation | Index Scan using uk_customer_email |
| E-rows / A-rows | 1 / 1 |
| buffers, execution | shared hit=4 |
| execution time | 0.058 ms median of five runs, 0.051 to 0.080 |

execution buffers are 4 on every run. planning buffers read 15 in a warm
session and 106 in a fresh one, so the planning figure measures catalog
caching rather than the query and is not the number to compare across
experiments.

The baseline is an Index Scan, not the `TABLE ACCESS FULL` the lab guide
expects. `customer` inherits `CONSTRAINT uk_customer_email UNIQUE
(email_normalized)` from the Lab 37 DDL, and PostgreSQL backs a unique
constraint with a unique index, so the column was already indexed before this
lab began. Estimated rows equal actual rows, so the plan is not a statistics
problem either.

The consequence for lab38-002 is recorded there: the index step 6 asks for
already exists under a different name.

## Step 6 — email index, measured before and after

The guide's step 6 creates `ux_customer_email_norm` on `customer
(email_normalized)` and expects the plan to move off a full scan. That
comparison is not available on this schema: the Lab 37 DDL already carries
`CONSTRAINT uk_customer_email UNIQUE (email_normalized)`, PostgreSQL backs a
unique constraint with a unique index, and lab38-001 was therefore already an
Index Scan.

To get a real contrast the inherited constraint was dropped first, the
unindexed state measured, the index built, and the constraint then restored by
promoting the new index with `ALTER TABLE ... ADD CONSTRAINT uk_customer_email
UNIQUE USING INDEX ux_customer_email_norm`. The "before" below is manufactured
on purpose. It is not the state the schema arrives in.

### Read, identical query and bind

| | No index | With index |
| --- | --- | --- |
| operation | Seq Scan on customer | Index Scan using ux_customer_email_norm |
| estimated cost | 0.00..1545.03 | 0.41..8.43 |
| E-rows / A-rows | 1 / 1 | 1 / 1 |
| rows removed by filter | 50,001 | 0 |
| buffers | 920 shared hit | 4 (1 hit + 3 read) |
| execution time | 3.251 ms | 0.114 ms |

230x fewer buffers and roughly 28x less time. `Rows Removed by Filter: 50001`
is the line that names the problem in the unindexed plan: the query returns
one row and the executor examined every row in the table to find it.

The index scan reads 4 blocks whether the index was just built or has been in
place since Lab 37, so the lab38-001 and lab38-002 index figures agree. The
0.114 ms here against 0.058 ms in lab38-001 is a cold index just after
creation, three of its four blocks came from disk rather than cache.

### Write cost, 5,000 inserts, transaction rolled back

| | No index | With index |
| --- | --- | --- |
| buffers | 32,609 | 35,486 |
| dirtied / written | 99 / 99 | 164 / 155 |
| execution time, four runs | 38.1, 40.2, 50.4, 63.2 ms | 51.3, 56.4, 59.8, 128.1 ms |
| median | 45.3 ms | 58.1 ms |

The unique index costs about 2,877 extra buffers per 5,000 inserts, roughly
9% more block traffic, and it dirties and writes about 60% more pages. Those
figures are stable across runs.

The timings are not. The two ranges overlap, one with-index run at 128 ms is
far outside the rest, and four samples on a laptop container is not enough to
put a percentage on the difference. Direction agrees with the buffer counts
and with what the index has to do, the magnitude from timing alone should not
be quoted.

This answers the exercise 2 prediction directly. A unique email index does not
help inserts, it charges them. Every insert has to check the index for an
existing value before it can commit, and that check is the mechanism enforcing
uniqueness. `uk_customer_email` exists because the constraint requires it, and
the read speed in the table above is a consequence of that rather than the
reason it was created.

### Decision

Keep. Not because step 6 improved anything, it did not, the index was already
there. It is retained because the constraint requires it and the read evidence
above shows what the schema would lose without it: a 230x buffer increase on
the lookup every CRM screen calls first.

Naming: the guide's step 12 lists `ux_customer_email_norm` among retained
indexes. Here that index exists as `uk_customer_email`, because the promote in
`03_indexes.sql` renames it into the Lab 37 constraint. A bare unique index
would enforce the same rule, but it would disappear from `pg_constraint`, the
Lab 37 verify script asserts the constraint by name, and Lab 39 inherits the
constraint names as its contract. The `CREATE UNIQUE INDEX
ux_customer_email_norm` statement the guide asks for is in `03_indexes.sql`
as written.

Final state verified: `pk_customer`, `uk_customer_email`, `uk_customer_public`
on customer, 50,002 rows, both write-cost transactions rolled back.

## Step 7 — active-list index

`ix_customer_status_created` on `customer (status, created_at DESC,
customer_id DESC)`, measured with the list query the CRM screen runs: status
equality, newest first, `customer_id` as the tiebreak, `FETCH FIRST 20 ROWS
ONLY`. Identical query and literal both sides, three runs each.

| | Before | After |
| --- | --- | --- |
| operation | Seq Scan → Sort (top-N heapsort) → Limit | Index Scan using ix_customer_status_created → Limit |
| rows scanned | 50,002, 15,001 removed by filter | 23 index entries, stops at row 20 |
| buffers | 1,006 shared hit | 23 |
| execution time, three runs | 10.086, 10.568, 12.525 ms | 0.051, 0.070, 0.074 ms |
| median | 10.57 ms | 0.070 ms |

44x fewer buffers, ~150x less time. The before plan reads the whole table,
filters to 35,001 ACTIVE rows, and heapsorts all of them to surface 20. The
after plan starts at the newest ACTIVE entry and walks the index forward,
already in order, and the Limit stops it after 20 rows — the estimated total
is 35,065 rows but only 23 buffers are ever touched.

The prediction in lab38-index-tradeoffs.md was that status alone stays a Seq
Scan because four values are not selective, and that the index earns its place
composite with created_at. This measurement is the second half confirmed, with
the mechanism visible: it is not the equality filter that pays for this index,
ACTIVE still matches 70% of the table. It is the ordering. The index delivers
rows pre-sorted newest-first inside each status, so a 20-row page costs 20
entries instead of a 35,001-row sort. The first half — status alone — is
what step 12's challenge cycle can test by dropping this index and filtering
without the ORDER BY.

Decision: keep, pending the step 12 challenge. Every retained index rides on
the write path measured in step 6, so the evidence standard is the same.

## Step 8 — sargable date rewrite

Both forms in `04_optimized.sql`, day bind `2026-07-01`, run as crm_app. The
guide's contrast query uses Oracle `TRUNC(created_at)`, which does not exist
in PostgreSQL; the wrapped form here is `date_trunc('day', created_at)`, the
same wrap with the PostgreSQL name.

Identity proven first: `EXCEPT` in both directions returns zero rows, and the
day holds 556 rows. The rewrite changes the plan, not the answer.

| | Wrapped: date_trunc = day | Range: >= day AND < day+1 |
| --- | --- | --- |
| operation | Seq Scan | Seq Scan |
| buffers | 1,006 shared hit | 1,006 shared hit |
| E-rows / A-rows | 250 / 556 | 530 / 556 |
| execution time, single run | 10.208 ms | 4.662 ms |

Three findings, and the first is the honest one:

Buffers are identical. Sargability is a capability, not a speedup by itself.
The range form can ride a B-tree with `created_at` leading, but no such index
exists on this schema — `ix_customer_status_created` leads with `status`, so
a bare date range cannot enter it. Both queries therefore read the whole
table, and the guide's expected "index range capability when a suitable index
exists" is real but unrealized here. The capability shows up when the range
is combined with the status equality the composite index leads with; that
pairing is measurable in the step 12 challenge cycle without creating any
index this lab has not already justified.

The time still halved, and the Filter line says why: the wrapped form executed
`date_trunc()` once per row, 50,002 calls, while the range form did two inline
comparisons per row. Per-row predicate cost is separate from the index
question, and the rewrite wins it even with no index in sight.

The estimates diverge. The planner cannot see through a function, so the
wrapped form's E-rows is a canned guess, 250 against an actual 556, off by
2.2x. The range form reads the column histogram directly, 530 against 556.
Estimate quality is not cosmetic: join strategy choices in the next step are
made from these numbers, and a predicate that blinds the histogram makes them
worse everywhere downstream.

Timezone note: the range literals are timezone-naive `TIMESTAMP` compared
against a `TIMESTAMPTZ` column, so the session timezone places the day
boundary. This container runs UTC and the generator anchored its timestamps
in UTC, so the boundary is consistent; a client connecting with a local
TimeZone setting would carve a different 24 hours. Application SQL in Lab 39
should bind timestamptz values, not bare dates.

Decision: use the range form in application SQL. Zero cost, same answer,
better estimates, and it is the only form an index on created_at could ever
serve.

## Step 9 — join strategies, customer → account

The guide opens this step with `CREATE INDEX ix_account_customer`; that index
exists from Lab 37 step 9, the third inherited index this lab's script would
have re-created. Measured as-is, nothing created.

Selective join, Amina by `public_id = 'CUS-1001'`:

| | |
| --- | --- |
| operation | Nested Loop, Index Scan on uk_customer_public → Index Scan on ix_account_customer |
| outer E-rows / A-rows | 1 / 1 |
| buffers | 6 shared hit, 3 per side |
| execution time, single run | 0.072 ms |

Broad join, all ACTIVE customers with accounts, aggregated:

| | |
| --- | --- |
| operation | Hash Join under GroupAggregate, Seq Scan both inputs |
| hash side | customer, 35,001 rows, 2,153 kB, Buckets 65536, Batches 1 |
| join E-rows / A-rows | 17,532 / 20,001 |
| buffers | 1,320 shared hit |
| execution time, single run | 51.028 ms |

The planner flipped strategy on one number: estimated outer rows, 1 against
35,065. At one row the inner index probe runs once and a nested loop is
unbeatable. At 35,000 rows, 35,000 index probes lose to building a hash table
once and probing it in memory; Batches: 1 confirms the whole hash fit in
work_mem with no spill. The planner also hashed the larger input, 35k
customers rather than 25k accounts — with both fitting in memory it minimized
the probe count, 25,001 probes instead of 35,001.

The join estimate ran 14% low, 17,532 against 20,001, close enough to choose
soundly. This is the step 8 estimate point closing the loop: the strategy
choice is made from row estimates, and it is only as good as the predicates
feeding it are sargable.

Neither plan is the correct one in general. Nested loop is right for the
detail screen, hash is right for the report, and the same schema serves both
because the planner re-decides per query from cardinality. Nothing to keep or
drop: both indexes involved are Lab 37 inheritance, already justified by the
step 6 and step 12 evidence standard.

## Step 10 — deterministic offset paging

Query per the guide: status equality, `ORDER BY created_at DESC, customer_id
DESC`, `OFFSET :offset ROWS FETCH NEXT :page_size ROWS ONLY`. page_size 20,
matching the step 7 baseline. Disjointness held: page 0 INTERSECT page 1
returned zero rows.

| Page | OFFSET | Plan | Buffers | Time, single run | Produced -> kept |
| --- | --- | --- | --- | --- | --- |
| 0 | 0 | Index Scan -> Limit | 23 | 0.500 ms | 20 -> 20 |
| 1 | 20 | Index Scan -> Limit | 43 | 0.107 ms | 40 -> 20 |
| deep | 5,000 | Index Scan -> Limit | 5,047 | 8.146 ms | 5,020 -> 20 |
| past end | 100,000 | Seq Scan -> Sort -> Limit | 1,012 | 17.061 ms | 35,001 -> 0 |

The offset tax is visible at page 1 already: 40 rows produced, 20 discarded.
Cost grows linearly with the offset because every skipped row is produced
first and thrown away.

The past-end page is the sharpest line in the table. Only 35,001 ACTIVE rows
exist, so page 5000 of 20 returns nothing — and at OFFSET 100,000 the planner
knows early termination cannot happen, abandons the index entirely, and
switches to Seq Scan plus a full quicksort of 35,001 rows. The empty page is
the most expensive page in the system, and it does not even use the index the
real pages use. An application that lets a client ask for an arbitrary page
number is buying this plan.

Determinism note, failure experiment 3: the bulk generator produces unique
timestamps, but the two Lab 37 fixtures were inserted in one transaction and
`CURRENT_TIMESTAMP` is transaction start time, so Amina and Ravi share a
`created_at` to the microsecond — a real tie. Under `ORDER BY created_at`
alone their relative order is unspecified and may differ between plans, which
is how page peers shuffle. The `customer_id` tiebreak closes it.

## Step 11 — keyset paging

Boundary rows were read into psql variables with `\gset`, so every seek bind
is provably the row at that position in this dataset. Keyset page 1 equals
offset page 1, EXCEPT in both directions, zero rows.

The finding: the guide's seek predicate does not seek in PostgreSQL. Its OR
form, `created_at < :ts OR (created_at = :ts AND customer_id < :id)`, is
logically correct — every page matched its offset twin exactly — but the
planner cannot push an OR across an index boundary. The plans show
`Index Cond: status = 'ACTIVE'` only, the OR in the Filter line, and
`Rows Removed by Filter` equal to the position: 20, 40, 60, then 5,000. At
position 5,000 the OR form costs 5,047 buffers — identical to OFFSET 5,000.
Walking pages with it re-pays the whole prefix every page, exactly the cost
keyset exists to remove.

The PostgreSQL spelling is the row-value comparison from the starter's own
TODO: `(created_at, customer_id) < (:ts, :id)`. Both columns sit in
`ix_customer_status_created` with matching direction, so the planner pushes
the whole tuple into the index:

| Position 5,000 | OR form | Row-value form |
| --- | --- | --- |
| Index Cond | status only | status AND ROW(created_at, customer_id) < ROW(:ts, :id) |
| Filter / rows removed | the OR / 5,000 | none / none |
| buffers | 5,047 | 23 |
| execution time, single run | 3.915 ms | 0.387 ms |

Both forms return the identical page, EXCEPT both directions, zero rows. The
rewrite changes cost, not answers — same shape of result as step 8, and the
same lesson: the predicate must be spelled so the index can see it.

## Step 12 — challenge cycle

`ix_customer_status_created` challenged by DDL in `05_cleanup_indexes.sql`:
measured with the index, dropped, measured without, recreated on the evidence.

| ACTIVE list, first page | With index | Without |
| --- | --- | --- |
| plan | Index Scan -> Limit | Seq Scan -> top-N heapsort -> Limit |
| buffers | 23 | 1,006 |
| time, single run | 0.116 ms | 9.506 ms |

The low-selectivity prediction from the module exercises was also tested while
the index existed: `COUNT(*) WHERE status = 'ACTIVE'` with no ORDER BY chose a
Seq Scan, 1,006 buffers, with the index available. ACTIVE is 70% of the
table; equality on status alone is not what this index is for. Its value is
the ordering — and keyset paging additionally depends on it for the row-value
seek.

Write cost, the same 5,000-insert transaction rolled back, single runs: without
this index 37,692 buffers, 71.8 ms; with it 64,684 buffers, 82.5 ms. The
buffer delta, ~27,000, is far larger than the email index's ~2,900 in step 6:
a three-column composite touched by every insert is not free, and this is the
honest price of the read numbers above.

Retained indexes and why:

| Index | Evidence |
| --- | --- |
| uk_customer_email (guide: ux_customer_email_norm, promoted) | step 6: 920 -> 4 buffers on email lookup; constraint requires it |
| ix_customer_status_created | this cycle: 1,006 -> 23 on the list page; sole enabler of the keyset seek |
| ix_account_customer | step 9: nested-loop join at 6 buffers; Lab 37 inheritance |
| pk_customer, uk_customer_public, uk_account_number, pk_account | constraint-backed, Lab 37 contract, not challengeable by DROP INDEX |

Dropped / not retained: none. Every index in the final state either carries a
constraint or has a measured regression recorded above. No index was added
that EXPLAIN did not justify; the two CREATE INDEX statements the guide
supplies for already-indexed columns (email, account.customer_id) were
measured as inherited rather than duplicated.

## Step 13 — failure experiments and evidence

| # | Experiment | Where | Observed |
| - | --- | --- | --- |
| 1 | wrapped date vs half-open range | step 8, 04_optimized.sql | equal buffers, 2x time from per-row date_trunc, E-rows 250 vs 530 |
| 2 | drop email index, re-run lookup | step 6, 03_indexes.sql | Seq Scan, 920 buffers, 50,001 rows filtered; restored |
| 3 | ORDER BY without the id tiebreak | step 10, 04_optimized.sql | fixtures tie on created_at to the microsecond; order unspecified |
| 4 | deep OFFSET vs keyset at 5,000 | steps 10-11, 04_optimized.sql | 5,047 vs 23 buffers; OR-form keyset secretly costs the same as OFFSET |
| 5 | stats after bulk load | step 3, 02_baseline.sql | gathered before measuring; reltuples matches actuals, E-rows track A-rows throughout |

Plan text saved under `notes/screenshots/lab-38/`. No dumps, no credentials;
passwords live in `.env`, which is gitignored.

## Why keyset beats deep OFFSET

Because OFFSET is work already done and thrown away, and keyset is a seek to
where the last page ended. OFFSET n produces n rows and discards them, every
page, so cost grows with the page number and the empty past-end page costs
the most of all — the planner even abandons the index for a full sort once
the offset exceeds what early termination could save. Keyset carries the last
row's (created_at, customer_id) forward and enters the index at that exact
tuple: 23 buffers at page 1 and 23 buffers at position 5,000, position-blind.
The price is honesty in the contract: pages are walked in order, there is no
random jump to page 250 — and in PostgreSQL the seek must be spelled as a
row-value comparison, because the textbook OR form silently degrades to
exactly the OFFSET cost it was meant to avoid.
