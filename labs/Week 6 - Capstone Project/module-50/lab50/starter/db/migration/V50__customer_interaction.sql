-- V50__customer_interaction.sql
-- Lab 50 Flyway stub — fill TODOs before applying to shared/local PostgreSQL.
-- Do not invent a parallel schema that fights Lab 49 migrations; merge versions carefully.

-- TODO: choose schema (public vs crm)
-- CREATE SCHEMA IF NOT EXISTS crm;

CREATE TABLE IF NOT EXISTS customer_interaction (
    id              UUID PRIMARY KEY,
    customer_id     VARCHAR(32)  NOT NULL,
    interaction_type VARCHAR(32) NOT NULL,
    summary         VARCHAR(1024) NOT NULL,
    correlation_id  VARCHAR(64)  NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    -- TODO: FK to customer table if it exists (customer_id → customer.id)
    -- TODO: CHECK constraint on interaction_type
);

-- TODO: index for timeline queries by customer
-- CREATE INDEX IF NOT EXISTS idx_interaction_customer_created
--   ON customer_interaction (customer_id, created_at DESC);

-- TODO: optional uniqueness for idempotent correlation per customer
-- CREATE UNIQUE INDEX IF NOT EXISTS uq_interaction_customer_correlation
--   ON customer_interaction (customer_id, correlation_id);

-- Seed note (optional): CUS-1001 / CUS-1002 belong in customer table migration, not here.
