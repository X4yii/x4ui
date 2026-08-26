# Installation

X4UI is distributed as a standard modding dependency for Minecraft Forge 1.12.2 using Gradle.

## Requirements

* Minecraft 1.12.2
* Minecraft Forge (1.12.2)

## Project Setup (build.gradle)

To integrate X4UI into an external or standalone mod, you must add the JitPack repository and register X4UI as a compile dependency.

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    // Replace r1.0b3 with the desired version/tag
    compile 'com.github.X4yi:X4UI:r1.0b3'
}
```

Make sure to refresh Gradle and your IDE workspace (`setupDecompWorkspace` and/or native sync) before importing the framework classes.
