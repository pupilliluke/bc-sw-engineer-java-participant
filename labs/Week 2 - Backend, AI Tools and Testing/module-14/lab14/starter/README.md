# Lab 14 starter — timed path (~45 minutes)

**Theme:** DTOs + Jakarta Bean Validation at the API boundary

## Copy into your workspace

**Windows (PowerShell)** — from this lab folder:

```powershell
New-Item -ItemType Directory -Force -Path "$env:USERPROFILE\java-bootcamp\examples\lab14-crm" | Out-Null
Copy-Item -Recurse -Force ".\starter\*" "$env:USERPROFILE\java-bootcamp\examples\lab14-crm\"
cd $env:USERPROFILE\java-bootcamp\examples\lab14-crm
```

**macOS / Linux:**

```bash
mkdir -p ~/java-bootcamp/examples/lab14-crm
cp -R starter/. ~/java-bootcamp/examples/lab14-crm/
cd ~/java-bootcamp/examples/lab14-crm
```

Full GUIDE: [`../LAB-14-GUIDE.md`](../LAB-14-GUIDE.md)

## 45-minute checklist

- [ ] Add Bean Validation annotations on `CustomerRequestDTO`
- [ ] Implement `CustomerMapper` + `CustomerApiFacade` TODOs
- [ ] Complete validation tests (valid / bad email / blank name)
- [ ] Main demo returns response DTOs only; invalid path shows `lab-request-001`
- [ ] Fill `docs/dto-boundary-notes.md`
- [ ] Run smoke test

## Smoke test

```bash
mvn -B clean test
```

## Timed-path Pass criteria

| Criterion | Pass / Fail |
| --------- | ----------- |
| Validation tests green | Pass / Fail |
| Facade returns DTOs only (no entity leak) | Pass / Fail |
| Invalid payload rejected before service | Pass / Fail |
| Correlation id visible on failure | Pass / Fail |

Continue remaining GUIDE steps as homework / full path if needed.
