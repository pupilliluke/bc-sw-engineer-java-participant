# Deployment Runbook — Lab 42, Northstar CRM

Apply, verify, roll back and remove the `crm-api` workload on the local k3d
cluster without reference to the lab guide.

## Prerequisites

- Work in `java-bootcamp/examples/lab42-crm`, not the course clone.
- Docker Engine running. The Docker Desktop GUI is not required; only the
  engine is. `docker version` must show a **Server** line.
- Host Postgres reachable on **5433**. `docker start crm-postgres-lab41`, or
  `docker compose up -d` from `examples/lab41-crm/`. The published port was
  moved from 5432 to 5433 because an unrelated capstone Postgres holds 5432 on
  this machine; the container still listens on 5432 internally. If you restore
  5432, change `CRM_DB_PORT` in the ConfigMap to match.
- Database **`crm_lab42`** exists with role **`crm`** granted `CONNECT` and
  owning schema `crm`. Created out-of-band; not migrated by hand, Flyway does
  that on first pod start.
- k3d cluster `lab42`:

```
k3d cluster create lab42 --image rancher/k3s:v1.28.15-k3s1 -p "8088:80@loadbalancer"
```

  The k3s version is pinned deliberately. The k3d default (v1.35.x) fails on
  this host with `kubelet cgroup v1 unsupported`.

- Kubeconfig `server:` rewritten to `127.0.0.1`. k3d writes
  `host.docker.internal`, which does not resolve from Windows and leaves
  `kubectl` hanging until timeout:

```
kubectl config set-cluster k3d-lab42 --server="https://127.0.0.1:<mapped-port>"
```

  The mapped port is assigned per cluster; read it with
  `docker port k3d-lab42-serverlb 6443/tcp`. It was `63807` on this build.

- Namespace `crm-training`:

```
kubectl create namespace crm-training --dry-run=client -o yaml | kubectl apply -f -
```

- Image imported into the cluster:

```
k3d image import crm-api:lab41 -c lab42
```

  This is required. k3s runs its own containerd and cannot see the Docker image
  store. Without the import the pod resolves the bare tag to
  `docker.io/library/crm-api:lab41`, goes to Docker Hub, and lands in
  ImagePullBackOff. `imagePullPolicy: IfNotPresent` on the Deployment is what
  makes the imported copy win.

- Lab 41 Image Id: `sha256:d3bb2e23660e8bb98b68e5d63f8e652457306f2509fc920b6fb16c0b8d06bbe0`

## Apply

Apply the four files by name. Never `kubectl apply -f k8s/` — that directory
contains `secret.example.yaml`, and applying it creates a Secret whose password
is the literal string `REPLACE-OUT-OF-BAND`, which leaves the pod running with a
credential that cannot authenticate.

```
kubectl -n crm-training apply -f k8s/configmap.yaml
```

Then the Secret, out-of-band, values never written to a file:

```
kubectl -n crm-training create secret generic crm-api-secrets \
  --from-literal=CRM_DB_PASSWORD='<the crm role password>' \
  --from-literal=JWT_SECRET='<the JWT signing key>' \
  --dry-run=client -o yaml | kubectl apply -f -
```

`JWT_SECRET` is not in the course baseline. This build carries Spring Security
and `application.yml` declares `jwt-secret` with no default, so the pod will not
start without it.

```
kubectl -n crm-training apply -f k8s/deployment.yaml
kubectl -n crm-training apply -f k8s/service.yaml
kubectl -n crm-training apply -f k8s/ingress.yaml
kubectl -n crm-training rollout status deployment/crm-api --timeout=180s
```

Allow up to two minutes. See Operational numbers.

## Verify

```
kubectl -n crm-training get pods
kubectl -n crm-training get endpoints crm-api
kubectl -n crm-training exec deploy/crm-api -- id
```

Expect one pod `1/1 Running`, one endpoint on port 8080, and
`uid=10001(spring)`. The endpoint check matters more than the pod check: a pod
can be `Running` and still absent from Endpoints.

Smoke through the Ingress with a Host header. The hostname is not in DNS, so the
request goes to loopback and Traefik routes on the header:

```
curl -s -H "Host: crm-api.training.example.test" \
  http://127.0.0.1:8088/actuator/health/readiness
```

Every `/api/customers` route requires a bearer token; `httpBasic` is disabled.
Log in first:

```
curl -s -X POST http://127.0.0.1:8088/api/auth/login \
  -H "Host: crm-api.training.example.test" \
  -H "Content-Type: application/json" \
  -d '{"username":"agent1","password":"agent1"}'

curl -s http://127.0.0.1:8088/api/customers \
  -H "Host: crm-api.training.example.test" \
  -H "Authorization: Bearer <accessToken from above>" \
  -H "X-Correlation-Id: lab-request-001"
```

Expect `CUS-1001` Amina Khan ACTIVE and `CUS-1002` Ravi Singh PROSPECT. The same
call without the token is 401.

On Windows PowerShell, `curl` is an alias for `Invoke-WebRequest` — use
`curl.exe`. PowerShell 5.1 also mangles inline JSON quotes passed to a native
executable; write the body to a file and use `-d "@file"`.

## Rollback

```
kubectl -n crm-training set image deployment/crm-api crm-api=crm-api:does-not-exist
kubectl -n crm-training rollout status deployment/crm-api --timeout=60s
kubectl -n crm-training rollout history deployment/crm-api
kubectl -n crm-training rollout undo deployment/crm-api
kubectl -n crm-training rollout status deployment/crm-api --timeout=180s
```

Re-run the Verify smoke afterwards. A green `rollout status` proves the object
converged, not that the application serves.

Three things observed during the rehearsal that the commands do not tell you:

`rollout status` never detects a bad image. It printed `1 old replicas are
pending termination` and then `error: timed out waiting for the condition`. It
names the old pod, which is the healthy one, and never mentions the image.
Without `--timeout` it waits indefinitely. The reason lives in
`kubectl describe pod`, not in `rollout status`.

`undo` does not delete the bad revision. History went from `1, 2` to `2, 3`: the
ReplicaSet holding the good template was re-annotated as revision 3 and the bad
revision 2 stayed. The rollback is itself an auditable event.

The rollback restarted nothing. The serving pod was 81 minutes old with 0
restarts before the drill and the same pod with the same age and restart count
after it. `undo` scaled the bad ReplicaSet to 0; the good ReplicaSet and its pod
were never touched, which is why `rollout status` returned instantly.

## Operational numbers

Measured on this host, not estimated.

| Event | Time |
| --- | --- |
| Recovery from `replicas: 0` to serving | **97s** |
| of which application startup | **78.8s** |
| Scale 1 to 2, second pod ready | 82.4s |
| Same image under `docker run`, Lab 41 | **9.1s** |

The application starts **8.6x slower** in the pod than in Docker. The controlled
difference is CPU: the Lab 41 container had no CPU limit, the pod is capped at
`500m`. JVM startup is CPU-bound, so half a core stretches a 9 second boot to 79.

This makes the startup probe load-bearing rather than generous.
`failureThreshold: 30` at `periodSeconds: 5` is a 150 second budget and the
application consumed 85 seconds of it, 17 failed polls out of 30. A
`failureThreshold: 12` — an ordinary-looking 60 seconds — would kill the
container before it finished booting and CrashLoop forever.

`timeoutSeconds: 1` is the other exposure. One startup probe failed with
`context deadline exceeded` rather than `connection refused`: the port was open
and the throttled JVM could not answer a trivial actuator call within one
second. A healthy application can fail a probe purely because it is starved.

## Troubleshooting

| Symptom | Cause | Fix |
| --- | --- | --- |
| `Bind for 0.0.0.0:5433 failed: port is already allocated` | another Postgres holds the port | `docker ps --format "{{.Names}}\t{{.Ports}}"` to find the holder, then stop it or republish this one and update `CRM_DB_PORT` |
| Pod `0/1` after the cluster restarts | `envFrom` is read at container start, so a ConfigMap change does not reach a running pod | `kubectl rollout restart deployment/crm-api` |
| `kubectl` hangs then times out | kubeconfig `server:` is `host.docker.internal` | rewrite to `127.0.0.1:<mapped-port>` |
| `kubelet cgroup v1 unsupported` | k3d default k3s version | pin `rancher/k3s:v1.28.15-k3s1` |
| ImagePullBackOff, `insufficient_scope: authorization failed` | **not a credentials problem** — the tag is not in the cluster | `k3d image import`; check the tag for typos before hunting for an imagePullSecret |
| **404 through the Ingress** | **check `get endpoints` first** | empty Endpoints returns 404, not 503. The Ingress host and class can be perfectly correct |
| Endpoints empty, pod `Running` | Service selector does not match pod labels | align `app: crm-api` |
| Endpoints empty, no pod | scaled to 0, or all pods unready | `get pods` distinguishes this from the row above; the 404 is identical |
| Restored a broken Service and still 404 | Traefik convergence lag | the Endpoints object recovers before the data plane; re-test for a few seconds before concluding the fix failed |
| `CreateContainerConfigError` | Secret missing | create out-of-band; never apply the example file |
| Pod starts then fails on the datasource | `CRM_DB_HOST` unresolved | surfaces as Hikari `Driver ... claims to not accept jdbcUrl`, **not** as `Could not resolve placeholder`. A missing `JWT_SECRET` does give the placeholder error |
| 401 on `/api/customers` | no bearer token | log in first; `httpBasic` is disabled in this build |

## Residual risks

- **Single replica.** The default is `replicas: 1`, so any pod loss is a full
  outage until a replacement passes its probes — 97 seconds measured. Two
  replicas were verified working and both appeared in Endpoints.
- **Startup budget has no headroom on a busier host.** 17 of 30 polls consumed
  on an idle laptop. A loaded machine could exceed the 150 second budget and
  CrashLoop a healthy application.
- **The database is not in the cluster.** It is a Docker container on the host,
  reached through `host.k3d.internal`. Nothing in these manifests manages its
  lifecycle, backup, or availability.
- **The Secret is unmanaged.** Created imperatively, held only in cluster etcd.
  It is not versioned, not rotated, and would be lost with the cluster. Rotation
  owner: whoever owns the `crm` role and the JWT signing key.
- **`readOnlyRootFilesystem: false`.** The Lab 41 evidence shows the application
  writes nothing outside `/tmp`, so `true` with an `emptyDir` at `/tmp` is
  achievable and was not done.

## Cleanup

Capture evidence first. Delete only what this runbook created.

```
kubectl -n crm-training delete -f k8s/ingress.yaml -f k8s/service.yaml \
  -f k8s/deployment.yaml -f k8s/configmap.yaml --ignore-not-found
kubectl -n crm-training delete secret crm-api-secrets --ignore-not-found
git status --short
```

`k3d cluster stop lab42` preserves the cluster; `k3d cluster delete lab42`
destroys it. Do not delete `crm-postgres-lab41` or the `lab41-crm_crm_pgdata`
volume — Lab 41 evidence depends on the `crm41` database inside it.

## Recorded identity

Deployed 2026-08-21.

| Field | Value |
| --- | --- |
| Image | `crm-api:lab41` |
| Image Id | `sha256:d3bb2e23660e8bb98b68e5d63f8e652457306f2509fc920b6fb16c0b8d06bbe0` |
| Id inside the cluster | `6be309fbd6e2`, 163MB per `crictl images` |
| `imagePullPolicy` | `IfNotPresent` |
| Runtime user | `uid=10001(spring)` confirmed by `exec`, not only by the manifest field |
| Cluster | k3d `lab42`, k3s `v1.28.15+k3s1`, LB `8088:80` |
| Namespace | `crm-training` |
| Ingress host | `crm-api.training.example.test`, class `traefik` |
| Database | `crm_lab42`, role `crm`, schema `crm`, Flyway v1 and v2 |

`crictl` reports a different id from `docker image inspect` because the cluster
records the config digest while Docker's containerd store reports the
manifest-list digest. Both name the same image. Record which command produced
which number.

## Deviations from the lab guide

1. **`JWT_SECRET` added to the Secret.** This build carries Spring Security,
   which the guide's Lab 41 baseline does not. Without the key the pod fails
   startup with `Could not resolve placeholder 'JWT_SECRET'`.
2. **Smoke requires a login.** The guide's `GET /api/customers` returns 401 on
   this build. The runbook's smoke logs in as `agent1` first.
3. **`application-docker.yml` was added to `lab41-crm` and the image rebuilt**
   so the guide's `CRM_DB_*` ConfigMap keys map onto a build that otherwise
   reads a single `CRM_DB_URL`. The file is inert when the profile is not
   active, so Lab 41 behaves as it did. The pre-rebuild image is preserved as
   `crm-api:lab41-a57412f-prerebuild`,
   `sha256:4cf59c01fcd5afcb3a61c02b6140f24284185e579a7ef8cb2a4455f37b755fbd`.
4. **Database and role provisioned differently.** The guide assumes `crm-postgres`
   from Lab 37 with user `crm` / `change-me`. This uses `crm-postgres-lab41`, and
   the `crm` role was created least-privilege with the same password as
   `crm_app` rather than the courseware default.
