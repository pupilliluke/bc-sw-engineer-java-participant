# Lab 38 starter — timed path (~45 minutes)

**Theme:** SQL performance — plans, indexes, keyset paging

## Copy into your workspace

Do **not** grade work only inside the course `labs/` clone. Copy this `starter/` into your bootcamp examples tree as `lab38-crm`.

**Windows (PowerShell)** — from this lab folder:

```powershell
New-Item -ItemType Directory -Force -Path "$env:USERPROFILE\java-bootcamp\examples\lab38-crm" | Out-Null
Copy-Item -Recurse -Force ".\starter\*" "$env:USERPROFILE\java-bootcamp\examples\lab38-crm\"
cd $env:USERPROFILE\java-bootcamp\examples\lab38-crm
```

**macOS / Linux:**

```bash
mkdir -p ~/java-bootcamp/examples/lab38-crm
cp -R starter/. ~/java-bootcamp/examples/lab38-crm/
cd ~/java-bootcamp/examples/lab38-crm
```

## 45-minute checklist

- [ ] Copy Lab 37 DDL into `database/ddl/` (or use provided stubs)
- [ ] Complete `01_generate_data.sql` (≥ synthetic customers; keep CUS-1001)
- [ ] Capture baseline plan in `02_baseline.sql`; add indexes in `03`
- [ ] Rewrite TRUNC/range + compare joins; implement keyset vs OFFSET
- [ ] Fill `performance/report.md` with plan hash / buffers / median time notes

## Smoke test

```bash
# apply scripts with psql in order 01→05; paste EXPLAIN into report.md
```

Evidence under `~/java-bootcamp/notes/screenshots/lab-38/` (redact secrets).

## Timed-path Pass criteria

| Criterion | Pass / Fail |
| --------- | ----------- |
| Baseline EXPLAIN captured before indexes | Pass / Fail |
| Email/status indexes added and re-measured | Pass / Fail |
| Keyset paging script present | Pass / Fail |
| report.md has experiment ids lab38-001+ | Pass / Fail |

Continue remaining GUIDE steps as homework / full path if needed.
