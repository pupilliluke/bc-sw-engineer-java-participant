import { render, screen } from '@testing-library/react'
import { CustomerList } from './CustomerList'
import { seedCustomers } from '../data/seedCustomers'

describe('CustomerList', () => {
  it('renders fixture customers by name', () => {
    render(<CustomerList customers={seedCustomers} onEdit={() => {}} />)
    // TODO: assert Amina / Ravi visible (getByText or getByRole)
    expect(screen.getByText(/Amina Khan/i)).toBeInTheDocument()
  })
})
