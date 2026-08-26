# Compatibilidad con JEI

Si estás construyendo un `GuiBaseContainer` y quieres que Just Enough Items (JEI) ignore ciertas áreas de tu interfaz personalizada (como paneles laterales, pestañas o elementos superpuestos) para que no dibuje sus listas de ítems por encima, puedes utilizar la lista integrada `jeiRecipeAreas`.

## ¿Cómo funciona?

La clase `GuiBaseContainer` proporciona un método `getJeiRecipeAreas()` que retorna una lista de rectángulos (`java.awt.Rectangle`). Cuando la interfaz gráfica se inicializa (incluso al redimensionar la ventana), esta lista se vacía automáticamente para evitar fugas de memoria.

Simplemente necesitas definir tus "Áreas Extra" durante la fase de inicialización de la interfaz, y luego registrar un manejador avanzado (`IAdvancedGuiHandler`) en el plugin JEI de tu mod para conectar JEI con X4UI.

## 1. Definiendo Áreas Extra

En tu clase heredada de `GuiBaseContainer`, sobrescribe el método `initComponents()` y añade tus rectángulos:

```java
@Override
protected void initComponents() {
    // Configuración estándar de X4UI
    GuiPanel container = new GuiPanel(width / 2 - 128, height / 2 - 100, 256, 200);
    addComponent(container);
    
    // Indicarle a JEI que no dibuje sobre una caja específica (ej. x: 10, y: 10, w: 100, h: 200)
    jeiRecipeAreas.add(new java.awt.Rectangle(10, 10, 100, 200));
}
```

## 2. Registrando el Handler en JEI

En la clase del Plugin JEI de tu mod (aquella anotada con `@JEIPlugin` y que implementa `IModPlugin`), debes registrar el manejador avanzado para que JEI sepa que debe preguntarle a `GuiBaseContainer` por estas áreas.

```java
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import com.x4yi.x4ui.client.gui.base.GuiBaseContainer;
import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.List;

@JEIPlugin
public class MyModJeiPlugin implements IModPlugin {
    @Override
    public void register(IModRegistry registry) {
        registry.addAdvancedGuiHandlers(new IAdvancedGuiHandler<GuiBaseContainer>() {
            @Override
            public Class<GuiBaseContainer> getGuiContainerClass() {
                // Este handler aplicará a CUALQUIER GUI que herede de GuiBaseContainer
                return GuiBaseContainer.class;
            }

            @Nullable
            @Override
            public List<Rectangle> getGuiExtraAreas(GuiBaseContainer gui) {
                // Retornar las áreas definidas dentro de la GUI
                return gui.getJeiRecipeAreas();
            }

            @Nullable
            @Override
            public Object getIngredientUnderMouse(GuiBaseContainer gui, int mouseX, int mouseY) {
                // Opcional: retornar ítems/fluidos bajo el ratón para búsquedas con R/U
                return null;
            }
        });
    }
}
```

Una vez configurado, JEI ajustará y envolverá automáticamente su lista lateral de objetos alrededor de los límites que le proveas, previniendo recortes visuales o botones que no se puedan presionar.
