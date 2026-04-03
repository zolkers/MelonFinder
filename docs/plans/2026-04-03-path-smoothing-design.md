# Path Smoothing Design

Date: 2026-04-03

## Goal

Replace the current elastic-band-only smoother with a two-stage pipeline that produces
human-looking, curved paths: sub-block noise → gradient descent with obstacle repulsion →
Catmull-Rom spline densification. The rendered path line switches from block-center dots to
the actual smooth Vec3 curve.

---

## Problem Statement

Current issues:
1. `SubBlockSampler.MAX_VARIANCE=0.1` gives only ±10 cm offset — visually indistinguishable
   from block centers.
2. `GradientSmoother` pulls waypoints toward neighbors' midpoint (elastic band only) with no
   obstacle repulsion — diagonal staircases survive.
3. `PathRenderer.renderPathLine` connects `node.pos() + 0.5` (block centers), ignoring the
   `Segment.start/end` Vec3 produced by the smoother.
4. Only start/end boxes needed on the rendered path — intermediate boxes add clutter.

---

## Pipeline

```
A* nodes (block-center BlockPos)
    │
    ▼
SubBlockSampler          variance=0.3, bias=0.2
    │  List<Vec3>
    ▼
GradientDescentSmoother  α=0.25, β=0.15, 8 probe dirs, radius=0.6, 10 iterations
    │  List<Vec3>
    ▼
CatmullRomSmoother       8 sub-points/gap, Catmull-Rom X/Z, linear Y, AABB fallback
    │  List<Vec3> (dense)
    ▼
buildSegments            1 Segment per consecutive Vec3 pair
```

---

## Components

### 1. SubBlockSampler (update)

File: `src/pathfinder/java/fr/riege/pathfinder/path/SubBlockSampler.java`

Changes:
- `MAX_VARIANCE`: 0.1 → **0.3** (±30 cm, visually significant)
- `DIRECTION_BIAS`: 0.1 → **0.2** (stronger tendency to drift toward next waypoint)

No structural changes — collision fallback to center is kept.

---

### 2. GradientDescentSmoother (replace GradientSmoother)

File: `src/pathfinder/java/fr/riege/pathfinder/path/GradientDescentSmoother.java`

Replaces `GradientSmoother.java` (delete old file + tests).

**Algorithm** (X/Z only, Y fixed throughout):

For each interior point `p[i]` over `ITERATIONS=10` passes:

```
midpoint  = (p[i-1] + p[i+1]) / 2
elastic   = α * (midpoint − p[i])          // pull toward chord midpoint

repulsion = sum over 8 cardinal+diagonal directions at PROBE_RADIUS=0.6:
              if AABB collides at probe point: β * (p[i] − probe_point).normalize()

candidate = p[i] + elastic + repulsion
p[i]      = candidate  if no AABB collision, else p[i]  (keep original)
```

Constants:
- `ALPHA = 0.25` (elastic weight)
- `BETA = 0.15` (repulsion weight per blocked direction)
- `PROBE_RADIUS = 0.6` (metres from waypoint center)
- `ITERATIONS = 10`
- Probe directions: N, NE, E, SE, S, SW, W, NW (8 directions, X/Z plane)

AABB collision: player box 0.6 wide × 1.8 tall centred on candidate point.

No Minecraft imports — takes `ICollisionProvider` interface (same as existing smoothers).

---

### 3. CatmullRomSmoother (new)

File: `src/pathfinder/java/fr/riege/pathfinder/path/CatmullRomSmoother.java`

**Algorithm**:

Input: control points `p[0..n]` (post-gradient-descent Vec3 list).

Phantom endpoints: duplicate first and last point so all gaps have four surrounding control points.

For each gap `[p[i], p[i+1]]`, emit `SAMPLES_PER_GAP=8` sub-points at `t = 1/8, 2/8, … 8/8`:

```
X/Z:  standard Catmull-Rom formula with tension τ=0.5
        CR(t) = 0.5 * [ (2p1) + (-p0+p2)t + (2p0-5p1+4p2-p3)t² + (-p0+3p1-3p2+p3)t³ ]
Y:    linear interpolation between p[i].y and p[i+1].y
```

Collision check per sub-point: if AABB blocked, fall back to whichever of `p[i]` or `p[i+1]`
is nearer (avoids threading through walls on tight corners).

Output: dense `List<Vec3>` (≈8× the input count).

No Minecraft imports.

---

### 4. PathAssembler (update)

File: `src/pathfinder/java/fr/riege/pathfinder/path/PathAssembler.java`

Chain order:
```java
List<Vec3> sampled    = new SubBlockSampler(collision).sample(nodes);
List<Vec3> smoothed   = new GradientDescentSmoother(collision).smooth(sampled);
List<Vec3> dense      = new CatmullRomSmoother(collision).smooth(smoothed);
List<Segment> segs    = buildSegments(dense);
```

`buildSegments` unchanged in structure — creates one `Segment` per consecutive `Vec3` pair.
With 8 sub-points per gap and a typical 12-waypoint cap, expect ~88 segments.

---

### 5. PathRenderer (update)

File: `src/client/java/fr/riege/client/render/PathRenderer.java`

**renderPathLine**: switch from `node.pos() + 0.5` to `segment.start()` / `segment.end()`:
```java
for (Segment seg : path.segments()) {
    Vec3 a = seg.start();
    Vec3 b = seg.end();
    // emit line from a to b (camera-relative)
}
```

**renderNodes**: draw boxes only for start and end nodes (cyan / magenta). Remove intermediate
node boxes — they add visual clutter and are irrelevant once the smooth line is visible.

The start Vec3 is `path.segments().getFirst().start()` and end is `path.segments().getLast().end()`.

---

## Deletions

- `src/pathfinder/java/fr/riege/pathfinder/path/GradientSmoother.java`
- `src/test/java/fr/riege/pathfinder/path/GradientSmootherTest.java`

---

## Tests

### GradientDescentSmootherTest

- Straight line of collinear points → no drift (elastic term cancels)
- Single obstacle in path → waypoints pushed away
- All-clear path → no AABB rejections, output length equals input length
- Y coordinates unchanged after smoothing

### CatmullRomSmootherTest

- 3 control points → output has `2 * SAMPLES_PER_GAP` points
- Points on a straight line → output stays on the line (Catmull-Rom degeneracy)
- Y interpolation is linear between control points
- Collision fallback: blocked sub-point replaced by nearer control point

---

## Trade-offs Considered

| Option | Pro | Con | Decision |
|--------|-----|-----|----------|
| Elastic only (current) | Simple | Diagonal staircases, no obstacle awareness | Replaced |
| Gradient descent only | Obstacle-aware | Still polygonal, no inter-waypoint curves | Stage 1 only |
| Bézier instead of Catmull-Rom | Smooth | Doesn't pass through control points | Rejected |
| **Gradient descent + Catmull-Rom** | Obstacle-aware AND curved, passes through smoothed waypoints | Two passes | **Chosen** |
