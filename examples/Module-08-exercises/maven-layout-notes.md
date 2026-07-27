Module 8 Exercise 1 - Maven project layout

Reference project is customer-management-platform. pom.xml at the root, then
src/main for what ships, src/test for what proves it works, docs for humans,
target for whatever Maven generates.


STEP 1 - CLASSIFY THE SIX FILES

  file                          destination
  Customer.java                 src/main/java/com/northstar/crm/
  CustomerServiceTest.java      src/test/java/com/northstar/crm/
  application.properties        src/main/resources/
  sample-customers.json         src/test/resources/        tests only
  CODING-STANDARDS.md           docs/
  Customer.class                target/classes/            generated

Matched the answer key on all six.

The split that decides everything: java vs resources is code vs everything else,
main vs test is ships vs doesn't. Customer.class isn't in the list because you
never put it anywhere, javac writes it.


STEP 2 - CHECK

  Customer.java             src/main/java/...       match
  CustomerServiceTest.java  src/test/java/...       match
  application.properties    src/main/resources/     match
  test JSON                 src/test/resources/     match
  standards                 docs/                   match
  Customer.class            target/classes/         match


STEP 3 - WHY target/ IS IGNORED

target/ is generated from source by Maven. It can be deleted and rebuilt, so it
should be ignored rather than committed.

Nothing in it is a source of truth. mvn clean deletes the whole directory and the
next build recreates it from src/. Committing it means merge conflicts on binary
files and a repo that grows for no reason. Same rule as out/ and *.class in the
Week 1 gitignore.


STEP 4 - THE FOUR MISTAKES

production Java in src/test/java
Test sources compile with test scope and never get packaged into the jar. The
class works locally and is missing at runtime. It can also see test-only
dependencies like JUnit, which won't be on the production classpath.

passwords committed in application.properties
src/main/resources is committed, so the password is in git history permanently.
Deleting it later doesn't remove it, anyone with repo access can read the old
commit. Config belongs in the file, the secret belongs in an environment
variable or a secret manager.

hand-editing target/classes
It's generated output. The next mvn clean or rebuild overwrites the edit and it's
gone. Worse while it lasts, the bytecode no longer matches the source, so
everyone reading the .java file sees something that isn't running.

test fixtures in production resources without a runtime need
Ships dead weight inside the jar and puts fake data where production code can
load it. Test data belongs in src/test/resources, which never gets packaged.


PASS CRITERIA

| # | Confirm | Notes |
| - | ------- | ----- |
| 1 | Six files classified correctly | PASS |
| 2 | You explain why `target/` is ignored | PASS |
| 3 | You state that resources must not contain committed secrets | PASS |
