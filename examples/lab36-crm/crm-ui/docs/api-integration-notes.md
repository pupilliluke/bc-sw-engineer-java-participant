# Lab 35 — API integration notes

## One boundary

all HTTP goes through src/api/http.ts. request() owns the base URL from
VITE_API_BASE_URL, the Content-Type and X-Correlation-Id headers, the
res.ok check, the 204 guard and the translation of every failure into
ApiError. components never call fetch. lab 36 adds the Authorization
header in this one file.

## ApiError

kind is a closed union, network | http | abort | parse. fetch only rejects
on network trouble, an HTTP 404 or 500 resolves normally, so res.ok is
checked at the boundary and non-OK responses become ApiError via
ApiError.from, which reads message and fieldErrors from the JSON body and
never dumps HTML. AbortError converts to kind abort so the UI can ignore
it instead of showing an error for a request the app itself cancelled.

## Field mapping

the API speaks id and name, the UI types from lab 34 use customerId and
fullName. the mapping lives in src/api/customers.ts, fromApi/toApi for
records and mapApiFieldErrors for 400 bodies, so neither side renamed its
fields and the components from lab 34 are untouched.

## Request states

App holds LoadState, loading | data | error, one discriminated value like
UiMode. loading renders role=status, error renders role=alert with Retry
(bumps reloadKey to re-run the effect), data with zero rows renders "No
customers yet", a search miss over loaded rows renders "No customers
found". filtering stays a render-time computation.

## Abort

the load effect creates an AbortController and cleanup aborts it. cleanup
runs on unmount and before re-runs, StrictMode's double mount shows the
first GET as ERR_ABORTED in the network tab, which is the cleanup working.

## Saving guard

saving flips true before the await and false in finally, Save is disabled
while true, so a double click sends one POST. if one ever gets through,
the server rejects the duplicate id with a 409.

## Backend changes (crm-api)

crm-api is a copy of lab25-crm, the original is untouched. added for this
lab: WebConfig CORS allowlist for http://localhost:5173,
ApiExceptionHandler mapping not-found to 404 {message}, duplicate and
illegal transition to 409, FieldValidationException to 400 {message,
fieldErrors}, server-side validation of name/email/status, and PUT
/api/customers/{id} for full updates. before the handler existed an
unknown id surfaced as 500 with Spring's default error body, captured in
01-api-contract.txt.
