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

---

## Physics Over Identity

The pathfinder **never asks what a block is** — only what it does to movement.
All translation from block identity to physics properties happens exclusively in `layer`.

```java
// WRONG — block identity in pathfinder or api
if (block == Blocks.ICE) { ... }

// RIGHT — physics query through layer interface
@Override
public float getSlipperiness(BlockPos pos) {
    return level.getBlockState(toMc(pos)).getBlock().getFriction();
}
```

This principle also applies to fluids, entities, and any MC concept. If the pathfinder needs
to know "is this water?", it calls `worldLayer.getFluidType(pos) == FluidType.WATER`, never
any Minecraft fluid class.

---

## Movement Evaluators

`IMovementEvaluator.evaluate(from, to)` returns a `MovementResult`:
- `MovementResult.impossible()` — the move cannot be executed
- `MovementResult.possible(cost)` — executable with a positive cost

To add a new movement type:
1. Create a class implementing `IMovementEvaluator` in `src/pathfinder/java/.../evaluator/`
2. Add a key constant in `src/api/java/.../registry/MovementKeys.java`
3. Register it in `src/layer/java/.../PathfinderContextFactory.buildRegistry()`
4. Add neighbor generation in `NodeGraph.getNeighbors()` if it produces new candidates

Never hardcode physics constants in evaluators — query the appropriate layer interface
(`IEntityPhysicsLayer`, `IBlockPhysicsLayer`, `ICollisionLayer`).

---

## NodeGraph Movement Model

`NodeGraph` generates walkable neighbors for each node. The model:

- **Horizontal (WALK/JUMP)**: For each of 8 directions `(dx, dz)`, tries `dy ∈ {-1, 0, +1}`.
  - `dy = 0` (same level) and `dy = -1` (step-down) use `WalkEvaluator`.
  - `dy = +1` (step-up) uses `JumpEvaluator`.
- **Fall (FALL)**: For each of 8 horizontal directions, scans `dy ∈ {-2, -3, ..., -(MAX_FALL_DEPTH+1)}`.
  Stops at the first reachable landing per direction. Checks fall damage via `IEntityPhysicsLayer.evaluateFallDamage()`.

Never add a pure vertical-only neighbor (except for `ClimbEvaluator` on ladders/vines).
A jump with no horizontal component is not reachable in normal Minecraft movement.

---

## Event System

- `api` defines: `Event`, `IEventBus`, `EventHandler<T>`, `EventPhase`, `EventPriority`, `Subscription`
- `layer` implements: `EventBusImpl`, `MelonFinderEvents` (the singleton bus), server-side event types (`PathCompleteEvent`, `TickEvent`)
- `client` implements: `EventBridge` (hooks `ClientTickEvents`, `WorldRenderEvents`, `HudRenderCallback`), client-only event types (`RenderWorldEvent`, `RenderHudEvent`)
- `pathfinder` does **not** depend on the event system. Events are posted from `layer` command handlers after the engine returns.

**Rule:** Any class that imports from `net.fabricmc.fabric.api.client.*` or `net.minecraft.client.*` belongs in `client`, not `layer`.

---

## TEST COVERAGE

- IT IS MANDATORY TO ADD TESTS
- All tests live in `src/test/java/`
- Tests have zero MC dependency — implement api interfaces inline with anonymous classes or lambdas
- Every evaluator must have tests for: happy path, impossible conditions, and boundary cases
- `NodeGraph` and `AStarSearch` must have tests for terrain variation: step-up, step-down, multi-block fall
- New event types must have tests for post/subscribe/cancel lifecycle
