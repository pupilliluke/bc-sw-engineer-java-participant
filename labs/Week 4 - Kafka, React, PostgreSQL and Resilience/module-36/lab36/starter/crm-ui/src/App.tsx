import { ProtectedRoute } from './auth/ProtectedRoute'
import { useAuth } from './auth/AuthContext'

function Dashboard() {
  const { logout } = useAuth()
  return (
    <main>
      <h1>Customer Management Platform</h1>
      <button type="button" onClick={logout}>
        Sign out
      </button>
    </main>
  )
}

export default function App() {
  return (
    <ProtectedRoute>
      <Dashboard />
    </ProtectedRoute>
  )
}
