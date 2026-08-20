export type AuthEvent = 'expired'

type Listener = (event: AuthEvent) => void

const listeners = new Set<Listener>()

export function onAuthEvent(listener: Listener): () => void {
  listeners.add(listener)
  return () => {
    listeners.delete(listener)
  }
}

export function emitAuthEvent(event: AuthEvent) {
  listeners.forEach((listener) => listener(event))
}
