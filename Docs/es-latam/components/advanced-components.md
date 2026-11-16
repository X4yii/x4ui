# Componentes Avanzados (`GuiVirtualList`, `GuiDropdown`, `GuiMarkdown`, `GuiModelRender`, `GuiTooltip`)

Componentes especializados para listas masivas recicladas, selección desplegable, renderizado Markdown, modelos 3D con depth buffer y tooltips contextuales.

---

## 1. `GuiVirtualList<T>`

Implementa **reciclaje de vistas (*View Recycling*)**: solo instancia y dibuja las filas visibles en el viewport actual, manteniendo uso constante de memoria incluso con miles de registros:

```java
class Entry {
    final String name;
    Entry(String name) { this.name = name; }
}

GuiVirtualList<Entry> list = (GuiVirtualList<Entry>) new GuiVirtualList<Entry>(
    0, 0, 0, 240, 24,
    // Builder: instancia una fila visible (debe usar withPercentWidth(1.0f))
    item -> new GuiLabel(0, 0, item.name, 0xFFE6E8EC).withPercentWidth(1.0f),
    // Binder: actualiza la fila reciclada al hacer scroll
    (comp, item) -> ((GuiLabel) comp).setText(item.name)
).withPercentWidth(1.0f);

list.setItems(entries);
```

---

## 2. `GuiDropdown<T>`

Selector desplegable enlazado a un `State<T>` y a una lista de opciones:

```java
State<String> selectedMode = new State<>("Normal");
List<String> modes = Arrays.asList("Normal", "Compacto", "Avanzado");

GuiDropdown<String> dropdown = (GuiDropdown<String>) new GuiDropdown<>(
    0, 0, 0, 22, selectedMode, modes
).withPercentWidth(1.0f);
```

---

## 3. `GuiMarkdown`

Renderiza texto enriquecido (encabezados, listas, código, enlaces y separadores) respetando el ancho del contenedor y la fuente asignada:

```java
GuiMarkdown docView = (GuiMarkdown) new GuiMarkdown(0, 0, 0, "# Guía\nContenido **enriquecido**.")
    .withFont("roboto_regular")
    .withPercentWidth(1.0f);
```

---

## 4. `GuiModelRender` y `GuiTooltip`

- **`GuiModelRender`:** renderiza entidades (`setEntity`) o ítems (`setItemStack`) en 3D limpiando el buffer de profundidad de forma segura (`IGraphics.clearDepthBuffer()`), con soporte de escala (`setScale`), rotación (`setRotation`) y arrastre (`setAllowDragRotation`).
- **`GuiTooltip`:** componente de tooltip flotante configurable también mediante `.withTooltip(...)` en cualquier `GuiComponent`.

---

## 5. Qué Evitar en Componentes Avanzados

| Antipatrón | Causa Técnica del Problema | Solución Correcta |
| :--- | :--- | :--- |
| Omitir `.withPercentWidth(1.0f)` en el lambda builder de `GuiVirtualList` | La fila conserva ancho `0` y no se renderiza | Devolver el componente raíz de cada fila con `.withPercentWidth(1.0f)` |
| Instanciar miles de `GuiPanel` en `GuiScrollPanel` para datos tabulares | Satura memoria y tiempo de layout | Utilizar `GuiVirtualList<T>` con reciclaje de vistas |
