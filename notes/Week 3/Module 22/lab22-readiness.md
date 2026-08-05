# Lab 22 readiness checklist

| File | Present? (yes/no) |
| ---- | ----------------- |
| notes/ioc-vs-new.md | yes |
| notes/constructor-di.md | yes |
| notes/lab22-lifecycle-notes.md | yes |
| notes/stereotype-map.md | yes |
| notes/bean-graph-sketch.md | yes |

## Scope
Pre-lab only. Primary DI style for lab? constructor injection with final fields.

## Self mark
Overall prep: Pass
If Fail, revisit: n/a


If constructor-di notes still prefer field @Autowired, which exercise do you reopen?

Exercise 2. That is where the preferred pattern is recorded, and the lab's
critical scope rules out field @Autowired as the primary wiring.


Which package should CrmApplication live in so component scan finds CRM beans?

com.northstar.crm, the root above api, service, repository, metrics and
logging. @SpringBootApplication scans down from its own package.


- [ x ] File exists at `notes/lab22-readiness.md`
- [ x ] Artifacts confirmed
- [ x ] DI style noted
- [ x ] Pass/Fail marked
