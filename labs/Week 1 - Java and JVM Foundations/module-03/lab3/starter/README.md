# Lab 3 starter — 45-minute timed path

Fill every `// TODO`. Do **not** open `../solution/` first. `Main`, `Printable`, `Customer`, and `Transaction` shells are given.

## Target copy path

`~/java-bootcamp/examples/Lab3-BankingSystem/`

### Windows PowerShell

```powershell
$src = "<path-to-course-repo>\labs\Week 1 - Java and JVM Foundations\module-03\lab3\starter\Lab3-BankingSystem"
$dst = "$env:USERPROFILE\java-bootcamp\examples\Lab3-BankingSystem"
New-Item -ItemType Directory -Force -Path $dst | Out-Null
Copy-Item "$src\*" $dst -Recurse -Force
cd $dst
```

### macOS / bash

```bash
SRC="<path-to-course-repo>/labs/Week 1 - Java and JVM Foundations/module-03/lab3/starter/Lab3-BankingSystem"
DST="$HOME/java-bootcamp/examples/Lab3-BankingSystem"
mkdir -p "$DST"
cp -R "$SRC"/. "$DST"/
cd "$DST"
```

## 45-minute checklist (ordered TODOs)

1. Implement `Account.deposit` and `Account.withdraw`.
2. Implement `SavingsAccount.calculateInterest` and `CurrentAccount.calculateCharges`.
3. Implement `BankService.createCustomer`, `createSavingsAccount`, `createCurrentAccount`.
4. Implement `deposit`, `withdraw`, `displayAccounts`.
5. Run smoke test; evidence under `notes/screenshots/lab-3/`.

## Smoke test

```powershell
javac -d out `
  src\com\academy\bank\Printable.java `
  src\com\academy\bank\Customer.java `
  src\com\academy\bank\Transaction.java `
  src\com\academy\bank\Account.java `
  src\com\academy\bank\SavingsAccount.java `
  src\com\academy\bank\CurrentAccount.java `
  src\com\academy\bank\BankService.java `
  src\com\academy\bank\Main.java
java -cp out com.academy.bank.Main
```

Interactive path: create customer `C101` → savings account balance `10000` rate `5` → deposit `2000` → withdraw `3000` → display accounts → exit.

**Expected output snippet:**

```text
Customer Created Successfully.
Savings Account Created.
...
Balance : 9000
Interest : 450
Thank You
```

## Timed-path Pass criteria

| # | Criterion | Pass / Fail |
| - | --------- | ----------- |
| 1 | Project compiles | |
| 2 | Create / deposit / withdraw / display work | |
| 3 | Evidence under `notes/screenshots/lab-3/` | |

> Full GUIDE steps (bonus transfer/history/reports) remain for homework / extended work.
