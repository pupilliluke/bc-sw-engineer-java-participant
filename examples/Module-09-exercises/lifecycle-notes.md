Module 9 Exercise 3 - the default lifecycle

validate -> compile -> test -> package -> verify -> install -> deploy

Phases are cumulative. Asking for a later one runs everything before it, so by
the time mvn package writes the jar it has already validated, compiled and
tested. Naming a phase is naming where to stop.


STEP 1 - INTENT TO COMMAND

  intent                                    command
  confirm the POM parses                    mvn validate
  compile production Java only              mvn compile
  run unit tests                            mvn test
  produce target/customer-service.jar       mvn package
  package plus the checks CI cares about    mvn verify        (mvn -B verify)
  put the jar in the local cache            mvn install


STEP 2 - CHECK

All six matched the reference.

None of these are isolated commands. mvn test has already run validate and
compile, mvn install runs the whole chain. What separates them is what each one
leaves behind: compile writes target/classes, package writes the jar, install
copies that jar into ~/.m2.


STEP 3 - ORDER THE WALK

  1  validate
  2  compile
  3  test
  4  package
  5  verify
  6  install

deploy is excluded on purpose. Walking the six one at a time is a first-time
exercise, mvn verify covers all of them in one run once you trust the order.


STEP 4 - WHY CI PREFERS verify

Continuous Integration usually runs mvn -B verify so the build is
batch/non-interactive and stops after verification without casually installing
or deploying from every laptop.

-B is batch mode. No prompts, no download progress noise in the log, and the
build fails instead of waiting for an answer nobody is there to give.

verify is the last phase that only touches target/. install writes into ~/.m2,
which every other project on that machine reads, so a stale snapshot there can
make an unrelated build pass for a reason that isn't in git. deploy publishes to
a shared remote and needs credentials, so it belongs to a release job rather
than a classroom laptop.


PASS CRITERIA

| # | Confirm | Notes |
| - | ------- | ----- |
| 1 | Six intent → command rows match | PASS |
| 2 | Lifecycle order is correct without `deploy` | PASS |
| 3 | You state why CI uses `mvn -B verify` | PASS |
