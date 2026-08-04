# Lab 20 — Rewrite Unsafe Logs

## Unsafe example
log.info("Customer {}", customer);
toString() may leak email/phone if present.

## Safe rewrite (Amina/CUS-1001)
log.info("customerId={} status={} corr={}", "CUS-1001", "ACTIVE", "lab-request-001");

## Safe Ravi activate start
log.info("customerId={} status={} corr={}", "CUS-1002", "PROSPECT", "lab-request-001");

## Scope
Pre-lab only.

- [ X ] File exists at `notes/lab20-safe-logs.md`
- [ X ] Unsafe labeled
- [ X ] Safe Amina line
- [ x ] Safe Ravi line
