import { useEffect, useState } from 'react'
import type { ChangeEvent } from 'react'
import { ApiError } from './api/ApiError'
import { customersApi, mapApiFieldErrors } from './api/customers'
import { CustomerForm } from './components/CustomerForm'
import type { Customer, CustomerDraft, UiMode } from './types/customer'
import type { FieldErrors } from './validation/customerValidation'
import { validateCustomerDraft } from './validation/customerValidation'

const emptyDraft = (): CustomerDraft => ({
  fullName: '',
  email: '',
  status: 'PROSPECT',
})

type LoadState =
  | { kind: 'loading' }
  | { kind: 'data'; customers: Customer[] }
  | { kind: 'error'; message: string }

export default function App() {
  const [load, setLoad] = useState<LoadState>({ kind: 'loading' })
  const [reloadKey, setReloadKey] = useState(0)
  const [query, setQuery] = useState('')
  const [mode, setMode] = useState<UiMode>({ type: 'list' })
  const [draft, setDraft] = useState<CustomerDraft>(emptyDraft())
  const [saving, setSaving] = useState(false)
  const [errors, setErrors] = useState<FieldErrors>({})
  const [saveError, setSaveError] = useState('')

  useEffect(() => {
    const controller = new AbortController()
    let cancelled = false
    setLoad({ kind: 'loading' })
    customersApi
      .list(controller.signal)
      .then((data) => {
        if (!cancelled) setLoad({ kind: 'data', customers: data })
      })
      .catch((err) => {
        if (controller.signal.aborted) return
        if (!cancelled) {
          const message =
            err instanceof ApiError ? err.message : 'Something went wrong'
          setLoad({ kind: 'error', message })
        }
      })
    return () => {
      cancelled = true
      controller.abort()
    }
  }, [reloadKey])

  async function handleSubmit() {
    const nextErrors = validateCustomerDraft(draft)
    setErrors(nextErrors)
    if (Object.keys(nextErrors).length > 0) return
    setSaving(true)
    setSaveError('')
    try {
      if (mode.type === 'edit') {
        const updated = await customersApi.update(mode.customerId, draft)
        setLoad((prev) =>
          prev.kind === 'data'
            ? {
                kind: 'data',
                customers: prev.customers.map((c) =>
                  c.customerId === updated.customerId ? updated : c
                ),
              }
            : prev
        )
        console.log('update', 'lab-request-001')
      } else {
        const created = await customersApi.create(crypto.randomUUID(), draft)
        setLoad((prev) =>
          prev.kind === 'data'
            ? { kind: 'data', customers: [...prev.customers, created] }
            : prev
        )
        console.log('create', 'lab-request-001')
      }
      setMode({ type: 'list' })
      setDraft(emptyDraft())
      setErrors({})
    } catch (err) {
      if (err instanceof ApiError && err.status === 400 && err.fieldErrors) {
        setErrors(mapApiFieldErrors(err.fieldErrors))
      } else if (err instanceof ApiError && err.kind !== 'abort') {
        setSaveError(err.message)
      } else if (!(err instanceof ApiError)) {
        setSaveError('Something went wrong')
      }
    } finally {
      setSaving(false)
    }
  }

  function handleCancel() {
    setMode({ type: 'list' })
    setDraft(emptyDraft())
    setErrors({})
    setSaveError('')
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

  const customers = load.kind === 'data' ? load.customers : []
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
      {load.kind === 'loading' && <p role="status">Loading customers…</p>}
      {load.kind === 'error' && (
        <div role="alert">
          <p>{load.message}</p>
          <button type="button" onClick={() => setReloadKey((k) => k + 1)}>
            Retry
          </button>
        </div>
      )}
      {load.kind === 'data' &&
        (visible.length === 0 ? (
          customers.length === 0 ? (
            <p>No customers yet. Create your first customer to get started.</p>
          ) : (
            <p>No customers found</p>
          )
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
                        setSaveError('')
                    }}
                >
                    Edit
                </button>

              </li>
            ))}
          </ul>
        ))}
      {mode.type !== 'list' && (
        <>
          {saveError && <p role="alert">{saveError}</p>}
          <CustomerForm
            draft={draft}
            errors={errors}
            saving={saving}
            onChange={handleFieldChange}
            onSubmit={handleSubmit}
            onCancel={handleCancel}
          />
        </>
      )}
      <button
        type="button"
        onClick={() => {
          setMode({ type: 'create' })
          setDraft(emptyDraft())
          setErrors({})
          setSaveError('')
        }}
      >
        New customer
      </button>
    </main>
  )
}
