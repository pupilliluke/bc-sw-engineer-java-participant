import { ApiError } from './ApiError'

const API_URL = import.meta.env.VITE_API_BASE_URL.replace(/\/$/, '')

export async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  let response: Response
  try {
    response = await fetch(`${API_URL}${path}`, {
      ...init,
      headers: {
        'Content-Type': 'application/json',
        'X-Correlation-Id': 'lab-request-001',
        ...init.headers,
      },
    })
  } catch (err) {
    if (err instanceof DOMException && err.name === 'AbortError') {
      throw new ApiError('Request cancelled', 'abort')
    }
    throw new ApiError('Cannot reach the CRM service', 'network')
  }
  if (!response.ok) throw await ApiError.from(response)
  if (response.status === 204) return undefined as T
  try {
    return (await response.json()) as T
  } catch {
    throw new ApiError('Invalid response from the CRM service', 'parse')
  }
}
