Lab 38 PostgreSQL query performance on the CRM schema (reflection
questions, checkpoints)

built as examples\lab38-crm on a copy of the lab 37 schema, five numbered
performance scripts against PostgreSQL 17 in Docker. the work was a
50,002-row load with 70/30 status skew, a measured baseline, an email
index cycle, the status/created composite, the sargable date rewrite,
join strategy comparison, offset and keyset paging, and a challenge cycle
that dropped and re-earned the composite. evidence is in
database\performance\report.md and plan text under
notes\screenshots\lab-38.

the GUIDE is Oracle throughout, DBMS_STATS, DBMS_XPLAN, ALLSTATS LAST,
PL/SQL loops, TRUNC on timestamps. the PostgreSQL equivalents are used
and each script keeps the Oracle original commented at the bottom rather
than swapped silently. the container is crm-postgres-lab38 because lab 37
holds the crm-postgres name and container_name is not project-prefixed.

three of the guide's expected before-states did not exist because the lab
37 schema already carried the index, uk_customer_email, ix_account_customer,
and the report records those as inherited, with the email before-state
manufactured by dropping the constraint on purpose and restoring it by
promotion.


REFLECTION QUESTIONS

1. Which design decision most affected correctness of page results?

the customer_id tiebreak in the ORDER BY. the two fixtures share a
created_at to the microsecond because the seed inserted them in one
transaction, so ordering by created_at alone leaves their relative order
unspecified and page peers can shuffle between plans. with the tiebreak,
page 0 and page 1 were disjoint and the keyset page equalled the offset
page by EXCEPT in both directions.

2. What evidence proves the email index was worth the write cost?

920 buffers down to 4 on the lookup every login and duplicate check runs,
against about 2,900 extra buffers per 5,000 inserts. the honest half of
the answer is that the read win is a side effect, the index exists
because the UNIQUE constraint requires one to enforce itself, so the
write cost was never optional.

3. Which failure was hardest to diagnose (wrong plan, skew, ties)?

wrong plan, the guide's keyset predicate. the OR form returned the right
rows from an Index Scan, which looks healthy, but Rows Removed by Filter
equalled the page position and buffers matched OFFSET exactly, 5,047 at
position 5,000. the seek was silently a scan. the row-value form
(created_at, customer_id) < (ts, id) put the tuple into Index Cond and
the same position cost 23 buffers.


CHECKPOINTS

| Checkpoint | Confirm | Result |
| --- | --- | --- |
| A1 | lab38-crm under examples | Pass, examples/lab38-crm, compose and ddl and performance scripts |
| A2 | 50k+ customers with documented skew | Pass, 50,002 rows, 35,001 ACTIVE / 15,001 PROSPECT, fixtures preserved |
| A3 | stats gathered, rows and last-analyzed recorded | Pass, ANALYZE not DBMS_STATS, reltuples 50,002 / 25,001 matches actuals |
| B1 | baseline email plan with actuals | Pass, EXPLAIN (ANALYZE, BUFFERS) not ALLSTATS LAST, Index Scan, 4 buffers, median 0.058 ms |
| B2 | unique email index with improved plan evidence | Pass, 920 to 4 buffers, before-state manufactured and labelled, constraint restored by promotion |
| B3 | status/created list index with measured list query | Pass, 1,006 to 23 buffers, 10.57 to 0.070 ms median |
| C1 | half-open range vs TRUNC comparison | Pass, date_trunc for TRUNC, identical 556 ids, equal buffers, 2x time, E-rows 250 vs 530 |
| C2 | selective vs broad join notes | Pass, nested loop at rows=1 and 6 buffers, hash join at 35k and 1,320 buffers |
| C3 | deterministic offset and keyset paging, no dup/missing ids | Pass, INTERSECT and EXCEPT all zero rows, row-value keyset 23 buffers at position 5,000 |
| D1 | each retained index challenged with drop/remeasure | Pass, composite dropped and re-earned 1,006 to 23, email cycle in 03, constraint-backed documented not dropped |
| D2 | report.md complete | Pass, buffers, medians where multi-run, write cost, plan-hash note since PostgreSQL has none |
| D3 | no secrets or dumps in git | Pass, .env gitignored, .env.example committed, evidence is plan text only |
