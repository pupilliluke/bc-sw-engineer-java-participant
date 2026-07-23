# Lab 8 starter — timed path (~45 minutes)

**Theme:** Maven layered project structure (Northstar CRM skeleton)

## Copy into your workspace

Do **not** grade work only inside the course `labs/` clone. Copy this `starter/` into your bootcamp examples tree as `lab8-crm`.

**Windows (PowerShell)** — from this lab folder:

```powershell
New-Item -ItemType Directory -Force -Path "$env:USERPROFILE\java-bootcamp\examples\lab8-crm" | Out-Null
Copy-Item -Recurse -Force ".\starter\*" "$env:USERPROFILE\java-bootcamp\examples\lab8-crm\"
cd $env:USERPROFILE\java-bootcamp\examples\lab8-crm
```

**macOS / Linux:**

```bash
mkdir -p ~/java-bootcamp/examples/lab8-crm
cp -R starter/. ~/java-bootcamp/examples/lab8-crm/
cd ~/java-bootcamp/examples/lab8-crm
```

Full GUIDE path (homework / extended): [`../LAB-8-GUIDE.md`](../LAB-8-GUIDE.md)

## 45-minute checklist

- [ ] Confirm seven layer packages under `com.northstar.crm` (no Spring/JPA imports)
- [ ] Fill `// TODO` on entity, DTOs, repository, service, controller, config, exception
- [ ] Complete `Main` banner lines (packages + `CUS-1001` / `CUS-1002`)
- [ ] Fill `docs/layer-flow.md` and `docs/CODING-STANDARDS.md` TODOs
- [ ] Run smoke test; capture evidence under `notes/screenshots/lab-8/`

## Smoke test

```powershell
mvn -q clean compile
java -cp target\classes com.northstar.crm.Main
```

**macOS / Linux:**

```bash
mvn -q clean compile
java -cp target/classes com.northstar.crm.Main
```

Expected snippet: `Northstar CRM skeleton — Lab 8` plus seven packages and sample customer IDs.

## Timed-path Pass criteria

| Criterion | Pass / Fail |
| --------- | ----------- |
| `mvn clean compile` succeeds | Pass / Fail |
| `Main` prints skeleton banner + packages + CUS-1001/CUS-1002 | Pass / Fail |
| Seven layer packages present; no Spring/JPA/Kafka in stubs | Pass / Fail |
| `docs/layer-flow.md` + `docs/CODING-STANDARDS.md` filled | Pass / Fail |

Continue remaining GUIDE steps as homework / full path if needed.
