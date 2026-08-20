# Lab 41 — Fill Dockerfile TODO Skeleton

## Step 1 — Skeleton

```dockerfile
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre AS runtime
RUN useradd --uid 10001 --create-home --shell /usr/sbin/nologin appuser
WORKDIR /app
COPY --from=build --chown=10001:10001 /workspace/target/*.jar /app/app.jar
USER 10001
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=3s --start-period=45s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health/readiness || exit 1
ENTRYPOINT ["java","-jar","/app/app.jar"]
```

## Step 2 — Fill blanks

| Blank | Fill | Why |
| --- | --- | --- |
| build FROM | `eclipse-temurin:21-jdk` | matches the JDK 21.0.4 this project already builds with |
| COPY into build | `pom.xml` first, then `src` | see the layer-cache note below |
| runtime FROM | `eclipse-temurin:21-jre` | JRE not JDK; the compiler is build-time only |
| USER | `10001` | non-root, high enough not to collide with a host system account |
| COPY --from | `/workspace/target/*.jar` | the artifact only, not the directory around it |
| HEALTHCHECK | readiness over localhost | see step 3 |

**pom.xml before src, deliberately.** Docker caches per layer and
invalidates every layer after the first change. dependencies change
rarely and source changes constantly, so resolving dependencies in their
own layer means an ordinary code edit reuses the cached dependency layer
instead of re-downloading the world. copying everything at once would
make each edit a full resolve.

**`--chown` on the COPY, not a later `RUN chown`.** A separate chown
would add a whole extra layer duplicating the JAR, since a layer records
changed files and changing ownership changes the file. Doing it during
the copy costs nothing. This is also the fix for the Debug prompt: copy
as root, switch to 10001, get permission denied, because the file is
owned by a user the process no longer is.

**`target/*.jar` rather than a literal name.** The artifact is
`<artifactId>-<version>.jar`, which is `lab40-crm-0.0.1-SNAPSHOT.jar`
today and will be `lab41-crm-...` after the copy is renamed. The wildcard
survives the rename; a hardcoded name is the usual cause of the Predict
prompt's "jar not found", along with a stale `target/` or a build that
skipped `package`.

## Step 3 — Peer check

marked unsure, to confirm during the lab:

**`./mvnw` does not exist in this project.** The guide's skeleton runs
`./mvnw -B -DskipTests package`, and there is no wrapper in
`lab40-crm/crm-api/`. Either generate one with
`mvn wrapper:wrapper` or use `mvn` from the base image. The wrapper is
the better answer for reproducibility, since it pins the Maven version
into the repository the way `${dependency-check.version}` pins the
scanner, but it is a change to make deliberately rather than discover
when the build fails.

**HEALTHCHECK needs a client in the runtime image.** `wget` is present in
the Temurin JRE image but this should be confirmed rather than assumed;
if absent the options are installing curl, which adds layers and CVEs to
the image lab 40 just scanned, or `HEALTHCHECK NONE` in the image with
the probe defined in compose or in the lab 42 k8s manifest instead. The
orchestrator-side probe is the more usual production answer.

**The readiness path returned 404 and 401, now fixed.** From
lab41-health-resources.md: `probes.enabled` was unset, and `SecurityConfig`
permitted `/actuator/health` as an exact match, which does not cover the
readiness sub-path. Both were corrected in lab40-crm so lab 41 inherits a
working probe, and `ProbeEndpointsTest` guards them. Worth remembering
why it mattered: a HEALTHCHECK failing for a configuration reason looks
identical to an application that is genuinely broken.

## Step 4 — Security scrub

no `ARG` and no `ENV` carrying a secret. nothing in this file holds a
password, a signing key or a database URL.

the two `--build-arg` traps avoided: build args are visible in
`docker history` on the finished image, and a `COPY .env` would place the
value in a layer that a later `RUN rm` cannot remove, because layers are
additive.

everything the application needs at run time arrives at run time:

```
docker run -e JWT_SECRET=... -e SPRING_DATASOURCE_PASSWORD=... crm-api:lab41
```

which works because both properties are declared without defaults, so a
missing value stops startup rather than falling back.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab41-dockerfile-todos.md`
- [ x ] All major blanks filled or marked unsure
- [ x ] Non-root USER set
- [ x ] No secrets present
