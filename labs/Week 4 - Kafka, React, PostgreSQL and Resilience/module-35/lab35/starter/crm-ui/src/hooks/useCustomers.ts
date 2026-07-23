import { useEffect, useState } from 'react'
import { customersApi } from '../api/customers'
import { ApiError } from '../api/ApiError'
import type { Customer } from '../types/customer'

export function useCustomers() {
  const [customers, setCustomers] = useState<Customer[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const ac = new AbortController()
    setLoading(true)
    customersApi
      .list(ac.signal)
      .then(setCustomers)
      .catch((e) => {
        if (e instanceof ApiError && e.kind === 'abort') return
        setError(e instanceof Error ? e.message : 'Unknown error')
      })
      .finally(() => setLoading(false))
    return () => ac.abort()
  }, [])

  return { customers, loading, error }
}
