# Lab 39 — Repository Sketch

## Step 1 — CustomerRepository

```java
interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {
    Optional<CustomerEntity> findByPublicId(String publicId);
    boolean existsByEmailNormalized(String emailNormalized);
    Optional<CustomerEntity> findByEmailNormalized(String emailNormalized);
    Page<CustomerEntity> findByStatus(String status, Pageable pageable);
}
```

the exercise sketch types this `JpaRepository<CustomerEntity, String>`
with `findById(String customerId)`. the deck's own repository slide types
it `<CustomerEntity, Long>` and reaches `CUS-1001` through
`findByPublicId(String publicId)`, and that is the one that matches the
lab 37 schema, where customer_id is the BIGINT identity and public_id
holds the CUS- value. so the ID type parameter is Long, `findById` takes
the surrogate, and every lookup by `CUS-1001` goes through
`findByPublicId`. same split as lab39-jpa.md, carried into the interface.

`findAll(Pageable)` is inherited from JpaRepository, nothing to declare.
it is on the list because the troubleshooting row warns about unbounded
findAll, and the version without a Pageable is also inherited and is the
one to avoid, 50,002 rows into a list is the failure mode.

the four methods above are the four access patterns from lab38-perf.md,
one to one. `findByEmailNormalized` is the hot path, `findByStatus` with
a Pageable is the ACTIVE list, and both ride indexes lab 38 measured.
`existsByEmailNormalized` is the duplicate check before an insert, which
is a cheaper question than fetching the row when the answer is only
yes or no.

## Step 2 — AccountRepository

```java
interface AccountRepository extends JpaRepository<AccountEntity, Long> {
    List<AccountEntity> findByCustomer_CustomerId(Long customerId);
    List<AccountEntity> findByCustomer_PublicId(String publicId);
}
```

the exercise sketches `findByCustomerId(String customerId)`, a plain
column field. mapping account with `@ManyToOne` to CustomerEntity in
lab39-jpa.md changes what the derived name has to say, there is no
`customerId` field on AccountEntity any more, there is a `customer`
reference, so the property path traverses it. the underscore is what
makes that explicit rather than leaving Spring Data to guess where
`findByCustomerId` splits.

both forms are useful. the first takes the surrogate a caller already
holds after loading the customer, the second takes `CUS-1001` straight
from a URL and joins, which is one query instead of two.

this is the join lab 38 measured, nested loop through
`ix_account_customer` at 6 buffers for one customer. Amina returns one
account, Ravi returns an empty list, and an empty list is the reason the
return type is List rather than Optional or a bare entity.

## Step 3 — Derived vs @Query

derived while the predicate is short and reads as a sentence. `@Query`
once the name stops being shorter than the JPQL it stands for, or once
the query needs something a method name cannot say.

| Case | Which | Why |
| --- | --- | --- |
| one column, equality | derived | `findByPublicId` is the whole story |
| two or three columns | derived, up to a point | `findByStatusAndCurrency` is still readable |
| four or more, mixed operators | `@Query` | the name becomes a worse spelling of the JPQL |
| fetch the accounts with the customer | `@Query` with JOIN FETCH | a derived name cannot express fetching |
| keyset paging | `@Query` | needs the row-value seek |

the keyset row is the one with evidence behind it. lab 38 measured the
paging seek as `(created_at, customer_id) < (:ts, :id)`, 23 buffers at
position 5,000 against 5,047 for the OR form, and a derived method name
has no way to write a row-value comparison. so deep paging in this
application is a `@Query`, not because the name would be long but
because the fast form cannot be spelled as a name at all.

## Step 4 — Service boundary

controllers talk to services, services use repositories. a controller
that injects a repository has skipped the layer where the transaction
and the business rule live, and a controller holding an EntityManager
has skipped two.

the repository interface is where the query lives and nothing else. no
business logic in it, because the interface has no body to put logic in
and the moment something needs a decision, a status transition rule, a
duplicate check turning into a 409, that decision belongs in the service
where it can be read, tested and wrapped in `@Transactional`. the
repository answers questions about rows; the service decides what the
answers mean.

this is the same seam labs 25 and 35 already use, and the reason the API
speaks its own shape rather than the entity's. lab 39 changes what sits
behind the repository, real PostgreSQL instead of an in-memory list, and
the layer above should not be able to tell.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab39-repository-sketch.md`
- [ x ] ≥3 customer methods
- [ x ] Account-by-customer method
- [ x ] Layering note
