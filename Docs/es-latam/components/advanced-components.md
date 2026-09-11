# Componentes Avanzados

Esta sección documenta los componentes diseñados para manejar alto rendimiento o entradas de datos complejas.

## GuiVirtualList<T>

`GuiVirtualList` es el componente más avanzado de X4UI para mostrar listas masivas (cientos o miles de elementos). En lugar de instanciar miles de componentes y saturar la memoria, utiliza **Reciclaje de Vistas (View Recycling)**: solo instancia y renderiza los componentes que son actualmente visibles en la pantalla. A medida que el usuario hace scroll, los componentes que salen por arriba se reciclan y se rellenan con los datos que entran por abajo.

### Implementación Responsiva

Para que las filas de una lista virtual se escalen correctamente bajo el estándar de `FlexLayout`, debes asegurarte de dos cosas:
1. El `GuiVirtualList` debe recibir un tamaño (mediante `.withPercentWidth()`).
2. Las filas generadas por tu lambda **deben** solicitar heredar ese tamaño devolviendo su componente con `.withPercentWidth(1.0f)`.

### Ejemplo Práctico:

```java
import com.x4yi.x4ui.client.gui.component.GuiVirtualList;
import com.x4yi.x4ui.client.gui.component.GuiLabel;

// Definición de tu modelo de datos
class MyItem {
    public String name;
    public MyItem(String name) { this.name = name; }
}

// 1. Instanciar pasándole el Alto por fila (ej. 24px) y los lambdas de construcción y enlazado.
GuiVirtualList<MyItem> virtualList = (GuiVirtualList<MyItem>) new GuiVirtualList<MyItem>(
    0, 0, 0, 300, 24,
    // Constructor (se llama solo para los elementos visibles iniciales):
    (MyItem item) -> {
        // MUY IMPORTANTE: La fila debe tener width porcentual 1.0f para llenar el panel
        return new GuiLabel(0, 0, 0, 20, "", null).withPercentWidth(1.0f);
    },
    // Binder (se llama constantemente al hacer scroll para rellenar datos):
    (GuiComponent comp, MyItem item) -> {
        GuiLabel label = (GuiLabel) comp;
        label.setText(item.name);
    }
).withPercentWidth(1.0f); // El VirtualList en sí también toma el 100% de su padre.

// Llenarlo con 1000 elementos
List<MyItem> thousandItems = new ArrayList<>();
for (int i = 0; i < 1000; i++) {
    thousandItems.add(new MyItem("Fila " + i));
}
virtualList.setItems(thousandItems);
```

> [!CAUTION]
> **El Ancho Cero (0 Width) en Filas Virtuales**
> Si olvidas poner `.withPercentWidth(1.0f)` en la raíz del componente que devuelve el lambda constructor, la fila heredará el ancho absoluto inicial (`0` en el ejemplo superior) y se volverá totalmente invisible, dejándote con una lista aparentemente vacía aunque los datos estén allí.