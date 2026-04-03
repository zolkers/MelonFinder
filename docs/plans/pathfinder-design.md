# Pathfinder Design — MelonFinder

Date: 2026-04-01

## Goal

A professional, human-looking A* pathfinder for Minecraft 1.21.11 (Fabric).
The pathfinder controls the player on demand via `/goto`. One active path at a time.
Designed for longevity — MC version upgrades touch only the `layer` module.

---

## Module Structure

Five Gradle source sets under `src/`:

```
src/
├── api/          Pure Java — interfaces, data types, registry contracts. Zero MC imports.
├── pathfinder/   Pure Java — A* engine, smoother, registries. Depends on api only.
├── layer/        Fabric/MC — adapters implementing api interfaces. Only MC-aware code.
├── client/       Fabric client — debug rendering, path visualization.
└── test/         JUnit — tests against pathfinder + api only, no MC needed.
```

**Dependency arrows (one-way, never circular):**
```
layer      → api
pathfinder → api
client     → api, layer
test       → api, pathfinder
```

`pathfinder` never imports anything from `layer`. This is the hard contract that makes MC version upgrades trivial.

---

## Package Map

```
fr.riege.api
  ├── layer/       IWorldLayer, IBlockPhysicsLayer, IEntityPhysicsLayer, ICollisionLayer
  ├── path/        Node, Segment, Path, PathStatus, PathResult, BlockPos, AABB, Vec3
  └── registry/    IRegistry, RegistryKey, MovementKeys (key constants)

fr.riege.pathfinder
  ├── engine/      PathfinderEngine, PathfinderContext, PathSession
  ├── astar/       AStarSearch, NodeGraph, OpenSet, ClosedSet
  ├── smooth/      PathSmoother, NodeReducer, SegmentCapper, SubBlockSampler
  ├── registry/    MovementTypeRegistry, HeuristicRegistry, BehaviorRegistry,
  │                MovementEvaluatorRegistry, PathModifierRegistry
  └── evaluator/   WalkEvaluator, JumpEvaluator, FallEvaluator, SwimEvaluator,
                   ClimbEvaluator, SprintEvaluator, SneakEvaluator, ParkourEvaluator

fr.riege.layer
  ├── MelonFinder.java
  ├── adapter/     FabricWorldLayer, FabricBlockPhysicsLayer, FabricEntityPhysicsLayer,
  │                FabricCollisionLayer
  └── command/     GotoCommand

fr.riege.client
  └── render/      PathRenderer, DebugOverlay

fr.riege.test
  └── pathfinder/  AStarTest, SmootherTest, EvaluatorTest
```

---

## API Layer

### Layer Interfaces

```java
// fr.riege.api.layer

public interface IWorldLayer {
    boolean isWalkable(int x, int y, int z);
    boolean isSolid(int x, int y, int z);
    FluidType getFluidType(int x, int y, int z);
    int getLightLevel(int x, int y, int z);
}

public interface IBlockPhysicsLayer {
    float getSpeedMultiplier(BlockPos pos);   // soul sand, ice, honey...
    float getSlipperiness(BlockPos pos);
    boolean isPassable(BlockPos pos);          // grass, torch, cobweb...
    double getStandingY(BlockPos pos);         // slabs, stairs, snow layers
    float getDragFactor(BlockPos pos);         // water, lava, air
    float getBlockDamage(BlockPos pos);        // cactus, magma, lava
}

public interface IEntityPhysicsLayer {
    double getHitboxWidth();
    double getHitboxHeight();
    double getStepHeight();            // vanilla = 0.6
    double getJumpVelocity();          // actual blocks/tick
    float evaluateFallDamage(int blocks);
    double getSwimSpeed();
    double getSprintMultiplier();
    double getSneakSpeedMultiplier();
}

public interface ICollisionLayer {
    List<AABB> getCollisionBoxes(BlockPos pos);
    boolean hasCollisionAt(AABB box);
    double getMaxReach(BlockPos from, Direction dir, double hitboxHalf);
}
```

**Principle:** The pathfinder never asks "what is this block" — only "what does this block do to movement."
All block identity → physics property translation happens exclusively in `layer`.

### Path Data Structures (immutable)

```
BlockPos     int x, y, z                          — MC-free coordinate
Vec3         double x, y, z                       — sub-block position
AABB         Vec3 min, Vec3 max                   — axis-aligned bounding box
Node         BlockPos + MovementType + gCost + hCost
Segment      List<Node> + Vec3 start + Vec3 end + double length
Path         List<Segment> + double totalCost + PathStatus
PathStatus   FOUND, UNREACHABLE, TIMEOUT, CANCELLED
PathResult   Path + long computeMs + int nodesExplored
```

### Registry Contract

```java
@FunctionalInterface
public interface IRegistry<K, V> {
    void register(K key, V value);
    Optional<V> get(K key);
    Collection<V> getAll();
}
```

Keys are `RegistryKey` (namespace:path string wrapper). Constants live in `MovementKeys`.

---

## Pathfinder Engine

### PathfinderEngine

```java
@ApiStatus.Internal
public final class PathfinderEngine {
    private PathSession activeSession;   // one at a time, enforced here

    public PathResult compute(BlockPos from, BlockPos to, PathfinderContext ctx);
    public void cancel();
    public boolean isRunning();
}
```

`PathfinderContext` bundles all layer references and registry references.
The engine has zero MC imports — everything arrives through context.

### A* Search

- Search space: block-level nodes
- `fCost = gCost + hCost`
- Heuristic: pluggable via `HeuristicRegistry` — default key `MovementKeys.HEURISTIC_EUCLIDEAN_3D`
- Movement cost per node:
  ```
  actualCost = baseMovementCost
             × (1 / speedMultiplier)
             × dragFactor
             + blockDamage penalty
             + behaviorCostPenalty   ← from BehaviorRegistry
  ```
- `BehaviorRegistry` entries add cost, never veto — tight corridors are expensive, not forbidden

### Pipeline (in order)

```
1. A* on full block grid                → raw Node list
2. NodeReducer                          → keep only important nodes
                                           (direction changes, movement type changes,
                                            obstacle corners, segment boundaries, start/goal)
3. PathSmoother — LOS pass              → raycast hitbox-swept between nodes,
                                           cut redundant intermediates (A1→B3 shortcut)
4. SegmentCapper                        → split segments > maxSegmentLength (default 12 blocks)
                                           by recursive midpoint until all within limit
5. SubBlockSampler                      → assign Vec3 target per node
                                           biased toward travel direction,
                                           ±variance (default ±0.1 blocks),
                                           hitbox-safe (tested against ICollisionLayer)
```

**Re-path trigger:** at each segment boundary, the engine checks if the next segment is still valid.
If not, it re-runs from the current position.

### LOS Raycast

The LOS check is a **swept hitbox**, not a centerpoint ray.
It uses `ICollisionLayer.hasCollisionAt(AABB)` along the path direction.
This prevents false positives in 1-block-wide corridors.

### Sub-block Positioning

```
validPosition = desiredOffset
              → shrink by (hitboxWidth / 2) margin in all horizontal directions
              → test expanded hitbox against all neighbors via ICollisionLayer
              → if collision: pull back toward block center until clear
```

Goal block arrival uses `approach(fromDirection, variance)` — never the exact block center.

---

## Registries

All five registries live in `fr.riege.pathfinder.registry`:

| Registry                    | Key type      | Value type           | Purpose                                               |
|-----------------------------|---------------|----------------------|-------------------------------------------------------|
| `MovementTypeRegistry`      | `RegistryKey` | `IMovementEvaluator` | walk, jump, fall, swim, climb, sprint, sneak, parkour |
| `HeuristicRegistry`         | `RegistryKey` | `IHeuristic`         | A* heuristic functions                                |
| `MovementEvaluatorRegistry` | `RegistryKey` | `IMovementEvaluator` | cost evaluation per movement                          |
| `PathModifierRegistry`      | `RegistryKey` | `IPathModifier`      | post-process path modifiers in order                  |
| `BehaviorRegistry`          | `RegistryKey` | `IPathBehavior`      | geometry preferences as cost penalties                |

**Registration is always explicit, always in `layer`:**
```java
registry.register(MovementKeys.WALK, new WalkEvaluator(physicsLayer));
registry.register(MovementKeys.JUMP, new JumpEvaluator(physicsLayer, collisionLayer));
```

---

## Layer Adapters (`@Layer`)

Each adapter is annotated `@Layer` — a marker annotation, no runtime processing.

```
FabricWorldLayer          implements IWorldLayer
FabricBlockPhysicsLayer   implements IBlockPhysicsLayer
FabricEntityPhysicsLayer  implements IEntityPhysicsLayer
FabricCollisionLayer      implements ICollisionLayer
```

These are the only classes that import Minecraft or Fabric APIs.
Updating for a new MC version = rewriting these four classes only.

---

## Command System

`/goto` registered via Fabric `CommandRegistrationCallback` in `GotoCommand`:

```
/goto <x> <y> <z>       absolute coordinates
/goto <dx> <dy> <dz>    relative (~x ~y ~z)
/goto cancel            cancels active session
/goto status            prints current session info
```

Calling `/goto` while a session is active automatically cancels the previous one.
`GotoCommand` builds `PathfinderContext` from current adapters, calls `PathfinderEngine.compute()`.

---

## Behavioral System (path geometry only — execution timing is future)

`BehaviorRegistry` entries affect path geometry during A*:
- `preferWiderCorridors` — adds cost per adjacent solid block
- `avoidEdges` — adds cost for nodes near drop-offs
- `avoidExposedRidges` — adds cost for nodes with low overhead clearance

All expressed as cost penalties. The pathfinder naturally avoids uncomfortable geometry
without explicit rules — it just costs more.

Sub-block noise via `PathModifierRegistry`:
- `cornerBias` — offsets waypoints toward the inside of turns
- `positionNoise` — small configurable variance so path is never identical twice

---

## Human-Look Summary

The path looks human because:
1. Diagonal shortcuts taken naturally (LOS culling, not grid-locked)
2. Never walks exact block centers (sub-block positioning with bias + variance)
3. Prefers wider, safer geometry (behavior cost penalties)
4. Re-evaluates at checkpoints (segment boundaries) — responds to world changes
5. Path geometry varies slightly each run (position noise modifier)

Execution timing variation (micro-pauses, acceleration curves, head-turn before body) is
designed for but deferred — the layer interfaces already expose what's needed.
