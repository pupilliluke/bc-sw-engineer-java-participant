Module 8 Exercise 2 - package plan

Root is com.northstar.crm, reverse domain so the name can't collide with anyone
else's. Everything under it is named for one responsibility, so the package says
what a class is for before you open it.


STEP 1 - MAP

Package is everything before the last dot.

  type                        fully qualified name
  CustomerController          com.northstar.crm.controller.CustomerController
  CustomerService             com.northstar.crm.service.CustomerService
  CustomerRepository          com.northstar.crm.repository.CustomerRepository
  Customer                    com.northstar.crm.entity.Customer
  CustomerRequest             com.northstar.crm.dto.CustomerRequest
  AppConfig                   com.northstar.crm.config.AppConfig
  CustomerNotFoundException   com.northstar.crm.exception.CustomerNotFoundException


STEP 2 - CHECK

All seven matched the reference.

Customer and CustomerRequest are the pair worth noticing. Same domain word,
different packages, because one is the stored shape and the other is the wire
shape. Ex 4 splits them for real.

controller, service, repository are the three layers. entity, dto, config,
exception are supporting kinds. Naming by responsibility is what makes a wrong
import obvious later, a repository importing controller reads as wrong on sight.


STEP 3 - PACKAGE TO PATH

  package com.northstar.crm.service    ->  src/main/java/com/northstar/crm/service/

CustomerRequest:

  src/main/java/com/northstar/crm/dto/CustomerRequest.java

Every dot is a directory separator. src/main/java is the source root and the
package starts after it. Declaration and folders have to be identical, javac
rejects the file otherwise.


STEP 4 - BAD NAMES

  bad                                         correct
  com.Northstar.CRM.Service                   com.northstar.crm.service
  utils for customer business rules           service, or a focused domain package
  customer_service.java                       CustomerService.java
  declaration doesn't match folders           make both identical

Capitals in package segments compile but break the convention every tool assumes.
On a case-insensitive filesystem like Windows com.Northstar and com.northstar are
the same directory, so the two can't coexist and it fails somewhere confusing.

utils is a dumping ground. Nothing ever gets removed from it because nobody knows
what depends on it. Naming the package for the concern keeps the boundary visible
and gives new code an obvious home.

customer_service.java breaks the rule that a public class matches its filename.
javac wants CustomerService.java for public class CustomerService, underscores and
lowercase both fail it.

Mismatched declaration and folders is the one that actually stops the build.
The package is the folder path, not a label sitting at the top of the file.


PASS CRITERIA

| # | Confirm | Notes |
| - | ------- | ----- |
| 1 | Seven FQCNs are correct | PASS |
| 2 | DTO path matches its declaration | PASS |
| 3 | Package segments are lowercase and meaningful | PASS |
