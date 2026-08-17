# Lab 36 prep checklist

## Earlier exercise files present?

| File | Present? (yes/no) |
| ---- | ----------------- |
| notes/lab36-security.md | yes |
| notes/lab36-token-storage.md | yes |
| notes/lab36-xss-csp.md | yes |
| notes/lab36-csrf-notes.md | yes |
| notes/lab36-todos.md | yes |

kept under notes/Week 4/Module 36/ with the rest of the module notes.

## Decision log

| Decision | Value |
| --- | --- |
| storage choice | in-memory variable, nothing in localStorage or sessionStorage |
| guard redirect target | `/login` with `replace`, internal paths only |
| xss rule | render api text as plain children, no `dangerouslySetInnerHTML` or other html sinks |

## Fixtures (verify)

| ID | Name | Status |
| -- | ---- | ------ |
| CUS-1001 | Amina Khan | ACTIVE |
| CUS-1002 | Ravi Singh | PROSPECT |

## Runtime

lab 35 UI at examples/lab35-crm/crm-ui builds and tests green, and its
http.request already sets headers explicitly and carries
X-Correlation-Id: lab-request-001, which is where the origin-scoped
Authorization header goes in the lab.

## Evidence preview

an unauthenticated visit to a guarded route redirects to /login, the xss
test string renders as literal text with no img node, and the Application
tab shows no token in local or session storage. evidence lands under
notes/screenshots/lab-36/ with the Authorization header redacted.

## No real IdP

this pre-lab needs no real identity provider, no Okta or Auth0 tenant, the
login is a course demo credential against the lab api.

## Scope statement

Pre-lab only — prepare for lab; do not complete full Lab 36 now.

## Self mark

Overall prep: Pass
If Fail, revisit exercise(s): n/a
