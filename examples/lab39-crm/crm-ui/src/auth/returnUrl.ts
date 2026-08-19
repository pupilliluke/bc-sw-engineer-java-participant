export function safeReturnPath(candidate: string | null | undefined, fallback = '/'): string {
  if (!candidate) return fallback
  if (!candidate.startsWith('/')) return fallback
  if (candidate.startsWith('//')) return fallback
  if (candidate.includes('\\')) return fallback
  if (/[\r\n\t]/.test(candidate)) return fallback
  try {
    const url = new URL(candidate, window.location.origin)
    if (url.origin !== window.location.origin) return fallback
    return url.pathname + url.search
  } catch {
    return fallback
  }
}

// Where the user was heading when the guard stopped them. An explicit
// returnUrl wins if it survives the allowlist, otherwise the blocked path
// itself, so a deep link is preserved without trusting a query parameter.
export function blockedReturnPath(location = window.location): string {
  const requested = new URLSearchParams(location.search).get('returnUrl')
  if (requested !== null) return safeReturnPath(requested)
  return safeReturnPath(location.pathname + location.search)
}
