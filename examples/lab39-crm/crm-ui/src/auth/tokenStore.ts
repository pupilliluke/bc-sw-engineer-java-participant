export type SessionUser = { username: string; role: string }

let session: { accessToken: string; user: SessionUser } | null = null

export const tokenStore = {
  get: (): string | null => session?.accessToken ?? null,
  getUser: (): SessionUser | null => session?.user ?? null,
  set: (accessToken: string, user: SessionUser) => {
    session = { accessToken, user }
  },
  clear: () => {
    session = null
  },
}
