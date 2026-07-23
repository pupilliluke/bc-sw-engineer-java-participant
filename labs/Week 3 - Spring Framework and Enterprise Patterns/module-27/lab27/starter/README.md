# Lab 27 starter — timed path (~45 minutes)

**Theme:** @Transactional boundaries

## Copy into your workspace

Do **not** grade work only inside the course `labs/` clone. Copy this `starter/` into your bootcamp examples tree as `lab27-crm`.

**Windows (PowerShell)** — from this lab folder:

```powershell
New-Item -ItemType Directory -Force -Path "$env:USERPROFILE\java-bootcamp\examples\lab27-crm" | Out-Null
Copy-Item -Recurse -Force ".\starter\*" "$env:USERPROFILE\java-bootcamp\examples\lab27-crm\"
cd $env:USERPROFILE\java-bootcamp\examples\lab27-crm
```

**macOS / Linux:**

```bash
mkdir -p ~/java-bootcamp/examples/lab27-crm
cp -R starter/. ~/java-bootcamp/examples/lab27-crm/
cd ~/java-bootcamp/examples/lab27-crm
```

## 45-minute checklist

- [ ] Add `@Transactional` on `TransferService.transfer` (service boundary)
- [ ] Implement debit/credit + `TransactionLog` write inside the TX
- [ ] Force rollback when destination is `ACC-FORCE-FAIL`
- [ ] Fill ACID table in `docs/acid-notes.md`
- [ ] Smoke: happy MAIN→LOYALTY; failure leaves MAIN unchanged

## Smoke test

```bash
mvn -B test
mvn -B spring-boot:run
```

Evidence under `~/java-bootcamp/notes/screenshots/lab-27/` (redact secrets).

## Timed-path Pass criteria

| Criterion | Pass / Fail |
| --------- | ----------- |
| Happy transfer updates both balances + log | Pass / Fail |
| Forced fail rolls back (no log row / MAIN unchanged) | Pass / Fail |
| `@Transactional` on service method (not controller) | Pass / Fail |
| ACID notes cite evidence | Pass / Fail |

Continue remaining GUIDE steps as homework / full path if needed.
