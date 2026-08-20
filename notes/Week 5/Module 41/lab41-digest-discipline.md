# Lab 41 — Digest vs Latest

## Step 1 — Define

a **tag** is a mutable label pointing at an image. `crm-api:lab41` today
and `crm-api:lab41` next week can be different bits, because a tag is a
name someone can repoint at any time.

a **digest** is the sha256 of the image manifest, so it names the content
rather than a label. `crm-api@sha256:abc...` is the same bits forever;
push different content and you get a different digest, by construction.

## Step 2 — Check the reference

the Predict prompt asks whether two engineers pulling `:latest` a week
apart get the same bits. no, and nothing warns them. both machines report
`crm-api:latest`, both believe they are running the same thing, and the
one who cannot reproduce a bug has no way to see why from the tag alone.

this is the same failure as an unpinned Maven plugin, one step out. lab
40 pinned `${dependency-check.version}` because an unpinned plugin makes
the same commit scan differently week to week with no diff to explain it.
`:latest` does that to the whole application: same tag, different
software, no diff.

so labs 42 and 44 promote by digest. staging is tested, the digest of
what was tested is recorded, and production deploys that digest — not a
tag that happens to point there right now. a tag is fine for a human to
type; a digest is what a pipeline promotes.

`:latest` has an extra sharp edge worth naming: it is not special to
Docker, just a default when no tag is given. so a `docker pull crm-api`
that quietly resolves to `:latest` looks deliberate in a script and is
not.

## Step 3 — CRM example

tag scheme, three tags on the same build:

| Tag | Purpose |
| --- | --- |
| `crm-api:lab41` | the human-readable name for this lab |
| `crm-api:0.0.1-SNAPSHOT` | matches the Maven version in pom.xml |
| `crm-api:<git-sha>` | ties the image to the commit that produced it |

and the digest captured at build time and recorded in the runbook:

```
crm-api@sha256:_____
```

captured with

```
docker inspect --format='{{index .RepoDigests 0}}' crm-api:lab41
```

after a push, or `.Id` for a purely local image. the value goes in
`docs/container-runbook.md` beside the git sha, because a digest with no
link back to source is only half an answer: it tells you what ran, not
what it was built from.

## Step 4 — Runbook heading

`docs/container-runbook.md`:

```
# Container runbook — crm-api

## Build
## Inspect (user, size, layers)
## Run (env vars, ports, limits)
## Verify (readiness, CUS-1001 fetch)
## Stop (graceful, SIGTERM)
## Digest capture
## Rollback (redeploy previous digest)
```

Rollback is the section that earns the digest. rolling back to "the
previous `:latest`" is not a thing that exists, because the tag moved and
the old target may be untagged or gone. rolling back to a recorded digest
is a single command, and it is the reason the capture step happens on
every build rather than when something breaks.

Inspect covers the lab 40 carry-over: confirm the image runs as 10001
rather than root, and that its size reflects a JRE runtime stage rather
than a JDK.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab41-digest-discipline.md`
- [ x ] Digest vs tag explained
- [ x ] Example tag scheme written
- [ x ] Runbook headings listed
