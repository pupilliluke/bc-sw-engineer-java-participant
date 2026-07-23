
| Objects | Used Memory (approx) | Execution Time |
| ------- |----------------------|----------------|
| 10 | 0                    | 1              |
| 100 | .46                  | 0              |
| 1,000 | .46                  | 0              |
| 100,000 | 10.92                | 6              |
| 1,000,000 | 106.56               | 142            |



1. Stack vs Heap?

A: Reference map, Object/array storage. Stack = per-thread frames holding primitives + references (the locals of each method call); Heap = shared memory where the actual objects/arrays live. 

2. Why locals on the Stack? 

A local belongs to its method's stack frame, which is pushed when the method is called and popped when it returns (LIFO). That makes allocation/cleanup    
automatic and instant, no GC needed

3. Why objects on the Heap?

They are larger, also have longer lives.  Objects can outlive the method that created them and be shared across methods (and threads) via references, so their lifetime isn't tied to a single  
frame, they need the shared, GC-managed heap.


4. When is an object GC-eligible?

When all references are gone. An object is GC-eligible when no live thread can reach it from a GC root.

5. Does `System.gc()` guarantee collection?

No. Only a suggestion

6. What caused the leak?

Reference remained, Garbage collection couldn't remove it. 

7. How did clearing the list fix it?

Clearing the list cleared the references to the list in heap. GC can now collect it.

8. Why are WeakReferences useful?

WeakReference does not pin its target — once only weak refs remain, GC can collect it and get() returns null. Useful for caches and listeners

9. What happens when the heap is exhausted?

The JVM throws OutOfMemoryError. Java heap space, it can't allocate and can't free enough reachable memory.

10. Which laptop tool would you try first for rising heap—and why?

Jps/Jstat. ease of setting it up. 

11. How could a CRM unbounded cache repeat this leak?

A CRM cache that adds every customer and never evicts keeps them all reachable → used heap climbs → eventually OutOfMemoryError. 


LMS OVERVIEW:
Tools used, JPS/Jstat. 

The leak was caused by a long-lived static collection that the GC correctly left alone as it grew.  
The fix was to drop the references so the objects become unreachable 