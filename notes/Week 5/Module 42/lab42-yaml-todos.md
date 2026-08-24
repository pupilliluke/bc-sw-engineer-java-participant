# Lab 42 — Fill Deployment YAML TODOs

## Step 1 — Skeleton

In notes, draft:

```yaml
# deployment-skeleton.yaml
spec:
  replicas: 1
  template:
    spec:
      securityContext:
        runAsNonRoot: true
        runAsUser: 10001
      containers:
      - name: crm-api
        image: crm-api:lab41
        ports:
        - containerPort: 8080
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: http
        resources:
          requests:
            cpu: 250m
            memory: 256Mi
          limits:
            cpu: 500m
            memory: 512Mi
```

## Step 2 — Fill

replicas `1`, `runAsNonRoot: true`, `runAsUser: 10001`, image tag
`crm-api:lab41`, container port `8080`, readiness path
`/actuator/health/readiness`. The UID matches the lab 41 image, where
`docker exec id` returns `uid=10001(spring)`.

## Step 3 — Resources block

`requests` 250m CPU and 256Mi memory, `limits` 500m CPU and 512Mi memory. Lab 41
ran the image under `--memory=512m` with `MaxRAMPercentage=75`, which gave a
384MB heap and exited with `OOMKilled: false`.

## Step 4 — Do not apply

Do not `kubectl apply` this skeleton. The apply, smoke and rollback are Lab 42
work.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [x] File exists at `notes/lab42-yaml-todos.md`
- [x] Skeleton blanks filled
- [x] Resources placeholders present
- [x] No apply claimed
