import { useCustomers } from './hooks/useCustomers'

export default function App() {
  const { customers, loading, error } = useCustomers()
  if (loading) return <p role="status">Loading…</p>
  if (error) return <p role="alert">{error}</p>
  return (
    <main>
      <h1>Customer Management Platform</h1>
      <ul>
        {customers.map((c) => (
          <li key={c.customerId}>
            {c.customerId} — {c.fullName}
          </li>
        ))}
      </ul>
    </main>
  )
}
