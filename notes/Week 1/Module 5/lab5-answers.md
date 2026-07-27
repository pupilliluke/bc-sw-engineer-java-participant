Lab 5  Library Management System
Module 5  Java Collections Framework


COLLECTION MAPPING (field → type → why)

  books             ArrayList<Book>          ordered catalog, index access + iteration. dupes guarded by bookIds
  members           ArrayList<Member>        same, ordered roster
  bookIds           HashSet<String>          O(1) duplicate-ID check before inserting a book
  memberIds         HashSet<String>          O(1) duplicate-ID check before registering a member
  borrowRecords     HashMap<String,String>   bookId → memberId. who holds this book, O(1). remove key = return
  categories        TreeSet<String>          unique categories, sorted for the insights output
  categoryBookCount TreeMap<String,Integer>  category → count, sorted keys for tidy reports
  borrowHistory     ArrayList<BorrowRecord>  ordered audit trail of every borrow
  borrowFrequency   HashMap<String,Integer>  bookId → times borrowed, ranks top-5


REFLECTION

1. List over Set when?

order matters or dupes ok, or I need index access. Catalog is a List, shown in insertion order, iterated by index. Uniqueness is the ID Set's job, kept separate.

2. Why HashSet check before adding a book ID?

contains() is O(1), rejects a dupe instantly. List.contains() is O(n), gets slower as the catalog grows.

3. Map for "currently borrowed" vs just a boolean?

Boolean only says if it's out, not who has it. borrowRecords (bookId → memberId) answers who holds it in O(1). Remove the key on return, state stays clean. I keep the available flag in sync too so listings read right.

4. HashMap vs TreeMap here?

HashMap for borrowRecords + borrowFrequency, just need fast lookup, order irrelevant. TreeMap for categoryBookCount, want sorted keys so reports print tidy without re-sorting each time. Same idea HashSet vs TreeSet: IDs unordered, category names sorted.

5. Comparable vs Comparator for books?

Comparable = natural order on Book (compareTo by title, case-insensitive), used by Collections.sort(books). Comparator (BookComparator by price) = external, swappable strategy → can offer price/author/category sorts without touching Book.

6. Iteration style in production, why?

forEach / streams. Readable, intent is clear. Explicit Iterator only when I have to remove during traversal. Indexed for only when I actually need the index.

7. CRM: customer list / unique emails / id→customer?

list → ArrayList<Customer>. unique emails → HashSet<String>. id lookup → HashMap<String,Customer>.


MANUAL VERIFICATION

  1  invalid input (abc/empty) → "Invalid choice" → menu returns   Pass
  2  add 101 / member 1 / borrow / reports                         Pass
  3  duplicate book ID 101 → Book already exists.                  Pass
  4  display books shows iteration style                           Pass
  5  sort by title reorders                                        Pass
  6  category insights lists Programming                           Pass
  7  exit 11 → Thank You, process ends                             Pass

Sample Reports output:

  Reports
  Books : 1
  Borrowed : 1
  Available : 0
  Members : 1
  Most Popular Category : Programming


CHECKPOINTS

A: packages + models (package com.academy.library; on all files): Pass
B: collections wired, dupe IDs rejected, borrow uses HashMap: Pass
C: javac -d out compiles, menu + sample session run, exit prints Thank You: Pass
D: screenshots under notes/screenshots/lab-5/, this file: Pass


PERF COMPARISON (menu 14)
runPerformanceComparison() times ArrayList vs LinkedList with System.nanoTime().
100,000 elements, 50,000 front inserts, 10,000 index reads. Times vary per run.

  operation                  ArrayList   LinkedList
  add at end                 9.68 ms     8.81 ms
  insert at front            139.70 ms   5.71 ms
  random access (by index)   0.48 ms     514.41 ms

Add at end: about equal, both amortize to O(1).
Front insert: LinkedList wins big, relinks head (O(1)) vs ArrayList shifting every element (O(n)).
Random access: ArrayList wins big, index math (O(1)) vs LinkedList walking the chain (O(n)).

Takeaway: default ArrayList for index/iteration-heavy work like this catalog. LinkedList only when the work is mostly insert/remove at the ends.


BONUS
Done: borrow history (15), top-5 borrowed (16), partial-title search, multi-field sort, export library-report.txt (17), perf comparison (14).
