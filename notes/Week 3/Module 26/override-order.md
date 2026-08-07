# Lab 26 — Property Override Order

## Highest to lowest
1. Command-line args (-D / --)
2. Environment variables
3. application-{profile}.yml
4. application.yml
5. Code defaults

## Property you will measure in lab
measure one property across sources.

## Debug / design challenge

If env sets logging.level.root=INFO and profile YAML sets DEBUG, who wins?

env wins

## Predict the Output / Behavior

Where do code `@Value` defaults sit relative to application.yml?

end of the resolution chain

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/override-order.md`
- [ x ] Ordered list
- [ x ] Measurement property
