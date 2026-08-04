# Lab 21 — Liveness vs Readiness

## Liveness
Liveness: process stuck → restart (e.g., deadlocked threads).


## Readiness
Readiness: dependency down → not ready, keep process.


## Wrong mix
Wrong mix: restarting on transient DB outage.


## Lab expectation
Lab: toggle CrmReadinessIndicator OUT_OF_SERVICE; liveness stays UP.


If readiness is DOWN and liveness UP, should Kubernetes kill the pod?
no, restart?

Map CrmReadinessIndicator OUT_OF_SERVICE to which probe?
-to the down pod


- [ x ] File exists at `notes/lab21-probes.md`
- [ x ] Liveness defined
- [ x ] Readiness defined
- [ x ] Wrong mix noted
