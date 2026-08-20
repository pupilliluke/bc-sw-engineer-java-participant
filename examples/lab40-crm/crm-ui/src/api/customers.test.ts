import { vi } from 'vitest'
import { ApiError } from './ApiError'
import { customersApi } from './customers'

const jsonResponse = (status: number, body: unknown) =>
  new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  })

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('customersApi response classes', () => {
  it('200 list maps api fields to ui fields', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(
        jsonResponse(200, [
          { id: 'CUS-1001', name: 'Amina Khan', email: 'amina.khan@example.com', status: 'ACTIVE' },
          { id: 'CUS-1002', name: 'Ravi Singh', email: 'ravi.singh@example.com', status: 'PROSPECT' },
        ])
      )
    )
    const rows = await customersApi.list()
    expect(rows).toHaveLength(2)
    expect(rows[0]).toEqual({
      customerId: 'CUS-1001',
      fullName: 'Amina Khan',
      email: 'amina.khan@example.com',
      status: 'ACTIVE',
    })
  })

  it('201 create sends correlation header and returns the mapped customer', async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      jsonResponse(201, {
        id: 'CUS-2001',
        name: 'Lena Cho',
        email: 'lena.cho@example.com',
        status: 'PROSPECT',
      })
    )
    vi.stubGlobal('fetch', fetchMock)
    const created = await customersApi.create('CUS-2001', {
      fullName: 'Lena Cho',
      email: 'lena.cho@example.com',
      status: 'PROSPECT',
    })
    expect(created.customerId).toBe('CUS-2001')
    expect(created.fullName).toBe('Lena Cho')
    const [url, init] = fetchMock.mock.calls[0]
    expect(String(url)).toMatch(/\/api\/customers$/)
    expect(init.method).toBe('POST')
    // lab 36 builds request headers as a Headers object, the origin check needs
    // set() rather than the plain object literal lab 35 passed to fetch.
    expect(init.headers.get('X-Correlation-Id')).toBe('lab-request-001')
    expect(JSON.parse(init.body).name).toBe('Lena Cho')
  })

  it('400 becomes ApiError http with fieldErrors', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(
        jsonResponse(400, {
          message: 'Validation failed',
          fieldErrors: { email: 'Enter a valid email' },
        })
      )
    )
    const err = await customersApi
      .create('CUS-2002', { fullName: 'Bo Li', email: 'bad', status: 'PROSPECT' })
      .catch((e) => e)
    expect(err).toBeInstanceOf(ApiError)
    expect(err.kind).toBe('http')
    expect(err.status).toBe(400)
    expect(err.fieldErrors).toEqual({ email: 'Enter a valid email' })
  })

  it('500 becomes ApiError http with the server message', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(jsonResponse(500, { message: 'boom' }))
    )
    const err = await customersApi.list().catch((e) => e)
    expect(err).toBeInstanceOf(ApiError)
    expect(err.kind).toBe('http')
    expect(err.status).toBe(500)
    expect(err.message).toBe('boom')
  })

  it('network failure becomes ApiError network with a safe message', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('Failed to fetch')))
    const err = await customersApi.list().catch((e) => e)
    expect(err).toBeInstanceOf(ApiError)
    expect(err.kind).toBe('network')
    expect(err.message).toBe('Cannot reach the CRM service')
  })

  it('abort becomes ApiError abort', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockRejectedValue(new DOMException('The operation was aborted.', 'AbortError'))
    )
    const err = await customersApi.list().catch((e) => e)
    expect(err).toBeInstanceOf(ApiError)
    expect(err.kind).toBe('abort')
  })
})
