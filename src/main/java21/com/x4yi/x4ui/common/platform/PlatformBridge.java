package com.x4yi.x4ui.common.platform;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

public final class PlatformBridge {
    private static final Object UNSAFE;
    private static final Method INVOKE_CLEANER;

    static {
        Object unsafeObj = null;
        Method cleanerMethod = null;
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field f = unsafeClass.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafeObj = f.get(null);
            cleanerMethod = unsafeClass.getMethod("invokeCleaner", ByteBuffer.class);
        } catch (Throwable ignored) {
        }
        UNSAFE = unsafeObj;
        INVOKE_CLEANER = cleanerMethod;
    }

    private PlatformBridge() {}

    public static int getJavaFeatureVersion() {
        return Runtime.version().feature();
    }

    public static boolean isVirtualThreadsSupported() {
        return true;
    }

    public static boolean isCleanroom() {
        return hasClass("com.cleanroommc.boot.Main")
                || hasClass("top.outlands.foundation.LaunchHandler");
    }

    public static boolean isLwjgl3() {
        return hasClass("org.lwjgl.Version");
    }

    public static String getRuntimeDescription() {
        String env = isCleanroom() ? "CleanroomMC" : "Forge 1.12.2";
        return env + " (Java " + getJavaFeatureVersion() + ")";
    }

    private static boolean hasClass(String name) {
        try {
            Class.forName(name, false, PlatformBridge.class.getClassLoader());
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static void cleanDirectBuffer(ByteBuffer buffer) {
        if (buffer == null || !buffer.isDirect() || UNSAFE == null || INVOKE_CLEANER == null) return;
        try {
            INVOKE_CLEANER.invoke(UNSAFE, buffer);
        } catch (Throwable ignored) {
        }
    }
}
