# What is X4UI?

X4UI is a Graphical User Interface (GUI) framework for Minecraft Forge 1.12.2. It simplifies menu creation by replacing the rigid Vanilla system with a Component Object Model (DOM), similar to modern web development.

## Main Features

* **Tree Architecture (DOM)**: Interfaces are built by nesting components inside other components (parents and children).
* **FlexBox Layout**: Automatic positioning and dynamic spacing. No manual X and Y coordinate math is needed.
* **High Performance**: The *Dirty Flags* system ensures layout and rendering are only recalculated when a component actually changes.
* **Robust Events**: Mouse clicks and key presses respect visual ordering (Z-Index). A click will not pass through overlapping windows.
* **Reactivity (`State<T>`)**: Variables that automatically notify the UI when their value changes.
