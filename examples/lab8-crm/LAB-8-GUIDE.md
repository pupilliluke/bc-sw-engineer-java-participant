lab8-crm - Northstar CRM skeleton

Maven skeleton for the Customer Management Platform. Structure only, no Spring,
no JPA, no HTTP. Seven layer packages under com.northstar.crm with stubs that
compile, plus the standards docs the later labs build on.

COMPILE AND RUN

From this folder:

  mvn -q clean compile
  java -cp target\classes com.northstar.crm.Main

Expected output:

  Northstar CRM skeleton — Lab 8
  Packages: controller, service, repository, entity, dto, config, exception
  Examples: CUS-1001 Amina Khan ACTIVE | CUS-1002 Ravi Singh PROSPECT

Repository and service methods throw UnsupportedOperationException when called.
That is the Lab 8 contract, the signatures are the deliverable, the bodies come
in Labs 10-12.

DOCS

  docs/layer-flow.md          create CUS-1001 traced through the layers
  docs/CODING-STANDARDS.md    layer rules, naming, what not to commit

DESIGN DECISIONS

Layers over features because the bootcamp adds Spring, PostgreSQL and Kafka
into this exact shape later, and a shared layout beats renaming packages every
lab. Dependency direction is the one rule enforced from day one: controller ->
service -> repository -> entity, nothing points back out.

DTOs split from the entity now, while both are empty, because the split is free
today and expensive to retrofit once JPA annotations land on Customer and API
fields start leaking into storage.

Stubs throw instead of returning null so nobody mistakes a placeholder for a
working path. A caller hitting the stub fails loudly at the exact line.

Plain JDK and a minimal pom on purpose. Lab 9 expands the POM, keeping this one
boring gives it a clean diff.
