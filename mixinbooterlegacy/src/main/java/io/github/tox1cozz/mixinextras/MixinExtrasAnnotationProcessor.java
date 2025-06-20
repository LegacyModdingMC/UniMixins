package io.github.tox1cozz.mixinextras;

import org.spongepowered.asm.util.logging.MessageRouter;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@SupportedAnnotationTypes({})
public class MixinExtrasAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return false;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        try {
            MessageRouter.setMessager(processingEnv.getMessager());
            MixinExtrasBootstrap.init();
        } catch (NoClassDefFoundError e) {
            // The Mixin AP probably isn't available, e.g. if loom has excluded it from IDEA.
        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}