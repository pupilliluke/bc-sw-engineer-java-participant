import type { CustomerDraft } from '../types/customer'

export type FieldErrors = Partial<Record<keyof CustomerDraft, string>>

export function validateCustomerDraft(draft: CustomerDraft): FieldErrors {
  const errors: FieldErrors = {}
  if (!draft.fullName.trim()) {
    errors.fullName = 'Name is required'
  } else if (draft.fullName.trim().length < 2) {
    errors.fullName = 'Name must be at least 2 characters'
  }
  if (!/^\S+@\S+\.\S+$/.test(draft.email)) {
    errors.email = 'Enter a valid email'
  }
  if (!draft.status) {
    errors.status = 'Choose a status'
  }
  return errors
}
