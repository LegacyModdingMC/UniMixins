package io.github.legacymoddingmc.unimixins.compat;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;

import java.util.*;

import static io.github.legacymoddingmc.unimixins.compat.CompatCore.LOGGER;

public class CrashReportEnhancer {
    public static void addMixinErrorsToCrashReport(CrashReport crashReport, CrashReportCategory category) {
        try {
            Collection<String> classesInStackTrace = getClassesInStackTrace(crashReport);

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

                    msg += "\t\t" + cls + ":\n";
                    for(String clsError : e.getValue()) {
                        msg += "\t\t\t" + clsError + "\n";
                    }
                }

                String sep = "\n\t\t";
                category.addCrashSection(
                        String.format("Mixin Errors in Stacktrace"),
                        "\n" + msg);
            }
        } catch (Throwable t) {
            // If an error happens here, the game will silently crash, so we must make sure that does not happen.
            LOGGER.error("Encountered error while enhancing crash report");
            t.printStackTrace();
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
