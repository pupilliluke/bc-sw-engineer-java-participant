# Lab 4 starter — 45-minute timed path

Fill measurement / comparison `// TODO`s. Do **not** open `../solution/` first. `Person`, `MemoryMonitor`, `StackExample`, `HeapExample`, and `ObjectLifecycle` are mostly given.

## Target copy path

`~/java-bootcamp/examples/Lab4-MemoryManagement/`

### Windows PowerShell

```powershell
$src = "<path-to-course-repo>\labs\Week 1 - Java and JVM Foundations\module-04\lab4\starter\Lab4-MemoryManagement"
$dst = "$env:USERPROFILE\java-bootcamp\examples\Lab4-MemoryManagement"
New-Item -ItemType Directory -Force -Path $dst | Out-Null
Copy-Item "$src\*" $dst -Recurse -Force
cd $dst
```

### macOS / bash

```bash
SRC="<path-to-course-repo>/labs/Week 1 - Java and JVM Foundations/module-04/lab4/starter/Lab4-MemoryManagement"
DST="$HOME/java-bootcamp/examples/Lab4-MemoryManagement"
mkdir -p "$DST"
cp -R "$SRC"/. "$DST"/
cd "$DST"
```

## 45-minute checklist (ordered TODOs)

1. Run given demos: `StackExample`, `HeapExample`, `ObjectLifecycle`.
2. Complete `GarbageCollectionDemo` allocation + GC measurement.
3. Complete `MemoryLeakDemo` `leak` and `fix` modes.
4. Complete `PerformanceTest.runAllocationTest` (core timed path).
5. Optional if time: `WeakReferenceDemo`, `ListMemoryComparison`.
6. Evidence under `notes/screenshots/lab-4/`.

## Smoke test

```powershell
javac Person.java MemoryMonitor.java StackExample.java HeapExample.java ObjectLifecycle.java GarbageCollectionDemo.java MemoryLeakDemo.java PerformanceTest.java
java StackExample
java GarbageCollectionDemo
java MemoryLeakDemo fix
```

**Expected output snippet:**

```text
===== Stack Memory Demonstration =====
...
===== Garbage Collection Demonstration =====
Objects Created : 100000
Garbage Collection Completed
...
===== Memory Leak Fix Demonstration =====
...
Used Memory drops after GC (After GC report)
```

## Timed-path Pass criteria

| # | Criterion | Pass / Fail |
| - | --------- | ----------- |
| 1 | Core demos compile and run | |
| 2 | GC + leak-fix measurement TODOs complete | |
| 3 | Evidence under `notes/screenshots/lab-4/` | |

> Full GUIDE steps (jvisualvm, OOM, string comparison) remain for homework / extended work.
