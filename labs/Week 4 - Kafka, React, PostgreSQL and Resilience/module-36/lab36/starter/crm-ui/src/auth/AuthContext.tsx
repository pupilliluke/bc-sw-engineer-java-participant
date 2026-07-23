import { createContext, useContext, useMemo, useState, type ReactNode } from 'react'
import { tokenStore } from './tokenStore'

type AuthState = {
  isAuthenticated: boolean
  login: (token: string) => void
  logout: () => void
}

const AuthContext = createContext<AuthState | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState(!!tokenStore.get())

  const value = useMemo<AuthState>(
    () => ({
      isAuthenticated,
      login: (token: string) => {
        // TODO: tokenStore.set(token); setIsAuthenticated(true)
        throw new Error('TODO: login')
      },
      logout: () => {
        // TODO: tokenStore.clear(); setIsAuthenticated(false)
        throw new Error('TODO: logout')
      },
    }),
    [isAuthenticated],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth requires AuthProvider')
  return ctx
}
