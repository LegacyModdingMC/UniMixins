package io.github.legacymoddingmc.unimixins.compat;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.ClassInfo;

import java.lang.reflect.Field;
import java.util.*;

import static io.github.legacymoddingmc.unimixins.compat.CompatCore.LOGGER;

public class CrashReportEnhancer {
    public static void addMixinsToCrashReport(CrashReport crashReport, CrashReportCategory category) {
        try {
            Collection<String> classesInStackTrace = getClassesInStackTrace(crashReport);

            addMixinErrorsToCrashReport(category, classesInStackTrace);
            addMixinsToCrashReport(category, classesInStackTrace);
        } catch (Throwable t) {
            // If an error happens here, the game will silently crash, so we must make sure that does not happen.
            LOGGER.error("Encountered error while enhancing crash report");
            t.printStackTrace();
        }
    }

    // Inspired by https://github.com/comp500/mixintrace
    private static void addMixinsToCrashReport(CrashReportCategory category, Collection<String> classesInStackTrace) {
        String msg = "";
        for(String s : classesInStackTrace) {
            Collection<IMixinInfo> mixins = getMixinsForClass(s);
            if(!mixins.isEmpty()) {
                msg += "\n\t\t" + s + ":";
                for (IMixinInfo mi : mixins) {
                    msg += "\n\t\t\t" + mi;
                }
            }
        }

        if(!msg.isEmpty()) {
            category.addCrashSection("Mixins in Stacktrace", msg);
        }
    }

    // Mixins#getMixinsForClass doesn't work properly, so we have to do this
    // see https://github.com/SpongePowered/Mixin/pull/529
    private static Collection<IMixinInfo> getMixinsForClass(String s) {
        try {
            ClassInfo ci = ClassInfo.fromCache(s);
            if (ci != null) {
                Field f = ClassInfo.class.getDeclaredField("mixins");
                if (f != null) {
                    f.setAccessible(true);
                    return (Set<IMixinInfo>) f.get(ci);
                }
            }
        } catch (Exception e) {
        }
        return Collections.emptySet();
    }

    private static void addMixinErrorsToCrashReport(CrashReportCategory category, Collection<String> classesInStackTrace) {
        Map<String, List<String>> errors = new HashMap<>();
        for (String s : classesInStackTrace) {
            List<String> e = MixinErrorHandler.getErrorsForClass(s);
            if(!e.isEmpty()) {
                errors.put(s, e);
            }
        }
        if (!errors.isEmpty()) {
            String msg = "";

            for(Map.Entry<String, List<String>> e : errors.entrySet()) {
                String cls = e.getKey();
                List<String> clsErrors = e.getValue();

                msg += "\n\t\t" + cls + ":";
                for(String clsError : e.getValue()) {
                    msg += "\n\t\t\t" + clsError;
                }
            }

            category.addCrashSection(
                    String.format("Mixin Errors in Stacktrace"),
                    msg);
        }
    }

    private static Collection<String> getClassesInStackTrace(CrashReport crashReport) {
        Set<String> classes = new HashSet<>();

        Throwable th = crashReport.getCrashCause();

        while(th != null) {
            if(th instanceof ClassNotFoundException) {
                String msg = th.getMessage();
                if(msg != null && !msg.isEmpty()) {
                    classes.add(msg);
                }
            }
            for(StackTraceElement elem : th.getStackTrace()) {
                classes.add(elem.getClassName());
            }
            th = th.getCause();
        }

        return classes;
    }
}
