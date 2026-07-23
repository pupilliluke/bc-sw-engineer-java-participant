# Lab 23 starter — timed path (~45 minutes)

**Theme:** Boot auto-config / application.yml basics

## Copy into your workspace

Do **not** grade work only inside the course `labs/` clone. Copy this `starter/` into your bootcamp examples tree as `lab23-crm`.

**Windows (PowerShell)** — from this lab folder:

```powershell
New-Item -ItemType Directory -Force -Path "$env:USERPROFILE\java-bootcamp\examples\lab23-crm" | Out-Null
Copy-Item -Recurse -Force ".\starter\*" "$env:USERPROFILE\java-bootcamp\examples\lab23-crm\"
cd $env:USERPROFILE\java-bootcamp\examples\lab23-crm
```

**macOS / Linux:**

```bash
mkdir -p ~/java-bootcamp/examples/lab23-crm
cp -R starter/. ~/java-bootcamp/examples/lab23-crm/
cd ~/java-bootcamp/examples/lab23-crm
```

## 45-minute checklist

- [ ] Confirm `pom.xml` has web + actuator + test starters
- [ ] Complete `application.yml` (name, port, Actuator exposure)
- [ ] Fill profile teasers `application-dev.yml` / `application-prod.yml`
- [ ] Implement create/get for CUS-1001 / CUS-1002 in service
- [ ] Smoke: health UP; note 3 auto-config gifts vs 3 ownership items in docs

## Smoke test

```bash
mvn -B test
mvn -B spring-boot:run
# then: curl http://localhost:8080/actuator/health
```

Evidence under `~/java-bootcamp/notes/screenshots/lab-23/` (redact secrets).

## Timed-path Pass criteria

| Criterion | Pass / Fail |
| --------- | ----------- |
| App starts on 8080 | Pass / Fail |
| /actuator/health returns UP | Pass / Fail |
| CUS-1001 create/get evidence (or IT green) | Pass / Fail |
| YAML + profile teasers present | Pass / Fail |

Continue remaining GUIDE steps as homework / full path if needed.
