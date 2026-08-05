# Lab 23 — Auto-config vs ownership

## Three things Boot auto-configured

1. Embedded Tomcat on 8080 plus the DispatcherServlet, from spring-boot-starter-web. No server install, no web.xml.
2. Jackson JSON binding. The Customer JavaBean maps to and from the request and response bodies with no mapper config.
3. Actuator management endpoints, from spring-boot-starter-actuator. /actuator/health and /actuator/info exist once exposure.include names them.

## Three things you still own

1. Customer rules in CustomerService: blank id rejected, missing id throws IllegalArgumentException, which Boot answers with its default 500. A 404 needs @ControllerAdvice that this lab does not add.
2. The fixtures and the wire contract: CUS-1001 and CUS-1002 seeds, the id/name/email/status fields, and reading X-Correlation-Id (default lab-request-001) into the create log line.
3. Exposure and profile policy. application.yml exposes health,info; application-prod.yml narrows to health with show-details never. Unrestricted Actuator exposure is lab-only, not for public prod hosts. Secrets stay out of every profile file until Lab 26.
