Northstar CRM build (Lab 19)

  mvn -B -Dtest=CustomerApiIT test
  mvn -B -Dtest=CustomerUiIT test
  mvn -B clean verify
  mvn -q spring-boot:run   (serves /customers.html and /api/customers on 8080)

Built from the module 19 starter, not copied from lab18-crm. The starter's
model.Customer is 4 String fields with no CustomerStatus, no validator and no
facade. lab18-crm is unchanged and still owns the service rules. Artifact is
com.northstar:lab19-crm:0.0.1-SNAPSHOT under the Boot 3.3.5 parent.

maven-failsafe-plugin is the one pom addition. Both test classes end in IT,
so plain mvn test runs nothing here; failsafe runs them from target/classes
during verify. The first two commands above are the starter's smoke pair and
still work, -Dtest overrides surefire's includes.

TIMED-PATH PASS CRITERIA

| Criterion | Result |
| --------- | ----------- |
| CustomerApiIT green | Pass |
| CustomerUiIT green (Chrome/Chromium available) | Pass, Chrome 150 |
| UI uses data-testid + Page Object (no raw sleeps) | Pass |
| Correlation header echoed on create | Pass |

SECURITY NOTES

untrusted: everything over HTTP, the form fields, the JSON body and the
correlation header. the header is echoed and never treated as identity.

checks: the service rejects blank customerId and blank fullName, the
controller maps that to 400. no browser-side validation on purpose.

sensitive: example.com data only. ui-failure.png shows only the CUS-2001
fixture. no driver binaries, cookies or profiles committed.

CLEANUP

  mvn -B clean
  git status

Stop spring-boot:run if it is still serving 8080. target/ is ignored. Keep
lab19-crm for Lab 20.

NOTES

Regression scope and the CI browser strategy are in docs/regression-notes.md.
Evidence transcripts, the failure experiments and ui-failure.png are in
notes/screenshots/lab-19/. Reflection answers and checkpoints are in
notes/Week 2/Module 19/lab19-answers.md. Full GUIDE at
labs/Week 2 - Backend, AI Tools and Testing/module-19/lab19/.
