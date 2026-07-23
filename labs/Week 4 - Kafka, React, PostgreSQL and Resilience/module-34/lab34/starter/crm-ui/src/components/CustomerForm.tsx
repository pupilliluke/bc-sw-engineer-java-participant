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
  onChange: (next: CustomerDraft) => void
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
        value={draft.fullName}
        onChange={(e) => onChange({ ...draft, fullName: e.target.value })}
      />
      {errors.fullName && <p role="alert">{errors.fullName}</p>}
      {/* TODO: email + status fields with labels/errors */}
      <button type="submit" disabled={saving}>
        Save
      </button>
      <button type="button" onClick={onCancel}>
        Cancel
      </button>
    </form>
  )
}
