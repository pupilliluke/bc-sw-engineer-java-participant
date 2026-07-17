# Lab 50: Capstone Frontend and Persistence — Northstar CRM UI→PostgreSQL Journey — macOS

**OS:** macOS  
**Primary IDE:** IntelliJ IDEA Community Edition  
**Optional IDE:** VS Code  
**Shell:** macOS Terminal (zsh)  
**Stack hint:** Capstone: full stack from prior weeks · IntelliJ primary  
**Full lab steps:** [LAB-50-GUIDE.md](LAB-50-GUIDE.md)  
**Other OS:** [Windows guide](LAB-50-WINDOWS.md) · [IDE conventions](../../../Week%201%20-%20Java%20and%20JVM%20Foundations/_IDE-CONVENTIONS.md)

## Prerequisites (macOS)

- [Lab 0 (macOS)](../../../Week%201%20-%20Java%20and%20JVM%20Foundations/module-00/lab0/LAB-0-MACOS.md) complete (JDK 21, Maven when needed, Git)
- IntelliJ IDEA Community with **Project SDK 21**
- Optional: VS Code + Extension Pack for Java
- Node.js **22** LTS + npm (`node -v`, `npm -v`)
- Instructor shared Kafka / PostgreSQL credentials when this lab needs them ([FINAL-SETUP](../../../FINAL-SETUP-README.md))
- Docker Desktop (or Engine) when the lab builds images
- `kubectl` + your `studentNN.yaml` kubeconfig

## Open this lab in IntelliJ (primary)

1. Start **IntelliJ IDEA Community**.
2. **File → Open…** → `~/java-bootcamp` (Lab 0 workspace root — same folder every lab).  
   If `examples/customer-management-platform` does not exist yet, create it as the lab GUIDE describes; keep the workspace open at `~/java-bootcamp`.
3. Trust the project if prompted.
4. **File → Project Structure → Project** → SDK = **21**, language level **21**.
5. For React labs, open or focus the `crm-ui` / `frontend` folder when editing the SPA.
6. **View → Tool Windows → Terminal** → `cd ~/java-bootcamp` then `cd examples/customer-management-platform/frontend` when ready.

## Optional: VS Code

1. **File → Open Folder…** → `~/java-bootcamp` (same Lab 0 workspace).
2. Confirm **Extension Pack for Java** (and Maven for Java when needed) are installed.
3. **Terminal → New Terminal** → `cd examples/customer-management-platform/frontend` for this lab’s commands.

## Paths (macOS)

| Item | macOS |
| ---- | ----- |
| Workspace (open in IDE) | `~/java-bootcamp` |
| This lab project | `~/java-bootcamp/examples/customer-management-platform` |
| Evidence / screenshots | `~/java-bootcamp/notes/screenshots/lab-50` |
| Shell | zsh / bash inside IntelliJ |
| Path style | Forward slashes; case-sensitive |

```bash
cd ~/java-bootcamp
# Lab 0 layout: evidence at workspace root; code under examples/
mkdir -p notes/screenshots/lab-50
cd examples/customer-management-platform/frontend
```

### Commands this lab typically uses

```text
npm run build
```

## Run configurations (IntelliJ)

1. Open the SPA under `examples/customer-management-platform/crm-ui` (or `frontend` for Lab 50).
2. Use the IntelliJ terminal: `npm install` then `npm run dev` (or as the GUIDE says).
3. **Run → Edit Configurations…** → set **Working directory** to that SPA folder when needed.
4. If this lab also has a Spring `pom.xml`, import Maven from that module separately.

## Do the lab

Complete **every step** in **[LAB-50-GUIDE.md](LAB-50-GUIDE.md)**.  
Wherever that guide shows `~/java-bootcamp`, on macOS use `~/java-bootcamp`. Prefer IntelliJ for Java editing and runs; use VS Code only if you already prefer it.

## Evidence / screenshots

Save screenshots under `~/java-bootcamp/notes/screenshots/lab-50` (Lab 0 workspace layout). Capture IntelliJ (project tree + Run/Terminal) on macOS. Redact passwords, tokens, and kubeconfig contents.

## Pass criteria

_Mark each row **Pass** or **Fail** in your lab notes (GitHub markdown files are not interactive checklists)._

| # | Confirm | Your notes |
| - | ------- | ---------- |
| 1 | Workspace `~/java-bootcamp` open in IntelliJ with SDK **21** | Pass / Fail |
| 2 | Lab project under `examples/customer-management-platform` as in [LAB-50-GUIDE.md](LAB-50-GUIDE.md) | Pass / Fail |
| 3 | Lab pass criteria / deliverables in the GUIDE are complete | Pass / Fail |
| 4 | Commands above succeed in the IntelliJ terminal (or as the lab specifies) | Pass / Fail |
| 5 | Screenshots (if required) saved under `notes/screenshots/lab-50/` | Pass / Fail |
