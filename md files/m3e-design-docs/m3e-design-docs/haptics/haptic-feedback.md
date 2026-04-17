# Haptic Feedback in M3 Expressive

Haptic feedback is the physical companion to visual motion in M3 Expressive. When a spring-based animation plays, a corresponding haptic pulse gives the user tactile confirmation. Together, they create the sense that UI elements have physical mass and presence.

---

## Why Haptics Matter in M3 Expressive

M3 Expressive's spring-based motion already makes interactions feel more physical. Haptics complete that loop at the sensory level — the user hears, sees, and *feels* the interaction simultaneously. In Google's research, the combination of expressive motion + haptics significantly outperformed motion alone for user satisfaction scores.

Real examples shipped in Android 16:
- Dismissing a notification → smooth detach animation + haptic rumble
- Dragging the notification shade down → progressive haptic clicks tracking the drag position
- Volume slider → haptic tick at each step
- Button group selection → subtle haptic on shape morph + selection

---

## The Android Haptics API

Android provides `HapticFeedbackConstants` for standard semantic haptic events. In Compose, these are triggered via `LocalHapticFeedback`.

```kotlin
val haptic = LocalHapticFeedback.current

// Common haptic events
haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
haptic.performHapticFeedback(HapticFeedbackType.LongPress)
haptic.performHapticFeedback(HapticFeedbackType.Confirm)
haptic.performHapticFeedback(HapticFeedbackType.Reject)
```

---

## Haptic Feedback Types (Compose + Android)

### Compose HapticFeedbackType

| Type | Use case |
|---|---|
| `LongPress` | Long-press interaction detected |
| `TextHandleMove` | Dragging a text selection handle |
| `Confirm` (Android 13+ / API 33) | Successful action, confirmation |
| `Reject` (Android 13+ / API 33) | Failed action, rejection |
| `ToggleOn` (API 34) | Toggle turned on |
| `ToggleOff` (API 34) | Toggle turned off |
| `GestureStart` (API 34) | Gesture begins |
| `GestureEnd` (API 34) | Gesture completes |
| `ContextClick` (API 34) | Secondary action / right-click equivalent |
| `VirtualKey` (API 34) | Keyboard/virtual key press |
| `ClockTick` | Timer increment, progress step |
| `KeyboardPress` | Key press on soft keyboard |
| `KeyboardRelease` | Key release on soft keyboard |
| `VirtualKeyRelease` | Virtual key release |
| `DragStart` | Drag initiated |
| `SegmentFrequentTick` | Frequent segment change (slider) |
| `SegmentTick` | Segment/step change |

---

## Haptic Patterns for M3 Expressive Interactions

### Button Press
```kotlin
val haptic = LocalHapticFeedback.current

Button(
    onClick = {
        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
        // do action
    }
) { Text("Confirm") }
```

### Toggle Switch
```kotlin
var checked by remember { mutableStateOf(false) }
val haptic = LocalHapticFeedback.current

Switch(
    checked = checked,
    onCheckedChange = { newValue ->
        haptic.performHapticFeedback(
            if (newValue) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff
        )
        checked = newValue
    }
)
```

### Drag to Dismiss / Swipe
```kotlin
val haptic = LocalHapticFeedback.current
val dismissThreshold = 200.dp.toPx()

// Trigger haptic when crossing dismissal threshold
LaunchedEffect(dragOffset) {
    if (abs(dragOffset) > dismissThreshold && !hapticFired) {
        haptic.performHapticFeedback(HapticFeedbackType.GestureEnd)
        hapticFired = true
    }
}
```

### Slider (with per-step haptic)
```kotlin
val haptic = LocalHapticFeedback.current
var sliderValue by remember { mutableFloatStateOf(0f) }
var lastStep by remember { mutableIntStateOf(0) }

Slider(
    value = sliderValue,
    onValueChange = { newValue ->
        val newStep = (newValue * 10).roundToInt()
        if (newStep != lastStep) {
            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
            lastStep = newStep
        }
        sliderValue = newValue
    },
    steps = 9  // 10 steps total
)
```

### Error / Rejection
```kotlin
val haptic = LocalHapticFeedback.current
var showError by remember { mutableStateOf(false) }

LaunchedEffect(showError) {
    if (showError) {
        haptic.performHapticFeedback(HapticFeedbackType.Reject)
        delay(3000)
        showError = false
    }
}
```

### Selection Change (ButtonGroup, FilterChip)
```kotlin
val haptic = LocalHapticFeedback.current

FilterChip(
    selected = selected,
    onClick = {
        haptic.performHapticFeedback(
            if (!selected) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff
        )
        selected = !selected
    },
    label = { Text("Filter") }
)
```

---

## Advanced: Vibration Effect API (API 26+)

For more precise haptic control, use `VibrationEffect` directly:

```kotlin
val vibrator = context.getSystemService(Vibrator::class.java)

// Simple pulse
vibrator.vibrate(
    VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
)

// Waveform pattern (timing in ms, amplitude)
vibrator.vibrate(
    VibrationEffect.createWaveform(
        longArrayOf(0, 30, 50, 30),   // timings
        intArrayOf(0, 180, 0, 100),   // amplitudes (0–255)
        -1  // no repeat
    )
)

// Predefined effects (API 29+)
vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
```

---

## Haptics Design Guidelines

**Do:**
- Pair haptics with visual spring animations — the haptic should coincide with the peak of the spring
- Use `Confirm` for completed actions, `Reject` for errors, `ToggleOn/Off` for state toggles
- Use `SegmentTick` for continuous drag/slider interactions at each discrete step
- Keep haptics subtle — `DEFAULT_AMPLITUDE` is usually correct; never use max amplitude for standard interactions

**Don't:**
- Play haptics on every animation frame — only on discrete state changes
- Play haptics during animations that have no user interaction (loading indicators, auto-advancing carousels)
- Ignore `AudioManager.getRingerMode()` — if the device is in silent/vibrate mode, reduce or skip non-essential haptics
- Use haptics as the *only* feedback channel — always pair with visual change

---

## Checking Haptic Support

```kotlin
val vibrator = context.getSystemService(Vibrator::class.java)

if (vibrator.hasVibrator()) {
    // Device has a vibrator
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        if (vibrator.hasAmplitudeControl()) {
            // Precise amplitude control available
        }
    }
}
```
