# Collection choices

| Scenario | Interface  | Implementation | Why                 |
| -------- |------------|----------------|---------------------|
| Ordered catalog; duplicate titles allowed | List<book> | ArrayList      | non-unique, ordered |
| Unique registered book IDs | Set<String | HashSet        |  Fast average membership; no duplicates                   |
| Book ID → current borrower ID | Map<String,String> | HashMap        | easy access         |
| Alphabetically sorted categories | Set        | TreeSet        |    Unique and naturally sorted                 |
| Category → count, sorted by category | Map        | TreeMap        |        Key-value data with sorted keys             |
| Checkout history in event order | List       | ArrayList      |Append and iterate in event order|


1. If unique IDs must also preserve registration order, what changes?

Change the implementation from `HashSet` to `LinkedHashSet`; the interface stays `Set<String>`. `LinkedHashSet` threads a doubly-linked list through its entries, so iteration follows registration (insertion) order while still rejecting duplicates and keeping O(1) average membership checks. (Use `TreeSet` only if you want IDs sorted by natural order rather than the order they were registered.)
Reference: LinkedHashSet.


2. If borrower lookup must preserve insertion order for display, what changes?

Change the implementation from `HashMap` to `LinkedHashMap`; the interface stays `Map<String,String>`. `LinkedHashMap` keeps a doubly-linked list threaded through its entries in insertion order, so iterating the map for display shows borrowers in the order they were added, while keeping the same O(1) average `get`/`put` as `HashMap`. (Use `TreeMap` only if you need keys sorted by natural order rather than insertion order.)
Reference: LinkedHashMap.


3. If many insertions/removals occur in the middle, is LinkedList automatically best?

No. `LinkedList` inserts/removes in O(1) only when you already hold the position (an iterator or node reference); locating the middle first costs O(n) because it can't index directly. `ArrayList` indexes in O(1) but shifts elements on a middle insert/remove (also O(n)). In practice `ArrayList` often wins anyway thanks to contiguous memory and cache locality, and `LinkedList` carries higher per-node memory overhead — so measure your real access pattern rather than assuming.
Reference: No. Access pattern and traversal cost matter; measure rather than assume.