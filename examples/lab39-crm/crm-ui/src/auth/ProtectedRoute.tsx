import type { ReactNode } from 'react'
import { LoadingPage } from '../pages/LoadingPage'
import { LoginPage } from '../pages/LoginPage'
import { useAuth } from './AuthContext'
import { blockedReturnPath } from './returnUrl'

// UX only. The Spring API rejects an anonymous or forbidden call on its own,
// this guard just keeps the browser from rendering a view that cannot load.
export function ProtectedRoute({ children }: { children: ReactNode }) {
  const { state } = useAuth()
  if (state.status === 'checking') return <LoadingPage />
  if (state.status === 'anonymous') {
    return <LoginPage expired={state.expired} returnPath={blockedReturnPath()} />
  }
  return <>{children}</>
}
