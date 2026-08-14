import type { ReactNode } from 'react'

export function AppLayout({ children }: { children: ReactNode }) {
    return (
        <main>
            <h1>Customer Management Platform</h1>
            {children}
        </main>
    )
}
