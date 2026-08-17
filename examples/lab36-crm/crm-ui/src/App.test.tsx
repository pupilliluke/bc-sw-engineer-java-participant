import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { vi } from 'vitest'
import App from './App'
import { tokenStore } from './auth/tokenStore'

const seeds = [
  { id: 'CUS-1001', name: 'Amina Khan', email: 'amina.khan@example.com', status: 'ACTIVE' },
  { id: 'CUS-1002', name: 'Ravi Singh', email: 'ravi.singh@example.com', status: 'PROSPECT' },
]

const jsonResponse = (status: number, body: unknown) =>
  new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  })

function stubApi(listBody: unknown = seeds) {
  vi.stubGlobal(
    'fetch',
    vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
      const url = String(input)
      const method = init?.method ?? 'GET'
      if (method === 'GET' && url.endsWith('/api/customers')) {
        return jsonResponse(200, listBody)
      }
      if (method === 'POST' && url.endsWith('/api/customers')) {
        return jsonResponse(201, JSON.parse(String(init?.body)))
      }
      if (method === 'PUT') {
        return jsonResponse(200, JSON.parse(String(init?.body)))
      }
      throw new TypeError('unmocked request: ' + method + ' ' + url)
    })
  )
}

beforeEach(() => {
  // Lab 36 puts the workspace behind ProtectedRoute, so these flows start from
  // an in-memory session. lab-token-001 is a fake fixture, not a real token.
  tokenStore.set('lab-token-001', { username: 'agent1', role: 'AGENT' })
})

afterEach(() => {
  tokenStore.clear()
  vi.unstubAllGlobals()
})

describe('App flows', () => {
  it('loads seed customers from the api', async () => {
    stubApi()
    render(<App />)
    expect(screen.getByRole('status')).toBeInTheDocument()
    expect(await screen.findByText(/Amina Khan/i)).toBeInTheDocument()
    expect(screen.getByText(/Ravi Singh/i)).toBeInTheDocument()
    expect(screen.queryByRole('status')).not.toBeInTheDocument()
  })

  it('shows the empty state when the api returns []', async () => {
    stubApi([])
    render(<App />)
    expect(await screen.findByText(/no customers yet/i)).toBeInTheDocument()
  })

  it('shows the error state with retry when the load fails', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('Failed to fetch')))
    render(<App />)
    const alert = await screen.findByRole('alert')
    expect(alert).toHaveTextContent('Cannot reach the CRM service')
    stubApi()
    await userEvent.setup().click(within(alert).getByRole('button', { name: /retry/i }))
    expect(await screen.findByText(/Amina Khan/i)).toBeInTheDocument()
  })

  it('search amina leaves one card', async () => {
    stubApi()
    const user = userEvent.setup()
    render(<App />)
    await screen.findByText(/Amina Khan/i)
    await user.type(screen.getByLabelText(/search customers/i), 'amina')
    expect(screen.getByText(/Amina Khan/i)).toBeInTheDocument()
    expect(screen.queryByText(/Ravi Singh/i)).not.toBeInTheDocument()
  })

  it('search miss shows the not-found empty state', async () => {
    stubApi()
    const user = userEvent.setup()
    render(<App />)
    await screen.findByText(/Amina Khan/i)
    await user.type(screen.getByLabelText(/search customers/i), 'missing')
    expect(screen.getByText(/no customers found/i)).toBeInTheDocument()
  })

  it('creates a valid customer exactly once via POST', async () => {
    stubApi()
    const user = userEvent.setup()
    render(<App />)
    await screen.findByText(/Amina Khan/i)
    await user.click(screen.getByRole('button', { name: /new customer/i }))
    await user.type(screen.getByLabelText(/full name/i), 'Lena Cho')
    await user.type(screen.getByLabelText(/email/i), 'lena.cho@example.com')
    await user.click(screen.getByRole('button', { name: /save/i }))
    expect(await screen.findAllByText(/Lena Cho/i)).toHaveLength(1)
    expect(screen.queryByLabelText(/full name/i)).not.toBeInTheDocument()
  })

  it('client validation blocks an invalid create with no POST', async () => {
    stubApi()
    const user = userEvent.setup()
    render(<App />)
    await screen.findByText(/Amina Khan/i)
    const fetchMock = fetch as ReturnType<typeof vi.fn>
    const callsAfterLoad = fetchMock.mock.calls.length
    await user.click(screen.getByRole('button', { name: /new customer/i }))
    await user.click(screen.getByRole('button', { name: /save/i }))
    expect(screen.getByText('Name is required')).toBeInTheDocument()
    expect(screen.getByText('Enter a valid email')).toBeInTheDocument()
    expect(fetchMock.mock.calls.length).toBe(callsAfterLoad)
  })

  it('maps a backend 400 fieldErrors body onto the form', async () => {
    stubApi()
    const user = userEvent.setup()
    render(<App />)
    await screen.findByText(/Amina Khan/i)
    const fetchMock = fetch as ReturnType<typeof vi.fn>
    fetchMock.mockImplementationOnce(async () =>
      jsonResponse(400, {
        message: 'Validation failed',
        fieldErrors: { email: 'Enter a valid email' },
      })
    )
    await user.click(screen.getByRole('button', { name: /new customer/i }))
    await user.type(screen.getByLabelText(/full name/i), 'Bo Li')
    await user.type(screen.getByLabelText(/email/i), 'bo.li@example.com')
    await user.click(screen.getByRole('button', { name: /save/i }))
    expect(await screen.findByText('Enter a valid email')).toBeInTheDocument()
    expect(screen.getByLabelText(/full name/i)).toBeInTheDocument()
  })

  it('edits Ravi through PUT and shows the updated name', async () => {
    stubApi()
    const user = userEvent.setup()
    render(<App />)
    const raviItem = (await screen.findByText(/Ravi Singh/i)).closest('li') as HTMLElement
    await user.click(within(raviItem).getByRole('button', { name: /edit/i }))
    const nameInput = screen.getByLabelText(/full name/i)
    expect(nameInput).toHaveValue('Ravi Singh')
    await user.clear(nameInput)
    await user.type(nameInput, 'Ravi Kumar')
    await user.click(screen.getByRole('button', { name: /save/i }))
    expect(await screen.findByText(/Ravi Kumar/i)).toBeInTheDocument()
    expect(screen.queryByText(/Ravi Singh/i)).not.toBeInTheDocument()
    expect(screen.getByText(/Amina Khan/i)).toBeInTheDocument()
  })

  it('cancel discards a typed draft', async () => {
    stubApi()
    const user = userEvent.setup()
    render(<App />)
    await screen.findByText(/Amina Khan/i)
    await user.click(screen.getByRole('button', { name: /new customer/i }))
    await user.type(screen.getByLabelText(/full name/i), 'Temp Person')
    await user.click(screen.getByRole('button', { name: /cancel/i }))
    expect(screen.queryByText(/Temp Person/i)).not.toBeInTheDocument()
    expect(screen.getAllByRole('listitem')).toHaveLength(2)
  })
})
