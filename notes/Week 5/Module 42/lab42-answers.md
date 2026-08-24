Lab 42 kubernetes (k3s) deployment of the CRM (evidence log, reflection
questions, checkpoints)

built as examples\lab42-crm from the course starter, deployed to a local k3d
cluster lab42 pinned to k3s v1.28.15-k3s1 with the load balancer on 8088:80.
the work was a ConfigMap and an out-of-band Secret, a non-root Deployment at uid
10001 with resource bounds and three probes, a ClusterIP Service, a Traefik
Ingress reached by Host header, a rollout drill with rollback, and three of the
six failure experiments.

the guide's Lab 41 baseline describes a different application from the one this
repository builds: it assumes no Spring Security, an application-docker.yml
mapping CRM_DB_*, and a postgres user crm. to meet the lab rather than work
around it, application-docker.yml was added to lab41-crm and the image rebuilt,
and a least-privilege crm role was created. the pre-rebuild image is preserved
under a separate tag so lab 41's recorded identity stays verifiable.


EVIDENCE LOG

- Repo (must be java-bootcamp): examples\lab42-crm in java-bootcamp. nothing
  was written under labs/.
- k3d cluster / k3s image pin: k3d lab42, rancher/k3s:v1.28.15-k3s1, created
  with -p "8088:80@loadbalancer". node reports v1.28.15+k3s1 Ready. the pin is
  required; the k3d default v1.35.5-k3s1 fails with kubelet cgroup v1
  unsupported.
- Lab 41 Image Id:
  sha256:d3bb2e23660e8bb98b68e5d63f8e652457306f2509fc920b6fb16c0b8d06bbe0.
  inside the cluster crictl reports 6be309fbd6e2 at 163MB, which is the config
  digest rather than the manifest-list digest. the pre-rebuild image is
  crm-api:lab41-a57412f-prerebuild,
  sha256:4cf59c01fcd5afcb3a61c02b6140f24284185e579a7ef8cb2a4455f37b755fbd.
- ConfigMap profile / JDBC host / db / user: SPRING_PROFILES_ACTIVE=docker,
  CRM_DB_HOST=host.k3d.internal, CRM_DB_NAME=crm_lab42, CRM_DB_USER=crm,
  CRM_DB_PORT=5433, SERVER_PORT=8080. the host port moved from 5432 to 5433
  on 2026-08-24 because an unrelated capstone Postgres holds 5432; the database
  container still listens on 5432 internally. the Secret holds CRM_DB_PASSWORD and
  JWT_SECRET, created with kubectl create secret --from-literal and never
  written to a file. secret.example.yaml was not applied; the live value was
  checked and is not the placeholder.
- Readiness curl (Host header): HTTP 200 {"status":"UP"} on
  http://127.0.0.1:8088/actuator/health/readiness with
  Host: crm-api.training.example.test. anonymous GET /api/customers is 401.
- GET /api/customers result: HTTP 200 returning CUS-1001 Amina Khan ACTIVE and
  CUS-1002 Ravi Singh PROSPECT, with a bearer token from POST /api/auth/login
  as agent1 and X-Correlation-Id: lab-request-001. at two replicas, six
  consecutive calls all 200 with both pods in Endpoints.
- Rollback undo observation: set image to crm-api:does-not-exist created
  revision 2 and a second pod in ImagePullBackOff. rollout status never named
  the image, reported 1 old replicas are pending termination, and exited on the
  60s timeout. traffic was unaffected throughout: Endpoints held only the ready
  pod and did not churn, readiness stayed 200. rollout undo returned
  immediately, history went from 1,2 to 2,3 rather than back to 1, and the
  serving pod was the same pod at 81 minutes and 0 restarts before and after.
- Runbook peer-tested: N. step 11 not performed.


REFLECTION QUESTIONS

1. Which design decision most affected traffic safety (which probe)?


2. What evidence proves rollback worked?


3. Which failure was hardest to diagnose from events/logs (pull vs probes vs JDBC)?



CHECKPOINTS

| # | Confirm | Result |
| - | ------- | ------ |
| A1 | work is in java-bootcamp/examples/lab42-crm, not the course clone | Pass, starter copied out of labs/ and nothing under labs/ was edited |
| A2 | k3d lab42 context works; namespace crm-training; image imported | Pass, cluster-info answers after the kubeconfig rewrite, namespace Active, crictl shows docker.io/library/crm-api:lab41 |
| A3 | ConfigMap in Git (profile docker, crm_lab42, host.k3d.internal, user crm); Secret only in cluster | Pass, all four values present; Secret created out-of-band with two keys and exists only in the cluster |
| B1 | Deployment labels/selectors aligned; image crm-api:lab41, no fake digest | Pass, tag only with imagePullPolicy IfNotPresent, no @sha256 on the live Deployment |
| B2 | Resources + runAsUser 10001 | Pass, requests 100m/256Mi limits 500m/512Mi, and exec into the pod returns uid=10001(spring) |
| B3 | startup, readiness, liveness all present, startup on readiness path | Pass, startup and readiness on /actuator/health/readiness, liveness on /actuator/health/liveness |
| C1 | Service Endpoints populated | Pass, 10.42.0.9:8080 at one replica and both pod IPs at two |
| C2 | Ingress reachable via Host header on 127.0.0.1:8088 | Pass, readiness 200 through Traefik |
| C3 | GET /api/customers 200 + correlation header | Pass, 200 with both fixtures, X-Correlation-Id lab-request-001 |
| D1 | rollout undo rehearsed and verified | Pass, drill and re-smoke in notes/screenshots/lab-42/lab42-failure-experiments.txt |
| D2 | deployment-runbook.md lists files, not apply -f k8s/ | Pass, the four manifests are named individually and the reason is stated |
| D3 | no kubeconfig/Secret data in Git; secret.example.yaml never applied | Pass, secret.example.yaml carries key names only, the real Secret was created imperatively, no kubeconfig is in the repository |
| D4 | peer apply from runbook succeeded | Fail, not performed |
| D5 | pushes went to your java-bootcamp remote | Not yet, nothing from this lab has been committed |


CORRECTIONS TO THE PRE-LAB PREDICTIONS

lab42-manifest-map.md proposed Namespace lab42. the lab uses crm-training.

lab42-manifest-map.md recorded the image id as sha256:4cf59c01fcd5, which was
correct when written. the deployed image is sha256:d3bb2e23660e after
application-docker.yml was added and the image rebuilt.

lab42-rollout-rollback.md used Ingress host crm-api.localhost. the starter's
host is crm-api.training.example.test. the pre-lab source rule barred reading
the starter, so the real host was not knowable at the time.

lab42-config-vs-secret.md classifies only the six settings the exercise lists.
the live Secret also carries JWT_SECRET, because this build has Spring Security
and application.yml declares jwt-secret with no default.

lab42-yaml-todos.md follows the exercise-04 template, which sets
containerPort: 8080 without a name while referencing port: http on the probe.
that template is internally inconsistent and the probe would not resolve. the
real starter names the port http, and the deployed Service targetPort: http
resolves to 10.42.0.9:8080.

lab42-probe-design.md argued readiness should include db in the health group so
it reflects the datasource. the deployed manifest does not do that; the starter
does not and the guide does not ask for it. the claim stands as design
reasoning, not as something implemented here.

predictions made during the drill and how they came out:

rollout status was predicted to report the deployment offline. it does not
detect the bad image at all. it prints a progress line naming the old replica
and then exits on the timeout, so without --timeout it waits indefinitely.

one running pod was predicted. there were two, the incumbent plus a surge pod
in ImagePullBackOff, and that second pod is the explanation for why traffic
survived.

after rollout undo, one revision holding the good image was predicted. history
showed revisions 2 and 3. undo does not erase the bad revision; it re-annotates
the ReplicaSet holding the good template as the newest revision, so the
rollback stays auditable.

on the selector break the HTTP code was predicted to be the value that stayed
the same. it was the one that changed, 200 to 404, while the pod stayed 1/1
Running and untouched.


SCOPE HONESTY

D4 is a genuine Fail. the runbook has not been executed by anyone other than
its author.

D5 is not a Pass. nothing from lab 42 has been committed: examples/lab42-crm,
the six module 42 pre-lab notes, notes/screenshots/lab-42 and
examples/lab41-crm/crm-api/src/main/resources/application-docker.yml are all
uncommitted.

four of six failure experiments were performed, 1, 3, 5 and 6. experiments 2
and 4 are document-only by the guide's own framing and are written up rather
than run. the deployment is back at replicas: 1 and the optional delete-a-pod
check was run: eight consecutive 200s with no interruption.

lab 41's tree changed after lab 41 was marked complete. application-docker.yml
was added to examples/lab41-crm and crm-api:lab41 was rebuilt, so the image id
recorded in lab41-answers.md and docs/container-runbook.md no longer matches
the tag crm-api:lab41. the original image is preserved as
crm-api:lab41-a57412f-prerebuild at the recorded digest, so the lab 41 evidence
remains verifiable, but the tag alone no longer resolves to it.

the rebuilt image carries an org.opencontainers.image.revision label naming a
commit that does not contain application-docker.yml, because the file was
uncommitted at build time. the label is wrong until the image is rebuilt from a
clean tree.

the lab 41 gate still fails at failBuildOnCVSS 7, unchanged.
