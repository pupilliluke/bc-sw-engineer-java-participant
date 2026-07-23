# Lab 7 starter — 45-minute timed path

Exception classes, `Account`, `Transaction`, `LoggerUtil`, and `Main` are mostly given. Fill `ATMService.login` / `deposit` / `withdraw`. Do **not** open `../solution/` first.

## Target copy path

`~/java-bootcamp/examples/Lab7-ATMSystem/`

### Windows PowerShell

```powershell
$src = "<path-to-course-repo>\labs\Week 1 - Java and JVM Foundations\module-07\lab7\starter\Lab7-ATMSystem"
$dst = "$env:USERPROFILE\java-bootcamp\examples\Lab7-ATMSystem"
New-Item -ItemType Directory -Force -Path $dst | Out-Null
Copy-Item "$src\*" $dst -Recurse -Force
New-Item -ItemType Directory -Force -Path "$dst\logs" | Out-Null
cd $dst
```

### macOS / bash

```bash
SRC="<path-to-course-repo>/labs/Week 1 - Java and JVM Foundations/module-07/lab7/starter/Lab7-ATMSystem"
DST="$HOME/java-bootcamp/examples/Lab7-ATMSystem"
mkdir -p "$DST" "$DST/logs"
cp -R "$SRC"/. "$DST"/
cd "$DST"
```

## 45-minute checklist (ordered TODOs)

1. Skim exception types + `Account.deposit` / `withdraw`.
2. Implement `ATMService.login` (success + invalid PIN / missing account).
3. Implement `deposit` via `executeTransaction`.
4. Implement `withdraw` via `executeTransaction` (insufficient funds path).
5. Smoke test from **project root**; evidence under `notes/screenshots/lab-7/`.

## Smoke test

```powershell
javac -d out `
  src\com\academy\atm\AccountNotFoundException.java `
  src\com\academy\atm\InvalidPinException.java `
  src\com\academy\atm\InvalidAmountException.java `
  src\com\academy\atm\InsufficientFundsException.java `
  src\com\academy\atm\Account.java `
  src\com\academy\atm\Transaction.java `
  src\com\academy\atm\LoggerUtil.java `
  src\com\academy\atm\ATMService.java `
  src\com\academy\atm\Main.java
java -cp out com.academy.atm.Main
```

Interactive path: login `1001` / `1234` → withdraw `20000` → deposit `1000` → balance → exit.

**Expected output snippet:**

```text
Login Successful
...
Insufficient Balance
Transaction Cancelled
...
Deposit Successful
Current Balance : 12000
Thank You
```

## Timed-path Pass criteria

| # | Criterion | Pass / Fail |
| - | --------- | ----------- |
| 1 | Project compiles; run from Lab7 root | |
| 2 | Login + withdraw fail + deposit success | |
| 3 | Evidence under `notes/screenshots/lab-7/` | |

> Full GUIDE steps (transfer, reports, unchecked demos) remain for homework / extended work.
