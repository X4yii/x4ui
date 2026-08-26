# GuiMarkdown

Un componente potente que permite mostrar textos grandes con formato (títulos, negritas, listas e imágenes) sin tener que programar decenas de pequeños textos separados. Es perfecto para crear tutoriales de modding o manuales in-game.

## Uso Básico (Texto Crudo)

```java
GuiMarkdown manual = new GuiMarkdown(0, 0, anchoCaja, 
    "# Generador de Minerales\n" + 
    "Este bloque **requiere energía** para extraer `diamantes`\n\n" + 
    "> Peligro: Puede explotar si se sobrecalienta.\n\n" + 
    "![200x150](mimod:textures/gui/diagrama_energia.png)"
);
```

## Cargar un archivo .md desde los Assets

La forma más limpia y recomendada es guardar tus textos en archivos `.md` dentro de la carpeta de recursos de tu mod (ej: `src/main/resources/assets/mimod/docs/manual.md`), y leerlos usando el sistema nativo de Minecraft.

```java
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.resources.IResource;
import org.apache.commons.io.IOUtils;
import java.nio.charset.StandardCharsets;

// 1. Obtener el archivo desde el ResourcePack de Minecraft
ResourceLocation mdLocation = new ResourceLocation("mimod", "docs/manual.md");
String contenidoMd = "";

try {
    IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(mdLocation);
    contenidoMd = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
} catch (Exception e) {
    contenidoMd = "# Error\nNo se pudo cargar el manual.";
}

// 2. Crear el componente pasándole el contenido leído
GuiMarkdown manualCargado = new GuiMarkdown(0, 0, 200, contenidoMd);
```

### Elementos Soportados

- **Títulos (`#`)**: Se muestran más grandes, en negrita, y añaden más espacio con el texto de abajo.
- **Listas (`* ` o `- `)**: Añaden un punto y sangría automáticamente. Útiles para listas de items.
- **Citas (`> `)**: Dibujan un rectángulo de fondo gris para resaltar advertencias.
- **Imágenes (`![AnchoXAlto](textura)`)**: Muestra assets de Minecraft dentro del texto.

### Auto-Ajuste de Texto (Word Wrapping)

No tienes que preocuparte de poner saltos de línea (`\n`) cuando una oración es muy larga. El componente ajusta automáticamente el texto a la siguiente línea cuando excede el ancho disponible. El ajuste se hace por palabra (por espacios), así que palabras muy largas sin espacios pueden desbordarse.

### Enlaces Clickeables

Si escribes `[Activar Máquina](accion_activar)`, el texto será azul y subrayado. Puedes detectar el clic para ejecutar código en tu mod:

```java
manual.setOnLinkClicked(enlace -> {
    if (enlace.equals("accion_activar")) {
        NetworkHandler.sendToServer(new PacketActivarMaquina());
    }
});
```
