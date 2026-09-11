package com.x4yi.x4ui.test;
import com.x4yi.x4ui.client.gui.utils.DefaultTheme;
import com.x4yi.x4ui.client.gui.utils.ITheme;
import com.x4yi.x4ui.client.gui.utils.ThemeRegistry;
import org.junit.Assert;
import org.junit.Test;
public class ThemeRegistryTest {
    @Test
    public void testDefaultThemeIsPresent() {
        Assert.assertNotNull(ThemeRegistry.getDefaultTheme());
        Assert.assertTrue(ThemeRegistry.hasTheme(ThemeRegistry.DEFAULT_THEME_ID));
        Assert.assertSame(DefaultTheme.INSTANCE, ThemeRegistry.getTheme(ThemeRegistry.DEFAULT_THEME_ID));
    }
    @Test
    public void testCustomThemeRegistration() {
        ITheme mockTheme = new DefaultTheme() {
            @Override
            public int getPrimaryColor() {
                return 0xFF00FF00;
            }
        };
        ThemeRegistry.registerTheme("mymod:green_matrix", mockTheme);
        Assert.assertTrue(ThemeRegistry.hasTheme("mymod:green_matrix"));
        Assert.assertSame(mockTheme, ThemeRegistry.getTheme("mymod:green_matrix"));
    }
    @Test
    public void testFallbackOnUnknownTheme() {
        ITheme fallback = ThemeRegistry.getTheme("non_existent:theme");
        Assert.assertSame(ThemeRegistry.getDefaultTheme(), fallback);
        ITheme nullFallback = ThemeRegistry.getTheme(null);
        Assert.assertSame(ThemeRegistry.getDefaultTheme(), nullFallback);
    }
}