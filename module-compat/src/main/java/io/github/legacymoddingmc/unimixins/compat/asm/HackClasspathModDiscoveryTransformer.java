package io.github.legacymoddingmc.unimixins.compat.asm;

import static io.github.legacymoddingmc.unimixins.compat.CompatCore.LOGGER;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;

/**
 * <p>Fixes classpath mods not getting loaded in dev env when running via the runClient Gradle task.</p>
 */

public class HackClasspathModDiscoveryTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(basicClass == null) {
            return null;
        }
        if(transformedName.startsWith("org.objectweb.asm.")) return basicClass;

        if(transformedName.equals("cpw.mods.fml.common.discovery.ModDiscoverer")) {
            basicClass = doTransformModDiscoverer(basicClass);
        }
        return basicClass;
    }

    private byte[] doTransformModDiscoverer(byte[] bytes) {
        LOGGER.info("HackClasspathModDiscoveryTransformer: Transforming ModDiscoverer#findClasspathMods to ignore reparseable coremods.");

        try {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(classNode, 0);
            for(MethodNode m : classNode.methods) {
                if(m.name.equals("findClasspathMods")) {
                    Iterator<AbstractInsnNode> it = m.instructions.iterator();

                    while(it.hasNext()) {
                        AbstractInsnNode i = it.next();
                        if(i.getOpcode() == INVOKESTATIC) {
                            MethodInsnNode mi = (MethodInsnNode)i;
                            if(mi.owner.equals("cpw/mods/fml/relauncher/CoreModManager") && mi.name.equals("getReparseableCoremods") && mi.desc.equals("()Ljava/util/List;")) {
                                m.instructions.insertBefore(mi, new MethodInsnNode(INVOKESTATIC, "io/github/legacymoddingmc/unimixins/compat/asm/HackClasspathModDiscoveryTransformer$Hooks", "redirectGetReparseableCoremods", mi.desc));
                                it.remove();
                                break;
                            }
                        }
                    }
                }
            }
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(writer);
            return writer.toByteArray();
        } catch(Exception e) {
            e.printStackTrace();
        }

        return bytes;
    }

    public static class Hooks {

        public static List<String> redirectGetReparseableCoremods() {
            return Arrays.asList();
        }

    }


}
