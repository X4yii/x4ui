# Instalación

X4UI se distribuye como una dependencia de modding estándar para Minecraft Forge 1.12.2 usando Gradle.

## Dependencias Requeridas

* Minecraft 1.12.2
* Minecraft Forge (1.12.2)

## Setup del Proyecto (build.gradle)

Para integrar X4UI en un mod externo o standalone, debes añadir el repositorio de JitPack y registrar X4UI como una dependencia de compilación.

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    // Reemplaza r1.0b3 con el tag / versión correspondiente deseada
    compile 'com.github.X4yi:X4UI:r1.0b3'
}
```

Es imperativo refrescar Gradle y su entorno de IDE (`setupDecompWorkspace` y/o sincronización nativa) antes de importar clases del framework.
