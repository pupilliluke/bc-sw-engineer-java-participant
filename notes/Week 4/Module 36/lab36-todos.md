# Lab 36 — Fill Route Guard TODOs

## Step 1 — Paste

```tsx
function RequireAuth({ children }: { children: React.ReactNode }) {
  const token = getAccessToken();
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
}

// TODO: attach Authorization: Bearer ${getAccessToken()} on API fetch (Lab 35+36)
// TODO: never log full token; log correlation lab-request-001 instead
```

## Step 2 — Fill

`getAccessToken()` reads the in-memory store from exercise 2, so the guard
holds no copy of the token itself. `"/login"` with `replace` keeps the
blocked url out of history, otherwise back lands on the guarded route
again. `<>{children}</>` returns the children unchanged, the guard is a
pass-through and not a wrapper that renders anything of its own. the
bearer value comes from the same store at fetch time, `lab-token-001` is
the fake stand-in used in notes only.

## Step 3 — Role note

```tsx
// TODO: hide AdminMenu unless role === "ADMIN"  — UI only
```

hiding the menu removes the affordance, nothing more. the admin endpoint
still has to reject a non-admin token, a hidden button is one devtools
edit away from visible.

## Step 4 — Backend reminder

Spring Security must still reject unauthorized API calls for CUS-1001 and
CUS-1002 data, even when the guard passes. the guard runs in the browser,
which is the part of the system an attacker controls.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab36-todos.md`
- [ x ] Blanks filled
- [ x ] Token logging avoided
- [ x ] Backend enforcement note
