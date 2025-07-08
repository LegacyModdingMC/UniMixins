package com.gtnewhorizon.gtnhmixins.builders;

import com.gtnewhorizon.gtnhmixins.GTNHMixins;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

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
