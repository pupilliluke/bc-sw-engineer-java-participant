# Lab 34 — Props vs State

## Step 1 — Scenario

Editing Amina (`CUS-1001`): name field, status dropdown, Save button.

## Step 2 — Classify

| Item | Prop or state | Why |
| --- | --- | --- |
| initialCustomer | prop | handed down by the parent to seed the draft, the form reads it and never changes it |
| draftName | state | changes on every keystroke in this component |
| draftStatus | state | changes when the user picks in this component |
| isSaving | state | flips when a save starts and finishes, owned by whichever component runs the save |
| onSaved | prop | a callback, not data. the parent passes it down and the child calls it, only the parent decides what a finished save means |

initialCustomer and the draft fields are not the same thing. initialCustomer
is a fact that arrived from outside, draftName and draftStatus are the user's
work in progress that started as a copy of it. editing the draft must not
touch the prop, the parent clones via an immutable update when the save lands.

## Step 3 — Rule

state = data that changes over time because of user interaction in this
component. everything else arrives as props, and changes go back up through
callbacks.

## Scope

Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab34-state.md`
- [ x ] Five items classified
- [ x ] Rule sentence present
- [ x ] Notes saved
