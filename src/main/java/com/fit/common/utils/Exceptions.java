package com.fit.common.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Exceptions {
    public Exceptions() {
    }

    public static RuntimeException unchecked(Exception e) {
        return e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
    }

    public static String getStackTraceAsString(Exception e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

    public static boolean isCausedBy(Exception ex, Class... causeExceptionClasses) {
        for (Object cause = ex; cause != null; cause = ((Throwable) cause).getCause()) {
            Class[] var6 = causeExceptionClasses;
            int var5 = causeExceptionClasses.length;

            for (int var4 = 0; var4 < var5; ++var4) {
                Class causeClass = var6[var4];
                if (causeClass.isInstance(cause)) {
                    return true;
                }
            }
        }

        return false;
    }
}
