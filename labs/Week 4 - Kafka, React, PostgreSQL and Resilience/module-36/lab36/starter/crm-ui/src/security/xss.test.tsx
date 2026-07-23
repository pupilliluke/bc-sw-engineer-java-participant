import { render, screen } from '@testing-library/react'

describe('XSS posture', () => {
  it('renders untrusted text as text, not HTML', () => {
    const untrusted = '<img src=x onerror=alert(1) />'
    render(<p>{untrusted}</p>)
    // TODO: assert text content equals untrusted string; no img role
    expect(screen.getByText(untrusted)).toBeInTheDocument()
  })
})
