Lab 34 React state and events (reflection questions, checkpoints)

built under examples\lab34-crm\crm-ui from the lab 34 starter. the starter
ships the scaffold, Vitest wiring, types with the UiMode union, seed
fixtures, the form stub and an empty validateCustomerDraft, so the work was
the query state and controlled search, the derived visible list, the Edit
buttons that enter edit mode and seed the draft, handleFieldChange with
per-field error clearing, the validation rules, the create/edit/cancel
handlers, the title effect and the seven RTL flow tests. 7 tests green on
two consecutive runs, npm run build succeeds.

same environment note as lab 33. the GUIDE hard gate names Node 22+ and this
machine is on Node 20.18.0, everything runs, recorded in the evidence.


REFLECTION QUESTIONS

1. Which design decision most affected correctness?

the functional spread in the create updater, and failure experiment 1 is why.
with push the whole test suite stayed green because the other setState calls
in handleSubmit forced a re-render over the mutated array, the duplicate row
only appeared in the browser where StrictMode invokes the updater twice. the
immutability rule is not style, it is the thing StrictMode checks in dev
that the tests cannot see.

2. What evidence proves the implementation works?

seven flow tests that drive the app the way a user does, click New customer,
type into labeled fields, submit, and they passed identically on two runs.
the browser pass on top of that showed the parts jsdom does not, the title
moving CRM (2) to (3) to (1) with the visible count, typing in Full name
clearing only that field's alert while the email alert stayed, and Ravi
reopening as ACTIVE after the edit saved. 01-npm-test.txt has both runs.

3. Which failure was hardest to diagnose?

the blank page after pasting the GUIDE's step 6 snippet at the bottom of
customerValidation.ts. the snippet is the inside of a submit handler that
the starter had already written in handleSubmit under different names, at
file top level its bare return is a syntax error, Vite returned a 500 for
the module and the page rendered nothing with only a generic failed-to-load
line in the console. the network tab pointing at the one 500 module found
it. same class of mistake happened twice more in the session, the field
change handler pasted into the Edit onClick and into the search onChange,
the fix each time was asking which function a snippet is the body of.


CHECKPOINTS

| Checkpoint | Confirm | Result |
| --- | --- | --- |
| A1 | lab34-crm/crm-ui copied and builds | Pass, from module-34/lab34/starter |
| A2 | customers, query, mode, draft, errors lifted into App | Pass, App is the only owner |
| A3 | validation module present | Pass, src/validation/customerValidation.ts, rules are the lab work |
| B1 | controlled search + derived visible | Pass, aria-label Search customers, filter runs in render |
| B2 | discriminated mode union list/create/edit | Pass, starter UiMode type, edit variant carries customerId |
| B3 | immutable create and update, cancel preserves list | Pass, spread append with randomUUID, map by customerId keeps the original id |
| B4 | field validation blocks bad saves | Pass, blank name and bad email return before setCustomers |
| C1 | useEffect title sync with cleanup | Pass, CRM (visible.length), cleanup restores the original title |
| C2 | no derived-state filter effects | Pass, experiment 2 shows why, the run hangs |
| C3 | 6+ RTL flow tests green twice | Pass, 7 and 7, 01-npm-test.txt |
| C4 | npm run build succeeds | Pass, tsc -b exits 0, dist/ written |
| D1 | state notes document anti-patterns | Pass, crm-ui/docs/state-notes.md |
| D2 | correlation logged as lab-request-001 | Pass, create, update and cancel log it |
| D3 | no secrets, node_modules or dist committed | Pass, both ignored, fixtures are fictional |
