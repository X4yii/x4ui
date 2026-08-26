package com.x4yi.x4ui.client.gui.utils;
import net.minecraft.client.gui.FontRenderer;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility for caching string widths to avoid heavy re-calculations in complex text parsers.
 * 
 * <p><b>IMPORTANT:</b> This cache is designed explicitly for processing static text 
 * files (e.g., Markdown, logs, long documents). It should <b>NOT</b> be used to cache 
 * highly mutable game variables (like timers, positions, or dynamic states).
 * 
 * <p>To prevent memory leaks when rendering large files, the cache is automatically 
 * limited to 10,000 unique entries per ModID using an LRU eviction policy.
 */
public class FontWidthCache {
    private static final int MAX_ENTRIES_PER_MOD = 10000;
    private static final Map<String, Map<String, Integer>> cache = new java.util.HashMap<>();
    
    public static int getStringWidth(FontRenderer fr, String text, String modid) {
        if (text == null || text.isEmpty()) return 0;
        Map<String, Integer> modCache = cache.computeIfAbsent(modid, k -> new LinkedHashMap<String, Integer>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Integer> eldest) {
                return size() > MAX_ENTRIES_PER_MOD;
            }
        });
        return modCache.computeIfAbsent(text, fr::getStringWidth);
    }
    
    public static void clearCache(String modid) {
        if (cache.containsKey(modid)) {
            cache.get(modid).clear();
        }
    }
    
    public static void clearAll() {
        for (Map<String, Integer> modCache : cache.values()) {
            modCache.clear();
        }
        cache.clear();
    }
}