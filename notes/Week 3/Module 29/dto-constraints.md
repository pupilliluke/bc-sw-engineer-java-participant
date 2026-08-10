# Lab 29 — DTO Constraint Plan

| Field | Constraints |
| --- | --- |
| fullName | @NotBlank, @Size(max = 100) |
| email | @NotBlank, @Email |
| customerId | @NotBlank, @Pattern(regexp = "CUS-\\d{4}") |
| status | @NotNull, allowed values ACTIVE and PROSPECT |

## How triggered
@Valid on the @RequestBody parameter of the create method. spring-boot-starter-validation
goes in pom.xml, it is what puts the validator on the classpath.

## Scope
Pre-lab only. Constraints go on the request DTO, not on the entity only.


## Debug / design challenge

What happens if annotations exist but @Valid is missing?

nothing runs. the body binds, no MethodArgumentNotValidException is thrown and a
blank name reaches the service.

## Predict the Output / Behavior

Should uniqueness of CUS-1001 be a Bean Validation annotation or a service rule?

service rule. the validator sees one object and cannot read the store, so
duplicate is a 409 from the service.


## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/dto-constraints.md`
- [ x ] Three fields
- [ x ] Trigger noted
