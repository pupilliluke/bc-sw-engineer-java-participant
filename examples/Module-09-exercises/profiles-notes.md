Module 9 Exercise 2 - build profiles

A profile is a named set of properties and plugin config that switches on
together. dev is activeByDefault so a plain build is the laptop build, prod has
to be asked for. Same pom.xml either way, only the app.env value moves.


STEP 1 - ANSWER BEFORE RUNNING MAVEN

  question                                my answer
  active profile for plain mvn package    dev, it's activeByDefault
  how to activate prod                    mvn -Pprod package
  app.env under dev                       dev
  app.env under prod                      prod


STEP 2 - CHECK

All four matched the reference.

activeByDefault is weaker than the name suggests. It only applies when nothing
else is activated in that build, so -Pprod replaces the default set and dev
switches off on its own.


STEP 3 - SPOT THE MISTAKES

production passwords inside the dev profile
pom.xml is committed, so the password lands in git history the moment it's
pushed and deleting it later doesn't remove it. It's also the wrong profile,
every laptop running a plain build would be holding production credentials it
has no reason to have.

prod activeByDefault on every laptop
The safe default stops being safe. Someone running plain mvn package gets the
production property set without asking for it, and a local test run points at
real infrastructure. The default should be the harmless environment, prod should
cost a deliberate -Pprod.

assuming profiles change Java package names
They don't. A profile changes build properties, plugin config and which
dependencies apply, the source tree is identical either way. Expecting a
com.northstar.crm.prod package to appear means hunting a bug that was never
there when the only difference is a property value.

secrets in screenshots of profile properties
A screenshot leaves the repo's access controls behind and ends up in a deck, a
ticket or a chat thread nobody thinks to rotate. Same outcome as committing it,
the value is exposed and has to be treated as burned.


STEP 4 - ACTIVATION RULE

Keep dev as the laptop default.
Activate prod intentionally with -Pprod.
Never store real production secrets in pom.xml profiles.

The pom names the property, the real value comes from an environment variable or
a secret manager at run time.


PASS CRITERIA

| # | Confirm | Notes |
| - | ------- | ----- |
| 1 | Four Q&A rows match the reference | PASS |
| 2 | You flag at least two profile mistakes | PASS |
| 3 | Activation rule is written | PASS |
