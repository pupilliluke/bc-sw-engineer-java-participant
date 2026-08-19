import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { vi } from 'vitest'
import App from '../App'
import { request } from '../api/http'
import { blockedReturnPath, safeReturnPath } from '../auth/returnUrl'
import { tokenStore } from '../auth/tokenStore'

const seeds = [
  { id: 'CUS-1001', name: 'Amina Khan', email: 'amina.khan@example.com', status: 'ACTIVE' },
  { id: 'CUS-1002', name: 'Ravi Singh', email: 'ravi.singh@example.com', status: 'PROSPECT' },
]

const loginBody = {
  accessToken: 'lab-token-001',
  tokenType: 'Bearer',
  username: 'agent1',
  role: 'AGENT',
}

const jsonResponse = (status: number, body: unknown) =>
  new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  })

type Routes = {
  login?: () => Promise<Response>
  list?: () => Promise<Response>
}

function stubApi(routes: Routes = {}) {
  const login = routes.login ?? (async () => jsonResponse(200, loginBody))
  const list = routes.list ?? (async () => jsonResponse(200, seeds))
  vi.stubGlobal(
    'fetch',
    vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
      const url = String(input)
      const method = init?.method ?? 'GET'
      if (method === 'POST' && url.endsWith('/api/auth/login')) return login()
      if (method === 'GET' && url.endsWith('/api/customers')) return list()
      return jsonResponse(200, {})
    })
  )
}

const fetchCalls = () => (fetch as ReturnType<typeof vi.fn>).mock.calls
const headerOf = (call: unknown[], name: string) =>
  ((call[1] as RequestInit).headers as Headers).get(name)
const crmListCall = () =>
  fetchCalls().find(
    (call) => String(call[0]).endsWith('/api/customers') && (call[1] as RequestInit).method === undefined
  )

async function signIn() {
  const user = userEvent.setup()
  render(<App />)
  await user.type(screen.getByLabelText(/username/i), 'agent1')
  await user.type(screen.getByLabelText(/password/i), 'agent1')
  await user.click(screen.getByRole('button', { name: /sign in/i }))
  return user
}

afterEach(() => {
  tokenStore.clear()
  localStorage.clear()
  sessionStorage.clear()
  vi.unstubAllGlobals()
})

describe('token discipline', () => {
  it('keeps the access token out of local and session storage', async () => {
    stubApi()
    await signIn()
    expect(await screen.findByText(/Amina Khan/i)).toBeInTheDocument()
    expect(tokenStore.get()).toBe('lab-token-001')
    expect(localStorage.length).toBe(0)
    expect(sessionStorage.length).toBe(0)
    expect(JSON.stringify({ ...localStorage, ...sessionStorage })).not.toContain('lab-token-001')
  })

  it('attaches the bearer token and correlation id to CRM API calls', async () => {
    stubApi()
    await signIn()
    await screen.findByText(/Amina Khan/i)
    const call = crmListCall()
    expect(call).toBeDefined()
    expect(headerOf(call!, 'Authorization')).toBe('Bearer lab-token-001')
    expect(headerOf(call!, 'X-Correlation-Id')).toBe('lab-request-001')
  })

  it('does not attach the bearer token to another origin', async () => {
    stubApi()
    tokenStore.set('lab-token-001', { username: 'agent1', role: 'AGENT' })
    await request('https://evil.example.com/steal')
    const call = fetchCalls().find((c) => String(c[0]).startsWith('https://evil.example.com'))
    expect(call).toBeDefined()
    expect(headerOf(call!, 'Authorization')).toBeNull()
    expect(headerOf(call!, 'X-Correlation-Id')).toBeNull()
  })
})

describe('401 and 403 are different', () => {
  it('401 clears the token and returns to the sign-in screen', async () => {
    stubApi({ list: async () => jsonResponse(401, { message: 'Unauthorized' }) })
    tokenStore.set('lab-token-001', { username: 'agent1', role: 'AGENT' })
    render(<App />)
    expect(await screen.findByText(/your session has ended/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument()
    expect(tokenStore.get()).toBeNull()
  })

  it('403 keeps the session and shows a forbidden message', async () => {
    stubApi({ list: async () => jsonResponse(403, { message: 'Forbidden' }) })
    tokenStore.set('lab-token-001', { username: 'agent1', role: 'AGENT' })
    render(<App />)
    const alert = await screen.findByRole('alert')
    expect(alert).toHaveTextContent(/do not have access/i)
    expect(screen.getByText(/signed in as agent1/i)).toBeInTheDocument()
    expect(tokenStore.get()).toBe('lab-token-001')
  })
})

describe('login and logout', () => {
  it('shows one generic message for bad credentials', async () => {
    stubApi({
      login: async () => jsonResponse(401, { message: 'Invalid credentials' }),
    })
    await signIn()
    expect(await screen.findByRole('alert')).toHaveTextContent('Invalid username or password')
    expect(screen.queryByText(/not found/i)).not.toBeInTheDocument()
    expect(tokenStore.get()).toBeNull()
  })

  it('disables the submit button while the login request is in flight', async () => {
    let release: (value: Response) => void = () => {}
    stubApi({
      login: () =>
        new Promise<Response>((resolve) => {
          release = resolve
        }),
    })
    const user = userEvent.setup()
    render(<App />)
    await user.type(screen.getByLabelText(/username/i), 'agent1')
    await user.type(screen.getByLabelText(/password/i), 'agent1')
    await user.click(screen.getByRole('button', { name: /sign in/i }))
    expect(screen.getByRole('button', { name: /sign in/i })).toBeDisabled()
    release(jsonResponse(200, loginBody))
    expect(await screen.findByText(/Amina Khan/i)).toBeInTheDocument()
  })

  it('sign out clears the token and the rendered customer data', async () => {
    stubApi()
    const user = await signIn()
    await screen.findByText(/Amina Khan/i)
    const callsBeforeLogout = fetchCalls().length
    await user.click(screen.getByRole('button', { name: /sign out/i }))
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument()
    expect(screen.queryByText(/Amina Khan/i)).not.toBeInTheDocument()
    expect(screen.queryByText(/CUS-1001/)).not.toBeInTheDocument()
    expect(tokenStore.get()).toBeNull()
    await user.type(screen.getByLabelText(/username/i), 'agent1')
    await user.type(screen.getByLabelText(/password/i), 'agent1')
    await user.click(screen.getByRole('button', { name: /sign in/i }))
    await screen.findByText(/Amina Khan/i)
    expect(fetchCalls().length).toBeGreaterThan(callsBeforeLogout)
  })
})

describe('open redirect', () => {
  it('rejects an external returnUrl and keeps internal paths', () => {
    expect(safeReturnPath('https://evil.example.com/steal')).toBe('/')
    expect(safeReturnPath('//evil.example.com')).toBe('/')
    expect(safeReturnPath('/\\evil.example.com')).toBe('/')
    expect(safeReturnPath('javascript:alert(1)')).toBe('/')
    expect(safeReturnPath(null)).toBe('/')
    expect(safeReturnPath('/customers?q=amina')).toBe('/customers?q=amina')
  })

  it('keeps the blocked deep link and drops an external returnUrl', () => {
    const deepLink = { pathname: '/customers/CUS-1001', search: '' } as Location
    expect(blockedReturnPath(deepLink)).toBe('/customers/CUS-1001')
    const hijacked = {
      pathname: '/customers/CUS-1001',
      search: '?returnUrl=https://evil.example.com/steal',
    } as Location
    expect(blockedReturnPath(hijacked)).toBe('/')
  })

  it('shows the blocked deep link on the sign-in screen', async () => {
    stubApi()
    window.history.pushState({}, '', '/customers/CUS-1002')
    try {
      render(<App />)
      expect(await screen.findByText(/return to \/customers\/CUS-1002/i)).toBeInTheDocument()
    } finally {
      window.history.pushState({}, '', '/')
    }
  })
})
