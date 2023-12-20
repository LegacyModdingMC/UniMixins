package io.github.legacymoddingmc.unimixins.compat.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** <p>Decorate your class with this annotation to make UniMixins remap references to <code>org.objectweb.asm</code> to
 * the ASM package name used by the current Mixin environment.</p>
 * <p>This is needed if you have a mixin config plugin compiled against this package name, and you want to pass the
 * ClassNode reference to a different class, since otherwise UniMixins only remaps references inside the mixin
 * config plugin class.</p> */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RemapASMForMixin {
}
