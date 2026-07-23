# Flyway stubs (Lab 50)

Place versioned SQL under your Spring module’s `src/main/resources/db/migration/` (or keep this folder and point Flyway locations).

## Rules

1. **Never** edit an already-applied version on a shared DB — add `V51__…` instead.
2. Session stub file: `V50__customer_interaction.sql` — resolve every `-- TODO` before apply.
3. Confirm with `flyway info` / app startup logs / `SELECT * FROM flyway_schema_history`.

## TODO checklist

- [ ] Align table name with JPA `@Table`
- [ ] FK / indexes decided
- [ ] Apply on shared Postgres (instructor host) or local Docker
- [ ] Capture apply proof screenshot for Lab 52 evidence index
