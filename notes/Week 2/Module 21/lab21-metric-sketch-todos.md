# Lab 21 — Fill Metric Sketch TODOs

Success counter: _create_success_total / crm.customer.create success___
Failure counter: _create_failure_total 
Forbidden label: customerId_
Alert name: CrmCreateFailuresHigh when failure rises above threshold
Alert threshold idea: ?
First responder action: correlation tags

Should the alert threshold use a raw forever-total or a rate over N minutes?
rate over N minutes

Why must the first responder look at logs instead of metric labels for CUS-1001?
Because the metric labels are forbidden to include customerId, so the first responder cannot identify the specific
customer from the metric labels and must look at the logs for correlation tags to find the relevant information.


- [ x ] File exists at `notes/lab21-metric-sketch-todos.md`
- [ x ] Both counters
- [ x ] Forbidden label
- [ x ] Alert + action
