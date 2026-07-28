
```xml
<groupId>com.northstar</groupId>
<artifactId>customer-service</artifactId>
<version>0.1.0-SNAPSHOT</version>
<packaging>jar</packaging>
```

| Question | Your answer                     |
| -------- |---------------------------------|
| What is the `groupId`? | com.northstar                   |
| What is the `artifactId`? | customer-service                |
| What is the `version`? | 0.1.0-SNAPSHOT                  |
| What is the packaging? | jar                             |
| Write the full GAV (`groupId:artifactId:version`) | com.northstar:customer-service:0.1.0-SNAPSHOT |

### Step 2 — Check the reference

| Question | Answer |
| -------- | ------ |
| `groupId` | `com.northstar` |
| `artifactId` | `customer-service` |
| `version` | `0.1.0-SNAPSHOT` |
| packaging | `jar` |
| GAV | `com.northstar:customer-service:0.1.0-SNAPSHOT` |
PASSED

### Step 3 — Explain SNAPSHOT

Write one sentence:

> A `-SNAPSHOT` version means the artifact is still under active development and may change without a new release number.

### Step 4 — Spot the mistakes

groupId com.example while the packages are com.northstar.crm
groupId is the reverse-domain namespace the org owns, com.example is the
placeholder an archetype leaves behind. It no longer matches the code, and
anyone else who skips the same step publishes into the same namespace. Lab 9
owns com.northstar, so the coordinates should say so.

artifactId CustomerService in PascalCase
The artifactId becomes the jar filename and the folder path under
~/.m2/repository, so it has to survive a case-insensitive filesystem on Windows
and a case-sensitive one on CI. Convention is lowercase with hyphens ->
customer-service, matching every other artifact in the tree.

omitting packaging and assuming WAR
Leaving the element out doesn't give you a war, Maven defaults to jar. The build
quietly produces customer-service-0.1.0-SNAPSHOT.jar while the deploy step looks
for a war and fails. War also expects src/main/webapp and the war plugin, none
of which Lab 9 has.

a different version on every laptop
The version is the shared identity of the build, not a local preference. If each
laptop commits its own, dependent modules resolve different artifacts and CI
can't reproduce what a developer ran. Team agrees one 0.1.0-SNAPSHOT and only
bumps it on release.

## Expected result

You can read a POM header and state the exact GAV plus packaging without guessing.

## Pass criteria

| # | Confirm | Notes      |
| - | ------- |------------|
| 1 | Five coordinate answers match the reference | PASS       |
| 2 | You explain what `-SNAPSHOT` means | PASS|
| 3 | You identify at least two coordinate mistakes | PASS|
