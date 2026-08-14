import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { CustomerList } from './CustomerList'
import { CustomerForm } from './CustomerForm'
import { seedCustomers } from '../data/seedCustomers'

describe('CustomerList', () => {
  it('renders fixture customers by name', () => {
    render(<CustomerList customers={seedCustomers} onEdit={() => {}} />)
    expect(screen.getByText(/Amina Khan/i)).toBeInTheDocument()
    expect(screen.getByText(/Ravi Singh/i)).toBeInTheDocument()
    expect(screen.getAllByRole('article')).toHaveLength(2)
  })

  it('shows the empty state for no customers', () => {
    render(<CustomerList customers={[]} onEdit={() => {}} />)
    expect(screen.getByText('No customers yet')).toBeInTheDocument()
    expect(screen.queryByRole('article')).not.toBeInTheDocument()
  })

  it('reports the selected customer', async () => {
    const user = userEvent.setup()
    const onEdit = vi.fn()
    render(<CustomerList customers={seedCustomers} onEdit={onEdit} />)
    const aminaCard = screen.getByTestId('card-CUS-1001')
    await user.click(within(aminaCard).getByRole('button', { name: 'Edit' }))
    expect(onEdit).toHaveBeenCalledWith('CUS-1001')
  })
})

describe('CustomerForm', () => {
  it('associates labels with inputs', () => {
    render(
      <CustomerForm
        draft={{ fullName: '', email: '', status: 'PROSPECT' }}
        onChange={() => {}}
        onSubmit={() => {}}
      />,
    )
    expect(screen.getByLabelText('Full name')).toBeInTheDocument()
    expect(screen.getByLabelText('Email')).toBeInTheDocument()
    expect(screen.getByLabelText('Status')).toBeInTheDocument()
  })
})
