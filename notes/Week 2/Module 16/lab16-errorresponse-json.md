# Lab 16 — ErrorResponse JSON Draft

## Step 1 — Fields

Fields: timestamp, status, error, message, path, correlationId.

## Step 2 — Sample

Sketch JSON for CUS-9999 not found with correlationId lab-request-001.

{
  "timestamp": "2026-07-31T12:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Customer CUS-9999 not found",
  "path": "/customers/CUS-9999",
  "correlationId": "lab-request-001"
}

## Step 3 — Hygiene

Message must not include stack traces or SQL.

## Step 4 — Boundary

Note: paper draft only; advice controller wiring is lab-time.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.