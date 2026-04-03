package fr.riege.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a Minecraft-version–specific adapter that belongs to the
 * {@code layer} source set.
 *
 * <p>This annotation is a purely informational marker.  It carries no runtime
 * semantics: its retention policy is {@link RetentionPolicy#SOURCE SOURCE}, so
 * the compiler discards it after the compilation phase and it never appears in
 * generated {@code .class} files.
 *
 * <h2>Purpose</h2>
 * <p>The MelonFinder module contract isolates all Minecraft and Fabric imports
 * inside the {@code layer} source set.  Upgrading to a new Minecraft version
 * therefore requires touching only the classes annotated with {@code @Layer}.
 * Placing this annotation on every adapter class makes the upgrade surface
 * immediately visible without relying on package names or project-wide
 * text searches.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * @Layer
 * public final class FabricWorldLayer implements IWorldLayer {
 *     // Minecraft-specific implementation
 * }
 * }</pre>
 *
 * @see fr.riege.api.layer.IWorldLayer
 * @see fr.riege.api.layer.IBlockPhysicsLayer
 * @see fr.riege.api.layer.ICollisionLayer
 * @see fr.riege.api.layer.IEntityPhysicsLayer
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Layer {
}
