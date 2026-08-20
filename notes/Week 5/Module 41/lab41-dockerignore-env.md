# Lab 41 — Plan .dockerignore and Env

## Step 1 — Ignore list

```
.git
.env
target/
reports/
node_modules/
dist/
notes/
docs/
.idea/
*.tfstate
*.dump
```

`.dockerignore` is to `docker build .` what `.gitignore` is to
`git add .`, and the two files end up looking alike for the same reason.

the trailing dot is the part worth being precise about. it is not "where
the Dockerfile is", it is the **build context root**: before Docker reads
a single instruction, it packages everything under that path and
transmits it to the daemon. measured on this tree:

| Path | Size |
| --- | --- |
| crm-ui/node_modules | 97 MB |
| crm-api/target | 52 MB |
| reports/ | 1.6 MB |
| lab40-crm total | 151 MB |
| repo root .git | 1.9 GB |

so `docker build .` from the project directory ships 151 MB before any
work begins, 97 MB of it node_modules that a Java image will never open.
from the repository root it would ship nearly two gigabytes of history.
that is the answer to why an unignored build is slow: the slowness
happens before the build starts.

`docs/` and `notes/` are excluded not because they are dangerous but
because nothing in the runtime needs them, and every byte in the context
is a byte transferred on every build.

## Step 2 — Check the reference

runtime configuration arrives through the environment. no
`ENV PASSWORD=...`, no `ARG JWT_SECRET`, no `.env` copied in.

a secret placed in the build reaches at least three places, and the
middle one is the one people get wrong:

1. **the context transfer.** it is sent to the daemon whether or not any
   COPY references it. on a CI runner or a remote builder that is a
   network hop to another machine.
2. **an image layer**, if something copies it. layers are additive, so
   `RUN rm .env` in a later step adds a layer where the file is absent
   while the earlier layer still holds it. `docker history`, or simply
   extracting the layer tarball, recovers it. deleting a secret from an
   image does not delete it.
3. **the build cache**, which persists on whichever machine built it.

and a fourth if `--build-arg` is used for a secret: build args are
readable in `docker history` on the finished image.

that ordering is why keeping a file out of the context beats not copying
it. the transfer happens first either way.

the app already reads what it needs from the environment,
`${JWT_SECRET}` and `${CRM_APP_PASSWORD}` with no defaults, so a missing
value fails startup rather than falling back to something. that property
is what makes it safe to ship an image with no configuration inside it.

## Step 3 — .env.example

keys only, values blank where they are secret:

```
SPRING_DATASOURCE_URL=jdbc:postgresql://crm-postgres-lab41:5432/crm41
SPRING_DATASOURCE_USERNAME=crm_app
SPRING_DATASOURCE_PASSWORD=
JWT_SECRET=
JAVA_OPTS=-XX:MaxRAMPercentage=75
```

the URL host is the compose service name rather than `localhost`,
because inside a container `localhost` is the container itself, not the
machine. this is the one line that has to change when the app moves from
running on the host to running in a container, and it changes in the
environment rather than in the image.

`.env.example` is committed. `.env` is not, and is already gitignored in
this project.

## Step 4 — Evidence path

`notes/screenshots/lab-41/` for the lab's evidence, as plain text:

| File | Holds |
| --- | --- |
| 01-build-context-size.txt | the sending-build-context line, before and after .dockerignore |
| 02-image-size.txt | `docker images` for the multi-stage result |
| 03-inspect-user.txt | `docker inspect` showing USER 10001, not root |
| 04-smoke.txt | readiness, the CUS-1001 fetch, graceful stop |

sanitized: no environment dumps, since a running container's environment
holds the values that were deliberately kept out of the image.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab41-dockerignore-env.md`
- [ x ] .dockerignore candidates listed
- [ x ] No password baked into Dockerfile plan
- [ x ] .env.example keys only
