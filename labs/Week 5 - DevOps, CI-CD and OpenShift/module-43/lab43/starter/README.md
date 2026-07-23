# Lab 43 starter — timed path (~45 minutes)

**Theme:** GitHub Actions CI — verify, reports, package-once checksum

## Copy into your workspace

Do **not** grade work only inside the course `labs/` clone. Copy this `starter/` into your bootcamp examples tree as `lab43-crm`.

**Windows (PowerShell)** — from this lab folder:

```powershell
New-Item -ItemType Directory -Force -Path "$env:USERPROFILE\java-bootcamp\examples\lab43-crm" | Out-Null
Copy-Item -Recurse -Force ".\starter\*" "$env:USERPROFILE\java-bootcamp\examples\lab43-crm\"
cd $env:USERPROFILE\java-bootcamp\examples\lab43-crm
```

**macOS / Linux:**

```bash
mkdir -p ~/java-bootcamp/examples/lab43-crm
cp -R starter/. ~/java-bootcamp/examples/lab43-crm/
cd ~/java-bootcamp/examples/lab43-crm
```

## 45-minute checklist

- [ ] Complete TODOs in `.github/workflows/ci.yml` (PR vs main/tag gates)
- [ ] Wire Maven cache + `clean verify` (no silent `-DskipTests` on verify)
- [ ] Upload Surefire reports; package-once JAR + SHA-256 on main/tags
- [ ] Optional: add `-Psecurity-scan` step (document residual risk if skipped)
- [ ] Fill `docs/ci-runbook.md` (rerun policy, secrets location)

## Smoke test

```bash
# Local YAML sanity (optional):
# actionlint .github/workflows/ci.yml
# Push to a branch with Actions enabled and open a PR — expect verify green/red with reports.
```

Evidence under `~/java-bootcamp/notes/screenshots/lab-43/` (redact tokens).

## Timed-path Pass criteria

| Criterion | Pass / Fail |
| --------- | ----------- |
| Workflow triggers on PR and push | Pass / Fail |
| Verify job runs without skipTests | Pass / Fail |
| Package job gated to main/tags | Pass / Fail |
| Runbook explains secret storage | Pass / Fail |

Continue remaining GUIDE steps as homework / full path if needed.
