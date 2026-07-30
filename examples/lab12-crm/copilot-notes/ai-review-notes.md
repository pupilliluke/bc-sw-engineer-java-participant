# AI review notes — Lab 10

Assistant used for lab10-001 and lab10-002: Claude Code (Opus 5) in the IntelliJ
terminal, not Copilot inline ghost text. Recorded here so the evidence is not
misattributed. Scratch code both entries describe is under
copilot-notes/scratch/, ScratchWeak.java and ScratchStrong.java, both compiled
with javac 21 outside the maven build.

## lab10-001 — weak vs strong (entity)
- Date: 2026-07-28
- Weak prompt used: `// customer class`
- Output summary: compiled fine and was wrong for Northstar in six ways. `int id`
  instead of a String holding "CUS-1001", so the id format the whole platform
  uses can't even be represented. `name` instead of `fullName`. Invented an
  `address` field nobody asked for. No `status` at all, so PROSPECT and ACTIVE
  don't exist. No `createdAt`. No equals/hashCode, so two loads of CUS-1001 are
  different objects to a List or a Set. Nothing about it is flagged by the
  compiler, it just quietly models a different business.
- Strong prompt used: `// Java entity class Customer in package
  com.northstar.crm.entity representing a Northstar CRM customer. Fields:
  customerId (String, format "CUS-1001"), fullName (String), email (String),
  phone (String), status (CustomerStatus enum: PROSPECT, ACTIVE, SUSPENDED,
  CLOSED), createdAt (LocalDateTime). No-args constructor, all-args constructor,
  getters and setters, equals/hashCode based only on customerId, toString.`
- Output summary: all six fields with the named types, CustomerStatus enum with
  the four constants, both constructors, getters and setters, equals/hashCode on
  customerId only, toString printing
  `Customer{customerId='CUS-1001', fullName='Amina Khan', status=ACTIVE}`.
  No JPA and no Spring, imports were java.time.LocalDateTime, java.util.Objects,
  java.util.ArrayList and java.util.List, all nameable from the JDK.
- Decision: partial
- Reason (1 sentence): took the strong output as the shape for Step 4 and threw
  the weak one away, the weak prompt didn't produce a worse version of the same
  class, it produced a different class.

## lab10-002 — weak vs strong (addCustomer)
- Date: 2026-07-28
- Weak prompt used: `// add a customer`
- Output summary: `public void addCustomer(Customer customer) {
  customers.add(customer); }`. One line, happy path only, returns void. Null
  customer, blank id and a duplicate CUS-1001 all get stored without complaint,
  and the caller gets nothing back to confirm what was saved.
- Strong prompt used: `// Method addCustomer(Customer customer) on
  CustomerService: reject if customerId is null/blank, reject if a customer with
  the same customerId already exists (throw IllegalStateException), otherwise
  store it in the in-memory list and return it.`
- Output summary: three guard clauses ahead of the mutation,
  IllegalArgumentException for a null customer and for a null or blank
  customerId, IllegalStateException for a duplicate id, then add and return the
  customer. Validate-then-mutate ordering, so a rejected call leaves the list
  untouched.
- Decision: accept
- Reason (1 sentence): rules named in the prompt came back as guard clauses in
  the code, which is the whole difference between the two prompts.

Harness run against the strong output, javac 21, run from the scratch copy:

  added: Customer{customerId='CUS-1001', fullName='Amina Khan', status=ACTIVE}
  duplicate rejected: customer already exists: CUS-1001
  blank id rejected: customerId must not be null or blank
  added: Customer{customerId='CUS-1002', fullName='Ravi Singh', status=PROSPECT}
  equals on customerId only: true
  count: 2

Both guards fired and the list held two customers, so the duplicate never
landed. Phantom annotation check from the module 10 pre-lab: zero annotations in
either output, nothing to reject this time.

## lab10-003 — human review pass (GUIDE Step 7)

Numbering note: the starter template labels 003 "CustomerStatus / Customer
scaffold" and 004 "CustomerService review". The GUIDE Steps 7 and 8 label them
"human review pass" and "AI risk awareness". Followed the GUIDE, since that is
what Checkpoint D and the rubric are written against. The scaffold and service
review content sits inside 003 below, so nothing is lost.

- Date: 2026-07-28
- Assistant: GitHub Copilot Chat wrote the class, reviewed here with Claude Code.
- File: starter/src/main/java/com/northstar/crm/service/CustomerService.java
- Decision: partial, accepted after three edits
- Compiles: mvn -q clean compile in starter/, BUILD SUCCESS.
- No Spring, no JPA, no annotations. Imports are Customer, CustomerStatus,
  ArrayList, List, Optional, all nameable, so the ex 3 reject rule passes.
- All five methods from the prompt are present with the right exception types,
  IllegalArgumentException for blank id, IllegalStateException for a duplicate,
  and every guard runs before the list is touched.

Better than the GUIDE reference shape in four places, kept as-is:
- guards a null customer, the reference NPEs on addCustomer(null)
- compares id.equals(c.getCustomerId()) argument first, survives a stored null
  id where the reference would NPE
- findByCustomerId(null) returns Optional.empty instead of scanning
- updateStatus rejects a blank id before the lookup

Three things review caught, all fixed:

| # | Found | Fix applied |
| --- | --- | --- |
| 1 | updateStatus(id, null) set status to null, the customer then vanished from every findByStatus query | guard, IllegalArgumentException "newStatus must not be null" |
| 2 | findByStatus returned a mutable ArrayList, the reference returns .toList() | rebuilt as stream().filter().toList() |
| 3 | class javadoc still said TODO (Copilot Chat) after the work was done | rewritten to describe the class, plus an honest line that synchronized guards the list but does not make the class thread-safe |

Issue 1 exists in the GUIDE reference too, so it wasn't a Copilot regression, it
was a gap in the spec that both copies inherited.

The synchronized blocks were not asked for anywhere in the prompt. Kept them
rather than stripping scope out of working code, but documented what they
actually protect, findByCustomerId and listAll hand back live Customer objects
a caller can mutate outside the lock.

Message text differs from the reference, matters if anything asserts on it:

| | This class | GUIDE reference |
| --- | --- | --- |
| duplicate | Duplicate customerId: CUS-1001 | Customer already exists: CUS-1001 |
| not found | Customer does not exist: X | No such customer: X |

Harness run after the fixes, javac 21 against starter/target/classes:

  add amina : Customer{customerId='CUS-1001', fullName='Amina Khan', status=ACTIVE}
  IllegalStateException | duplicate CUS-1001 -> Duplicate customerId: CUS-1001
  IllegalArgumentException | blank id -> customerId must not be blank
  IllegalArgumentException | null customer -> customer must not be null
  IllegalArgumentException | unknown updateStatus -> Customer does not exist: CUS-9999
  find null : Optional.empty
  byStatus PROSPECT : [Customer{customerId='CUS-1002', fullName='Ravi Singh', status=PROSPECT}]
  updateStatus 1002 : Customer{customerId='CUS-1002', fullName='Ravi Singh', status=ACTIVE}
  UnsupportedOperationException | mutate listAll()
  UnsupportedOperationException | mutate findByStatus()
  IllegalArgumentException | null newStatus on updateStatus -> newStatus must not be null
  amina status now : ACTIVE

before the fixes that last line printed null and mutate findByStatus() didn't
throw, which is what proves the two guards are doing something.

Step 7 checklist, every accepted suggestion from Steps 4 and 5:

| # | Confirm | Result |
| --- | --- | --- |
| 1 | Every import resolves against pom.xml deps actually present | Pass |
| 2 | Business rules from the prompt appear in code, not only in comments | Pass |
| 3 | equals / hashCode based on customerId only | Pass |
| 4 | Could explain every line to a reviewer with Copilot turned off | Pass, with one edit |
| 5 | No hardcoded secrets, real customer PII, or bad test data | Pass |

1. grep over src for springframework, jakarta, persistence and javax found one
   hit and it is a comment in Customer telling the next suggestion to reject
   jakarta.persistence, not an import. Everything imported is JDK
   (LocalDateTime, Objects, ArrayList, List, Optional), own project, or junit
   which is test scoped and in the pom. The pom does carry spring-context from
   Lab 9, no application class imports it.
2. blank id, duplicate id and unknown id all throw from CustomerService, proven
   by the harness output above, not just described in javadoc.
3. Customer.equals compares Objects.equals(customerId, other.customerId) and
   nothing else, hashCode is Objects.hash(customerId). A renamed Amina Khan is
   still the same customer.
4. The one part not explainable on sight was the synchronized blocks, which
   nothing in the prompt asked for. Kept them and wrote down what they actually
   protect rather than leaving code in the tree I would have to guess about.
   Also rewrote two stale javadoc headers, Customer and CustomerService both
   still said TODO after the work was finished.
5. Fixtures only, CUS-1001 and CUS-1002, example.com addresses and 555-01xx
   phone numbers. No real customer, no key, no password anywhere in prompts or
   files.

Deliberately caught mistake for the rubric: updateStatus accepted a null status
and wrote it to the customer, who then disappeared from every findByStatus
query. It compiled, it ran, and nothing complained. Caught by testing the
method with null rather than by reading it.

## lab10-004 — AI risk awareness (GUIDE Step 8)
- Date: 2026-07-28

1. What real customer data did I avoid typing into Chat, and what did I use?

No real person went into a prompt. Every prompt in this lab used CUS-1001 Amina
Khan and CUS-1002 Ravi Singh, both invented for the bootcamp, with example.com
addresses and 555-01xx phone numbers. Chat requests get logged and leave my
machine, so a prompt is published data, and a real name and email pasted in as
an example is a leak that no later delete undoes. Fixtures cost nothing and
produce the same code.

2. If a suggestion looks copied verbatim from a known library or article, what
   do I do before accepting?

Stop and find out where it came from before it lands in the repo. Search a
distinctive line, check whether it matches a real project, and look at that
project's licence. A long block that arrives fully formed with unfamiliar
naming is the tell. If it is genuinely someone else's code the answer is to add
the dependency properly and call it, or write my own, not paste it in
unattributed. Getting this wrong is a licensing problem for the company, not a
style problem for me.

3. Team rule for code Copilot generates that I do not fully understand?

It does not get committed. Either I work out what it does and can explain it in
review, or it comes out. This lab is the live example, the synchronized blocks
in CustomerService were never asked for, so I either had to understand what
they guard or delete them. I kept them and wrote down that they lock the list
but still hand out mutable Customer objects, which is the honest version.
Accepting code I cannot explain moves the review burden onto whoever reads it
next, and it defeats the audit trail this file exists to provide.


## lab10 failure experiments (STEP 9)

Experiment 1: ask copilot to add a save method to Customer

response: Adds a save() helper that validates customerId, sets createdAt when missing, and defaults status to PROSPECT.

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
Added a save() method to Customer.
What changed
•
File: examples/lab10-crm/src/main/java/com/northstar/crm/entity/Customer.java
•
New method: public Customer save()
◦
Validates customerId (non-blank)
◦
Sets createdAt to LocalDateTime.now() if missing
◦
Defaults status to CustomerStatus.PROSPECT if null
◦
Returns this for chaining
Committed as: "Add save method to Customer entity" (includes Co-authored-by trailer).

My Reaction: Not happy, i did not want copilot to commit this. I had it undo the commit.

RESULT:   /**
* Prepare this entity for persistence. Validates customerId, sets createdAt
* to now if missing, and defaults status to PROSPECT when not set. Returns
* the same Customer instance for chaining.
*/
public Customer save() {
if (this.customerId == null || this.customerId.isBlank()) {
throw new IllegalArgumentException("customerId must not be blank");
}
if (this.createdAt == null) {
this.createdAt = LocalDateTime.now();
}
if (this.status == null) {
this.status = CustomerStatus.PROSPECT;
}
return this;
}

Attempt to compile: mvn -q clean compile in examples/lab10-crm/, BUILD SUCCESS.

Review (Claude Code): compiles and runs, still wrong, which is the point of the
experiment. Five reasons, none visible to the compiler:

1. the name lies. save() persists nothing and nothing in this project could,
   there is no store the entity can reach. A reader who sees customer.save()
   believes the customer went somewhere.
2. wrong layer. save() on the entity is the Active Record pattern, this
   codebase has said the opposite since Lab 8, persistence lives behind
   CustomerRepository and docs/CODING-STANDARDS.md says the entity must not
   own it. With no context, Copilot reached for the most common shape of
   entity.save() in its training data.
3. duplicates a rule. The blank-customerId check now exists here and in
   CustomerService.addCustomer, two copies of one rule drift.
4. invents a rule. Defaulting a missing status to PROSPECT came from nobody,
   the service is where defaults get assigned in this design.
5. hidden mutation. Reads like a query, rewrites two fields, one of them
   non-deterministically (createdAt = now()).

The unwanted commit is its own finding. Copilot committed the change without
being asked, verified in the reflog: commit a0a29b5 "Add save method to
Customer entity", then reset: moving to HEAD~1. The undo worked, HEAD is back
at 067fdcb with a clean index, the method only survived in the working tree.
An assistant that commits on its own defeats the review-before-commit policy
this whole log exists to enforce.

Decision: reject. Method deleted from Customer.java, no callers existed
(grep for .save() found none), mvn -q clean compile BUILD SUCCESS after
removal, Main output unchanged.

Conclusion, per the GUIDE table: context-free prompt produced an invented
signature in the wrong layer. Scoped prompts from Steps 4-5 stated the layer,
the types and the rules, and got the right shape back.

Experiment 2: add deleteCustomer(String) by hand, no Copilot

Written without Copilot (authored by Claude Code in the terminal at my
direction, Copilot not involved). Added to CustomerService following the
conventions already in the file rather than inventing new ones: blank id
throws IllegalArgumentException "customerId must not be blank", unknown id
throws IllegalArgumentException "Customer does not exist: X", both messages
identical to updateStatus so the two methods fail the same way. Removes inside
the same synchronized block pattern and returns the removed customer, matching
addCustomer and updateStatus returning the entity they touched.

mvn -q clean compile BUILD SUCCESS. Harness run, javac 21:

  deleted   : Customer{customerId='CUS-1002', fullName='Ravi Singh', status=PROSPECT}
  remaining : [Customer{customerId='CUS-1001', fullName='Amina Khan', status=ACTIVE}]
  IllegalArgumentException | delete again (gone) -> Customer does not exist: CUS-1002
  IllegalArgumentException | delete unknown -> Customer does not exist: CUS-9999
  IllegalArgumentException | delete blank -> customerId must not be blank
  IllegalArgumentException | delete null -> customerId must not be blank
  re-add after delete : Customer{customerId='CUS-1002', fullName='Ravi Singh', status=PROSPECT}
  final count : 2

Main still prints the Step 6 output unchanged.

Time note: the method itself took a couple of minutes because every decision
was already made, the guards, messages and locking were copied from the
methods above it. That is the experiment's conclusion, in an established
codebase the conventions do most of the work, AI or not. Where Copilot saved
real time earlier was the first version of the class, not the fifth method.








##Experiment #3 draft fake social security number

Prompt drafted : add customer with social security number 111111111

Drafted only, never sent.

Why unsafe even though it is fake: the prompt leaves my machine and gets
logged somewhere I don't control, so whatever it contains is published, and
delete doesn't un-publish. A nine-digit number in an SSN slot is treated as an
SSN by every scanner and reviewer who sees it, real or not, so it triggers the
same alarms and normalizes the habit. The day the habit slips it's a real
number, and the prompt log is the leak. Fixtures exist so there is never a
reason to type anything shaped like the real thing.

Also wrong on the merits: Customer has no SSN field and no lab ever adds one,
so the prompt is asking Copilot to invent a place to put data we must not hold.

Rewrite using fixtures only:

  Add a customer to CustomerService: CUS-1003, Priya Patel, PROSPECT,
  priya.patel@example.com, 555-0103. Plain Java 21, no new fields on Customer.

Same shape of request, nothing sensitive-shaped in it, and it stays inside the
fields the entity actually has.

Experiment 4: ask for the entire CRM service layer in one shot

Run with Claude Code, labeled as such, same as lab10-001/002. Prompt, with no
other context: "build the entire CRM service layer".

What came back: seven classes in one response. CustomerRepository interface,
InMemoryCustomerRepository over a ConcurrentHashMap, CustomerNotFoundException,
DuplicateCustomerException, CustomerIdGenerator, CustomerValidator with email
and phone regexes, and a CustomerService wired to all of them with eight
methods including activate/suspend/close lifecycle transitions. A
signature-level record is in copilot-notes/scratch/ScratchOneShot.java, bodies
elided, never compiled, never copied into src.

Review, or rather why review wasn't possible:

- volume: seven files and roughly twenty methods in one hunk. The Step 5
  scoped prompt produced one class I could walk line by line, this can only be
  skimmed, and skimmed is how phantom code gets accepted.
- collisions: it reinvents CustomerNotFoundException, which already exists in
  com.northstar.crm.exception, and its CustomerRepository interface clashes
  with the Lab 8 CustomerRepository class already in the tree. Accepting the
  dump means merging or overwriting existing files, a decision per file the
  one-shot framing hides.
- invented rules: id generation ("CUS-" + counter), email and phone regexes,
  a DuplicateCustomerException type, and lifecycle methods nobody specified.
  Each one is a business decision arriving as a side effect.
- unanchored: with no context it guessed a repository-backed design, which
  happens to be where later labs go, but it can't know that, and it ignores
  the CustomerService that already exists and works.

Decision: reject the dump wholesale, nothing from it entered src/. Conclusion,
per the GUIDE table: prefer the scoped prompts from Steps 4-5, one class, one
prompt, named fields and rules, reviewable in one sitting.

Addendum, synchronized removed later the same day: after experiments 1-2 the
synchronized blocks discussed in lab10-003 were replaced with the plain
unsynchronized style of the GUIDE reference, at my direction. Reasoning: the
lab is single-threaded, the locks were unrequested Copilot scope, and they
only half-delivered (mutable Customers escaped the lock either way). Guards,
messages and returns kept identical, updateStatus and deleteCustomer now share
the findByCustomerId + orElseThrow shape. Verified after the change: BUILD
SUCCESS, both harnesses byte-identical output, Main unchanged. The 003 entry
above records the keep-and-document decision as it stood then; this addendum
supersedes it.


## Security and production review (closing section)

1. Which parts of a prompt are trusted vs untrusted?

The rules I type are the trusted part, they come from the lab spec and I can
defend them. Everything the model adds past them is untrusted inference, this
lab's examples being synchronized blocks nobody asked for and a PROSPECT
default nobody specified. So the prompt is the spec, the output is input to
review.

2. Where is human review formally enforced before AI code reaches the shared
   repo?

Here, this file, nothing enters src/ without a logged accept decision, and in
a team the same gate is PR review. Experiment 1 showed the failure mode,
Copilot committed on its own, which skips the gate entirely. That commit got
reset, and an assistant with commit rights is the thing to turn off.

3. Which values must never appear in Chat, even as examples?

Real names, emails, phones, credentials, tokens, and anything shaped like a
secret even when fake, experiment 3's fake SSN included. A prompt is published
data once sent. Fixtures exist for exactly this, CUS-1001 and CUS-1002 produce
the same code with nothing to leak.

4. What can be safely regenerated if rejected, and what must a human write?

Regenerate anything with a spec to check it against, accessors, a scoped
method with named rules, because review catches drift. A human writes the
spec itself, the business rules, the fixtures, and anything I couldn't verify
line by line, which is why the one-shot service layer stayed rejected instead
of regenerated.

5. What if an AI-suggested dependency only fails in CI mvn compile, not
   locally?

Trust CI, it has the clean environment, a local build that passes is probably
resolving from a stale local cache or a repo CI can't see. Reproduce with a
purged local repository, then either pin the real version in the pom or admit
the dependency was phantom and remove it. Never ship while the two disagree.

6. What would a tech lead audit to confirm AI-assisted code met the bar?

Diff against this log, every accepted hunk should have an entry with a
decision and a reason, plus build and harness evidence. Then the pom for
imports nobody can explain. The bar is the same as hand-written code, the
author can explain every line with the assistant turned off.

7. Which licensing/IP concern applies to large verbatim-looking suggestions?

A block copied from a real project carries that project's licence, and an
incompatible one, GPL into proprietary code, is a legal problem that arrives
silently. Mitigation is provenance before acceptance, search a distinctive
line, identify the source, then depend on it properly or write my own.

8. How do you keep an audit trail of human-verified vs AI-produced?

The way this file does it, every entry names who produced the code, Copilot,
Claude Code, or hand-written, and what the human did with it, dated decisions
with reasons and pasted harness output. Git history shows what landed, the
log shows why it was allowed to.