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