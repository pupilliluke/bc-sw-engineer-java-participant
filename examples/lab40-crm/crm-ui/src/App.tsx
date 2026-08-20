import { AuthProvider } from './auth/AuthContext'
import { ProtectedRoute } from './auth/ProtectedRoute'
import { CustomerWorkspace } from './pages/CustomerWorkspace'

export default function App() {
  return (
    <AuthProvider>
      <ProtectedRoute>
        <CustomerWorkspace />
      </ProtectedRoute>
    </AuthProvider>
  )
}
