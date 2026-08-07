# Lab 26 — ConfigurationProperties Sketch

## Class name
NorthstarIntegrationProperties

## Prefix
northstar.integration

## Fields
apiBaseUrl, apiKey (apiKey from env in prod)

## How enabled
@EnableConfigurationProperties or @ConfigurationPropertiesScan

## Debug / design challenge

Why prefer @ConfigurationProperties over five unrelated @Value fields?

decoupling profiles, easier for switching between contexts

## Predict the Output / Behavior

If prefix mismatches YAML, what do you observe at runtime?

error running application

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/northstar-props.md`
- [ x ] Prefix
- [ x ] Fields
- [ x ] Enable path
