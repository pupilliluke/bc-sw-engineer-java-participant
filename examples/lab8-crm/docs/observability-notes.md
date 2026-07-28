Lab 8 - observability notes (bonus)

Nothing is logged yet, this records what the ids are for so the fields exist in
everyone's head before the logging labs arrive.

IDS

  lab-request-001   correlation id, one per request, minted at the controller
                    boundary and carried on every log line the request touches
  CUS-1001          stable customer id, appears in logs instead of names

A create that fails in the repository should be findable by grepping one value:

  lab-request-001 controller accepted create
  lab-request-001 service assigned CUS-1001 status=ACTIVE
  lab-request-001 repository save failed <exception type>

SENSITIVE FIELDS

Names, emails and anything else about the person stay out of log lines, the ids
carry the trace. Amina Khan appears in the database, CUS-1001 appears in the
log. Same user vs operator split as the Lab 7 ATM, the log gets detail about
the failure, never detail about the person.

LATER

Latency and error-count metrics per operation, log level split (INFO for the
happy path, ERROR with exception type for failures), correlation id propagated
into Kafka messages so the notification and audit consumers can join the same
trace.
