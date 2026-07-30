Module 13: Lab 13 placeholder endpoint honesty (exercise 5)

what the endpoint address in a contract means when nothing is listening behind
it, what I am not building in this prep, and what lab 24 adds.


DEFINITION

A placeholder endpoint is a contract plus sample messages, with no production
host behind the address.

the address is still a real element with a syntactically real url in it. the
slides' evaluation criteria ask for a valid endpoint address placeholder in
service, so writing one is part of the work rather than a shortcut around it.
what it does not do is answer.


WHAT I AM NOT DOING IN THIS PREP

| Not doing | Why not |
| --- | --- |
| No Spring-WS @Endpoint | the endpoint class is lab 24's, and there is no spring on the classpath to write it against |
| No Spring Boot app | week 2 has been plain java since lab 8, adding boot to run a pre-lab note is scope creep |
| No deploy to Tomcat | nothing to deploy, the artifacts here are xml and markdown |

the classpath point is not a guess. lab 12's Customer carries a comment saying
week 2 has no spring and no JPA on the classpath and that persistence
suggestions get rejected rather than imported. same answer here, the contract
gets designed before anything is wired up to serve it.


WHAT LAB 24 ADDS

Lab 24 introduces Spring-WS hosting for this same contract.

that is when the address starts answering, the endpoint class exists, and the
faults designed here get raised by code instead of written down. the contract
should not need rewriting for it to happen, which is the whole reason for
freezing it first.


BOUNDARY

pre-lab only, prepare for lab 13, do not complete it now.


SELF-CHECK

| # | Confirm | Result |
| --- | --- | --- |
| 1 | File exists, lab13-placeholder-honesty.md | Pass, under notes\Week 2\Module 13 |
| 2 | Placeholder defined | Pass, one sentence under DEFINITION |
| 3 | Three non-goals listed | Pass, @Endpoint, Boot app, Tomcat deploy |
| 4 | Lab 24 referenced | Pass, as the lab that hosts this contract with spring-ws |
