# Lab 33 — Props Sketch

## Reference

| Prop | Example |
| --- | --- |
| customerId | CUS-1001 |
| name | Amina Khan |
| status | ACTIVE |
| onSelect | () => void |

## Step 2 — Types

Write TypeScript-ish types: `status: 'ACTIVE' | 'SUSPENDED' | PROSPECT`.

## Step 3 — Children?

Decide whether `CustomerCard` takes `children` or only props — one sentence.

Can take both. These children may use the props passed to CustomerCard. 


## Step 4 — Anti-pattern

Note: do not pass the entire global store as one mega-prop.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab33-props-sketch.md`
- [ x ] Both customers exemplified
- [ x ] Status union drafted
- [ x ] Mega-prop anti-pattern noted

