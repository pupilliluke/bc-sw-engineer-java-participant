Lab 33 React components (reflection questions, checkpoints)

built under examples\lab33-crm\crm-ui from the lab 33 starter. the starter
ships the Vite React-TS scaffold, the Vitest wiring, the types file and stub
components, so the work was the seed fixtures, StatusBadge, CustomerCard,
CustomerList with EmptyState, the controlled CustomerForm, AppLayout and
CustomerToolbar, the LoadingState and ErrorState shells and the RTL tests.
4 tests green on two consecutive runs, npm run build succeeds. no backend and
no browser needed for the suite, Vitest runs on jsdom.

one environment note. the GUIDE hard gate names Node 22+ and this machine is
on Node 20.18.0. everything in the lab runs, Vite 5 requires 18+, recorded in
the evidence rather than hidden.


REFLECTION QUESTIONS

1. Which design decision most affected correctness?

the closed CustomerStatus union, and the select is where it earned its keep.
the select's onChange needs e.target.value as CustomerStatus because the DOM
types say string, and behind that cast an option with value SUSPENDED
compiled cleanly while feeding a value outside the union into the draft. the
cast is a promise the compiler cannot check, so the options list is the only
thing keeping it honest, and one of them was wrong until review caught it.

2. What evidence proves the implementation works?

the RTL tests query what a user can perceive, names by text, buttons by role
and accessible name, inputs by label, so green tests mean the visible
contract holds rather than that some class name exists. 4 tests green twice
in 01-npm-test.txt, and experiment 3 shows the label test failing on a one
character htmlFor mismatch, so the queries do catch the regression they claim
to catch.

3. Which failure was hardest to diagnose?

the AppLayout draft that returned <AppLayout> from inside itself and recursed
with no base case. the confusion behind it was
which component owns the frame and which supplies the children, App renders
AppLayout and the nesting only goes one direction. the same session also hit
export default against a braced named import on EmptyState, where the error
says no exported member even though the export exists as a default.


CHECKPOINTS

| Checkpoint | Confirm | Result |
| --- | --- | --- |
| A1 | lab33-crm/crm-ui under examples/ | Pass, copied from starter/ |
| A2 | Vite React-TS, Vitest, Testing Library installed | Pass, Vite 5.4.21, Vitest 2.1.9, RTL 16.3.2 |
| A3 | npm run build succeeds | Pass, tsc -b then vite build, dist/ written |
| B1 | Customer, CustomerStatus, CustomerDraft types | Pass, starter file, union PROSPECT ACTIVE CLOSED |
| B2 | StatusBadge, CustomerCard, CustomerList stable keys, empty state | Pass, key=customerId, [] renders No customers yet |
| B3 | CustomerForm with labels | Pass, htmlFor/id on all three fields, controlled by draft/onChange. alert errors are the guide's fuller prop set, the starter form has no errors prop |
| B4 | fixtures Amina CUS-1001 and Ravi CUS-1002 | Pass, seedCustomers matches the scenario table |
| C1 | dashboard composed with layout, toolbar, form | Pass, AppLayout owns the one main, stubs log lab-request-001 |
| C2 | loading and error shells exist | Pass, role status and role alert, optional Retry prop, PNGs in notes/screenshots/lab-33/ |
| C3 | RTL tests query by role, Edit reports CUS-1001 | Pass, within(card-CUS-1001) scopes the button, onEdit called with CUS-1001 |
| C4 | two consecutive test runs green | Pass, 4 and 4, 01-npm-test.txt |
| D1 | README documents dev, test, build | Pass, examples/lab33-crm/README.md |
| D2 | no secrets, node_modules or dist committed | Pass, both ignored, fixtures are fictional |
| D3 | component notes cover keys, a11y, lab 34 handoff | Pass, crm-ui/docs/component-notes.md |
