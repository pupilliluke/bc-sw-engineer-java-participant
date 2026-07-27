Lab 6 Employee Analytics (reflection answers)


REFLECTION QUESTIONS

1. Advantages of Streams over loops?

A: You say what you want, not how. filter/map/collect reads top to bottom as a pipeline. No index bookkeeping, no temp lists, no manual counters. Also lazy, fuses into one pass and can short-circuit (limit/findFirst). Same code can go parallel with parallelStream.

2. When should Streams be preferred?

Transforming or aggregating a collection - filter/map/group/reduce. If the pipeline reads clearer than the loop, use the stream. Plain for loop still fine for simple index work, side effects, or when I need break/continue.

3. Difference between filter() and map()?

filter keeps the type, drops elements that fail the predicate. count down, type same.
map transforms each element, count same, type can change. 

4. Why is reduce() useful?

Folds the whole stream to one value. Used it in displayReductions, reduce(Double::max) / reduce(Double::min) for highest and lowest salary. Returns Optional so an empty stream doesn't blow up.

5. What does Collectors.groupingBy() do?

Splits the stream into a Map by a classifier. groupingBy(Employee::getDepartment) -> Map<String,List<Employee>>. With a downstream collector (counting, summarizingDouble) each group aggregates in one pass. that's how getDepartmentStatistics and the histogram work.

6. Benefit of using Optional?

"might be absent" is in the return type instead of a null I can forget to check. findHighestPaidEmployee returns Optional, caller handles empty with ifPresentOrElse/orElse. No NPE.

7. Why are Lambda Expressions more readable?

No anonymous-class scaffolding, just param and body. e -> e.getSalary() > 80_000 vs a whole new Predicate(){ test(...) } block. The behaviour sits inline where it's used.

8. When should method references be used?

When the lambda only forwards the arg unchanged. Employee::getName, System.out::println. Anything more than one plain call (expression, chaining) needs the lambda form back.

9. Which stream operation is terminal? Give three examples from your lab.

Terminal = actually runs the pipeline and consumes the stream. From the lab: forEach (displayAllEmployees), collect (getDepartmentStatistics), max (findHighestPaidEmployee). count/reduce/findFirst/toList also terminal.

10. How do Streams improve enterprise Java applications?

Most service code is filter records, project fields, group for reports. Streams express that directly, less boilerplate = fewer bugs, and the same filter/map/collect shape everywhere keeps the codebase consistent.

11. Forward look, CRM using filter / map / groupingBy on customers?

Same shape, different domain. "active customers in region X by lifetime value" = filter -> sorted(value desc) -> limit. customers-per-agent = groupingBy like department stats here. Not building CRM today, the pipeline judgment just carries over.


ALSO (from concepts to discuss)

Why postpone work until the terminal op? intermediate ops are lazy, they just describe the pipeline. nothing runs until a terminal op. lets it fuse into one pass and short-circuit. no terminal op = nothing runs at all.

Lambda vs extracting a method - lambda when short and one-off. name it (highEarner in demonstrateFunctionalInterfaces) when reused or non-trivial.

comparingDouble(Employee::getSalary).reversed() over hand-written compare: reads as intent, no sign/argument-order slip, numeric not lexicographic, and .thenComparing gives the tiebreak.


CHECKPOINTS

A (project + domain model): Pass
  src/com/academy/analytics/ exists. Employee + EmployeeData with 25 rows 
B (service + reports compile): Pass
  EmployeeService, ReportService, Main present. javac -d out compiles clean. menu shows CORE 1-9. exit 9 prints Thank You.
C (stream features): Pass
  lambdas + Predicate/Function/Consumer/Supplier in demonstrateFunctionalInterfaces. filter single + chained, map, sorted, distinct, limit/skip all work. count, reduce/summarizing, groupingBy, partitioningBy visible. Optional highest-paid via max, ifPresentOrElse covers the empty case so no NPE.
D (dashboard + evidence): Pass
  option 8 dashboard matches solution numbers (25 employees, Average Salary 100680). stream-operations-table.md filled, reflection answers drafted (this file). screenshots saved under notes/screenshots/lab-6. bonuses menu 10-21 done after CORE 1-9.


MANUAL VERIFICATION

  1   menu 1-9 appears, invalid abc -> invalid message -> menu returns   Pass
  2   choice 1 lists all employees, total 25                             Pass
  3   choice 2 departments with names indented                           Pass
  4   choice 3 reductions + summarizing + true/false partition           Pass
  5   choice 4 performers rating >= 4                                    Pass
  6   choice 5 highest paid via Optional (John Smith)                    Pass
  7   choice 6 per-dept count/avg/max/min                                Pass
  8   choice 7 active employees only                                     Pass
  9   choice 8 dashboard matches sample shape                            Pass
  10  choice 9 Thank You, recompiled after edits                         Pass


SUCCESS CRITERIA

0  Module 6 Exercises 1-7 Pass before Lab Step 1 (1-8 all Pass, see PASS CRITERIA): Pass
1  work in examples/Lab6-EmployeeAnalytics/ with package com.academy.analytics: Pass
2  CORE menu 1-9 run, Dashboard option 8 shows expected numbers (avg 100680): Pass
3  filter/map/sort/grouping/Optional pipelines all Streams, no nested loops: Pass
4  javac -d out src/com/academy/analytics/*.java + sample session succeed: Pass
5  stream-operations table + intermediate vs terminal explanation in notes (stream-operations-table.md, Notes.md Ex 2): Pass
6  screenshots under notes/screenshots/lab-6/ no secrets: Pass


Evidence: screenshots in notes/screenshots/lab-6/ and lab-6/bonus. 1.png menu/display, 2.png by-department, 3.png salary report, 8.png dashboard (Average Salary 100680), 9.png exit.
