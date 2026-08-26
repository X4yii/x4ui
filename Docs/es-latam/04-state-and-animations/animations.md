# Animaciones (`GuiAnimator`)

En lugar de hacer cálculos manuales en cada frame, X4UI usa el `GuiAnimator` para mover y transicionar cosas de forma fluida.

## ¿Cómo funciona?

El animador cambia un valor numérico (como una posición o un tamaño) desde un inicio hasta un fin, en un tiempo específico. Usa el reloj del sistema, por lo que las animaciones siempre duran lo mismo, sin importar si Minecraft va a 30 o a 144 FPS.

## API Principal

Para iniciar una animación, usa el método `GuiAnimator.animate`:

```java
GuiAnimator.animate(
    this,              // Componente objetivo (para identificar la animación)
    "opacidad_fondo",  // Nombre único para esta propiedad
    0.0f,              // Valor inicial
    1.0f,              // Valor final
    100,               // Duración total en milisegundos
    Easing.EASE_OUT,   // Curva de velocidad
    valor -> miOpacidad = valor // Qué hacer con el valor animado cada frame
);
```

### Evitar Conflictos

Si llamas a `animate()` usando el mismo componente y el mismo nombre ("opacidad_fondo"), la animación anterior se cancela al instante y comienza la nueva. Esto es perfecto para evitar errores cuando el ratón entra y sale rápidamente de un botón.

## Tipos de Easing (Velocidad)

Las curvas `Easing` le dan naturalidad al movimiento:
* `LINEAR`: Velocidad constante y plana de principio a fin.
* `EASE_IN`: Empieza despacio, termina rápido.
* `EASE_OUT`: Empieza rápido, termina despacio (ideal para entradas en pantalla de menús del mod).
* `EASE_IN_OUT`: Suave al inicio y al final.
