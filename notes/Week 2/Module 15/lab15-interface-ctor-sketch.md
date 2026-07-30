Module 15: Lab 15 interface and constructor sketch (exercise 4)


STEP 1, INTERFACE

    public interface CustomerService {
        Customer findById(String customerId);
        Customer activate(String customerId);
    }


STEP 2, CONSTRUCTOR

    public DefaultCustomerService(CustomerRepository repository,
                                  CustomerNotifier notifier)

both held in final fields. CustomerRepository is required. the notifier is
taken as required rather than optional.


STEP 3, NO FRAMEWORK MAGIC

explicit constructor, not field injection. the wiring is a plain new, there is
no spring on the week 2 classpath.


STEP 4, PREP BOUNDARY

prepare for lab 15, do not complete the full service implementation now.


SCOPE

pre-lab only, do not finish the full graded lab in this exercise.


SELF-CHECK

fixtures confirmed, Amina CUS-1001 ACTIVE, Ravi CUS-1002 PROSPECT, correlation
lab-request-001.

| # | Confirm | Result |
| --- | --- | --- |
| 1 | File exists, lab15-interface-ctor-sketch.md | Pass |
| 2 | Methods listed | Pass, findById and activate under STEP 1 |
| 3 | Deps listed | Pass, repository and notifier under STEP 2 |
| 4 | Pre-lab boundary present | Pass, under STEP 4 |
