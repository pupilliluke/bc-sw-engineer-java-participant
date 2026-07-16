# Lab 0: Development Environment Setup — Windows

**OS:** Windows 10 / 11  
**Primary IDE:** IntelliJ IDEA Community Edition  
**Optional IDE:** VS Code (if you already prefer it)  
**Lab folder:** `labs/Week 1 - Java and JVM Foundations/module-00/lab0/`  
**Also see:** [LAB-0-GUIDE.md](LAB-0-GUIDE.md) (overview) · [macOS guide](LAB-0-MACOS.md) · [IDE conventions](../_IDE-CONVENTIONS.md)

> Install tools on **your Windows laptop**. Shared cloud services are not needed until Week 4+.

---

## What you install

| Tool | Target |
| ---- | ------ |
| IntelliJ IDEA Community | Latest stable (primary) |
| VS Code (optional) | Latest stable + Extension Pack for Java |
| Temurin OpenJDK | **21** LTS |
| Maven | **3.9.x** |
| Git | 2.x |

**Verified reference layout:** Temurin 21 at `C:\Program Files\Eclipse Adoptium\jdk-21`; Maven at `C:\Program Files\Apache\maven\current`; workspace `%USERPROFILE%\java-bootcamp`.

---

## Steps

### Step 1 — Install IntelliJ IDEA Community (primary)

1. Download **Community Edition**: [jetbrains.com/idea/download](https://www.jetbrains.com/idea/download/) (Windows).
2. Run the installer. Prefer options that add a desktop/Start menu shortcut.
3. Launch IntelliJ once and finish the first-run wizard.

**Expected:** Welcome screen opens.

### Step 2 — Optional: Install VS Code

Only if you already prefer VS Code:

1. Install from [code.visualstudio.com](https://code.visualstudio.com/).
2. Extensions: **Extension Pack for Java** (`vscjava.vscode-java-pack`).

### Step 3 — Install Temurin JDK 21 and set `JAVA_HOME`

1. Download Temurin 21 **JDK** Windows x64 **MSI**: [Adoptium Temurin 21](https://adoptium.net/temurin/releases/?version=21&os=windows&arch=x64&package=jdk).
2. Run the MSI as Administrator. Typical path: `C:\Program Files\Eclipse Adoptium\jdk-21`.
3. Set **system** environment variables (Win + R → `sysdm.cpl` → Advanced → Environment Variables):
   - `JAVA_HOME` = that JDK folder
   - Prefixed Path entry: `%JAVA_HOME%\bin`
4. Remove other JDKs from Path if present.
5. Open a **new** PowerShell window.

```powershell
java -version
javac -version
echo $env:JAVA_HOME
```

**Expected:** Versions show `21.x.x` (Temurin / Eclipse Adoptium).

### Step 4 — Install Maven 3.9.x and Git

**Git:** install from [git-scm.com](https://git-scm.com/) if `git --version` fails.

**Maven:**

1. Download `apache-maven-3.9.x-bin.zip` from [Maven download](https://maven.apache.org/download.cgi) or [archive.apache.org](https://archive.apache.org/dist/maven/maven-3/).
2. Extract to `C:\Program Files\Apache\maven\apache-maven-3.9.x`.
3. Create junction `C:\Program Files\Apache\maven\current` → that folder (optional but recommended).
4. System env: `MAVEN_HOME` = `C:\Program Files\Apache\maven\current`; add `%MAVEN_HOME%\bin` to Path.
5. New PowerShell:

```powershell
mvn -version
git --version
```

**Expected:** Maven **3.9.x** reporting Java **21**.

### Step 5 — Create `java-bootcamp` workspace

```powershell
$root = Join-Path $env:USERPROFILE 'java-bootcamp'
New-Item -ItemType Directory -Force -Path @(
  "$root\labs",
  "$root\examples",
  "$root\assignments",
  "$root\projects",
  "$root\notes"
) | Out-Null
cd $root
Get-ChildItem
```

**Expected:** `%USERPROFILE%\java-bootcamp` with five subfolders.

### Step 6 — Open workspace in IntelliJ

1. **File → Open…** → `%USERPROFILE%\java-bootcamp`.
2. Trust the project if prompted.
3. **File → Project Structure → Project** → SDK **21**, language level **21**.
4. **View → Tool Windows → Terminal** → confirm `java -version`.

**Optional VS Code:** File → Open Folder… → same path; Terminal → New Terminal.

### Step 7 — HelloWorld sources

```powershell
cd $env:USERPROFILE\java-bootcamp
New-Item -ItemType Directory -Force -Path examples\HelloJava\src, examples\HelloJava\out | Out-Null
```

Create `examples\HelloJava\src\HelloJava.java`:

```java
public class HelloJava {
    public static void main(String[] args) {
        System.out.println("Hello Java Bootcamp!");
    }
}
```

### Step 8 — Run from IntelliJ terminal

```powershell
cd $env:USERPROFILE\java-bootcamp\examples\HelloJava
javac -d out src\HelloJava.java
java -cp out HelloJava
```

**Expected:**

```text
Hello Java Bootcamp!
```

### Step 9 — Run with IntelliJ green arrow

1. Open `HelloJava.java`.
2. Right-click `src` → **Mark Directory as → Sources Root** if needed.
3. Green ▶ beside `main` → **Run ‘HelloJava.main()’**.

### Step 10 — GitHub + git identity

```powershell
git config --global user.name "Your Name"
git config --global user.email "you@example.com"
git config --global --list
```

Create/sign in at [github.com](https://github.com).

---

## Pass criteria (Windows)

```powershell
java -version
javac -version
mvn -version
git --version
echo $env:JAVA_HOME
cd $env:USERPROFILE\java-bootcamp
Get-Location
```

_Mark each row **Pass** or **Fail** in your lab notes (GitHub markdown files are not interactive checklists)._

| # | Confirm | Your notes |
| - | ------- | ---------- |
| 1 | Java / javac 21 (Temurin) | Pass / Fail |
| 2 | Maven 3.9.x on Java 21 | Pass / Fail |
| 3 | Git works; `user.name` / `user.email` set | Pass / Fail |
| 4 | `JAVA_HOME` points at Temurin 21 | Pass / Fail |
| 5 | Workspace `%USERPROFILE%\java-bootcamp` with five folders | Pass / Fail |
| 6 | HelloWorld prints from terminal | Pass / Fail |
| 7 | HelloWorld runs via IntelliJ green arrow | Pass / Fail |
| 8 | (Optional) VS Code opens the same folder | Pass / Fail |

**Do not start Lab 1 until every Pass criteria row is Pass in your notes.**

Next: [Lab 1 Windows guide](../../module-01/lab1/LAB-1-WINDOWS.md) · [Lab 1 full guide](../../module-01/lab1/LAB-1-GUIDE.md)
