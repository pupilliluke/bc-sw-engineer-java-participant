import type { Customer } from '../types/customer'
import { http } from './http'

export const customersApi = {
  list(signal?: AbortSignal): Promise<Customer[]> {
    // TODO: GET /api/customers
    return http<Customer[]>('/api/customers', {}, signal)
  },
  get(customerId: string, signal?: AbortSignal): Promise<Customer> {
    // TODO: GET /api/customers/{id}
    return http<Customer>(`/api/customers/${customerId}`, {}, signal)
  },
}
