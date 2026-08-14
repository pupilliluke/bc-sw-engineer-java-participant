# Lab 33 — JSX on Paper

## Step 1 — Tree

Sketch `<CustomerList>` containing two `<CustomerCard>` nodes.

<CustomerList>
    <CustomerCard customerId="CUS-1001">
        <h2>Amina</h2>
        <StatusBadge status="ACTIVE" />
    </CustomerCard>
    <CustomerCard customerId="CUS-1002" >
        <h2>Brian</h2>
        <StatusBadge status="INACTIVE" />
    </CustomerCard>

</CustomerList>

## Step 2 — Keys

Write why `key={customerId}` should be `CUS-1001`, not array index.

Because we did not use an array, the array index is not a key.
The key is the value of the `customerId` prop.

## Step 3 — Badge

Nest `<StatusBadge status="ACTIVE" />` inside Amina's card.

## Step 4 — No runtime

Do not create a Vite app in this exercise.


## Pass criteria

Self-check before marking Pass:

- [ ] File exists at `notes/lab33-jsx-paper.md`
- [ ] Two cards sketched
- [ ] Key rationale written
- [ ] StatusBadge nested
