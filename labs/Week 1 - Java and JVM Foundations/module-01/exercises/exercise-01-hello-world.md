# Exercise — Hello World

**Module 1** · Pre-lab practice · then open [`../../lab1/LAB-1-GUIDE.md`](../lab1/LAB-1-GUIDE.md)

## Goal

Write, compile, and run a minimal program that prints `Hello, JVM!`.

## Starter / reference

```java
public class Hello {
    public static void main(String[] args) {
        System.out.println("Hello, JVM!");
    }
}
```

## Do this

- Save as `Hello.java`
- `javac Hello.java`
- `java Hello`
- Optional: `javap -c Hello`

## Expected result

Console prints `Hello, JVM!`; `Hello.class` exists.

## Pass criteria

_Mark each row **Pass** or **Fail** in your lab notes (GitHub markdown files are not interactive checklists)._

| # | Confirm | Your notes |
| - | ------- | ---------- |
| 1 | Code compiles and runs (or notes complete if analysis-only) | Pass / Fail |
| 2 | You can explain the result in one sentence | Pass / Fail |
