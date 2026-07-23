import { useState } from 'react'
import { useAuth } from '../auth/AuthContext'

export function LoginPage() {
  const { login } = useAuth()
  const [token, setToken] = useState('lab-demo-token')

  return (
    <form
      onSubmit={(e) => {
        e.preventDefault()
        // TODO: call login(token) — demo only; real apps POST /login
        login(token)
      }}
    >
      <h1>Sign in</h1>
      <label htmlFor="token">Access token</label>
      <input id="token" value={token} onChange={(e) => setToken(e.target.value)} />
      <button type="submit">Sign in</button>
    </form>
  )
}
