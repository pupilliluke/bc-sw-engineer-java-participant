# PostgreSQL notes — Lab 37

Working notes for the container and psql, kept separate from the design
rationale in `database/design-decisions.md`.

## Two roles, on purpose

| Role | Created by | Privileges |
| --- | --- | --- |
| `crm_admin` | the container image, from `POSTGRES_USER` | bootstrap superuser |
| `crm_app` | `database/01_create_user.sql` | LOGIN, owns schema `crm_app`, nothing else |

Everything after step 4 runs as `crm_app`. If a script needs `crm_admin`, that
is a signal the schema is asking for a privilege the application should not
have.

## psql on Windows

`psql` 17 is installed natively (`C:\Program Files\PostgreSQL\17\bin`), so the
scripts run from the host against the published port rather than through
`docker exec`. Either works; the host client keeps the file paths simple.

Password handling: set `PGPASSWORD` for the command rather than typing it at a
prompt, and let it come from `.env`, never from a literal in a script.

```bash
export $(grep -v '^#' .env | xargs)
PGPASSWORD="$CRM_APP_PASSWORD" psql -h localhost -U crm_app -d crm -f database/02_schema.sql
```

## Useful psql

| Command | What it shows |
| --- | --- |
| `\dt` | tables in the search path |
| `\d customer` | columns, indexes and constraints of one table |
| `\dn` | schemas |
| `\du` | roles and their attributes |
| `\di` | indexes |
| `\errverbose` | full SQLSTATE and constraint name of the last error |
| `\conninfo` | who and where you are connected as |

`\errverbose` is the one worth remembering — the default error line names the
constraint, and `\errverbose` adds the five-digit SQLSTATE that application
code branches on.

## Volume

`compose.yaml` mounts the named volume `crm_pgdata` at
`/var/lib/postgresql/data`, so `docker compose down` keeps the data and
`docker compose down -v` destroys it. The volume is what makes the runtime
persistent across a laptop restart; it is Docker-managed and never committed.

## Reset paths

| Goal | Command |
| --- | --- |
| stop, keep data | `docker compose stop` |
| remove container, keep data | `docker compose down` |
| start over completely | `docker compose down -v` |
| rebuild the schema only | `psql -f database/05_drop.sql` then `02` and `03` |
