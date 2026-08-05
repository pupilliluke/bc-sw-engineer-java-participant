# Lab 23 — application.yml Sketch

## Base keys
application name: lab23-crm
server.port: 8080
management exposure: include: health

## dev teaser
logging.level.root=DEBUG

## prod teaser
prod teaser: logging.level.root=INFO (no secrets)


## Debug / design challenge

Should prod teaser include a hard-coded database password?

NO! NEVER!!!!!!!

## Predict the Output / Behavior

What happens if exposure.include omits health?

that endpoint is not exposed by default.