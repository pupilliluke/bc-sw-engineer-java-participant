Lab 35 React + Spring integration (reflection questions, checkpoints)

built as examples\lab35-crm, crm-ui carried forward from lab34-crm and
crm-api copied from lab25-crm so the lab 35 backend changes stay out of
the lab 25 artifact. the work was the ApiError type, the request() fetch
boundary, customersApi with field mapping, the LoadState machine with
AbortController and Retry, backend 400 fieldErrors onto the form, the
saving guard, and on the Spring side CORS, the exception handler,
validation and PUT. 16 tests green on two consecutive runs, build green,
plus a live pass through the real stack in the browser.

same environment note as labs 33 and 34, the GUIDE hard gate names Node
22+, this machine runs Node 20.18.0, everything works, recorded as found.


REFLECTION QUESTIONS

1. Which design decision most affected correctness?

checking res.ok inside the one request() boundary. fetch resolves happily
on a 404 or 500, it only rejects on network failure, so without that check
error pages would flow into res.json() and surface as parse garbage far
from the cause. putting the check in one place means every caller gets the
same ApiError contract, and the same file is where lab 36 will add the
auth header.

2. What evidence proves the implementation works?

the six response-class tests pin the boundary, 200/201/400/500/network/
abort each produce the right ApiError kind, and the ten flow tests drive
the UI against a mocked server. on top of that the live browser pass
covered what the mocked tests leave out, the OPTIONS preflights answering
200, StrictMode's first GET showing as ERR_ABORTED because the
AbortController cleanup ran, Ravi's PUT persisting server-side across a
page reload, and the evil Origin curl coming back 403 with no ACAO header.

3. Which failure was hardest to diagnose?

the step 1 contract surprises rather than any code failure. the backend
speaks id and name while the UI types say customerId and fullName, and an
unknown id came back 500 instead of 404 because lab25 had no exception
handler, both only visible in the curl output. the fix for the first was
a mapping layer in customers.ts so neither side renamed fields, and for
the second an ApiExceptionHandler so the frontend can branch on status
honestly. the GUIDE's own worked example posts fullName/phone, which
matches neither side, the curl output was the only place the real field
names appeared.


CHECKPOINTS

| Checkpoint | Confirm | Result |
| --- | --- | --- |
| A1 | lab35-crm/crm-ui from Lab 34 | Pass, copied tree, node_modules reinstalled |
| A2 | Spring API reachable, contract documented via curl | Pass, 01-api-contract.txt, crm-api from lab25 |
| A3 | .env.example with VITE_API_BASE_URL, no secrets | Pass, .env gitignored, example committed |
| B1 | ApiError + http helper + customersApi | Pass, kind union, ok/204 guards, field mapping |
| B2 | Abortable list load, distinct UI states | Pass, LoadState union, ERR_ABORTED seen in dev tools |
| B3 | Create/update with correlation header | Pass, header set in request(), asserted in tests |
| B4 | 400 field errors mapped, saving disables duplicate POST | Pass, mapApiFieldErrors, disabled={saving} |
| C1 | Spring CORS allowlist for Vite origin | Pass, WebConfig, exact origin with port |
| C2 | Evil Origin probe recorded | Pass, 403 no ACAO, 03-cors-evidence.txt |
| C3 | Response-class tests green twice, build green | Pass, 16 and 16, 02-npm-test.txt |
| D1 | Integration notes + screenshots | Pass, crm-ui/docs/api-integration-notes.md, evidence txt |
| D2 | No secrets / node_modules / dist / .env committed | Pass, all ignored, fixtures fictional |
| D3 | README runbook starts Spring + Vite | Pass, examples/lab35-crm/README.md |
