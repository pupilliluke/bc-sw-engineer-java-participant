# Lab 37 prep checklist

## Earlier exercise files present?

| File | Present? (yes/no) |
| ---- | ----------------- |
| notes/lab37-design.md | yes |
| notes/lab37-er-sketch.md | yes |
| notes/lab37-constraints.md | yes |
| notes/lab37-ddl-todos.md | yes |
| notes/lab37-seed-and-verify-plan.md | yes |

kept under notes/Week 4/Module 37/ with the rest of the module notes.

## Deliverables preview

the four artifacts the lab grades: schema SQL, seed SQL, an ER diagram,
and design-decisions notes.

## Fixtures (verify)

| ID | Name | Status |
| -- | ---- | ------ |
| CUS-1001 | Amina Khan | ACTIVE |
| CUS-1002 | Ravi Singh | PROSPECT |

Amina owns an account, Ravi owns none.

## Env reminder

database credentials live in a local .env file only, never committed. the
same rule as lab 36's Vite env, with the difference that this one holds a
real password rather than a public base URL, so the file is in .gitignore
before it is created rather than after.

## Runtime

Docker 27.3.1 and psql 17 are both on this machine, so the lab can run a
local PostgreSQL container rather than depending on a shared instance.
nothing is started in the pre-lab.

## Next labs preview

lab 38 tunes query performance on this same schema, lab 39 maps it with
JPA entities. that is why the column types and the constraint names
matter now, both later labs inherit them.

## Scope statement

Pre-lab only — prepare for lab; do not complete full Lab 37 now.

## Self mark

Overall prep: Pass
If Fail, revisit exercise(s): n/a
