import { emitAuthEvent } from '../auth/authEvents'
import { tokenStore } from '../auth/tokenStore'
import { ApiError, ForbiddenError } from './ApiError'

const API_URL = import.meta.env.VITE_API_BASE_URL.replace(/\/$/, '')
const apiOrigin = new URL(API_URL).origin

export type RequestOptions = { intercept401?: boolean }

export async function request<T>(
  path: string,
  init: RequestInit = {},
  options: RequestOptions = {}
): Promise<T> {
  const { intercept401 = true } = options
  const url = new URL(path.startsWith('http') ? path : `${API_URL}${path}`)
  const headers = new Headers(init.headers)
  headers.set('Content-Type', 'application/json')

  const token = tokenStore.get()
  if (url.origin === apiOrigin) {
    headers.set('X-Correlation-Id', 'lab-request-001')
    if (token) headers.set('Authorization', `Bearer ${token}`)
  }

  let response: Response
  try {
    response = await fetch(url.toString(), { ...init, headers })
  } catch (err) {
    if (err instanceof DOMException && err.name === 'AbortError') {
      throw new ApiError('Request cancelled', 'abort')
    }
    throw new ApiError('Cannot reach the CRM service', 'network')
  }

  if (response.status === 401 && intercept401) {
    tokenStore.clear()
    emitAuthEvent('expired')
    throw new ApiError('Your session has ended. Sign in again.', 'http', 401)
  }
  if (response.status === 403) {
    throw new ForbiddenError('You do not have access to this customer data.')
  }
  if (!response.ok) throw await ApiError.from(response)
  if (response.status === 204) return undefined as T
  try {
    return (await response.json()) as T
  } catch {
    throw new ApiError('Invalid response from the CRM service', 'parse')
  }
}
