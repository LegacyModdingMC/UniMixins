package makamys.mixingasm.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Decorate your class with this annotation to signal that your transformer is "Mixin-safe", i.e. it does not cause issues when run by Mixin's preprocessor. This will be used as a hint that it shouldn't get excluded from the mixin preprocessor's transformer list by Mixingasm. */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MixinSafeTransformer {
}
