import { useEffect, useState } from 'react'
import type { ChangeEvent } from 'react'
import { CustomerForm } from './components/CustomerForm'
import { seedCustomers } from './data/seedCustomers'
import type { Customer, CustomerDraft, UiMode } from './types/customer'
import { validateCustomerDraft } from './validation/customerValidation'

const emptyDraft = (): CustomerDraft => ({
  fullName: '',
  email: '',
  status: 'PROSPECT',
})

export default function App() {
  const [customers, setCustomers] = useState<Customer[]>(seedCustomers)
  const [query, setQuery] = useState('')
  const [mode, setMode] = useState<UiMode>({ type: 'list' })
  const [draft, setDraft] = useState<CustomerDraft>(emptyDraft())
  const [saving, setSaving] = useState(false)
  const [errors, setErrors] = useState(validateCustomerDraft(emptyDraft()))




  function handleSubmit() {
    const nextErrors = validateCustomerDraft(draft)
    setErrors(nextErrors)
    if (Object.keys(nextErrors).length > 0) return
    setSaving(true)
    if (mode.type === 'edit') {
      const editingId = mode.customerId
      setCustomers((prev) =>
        prev.map((c) =>
          c.customerId === editingId ? { ...c, ...draft, customerId: c.customerId } : c
        )
      )
      console.log('update', 'lab-request-001')
    } else {
      setCustomers((prev) => [...prev, { ...draft, customerId: crypto.randomUUID() }])
      console.log('create', 'lab-request-001')
    }
    setSaving(false)
    setMode({ type: 'list' })
    setDraft(emptyDraft())
    setErrors({})
  }

  function handleCancel() {
    setMode({ type: 'list' })
    setDraft(emptyDraft())
    setErrors({})
    console.log('cancel', 'lab-request-001')
  }

  function handleFieldChange(event: ChangeEvent<HTMLInputElement | HTMLSelectElement>) {
    setDraft((prev) => ({ ...prev, [event.target.name]: event.target.value }));
    setErrors((prev) => {
        const next = { ...prev };
        delete next[event.target.name as keyof CustomerDraft];
        return next;
    });
  }

  const visible = customers.filter((c) =>
    [c.customerId, c.fullName, c.email].some((v) =>
      v.toLowerCase().includes(query.trim().toLowerCase())
    )
  )

  useEffect(() => {
    const original = document.title
    document.title = `CRM (${visible.length})`
    return () => {
      document.title = original
    }
  }, [visible.length])

  return (
    <main>
      <h1>Customer Management Platform</h1>
      <input
        type="search"
        aria-label="Search customers"
        value={query}
        onChange={(e) =>
            setQuery(e.target.value)}
      />
      {visible.length === 0 ? (
        <p>No customers found</p>
      ) : (
        <ul>
          {visible.map((c) => (
            <li key={c.customerId}>{c.fullName}

                <button
                    type="button"
                    onClick={() => {
                        setMode({ type: 'edit', customerId: c.customerId })
                        setDraft({ fullName: c.fullName, email: c.email, status: c.status })
                        setErrors({})
                    }}
                >
                    Edit
                </button>

            </li>
          ))}
        </ul>
      )}
      {mode.type !== 'list' && (
        <CustomerForm
          draft={draft}
          errors={errors}
          saving={saving}
          onChange={handleFieldChange}
          onSubmit={handleSubmit}
          onCancel={handleCancel}
        />
      )}
      <button
        type="button"
        onClick={() => {
          setMode({ type: 'create' })
          setDraft(emptyDraft())
          setErrors({})
        }}
      >
        New customer
      </button>
    </main>
  )
}
