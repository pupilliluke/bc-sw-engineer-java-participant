import type { ChangeEvent } from 'react'
import type { CustomerDraft } from '../types/customer'
import type { FieldErrors } from '../validation/customerValidation'

export function CustomerForm({
  draft,
  errors,
  saving,
  onChange,
  onSubmit,
  onCancel,
}: {
  draft: CustomerDraft
  errors: FieldErrors
  saving: boolean
  onChange: (event: ChangeEvent<HTMLInputElement | HTMLSelectElement>) => void
  onSubmit: () => void
  onCancel: () => void
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
        name="fullName"
        value={draft.fullName}
        onChange={onChange}
      />
      {errors.fullName && <p role="alert">{errors.fullName}</p>}
      <label htmlFor="email">Email</label>
      <input
        id="email"
        name="email"
        value={draft.email}
        onChange={onChange}
      />
      {errors.email && <p role="alert">{errors.email}</p>}
      <label htmlFor="status">Status</label>
      <select
        id="status"
        name="status"
        value={draft.status}
        onChange={onChange}
      >
        <option value="PROSPECT">PROSPECT</option>
        <option value="ACTIVE">ACTIVE</option>
        <option value="CLOSED">CLOSED</option>
      </select>
      {errors.status && <p role="alert">{errors.status}</p>}
      <button type="submit" disabled={saving}>
        Save
      </button>
      <button type="button" onClick={onCancel}>
        Cancel
      </button>
    </form>
  )
}
