import type { Customer } from '../types/customer'
import { StatusBadge } from './StatusBadge'

export function CustomerCard({
  customer,
  onEdit,
}: {
  customer: Customer
  onEdit: (customerId: string) => void
}) {
  const headingId = `customer-${customer.customerId}`
  return (
    <article
        className="card"
        aria-labelledby={headingId}
        data-testid={`card-${customer.customerId}`}>
        <h3 id={headingId}>{customer.fullName}</h3>
        <p><a href={`mailto:${customer.email}`}>{customer.email}</a></p>
        <StatusBadge status={customer.status} />
        <button onClick={() => onEdit(customer.customerId)}>Edit</button>
    </article>
  )
}
