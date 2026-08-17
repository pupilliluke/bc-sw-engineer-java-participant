import type { Customer } from '../types/customer'

// Every field is a text child, JSX escapes it. No dangerouslySetInnerHTML here
// or anywhere else in the UI, a customer name is data and never markup.
export function CustomerCard({
  customer,
  onEdit,
}: {
  customer: Customer
  onEdit: () => void
}) {
  return (
    <li>
      <span>{customer.fullName}</span>{' '}
      <span>{customer.customerId}</span>{' '}
      <span>{customer.status}</span>{' '}
      <button type="button" onClick={onEdit}>
        Edit
      </button>
    </li>
  )
}
