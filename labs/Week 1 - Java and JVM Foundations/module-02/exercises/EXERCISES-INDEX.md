# Module 2 — Pre-Lab Exercises

> **Start here for Module 2:** [`../README.md`](../README.md) · **Which file when?** [`../../../_PARTICIPANT-FILE-GUIDE.md`](../../../_PARTICIPANT-FILE-GUIDE.md)

**Module:** 2 — Java Syntax and Core Constructs  
**Source:** Module 2 slides: Lab Overview / Lab Tasks (Java Syntax and I/O)  
**Next (after these exercises):** OS how-to → [`../lab2/LAB-2-WINDOWS.md`](../lab2/LAB-2-WINDOWS.md) or [`../lab2/LAB-2-MACOS.md`](../lab2/LAB-2-MACOS.md) → then [`../lab2/LAB-2-GUIDE.md`](../lab2/LAB-2-GUIDE.md)

> **When:** Complete these exercises **after the module slides** and **before** the full lab.  
> **JDK:** 21 · **IDE:** IntelliJ Community (primary) or VS Code (optional).  
> Keep practice sources separate from the graded lab.

## Already covered — do not redo

| Topic | Where you did it |
| ----- | ---------------- |
| `Hello` / `main` / `System.out.println` | Lab 0 · Module 1 Exercise 1 |
| Hard-coded variables and types | Module 1 Exercise 2 |
| Methods, control flow, classes, `javap`, WORA | Module 1 Exercises 3–7 |

Module 2 practice is **new**: console input with `Scanner`, typed reads (`nextLine` / `nextInt` / `nextDouble`), leftover-newline pitfalls, `Math.PI`, and `printf` formatting — the skills Lab 2 needs for the student menu app.

## Workspace

| Item | Windows | macOS |
| ---- | ------- | ----- |
| Lab 0 workspace (open in IntelliJ) | `%USERPROFILE%\java-bootcamp` | `~/java-bootcamp` |
| Pre-lab exercises folder | `%USERPROFILE%\java-bootcamp\examples\module-02-exercises` | `~/java-bootcamp/examples/module-02-exercises` |
| Shell | IntelliJ **Terminal** (PowerShell) | IntelliJ **Terminal** (zsh) |

### Setup — create the exercises folder (do once)

**Windows (PowerShell):**

```powershell
cd $env:USERPROFILE\java-bootcamp
New-Item -ItemType Directory -Force -Path examples\module-02-exercises | Out-Null
cd examples\module-02-exercises
pwd
```

**macOS (zsh/bash):**

```bash
cd ~/java-bootcamp
mkdir -p examples/module-02-exercises
cd examples/module-02-exercises
pwd
```

## Exercise index

| # | Exercise | New skill | File |
| - | -------- | --------- | ---- |
| 1 | Personal Details | `Scanner` + `nextLine` / `nextInt` | [`exercise-01-personal-details.md`](exercise-01-personal-details.md) |
| 2 | Product Information | Mix `String` / `int` / `double` input | [`exercise-02-product-info.md`](exercise-02-product-info.md) |
| 3 | Calculations | Read two numbers; arithmetic + labels | [`exercise-03-calculator.md`](exercise-03-calculator.md) |
| 4 | Area of Circle | `Math.PI` + `printf` decimals | [`exercise-04-circle-area.md`](exercise-04-circle-area.md) |
| 5 | Bill Summary (challenge) | Money math + `%.2f` | [`exercise-05-bill-summary.md`](exercise-05-bill-summary.md) |
| 6 | Personal Profile (bonus) | Aligned `printf` table layout | [`exercise-06-profile-bonus.md`](exercise-06-profile-bonus.md) |

Work in order. Keep practice sources separate from the graded lab submission.
