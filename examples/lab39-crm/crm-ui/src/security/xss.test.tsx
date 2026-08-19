import { render, screen } from '@testing-library/react'
import { CustomerCard } from '../components/CustomerCard'
import type { Customer } from '../types/customer'

const amina: Customer = {
  customerId: 'CUS-1001',
  fullName: 'Amina Khan',
  email: 'amina.khan@example.com',
  status: 'ACTIVE',
}

describe('customer text cannot execute', () => {
  it('renders a malicious fullName as literal text with no img node', () => {
    render(
      <CustomerCard
        customer={{ ...amina, fullName: '<img onerror=alert(1)>' }}
        onEdit={() => {}}
      />
    )
    expect(document.querySelector('img')).toBeNull()
    expect(screen.getByText(/<img onerror/)).toBeInTheDocument()
  })

  it('renders bold markup in a name as visible angle brackets', () => {
    render(
      <CustomerCard customer={{ ...amina, fullName: 'Amina <b>Khan</b>' }} onEdit={() => {}} />
    )
    expect(document.querySelector('b')).toBeNull()
    expect(screen.getByText('Amina <b>Khan</b>')).toBeInTheDocument()
  })

  it('renders a script payload without adding a script node', () => {
    render(
      <CustomerCard
        customer={{ ...amina, fullName: '<script>alert(document.cookie)</script>' }}
        onEdit={() => {}}
      />
    )
    expect(document.querySelector('script')).toBeNull()
    expect(screen.getByText(/<script>alert/)).toBeInTheDocument()
  })
})
