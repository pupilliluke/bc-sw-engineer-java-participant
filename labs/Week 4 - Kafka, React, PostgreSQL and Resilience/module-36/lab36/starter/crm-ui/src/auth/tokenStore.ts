/** In-memory token only — never localStorage/sessionStorage for timed path. */
let accessToken: string | null = null

export const tokenStore = {
  get(): string | null {
    return accessToken
  },
  set(token: string | null) {
    // TODO: assign accessToken; do not write to Web Storage
    accessToken = token
  },
  clear() {
    accessToken = null
  },
}
