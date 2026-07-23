import { useState } from 'react'
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
  const [mode, setMode] = useState<UiMode>({ type: 'list' })
  const [draft, setDraft] = useState<CustomerDraft>(emptyDraft())
  const [saving, setSaving] = useState(false)
  const [errors, setErrors] = useState(validateCustomerDraft(emptyDraft()))

  function handleSubmit() {
    const nextErrors = validateCustomerDraft(draft)
    setErrors(nextErrors)
    if (Object.keys(nextErrors).length > 0) return
    // TODO: create → append with new CUS-id; edit → map update by customerId
    // TODO: setSaving true/false around the state update (soft disable)
    throw new Error('TODO: implement create/edit state update')
  }

  function handleCancel() {
    // TODO: discard draft and return to list mode
    setMode({ type: 'list' })
  }

  return (
    <main>
      <h1>Customer Management Platform</h1>
      <ul>
        {customers.map((c) => (
          <li key={c.customerId}>{c.fullName}</li>
        ))}
      </ul>
      {mode.type !== 'list' && (
        <CustomerForm
          draft={draft}
          errors={errors}
          saving={saving}
          onChange={setDraft}
          onSubmit={handleSubmit}
          onCancel={handleCancel}
        />
      )}
      <button type="button" onClick={() => setMode({ type: 'create' })}>
        New customer
      </button>
    </main>
  )
}
