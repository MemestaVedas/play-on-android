# Text Fields (Core M3)

M3 provides two text field styles: **Filled** and **Outlined**. Both are functionally identical but differ visually in emphasis and context suitability.

---

## Variants

| Variant | Composable | Use When |
|---|---|---|
| Filled | `TextField` | Forms embedded in a surface (default) |
| Outlined | `OutlinedTextField` | Standalone forms, higher contrast needed |
| Secure (new 1.4.0) | `SecureTextField` / `OutlinedSecureTextField` | Passwords, PINs |

---

## Basic Usage

```kotlin
var value by remember { mutableStateOf("") }

// Filled
TextField(
    value = value,
    onValueChange = { value = it },
    label = { Text("Email") }
)

// Outlined
OutlinedTextField(
    value = value,
    onValueChange = { value = it },
    label = { Text("Email") }
)
```

---

## With Leading/Trailing Icons

```kotlin
OutlinedTextField(
    value = value,
    onValueChange = { value = it },
    label = { Text("Search") },
    leadingIcon = {
        Icon(Icons.Filled.Search, contentDescription = null)
    },
    trailingIcon = {
        if (value.isNotEmpty()) {
            IconButton(onClick = { value = "" }) {
                Icon(Icons.Filled.Close, contentDescription = "Clear")
            }
        }
    }
)
```

---

## Error State

```kotlin
var isError by remember { mutableStateOf(false) }

OutlinedTextField(
    value = value,
    onValueChange = {
        value = it
        isError = it.length > 50
    },
    label = { Text("Username") },
    isError = isError,
    supportingText = {
        if (isError) {
            Text(
                text = "Username cannot exceed 50 characters",
                color = MaterialTheme.colorScheme.error
            )
        } else {
            Text("${value.length}/50")
        }
    },
    trailingIcon = {
        if (isError) Icon(Icons.Filled.Error, contentDescription = "Error")
    }
)
```

---

## Secure Text Fields (New in 1.4.0)

`SecureTextField` and `OutlinedSecureTextField` handle password masking automatically and prevent screenshots/clipboard access on Android.

```kotlin
// Filled secure field
SecureTextField(
    state = rememberTextFieldState(),
    label = { Text("Password") }
)

// Outlined secure field
OutlinedSecureTextField(
    state = rememberTextFieldState(),
    label = { Text("Confirm Password") }
)
```

---

## TextFieldState API (Experimental, 1.4.0+)

The new `TextFieldState`-based API provides more robust state management:

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatefulTextField() {
    val state = rememberTextFieldState(initialText = "Hello")

    TextField(
        state = state,
        label = { Text("Name") }
    )

    // Access text value
    Text("You typed: ${state.text}")
}
```

Key advantage: `TextFieldState` integrates natively with Compose's state system and handles IME actions/selection more robustly than the `value`/`onValueChange` pattern.

---

## Keyboard Options

```kotlin
OutlinedTextField(
    value = value,
    onValueChange = { value = it },
    label = { Text("Phone Number") },
    keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Phone,
        imeAction = ImeAction.Done
    ),
    keyboardActions = KeyboardActions(
        onDone = { /* handle done */ }
    )
)
```

---

## Single vs Multi-line

```kotlin
// Single line (no newlines allowed)
TextField(
    value = value,
    onValueChange = { value = it },
    label = { Text("Title") },
    singleLine = true
)

// Multi-line with min/max lines
TextField(
    value = value,
    onValueChange = { value = it },
    label = { Text("Description") },
    minLines = 3,
    maxLines = 6
)
```

---

## Auto-size Text (1.4.0+)

The M3 `Text` composable now supports `autoSize` behavior. For text fields, use `autoSize` with a `BasicTextField` if you need the label to shrink to fit:

```kotlin
Text(
    text = longText,
    maxLines = 1,
    overflow = TextOverflow.Clip,
    style = MaterialTheme.typography.bodyMedium.copy(
        fontSize = TextUnit.Unspecified  // enables auto-sizing
    )
)
```

---

## Design Guidelines

- Use `OutlinedTextField` when the field stands alone against a non-surface background
- Use `TextField` (filled) in forms on cards or sheets
- Always provide a `label` — placeholders alone are not accessible
- Use `supportingText` for helper text or character counts (shown below the field)
- `isError` should reflect real-time validation — don't wait for form submission
- Minimum width: 280dp for readability; full-width on mobile screens
