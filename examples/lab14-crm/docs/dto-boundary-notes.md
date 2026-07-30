Lab 14, entity versus DTO boundary

Where the line is, and what crosses it.

THE LINE

Customer is the persistence shape. It is what the store holds and what grows when
JPA lands in a later lab. CustomerRequestDTO and CustomerResponseDTO are the
contract shape, and they only change when the contract changes.

CustomerMapper is the only class that imports both. That is deliberate, it means
the question "what does a client see" has exactly one answer to read, and a field
added to Customer cannot reach a consumer unless somebody edits the mapper.

WHAT DOES NOT CROSS

phone. It is on the request, on the entity, and not on the response. Nothing
about phone is secret, it is here because a project needs one honest example of a
field that is accepted and deliberately not echoed, otherwise the no-leak rule
stays theoretical until the first real secret arrives.

createdAt and updatedAt in the inbound direction. toEntity leaves both null and
CustomerService stamps them, so a client cannot backdate a record by sending a
timestamp. They cross outbound only.

CustomerStatus itself. The response publishes the enum name as text. Consumers
depend on the four strings, not on a Java type they cannot import.

Later, and written down now so it is not rediscovered, credential material,
internal risk flags, audit columns beyond the two timestamps, and any storage key
that is not customerId.

WHY THE ENTITY IS NOT THE PAYLOAD

Lab 13 is the argument. CustomerType in contracts/customer.xsd returns all six
Customer fields because it was built from the entity's field list rather than from
what a billing partner needs. Nothing went wrong, and that is the problem, the
contract acquired a field without anyone deciding it should.

With a DTO in the way, that same mistake requires editing the mapper and the
README table, and CustomerMapperTest fails until both are done.

DIRECTION IS NOT SYMMETRIC

The two mapping directions carry different fields and enforce different rules,
so they are two methods rather than one with a flag.

Inbound, the client controls customerId, fullName, email, phone and status. It
does not control the timestamps.

Outbound, everything is server-controlled by definition, it came out of the
store. The only question left is which subset to publish.

WHAT CHANGES UNDER SPRING

The trigger, and nothing else. @Valid on a controller parameter calls the same
Validator against the same annotations. The DTOs, the mapper and the two-layer
split survive the move unchanged, which is why they were worth writing by hand
once.
