import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import App from './App'

describe('App flows', () => {
  it('shows seed customers', () => {
    render(<App />)
    expect(screen.getByText(/Amina Khan/i)).toBeInTheDocument()
  })

  it('opens create form', async () => {
    const user = userEvent.setup()
    render(<App />)
    await user.click(screen.getByRole('button', { name: /new customer/i }))
    // TODO: assert form fields visible
    expect(screen.getByLabelText(/full name/i)).toBeInTheDocument()
  })
})
