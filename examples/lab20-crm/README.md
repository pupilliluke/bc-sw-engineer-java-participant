Northstar CRM build (Lab 20)

  mvn -B -Dtest=CustomerLoggingIT test
  mvn -B clean verify
  mvn -q spring-boot:run   (serves /customers.html and /api/customers on 8080)

  mvn -q dependency:tree | findstr /i "logback slf4j"

Copied from lab19-crm per step 1 of the guide, artifact renamed to
com.northstar:lab20-crm:0.0.1-SNAPSHOT. Structured logging is the whole of the
change: logback-spring.xml with a corr/cust/op pattern, a CorrelationFilter
that owns the MDC lifecycle, SLF4J in CustomerService and CustomerController,
and CustomerLoggingIT to hold the no-PII rule. The lab 19 IT and UI suites are
untouched and still green.

The dependency:tree command above prints nothing as written: -q suppresses the
INFO lines the tree is printed on. Use mvn -B dependency:tree to see it.
logback-classic 1.5.11 and slf4j-api 2.0.16 are the only binding;
log4j-to-slf4j and jul-to-slf4j are bridges into SLF4J, not competing bindings.

CHANGES BEYOND THE LOGGING WORK

create no longer takes a correlationId parameter, the filter owns correlation
now. Duplicate ids are rejected: the service checks findById before saving and
throws DuplicateCustomerException, the controller maps it to 409. lab19-crm
overwrote silently, see experiment 3. Validation moved to the controller edge
where step 5 puts it, so the service create holds only the duplicate rule.

TIMED-PATH PASS CRITERIA

| Criterion | Result |
| --------- | ----------- |
| logback-spring.xml with corr/cust/op | Pass |
| CorrelationFilter puts MDC, clears in finally | Pass |
| Service create/get logged without PII | Pass |
| CustomerLoggingIT green | Pass, 2 tests |

SECURITY NOTES

untrusted: everything over HTTP, the JSON body, the form fields and the
X-Correlation-Id header. the header is echoed and never treated as identity.

checks: the controller rejects blank customerId, fullName and status at the
edge and logs a reason code; the service rejects duplicate ids. logs do not
enforce anything, they record what was enforced.

sensitive: fullName, email, phone, address, tokens. all forbidden in log lines
and in MDC values. example.com data only in the fixtures. the full list is in
docs/logging.md and CustomerLoggingIT asserts it.

CLEANUP

  mvn -B clean
  git status

Stop spring-boot:run if it is still serving. target/ is ignored. Keep
lab20-crm, Lab 21 adds Actuator and Micrometer beside these logs.

NOTES

The logging contract is docs/logging.md. Evidence transcripts and the five
failure experiments are in notes/screenshots/lab-20/. Reflection answers and
checkpoints are in notes/Week 2/Module 20/lab20-answers.md. Regression scope
carried over from lab 19 is in docs/regression-notes.md. Full GUIDE at
labs/Week 2 - Backend, AI Tools and Testing/module-20/lab20/.
