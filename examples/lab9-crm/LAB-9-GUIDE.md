lab9-crm - Northstar CRM build

Lab 8's skeleton with a real build around it. Same seven layer packages, same
stubs, the deliverable this time is the pom.xml and the evidence that the
lifecycle does what it claims.

  com.northstar:customer-service:0.1.0-SNAPSHOT

BUILD AND RUN

From this folder:

  mvn -q clean package
  java -jar target\customer-service.jar

Expected output:

  Northstar CRM skeleton — Lab 8
  Packages: controller, service, repository, entity, dto, config, exception
  Examples: CUS-1001 Amina Khan ACTIVE | CUS-1002 Ravi Singh PROSPECT

Same banner as Lab 8, reached a different way. Lab 8 ran it off target/classes
with -cp, here it comes out of a jar whose manifest names the entry point.

WHAT THE POM ADDS

  packaging jar          explicit, not left to the default
  properties             junit and spring versions in one place
  spring-context         compile scope, learning placeholder, no Boot code yet
  junit-jupiter          test scope, PlaceholderTest only
  compiler plugin        release 21
  surefire plugin        so tests actually run in the test phase
  jar plugin             Main-Class in the manifest, finalName customer-service
  profiles               dev active by default, test and prod on request

PROFILES

  mvn help:active-profiles           dev
  mvn -Ptest help:active-profiles    test
  mvn -Pprod help:active-profiles    prod

dev is activeByDefault, so a plain build is the laptop build. Naming another
profile replaces the default set rather than adding to it, dev switches off on
its own. No credentials live in any of them, app.environment is the only
property and application-dev.properties holds nothing secret.

CI NOTE (preview, pipelines deepen in later modules)

Preferred verify command on agents:

  mvn -B verify

-B is batch mode, non-interactive, so nothing waits on a prompt no one is there
to answer and the log stays readable. Prefer verify over install on CI unless
the pipeline intentionally publishes to an artifact repository. install writes
into the agent's ~/.m2, which every other job on that agent reads. Never deploy
snapshots from a developer laptop without agreement.

  Artifact coordinates: com.northstar:customer-service:0.1.0-SNAPSHOT
  Sample customer IDs (docs only): CUS-1001, CUS-1002
  Correlation ID (logs later): lab-request-001

DESIGN DECISIONS

Versions in properties rather than inline so junit.version and spring.version
are changed in one place. Two dependencies is small enough that inline would
work, the habit is for when the list is thirty long.

finalName customer-service so the run command is stable, target/customer-service.jar
keeps its name when the version bumps. install ignores finalName and stores the
artifact as customer-service-0.1.0-SNAPSHOT.jar, which is correct, the local
repository is keyed by coordinates and cannot have two builds colliding.

Spring is on the classpath and nothing imports it. Deliberate for Lab 9, the
point is resolving and scoping a real dependency rather than writing Spring.
Boot arrives in Lab 22.

EVIDENCE

  docs/lifecycle-evidence.md      validate through install, run separately
  docs/dependency-tree.txt        annotated, direct vs transitive, junit scope
  docs/architecture-now-later.md  build time now, React/Kafka/PostgreSQL later
  notes/lab9-answers.md           concepts, failure experiments, reflection

CLEANUP

  mvn clean

Leaves sources and docs, removes target/. The installed snapshot under
~/.m2/repository/com/northstar/customer-service can be deleted if disk is tight,
the next mvn install puts it back.
