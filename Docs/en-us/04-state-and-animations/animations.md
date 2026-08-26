# Animations (`GuiAnimator`)

Instead of doing manual calculations every frame, X4UI uses `GuiAnimator` to move and transition things smoothly.

## How does it work?

The animator changes a numeric value (like a position or size) from a start to an end, in a specific time. It uses the system clock, so animations always take the same time, regardless of whether Minecraft is running at 30 or 144 FPS.

## Main API

To start an animation, use the `GuiAnimator.animate` method:

```java
GuiAnimator.animate(
    this,              // Target component (to identify the animation)
    "background_opacity", // Unique name for this property
    0.0f,              // Start value
    1.0f,              // End value
    100,               // Total duration in milliseconds
    Easing.EASE_OUT,   // Speed curve
    value -> myOpacity = value // What to do with the animated value each frame
);
```

### Avoiding Conflicts

If you call `animate()` using the same component and the same name ("background_opacity"), the previous animation is instantly canceled and the new one begins. This is perfect to avoid bugs when the mouse quickly enters and leaves a button.

## Easing Types (Speed)

`Easing` curves give a natural feel to the movement:
* `LINEAR`: Constant and flat speed from start to finish.
* `EASE_IN`: Starts slowly, ends fast.
* `EASE_OUT`: Starts fast, ends slowly (ideal for screen pop-ins).
* `EASE_IN_OUT`: Smooth at the start and the end.
