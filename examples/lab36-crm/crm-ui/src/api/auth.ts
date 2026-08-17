import { request } from './http'

type LoginResponse = {
  accessToken: string
  tokenType: string
  username: string
  role: string
}

export const authApi = {
  login: (username: string, password: string): Promise<LoginResponse> =>
    request<LoginResponse>(
      '/api/auth/login',
      { method: 'POST', body: JSON.stringify({ username, password }) },
      { intercept401: false }
    ),
}
