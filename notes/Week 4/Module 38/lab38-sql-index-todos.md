# Lab 38 — Fill SQL/Index TODOs

## Step 1 — Paste

Created `notes/lab38-todos.sql`:

```sql
-- baseline (avoid)
SELECT * FROM customer
WHERE lower(full_name) = 'amina khan';

-- optimized lookup
SELECT customer_id, full_name, status
FROM customer
WHERE customer_id = 'CUS-1001';

-- supporting index ideas
CREATE INDEX idx_customer_status ON customer (status);
CREATE INDEX idx_account_customer ON account (customer_id);

-- paging sketch
SELECT customer_id, full_name
FROM customer
ORDER BY customer_id
LIMIT 20 OFFSET 0;

-- TODO: prefer keyset pagination (WHERE customer_id > :last) for deep pages
```

## Step 2 — Fill

`lower`, `'CUS-1001'`, `idx_customer_status`, `idx_account_customer`,
`20`, `0`.

| Blank | Fill | Why |
| --- | --- | --- |
| baseline function | `lower` | the non-sargable shape to recognise, not to copy |
| lookup value | `'CUS-1001'` | Amina Khan's fixture |
| customer index | `idx_customer_status` | names the table and the column it covers |
| account index | `idx_account_customer` | the join key on the child side |
| LIMIT / OFFSET | `20` / `0` | one page, from the first row |

the baseline is labelled avoid for two separate reasons and both are
worth keeping straight. `SELECT *` returns columns the screen does not
render, and `lower(full_name)` wraps the column in a function so a plain
index on full_name cannot be matched. fixing one does not fix the other.

`idx_account_customer` is the same index as `ix_account_customer`, which
my lab 37 step 9 already creates. so in the lab that CREATE INDEX is
either a duplicate to skip or a rename, not a new structure, and the
before/after for it would show no change because the index is already
there. worth checking the existing indexes before running the block
rather than after.

the two names here use `idx_` while lab 37 used `ix_`. the deck supplies
`idx_`, my schema uses `ix_`, and consistency inside one database matters
more than either prefix, so anything I keep from this lab gets `ix_`.

## Step 3 — Keyset note

`-- TODO: prefer keyset pagination (WHERE customer_id > :last) for deep
pages` is on the last line of the file above.

OFFSET makes the database produce every row it then throws away, so page
5000 at 20 a page still generates 100000 rows to skip and the cost grows
with the page number. keyset carries the last key from the previous page
and seeks to it, `WHERE customer_id > :last ORDER BY customer_id LIMIT
20`, which is a range predicate against a sorted index and costs the same
on page 5000 as on page 1. the trade is that it walks pages in sequence
and cannot jump to an arbitrary page number.

it needs a stable unique sort key or rows are skipped and repeated
across pages. customer_id is the primary key so it qualifies. sorting by
created_at alone would not, two customers can share a timestamp.

## Step 4 — No run

nothing here is executed against PostgreSQL. this is paper. Lab 38 does
the loading and the measuring, and each index gets its plan recorded
before and after so the report has an entry per experiment: the query,
the plan before, the plan after, and whether the index was kept or
dropped.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab38-sql-index-todos.md`
- [ x ] Blanks filled
- [ x ] Keyset TODO present
- [ x ] No-run confirmation
