import type { CustomerDraft } from '../types/customer'

export function CustomerForm({
  draft,
  onChange,
  onSubmit,
}: {
  draft: CustomerDraft
  onChange: (next: CustomerDraft) => void
  onSubmit: () => void
}) {
  // TODO: labeled inputs (htmlFor/id) for fullName, email, status; submit button
  return (
    <form
      onSubmit={(e) => {
        e.preventDefault()
        onSubmit()
      }}
    >
      {/* TODO */}
      <button type="submit">Save</button>
    </form>
  )
}
