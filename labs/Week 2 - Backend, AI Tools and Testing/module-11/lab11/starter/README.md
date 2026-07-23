# Lab 11 starter — timed path (~45 minutes)

**Theme:** Copilot for tests + refactoring (JUnit 5 / Mockito)

## Copy into your workspace

**Windows (PowerShell)** — from this lab folder:

```powershell
New-Item -ItemType Directory -Force -Path "$env:USERPROFILE\java-bootcamp\examples\lab11-crm" | Out-Null
Copy-Item -Recurse -Force ".\starter\*" "$env:USERPROFILE\java-bootcamp\examples\lab11-crm\"
cd $env:USERPROFILE\java-bootcamp\examples\lab11-crm
```

**macOS / Linux:**

```bash
mkdir -p ~/java-bootcamp/examples/lab11-crm
cp -R starter/. ~/java-bootcamp/examples/lab11-crm/
cd ~/java-bootcamp/examples/lab11-crm
```

Full GUIDE: [`../LAB-11-GUIDE.md`](../LAB-11-GUIDE.md)

## 45-minute checklist

- [ ] Complete `CustomerTest` + `CustomerServiceTest` TODOs (reject weak assertions)
- [ ] Flesh out `CustomerNotifier`; wire into service; Mockito verify
- [ ] Extract duplicated validation helper
- [ ] Fill `copilot-notes/ai-test-refactor-notes.md` lab11-001–004
- [ ] Run smoke test

## Smoke test

```bash
mvn -B test
```

## Timed-path Pass criteria

| Criterion | Pass / Fail |
| --------- | ----------- |
| `mvn test` green (≥ a few meaningful assertions) | Pass / Fail |
| Mockito verifies notifier (or documented equivalent) | Pass / Fail |
| Notes show at least one rejected trivial test | Pass / Fail |

Continue remaining GUIDE steps as homework / full path if needed.
