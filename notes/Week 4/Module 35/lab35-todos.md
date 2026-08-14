# Lab 35 — Fill Fetch TODOs

## Step 1 — Paste

```ts
export type Customer = { customerId: string; name: string; status: string };

export async function listCustomers(signal?: AbortSignal): Promise<Customer[]> {
  const res = await fetch("http://localhost:8080/api/customers", {
    headers: { "X-Correlation-Id": "lab-request-001" },
    signal,
  });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return (await res.json()) as Customer[];
}

export async function getCustomer(id: string): Promise<Customer> {
  const res = await fetch(`http://localhost:8080/api/customers/${id}`);
  // TODO: handle 404 for unknown id
  return (await res.json()) as Customer;
}
```

## Step 2 — Fill

filled with the full http://localhost:8080/api/customers base,
lab-request-001, and Customer[]. getCustomer uses the same base, one URL
constant once the lab moves it to VITE_CRM_API_URL.

## Step 3 — UI TODO

// TODO: on success setCustomers including Amina + Ravi fixtures from API

## Step 4 — Error TODO

// TODO: map 400 body.detail to form error string

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab35-todos.md`
- [ x ] URL and correlation filled
- [ x ] Return type filled
- [ x ] Two UI/error TODOs present
