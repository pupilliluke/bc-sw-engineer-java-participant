import { tokenStore } from '../auth/tokenStore'

describe('token storage', () => {
  it('keeps token out of Web Storage', () => {
    tokenStore.set('secret-token')
    expect(localStorage.getItem('token')).toBeNull()
    expect(sessionStorage.getItem('token')).toBeNull()
    tokenStore.clear()
  })
})
