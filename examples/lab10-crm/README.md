Northstar CRM build (Lab 9)

  mvn -q clean package
  java -jar target\customer-service.jar

Prints the skeleton banner with the seven packages and CUS-1001 / CUS-1002.
Artifact is com.northstar:customer-service:0.1.0-SNAPSHOT, packaged as
target/customer-service.jar.

CI

  mvn -B verify

Batch mode, non-interactive, stops after verification. Prefer it over install
on shared agents, install writes into that agent's ~/.m2 where every other job
can see it.

CLEANUP

  mvn clean

Full build story and design decisions in LAB-9-GUIDE.md, lifecycle evidence in
docs/lifecycle-evidence.md, annotated tree in docs/dependency-tree.txt, layer
rules in docs/CODING-STANDARDS.md.
