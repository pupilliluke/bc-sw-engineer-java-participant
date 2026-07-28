Lab 9 - answers, experiments and verification

Temurin 21.0.4, Maven 3.9.9, Windows PowerShell, run from examples/lab9-crm.


CONCEPTS

1. Main flow
Source under src/main/java goes through javac into target/classes, gets zipped
with a manifest into target/customer-service.jar, and optionally gets copied
into ~/.m2 as customer-service-0.1.0-SNAPSHOT.jar. Nothing here touches a
database or serves a request.

2. Trust boundary
Everything under org.springframework and org.junit came off Maven Central and I
did not write or review it. My source is the trusted side, the 16 downloaded
jars are the untrusted side, and the only controls right now are pinned
versions and reading the tree.

3. Phase contract
Each phase either completes and hands the chain on or stops the build. Failure
is the whole build, not a warning. Proved it with the failing test, verify
stopped at test and package never ran, so target/ had no jar.

4. Artifact identity vs customer id
com.northstar:customer-service:0.1.0-SNAPSHOT names a build. CUS-1001 names a
person in the CRM. Building the jar creates no customers, and the two ids never
appear in the same system.

5. install idempotency
Ran install twice, both succeeded. The snapshot in ~/.m2 is meant to be
overwritten, that is what SNAPSHOT means. The second run reused the up-to-date
jar rather than rebuilding it, so the file timestamp did not move.

6. dev vs prod
dev is activeByDefault so the harmless environment is what you get by accident.
prod costs a deliberate -Pprod. Neither carries a credential, the properties
only name an environment.

7. CI failure evidence
Which phase stopped, the surefire report for the first failing test, and the
exit code. The stack trace matters less than knowing whether it died at
resolution, compile or test.

8. Two instances from the same POM version
Two builds of the same SNAPSHOT can differ, because SNAPSHOT is a moving label
and the source may have changed between them. A released version is the only
version where identical coordinates promise identical bytes.

9. test scope and the runtime image
JUnit exists to prove the code works, not to run it. On compile scope it would
sit on the production classpath forever, and production code could import
org.junit and still compile.

10. verify over install on CI
verify is the last phase that only writes to target/. install writes into the
agent's ~/.m2, which every other job on that agent reads, so one bad snapshot
can change an unrelated build's result.


FAILURE EXPERIMENTS

1. spring.version set to 9.9.9-nope, mvn compile

  [ERROR] Could not resolve dependencies for project
          com.northstar:customer-service:jar:0.1.0-SNAPSHOT
  [ERROR] dependency: org.springframework:spring-context:jar:9.9.9-nope (compile)
  [ERROR]   Could not find artifact ... in central

Failed before javac ran. Resolution happens first, so a typo in a version is a
dependency error rather than a compile error. Restored 6.2.3.

2. assertTrue(false), mvn test then mvn clean verify

  [ERROR] Tests run: 1, Failures: 1, Errors: 0, Skipped: 0 <<< FAILURE!
  org.opentest4j.AssertionFailedError: ... expected: <true> but was: <false>

mvn clean verify left no jar at all, the chain stopped at test so package never
ran. Without the clean, the jar from the previous good build was still sitting
in target/ and made a failed build look packaged. Restored the assertion.

3. mvn install twice

Both BUILD SUCCESS. Snapshot overwritten in place, no version conflict, no
prompt. Safe to repeat, which is the point of SNAPSHOT. The second run did not
rebuild the jar because it was already up to date.

4. cold vs warm mvn -B verify

  cold   26.1 s   empty local repo, 24.9 MB downloaded
  warm    6.0 s   normal ~/.m2

Ran the cold one against a throwaway repo with -Dmaven.repo.local rather than
deleting anything real. Four times slower and 25 MB of network for a project
with two declared dependencies, which is the argument for a shared CI cache.

5. removed <scope>test</scope> from junit-jupiter, re-tree

  \- org.junit.jupiter:junit-jupiter:jar:5.11.4:compile

The tree is the only place it showed. The jar did not change, a plain jar
bundles no dependencies, so nothing in target/ looked different and nothing
failed. That is what makes it dangerous, it surfaces when a downstream module
inherits JUnit as a production dependency. Restored test scope.


MANUAL VERIFICATION

  1  pwd ends with lab9-crm                                    PASS
  2  six phases succeed individually, evidence file filled     PASS
  3  mvn test runs PlaceholderTest, 0 failures                 PASS
  4  tree shows spring-context compile, junit-jupiter test     PASS
  5  help:active-profiles shows dev, -Pprod activates prod     PASS
  6  java -jar prints skeleton banner and CUS-1001/CUS-1002    PASS
  7  mvn -B verify succeeds non-interactively                  PASS
  8  no passwords in pom.xml or properties                     PASS
  9  git status does not stage target/ or secrets              PASS
 10  GAV vs CUS-1001 distinction written up                    PASS


CHECKPOINTS

A - project copy and coordinates
  1  lab9-crm exists, copied from lab8-crm                     PASS
  2  com.northstar:customer-service:0.1.0-SNAPSHOT, jar        PASS
  3  maven.compiler.release 21 and compiler plugin release 21  PASS
  4  edited on the laptop, IntelliJ plus PowerShell terminal   PASS

B - dependencies, plugins, tests
  1  spring placeholder compile, junit test scope              PASS
  2  PlaceholderTest passes under Surefire                     PASS
  3  compiler plugin and jar Main-Class configured             PASS
  4  mvn test and mvn package succeed                          PASS

C - lifecycle, tree, profiles
  1  docs/lifecycle-evidence.md covers validate to install     PASS
  2  docs/dependency-tree.txt annotated                        PASS
  3  dev/test/prod shown with help:active-profiles             PASS
  4  application-dev.properties has no secrets                 PASS

D - jar, CI, failures, security
  1  java -jar target/customer-service.jar works               PASS
  2  README documents mvn -B verify                            PASS
  3  five failure experiments recorded and restored            PASS
  4  no secrets, target/ or .m2 contents committed             PASS


SECURITY AND PRODUCTION REVIEW

1. Untrusted inputs are the downloaded artifacts, 16 jars I did not write.
   Later it becomes API request bodies.
2. Authn and authz land in the application layers later, plus repository
   manager credentials on the CI side.
3. Sensitive values are database passwords and cloud keys. None belong in
   pom.xml or a profile, they come from environment variables or a secret
   manager at run time.
4. mvn verify and a snapshot install are both safe to retry.
5. A partial failure stops the chain at the phase that failed. Nothing later
   runs, so a failed test cannot be promoted as a packaged artifact.
6. An operator watches verify duration and the failure rate per job, plus
   which phase the failures cluster in.
7. The unacceptable production default is dev activeByDefault carrying real
   credentials. Fine as an environment label, never as a secret store.
8. Contracts are versioned by the artifact version now, by OpenAPI later.


REFLECTION

1. Most influential decision
Scoping junit-jupiter test. It costs one line, it decides what the production
classpath carries forever, and experiment 5 showed nothing fails when you get
it wrong. Second is pinning versions in properties so the two dependencies
cannot drift apart.

2. Hardest failure to diagnose
The scope one, because it never failed. Bad version and failing test both
stopped the build with a message naming the cause. Removing test scope produced
a green build and a wrong classpath, visible only in the tree.

3. Evidence the walk was real
Six separate commands with different output. compile reported 16 source files,
test reported 1 test, package named the jar it built, install named the two
files it copied into ~/.m2. One mvn package run could not produce that.

4. Ten times the dependencies
Version conflicts between transitives. Maven mediation picks a version nobody
chose and the first symptom is a runtime NoSuchMethodError rather than a build
failure. Cold build time and download size get worse alongside it, 25 MB was
the cost of two declared dependencies.

5. Move to shared infrastructure
The dependency cache and an internal artifact repository. Every agent pulling
the same 25 MB from Central is waste, and a proxy repository also gives one
place to block a bad version.

6. Before real customer data
Secrets out of properties files, a real config source per environment, and the
application layers implemented rather than stubbed. CUS-1001 and Amina Khan are
documentation samples, not seed data.

7. Connection to Lab 8 and Lab 10
Lab 8 decided where code lives, Lab 9 decided how it becomes an artifact.
Neither wrote behaviour. Lab 10 forks this tree and starts filling the stubs,
so the build has to be trustworthy before the code gets interesting.

8. Signal that matters when verify fails
The phase it stopped in, then the surefire report for the first failing test.
Knowing it died at resolution rather than test changes who fixes it.

9. test scope beyond style
compile scope is transitive and test scope is not. One missing line pushes
JUnit onto the classpath of every module that later depends on
customer-service, and it lets production code import org.junit.

10. When Spring Boot arrives
Stable: groupId, artifactId, version, packaging jar, release 21, finalName.
Changing first: a Boot parent or BOM takes over versions, spring-context is
replaced by starters, and spring-boot-maven-plugin repackages the jar so the
manual Main-Class manifest config goes away.
