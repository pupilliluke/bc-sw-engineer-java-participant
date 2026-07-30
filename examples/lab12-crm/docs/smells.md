# Code smells — Lab 12

Catalogued against the frozen baseline,
`src/main/java/com/northstar/crm/service/CustomerService.before.java.txt`.
Line numbers are that file. Ten smells; the guide asks for at least eight.

Every impact column below is observed, not predicted — the before harness in
`docs/before-after.md` was run against this exact class and its output is pasted
there.

| # | Smell | Location | Impact on CUS-1001 / CUS-1002 |
| - | ----- | -------- | ------------------ |
| 1 | Poor naming (`doStuff`, `data`, `a`–`e`) | 13, 15 | Five `String` parameters named `a` to `e`. `c` is email and `d` is phone, both `String` and adjacent, so a call site that swaps them compiles, runs, and files Ravi's phone number as his email address. Nothing in the signature tells a reviewer which is which — only the comment on line 16, which the compiler does not check. |
| 2 | Raw types | 13, 22, 44, 57 | `List data = new ArrayList()` holds `Object`, so every read casts (`(Customer) data.get(i)`). Put anything else in the list and the failure is a `ClassCastException` at read time, far from the line that inserted it. |
| 3 | Long method / mixed responsibilities | 15–53 | 39 lines doing five jobs: validate, scan for duplicate, map status, build and store, then conditionally update. Changing Amina's duplicate rule means editing the method that also decides Ravi's status. |
| 4 | Stringly-typed status | 33–37, 46–47 | Status arrives as a `String` matched by an else-if chain. `"AKTIVE"` misses every branch and the final `else` silently assigns `PROSPECT`. Observed: creating CUS-1003 with `"AKTIVE"` returned a customer with status `PROSPECT` and printed `ok`. A typo becomes valid-looking data with no error anywhere. |
| 5 | Incorrect equality (`==`) | 58 | `x.getCustomerId() == id` compares references. Observed: `get("CUS-1001")` returns Amina (string literals are interned) but `get(new String("CUS-1001"))` returns `null` for the same customer. Any ID arriving from a file, socket, or request — i.e. every real caller — misses. This is the support ticket in the lab's business scenario. |
| 6 | Second `==` on strings, unflagged | 17 | `a == ""` in the blank check, the same reference-comparison bug. The baseline comments the one on line 58 and not this one. A caller passing a runtime-built empty string gets past validation instead of being rejected. |
| 7 | Null as control flow | 19, 25, 62 | Three different outcomes — blank input, duplicate ID, not found — all return `null`. Observed: `get(new String("CUS-1001"))` and `get("CUS-9999")` both print `null`, so a caller cannot tell "no such customer" from "lookup is broken". Support has no way to distinguish them from the outside. |
| 8 | Side-effect logging | 18, 24, 40, 48 | `System.out.println("bad")`, `"dup"`, `"ok " + a`, `"upd"`. Untestable without capturing stdout, and `"bad"` names neither the field nor the customer — a blank ID and a blank name print the identical line. No correlation ID anywhere. |
| 9 | Magic `"UPDATE"` behaviour | 42 | `b.contains("UPDATE")` turns a create into an update, keyed off the customer's **name**. A customer legitimately named something containing "UPDATE" silently takes a different code path. Undiscoverable from the signature and impossible to document honestly. |
| 10 | Dead second scan | 43–50 | The update branch re-scans the list for the ID that was appended eleven lines earlier at 39, then mutates it. The record is already in hand as `x`; the loop cannot find anything else. Pure cost, and it hides the fact that create and update are two operations. |

## Priority

From the pre-lab smell bingo (`notes/Week 2/Module 12/lab12-smell-bingo.md`),
the two starred to fix first were **5** (`==` on IDs) and **4** (stringly-typed
status) — the two producing wrong answers rather than merely inviting them.
Both are fixed in Steps 4–5; the mapping to concrete fixes is in
`before-after.md`.
