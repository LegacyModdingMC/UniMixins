package com.gtnewhorizon.gtnhmixins.builders;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public interface ITransformers extends IBaseTransformer {

    @Nonnull
    TransformerBuilder getBuilder();

    /**
     * Returns the list of transformers that should be loaded from your {@link cpw.mods.fml.relauncher.IFMLLoadingPlugin} implementation.
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
        final Logger LOGGER = LogManager.getLogger("ITransformers Loader");
        LOGGER.info("Loading the following transformers: {}", toLoad);
        LOGGER.info("Not loading the following transformers: {}", toNotLoad);
        return toLoad.toArray(new String[0]);
    }
}
