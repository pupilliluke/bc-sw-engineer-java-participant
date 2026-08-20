# Lab 41 — Sketch Multi-Stage Build

## Reference

| Stage | Contains | Must not contain |
| --- | --- | --- |
| build | JDK 21, Maven, sources, ~/.m2 | runtime secrets |
| runtime | JRE 21, app JAR, non-root user | Maven, JDK, source, .git, .env |

## Step 1 — Stages

two stages. `build` runs Maven on a JDK 21 image and produces the JAR.
`runtime` starts from a JRE 21 image and receives exactly one thing:

```
COPY --from=build /workspace/target/lab41-crm-0.0.1-SNAPSHOT.jar /app/app.jar
```

that single line is the whole of multi-stage. everything else in the
build stage is discarded because nothing copies it forward, so the size
and the risk of the build tooling never reach the shipped image.

`target/` is the trap in the wording. the JAR lives inside it and must
survive; the directory around it, compiled classes, test classes and
surefire reports, must not. the artifact crosses, the workspace does not.

## Step 2 — Check the reference

what stays out, split by the reason it stays out, because the two
reasons need different fixes:

| Excluded | Size | Security |
| --- | --- | --- |
| JDK and Maven, plus the ~/.m2 cache | yes, hundreds of MB the runtime never executes | |
| src/ | small | hands a reader the source |
| pom.xml | negligible | |
| .git | 1.9 GB at this repo's root | every secret ever committed, including one already rotated |
| .env | negligible | JWT_SECRET and CRM_APP_PASSWORD in plaintext |

the two security rows are the ones lab 40 makes concrete. the assessment
records that the old signing default is still in git history because
history was not rewritten, so copying `.git` into an image ships a secret
that was already retired.

.env is a slightly different case and the distinction is worth stating.
it is not merely dangerous to copy, it is **runtime configuration**, so
it has no business in a build at all. the image holds code; the container
receives its config when it starts:

```
docker run -e JWT_SECRET=... -e CRM_APP_PASSWORD=... crm-api:lab41
```

the app is already shaped for this. `${JWT_SECRET}` reads the
environment, and `spring.config.import` is `optional:`, so when no file
is present the environment supplies the values instead. the same image
therefore runs on this laptop and in a container without a rebuild, which
is the point: anything that differs between environments arrives at run
time or you need one image per environment.

base images from Eclipse Temurin, matching the JDK 21.0.4 this project
already builds with.

## Step 3 — User

non-root, UID 10001, created in the runtime stage and selected with
`USER 10001` before the ENTRYPOINT.

root inside a container is not a separate, harmless root. it is UID 0 on
the host kernel, and the isolation between them is namespaces rather than
a security boundary of the kind a VM provides. so a container escape, or
a mounted host path, or a misconfigured capability, all cash out as root
on the host. a process that only needs to read a JAR and open port 8080
has no reason to hold that.

10001 rather than a low number because host UIDs below 1000 are system
accounts, and a high UID cannot collide with one if a volume is ever
shared.

the file the app runs needs to be readable by that UID, which is the
usual first failure: copy as root, switch user, get permission denied.

## Step 4 — CRM note

`CUS-1001` and `CUS-1002` are rows in the `crm40` database, read by the
application at runtime. they are not build arguments and not baked into
the image.

the database is a different container. the image being built here holds
the API and nothing else, so the fixtures live where the data lives and
the JAR reaches them over the network using a URL supplied at run time.
building them in would mean the image carries data, which makes it
environment-specific and impossible to promote unchanged from local to
staging.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab41-multistage-sketch.md`
- [ x ] Build vs runtime separated
- [ x ] JAR-only copy planned
- [ x ] Non-root UID noted
