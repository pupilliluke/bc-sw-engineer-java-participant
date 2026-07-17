# Exercise — Inspect Bytecode

**Module 1** · Pre-lab practice · then open [`../../lab1/LAB-1-GUIDE.md`](../lab1/LAB-1-GUIDE.md)  
**Folder:** `examples/module-01-exercises/` ([setup](EXERCISES-INDEX.md))

## Goal

Disassemble `Person` (or `Hello`) with `javap` and note three bytecode instructions.

## What each command piece means

| Piece | Easy meaning |
| ----- | ------------ |
| `javap` | Read a `.class` file and show its structure |
| `-c` | Include bytecode for each method |
| `-v` | Optional advanced detail; skip for this beginner exercise |
| `Person` | Class name to inspect (must already be compiled) |

## Do this

**Why:** Connect your Java source to the instructions the JVM actually runs.

From the exercises folder (after `javac` produced the `.class`):

**Windows:**

```powershell
cd $env:USERPROFILE\java-bootcamp\examples\module-01-exercises
javap -c Person
```

**macOS:**

```bash
cd ~/java-bootcamp/examples/module-01-exercises
javap -c Person
```

Use `-c` first because it shows only the useful method instructions. Your output is split into three readable sections:

| Section | What your Java did |
| ------- | ------------------ |
| `Person(String, int)` | Constructor saved `name` and `age` |
| `display()` | Read the fields, joined the text, and printed it |
| `main(String[])` | Created `new Person("Aman", 21)` and called `display()` |

### Read only `main` first

Your `main` bytecode tells this simple story:

| Bytecode | Plain English |
| -------- | ------------- |
| `new Person` | Make space for a new Person |
| `ldc "Aman"` | Use the text `Aman` |
| `bipush 21` | Use the number `21` |
| `invokespecial Person."<init>"` | Run the constructor |
| `astore_1` | Save the new Person in variable `person` |
| `aload_1` | Pick up `person` again |
| `invokevirtual display` | Call `person.display()` |
| `return` | Finish `main` |

**One sentence:** The JVM creates a Person with `Aman` and `21`, saves it, calls `display`, and finishes.

You do **not** need to understand the constant pool or descriptors yet.

**Optional advanced output:** `javap -c -v Person` also shows the constant pool, version, flags, descriptors, checksums, and bootstrap methods. That is why your output was very long.

- Save text or a local screenshot under `notes/screenshots/` (keep screenshots on your laptop only)
- Explain three of: `new`, `ldc`, `invokevirtual`, `aload`, `return`

### Quick opcode meanings (plain words)

**Picture the JVM putting items on a table, then acting on them.**

| Opcode | Everyday meaning |
| ------ | ---------------- |
| `ldc` | Put a value (text or number) on the table |
| `getstatic` | Pick up a shared tool like the printer (`System.out`) |
| `invokevirtual` | Do an action with what's on the table (e.g. print) |
| `aload` / `aload_0` | Put an object you already have on the table |
| `new` | Create a new object |
| `return` | Done — step away |

You do **not** need to memorize these. The point: `javac` turns your Java into small steps, and the **JVM runs the steps**, not your `.java` file. Full walkthrough: [Exercise 1 Step 4](exercise-01-hello-world.md).


## Expected result

You can name what three instructions do from your listing.

## Pass criteria

_Mark each row **Pass** or **Fail** in your lab notes (GitHub markdown files are not interactive checklists)._

| # | Confirm | Your notes |
| - | ------- | ---------- |
| 1 | Code compiles and runs (or notes complete if analysis-only) | Pass / Fail |
| 2 | You can explain the result in one sentence | Pass / Fail |
