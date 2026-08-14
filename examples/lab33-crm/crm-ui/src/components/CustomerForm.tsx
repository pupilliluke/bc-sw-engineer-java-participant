import type { CustomerDraft } from '../types/customer'
import type { CustomerStatus } from '../types/customer'

export function CustomerForm({
  draft,
  onChange,
  onSubmit,
}: {
  draft: CustomerDraft
  onChange: (next: CustomerDraft) => void
  onSubmit: () => void
}) {
  return (
    <form
      onSubmit={(e) => {
        e.preventDefault()
        onSubmit()
      }}
    >
        <label htmlFor="fullName">Full name</label>
        <input
            id="fullName"
            type="text"
            value={draft.fullName}
            onChange={(e) => onChange({
                ...draft, fullName: e.target.value
            })}/>
        <label htmlFor="email">Email</label>
        <input
            id="email"
            type="email"
            value={draft.email}
            onChange={(e) => onChange({
                ...draft, email: e.target.value
            })}/>
        <label htmlFor="status">Status</label>
        <select
            id="status"
            value={draft.status}
            onChange={(e) => onChange({
                ...draft, status: e.target.value as CustomerStatus
            })}>
            <option value="PROSPECT">Prospect</option>
            <option value="ACTIVE">Active</option>
            <option value="CLOSED">Closed</option>
        </select>
      <button type="submit">Save</button>
    </form>
  )
}
