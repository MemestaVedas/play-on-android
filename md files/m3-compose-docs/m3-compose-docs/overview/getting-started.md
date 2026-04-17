# Getting Started with Compose Material 3

## Dependencies

### Using the Compose BOM (recommended)

The Compose Bill of Materials (BOM) aligns all Compose library versions automatically.

```kotlin
// build.gradle.kts (app module)
dependencies {
    // BOM — use latest stable or latest alpha
    val composeBom = platform("androidx.compose:compose-bom:2025.12.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Material3 (version managed by BOM)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3-window-size-class")

    // Adaptive navigation suite
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite")

    // Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
```

### Using M3 Expressive (alpha)

To use M3 Expressive components, pin to the alpha artifact directly:

```kotlin
dependencies {
    // Replace BOM-managed version with explicit alpha
    implementation("androidx.compose.material3:material3:1.5.0-alpha11")

    // Adaptive libraries (separate versioning)
    implementation("androidx.compose.material3.adaptive:adaptive:1.3.0-alpha09")
    implementation("androidx.compose.material3.adaptive:adaptive-layout:1.3.0-alpha09")
    implementation("androidx.compose.material3.adaptive:adaptive-navigation:1.3.0-alpha09")
}
```

### Android Gradle Plugin config

```kotlin
android {
    buildFeatures {
        compose = true
    }
    // composeOptions block is NOT needed when using Kotlin 2.0+ with KSP
    kotlinOptions {
        jvmTarget = "17"
    }
}
```

---

## Minimal App Setup

```kotlin
// MainActivity.kt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // recommended for M3E
        setContent {
            MyAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Your root composable
                }
            }
        }
    }
}
```

---

## Generating a Theme with Material Theme Builder

The easiest way to create an M3 theme is the **Material Theme Builder** at https://m3.material.io/theme-builder.

1. Pick a seed color (or upload a wallpaper/logo)
2. Export as **Jetpack Compose**
3. Copy the generated `Color.kt`, `Theme.kt`, and `Type.kt` into your project

The generated `Theme.kt` will look like:

```kotlin
@Composable
fun MyAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true, // Android 12+ wallpaper-based color
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme   // your generated dark scheme
        else -> LightColorScheme       // your generated light scheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,       // your generated type scale
        content = content
    )
}
```

---

## Opting Into Experimental APIs

M3 Expressive APIs and some other M3 APIs are behind an opt-in annotation. Apply it at the call site or file level:

```kotlin
// File level
@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

// Function level
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MyScreen() {
    // Use expressive components here
}
```

---

## Icon Packs (Breaking Change in 1.4.0)

As of material3 `1.4.0`, the library **no longer automatically pulls in** `material-icons-core`. If you use Material icons, add the dependency explicitly:

```kotlin
// Basic icon set (~400 icons)
implementation("androidx.compose.material:material-icons-core")

// Extended icon set (~2000 icons, large binary size)
implementation("androidx.compose.material:material-icons-extended")
```

---

## Version Reference

| Artifact | Latest Stable | Latest Alpha |
|---|---|---|
| `material3` | `1.4.0` | `1.5.0-alpha11` |
| `material3-adaptive` | `1.2.0` | `1.3.0-alpha09` |
| Compose BOM | `2025.12.00` | — |
| Kotlin recommended | `2.0.x` | `2.1.x` |
| Android Gradle Plugin | `8.5+` | — |
