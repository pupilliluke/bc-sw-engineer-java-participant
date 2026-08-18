# Lab 38 — Sargability

## Reference

| Predicate | Sargable? |
| --- | --- |
| customer_id = 'CUS-1001' | Yes |
| status = 'ACTIVE' | Yes (with index) |
| LOWER(full_name) = 'amina khan' | Usually no on plain index |
| created_at >= TIMESTAMP '2026-01-01' | Yes (range) |
| date_trunc('day', created_at) = ... | Often weaker than range |

## Step 1 — Study table

table copied above. search ARGument-ABLE, the predicate is sargable when
the optimizer can hand the value straight to an index, which it can do
while the indexed column sits alone on one side of the comparison.

the rule the five rows share: index the column, then compare the column.
the moment the column is wrapped in a function the index holds
`full_name` while the query asks about `lower(full_name)`, and those are
not the same sorted values, so the plan falls back to a Seq Scan.

status is the row worth reading twice. sargable is not the same as fast.
the predicate is index-friendly and the index may still go unused,
because four values across the table means the filter matches too many
rows to be worth the lookups. sargability is about whether the optimizer
*can* use an index, selectivity is about whether it *should*.

## Step 2 — Rewrite

```sql
-- non-sargable
SELECT customer_id, full_name FROM customer
WHERE lower(full_name) = 'amina khan';

-- store the normalized value and index that
ALTER TABLE customer ADD COLUMN full_name_normalized VARCHAR(150);
CREATE INDEX ix_customer_name_norm ON customer (full_name_normalized);
SELECT customer_id, full_name FROM customer
WHERE full_name_normalized = 'amina khan';
```

my lab 37 schema already solves this once, for email. `email_normalized`
is a stored lowercased column with a CHECK that keeps it lowercase and a
unique index on it, so the search is plain equality against an indexed
column and never needs `lower()` at query time. the name search is the
same problem with the same fix, and the CHECK is the part that makes it
hold, a normalized column nothing enforces drifts.

the alternative is a functional index, `CREATE INDEX ... ON customer
(lower(full_name))`, which indexes the expression itself and makes the
original query sargable without a new column. it costs less schema churn
and more care, the query has to spell the expression exactly as the index
declares it.

`ILIKE 'amina%'` is index-friendlier than `ILIKE '%amina%'`, a leading
wildcard has no prefix to seek to so it scans regardless.

## Step 3 — Half-open range

prefer `created_at >= d AND created_at < d+1` over wrapping the column.

```sql
-- non-sargable
WHERE date_trunc('day', created_at) = DATE '2026-07-01'

-- sargable half-open range
WHERE created_at >= TIMESTAMP '2026-07-01 00:00:00'
  AND created_at <  TIMESTAMP '2026-07-02 00:00:00'
```

half-open, `>=` on the lower bound and `<` on the upper, because
created_at is TIMESTAMPTZ in my schema and BETWEEN with a closed upper
bound either drops the last fraction of a second or double-counts
midnight at the boundary of the next window. the two-sided form is also
the one that stays correct when the window is a month rather than a day.

## Step 4 — Oracle note

`TRUNC(created_at)` is Oracle. PostgreSQL has `date_trunc(text,
timestamp)` and it takes the unit as the first argument, so the mapping
is `TRUNC(created_at)` to `date_trunc('day', created_at)`. both wrap the
column and both lose the plain index, so the mapping is only about
syntax. the answer to either one is the range rewrite above, not the
translation.

same family as the dialect list from lab 37: NUMBER to NUMERIC, VARCHAR2
to VARCHAR, DBMS_STATS to ANALYZE, DBMS_XPLAN to EXPLAIN.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab38-sargability.md`
- [ x ] Table copied
- [ x ] One rewrite written
- [ x ] Range preference stated
