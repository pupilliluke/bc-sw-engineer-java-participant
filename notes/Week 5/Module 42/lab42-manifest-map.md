# Lab 42 — Map k3s Manifests

## Reference

| Object | Holds | Must not hold |
| --- | --- | --- |
| ConfigMap | Non-secret URLs/flags | DB passwords |
| Secret | Credentials (out-of-band) | Values in Git |
| Deployment | Pod template, probes | HostPath secrets |
| Service | ClusterIP ports | TLS private keys |
| Ingress | Host/path/TLS redirect | App business logic |

## Step 1 — Objects

| Object | Name | Carries |
| --- | --- | --- |
| Namespace | `lab42` | everything below |
| ConfigMap | `crm-api-config` | `CRM_DB_URL`, `CRM_DB_USERNAME` |
| Secret | `crm-api-secrets` | `CRM_APP_PASSWORD`, `JWT_SECRET`, created out-of-band |
| Deployment | `crm-api` | pod template, probes, resources, non-root |
| Service | `crm-api` | ClusterIP, port 80 to container 8080 |
| Ingress | `crm-api` | Traefik, host and path to the Service |

The four keys are lab 41's `.env.local`. `CRM_DB_URL` and `CRM_DB_USERNAME` carry
no credential. `CRM_APP_PASSWORD` and `JWT_SECRET` have no defaults in
`application.yml`, so a missing value stops startup rather than falling back.

## Step 2 — Check the reference

Default student cluster is local k3d (`lab42`, Traefik Ingress). `kubectl` uses
the k3d kubeconfig, which is never committed. A shared instructor cluster is
optional and only if the instructor publishes it.

## Step 3 — Labels

`app=crm-api`, `lab=42`, `customer-fixture=synthetic`. The Service selects pods
by `app=crm-api`; a selector that does not match the pod labels gives a Service
with no Endpoints.

## Step 4 — Image pin

Image is the lab 41 tag `crm-api:lab41`, Image Id
`sha256:4cf59c01fcd5afcb3a61c02b6140f24284185e579a7ef8cb2a4455f37b755fbd`.
Not `:latest` alone. A registry digest is optional until the image is pushed.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [x] File exists at `notes/lab42-manifest-map.md`
- [x] Six object types listed
- [x] Traefik Ingress noted
- [x] Image tag `crm-api:lab41` + Image Id noted
