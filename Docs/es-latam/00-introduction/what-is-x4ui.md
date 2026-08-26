# ¿Qué es X4UI?

X4UI es un framework de interfaces gráficas (GUI) para Minecraft Forge 1.12.2. Simplifica la creación de menús reemplazando el sistema rígido de Vanilla por un modelo basado en componentes (DOM), similar al desarrollo web.

## Características Principales

* **Arquitectura de Árbol (DOM)**: Interfaces construidas organizando componentes dentro de otros componentes (padres e hijos).
* **Layout FlexBox**: Posicionamiento automático y espaciado dinámico. No necesitas calcular manualmente las coordenadas X e Y.
* **Alto Rendimiento**: El sistema de *Dirty Flags* asegura que el layout y el renderizado solo se recalculen cuando un componente realmente cambia.
* **Eventos Robustos**: Los clics y el teclado respetan el orden visual (Z-Index). Un clic no atravesará ventanas superpuestas.
* **Reactividad (`State<T>`)**: Variables que notifican automáticamente a la UI cuando su valor cambia.
