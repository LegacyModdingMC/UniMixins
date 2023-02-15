package io.github.legacymoddingmc.unimixins.compat;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;

import java.util.ArrayList;
import java.util.List;

import static io.github.legacymoddingmc.unimixins.compat.CompatCore.LOGGER;

public class CrashReportEnhancer {
    public static void addMixinErrorsToCrashReport(Object crashReportObj, Object categoryObj) {
        try {
            CrashReport crashReport = (CrashReport) crashReportObj;
            CrashReportCategory category = (CrashReportCategory) categoryObj;

            List<String> classesThatWereNotFound = determineClassesThatWereNotFound(crashReport);

            List<String> errors = new ArrayList<>();
            for (String s : classesThatWereNotFound) {
                errors.addAll(MixinErrorHandler.getErrorsForClass(s));
            }
            if (!errors.isEmpty()) {
                String sep = "\n\t\t";
                category.addCrashSection(
                        String.format("Mixin Errors in Class%s", classesThatWereNotFound.size() == 1 ? "" : "es"),
                        sep + String.join(sep, errors));
            }
        } catch (Throwable t) {
            // If an error happens here, the game will silently crash, so we must make sure that does not happen.
            LOGGER.error("Encountered error while enhancing crash report");
            t.printStackTrace();
        }
    }

    private static List<String> determineClassesThatWereNotFound(CrashReport crashReport) {
        List<String> classes = new ArrayList<>();

        Throwable th = crashReport.getCrashCause();

        while(th != null) {
            if(th instanceof ClassNotFoundException) {
                String msg = th.getMessage();
                if(msg != null && !msg.isEmpty()) {
                    classes.add(msg);
                }
            }
            th = th.getCause();
        }

        return classes;
    }
}
