Module 14: Lab 14 ValidatorFactory TODOs (exercise 5)

fill-in blanks for the programmatic validation bootstrap. source is the slides'
lab overview, with counts read off the paper DTO from exercise 3.


STEP 1 AND 2, BLANKS FILLED

    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();
    Invalid blank name -> expect 1 violation
    Invalid status TYPO -> expect 0 violations, rejected later at enum conversion
    Valid Amina ACTIVE sketch -> expect 0 violations
    Spring @Valid in this pre-lab? no

blank name is one violation, not two, because the paper DTO carries @NotBlank on
fullName and nothing else. add @Size later and the same payload starts
returning two.

the status typo is the one worth remembering. status has no constraint, so the
validator passes a legal string and the failure surfaces when CustomerStatus is
resolved. zero violations does not mean the payload is good.

the factory is a resource, the slides show it in try-with-resources, so lab 14
builds it once rather than per request.


STEP 3, INVALID CASES

blank fullName, unknown status, null customerId on activate.

same three as exercise 4, and they cover the two layers, the first and third are
structural and stop at the boundary, the second gets past the validator.


STEP 4, SELF-CHECK

spring @Valid is not used in this pre-lab. no spring on the week 2 classpath,
validate() is called directly, and @Valid needs a framework to invoke it.


SCOPE

pre-lab only, do not finish the full graded lab in this exercise.


SELF-CHECK

fixtures confirmed, Amina CUS-1001 ACTIVE, Ravi CUS-1002 PROSPECT, correlation
lab-request-001. no blanks left.

| # | Confirm | Result |
| --- | --- | --- |
| 1 | File exists, lab14-validatorfactory-todos.md | Pass |
| 2 | All blanks replaced | Pass, six under STEP 1 AND 2 |
| 3 | Three invalid cases listed | Pass, under STEP 3 |
| 4 | No Spring @Valid claimed | Pass, answered no |
