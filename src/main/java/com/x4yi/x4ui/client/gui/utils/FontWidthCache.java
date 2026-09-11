package com.x4yi.x4ui.client.gui.utils;
import net.minecraft.client.gui.FontRenderer;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Deprecated
public class FontWidthCache {
    private static final int MAX_ENTRIES_PER_MOD = 5000;
    private static final Map<String, Map<String, Integer>> cache = new ConcurrentHashMap<>();
    public static int getStringWidth(FontRenderer fr, String text, String modid) {
        if (text == null || text.isEmpty()) return 0;
        if (fr == null) return text.length() * 6;
        Map<String, Integer> modCache = cache.computeIfAbsent(modid, k ->
            Collections.synchronizedMap(new LinkedHashMap<String, Integer>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Integer> eldest) {
                    return size() > MAX_ENTRIES_PER_MOD;
                }
            })
        );
        synchronized (modCache) {
            return modCache.computeIfAbsent(text, fr::getStringWidth);
        }
    }
    public static void clearCache(String modid) {
        Map<String, Integer> modCache = cache.get(modid);
        if (modCache != null) {
            synchronized (modCache) {
                modCache.clear();
            }
        }
    }
    public static void clearAll() {
        for (Map<String, Integer> modCache : cache.values()) {
            synchronized (modCache) {
                modCache.clear();
            }
        }
        cache.clear();
    }
}