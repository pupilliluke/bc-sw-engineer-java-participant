# Lab 25 starter — timed path (~45 minutes)

**Theme:** Repository + service layer

## Copy into your workspace

Do **not** grade work only inside the course `labs/` clone. Copy this `starter/` into your bootcamp examples tree as `lab25-crm`.

**Windows (PowerShell)** — from this lab folder:

```powershell
New-Item -ItemType Directory -Force -Path "$env:USERPROFILE\java-bootcamp\examples\lab25-crm" | Out-Null
Copy-Item -Recurse -Force ".\starter\*" "$env:USERPROFILE\java-bootcamp\examples\lab25-crm\"
cd $env:USERPROFILE\java-bootcamp\examples\lab25-crm
```

**macOS / Linux:**

```bash
mkdir -p ~/java-bootcamp/examples/lab25-crm
cp -R starter/. ~/java-bootcamp/examples/lab25-crm/
cd ~/java-bootcamp/examples/lab25-crm
```

## 45-minute checklist

- [ ] Implement `InMemoryCustomerRepository` (seed CUS-1001 / CUS-1002)
- [ ] Put create/get/duplicate rules in `CustomerService` (not controller)
- [ ] Confirm controller has zero repository imports
- [ ] Run smoke + note AI review (or N/A) in docs/lab25-001.md

## Smoke test

```bash
mvn -B test
mvn -B spring-boot:run
```

Evidence under `~/java-bootcamp/notes/screenshots/lab-25/` (redact secrets).

## Timed-path Pass criteria

| Criterion | Pass / Fail |
| --------- | ----------- |
| GET CUS-1001 and CUS-1002 succeed | Pass / Fail |
| Duplicate create fails in service | Pass / Fail |
| Controller does not import repository types | Pass / Fail |
| `mvn -B test` green (or documented remaining TODOs) | Pass / Fail |

Continue remaining GUIDE steps as homework / full path if needed.
