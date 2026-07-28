Module 9 Exercise 5 - reading a dependency tree

The tree is my artifact at the root plus everything it drags in. Indentation is
distance from my pom, the +- and \- marks only say whether a node is the last
sibling. The scope on the end of each line says which classpath it lands on.

  com.northstar:build-demo:jar:0.1.0-SNAPSHOT
  +- org.junit.jupiter:junit-jupiter:jar:5.11.4:test
  |  \- org.junit.jupiter:junit-jupiter-params:jar:5.11.4:test
  \- (no production compile dependencies in the mini project)


STEP 1 - CLASSIFY THE ROWS

  artifact                direct or transitive           scope
  junit-jupiter           direct, declared in my pom     test
  junit-jupiter-params    transitive, came with jupiter  test


STEP 2 - CHECK

Both matched the reference.

junit-jupiter is an aggregator, mostly a pom that pulls the api, params and
engine artifacts along with it. One line in my pom, several jars on the test
classpath. I pin the version of what I declare and inherit the rest.

A transitive keeps the scope of the direct dependency that brought it in, which
is why params reads :test without me writing it anywhere.


STEP 3 - RUNNING IT

  mvn -q dependency:tree
  mvn dependency:tree -DoutputFile=dependency-tree.txt

mvn -q dependency:tree printed nothing. The plugin writes the tree at INFO level
and -q hides INFO, so the quiet flag suppresses the one thing I ran the command
for. Dropped -q, or used -DoutputFile which writes the tree regardless.

Actual output from mini-maven/ after Exercise 6:

  com.northstar:build-demo:jar:0.1.0-SNAPSHOT
  \- org.junit.jupiter:junit-jupiter:jar:5.11.4:test
     +- org.junit.jupiter:junit-jupiter-api:jar:5.11.4:test
     |  +- org.opentest4j:opentest4j:jar:1.3.0:test
     |  +- org.junit.platform:junit-platform-commons:jar:1.11.4:test
     |  \- org.apiguardian:apiguardian-api:jar:1.1.2:test
     +- org.junit.jupiter:junit-jupiter-params:jar:5.11.4:test
     \- org.junit.jupiter:junit-jupiter-engine:jar:5.11.4:test
        \- org.junit.platform:junit-platform-engine:jar:1.11.4:test

Deeper than the sample. One declared dependency, eight artifacts, three levels.
params sits beside api and engine rather than under api, and every line ends in
:test, so nothing here reaches the jar.


STEP 4 - CI COMMAND HABIT

  question                                answer
  what -B means                           batch mode, no interactive prompts,
                                          readable CI logs
  why verify and not casual install       proves package plus checks without
                                          writing into every agent's ~/.m2
  preferred command for this bootcamp     mvn -B verify

README sentence:

Teammates and CI should reproduce the build with mvn -B verify.


PASS CRITERIA

| # | Confirm | Notes |
| - | ------- | ----- |
| 1 | Direct vs transitive for Jupiter rows is correct | PASS |
| 2 | You ran or explained `mvn dependency:tree` | PASS  |
| 3 | Notes include `mvn -B verify` as the CI habit | PASS |
