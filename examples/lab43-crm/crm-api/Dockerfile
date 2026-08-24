# syntax=docker/dockerfile:1
# Build context is crm-api/, because that is where pom.xml lives and the first
# COPY expects it at the context root. See .dockerignore beside this file.

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Wrapper and pom first, source second, deliberately. Docker invalidates every
# layer after the first change, and dependencies change far less often than
# source. Copying src before dependency:go-offline would re-resolve the whole
# tree on every code edit.
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw
RUN chmod +x mvnw && ./mvnw -B -q -DskipTests dependency:go-offline

COPY src ./src

# package, not verify. `verify` runs CustomerRepositoryIT, which needs a live
# PostgreSQL, and `docker build` has no route to the database container. The
# tests are not skipped in the sense of untested: `mvn -B clean verify` runs
# them outside the image with the database up, 26 green, and that is the gate.
# Running them here would either fail or require a database inside the build,
# which is worse.
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre

# Non-root. UID 10001 is above the host system-account range, so it cannot
# collide with a real account if a volume is ever shared.
RUN useradd --system --uid 10001 --create-home spring
WORKDIR /app

# The artifact only. target/ around it holds classes, test classes and surefire
# reports, none of which the runtime needs. --chown during the copy rather than
# a later RUN chown, which would add a second layer duplicating the jar.
COPY --from=build --chown=spring:spring /workspace/target/*-SNAPSHOT.jar app.jar

USER 10001
EXPOSE 8080

# Percentage rather than a fixed -Xmx: the JVM then sizes the heap against
# whatever cgroup limit the container was given. A fixed -Xmx set for a laptop
# ignores the limit, the kernel kills the process at it, and the exit is 137
# with no Java stack trace.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"

# No secrets here. JWT_SECRET and CRM_APP_PASSWORD arrive at run time via -e;
# both are declared without defaults in application.yml, so a missing value
# stops startup rather than falling back to something.

# /dev/tcp rather than wget or curl: it is a bash builtin, so it needs no
# package added to the runtime image -- and every package added is another
# artifact for the Lab 40 scan to find. start-period covers Flyway plus context
# startup, measured at roughly 9-13 seconds locally.
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD ["bash", "-c", "exec 3<>/dev/tcp/127.0.0.1/8080 && printf 'GET /actuator/health/readiness HTTP/1.0\r\nHost: localhost\r\n\r\n' >&3 && cat <&3 | grep -q UP"]

# Labels belong in the runtime stage. Metadata attached to the build stage is
# discarded with it, so a LABEL up there would not reach the image anyone
# pulls.
#
# ARG rather than a literal value: .dockerignore excludes .git/, so nothing
# inside the build can read the SHA. It arrives via --build-arg. The default
# keeps an unstamped build honest instead of empty.
#
# Declared last because a changed ARG invalidates every layer after the
# instruction referencing it. The SHA changes on every commit and the jar does
# not, so the reference belongs below the COPY rather than above it.
ARG APP_VERSION=0.0.1-SNAPSHOT
ARG GIT_SHA=unknown
LABEL org.opencontainers.image.title="northstar-crm" \
      org.opencontainers.image.version="${APP_VERSION}" \
      org.opencontainers.image.revision="${GIT_SHA}"

ENTRYPOINT ["java","-jar","/app/app.jar"]
