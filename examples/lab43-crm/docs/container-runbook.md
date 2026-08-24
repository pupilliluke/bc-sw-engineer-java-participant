# Container Runbook — Lab 41, Northstar CRM

Build, run, verify and stop the `crm-api` image without reference to the lab
guide or to anyone who built it before.

## Prerequisites

- Docker Desktop running.
- PostgreSQL running as a sibling container. `docker compose up -d` from
  `examples/lab41-crm/` starts it as `crm-postgres-lab41` on the
  `lab41-crm_default` network with database `crm41`.
- `.env` in `examples/lab41-crm/` with the compose values. Copy `.env.example`
  and fill it. `.env` is gitignored.
- `.env.local` in `examples/lab41-crm/` with the four values the container
  needs. See Run, below. Also gitignored, via the `.env.*` rule.

## Build

The build context is `crm-api/`, not the project root, because the first
`COPY pom.xml .` expects `pom.xml` at the context root.

```
cd examples/lab41-crm/crm-api
docker build --pull --build-arg GIT_SHA=$(git rev-parse HEAD) -t crm-api:lab41 .
```

`--pull` re-checks the base image tags. `GIT_SHA` becomes the
`org.opencontainers.image.revision` label; without it the label reads
`unknown`. Build from a clean working tree or the label names a commit that
does not contain what was built.

The build stage runs `package`, not `verify`. `verify` runs
`CustomerRepositoryIT`, which needs a live PostgreSQL, and `docker build` has
no route to the database container. Run the tests outside the image instead,
with the database up:

```
cd examples/lab41-crm/crm-api
mvn -B clean verify
```

26 tests, 19 unit and 7 integration, Flyway at v2.

## Run

`.env.local` holds four keys. `CRM_DB_URL` must name the postgres container,
not `localhost`: inside `crm-lab41`, `localhost` is `crm-lab41`.

```
CRM_DB_URL=jdbc:postgresql://postgres:5432/crm41
CRM_DB_USERNAME=crm_app
CRM_APP_PASSWORD=<the crm_app password from .env>
JWT_SECRET=<the value from .env>
```

```
cd examples/lab41-crm
docker run -d --name crm-lab41 --network lab41-crm_default \
  --memory=512m --read-only --tmpfs /tmp \
  --env-file .env.local -p 8080:8080 crm-api:lab41
```

`--read-only` with `--tmpfs /tmp` is not required by the lab. It holds because
the application writes nothing to disk: there is no file I/O in `src/main` and
no file appender configured. Tomcat's servlet temp directory and the JVM's
hsperfdata file both live under `java.io.tmpdir`, which is `/tmp`.

`--memory=512m` with the image's `MaxRAMPercentage=75` gives a 384MB heap. A
fixed `-Xmx` would ignore the cgroup limit and the kernel would kill the
process at it with exit 137 and no Java stack trace.

On Windows Git Bash, prefix the command with `MSYS_NO_PATHCONV=1` or `/tmp` is
rewritten to a Windows path and the run fails with
`invalid mount path: 'C' mount path must be absolute`. PowerShell does not need
this.

## Verify

Poll readiness rather than sleeping a fixed interval.

```
curl.exe -fsS --retry 45 --retry-delay 2 --retry-all-errors \
  --retry-connrefused http://127.0.0.1:8080/actuator/health/readiness
```

Expect `{"status":"UP"}` and `docker ps` showing `(healthy)`. Startup is
9 to 13 seconds; the HEALTHCHECK `start-period` is 40s to cover it.

Every customer endpoint requires a token. Basic auth is disabled.

```
curl.exe -s -X POST http://127.0.0.1:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"agent1","password":"agent1"}'

curl.exe -s http://127.0.0.1:8080/api/customers/CUS-1001 \
  -H "Authorization: Bearer <accessToken from above>" \
  -H 'X-Correlation-Id: lab-request-001'
```

Expect 200 and `CUS-1001 / Amina Khan / ACTIVE`. `FixtureLoader` seeds
CUS-1001 and CUS-1002 at startup, idempotently by `public_id`.

Anonymous `GET /api/customers/CUS-1001` returns 401. `/actuator/health`,
`/actuator/health/readiness` and `/actuator/health/liveness` are the only
permitted anonymous paths; `env` and `beans` are not exposed.

Confirm identity:

```
docker exec crm-lab41 id
```

Expect `uid=10001(spring)`.

## Stop

```
docker stop --time 20 crm-lab41
```

Exit code is 143, which is 128 plus 15. That is a successful graceful stop, not
a failure: the JVM ran its shutdown hooks and terminated on SIGTERM. Logs show
`Commencing graceful shutdown` then `Graceful shutdown complete`, then the JPA
factory and the Hikari pool closing. Observed at 844ms idle.

The grace period must exceed `spring.lifecycle.timeout-per-shutdown-phase`,
which is 20s, or SIGKILL wins and in-flight requests are severed.

## Network and JDBC hostname

The postgres container carries two DNS aliases on `lab41-crm_default`:
`crm-postgres-lab41` and `postgres`. Either works as the JDBC host from another
container on that network. `postgres` is used above because it is the compose
service name and survives a rename of the container.

`host.docker.internal` is not used. It routes back to the Windows host, which
would reach postgres only through the published port and adds a hop for no gain
when both containers share a user-defined bridge network.

`localhost` in `CRM_DB_URL` is the single most common failure here. It resolves
to the CRM container itself and produces a connection refused at Flyway.

## Registry flow

Not pushed during Lab 41. This is what Lab 42 will do.

**Login stays outside source control.** `docker login` writes credentials to
the Docker config or a credential helper. No registry username, password or
token goes in a Dockerfile, a compose file, `.env.example`, or any tracked
file. In CI the credential comes from the runner's secret store.

**Tag by version and git SHA, never only `latest`.**

```
docker tag crm-api:lab41 crm-api:1.0.0-a57412f
docker push registry.example.com/training/crm-api:1.0.0-a57412f
```

`latest` is a mutable pointer. Two people pulling `latest` an hour apart can
get different images, a rollback has no earlier tag to return to, and an
incident cannot be traced to a commit. The version plus SHA form answers what
is running and which commit built it from the tag alone.

**Push authorization** is a separate grant from pull. Only CI should hold push
rights to a shared repository; a developer laptop that can push can overwrite a
tag another environment is running.

**Digest pinning.** A tag can be moved; a digest cannot. Anything promoted to
Lab 42 should be referenced as `name@sha256:...`, not by tag.

Record the digest from the push output. Do not expect to reproduce it by
rebuilding: BuildKit attaches a provenance attestation whose content varies per
build, so an identical rebuild of identical source produces a different
manifest-list digest. The layer `diff_id` values are stable; the manifest digest
is not.

Locally, `RepoDigests` is already populated because Docker Desktop uses the
containerd image store, where the image ID is the manifest-list digest. That is
a local digest, not a registry-issued one.

**Cleanup of old tags.** Remove superseded local tags so a stale image is not
mistaken for the current one:

```
docker image prune
docker rmi crm-api:<old-sha>
```

Keep any digest a deployed environment still references.

## Recorded identity

Built 2026-08-20 from commit `a57412fecf2d5b13282c8b568598d69ce38e3787`
(clean tree).

| Field | Value |
| --- | --- |
| Tags | `crm-api:lab41`, `crm-api:1.0.0-a57412f` |
| Image ID / local digest | `sha256:4cf59c01fcd5afcb3a61c02b6140f24284185e579a7ef8cb2a4455f37b755fbd` |
| `Config.User` | `10001` |
| Size | 555MB per `docker images` |
| Entrypoint | `["java","-jar","/app/app.jar"]` |
| Exposed | `8080/tcp` |
| Architecture | `amd64/linux` |
| `image.revision` | `a57412fecf2d5b13282c8b568598d69ce38e3787` |

Size is reported three ways and they disagree: `docker images` 555MB,
`docker history` layer sum 393MB, `docker image inspect .Size` 162696555. The
containerd image store builds a manifest list with an attestation manifest
attached and the commands total it differently. Record the command alongside
the number.

## Deviations from the lab guide

1. The build stage runs `package`, not `verify`. Reason under Build, above.
2. The Dockerfile lives in `crm-api/`, not the project root, so that the context
   root is where `pom.xml` is. A consequence: the `.env`, `.env.*` and
   `!.env.example` rules in `.dockerignore` match nothing, because those files
   live one level up in `examples/lab41-crm/` and are already outside the
   context. What keeps `.env` out of the image is the choice of context root,
   not those three lines.
3. `--read-only --tmpfs /tmp` is added to the run command. Reason under Run.
4. OCI labels set `title`, `version` and `revision`. `description` and `created`
   are left inherited from the base image and therefore still describe Ubuntu,
   not this application.
