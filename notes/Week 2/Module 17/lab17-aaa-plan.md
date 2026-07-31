Module 17: Lab 17 AAA service tests plan (exercise 5)


STEP 1, HAPPY PATH

activate Ravi, PROSPECT to ACTIVE.

    Arrange   fresh fake repository in @BeforeEach, save Ravi CUS-1002 with
              status PROSPECT, build the service on that repository and the
              validator.
    Act       service.changeStatus("CUS-1002", ACTIVE, "lab-request-001").
    Assert    returned customerId is CUS-1002 and returned status is ACTIVE,
              then read CUS-1002 back from the repository and assert ACTIVE
              there too, so the change was saved and not only returned.


STEP 2, NOT FOUND

CUS-9999 was never created.

    Arrange   same fixture, Amina and Ravi present, nothing under CUS-9999.
    Act       assertThrows around
              service.changeStatus("CUS-9999", ACTIVE, "lab-request-001").
    Assert    the thrown exception carries the not-found code and the 404 hint,
              the correlation id comes back as lab-request-001, and the
              repository still holds two customers, so a failed lookup created
              nothing.


STEP 3, ILLEGAL

Amina CUS-1001 is already ACTIVE.

    Arrange   same fixture, Amina saved as ACTIVE.
    Act       assertThrows around
              service.changeStatus("CUS-1001", ACTIVE, "lab-request-001").
    Assert    the conflict code and the 409 hint, the correlation id echoed,
              and CUS-1001 still ACTIVE in the repository, which is the
              assertion that separates a rejected transition from a
              half-applied one.

collaborators stay real or simple fakes, the in-memory repository from my
earlier labs. no stubbing, that is lab 18.


SCOPE

pre-lab only, do not finish the full graded lab in this exercise.


SELF-CHECK

fixtures confirmed, Amina CUS-1001 ACTIVE, Ravi CUS-1002 PROSPECT, correlation
lab-request-001.

| # | Confirm | Result |
| --- | --- | --- |
| 1 | File exists, lab17-aaa-plan.md | Pass |
| 2 | Three AAA outlines | Pass, STEP 1 to STEP 3 |
| 3 | Fixtures used | Pass, CUS-1002, CUS-9999 and CUS-1001 |
| 4 | Notes saved | Pass, under notes\Week 2\Module 17 |
