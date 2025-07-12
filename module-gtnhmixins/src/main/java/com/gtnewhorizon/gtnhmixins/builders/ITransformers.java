package com.gtnewhorizon.gtnhmixins.builders;

import com.gtnewhorizon.gtnhmixins.GTNHMixins;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * The ITransformers interface provides a flexible way of declaring and registering {@link net.minecraft.launchwrapper.IClassTransformer} during runtime.
 * Registration is done based on conditions such as user defined logic, looking configs, loading side (CLIENT, COMMON, SERVER)
 * or looking at the presence or absence of mods declared using the {@link com.gtnewhorizon.gtnhmixins.builders.ITargetMod} interface.
 * This interface must be implemented on an enum.
 * <pre>
 * {@code
 * public enum ASMTransformers implements ITransformers {
 *
 *     EXAMPLE_TRANSFORMER(new TransformerBuilder("Hello") // optional comment
 *         .addRequiredMod(TargetMod.IC2) // optional
 *         .addExcludedMod(...) // optional
 *         .addCommonTransformers("........") // transformers that needs to be both on client and server
 *         .addClientTransformers("........") // transformers only for the client
 *         .addServerTransformers("........") // transformers only for the dedicated server
 *         .setApplyIf(() -> Config.yourconfig....)); // optional
 *         // it is required to have at least one transformers class
 *
 *     private final TransformerBuilder builder;
 *
 *     ASMTransformers(TransformerBuilder builder) {
 *         this.builder = builder;
 *     }
 *
 *     @Override
 *     public TransformerBuilder getBuilder() {
 *         return this.builder;
 *     }
 * }
 * }
 * </pre>
 * <p>
 * If you do not need very complex logic in your enum, you can instantiate
 * the TransformerBuilder in the enum constructor and declare simplistic enum entries.
 *
 * <pre>
 * {@code
 * public enum ASMTransformers implements ITransformers {
 *
 *     CLIENT_TRANSFORMER1(Side.CLIENT, "TransformerClientClass1"),
 *     CLIENT_TRANSFORMER2(Side.CLIENT, "TransformerClientClass2"),
 *     COMMON_TRANSFORMER(Side.COMMON, "TransformerCommonClass");
 *
 *     private final TransformerBuilder builder;
 *
 *     ASMTransformers(Side side, String... transformers) {
 *         builder = new TransformerBuilder().addSidedTransformers(side, transformers);
 *     }
 *
 *     @Override
 *     public TransformerBuilder getBuilder() {
 *         return this.builder;
 *     }
 * }
 * }
 * </pre>
 */
@SuppressWarnings("unused")
public interface ITransformers extends IBaseTransformer {

    @Nonnull
    TransformerBuilder getBuilder();

    /**
     * Returns the list of transformers that should be loaded by your {@link cpw.mods.fml.relauncher.IFMLLoadingPlugin} implementation.
     * <p>
     * You may call it as such :
     * <pre>
     * {@code
     *     @Override
     *     public String[] getASMTransformerClass() {
     *         return ITransformers.getTransformers(YourTransformerEnum.class);
     *     }
     * }
     * </pre>
     */
    static <E extends Enum<E> & ITransformers> String[] getTransformers(Class<E> transformerEnum) {
        final List<String> toLoad = new ArrayList<>();
        final List<String> toNotLoad = new ArrayList<>();
        TransformerBuilder.loadTransformers(transformerEnum, toLoad, toNotLoad);
        GTNHMixins.log("Not loading the following transformers: {}", toNotLoad);
        for (String transformer : toLoad) {
            GTNHMixins.log("Loading ITransformer {}", transformer);
        }
        return toLoad.toArray(new String[0]);
    }
}
