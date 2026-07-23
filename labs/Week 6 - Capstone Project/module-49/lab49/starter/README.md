# Lab 49 starter — session block (~45 minutes)

**Theme:** Capstone backend interaction slice (service stub package)  
**Target:** merge into `~/java-bootcamp/examples/customer-management-platform/` (or standalone `lab49-crm`)

Timed-path policy: [`labs/_STARTER-PATH.md`](../../../../_STARTER-PATH.md)

## Copy into your workspace

**Windows (PowerShell)** — from this lab folder:

```powershell
$dest = "$env:USERPROFILE\java-bootcamp\examples\customer-management-platform"
New-Item -ItemType Directory -Force -Path "$dest\backend" | Out-Null
Copy-Item -Recurse -Force ".\starter\*" $dest\
cd $dest
```

**macOS / Linux:**

```bash
mkdir -p ~/java-bootcamp/examples/customer-management-platform
cp -R starter/. ~/java-bootcamp/examples/customer-management-platform/
cd ~/java-bootcamp/examples/customer-management-platform
```

If you already have a multi-module Maven layout, copy only `backend/` sources into your existing module and keep your root `pom.xml`.

## 45-minute session checklist

- [ ] Open starter under IntelliJ; JDK 21 SDK
- [ ] Fill `// TODO` in `InteractionService` (validate customer, persist, publish)
- [ ] Fill `CreateInteractionRequest` validation annotations
- [ ] Wire controller → service; return 201 shape for `CUS-1001`
- [ ] Sketch event fields in `CustomerInteractionRecordedV1`
- [ ] Run build smoke (`mvn -B -f backend/pom.xml test` or root verify)

## Build / smoke checklist

```bash
cd backend   # or platform root if pom is there
mvn -B -q -DskipTests compile
mvn -B test  # expect failures until TODOs filled — then green for session tests
```

Evidence under `~/java-bootcamp/notes/screenshots/lab-49/`.

## Timed-path Pass criteria

| Criterion | Pass / Fail |
| --------- | ----------- |
| Service TODOs filled (no `UnsupportedOperationException`) | Pass / Fail |
| Compile succeeds | Pass / Fail |
| At least one unit test for create-interaction path green (or documented baseline) | Pass / Fail |
| Fixtures `CUS-1001` / `lab-request-001` appear in code or test | Pass / Fail |

Full path (multi-day): Flyway migration, Kafka IT, Problem Details, consumer/DLT, `docs/backend-demo.md` — see GUIDE.
