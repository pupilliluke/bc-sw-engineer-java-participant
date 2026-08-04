
Forbidden: Email, phone, social security number, bank account number, credit card number, etc

Allowed customerId: customerId, correlationId, status, date, time, 
Allowed correlation: CUS-1001/CUS-1002, correlation lab-request-001
Clear MDC in finally? must be yes
try { … } finally { MDC.clear(); }

Is “Amina” alone forbidden even without email?

Not forbidden but unnecessary to log in ERROR logs. Only log customerId and correlationId. 

May ERROR logs include the request JSON body “just this once”?

no. must protect user privacy and limit to necessary identifiable user fields

- [ X ] File exists at `notes/lab20-forbidden-pii-todos.md`
- [ X ] Three forbidden
- [ X ] Allowed ids
- [ X ] Clear = yes
