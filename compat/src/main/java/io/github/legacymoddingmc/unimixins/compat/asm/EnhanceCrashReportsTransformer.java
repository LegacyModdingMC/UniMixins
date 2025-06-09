package io.github.legacymoddingmc.unimixins.compat.asm;

import io.github.legacymoddingmc.unimixins.compat.CrashReportEnhancer;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;
import static io.github.legacymoddingmc.unimixins.compat.CompatCore.LOGGER;

/**
 * <p>Inserts a hook at the end of FMLCommonHandler#enhanceCrashReport.
 * <p>We use a transformer instead of a mixin because FML is a minefield for mixin errors.
 */
public class EnhanceCrashReportsTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(basicClass == null) return null;

        if(name.equals("cpw.mods.fml.common.FMLCommonHandler")) {
            return transformFMLCommonHandler(basicClass);
        }
        return basicClass;
    }

    /**
     * <pre>
     * public void enhanceCrashReport(CrashReport crashReport, CrashReportCategory category)
     * {
     *     for (ICrashCallable call: crashCallables)
     *     {
     *         category.addCrashSectionCallable(call.getLabel(), call);
     *     }
     * + EnhanceCrashReportsTransformer.Hooks.postEnhanceCrashReport();
     * }
     * </pre>
     */
    private static byte[] transformFMLCommonHandler(byte[] bytes) {
        LOGGER.info("Transforming FMLCommonHandler to add hook for enhancing crash reports");

        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);
        for(MethodNode m : classNode.methods) {
            if (m.name.equals("enhanceCrashReport")) {
                AbstractInsnNode injectionTarget = null;
                for(int i = 0; i < m.instructions.size(); i++) {
                    AbstractInsnNode ain = m.instructions.get(i);
                    if(ain instanceof InsnNode) {
                        InsnNode in = (InsnNode)ain;
                        if(in.getOpcode() == RETURN) {
                            injectionTarget = in;
                            break;
                        }
                    }
                }

                if(injectionTarget != null) {
                    InsnList inject = new InsnList();
                    //ALOAD crashReport
                    inject.add(new VarInsnNode(ALOAD, 1));
                    //ALOAD category
                    inject.add(new VarInsnNode(ALOAD, 2));
                    //INVOKESTATIC foo/bar.postEnhanceCrashReport(Lnet/minecraft/crash/CrashReport;Lnet/minecraft/crash/CrashReportCategory;)V
                    inject.add(new MethodInsnNode(INVOKESTATIC, "io/github/legacymoddingmc/unimixins/compat/asm/EnhanceCrashReportsTransformer$Hooks", "postEnhanceCrashReport", "(Lnet/minecraft/crash/CrashReport;Lnet/minecraft/crash/CrashReportCategory;)V", false));
                    m.instructions.insertBefore(injectionTarget, inject);
                }
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    public static class Hooks {

        public static void postEnhanceCrashReport(CrashReport report, CrashReportCategory category) {
            CrashReportEnhancer.addMixinsToCrashReport(report, category);
        }
    }
}
