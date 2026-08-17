import type { Customer, CustomerDraft } from '../types/customer'
import type { FieldErrors } from '../validation/customerValidation'
import { request } from './http'

type ApiCustomer = {
  id: string
  name: string
  email: string
  status: Customer['status']
}

function fromApi(api: ApiCustomer): Customer {
  return {
    customerId: api.id,
    fullName: api.name,
    email: api.email,
    status: api.status,
  }
}

function toApi(customerId: string, draft: CustomerDraft): ApiCustomer {
  return {
    id: customerId,
    name: draft.fullName,
    email: draft.email,
    status: draft.status,
  }
}

export function mapApiFieldErrors(fieldErrors: Record<string, string>): FieldErrors {
  const mapped: FieldErrors = {}
  if (fieldErrors.name) mapped.fullName = fieldErrors.name
  if (fieldErrors.email) mapped.email = fieldErrors.email
  if (fieldErrors.status) mapped.status = fieldErrors.status
  return mapped
}

export const customersApi = {
  list: async (signal?: AbortSignal): Promise<Customer[]> => {
    const rows = await request<ApiCustomer[]>('/api/customers', { signal })
    return rows.map(fromApi)
  },
  create: async (customerId: string, draft: CustomerDraft): Promise<Customer> => {
    const created = await request<ApiCustomer>('/api/customers', {
      method: 'POST',
      body: JSON.stringify(toApi(customerId, draft)),
    })
    return fromApi(created)
  },
  update: async (customerId: string, draft: CustomerDraft): Promise<Customer> => {
    const updated = await request<ApiCustomer>(
      `/api/customers/${encodeURIComponent(customerId)}`,
      {
        method: 'PUT',
        body: JSON.stringify(toApi(customerId, draft)),
      }
    )
    return fromApi(updated)
  },
}
