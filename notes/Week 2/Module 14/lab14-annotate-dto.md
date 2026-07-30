Module 14: Lab 14 annotate the paper DTO (exercise 3)

required and optional constraints on a paper CreateCustomerRequest. sources are
the slides' presence and format sections and my lab 12 CustomerService, which
already enforces most of this by hand.


REFERENCE

| Field | Constraint idea | Paper annotation |
| --- | --- | --- |
| fullName | required, non-blank | @NotBlank |
| status | optional on create, default PROSPECT | none |
| customerId | server-assigned or pattern CUS-#### | @Pattern |


STEP 2, PAPER ANNOTATIONS

pseudo names only, documentation, nothing wired.

@NotBlank on fullName rather than @NotNull or @NotEmpty, the slides' table has
@NotNull allowing empty and whitespace and @NotEmpty still allowing whitespace,
so " " only fails on @NotBlank. that matches requireNonBlank in lab 12, which
rejects null and isBlank together.

status carries nothing. optional means absent is legal, and createCustomer
already defaults a null status to PROSPECT, which is how Ravi CUS-1002 starts.

@Pattern on customerId with CUS-#### as paper notation for four digits. the
slides say format constraints treat null separately, so @Pattern alone does not
make the field required, which is what I want for a server-assigned value. lab
12 takes customerId as a create parameter today, so moving it server-side is a
change the lab has to make, not something already true.


STEP 3, NO SPRING YET

no @Valid wired on a controller in this pre-lab. no spring on the classpath in
week 2, and the lab triggers validation programmatically through
ValidatorFactory instead.


STEP 4, CORRELATION

correlation lab-request-001 stays in headers and logs, not a DTO business field.

it identifies the request, not the customer, so it fails the client-controlled
field test for a request DTO and the server-controlled one for a response DTO.
lab 12 already treats it that way, CustomerService holds it as construction
state and appends it to failure messages.


SCOPE

pre-lab only, do not finish the full graded lab in this exercise.


SELF-CHECK

fixtures confirmed, Amina CUS-1001 ACTIVE, Ravi CUS-1002 PROSPECT, correlation
lab-request-001.

| # | Confirm | Result |
| --- | --- | --- |
| 1 | File exists, lab14-annotate-dto.md | Pass |
| 2 | Constraint table filled | Pass, three rows under REFERENCE |
| 3 | No @Valid wiring claimed | Pass, under STEP 3 |
| 4 | Correlation placement noted | Pass, under STEP 4 |
