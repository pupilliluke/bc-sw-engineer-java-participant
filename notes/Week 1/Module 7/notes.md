Module 7: Exception Handling (exercise notes)

Exercise 1: Common Unchecked Exceptions

three risky statements, three separate try blocks. one big try would stop at the
first throw and the other two demos would never run, catch resumes after the
block it belongs to not back inside it.

  10 / divisor, divisor 0   ArithmeticException              validate the divisor
  value.length(), null      NullPointerException             validate the reference
  values[5], length 2       ArrayIndexOutOfBoundsException   check 0 <= index < length

divisor kept in a variable on purpose. 10 / 0 written literally is a constant
expression, some compilers flag it before it ever runs.

output was the three names in source order then Program continued.

Step 4, pulled the array catch out:
  Caught: ArithmeticException
  Exception in thread "main" java.lang.ArrayIndexOutOfBoundsException: Index 5 out of bounds for length 2
        at NoArrayCatch.main(NoArrayCatch.java:11)
  exit 1

nothing after the throw ran, Program continued. never printed and the JVM exited
non-zero. the earlier catch still worked, an uncaught exception only kills the
frames above where it was thrown. put the catch back.

all three are unchecked (RuntimeException) so the compiler never forced any of
this, no catch and no throws needed to compile. that's the trap, the code builds
clean and dies at runtime.


Exercise 2: try-catch-finally

  success   try -> finally -> return
  failure   try throws -> catch -> finally -> return

finally sits after both, so cleanup printed twice off two transfer() calls, once
per path. that's the whole point of the exercise, one call each way in one run.

putting the cleanup line at the end of try instead only fires on success, the
throw jumps straight past it. it has to be in finally.

normal guarantee not an absolute one. System.exit, a killed process or a JVM
crash all skip finally, nothing runs after the process is gone.

failure path stayed recoverable because the catch is IllegalStateException,
the exact type thrown. main kept going and printed the second block.

for files, readers, streams use try-with-resources instead (Ex 3). manual
close in finally works but it drops the original exception if close() throws
too, try-with-resources closes for you and keeps the first one with the close
failure attached as suppressed.
