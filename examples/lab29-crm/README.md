Northstar CRM build (Lab 29)

  mvn -B test
  mvn -B spring-boot:run

  TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"agent1","password":"agent1"}' | jq -r .accessToken)

  curl -s -i -X POST http://localhost:8080/api/customers \
    -H "Content-Type: application/json" \
    -H "X-Correlation-Id: lab-request-001" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"id":"","name":"","email":"not-an-email","status":"ACTIVE"}'
  # expect HTTP 400 + violations[]

  curl -s -i http://localhost:8080/api/customers/CUS-1001 \
    -H "X-Correlation-Id: lab-request-001" \
    -H "Authorization: Bearer $TOKEN"
  # expect 200 Amina Khan ACTIVE

  mvn -B test
  # Tests run: 4

  git status --short

Copied from the lab 29 starter, which ships the lab 28 security baseline and the
ErrorResponse class. The customer routes need a Bearer token.

CLEANUP

  mvn -q clean
  git status --short

Ctrl+C spring-boot:run. target/ and .env are ignored. Keep lab29-crm, labs 30
and 31 add Kafka on top of this error contract.

NOTES

Error contract is docs/error-contract.md. Evidence and the failure experiments
are in notes/screenshots/lab-29/. Checkpoints and reflection answers are in
notes/Week 3/Module 29/lab29-answers.md. Full GUIDE at
labs/Week 3 - Spring Framework and Enterprise Patterns/module-29/lab29/.
