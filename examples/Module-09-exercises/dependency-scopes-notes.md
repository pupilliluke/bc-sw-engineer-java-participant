Module 9 Exercise 4 - dependency scopes

Scope answers two questions, is the jar on the compile classpath and is it on
the runtime classpath. compile is yes/yes, runtime is no/yes, provided is
yes/no. test is both, on the test classpath only, and never ships.


STEP 1 - ASSIGN A SCOPE

  dependency need                                  scope
  JUnit Jupiter, used in src/test/java only        test
  Spring Context called from production sources    compile (default)
  JDBC driver, never imported in source            runtime
  API the app server provides in production        provided


STEP 2 - CHECK

All four matched the reference.

The JDBC driver is the least obvious one. Production code compiles against the
java.sql interfaces and the driver is located at run time, so runtime scope
keeps it off the compile classpath and stops anyone importing a vendor class by
accident.

provided is the mirror of that, compile against the API and ship nothing.
Packaging it anyway puts a second copy of those classes beside the container's
own, and a classloader conflict is far harder to read than a missing class.


STEP 3 - JUNIT WITH NO SCOPE

JUnit becomes a production dependency: it is packaged/resolved for the main app,
pollutes the runtime classpath, and signals the wrong intent to teammates and
CI.

The reach is the part that bites. test scope isn't transitive, compile scope is,
so one missing line pushes JUnit and everything it drags in onto the classpath
of every module that depends on customer-service.


STEP 4 - TEAM RULE

Test libraries always use <scope>test</scope>.
Do not leave JUnit on the default compile scope.

Scope prints at the end of every line in mvn dependency:tree, so junit-jupiter
should read org.junit.jupiter:junit-jupiter:jar:5.11.4:test. Anything test
shaped without that suffix is on the wrong classpath. Checked properly in
Exercise 5.


PASS CRITERIA

| # | Confirm | Notes |
| - | ------- | ----- |
| 1 | Four scope assignments match the reference | PASS |
| 2 | You explain the JUnit-without-scope mistake | PASS |
| 3 | Team rule is written | PASS |
