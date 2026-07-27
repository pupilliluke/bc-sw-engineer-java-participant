# Stream Operations Table

| Operation / API | Used? | Where (method / menu) | Notes |
| --------------- | :---: | --------------------- | ----- |
| Lambda `forEach` | Yes | displayAllEmployees (menu 1), demonstrateLambdas (menu 10) | names/salaries/departments blocks |
| `Predicate` | Yes | highEarner in demonstrateFunctionalInterfaces (menu 11) | salary > 100000 test, also every filter predicate |
| `Function` | Yes | employeeSummary (menu 11) | Employee -> "name (dept)" string |
| `Consumer` | Yes | printRating (menu 11) | side-effect print, name + rating |
| `Supplier` | Yes | topSample (menu 11) | supplies highest-paid via max() |
| `filter` | Yes | displayHighSalaryEmployees (13), displayItEmployees, displayActiveEmployees (7), displayFilteredItTopPerformers (14) | 14 chains three filters, reads like SQL AND |
| `map` | Yes | demonstrateMapping (15), displayDistinctDepartments (17) | project to name/salary/dept |
| `sorted` | Yes | demonstrateSorting (16), getTopSalaries, displayTopAndNextSalaries (18) | comparingDouble + reversed() |
| `distinct` | Yes | displayDistinctDepartments (17), findSecondHighestSalary (21) | unique depts / unique salaries |
| `limit` / `skip` | Yes | displayTopAndNextSalaries (18), getTopSalaries | top 5 then skip 5 limit 5, same sort both times |
| `count` | Yes | displayCounts (19), displayDashboard (8) | long, not int |
| `reduce` | Yes | displayReductions (menu 3) | reduce(Double::max) / reduce(Double::min) for salary |
| `collect(toList/toSet)` | Yes | demonstrateCollectors (20), getTopSalaries uses stream.toList() | active list, dept set |
| `groupingBy` | Yes | displayGroupedEmployees (2), getDepartmentStatistics (6), generateSalaryHistogram (21) | classifier = keys, downstream collector = values |
| `partitioningBy` | Yes | displayPartitionedEmployees (menu 3) | salary > 100000 true/false split |
| `summarizingDouble` | Yes | displaySummaryStatistics (3), getDepartmentStatistics (6), displayDashboard (8) | max/min/avg/sum/count in one pass |
| `Optional` (`max` / `ifPresent`) | Yes | displayHighestPaidEmployeeOptional (5), findTopPerformer (8), findSecondHighestSalary (21) | ifPresentOrElse covers empty case, no NPE |
| Method references | Yes | Employee::getName, Employee::isActive, System.out::println, this::salaryBucket | everywhere the lambda just forwards |
| Dashboard composed report | Yes | menu 8 | 25 employees, avg 100680, active 23 / inactive 2 |

Example lambdas used:

  e -> e.getSalary() > 80_000                       filter predicate
  e -> "IT".equalsIgnoreCase(e.getDepartment())     dept filter, null safe
  Comparator.comparingDouble(Employee::getSalary).reversed()   salary desc
  Comparator.comparingInt(Employee::getRating).thenComparingDouble(Employee::getSalary)   rating then salary
  e -> e.getName().length()                         longest name (21)
  (department, list) -> { ... }                     map.forEach in displayGroupedEmployees
