# Code Rules (enforce on every task)

## Java Style

- Methods: cyclomatic complexity ≤ 15
- Loops: max 1 break or continue per for loop
- Fields: always private, getters only
- Qualifiers: fully qualified names only in imports
- `var`: never — always explicit types
- Registries: preferred over switch/if chains for extensible concerns
- No magic: no reflection scanning, no annotation processors, all registration is explicit
- Prefer streams over for loops when they improve readability
- Use try-with-resources for any AutoCloseable (streams, connections, readers)
- Use `Optional<T>` for methods that may not return a value. Never call `.get()` without `.isPresent()` — use `.orElse()` / `.orElseThrow()`
- Always use a logger — never `exception.printStackTrace()`
- Catch specific exceptions — never generic `Exception` or `Throwable`
- Use records for DTOs and immutable data carriers
- Java 21: `.getFirst()` and `.getLast()` exist on `List` — use them
- Never declare variables on the same line (`int x, y, z;` is forbidden)
- Avoid nesting — prefer early return:

```java
// avoid
public void someMethod() {
    if (isCar()) {
        doSomething();
    }
}

// prefer
public void anotherMethod() {
    if (!isCar()) return;
    doSomething();
}
```

---

## Module Contract

Five source sets with strict one-way dependency rules:

```
api        → (none)
pathfinder → api
layer      → api
client     → api, layer
test       → api, pathfinder
```

**Hard rules (never break these):**
- `api` and `pathfinder`: **zero MC imports**. Not a single `net.minecraft.*` or `net.fabricmc.*` import is permitted.
- `layer`: the only MC-aware code. All Minecraft/Fabric imports live here exclusively.
- `client`: Fabric client-side rendering only.
- `test`: JUnit only — no MC, no Fabric. Tests must run with `./gradlew test` without a game instance.

Violating this contract defeats the entire MC-version upgrade story. Updating MC version = rewriting `layer` adapters only, nothing else.

---

## @Layer Annotation

All Fabric adapter classes must carry `@fr.riege.api.annotation.Layer`.
This is a marker annotation — no runtime processing, no scanning.
It signals: "this class will break on MC upgrade, here is where to look first."

```java
@Layer
public final class FabricWorldLayer implements IWorldLayer { ... }
```

## TEST COVERAGE

- IT IS MANDATORY TO ADD TESTS
- All tests live in `src/test/java/`
- Tests have zero MC dependency — implement api interfaces inline with anonymous classes or lambdas
- Every evaluator must have tests for: happy path, impossible conditions, and boundary cases
- `NodeGraph` and `AStarSearch` must have tests for terrain variation: step-up, step-down, multi-block fall
- New event types must have tests for post/subscribe/cancel lifecycle
