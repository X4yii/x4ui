# Contenedores y Paneles

Los contenedores son los bloques de construcción estructurales de X4UI. Su propósito principal es hospedar otros componentes y gestionar su disposición a través de `FlexLayout`.

## GuiPanel

El contenedor básico por excelencia. Por defecto, no dibuja fondo a menos que se le asigne un `GuiSprite` explícitamente o tenga un tema con un panel de fondo asignado.

```java
import com.x4yi.x4ui.client.gui.component.layout.FlexDirection;

GuiPanel panel = new GuiPanel(0, 0, 0, 0).withPercentWidth(1.0f);
panel.setFlexDirection(FlexDirection.VERTICAL); // Apilar verticalmente
panel.setGap(5); // 5 píxeles de separación
panel.setPadding(new Insets(10)); // 10 píxeles de padding interno
```

## GuiScrollPanel

Este panel incluye todo el comportamiento de `GuiPanel`, pero encapsula a sus hijos dentro de un **viewport de desplazamiento**. Posee su propio sub-contenedor llamado `contentPanel`.

### La Regla del Ancho en GuiScrollPanel
Por un principio de responsabilidad de Layout, el `contentPanel` interno hereda el 100% del ancho (`.withPercentWidth(1.0f)`) automáticamente desde su constructor si su `GuiScrollPanel` padre recibe un ancho.
Aun así, al inicializar el padre responsivamente, la sintaxis correcta es:

```java
GuiScrollPanel scroll = (GuiScrollPanel) new GuiScrollPanel(0, 0, 0, 250).withPercentWidth(1.0f);
scroll.getContentPanel().setFlexDirection(FlexDirection.VERTICAL); // Configurar el layout interno

scroll.addChild(card1); // Nota: GuiScrollPanel redirige los addChildren a su contentPanel
scroll.addChild(card2);
```

> [!WARNING]
> No confundas `scroll.setFlexDirection(...)` con `scroll.getContentPanel().setFlexDirection(...)`. El primero afecta a cómo el panel exterior acomoda sus elementos (generalmente inútil, ya que su único hijo interno directo es el `contentPanel`), mientras que el segundo define cómo se organizan los elementos que tú agregas al scroll.