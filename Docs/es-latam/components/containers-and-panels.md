# Contenedores y Paneles (`GuiPanel`, `GuiCard`, `GuiScrollPanel`, `GuiSlot`)

Los contenedores organizan la jerarquía visual, gestionan el diseño mediante `ILayoutManager` (`FlexLayout`) y controlan el recorte (*clipping*) de sus hijos.

---

## 1. `GuiPanel` y Primitivas Semánticas

`GuiPanel` es el contenedor estructural base. En `r1.0b6` provee constructores estáticos orientados al estándar responsivo:

```java
// Contenedor en columna (VERTICAL, hugContent=true, 100% ancho)
GuiPanel column = GuiPanel.column(8);

// Contenedor en fila (HORIZONTAL, 100% ancho) con espaciador flexible
GuiPanel row = GuiPanel.row(8);
row.addChild(new GuiLabel(0, 0, "Título", 0xFFFFFFFF));
row.addChild(GuiPanel.spacer());
row.addChild(new GuiButton(0, 0, 20, 20, "X", this::closeScreen));

// Tarjeta temática con padding y hugContent automático
GuiPanel card = GuiPanel.card(10, 6);
```

También es posible configurar manualmente la dirección, alineación (`FlexAlign`) y relleno:

```java
GuiPanel panel = new GuiPanel(0, 0, 0, 0).withPercentWidth(1.0f);
panel.setFlexDirection(FlexDirection.VERTICAL);
panel.setGap(6);
panel.setPadding(new Insets(10));
```

---

## 2. `GuiCard`

`GuiCard` extiende `GuiPanel` con título integrado y estilo de tarjeta del tema activo:

```java
GuiCard card = (GuiCard) new GuiCard(0, 0, 0, 120, "Estadísticas")
    .withPercentWidth(1.0f);
```

---

## 3. `GuiScrollPanel`

`GuiScrollPanel` encapsula un sub-contenedor interno (`contentPanel`) dentro de un viewport con recorte hardware y desplazamiento normalizado (`UIInput`):

```java
// Forma recomendada: scroll vertical preconfigurado
GuiScrollPanel scroll = GuiScrollPanel.vertical(220, 6);
scroll.addChild(card1); // addChild() redirige internamente hacia contentPanel
scroll.addChild(card2);
```

Cuando el contenido supera la altura del viewport, `GuiScrollPanel` reserva automáticamente el espacio de la barra de desplazamiento para evitar recorte horizontal.

---

## 4. `GuiSlot`

`GuiSlot` vincula un `Slot` de inventario vanilla dentro de `GuiBaseContainer` al árbol de componentes:

```java
for (Slot slot : inventorySlots.inventorySlots) {
    rootPanel.addChild(new GuiSlot(slot, this));
}
```

---

## 5. Qué Evitar en Contenedores

| Antipatrón | Causa Técnica del Problema | Solución Correcta |
| :--- | :--- | :--- |
| Configurar `scroll.setFlexDirection(...)` en lugar de `scroll.getContentPanel().setFlexDirection(...)` | Modifica el contenedor externo del viewport en lugar del panel interno de ítems | Usar `GuiScrollPanel.vertical(...)` o configurar `getContentPanel()` |
| Usar `GuiPanel` sin `GuiScrollPanel` para listas o formularios largos | El contenido queda inaccesible fuera de pantalla en GUI Scale 3x y 4x | Envolver todo flujo vertical extenso en `GuiScrollPanel` |
