# State Management (`State<T>`)

X4UI offers a simple system for the interface to automatically react to data changes, using `State<T>`.

## What is a State?

A `State<T>` is a variable that notifies the interface when its value changes. This way, components update themselves.

## Main Methods

* `get()`: Gets the current value.
* `set(newValue)`: Changes the value and immediately notifies all components using it.
* `addListener(function)`: Executes a function when the value changes.
* `map(function)`: Transforms the value for another component. For example, converting an Entity ID into its display Name.

## Practical Modding Example

Many components, like `GuiSlider` or `GuiDropdown`, use states directly. Let's suppose we are creating a configuration menu to change the Mob spawn multiplier:

```java
// 1. Create a numeric state for the Spawn multiplier
State<Float> spawnRate = new State<>(1.5f);

// 2. Pass the state to the slider (Range from 0.0 to 5.0)
GuiSliderFloat sliderSpawn = new GuiSliderFloat(0, 0, 100, 20, spawnRate, 0.0f, 5.0f, 0.1f);

// 3. Listen to changes to send them to the server
spawnRate.addListener(newRate -> {
    NetworkHandler.sendToServer(new PacketSyncConfig("mob_spawn_rate", newRate));
});
```

If for some reason you receive a packet from the server (e.g. an admin modified the value globally) and you change `spawnRate.set(newValue)` from your network code, the visual slider on the player's screen will move automatically without touching any extra code.
