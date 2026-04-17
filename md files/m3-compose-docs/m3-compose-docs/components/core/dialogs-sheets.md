# Dialogs & Bottom Sheets (Core M3)

---

## AlertDialog

Use for confirmations, decisions, or brief information requiring a response.

```kotlin
var showDialog by remember { mutableStateOf(false) }

if (showDialog) {
    AlertDialog(
        onDismissRequest = { showDialog = false },
        icon = { Icon(Icons.Filled.Warning, contentDescription = null) },
        title = { Text("Delete item?") },
        text = { Text("This action cannot be undone. The item will be permanently deleted.") },
        confirmButton = {
            TextButton(onClick = {
                // confirm action
                showDialog = false
            }) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = { showDialog = false }) {
                Text("Cancel")
            }
        }
    )
}
```

---

## BasicAlertDialog (Custom Layout)

For custom dialog layouts beyond the standard title/body/buttons pattern:

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDialog(onDismiss: () -> Unit) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Custom Dialog", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
                // custom content
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Close") }
                }
            }
        }
    }
}
```

---

## ModalBottomSheet

Slides up from the bottom for secondary content, options, or contextual actions.

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetExample() {
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Button(onClick = { showSheet = true }) {
        Text("Open Sheet")
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 32.dp)) {
                Text("Sheet Title", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                ListItem(
                    headlineContent = { Text("Option A") },
                    leadingContent = { Icon(Icons.Filled.Share, contentDescription = null) },
                    modifier = Modifier.clickable { showSheet = false }
                )
                ListItem(
                    headlineContent = { Text("Option B") },
                    leadingContent = { Icon(Icons.Filled.Delete, contentDescription = null) },
                    modifier = Modifier.clickable { showSheet = false }
                )
            }
        }
    }
}
```

### Controlling Sheet Height

```kotlin
// Skip partially expanded state (go directly to fully expanded or hidden)
val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

// Peek height = 0 now disables partiallyExpanded (1.4.0+)
// Setting sheetPeekHeight to 0.dp in BottomSheetScaffold disables the partially expanded state
```

---

## BottomSheetScaffold

For sheets that are part of the screen layout (not modal overlays):

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaffoldWithSheet() {
    val scaffoldState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Sheet Content", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("Always visible content here.")
            }
        },
        sheetPeekHeight = 80.dp
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // main content
        }
    }
}
```

---

## StaticSheet (New in 1.4.0+)

`StaticSheet` is a new standalone static (non-modal) sheet component — useful for side panels or persistent content areas:

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaticSheetExample() {
    Row(modifier = Modifier.fillMaxSize()) {
        // Main content
        Box(modifier = Modifier.weight(1f)) {
            // screen content
        }

        // Static sheet on the side
        StaticSheet(modifier = Modifier.width(300.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Details Panel", style = MaterialTheme.typography.titleMedium)
                // panel content
            }
        }
    }
}
```

---

## Back Handler in BottomSheet

As of 1.4.0+, you can disable the built-in back handler for bottom sheets:

```kotlin
ModalBottomSheet(
    onDismissRequest = { showSheet = false },
    sheetState = sheetState,
    // Disable default back-press dismissal (handle it yourself)
    // Use parameter: dragHandle = null to remove the drag handle
)
```

A parameter to fully disable the back handler was introduced in a recent alpha — check `ModalBottomSheetDefaults` for current API.

---

## Design Guidelines

- **Use `AlertDialog`** for short, important interruptions requiring a decision
- **Never** put complex forms or scrollable content in an `AlertDialog` — use `ModalBottomSheet`
- **Use `ModalBottomSheet`** for: share sheets, action menus, filter panels, brief forms
- **Use `BottomSheetScaffold`** when the sheet is persistent and part of the page layout
- Dialog titles should be questions or noun phrases, not complete sentences
- Confirm button should describe the action ("Delete", "Save") not just "OK"
- Destructive actions (delete, remove) should use `error` color on the confirm button
