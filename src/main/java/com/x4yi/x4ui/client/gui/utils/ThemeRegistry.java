package com.x4yi.x4ui.client.gui.utils;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
public final class ThemeRegistry {
    public static final String DEFAULT_THEME_ID = "x4ui:default";
    private static final Map<String, ITheme> THEMES = new ConcurrentHashMap<>();
    private static volatile String currentThemeId = DEFAULT_THEME_ID;
    static {
        THEMES.put(DEFAULT_THEME_ID, DefaultTheme.INSTANCE);
    }
    private ThemeRegistry() {}
    public static void registerTheme(String id, ITheme theme) {
        if (id == null || theme == null) {
            throw new IllegalArgumentException("Theme ID and Theme instance must not be null");
        }
        THEMES.put(id, theme);
    }
    public static ITheme getTheme(String id) {
        if (id == null) return getDefaultTheme();
        ITheme theme = THEMES.get(id);
        return theme != null ? theme : getDefaultTheme();
    }
    public static boolean hasTheme(String id) {
        return id != null && THEMES.containsKey(id);
    }
    public static ITheme getDefaultTheme() {
        return DefaultTheme.INSTANCE;
    }
    public static Set<String> getRegisteredThemeIds() {
        return Collections.unmodifiableSet(THEMES.keySet());
    }
    public static void setCurrentTheme(String id) {
        if (hasTheme(id)) {
            currentThemeId = id;
        }
    }
    public static ITheme getCurrentTheme() {
        return getTheme(currentThemeId);
    }
    public static String getCurrentThemeId() {
        return currentThemeId;
    }
}