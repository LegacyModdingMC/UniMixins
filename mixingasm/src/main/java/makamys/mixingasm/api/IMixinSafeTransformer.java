package makamys.mixingasm.api;

/** Implement this interface to signal that your transformer is "Mixin-safe", i.e. it does not cause issues when run by Mixin's preprocessor. This will be used as a hint that it shouldn't get excluded from the mixin preprocessor's transformer list by Mixingasm. */

public interface IMixinSafeTransformer {

}
