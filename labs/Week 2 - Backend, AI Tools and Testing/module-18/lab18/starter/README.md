# Lab 18 starter — timed path (~45 minutes)

**Theme:** Mockito isolation — stub / verify / never / ArgumentCaptor + BDDMockito

## Copy into your workspace

Do **not** grade work only inside the course `labs/` clone. Copy this `starter/` into your bootcamp examples tree as `lab18-crm`.

**Windows (PowerShell)** — from this lab folder:

```powershell
New-Item -ItemType Directory -Force -Path "$env:USERPROFILE\java-bootcamp\examples\lab18-crm" | Out-Null
Copy-Item -Recurse -Force ".\starter\*" "$env:USERPROFILE\java-bootcamp\examples\lab18-crm\"
cd $env:USERPROFILE\java-bootcamp\examples\lab18-crm
```

**macOS / Linux:**

```bash
mkdir -p ~/java-bootcamp/examples/lab18-crm
cp -R starter/. ~/java-bootcamp/examples/lab18-crm/
cd ~/java-bootcamp/examples/lab18-crm
```

Full GUIDE: [`../LAB-18-GUIDE.md`](../LAB-18-GUIDE.md)

## 45-minute checklist

- [ ] Wire `CustomerServiceMockitoTest` with `@Mock` repo + real validator
- [ ] Implement activate stub/verify; not-found `never().save`; ArgumentCaptor on add
- [ ] Complete `CustomerServiceBddMockTest` with given/then/should
- [ ] Fill `docs/isolation-policy.md`
- [ ] Run smoke test twice (full suite green)

## Smoke test

```bash
mvn -B clean test
mvn -B test -Dtest=CustomerServiceMockitoTest,CustomerServiceBddMockTest
```

Evidence under `~/java-bootcamp/notes/screenshots/lab-18/` (redact secrets).

## Timed-path Pass criteria

| Criterion | Pass / Fail |
| --------- | ----------- |
| Mockito suite green (stub/verify/captor) | Pass / Fail |
| BDDMockito suite green | Pass / Fail |
| not-found path never calls save | Pass / Fail |
| isolation-policy.md filled | Pass / Fail |

Continue remaining GUIDE steps as homework / full path if needed.
