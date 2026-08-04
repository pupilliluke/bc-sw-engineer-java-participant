# Lab 20 — Log Level Quiz

| Event | Level |
| --- |-------|
| Activate success Ravi | INFO  |
| Illegal transition Amina | WARN  |
| Unexpected repo failure | ERROR |
| Mapper field copy detail | DEBUG |

## Prod habit
DEBUG off by default in prod.


## Scope
Pre-lab only.

Should blank-name validation be WARN or ERROR? Pick one and say why.

WARN. A blank name is caught by validation and the request is rejected, so it is an                                                                                                                                               
abnormal condition the code handled, not an unexpected failure. ERROR is for failures                                                                                                                                             
that need investigation, and expected client input errors should not produce noisy                                                                                                                                                
stack traces

If root is DEBUG in prod, what operational problem appears first?

DEBUG logs can generate a large volume of output, which may lead to performance issues or increased storage requirements in a production environment.

Pass criteria
Self-check before marking Pass:

File exists at notes/lab20-level-quiz.md -PASS
Four levels assigned -PASS
Prod habit present -PASS