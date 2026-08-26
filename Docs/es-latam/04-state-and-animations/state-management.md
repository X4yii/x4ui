# Manejo de Estados (`State<T>`)

X4UI ofrece un sistema simple para que la interfaz reaccione automáticamente a los cambios de datos, usando `State<T>`.

## ¿Qué es State?

Un `State<T>` es una variable que avisa a la interfaz cuando su valor cambia. Así, los componentes se actualizan solos.

## Métodos Principales

* `get()`: Obtiene el valor actual.
* `set(nuevoValor)`: Cambia el valor y avisa inmediatamente a todos los componentes que lo están usando.
* `addListener(funcion)`: Ejecuta una función cuando el valor cambia.
* `map(funcion)`: Transforma el valor para otro componente. Por ejemplo, convertir un Entity ID a su Nombre de display.

## Ejemplo Práctico en Modding

Muchos componentes, como `GuiSlider` o `GuiDropdown`, usan estados directamente. Supongamos que estamos creando un menú de configuración para cambiar el multiplicador de aparición de Mobs:

```java
// 1. Creamos un estado numérico para el multiplicador de Spawn
State<Float> spawnRate = new State<>(1.5f);

// 2. Le pasamos el estado al slider (Rango de 0.0 a 5.0)
GuiSliderFloat sliderSpawn = new GuiSliderFloat(0, 0, 100, 20, spawnRate, 0.0f, 5.0f, 0.1f);

// 3. Escuchamos los cambios para enviarlos al servidor
spawnRate.addListener(nuevoRate -> {
    NetworkHandler.sendToServer(new PacketSyncConfig("mob_spawn_rate", nuevoRate));
});
```

Si por alguna razón recibes un paquete del servidor (ej. un admin modificó el valor globalmente) y cambias `spawnRate.set(nuevoValor)` desde tu código de red, el slider visual en la pantalla del jugador se moverá automáticamente sin tocar código extra.
