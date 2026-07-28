Lab 9 - lifecycle evidence

Six phases run separately from examples/lab9-crm after mvn -q clean, so each one
starts from the state the previous one left. Temurin 21.0.4, Maven 3.9.9,
Windows PowerShell.


mvn validate

  [INFO] Building Northstar Customer Service 0.1.0-SNAPSHOT
  [INFO] BUILD SUCCESS

Nothing appeared on disk. validate only proves the model parses and the
coordinates resolve, target/ was still absent after this ran.


mvn compile

  [INFO] Copying 1 resource from src\main\resources to target\classes
  [INFO] Compiling 16 source files with javac [debug release 21] to target\classes
  [INFO] BUILD SUCCESS

16 files is the whole Lab 8 tree, Main plus seven package-info files plus the
layer classes. release 21 in the javac line is the compiler plugin config
showing up, not a default.

  target/classes/    com/northstar/crm/**.class + application.properties


mvn test

  [INFO] Compiling 1 source file with javac [debug release 21] to target\test-classes
  [INFO] Running com.northstar.crm.PlaceholderTest
  [INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.082 s
  [INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
  [INFO] BUILD SUCCESS

  target/test-classes/      PlaceholderTest.class
  target/surefire-reports/  txt + xml for the one test


mvn package

  [INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
  [INFO] Building jar: C:\Users\lukel\java-bootcamp\examples\lab9-crm\target\customer-service.jar
  [INFO] BUILD SUCCESS

  target/customer-service.jar    10169 bytes

Tests ran a second time here. Phases are cumulative, package doesn't trust that
test already passed, it runs the chain again.

customer-service.jar rather than customer-service-0.1.0-SNAPSHOT.jar because of
finalName in the build block.


mvn verify

  [INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
  [INFO] BUILD SUCCESS

Output is identical to package. Nothing is bound to verify in this POM, no
failsafe, no integration tests, no enforcer, so the phase runs and adds no
checks of its own. It still matters as the CI command, it's the point where
those checks get attached later without changing what CI types.


mvn install

  [INFO] Installing ...\lab9-crm\pom.xml to
         C:\Users\lukel\.m2\repository\com\northstar\customer-service\0.1.0-SNAPSHOT\customer-service-0.1.0-SNAPSHOT.pom
  [INFO] Installing ...\lab9-crm\target\customer-service.jar to
         C:\Users\lukel\.m2\repository\com\northstar\customer-service\0.1.0-SNAPSHOT\customer-service-0.1.0-SNAPSHOT.jar
  [INFO] BUILD SUCCESS

  ~/.m2/repository/com/northstar/customer-service/0.1.0-SNAPSHOT/
    customer-service-0.1.0-SNAPSHOT.jar
    customer-service-0.1.0-SNAPSHOT.pom
    maven-metadata-local.xml
    _remote.repositories

Two things land, the jar and the pom. The pom goes with it because anything
depending on customer-service needs to know what customer-service itself
depends on.

The name changed on the way in. finalName only controls target/, the local
repository stores it under the standard artifactId-version, so the same build
is customer-service.jar on disk and customer-service-0.1.0-SNAPSHOT.jar in .m2.


TARGET AFTER THE WALK

  classes/                 compiled production classes + resources
  test-classes/            compiled test classes
  surefire-reports/        test results, txt and xml
  maven-archiver/          pom.properties used to build the manifest
  maven-status/            incremental compile bookkeeping
  generated-sources/       empty, no annotation processors here
  generated-test-sources/  empty
  customer-service.jar     the artifact


THE PACKAGED ARTIFACT

  jar tf target\customer-service.jar

  META-INF/MANIFEST.MF
  application-dev.properties
  application.properties
  com/northstar/crm/Main.class
  com/northstar/crm/config/AppConfig.class
  com/northstar/crm/config/package-info.class
  com/northstar/crm/controller/CustomerController.class
  com/northstar/crm/controller/package-info.class
  com/northstar/crm/dto/CustomerRequest.class
  com/northstar/crm/dto/CustomerResponse.class
  com/northstar/crm/dto/package-info.class
  com/northstar/crm/entity/Customer.class
  com/northstar/crm/entity/package-info.class
  com/northstar/crm/exception/CustomerNotFoundException.class
  com/northstar/crm/exception/package-info.class
  com/northstar/crm/repository/CustomerRepository.class
  com/northstar/crm/repository/package-info.class
  com/northstar/crm/service/CustomerService.class
  com/northstar/crm/service/package-info.class
  META-INF/maven/com.northstar/customer-service/pom.xml
  META-INF/maven/com.northstar/customer-service/pom.properties

  10539 bytes

16 classes, the two properties files off src/main/resources, and a copy of the
pom under META-INF/maven so the jar carries its own coordinates. No Spring, no
JUnit. A plain jar packages this module and nothing it depends on, the
dependencies stay on the classpath at run time.

  MANIFEST.MF

  Manifest-Version: 1.0
  Created-By: Maven JAR Plugin 3.4.2
  Build-Jdk-Spec: 21
  Main-Class: com.northstar.crm.Main

Main-Class is the jar plugin config landing in the manifest. It is the whole
difference between java -jar working and failing with no main manifest
attribute.

  java -jar target\customer-service.jar

  Northstar CRM skeleton — Lab 8
  Packages: controller, service, repository, entity, dto, config, exception
  Examples: CUS-1001 Amina Khan ACTIVE | CUS-1002 Ravi Singh PROSPECT

Same three lines Lab 8 printed with java -cp target/classes. The code did not
change, the way it is delivered did.


ALL SIX PASSED

  validate  BUILD SUCCESS
  compile   BUILD SUCCESS
  test      BUILD SUCCESS   Tests run: 1, Failures: 0
  package   BUILD SUCCESS   jar built
  verify    BUILD SUCCESS
  install   BUILD SUCCESS   artifact in ~/.m2
