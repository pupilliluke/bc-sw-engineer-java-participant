```tsx
type CustomerCardProps = {
  customerId: string;
  name: string;
  status: _____;
  onSelect: (id: string) => void;
};

export function CustomerCard({ customerId, name, status, onSelect }: CustomerCardProps) {
  return (
    <article aria-label={customerId}>
      <h3>{name}</h3>
      <StatusBadge status={status} />
      <button type="button" onClick={() => onSelect(customerId)}>View</button>
    </article>
  );
}
```



## Step 4 — A11y note

Write: button has visible text; avoid icon-only without aria-label.

## Pass criteria

Self-check before marking Pass:

- [ ] File exists at `notes/lab33-todos.md`
- [ ] All blanks filled
- [ ] Amina usage present
- [ ] A11y note written
