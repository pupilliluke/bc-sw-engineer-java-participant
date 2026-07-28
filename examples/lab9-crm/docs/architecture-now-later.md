Lab 9 - build time now, platform later

What Lab 9 actually added is a build. No feature moved, no request got served,
no row got stored. The CRM still throws UnsupportedOperationException wherever
Lab 8 left a stub.

NOW

  pom.xml            coordinates, scopes, plugins, profiles
  Maven lifecycle    validate through install, each phase proved separately
  target/            classes, surefire reports, customer-service.jar
  ~/.m2              the snapshot, so another local project could depend on it
  src/               unchanged Lab 8 layer stubs

The artifact is the deliverable. com.northstar:customer-service:0.1.0-SNAPSHOT
is a name every engineer and every CI agent can resolve to the same bytes,
which is the thing you cannot get by emailing a jar around.

LATER

  React SPA          talks HTTPS/JSON to the API, own build, own npm tree
  Spring Boot API    replaces spring-context placeholder with starters, and
                     spring-boot-maven-plugin repackages the jar
  PostgreSQL         JPA plus a runtime-scoped driver, the scope Exercise 4
                     covered and this POM only shows commented out
  Kafka              clients as compile dependencies, consumers for
                     notification and audit

Each of those arrives as dependencies and plugins in this same pom.xml. That is
the point of getting the build right while the project is two dependencies
deep, the file only grows.

WHAT STAYS AND WHAT MOVES

Stable when Boot lands: groupId, artifactId, version, packaging jar, release
21, finalName, the dev/test/prod profile shape, and mvn -B verify as the
command CI runs.

First to change: a Boot parent or BOM takes over version management so
junit.version and spring.version stop being mine to pick, starters replace
spring-context, and the jar plugin's Main-Class config goes away because Boot's
repackage goal handles the entry point.

The boundary held in this lab: Spring is on the classpath and nothing imports
it. Resolving and scoping a real dependency is a Maven exercise. Writing
@SpringBootApplication is Lab 22.
