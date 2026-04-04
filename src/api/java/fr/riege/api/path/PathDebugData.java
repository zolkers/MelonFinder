package fr.riege.api.path;

import fr.riege.api.math.Vec3;

import java.util.List;

/**
 * Intermediate smoothing results captured after each pipeline stage, for
 * debug rendering.
 *
 * @param gradientPoints waypoints after gradient-descent smoothing (sparse control points)
 * @param catmullPoints  waypoints after Catmull-Rom densification (one per rendered segment node)
 */
public record PathDebugData(List<Vec3> gradientPoints, List<Vec3> catmullPoints) {}
