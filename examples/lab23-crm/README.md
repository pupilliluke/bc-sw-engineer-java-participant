Northstar CRM build (Lab 23)

  mvn -B test
  mvn -B spring-boot:run
  # then: curl http://localhost:8080/actuator/health

  mvn -B spring-boot:run "-Dspring-boot.run.profiles=dev"
  java -jar target/lab23-crm-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev

  curl -s http://localhost:8080/actuator/health
  curl -s http://localhost:8080/actuator/info
  curl -H "X-Correlation-Id: lab-request-001" -H "Content-Type: application/json" \
    -d '{"id":"CUS-1001","name":"Amina Khan","email":"amina.khan@example.com","status":"ACTIVE"}' \
    http://localhost:8080/api/customers
  curl -s http://localhost:8080/api/customers/CUS-1001
  curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/customers/CUS-MISSING

Copied from the lab 23 starter, not carried forward from lab22-crm. This is the
Initializr-style baseline: spring-boot-starter-parent 3.3.5, Java 21, starters
web + actuator + test, artifact com.northstar:lab23-crm:0.0.1-SNAPSHOT. There is
no repository layer, no metrics, no logging filter and no UI here; CustomerService
holds a ConcurrentHashMap seeded with CUS-1001 and CUS-1002.

Missing id returns 500, the Boot default for the service IllegalArgumentException.
The optional @ControllerAdvice that would make it 404 is not added. The validation
starter is not added either, so a blank name is accepted; blank id is rejected in
the service.

TIMED-PATH PASS CRITERIA

| Criterion | Result |
| --------- | ----------- |
| App starts on 8080 | Pass, Started CrmApplication in 2.883 s |
| /actuator/health returns UP | Pass, {"status":"UP"} |
| CUS-1001 create/get evidence (or IT green) | Pass, POST 201 GET 200, and CustomerControllerHttpTest |
| YAML + profile teasers present | Pass, application.yml + dev/prod |
| Autoconfig vs ownership notes present | Pass, docs/autoconfig-notes.md |

SECURITY NOTES

untrusted: the JSON body, the X-Correlation-Id header, and every caller of
/actuator.

authn/authz: none in this build. There is no validation starter, so the only
input rule is the blank-id check in CustomerService.

sensitive: name, email. The create log line carries the id, the status and the
correlation id. No secrets or real URLs in any profile file; Lab 26 owns that.

Actuator exposure is health,info and is lab-only. application-prod.yml narrows
to health with show-details never.

CLEANUP

  mvn -B clean
  git status

Stop spring-boot:run with Ctrl+C. target/ is ignored.

Keep lab23-crm, Lab 24 copies it into lab24-crm for Spring-WS.

NOTES

Evidence and the five failure experiments are in notes/screenshots/lab-23/.
Checkpoints and reflection answers are in notes/Week 3/Module 23/lab23-answers.md.
Auto-config vs ownership is docs/autoconfig-notes.md. Full GUIDE at
labs/Week 3 - Spring Framework and Enterprise Patterns/module-23/lab23/.
