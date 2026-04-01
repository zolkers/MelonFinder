# Code Rules (enforce on every task)

- Methods: cyclomatic complexity ≤ 15
- Loops: max 1 break or continue per for loop
- Fields: always private, getters only
- Qualifiers: fully qualified names only in imports
- `var`: never — always explicit types
- Registries: preferred over switch/if chains for extensible concerns
- No magic: no reflection scanning, no annotation processors, all registration is explicit
- Prefer streams over for loops when they improve readability.
- Use try-with-resources for any AutoCloseable (streams, connections, readers).
- Use Optional<T> for methods that may not return a value. Never call .get() without .isPresent() -- use .orElse() / .orElseThrow().
- Always use a logger -- never exception.printStackTrace().
- Catch specific exceptions -- never generic Exception or Throwable.
- Use records whenever needed (DTOs for instance)
- We are in java 21 .getFirst() and .getLast() exist !
- Never declare variables on the same line (int xAxis, yAxis, zAxis; SHOULD NEVER BE DONE) 
- Avoid nesting everything in a if condition when it can be avoided for instance:

```java

private boolean car;

private boolean isCar(){
    return car;
}
//avoid doing this
public void someMethod() {
    if(isCar()) {
        doSomething();
    }
}

//do this
public void anotherMethod() {
    if(!isCar()) return;
    doSomething();
}

```
