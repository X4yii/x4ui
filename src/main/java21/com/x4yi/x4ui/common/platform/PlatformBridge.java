package com.x4yi.x4ui.common.platform;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import sun.misc.Unsafe;

public final class PlatformBridge {
    private static final Unsafe UNSAFE;

    static {
        Unsafe u = null;
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            u = (Unsafe) f.get(null);
        } catch (Throwable ignored) {
        }
        UNSAFE = u;
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

    public static ExecutorService newIoExecutor(String namePrefix, int fixedThreads) {
        ThreadFactory factory = Thread.ofVirtual().name(namePrefix + "-", 1).factory();
        return Executors.newThreadPerTaskExecutor(factory);
    }

    public static void cleanDirectBuffer(ByteBuffer buffer) {
        if (buffer == null || !buffer.isDirect() || UNSAFE == null) return;
        try {
            UNSAFE.invokeCleaner(buffer);
        } catch (Throwable ignored) {
        }
    }
}

