# Lab 42 — Deployment runbook

## Prerequisites

- kubeconfig for instructor k3s (not in Git)
- Namespace: `TODO(lab42)`
- Image digest from Lab 41: `TODO(lab42)`

## Apply

```bash
kubectl apply -f k8s/configmap.yaml -n <ns>
# Secret created out-of-band — do not apply secret.example.yaml with real values from Git
kubectl apply -f k8s/deployment.yaml -n <ns>
kubectl apply -f k8s/service.yaml -n <ns>
kubectl apply -f k8s/ingress.yaml -n <ns>
kubectl rollout status deployment/crm-api -n <ns>
```

## Smoke

- Health via Ingress: `TODO(lab42)`
- Customer `CUS-1001` + correlation `lab-request-001`

## Rollback rehearsal

```bash
# TODO(lab42): Record bad revision then:
kubectl rollout undo deployment/crm-api -n <ns>
kubectl rollout status deployment/crm-api -n <ns>
```
