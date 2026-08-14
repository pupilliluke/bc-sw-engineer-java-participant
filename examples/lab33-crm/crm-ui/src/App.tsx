import { AppLayout } from './components/AppLayout'
import { CustomerForm } from './components/CustomerForm'
import { CustomerList } from './components/CustomerList'
import { CustomerToolbar } from './components/CustomerToolbar'
import { seedCustomers } from './data/seedCustomers'
import type { CustomerDraft } from './types/customer'

const emptyDraft: CustomerDraft = {
  fullName: '',
  email: '',
  status: 'PROSPECT',
}

export default function App() {
  return (
    <AppLayout>
      <CustomerToolbar onAdd={() => console.log('add', 'lab-request-001')} />
      <CustomerList
        customers={seedCustomers}
        onEdit={(id) => console.log('edit', id, 'lab-request-001')}
      />
      <CustomerForm draft={emptyDraft} onChange={() => {}} onSubmit={() => {}} />
    </AppLayout>
  )
}
