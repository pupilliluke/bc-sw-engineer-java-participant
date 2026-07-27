Module 8 Exercise 3 - entity vs DTO

Three types for one customer, each owning a different question.

  type               fields                 answers
  CustomerRequest    name, email            what the caller is allowed to send
  Customer           id, name, status       what we actually store
  CustomerResponse   id, name, status       what we're willing to show back

The field lists are the whole point. They deliberately don't match.

CustomerRequest has no id and no status because the caller doesn't get to choose
either one. id is generated on our side, status is a business decision. Accept
them from the boundary and anyone can post their own id or set themselves ACTIVE.

Customer holds id and status because that's the internal state. It has no email,
so the request carries a field the entity here doesn't keep. Real systems have
this in both directions, an entity carries plenty the outside never sees.

CustomerResponse looks like the entity today but it isn't the same type on
purpose. Once the entity grows a password hash, an internal note or an audit
column, returning the entity leaks it. A separate response class means adding a
field to the entity doesn't silently publish it.

Mapping is manual in StructureDemo, request -> entity -> response, so each
boundary crossing is a line you can point at. Later labs replace the hand-wiring,
the boundaries stay.


COMPILE AND RUN

  javac -d mini-out mini-src/com/northstar/crm/entity/Customer.java mini-src/com/northstar/crm/dto/CustomerRequest.java mini-src/com/northstar/crm/dto/CustomerResponse.java mini-src/com/northstar/crm/StructureDemo.java
  java -cp mini-out com.northstar.crm.StructureDemo

  CUS-1001 | Amina Khan | ACTIVE


FAILURE EXPERIMENT

Changed Customer.java to declare package com.northstar.crm.dto while leaving the
file in entity/. Compile failed:

  StructureDemo.java:5: error: package com.northstar.crm.entity does not exist
  import com.northstar.crm.entity.Customer;
  StructureDemo.java:12: error: cannot find symbol

The package declaration is what names the class, not the folder it sits in. So
the class became com.northstar.crm.dto.Customer and nothing was left at
com.northstar.crm.entity.Customer for the import to find. Same rule as Ex 2,
declaration and path have to be identical. Restored after.


PASS CRITERIA

| # | Confirm | Notes |
| - | ------- | ----- |
| 1 | Package tree matches declarations | PASS |
| 2 | Compile and run output matches expected | PASS |
| 3 | You explain entity vs request/response DTO | PASS |
