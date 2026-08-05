# Lab 23 — CrmApplication Stub (TODOs)

```java
@SpringBootApplication
public class CrmApplication {
  public static void main(String[] args) {
    SpringApplication.run(CrmApplication.class, args);
  }
}


# Lab 23 — CrmApplication Stub (TODOs)

## Main class annotation
@SpringBootApplication


## run(...) line
    SpringApplication.run(CrmApplication.class, args);

## Health URL
http://localhost:8080/actuator/health

## Package root
com.northstar.crm


## Debug / design challenge

If CrmApplication sits in com.demo instead of com.northstar.crm, what fails?

The class cannot be found

## Predict the Output / Behavior

Does @SpringBootApplication replace the need for component stereotypes on services?

no. It relies on these stereotypes to wire up the infrastructure.