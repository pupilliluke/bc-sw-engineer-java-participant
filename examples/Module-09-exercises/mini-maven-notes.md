Module 9 Exercise 6 - mini maven project

First thing in Module 9 that actually builds. Plain Java and JUnit, no Spring.

  mini-maven/
    pom.xml
    src/main/java/com/northstar/crm/BuildDemo.java
    src/test/java/com/northstar/crm/BuildDemoTest.java


STEP 1 - FILLED BLANKS

  blank               value
  groupId             com.northstar
  artifactId          build-demo
  version             0.1.0-SNAPSHOT
  packaging           jar
  junit scope         test
  compiler release    21
  mainClass           com.northstar.crm.BuildDemo

finalName build-demo is why the artifact lands at target/build-demo.jar. Without
it the default is artifactId-version, so build-demo-0.1.0-SNAPSHOT.jar, and the
java -jar command in the exercise would miss.


STEP 2 - RUN FROM mini-maven/

mvn -q test printed nothing at all. -q only lets warnings and errors through, so
silence is the pass. Ran it again without -q to have something to record.

  [INFO] --- surefire:3.5.2:test (default-test) @ build-demo ---
  [INFO] Running com.northstar.crm.BuildDemoTest
  [INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.104 s
  [INFO] Results:
  [INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
  [INFO] BUILD SUCCESS
  [INFO] Total time:  3.621 s

  mvn -q package
  target\build-demo.jar    2595 bytes

  java -jar target\build-demo.jar
  BuildDemo ready for Lab 9

Banner matches the expected result exactly.


STEP 3 - EVIDENCE

The jar plugin's mainClass writes Main-Class into META-INF/MANIFEST.MF, which is
the only reason java -jar knows where to start. Leave it out and the jar still
builds fine, it fails at run time with no main manifest attribute.

2595 bytes for a jar holding two classes. JUnit and the seven artifacts it drags
in are nowhere in it, which is test scope from Exercise 4 doing its job on a real
build rather than on paper.


PASS CRITERIA

| # | Confirm | Notes |
| - | ------- | ----- |
| 1 | `mvn -q test` succeeds | PASS |
| 2 | `java -jar target/build-demo.jar` prints the banner | PASS |
| 3 | JUnit is `test` scope; compiler release is 21 | PASS |
