main frame is created.

Person is allocated; main.person refers to it.

Calling printPerson creates a second frame.

Both frames temporarily hold references to the same Person.

printPerson returns; its frame is removed.

main returns; its frame is removed.



An object is not collectible merely because one reference becomes null.
It becomes GC-eligible only when no live strong-reference path can reach it.
Eligibility does not guarantee immediate collection, and System.gc() is only
a request.

The program allocated about 250 MB over time despite a 64 MB maximum heap.
GC log entries appeared between rounds. A before/after value that decreased
shows that memory was reclaimed. Exact pause times varied on my machine.


Command:
java -XX:+UseG1GC -Xms16m -Xmx64m -Xlog:gc GcObserve

Evidence:
The log began with "Using G1" and showed G1 evacuation pauses.
The collector flag selects G1; it does not guarantee a particular pause time.



Command:
java -XX:+UseZGC -Xms16m -Xmx64m -Xlog:gc GcObserve

Evidence:
The log began with "Using The Z Garbage Collector" instead of "Using G1".
Pause-related log lines look different — ZGC does most of its work concurrently,
so it does not report the same kind of stop-the-world "Evacuation Pause" G1 does.


loaded RetentionDemo class
→ static CACHE field
→ ArrayList entries
→ byte[] objects

Root cause: a long-lived static collection retained strong references after
the data was no longer needed. GC could not reclaim reachable entries.

Fix: clear/remove entries, bound the cache, apply eviction, or use a more
appropriate lifecycle. Weak references are not a universal cache fix.


| Run | String ms | StringBuilder ms |
| --- | ------- | ---------------- |
| 1 | 251.021| 2.416 ms|
| 2 | 256.865| 3.664|
| 3 |226.615 |4.784 |
Trends show String is 100x longer than Stringbuilder. Also, Stringbuilder increases while String decreases

Use StringBuilder when constructing text repeatedly in a loop. Ordinary + remains readable and appropriate for a small, fixed expression.

