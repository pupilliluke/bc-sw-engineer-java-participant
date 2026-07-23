import { tokenStore } from '../auth/tokenStore'

const apiBase = import.meta.env.VITE_API_BASE_URL as string

export async function http(path: string, init: RequestInit = {}): Promise<Response> {
  const url = `${apiBase}${path}`
  const headers = new Headers(init.headers)
  const token = tokenStore.get()
  // TODO: attach Authorization: Bearer only when url starts with apiBase (CRM API origin)
  if (token && url.startsWith(apiBase)) {
    headers.set('Authorization', `Bearer ${token}`)
  }
  return fetch(url, { ...init, headers })
}
