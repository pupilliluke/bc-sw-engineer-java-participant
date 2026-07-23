import { ApiError } from './ApiError'

const baseUrl = import.meta.env.VITE_API_BASE_URL as string

export async function http<T>(
  path: string,
  init: RequestInit = {},
  signal?: AbortSignal,
): Promise<T> {
  // TODO: fetch(`${baseUrl}${path}`) with signal; map failures to ApiError
  // TODO: never attach secrets in query string
  throw new ApiError('TODO: implement http helper', 'network')
}
