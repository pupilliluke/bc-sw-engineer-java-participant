-- Money as exact decimal rather than integer cents. V1 is already applied, so
-- the change arrives as a new migration rather than an edit to V1.
ALTER TABLE account
  ALTER COLUMN balance_cents TYPE NUMERIC(19, 2) USING balance_cents / 100.0;

ALTER TABLE account RENAME COLUMN balance_cents TO balance;

ALTER TABLE account ALTER COLUMN balance SET DEFAULT 0;
