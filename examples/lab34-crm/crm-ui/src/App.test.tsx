import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import App from './App'

describe('App flows', () => {
  it('shows seed customers', () => {
    render(<App />)
    expect(screen.getByText(/Amina Khan/i)).toBeInTheDocument()
    expect(screen.getByText(/Ravi Singh/i)).toBeInTheDocument()
  })

  it('search amina leaves one card', async () => {
    const user = userEvent.setup()
    render(<App />)
    await user.type(screen.getByLabelText(/search customers/i), 'amina')
    expect(screen.getByText(/Amina Khan/i)).toBeInTheDocument()
    expect(screen.queryByText(/Ravi Singh/i)).not.toBeInTheDocument()
  })

  it('search miss shows the empty state', async () => {
    const user = userEvent.setup()
    render(<App />)
    await user.type(screen.getByLabelText(/search customers/i), 'missing')
    expect(screen.getByText(/no customers found/i)).toBeInTheDocument()
  })

  it('creates a valid customer exactly once', async () => {
    const user = userEvent.setup()
    render(<App />)
    await user.click(screen.getByRole('button', { name: /new customer/i }))
    await user.type(screen.getByLabelText(/full name/i), 'Lena Cho')
    await user.type(screen.getByLabelText(/email/i), 'lena.cho@example.com')
    await user.click(screen.getByRole('button', { name: /save/i }))
    expect(screen.getAllByText(/Lena Cho/i)).toHaveLength(1)
    expect(screen.queryByLabelText(/full name/i)).not.toBeInTheDocument()
  })

  it('invalid create shows field errors and leaves the list unchanged', async () => {
    const user = userEvent.setup()
    render(<App />)
    await user.click(screen.getByRole('button', { name: /new customer/i }))
    await user.click(screen.getByRole('button', { name: /save/i }))
    expect(screen.getByText('Name is required')).toBeInTheDocument()
    expect(screen.getByText('Enter a valid email')).toBeInTheDocument()
    expect(screen.getAllByRole('listitem')).toHaveLength(2)
  })

  it('edits Ravi and saves the updated name', async () => {
    const user = userEvent.setup()
    render(<App />)
    const raviItem = screen.getByText(/Ravi Singh/i)
    await user.click(within(raviItem).getByRole('button', { name: /edit/i }))
    const nameInput = screen.getByLabelText(/full name/i)
    expect(nameInput).toHaveValue('Ravi Singh')
    await user.clear(nameInput)
    await user.type(nameInput, 'Ravi Kumar')
    await user.click(screen.getByRole('button', { name: /save/i }))
    expect(screen.getByText(/Ravi Kumar/i)).toBeInTheDocument()
    expect(screen.queryByText(/Ravi Singh/i)).not.toBeInTheDocument()
    expect(screen.getByText(/Amina Khan/i)).toBeInTheDocument()
  })

  it('cancel discards a typed draft', async () => {
    const user = userEvent.setup()
    render(<App />)
    await user.click(screen.getByRole('button', { name: /new customer/i }))
    await user.type(screen.getByLabelText(/full name/i), 'Temp Person')
    await user.click(screen.getByRole('button', { name: /cancel/i }))
    expect(screen.queryByText(/Temp Person/i)).not.toBeInTheDocument()
    expect(screen.getAllByRole('listitem')).toHaveLength(2)
  })
})
