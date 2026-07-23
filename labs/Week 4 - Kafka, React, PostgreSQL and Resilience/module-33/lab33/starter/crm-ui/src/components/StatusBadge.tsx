import type { CustomerStatus } from '../types/customer'

export function StatusBadge({ status }: { status: CustomerStatus }) {
  // TODO: render accessible status text (role or aria-label); style by status
  return <span className="badge">{/* TODO */ status}</span>
}
