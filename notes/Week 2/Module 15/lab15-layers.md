Module 15: Lab 15 layer diagram (exercise 1)


STEP 1, BOXES

three boxes, API adapter, CustomerService, CustomerRepository.

    [API adapter]  --activate(CUS-1002)-->  [CustomerService]
          ^                                         |
          |                                  --findById / save-->
     Customer returned                       [CustomerRepository]


STEP 2, ARROW LABELS

activate(CUS-1002) flows inward, the updated Customer returns outward. below
the service the arrows are findById and save.


STEP 3, CORRELATION

lab-request-001 crosses the API edge as an argument and is logged again in the
service.


SCOPE

pre-lab only, do not finish the full graded lab in this exercise.


SELF-CHECK

fixtures confirmed, Amina CUS-1001 ACTIVE, Ravi CUS-1002 PROSPECT, correlation
lab-request-001.

| # | Confirm | Result |
| --- | --- | --- |
| 1 | File exists, lab15-layers.md | Pass |
| 2 | Three layers named | Pass, under STEP 1 |
| 3 | Activate flow labeled | Pass, under STEP 2 |
| 4 | Correlation edge noted | Pass, under STEP 3 |
