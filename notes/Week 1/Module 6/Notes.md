Module 6: Streams and Lambdas (exercise notes)

Exercise 1: Lambda and a Custom Functional Interface

employee -> employee.salary() > 60_000 reads as: given one Employee, return whether salary > 60000. Param type never written, Java infers Employee from the target type SalaryCheck (single abstract method, takes Employee returns boolean).

@FunctionalInterface doesn't make the lambda work, one abstract method does. The annotation just makes the compiler fail fast if a 2nd abstract method ever gets added.

anon class vs lambda = identical results, same contract. anon writes all the boilerplate (new SalaryCheck() { @Override public boolean test(...) }), lambda keeps only the param + body.
salary is private in a record so use the accessor employee.salary(), employee.salary doesn't compile.

Threshold check (Step 5):
  60000 → Alice (72000) anon true, lambda true
  75000 → false, false
put it back to 60000 for Ex 2-7, they reuse this Employee/EmployeeData set.


Exercise 2: Filter Employees by Salary

Step 1 prediction, before running anything:
  Alice   72000  keep
  Bob     65000  keep
  Charlie 80000  keep
  Diana   90000  keep
  Evan    55000  discard
4 keeps 1 discard, matched.

filter is intermediate. only describes what the pipeline does, returns another stream, nothing runs on that line. work happens at the terminal toList(). drop the .toList() and the predicate never runs at all → "nothing happens" is the symptom of a missing terminal op, not an error.

toList() = new unmodifiable list, source untouched. source stayed 5, filtered 4 every run.

Boundary test (Step 4), Bob sits exactly on 65000:
  > 60000   → Alice, Bob, Charlie, Diana          size 4
  > 65000   → Alice, Charlie, Diana (Bob out)      size 3
  >= 65000  → Alice, Bob, Charlie, Diana (Bob back)  size 4

back to > 60000. heading is a fixed string so it still says "Employees above 60000" during the boundary runs, the rows change not the label.



Exercise 3: Map Employees to Names

type trace:
  before map   Stream<Employee>
  fn           Employee::name
  after map    Stream<String>
  final        List<String>

map changes element type not count, 5 in, 5 names out, source order. that's the diff from filter (keeps type, changes count). stream holds String after mapping so the result has to be List<String>, List<Employee> won't compile.

Employee::name and employee -> employee.name() gave byte-identical output. method ref is just shorthand for a lambda whose body is one call on the param, no args passed → name written without parens.

Step 4: mapper can build any value not just return a field. employee -> employee.name().toUpperCase() printed ALICE BOB CHARLIE DIANA EVAN. can't write that as a single method ref, which is exactly when you need the lambda form. restored Employee::name.

names.forEach(System.out::println), method ref on an object not a type, means "call println on that specific stream", takes each name as the arg.


Exercise 4: Highest and Lowest Salary

prediction, ascending by salary:
  1 Evan    55000  ← min
  2 Bob     65000
  3 Alice   72000
  4 Charlie 80000
  5 Diana   90000  ← max

min and max are opposite ends of one ordering so the same bySalary comparator drives both, no reversed comparator needed. reversing instead of swapping the method gives the same answers anyway, reusing the comparator is just clearer intent.

comparingDouble(Employee::salary) compares numerically. comparing a formatted salary string would sort lexicographically where "90000" comes before "9500", real bug, silently returns the wrong employee instead of failing.

both terminal reductions → each needs its own fresh employees.stream(). can't reuse a stream after a terminal op.


Why Optional here:
empty source has no highest/lowest and there's no sensible Employee to invent. returning null just pushes the problem to the caller silently. Optional<Employee> makes the empty case part of the return type so the caller has to deal with it.

Step 3 against List.of():
  Optional.empty
no exception because orElseThrow() wasn't called on it. orElseThrow() is only safe on the sample list because that list is known non-empty; on the empty stream it throws NoSuchElementException. removed the temp check after.



Exercise 5: Map a 10% Salary Raise

hand-check: 72000 × 1.10 = 79200. matched.

non-mutation (Step 3): map produced a new list of proposed values, did NOT touch the immutable Employee records. transformation not mutation. nothing assigned back into an Employee, each proposed salary is a brand new Double derived from an existing one, collected into a separate List<Double>. proof is the last line, Alice's original still prints 72000.00 after the whole pipeline. a record has no setters so mutating the source isn't just discouraged, it's impossible, the only way to express a change is produce a new value.

(also why output pairs each name with its proposal by index: map preserves encounter order + one output per input, so position i in proposedSalaries belongs to employee i.)

1.10 not 10, 1.10 is "original plus 10%", 10 would be a 900% raise.

Step 4, rule → 1.05:
  Alice   72000   ×1.10 79200.00   ×1.05 75600.00
  Bob     65000   ×1.10 71500.00   ×1.05 68250.00
  Charlie 80000   ×1.10 88000.00   ×1.05 84000.00
  Diana   90000   ×1.10 99000.00   ×1.05 94500.00
  Evan    55000   ×1.10 60500.00   ×1.05 57750.00
Alice's 5% came out 75600.00 as expected. put the 10% back. her original stayed 72000.00 under both rules → changing the rule only changes the derived list.


Exercise 6: Count Employees by Department

prediction:
  Finance → Diana
  HR      → Alice, Charlie
  IT      → Bob, Evan

grouping key vs downstream collector, groupingBy makes two separate decisions. classifier (Employee::department) decides which bucket each element lands in → the keys. downstream collector (Collectors.counting()) decides what each bucket stores → the values. change the first and the groups change, change the second and only the per-group value changes.

Step 3:
1. Long not Integer? counting() returns Long (matches Stream.count()) so a group can exceed Integer.MAX_VALUE. map has to be Map<String,Long>, Map<String,Integer> won't compile.
2. without counting()? single-arg groupingBy defaults to toList() so each value becomes the list of matching employees not a number. verified: {Finance=[Diana record], HR=[Alice, Charlie], IT=[Bob, Evan]}.
3. TreeMap only for presentation? groupingBy returns a HashMap (checked with getClass()), key order isn't a contract, same point as the Module 5 note. the aggregation itself is correct whatever the order, TreeMap is just copied over the result so the printed report comes out deterministic + alphabetical.

Step 4, add Employee(6, "Fatima", "Finance", 70000):
  Finance  1 → 2
  HR       2 → 2
  IT       2 → 2
only Finance moved, classifier put Fatima there alone. removed her after so Ex 7 + Lab 6 expectations stay stable.



Exercise 7: Compose a Pipeline for HR Names

element type after each op:
  filter  Stream<Employee> → Stream<Employee>
  map     Stream<Employee> → Stream<String>
  sorted  Stream<String>   → Stream<String>
  toList  Stream<String>   → List<String>

why filter before map (Step 4):
department lives on the Employee. once map(Employee::name) runs each element is a bare String, carries a name and nothing else, employee.department() isn't callable any more so the compiler just rejects the filter. filtering has to happen while the elements still carry the field the predicate needs. it's a type constraint not a style thing, though it's also faster (filter first → map + sorted handle 2 elements instead of 5).

sorted() after map so it sorts names alphabetically on the natural String order. sorting before map would try to order Employee records, no natural order, would need an explicit comparator.

Step 3 case sensitivity, Alice's dept set to "hr":
  equals("HR")            → [Charlie]
  equalsIgnoreCase("HR")  → [Alice, Charlie]
equals compares exact chars so "hr" and "HR" are different strings and Alice silently disappears, no error, just a quietly wrong report. put it back to "HR", kept equalsIgnoreCase as the more tolerant one like the exercise note said.


Exercise 8: parallelStream correctness bonus

why it's safe to parallelise: predicate reads one immutable Employee, returns a boolean, writes no shared state → no element's result depends on another element or on visit order. count() is a built-in reduction so the library does the combining, not me.

five runs, counts were 4 and 4 every single run. correctness never varied, that's the actual point:

  run  sequential ns  parallel ns  faster
  1    17,190,200     2,861,600    parallel
  2     7,373,400     2,108,100    parallel
  3    30,429,500     7,439,300    parallel
  4    21,823,100     4,635,700    parallel
  5    17,877,300     6,246,700    parallel

parallel "wins" 5/5 but this is NOT a performance result. 12 processors on the machine but the dataset is 5 rows, there's basically no work to split.

what it's really measuring is whichever pipeline runs first. the first stream pays the one-time costs, loading stream classes, linking the lambda call sites, JIT-compiling the pipeline, and the second one reuses all of it warmed up. sequential is written first so sequential eats the cost.

proof: ran an order-swapped copy that times parallel first:

  run  parallel ns (first)  sequential ns (second)  faster
  1    14,220,900           801,000                 sequential
  2    16,633,800           1,040,700               sequential
  3    20,341,400           1,105,100               sequential
  4    18,630,200           516,600                 sequential
  5    11,915,800           484,200                 sequential

flips 5/5 just by reordering the two blocks. sequential-second even drops under a millisecond, ~35× faster than sequential-first. so the timing is measuring JVM warm-up order, not parallelism. a real answer would need a proper harness (JMH) + a workload big enough to be worth splitting. conclusion from these runs: none.

Step 4: why a shared mutable counter is unsafe:
  int[] count = {0};
  employees.parallelStream().forEach(employee -> count[0]++);
count[0]++ looks like one step, it's three: read, add one, write back. parallel workers run those on several threads at once with no sync, so two threads read the same value, both add one, both write back the same result → two increments become one. update lost. nothing throws, you just get a wrong number.

measured in the scratchpad over 500,000 elements:
  run  shared int[]  lost     count()   AtomicLong
  1    101,316       398,684  500,000   500,000
  2     84,869       415,131  500,000   500,000
  3     79,143       420,857  500,000   500,000

~80% of the increments just vanished and the wrong answer was different every run, textbook data race. worse than a crash because a wrong count looks totally plausible sitting in a report.
doesn't reproduce on the 5-row set, too few elements to collide. a race that's invisible small and wrong at scale is the whole reason the rule is "don't share mutable state" instead of "test it and see".

count() and AtomicLong both returned 500,000 every time. prefer the built-in reduction, it says the intent and the library handles per-thread accumulation + combining.
