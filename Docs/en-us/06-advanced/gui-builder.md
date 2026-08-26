# GuiBuilder

So you don't have to write so many lines of repetitive code (boilerplate) creating components one by one, X4UI includes the `GuiBuilder` tool. It allows you to build components in a chain rapidly.

## Quick Creation Example

Instead of creating a panel, creating a text, adjusting its margin, and then adding it to the panel in separate steps, you can do it all at once:

```java
GuiPanel menu = GuiBuilder.createPanel()
    // Panel properties
    .position(0, 0)
    .size(200, 400)
    .padding(10)
    .gap(5)
    .flexDirection(FlexDirection.VERTICAL)
    
    // Add children directly
    .withChild(
        GuiBuilder.createLabel("Machine Configuration", 0xFFFF0000)
            .margin(2)
            .build()
    )
    .withChild(
        GuiBuilder.createButton("Force Overload")
            .size(100, 20)
            .margin(5)
            .onClick(() -> NetworkHandler.sendToServer(new PacketOverload()))
            .build()
    )
    
    // Finish and get the real GuiPanel object
    .build();
```

> **Note:** `createPanel()` creates a panel with default size 100x100. Always use `.size()` to set the desired dimensions.

## Factory Methods

| Method | Creates |
|---|---|
| `createPanel()` | `GuiPanel` |
| `createScrollPanel()` | `GuiScrollPanel` |
| `createButton(text)` | `GuiButton` |
| `createLabel(text)` / `createLabel(text, color)` | `GuiLabel` |
| `createMarkdown(text)` | `GuiMarkdown` |
| `createModelRender()` | `GuiModelRender` |
| `createSlot(slot, container)` | `GuiSlot` |
| `createSprite(texture, texW, texH)` | `GuiSprite` |
| `createSliderInt(state, min, max, step)` | `GuiSliderInt` |
| `createSliderFloat(state, min, max, step)` | `GuiSliderFloat` |
| `createSliderDouble(state, min, max, step)` | `GuiSliderDouble` |
| `createTextInput()` | `GuiTextInput` |
| `createDropdown(state, options)` | `GuiDropdown<E>` |

## Common Setters (All Components)

| Method | Description |
|---|---|
| `position(x, y)` | Set coordinates |
| `size(w, h)` | Set width and height |
| `margin(m)` | Set outer spacing |
| `padding(p)` | Set inner spacing |
| `visible(bool)` | Set visibility |
| `enabled(bool)` | Set enabled state |
| `layer(l)` | Set Z-index layer |
| `tooltip(text)` | Set hover tooltip |
| `draggable(bool)` | Enable free dragging |
| `draggableConstrained(bool)` | Enable dragging constrained to parent bounds |

## Panel-Specific Setters

| Method | Description |
|---|---|
| `flexDirection(dir)` | Set layout direction (VERTICAL, HORIZONTAL, ABSOLUTE) |
| `gap(g)` | Set spacing between children |
| `flexWrap(bool)` | Enable line wrapping |

## Button-Specific Setters

| Method | Description |
|---|---|
| `onClick(runnable)` | Set click callback |

## ModelRender-Specific Setters

| Method | Description |
|---|---|
| `entity(living)` | Set the entity to render |
| `itemStack(stack)` | Set the item to render |
| `modelScale(scale)` | Set model size |
| `modelRotation(yaw, pitch, roll)` | Set initial rotation |
| `modelTranslation(x, y, z)` | Set offset |
| `modelDragRotation(bool)` | Allow user drag rotation |
| `modelLookAtCursor(bool)` | Head follows mouse |

## TextInput-Specific Setters

| Method | Description |
|---|---|
| `placeholder(text)` | Set placeholder text |

## Sprite-Specific Setters

| Method | Description |
|---|---|
| `sliceSize(px)` | Set 9-slice border size |

## Markdown-Specific Setters

| Method | Description |
|---|---|
| `onLinkClicked(consumer)` | Set link click callback |

## Quick Reactive Binding

If you use the state system (`State<T>`), you can connect them directly while building the component.

```java
State<Boolean> requiresRedstone = new State<>(false);

GuiBuilder.createLabel("A Redstone signal is needed to operate")
    // It will automatically hide or show if the 'requiresRedstone' state changes
    .bindVisible(requiresRedstone) 
    .build();
```

| Method | Description |
|---|---|
| `bindVisible(state)` | Bind visibility to a `State<Boolean>` |
| `bindEnabled(state)` | Bind enabled state to a `State<Boolean>` |
| `bindLabelText(state)` | Bind label text to a `State<String>` |

## Building and Getting the Result

Call `.build()` at the end of the chain to get the actual component object:

```java
GuiButton btn = GuiBuilder.createButton("Click me")
    .size(120, 30)
    .margin(5)
    .onClick(() -> System.out.println("Clicked!"))
    .build();
```
