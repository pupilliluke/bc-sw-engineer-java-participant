# Lab 16 starter — timed path (~45 minutes)

**Theme:** API exception handling — ErrorResponse + GlobalExceptionHandler + correlation

## Copy into your workspace

Do **not** grade work only inside the course `labs/` clone. Copy this `starter/` into your bootcamp examples tree as `lab16-crm`.

**Windows (PowerShell)** — from this lab folder:

```powershell
New-Item -ItemType Directory -Force -Path "$env:USERPROFILE\java-bootcamp\examples\lab16-crm" | Out-Null
Copy-Item -Recurse -Force ".\starter\*" "$env:USERPROFILE\java-bootcamp\examples\lab16-crm\"
cd $env:USERPROFILE\java-bootcamp\examples\lab16-crm
```

**macOS / Linux:**

```bash
mkdir -p ~/java-bootcamp/examples/lab16-crm
cp -R starter/. ~/java-bootcamp/examples/lab16-crm/
cd ~/java-bootcamp/examples/lab16-crm
```

Full GUIDE: [`../LAB-16-GUIDE.md`](../LAB-16-GUIDE.md)

## 45-minute checklist

- [ ] Implement `ErrorResponse` (+ toJson always includes errors)
- [ ] Complete `BusinessException.notFound` / `conflict` factories
- [ ] Implement `GlobalExceptionHandler` (business / validation / unexpected)
- [ ] Wire facade create/get/changeStatus to return `ApiResult` (catch BusinessException before Exception)
- [ ] Refactor validator/service to throw BusinessException; demo 400/404/409 in Main
- [ ] Finish `GlobalExceptionHandlerTest`; run smoke test

## Smoke test

```bash
mvn -B clean test
```

Evidence under `~/java-bootcamp/notes/screenshots/lab-16/` (redact secrets).

## Timed-path Pass criteria

| Criterion | Pass / Fail |
| --------- | ----------- |
| Handler tests green (404 / 409 / generic 500) | Pass / Fail |
| Facade Fail JSON includes correlationId lab-request-001 | Pass / Fail |
| 400 validation / 404 not-found / 409 conflict demonstrated | Pass / Fail |
| Illegal transition leaves CUS-1001 ACTIVE | Pass / Fail |

Continue remaining GUIDE steps as homework / full path if needed.
