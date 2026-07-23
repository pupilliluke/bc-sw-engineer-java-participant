# Lab 2 starter — 45-minute timed path

Fill every `// TODO`. Do **not** open `../solution/` first. `Student.java` is mostly complete.

## Target copy path

`~/java-bootcamp/examples/Lab2-JavaSyntax/`

### Windows PowerShell

```powershell
$src = "<path-to-course-repo>\labs\Week 1 - Java and JVM Foundations\module-02\lab2\starter\Lab2-JavaSyntax"
$dst = "$env:USERPROFILE\java-bootcamp\examples\Lab2-JavaSyntax"
New-Item -ItemType Directory -Force -Path $dst | Out-Null
Copy-Item "$src\*" $dst -Recurse -Force
cd $dst
```

### macOS / bash

```bash
SRC="<path-to-course-repo>/labs/Week 1 - Java and JVM Foundations/module-02/lab2/starter/Lab2-JavaSyntax"
DST="$HOME/java-bootcamp/examples/Lab2-JavaSyntax"
mkdir -p "$DST"
cp -R "$SRC"/. "$DST"/
cd "$DST"
```

## 45-minute checklist (ordered TODOs)

1. Skim `Student.java` (fields, getters, `display`, pass/fail).
2. Implement `StudentManager.addStudent`.
3. Implement `displayStudents`, `searchStudent`, `calculateAverage`.
4. Confirm `Main` menu cases 1–5 work (bonus 6–10 optional).
5. Compile, run smoke test, capture evidence under `notes/screenshots/lab-2/`.

## Smoke test

```powershell
javac -d out src\com\academy\student\Student.java src\com\academy\student\StudentManager.java src\com\academy\student\Main.java
java -cp out com.academy.student.Main
```

Interactive path: menu → add student `101` / `John` / `Java` / `91` → display → search `101` → average → exit `5`.

**Expected output snippet:**

```text
Student Added Successfully.
...
Average Marks : 91.00
Thank You
```

## Timed-path Pass criteria

| # | Criterion | Pass / Fail |
| - | --------- | ----------- |
| 1 | Project compiles | |
| 2 | Add / display / search / average / exit work | |
| 3 | Evidence under `notes/screenshots/lab-2/` | |

> Full GUIDE steps (bonus menus, polish) remain for homework / extended work.
