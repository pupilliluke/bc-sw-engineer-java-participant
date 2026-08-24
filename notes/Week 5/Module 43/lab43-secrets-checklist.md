# Lab 43 — Actions Secrets Checklist

## Step 1 — Sort

| Item | OK in Git? | Where it belongs |
| --- | --- | --- |
| Workflow YAML | Yes | committed to the repository |
| README | Yes | committed to the repository |
| Registry password | No | GitHub Actions secrets |
| kubeconfig | No | GitHub Actions secrets |
| `.env` | No | never committed; local only, gitignored |
| Scan reports (sanitized) | Yes | committed or uploaded as artifacts |

`.env` is the one that has already been enforced in this repository:
`examples/lab41-crm/.gitignore` carries `.env`, `.env.*` and `!.env.example`, so
`.env` and `.env.local` are ignored while the blank-valued `.env.example` stays
tracked as the key list.

Dependency-Check reports are the qualifier on the last row. The raw HTML and
JSON are gitignored because they are bulky and embed absolute local paths;
sanitized excerpts go in `notes/screenshots/`. Sanitized is not automatic.

## Step 2 — Check the reference

Only non-secret config belongs in Git. Credentials go in Actions secrets or
variables.

Lab 42 already applied the same split at the Kubernetes layer: the ConfigMap
holds `SPRING_PROFILES_ACTIVE`, `CRM_DB_HOST`, `CRM_DB_PORT`, `CRM_DB_NAME` and
`CRM_DB_USER` and is committed; the Secret holds `CRM_DB_PASSWORD` and
`JWT_SECRET` and exists only in the cluster, created with
`kubectl create secret --from-literal`. `secret.example.yaml` is committed with
key names and the placeholder `REPLACE-OUT-OF-BAND`, never a real value.

Actions secrets behave the same way: the workflow references
`${{ secrets.NAME }}`, the name is in Git, the value is not.

## Step 3 — Leak response

1. **Rotate** the credential immediately. The committed value is compromised the
   moment it is pushed, whether or not anyone fetched it. Rotating first means
   the rest of the response is cleanup rather than a race.
2. **Purge history per policy.** Removing the line in a new commit does not
   remove it from history; the blob is still reachable. History rewriting is not
   done unilaterally — Lab 40's rule was to ask the instructor before rewriting
   shared history.
3. **Notify the instructor**, with what leaked, which commits, and when it was
   rotated.

Rotate before purge, in that order. A purge that runs first buys nothing and
delays the only step that actually closes the exposure.

## Step 4 — CRM note

`CUS-1001` Amina Khan and `CUS-1002` Ravi Singh are synthetic fixtures seeded by
`FixtureLoader` with `example.com` addresses. They are not secrets and may
appear in test evidence and uploaded reports.

Real customer dumps are forbidden, in the repository and in a run's artifacts.
A Surefire report from a run against real data is a leak in the same way a
committed password is.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [x] File exists at `notes/lab43-secrets-checklist.md`
- [x] Items classified
- [x] Leak response has three steps
- [x] Fixture vs secret clarified
