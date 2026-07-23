import type { ReactNode } from 'react'
import { useAuth } from './AuthContext'
import { LoginPage } from '../pages/LoginPage'

export function ProtectedRoute({ children }: { children: ReactNode }) {
  const { isAuthenticated } = useAuth()
  // TODO: if !isAuthenticated show LoginPage (or redirect)
  if (!isAuthenticated) return <LoginPage />
  return <>{children}</>
}
