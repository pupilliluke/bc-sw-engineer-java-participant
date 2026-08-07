# Lab 25 — Package Sketch

## Tree

com.northstar.crm
api/CustomerController
service/CustomerService
repository/CustomerRepository
repository/InMemoryCustomerRepository
model/Customer
CrmApplication

## Where does the controller live?
api/

## Where does InMemoryCustomerRepository live?
repository/


## Debug / design challenge

Should SOAP endpoints sit under repository?
no

## Predict the Output / Behavior

Why keep model free of Spring Web annotations?
strictly for contract


## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/package-tree.md`
- [ x ] api/service/repository/model
- [ x ] Application root noted
