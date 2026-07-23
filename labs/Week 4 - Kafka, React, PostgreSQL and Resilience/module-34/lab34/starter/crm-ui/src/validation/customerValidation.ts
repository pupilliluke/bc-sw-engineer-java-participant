import type { CustomerDraft } from '../types/customer'

export type FieldErrors = Partial<Record<keyof CustomerDraft, string>>

export function validateCustomerDraft(draft: CustomerDraft): FieldErrors {
  const errors: FieldErrors = {}
  // TODO: require fullName; basic email check; status required
  return errors
}
