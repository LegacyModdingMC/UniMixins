package io.github.legacymoddingmc.unimixins.compat.asm;

import cpw.mods.fml.common.discovery.ModCandidate;
import cpw.mods.fml.common.discovery.ModDiscoverer;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.util.*;

import static io.github.legacymoddingmc.unimixins.compat.CompatCore.LOGGER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

/**
 * Makes the mod discoverer ignore duplicate UniMixins jars that come from java agents. This allows UniMixins to be
 * safely used as a Java agent instead of having to hunt down the UniMix jar and add that as one.
 */
public class IgnoreDuplicateJarsTransformer implements IClassTransformer {

    private static Set<File> uniMixinsJavaAgentJars;

    public static boolean wantsToRun() {
        return !getUniMixinsJavaAgentJars().isEmpty();
    }

    private static Set<File> getUniMixinsJavaAgentJars() {
        if(uniMixinsJavaAgentJars == null) {
            uniMixinsJavaAgentJars = new HashSet<>();
            try {
                for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
                    if (arg.startsWith("-javaagent:")) {
                        String agent = arg.substring("-javaagent:".length());
                        File agentFile = new File(agent);
                        String name = agentFile.getName();
                        if (name.toLowerCase().contains("unimixins")) {
                            uniMixinsJavaAgentJars.add(agentFile);
                        }
                    }
                }
            } catch(Exception e) {
                LOGGER.error("Failed to enumerate command line java agents");
                e.printStackTrace();
            }

        }
        return uniMixinsJavaAgentJars;
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(basicClass == null) {
            return null;
        }

        if(transformedName.equals("cpw.mods.fml.common.discovery.ModDiscoverer")) {
            basicClass = doTransformModDiscoverer(basicClass);
        }
        return basicClass;
    }

    private byte[] doTransformModDiscoverer(byte[] bytes) {
        LOGGER.info("IgnoreDuplicateJarsTransformer: Transforming ModDiscoverer#identifyMods to ignore duplicate UniMixins jars coming from Java agents.");

        try {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(bytes);
            classReader.accept(classNode, 0);
            for(MethodNode m : classNode.methods) {
                if(m.name.equals("identifyMods")) {
                    InsnList patch = new InsnList();
                    patch.add(new VarInsnNode(ALOAD, 0));
                    patch.add(new MethodInsnNode(INVOKESTATIC, "io/github/legacymoddingmc/unimixins/compat/asm/IgnoreDuplicateJarsTransformer$Hooks", "preIdentifyMods", "(Lcpw/mods/fml/common/discovery/ModDiscoverer;)V"));
                    m.instructions.insert(patch);
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

    @SuppressWarnings("unused")
    public static class Hooks {
        public static void preIdentifyMods(ModDiscoverer dis) {
            try {
                List<ModCandidate> candidates = getModCandidates(dis);

                Map<File, List<ModCandidate>> modCandidatesByFile = new HashMap<>();
                for(ModCandidate mc : candidates) {
                    modCandidatesByFile.computeIfAbsent(getModContainer(mc), x -> new ArrayList<>()).add(mc);
                }

                for(Map.Entry<File, List<ModCandidate>> e : modCandidatesByFile.entrySet()) {
                    File file = e.getKey();
                    List<ModCandidate> mcs = e.getValue();
                    if(getUniMixinsJavaAgentJars().contains(file)) {
                        Iterator<ModCandidate> it = mcs.iterator();
                        while(mcs.size() > 1 && it.hasNext()) {
                            ModCandidate mc = it.next();
                            // The java agent duplicate can only be a classpath mod
                            if (mc.isClasspath()) {
                                LOGGER.info("Removing duplicate Java agent mod candidate: " + file.getName());
                                candidates.remove(mc);
                                mcs.remove(mc);
                            }
                        }
                    }
                }

            } catch(Exception e) {
                LOGGER.error("Failed to remove duplicate mod candidates, the Mixin Java agent will cause a duplicate mod error. Add the UniMix jar as the java agent instead.");
                e.printStackTrace();
            }
        }

        private static List<ModCandidate> getModCandidates(ModDiscoverer dis) throws Exception {
            Field f = dis.getClass().getDeclaredField("candidates");
            f.setAccessible(true);
            return (List<ModCandidate>)f.get(dis);
        }

        private static File getModContainer(ModCandidate mc) throws Exception {
            Field f = ModCandidate.class.getDeclaredField("modContainer");
            f.setAccessible(true);
            return (File)f.get(mc);
        }

    }

}
