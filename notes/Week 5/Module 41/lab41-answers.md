Lab 41 containerize the Spring Boot CRM (evidence log, reflection questions,
checkpoints)

built as examples\lab41-crm, a copy of lab40-crm with its own container
crm-postgres-lab41 and database crm41. the work was a multi-stage Dockerfile
building on maven:3.9-eclipse-temurin-21 and running on eclipse-temurin:21-jre,
a non-root runtime at uid 10001, a /dev/tcp HEALTHCHECK against the readiness
probe, OCI labels carrying the build commit, a run under a memory limit and a
read-only root filesystem, and the five failure experiments.

three predictions written in the pre-lab were wrong and the corrections are
recorded below and in notes/screenshots/lab-41/. the image builds and the smoke
passes end to end, but step 10, the peer build from the runbook alone, has not
been performed.


EVIDENCE LOG

- Image tag / id: crm-api:lab41 and crm-api:1.0.0-a57412f, both
  sha256:4cf59c01fcd5afcb3a61c02b6140f24284185e579a7ef8cb2a4455f37b755fbd
- Config.User: 10001
- Size (bytes): 162696555 per docker image inspect .Size. docker images reports
  555MB and the docker history layer sum is 393MB. the three disagree because
  the containerd image store builds a manifest list with an attestation
  manifest attached and each command totals it differently.
- Readiness curl result: HTTP 200 {"status":"UP"}, docker ps (healthy),
  liveness also 200. anonymous GET /api/customers/CUS-1001 is 401.
- Smoke CUS-1001 result: HTTP 200,
  {"id":"CUS-1001","name":"Amina Khan","email":"amina@example.com",
  "status":"ACTIVE"} with a bearer token from POST /api/auth/login as agent1.
  page?status=ACTIVE&size=20 returns one bounded page.
- Stop --time 20 observation: 844ms, exit 143, OOMKilled false. logs show
  Commencing graceful shutdown, Graceful shutdown complete, then the JPA
  factory and Hikari pool closing. 143 is 128 plus 15 and is the correct code
  for a JVM that ran its shutdown hooks and terminated on SIGTERM.
- Bad JDBC experiment: CRM_DB_URL pointed at no-such-host. exit 1, Flyway
  FlywaySqlException unable to obtain connection, context refresh cancelled,
  zero startup-complete lines, no listener on the published port.
- Runbook peer-tested: N. step 10 not performed.


REFLECTION QUESTIONS

1. Which design decision most affected image safety/size?

the two-stage split. the build stage is 797MB and none of it reaches the image:
docker history shows no Maven layer, no JDK build layer, no .m2 and no src. the
safety half is the same fact restated, because layers are additive and a delete
in a later layer only writes a whiteout while the bytes stay readable in the
earlier one. discarding a stage is the only thing that actually removes
build-time material.

2. What evidence proves non-root + readiness?

non-root: Config.User is 10001 in the image, and docker exec id on the running
container returns uid=10001(spring) gid=999(spring). the image field alone
would not prove the process runs as that user, the exec does. the negative
control is failure experiment 1, which commented USER out and produced an empty
Config.User and uid=0(root).

readiness: HTTP 200 {"status":"UP"} on /actuator/health/readiness and docker ps
reporting (healthy), which also means the /dev/tcp HEALTHCHECK passed, with
anonymous GET /api/customers/CUS-1001 returning 401 alongside it so an open
probe is not read as an open surface.

3. Which failure was hardest to diagnose (network vs health vs perms)?

none of the three. each of those failed loudly with a line naming the cause:
the bad JDBC host gave FlywaySqlException, the missing JWT_SECRET gave could not
resolve placeholder, and the /tmp mount error named the invalid path. the hard
one was identity. crm-api:lab41 existed with an image id that did not match the
build just run, and only the labels identified it, title=ubuntu meaning it
predated the label block and came from a previous session's build process that
was still alive. nothing had failed; the wrong artifact was sitting under the
right tag. second hardest was the correlation id, which also did not fail: the
GET returned 200 and the id simply never appeared in the logs, because the read
path does not take the header. a silent absence is harder than an exception.


CHECKPOINTS

| # | Confirm | Result |
| - | ------- | ------ |
| A1 | lab41-crm verifies before image work | Pass, mvn -B clean verify green at 26 tests, 19 unit and 7 IT, Flyway at v2 |
| A2 | .dockerignore excludes secrets/target | Pass, context is 170 KB of the 52M directory, target/ excluded |
| A3 | multi-stage Dockerfile builds JAR then JRE runtime | Pass, maven:3.9-eclipse-temurin-21 to eclipse-temurin:21-jre |
| B1 | runs as UID 10001 or fixed non-root | Pass, docker exec id returns uid=10001(spring) |
| B2 | no secrets in image env/layers | Pass, Config.Env holds only JAVA_TOOL_OPTIONS and Temurin's own entries; docker history shows no Maven, JDK build, .m2 or src layer |
| B3 | image id/size/user recorded | Pass, docs/container-runbook.md Recorded identity, with the size caveat stated |
| C1 | runtime env via .env.example pattern | Pass, .env.example committed with blank passwords, .env.local gitignored via .env.* |
| C2 | readiness healthy; CRM smoke with CUS-1001 | Pass, notes/screenshots/lab-41/lab41-smoke.txt |
| C3 | graceful stop + bad URL experiment documented | Pass, notes/screenshots/lab-41/lab41-failure-experiments.txt |
| D1 | container-runbook.md complete | Pass, build, run, verify, stop, network, registry flow, identity, deviations |
| D2 | registry/digest notes present | Pass, including the finding that an identical rebuild produces a different manifest-list digest because of the provenance attestation |
| D3 | no .env / tokens in Git | Pass, git status --short shows only docs/ and notes/screenshots/lab-41/; no .env variant visible to git |
| D4 | peer build from runbook succeeded | Fail, not performed |
| D5 | JDBC hostname for container to PostgreSQL documented | Pass, postgres and crm-postgres-lab41 are both aliases on lab41-crm_default; host.docker.internal not used and the reason recorded |
| D6 | actuator does not expose env/beans publicly without auth | Pass, exposure is health,info and SecurityConfig permits only the three health paths |


CORRECTIONS TO THE PRE-LAB PREDICTIONS

lab41-smoke-plan.md predicted the container would start, liveness would pass
and readiness would fail on a bad datasource. it does not. Flyway runs during
context initialization as a dependency of entityManagerFactory, so a database
unreachable at boot cancels the refresh before Tomcat starts. there is no
listener and no probe to answer. the readiness-fails-liveness-passes split
requires an application that started and then lost the database. in lab 42 this
state is CrashLoopBackOff, not an unready pod.

the same plan predicted a graceful stop exits 0. it exits 143.

the same plan expected X-Correlation-Id on GET /api/customers/CUS-1001 to
appear in the container logs. it does not. CustomerController.get takes only
the path variable; only create, update and updateStatus accept the header, and
the id is emitted only on the DuplicateCustomerException path as a ProblemDetail
property. there is no MDC filter, so no happy-path log line carries it. a POST
with a duplicate email and X-Correlation-Id: lab-request-003 returns 409 with
correlationId lab-request-003 in the body, which is the propagation the app
actually implements. this is a gap carried from labs 37 to 39, not a
containerization defect, and was not fixed inside a containerization lab.


SCOPE HONESTY

D4 is a genuine Fail. the runbook has not been executed by anyone other than
its author, so its claim to be sufficient is untested.

failure experiment 3, removing target/ from .dockerignore, did not measure what
the guide describes. BuildKit transfers context lazily and the build was cached,
so the reported figures were identical with and without the ignore. the 52M to
170 KB figure recorded is an on-disk proxy, not a measured transfer.

failure experiment 4, docker stop --time 1, did not reproduce a forced kill. the
application was idle and shut down in 535ms. the experiment would only bite
under load with in-flight requests.

the lab 40 gate still fails at failBuildOnCVSS 7, with 70 findings across 11
Spring Boot 3.3.5 managed transitives triaged in
examples/lab40-crm/docs/security-findings.csv. the parent bump was deferred
deliberately and that has not changed.

crm-api/README.md still describes lab 39. it was carried through the lab40 and
lab41 copies without being updated.
