Lab 6  Employee Analytics System
Module 6  Streams and Lambda Expressions
package com.academy.analytics


OVERVIEW

Console app that loads 25 sample employees and runs stream-based analytics over them.
No database, no framework, plain JDK 21. Menu-driven (Main), analytics live in a service
layer (EmployeeService), reports compose those queries (ReportService).

Classes:
  Employee         domain model (id, name, department, salary, experience, rating, active)
  EmployeeData     static factory, 25-row sample roster (2 inactive)
  EmployeeService  all the stream pipelines and analytics helpers
  ReportService    dashboard + composed reports
  Main             scanner loop, menu, switch dispatch

Menu 1-9 is the graded core (display, by-department, salary report, top performers,
highest salary, department stats, active employees, dashboard, exit). 10-21 are demo
and bonus options.


COMPILE / RUN

From examples/Lab6-EmployeeAnalytics/ :

  javac -d out src/com/academy/analytics/*.java
  java -cp out com.academy.analytics.Main

Windows PowerShell, if the *.java glob acts up, name the five files:

  javac -d out src\com\academy\analytics\Employee.java src\com\academy\analytics\EmployeeData.java src\com\academy\analytics\EmployeeService.java src\com\academy\analytics\ReportService.java src\com\academy\analytics\Main.java
  java -cp out com.academy.analytics.Main


STREAMS / FUNCTIONAL INTERFACES / LAMBDAS

Full checklist with the method each one is used in: see stream-operations-table.md
(in notes/Week 1/Module 6/).

Short version:
  intermediate  filter, map, sorted, distinct, limit, skip, mapToDouble
  terminal      forEach, count, reduce, max, findFirst, collect, toList
  collectors    toList/toSet, groupingBy, partitioningBy, summarizingDouble,
                averagingDouble, counting, custom Collector.of
  interfaces    Predicate, Function, Consumer, Supplier, Comparator
                (all shown in demonstrateFunctionalInterfaces)


SAMPLE DASHBOARD (menu option 8)

  =============================
  Employee Analytics Dashboard
  =============================
  Employees : 25
  Average Salary : 100680
  Highest Salary : 165000
  Lowest Salary : 48000
  Departments : 5
  Top Performer : John Smith (Rating 5)
  Highest Paid Department : IT
  Top 5 Highest Salaries
  1 John Smith - 165000
  2 Alice Johnson - 152000
  3 David Lee - 149000
  4 Sarah Brown - 141000
  5 Michael Chen - 138000
  Active Employees : 23
  Inactive Employees : 2


OBSERVATIONS

- filter -> map -> collect reads straight down the page, much easier to follow than the
  nested loops this would have been before streams.
- a stream is one-shot. every query rebuilds employees.stream(), the terminal op consumes it.
- Optional on the "find one" methods (highest paid, top performer) means the caller deals
  with the empty case instead of me returning null and hoping.
- groupingBy + a downstream collector (summarizingDouble / counting) does the whole
  per-department aggregation in one pass, no manual bucketing.
- method ref when it just forwards (Employee::getName), lambda when it does more
  (e -> e.getSalary() > 80_000).
- groupingBy hands back a HashMap, key order isn't guaranteed. copy to TreeMap when the
  printed report needs to be sorted.

Reflection answers: notes/Week 1/Module 6/lab6-answers.md
Screenshots: notes/screenshots/lab-6/
