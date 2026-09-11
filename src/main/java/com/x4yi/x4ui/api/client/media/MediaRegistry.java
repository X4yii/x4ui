package com.x4yi.x4ui.api.client.media;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
public class MediaRegistry {
    private static final Set<String> requiredFFmpegMods = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final Map<String, Set<String>> registeredMediaByMod = new ConcurrentHashMap<>();
    public static void requireFFmpeg(String modId) {
        if (modId != null && !modId.trim().isEmpty()) {
            requiredFFmpegMods.add(modId.toLowerCase(Locale.ROOT));
            com.x4yi.x4ui.impl.client.media.MediaManager.requireMediaEngine();
        }
    }
    public static void registerMedia(String modId, Collection<String> urls) {
        if (modId == null || urls == null || urls.isEmpty()) return;
        String cleanModId = modId.toLowerCase(Locale.ROOT);
        requireFFmpeg(cleanModId);
        Set<String> modSet = registeredMediaByMod.computeIfAbsent(cleanModId, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
        for (String url : urls) {
            if (url != null) {
                String trimmed = url.trim();
                if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                    modSet.add(trimmed);
                }
            }
        }
    }
    public static void registerMedia(String modId, String... urls) {
        if (urls != null && urls.length > 0) {
            registerMedia(modId, Arrays.asList(urls));
        }
    }
    public static boolean isFFmpegRequired() {
        return !requiredFFmpegMods.isEmpty();
    }
    public static Set<String> getRegisteredMedia(String modId) {
        if (modId == null) return Collections.emptySet();
        Set<String> set = registeredMediaByMod.get(modId.toLowerCase(Locale.ROOT));
        return set != null ? Collections.unmodifiableSet(set) : Collections.emptySet();
    }
    public static Map<String, Set<String>> getAllRegisteredMedia() {
        return Collections.unmodifiableMap(registeredMediaByMod);
    }
}