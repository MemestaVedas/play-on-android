# Expressive Art Direction: Real App Redesigns

This document analyzes how Google's own apps applied M3 Expressive in their 2025 redesigns — extracting practical patterns that any app can adopt.

---

## Gmail

**When:** Gradual rollout throughout 2025, mostly complete by December 2025.

### What Changed

**SearchAppBar:**
- Navigation hamburger moved **outside** the search pill
- Profile avatar moved **outside** the search pill (right side)
- The pill itself became the pure search container — nothing competes with it inside
- Pill shape: `Full` (fully rounded) with a slight tonal elevation

**Type:**
- Sender names: `titleMedium` → `titleMediumEmphasized` when unread
- Subject line: `bodyLarge` → `bodyLargeEmphasized` when unread
- Preview snippet: `bodySmall` in `onSurfaceVariant` — unchanged
- The emphasized/unread distinction is purely typographic, no color needed

**NavigationBar:**
- Shorter than before — reclaims content space
- Active item uses `secondaryContainer` indicator pill (M3 1.4.0 default)
- Active icon uses `secondary` color (changed from `onSurface` in 1.4.0)

**FAB:**
- Standard pill-shaped `ExtendedFloatingActionButton` ("Compose")
- Collapses to icon-only when scrolling down (existing pattern preserved)

**Motion:**
- Email open/close: spring slide from right, `defaultSpatialSpec()`
- Swipe-to-archive: spring detach with slight bounce at boundary, haptic rumble
- List load: stagger fade-in at 40ms per item

### Key Lessons

1. Moving nav elements outside the search pill creates an unambiguous search area
2. Typography alone (regular vs. emphasized) creates strong read/unread visual hierarchy
3. The shorter nav bar combined with a slightly bigger type scale makes more content visible

---

## Google Drive

**When:** H2 2025 redesign.

### What Changed

**Header:**
- Folder name: `headlineMedium` (28sp) — dramatically larger than before
- File/folder count: `bodySmall` in `onSurfaceVariant` immediately below
- The size contrast (28sp vs. 12sp) is the entire hierarchy — no color difference needed

**File Grid Cards:**
- File type icon: using `MaterialShapes.RoundedSquare.toShape()` as container background
- File name: `bodyMedium`
- Last modified: `labelSmall` in `onSurfaceVariant`
- Grid cards use `surfaceContainerLow` — one step above background

**FAB:**
- Large, `Full` shape, primary color
- Morphs to `ExtendedFloatingActionButton` with "New" label when idle for 2s (returning user pattern)

**Containers:**
- "Recent" section: `surfaceContainer` card wrapping recent files as a group
- "Quick Access" section: `surfaceContainerLow` with slightly more rounded (`extraLarge`) corners
- Clear spatial separation from page background (`background`) → section card (`surfaceContainer`) → file card (`surfaceContainerHigh`)

**Motion:**
- File rename: spring-expand the text field inline, `defaultSpatialSpec()`
- Upload complete: `LoadingIndicator` morphs to checkmark animation (custom)

### Key Lessons

1. Three-level surface container hierarchy (background → section → item) creates depth without shadows
2. Dramatic headline size for folder name makes current location instantly clear
3. Shape + container color together create a distinctive card "signature"

---

## Google Chrome (Android)

**When:** Rolling out with Chrome v141, October 2025.

### What Changed

**Overflow Menu:**
- Action icons at top: now in squircle (`RoundedSquare`) containers with `surfaceContainerLow` fill
- Previously: simple text list without any container
- Container fill color: `secondaryContainer` when selected (bookmark, etc.)
- The containerization makes the action strip scannable at a glance

**Tab Strip:**
- New tab button: `RoundedSquare` container with dynamic color background
- Incognito, Tab, Groups icons: all in squircle containers
- Tab groups: use the assigned group color as the chip color

**Progress Bar:**
- Now has fully-rounded pill ends (`StrokeCap.Round`)
- Taller (8dp) and more prominent

**Bookmarked State:**
- Star icon morphs to a rounded-square container when bookmarked — visual confirmation of state change

### Key Lessons

1. Adding containers to action icons (even in a menu) dramatically improves scanability
2. State changes communicated by shape morph (star → rounded container) reduce need for toast messages
3. Tab groups + color coding is a M3E pattern: `tertiaryContainer` for one group, `secondaryContainer` for another

---

## Google Keep

**When:** September 2025 update.

### What Changed

**Note Cards:**
- Notes now explicitly use `surfaceContainerLow` cards — previously floating without a clear container
- Grid layout: 2-column with `8dp` gaps and `16dp` padding
- Note card shape: `medium` (`12dp`) — not the more aggressive rounding seen in other apps
- Color-coded notes: individual note colors use `tertiaryContainer` variants

**Toolbar:**
- Bottom toolbar replaced with `DockedToolbar` (M3 Expressive migration of `BottomAppBar`)
- Actions: Archive, Background, Labels, More — `IconButton` style

**FAB:**
- "New note" FAB stays as pill `ExtendedFloatingActionButton`

**Typography:**
- Note title: `titleMedium`
- Note body preview: `bodySmall`, 2-line max with ellipsis
- Note metadata (last edit): `labelSmall` in `onSurfaceVariant`

### Key Lessons

1. Color-coded cards using `tertiaryContainer` variants for each color creates a natural M3E theming story
2. Grid layout with explicit gaps and padding is more M3E than borderless adjacent items
3. `DockedToolbar` migration was nearly invisible to users — same function, cleaner visual

---

## Androidify (Google Sample App, 2025)

The official Google sample demonstrating M3 Expressive best practices.

### Signature Patterns

**Camera button:** `MaterialShapes.Cookie9Sided.toShape()` — the most distinctive shape in the app. Recognizable as the "take photo" button instantly.

**Scale indication:** Custom `ScaleIndicationNodeFactory` using `MotionScheme.defaultSpatialSpec()` — replaces the standard ripple with a physics-based scale response.

**Shared element + shape morph:** Avatar transitions between screens using a morph from `Circle` (small avatar) to `RoundedSquare` (full-screen profile) via `sharedBoundsRevealWithShapeMorph`.

**Theme:** Uses `MaterialExpressiveTheme` with `MotionScheme.expressive()` — the bounciest, most lively preset.

**FloatingToolbar:** `HorizontalFloatingToolbar` for prompt type selection — one of the first real uses of this new M3E component.

### Source Code
https://github.com/android/socialite (Androidify sample)

---

## Common Patterns Across All Redesigns

1. **Navigation elements moved outside search pills** (Gmail, Drive)
2. **`DockedToolbar` replaces `BottomAppBar`** (Keep, Chrome)
3. **`surfaceContainerLow/High` for cards** — consistent across all apps
4. **Dramatic type size contrast** — large heading + tiny metadata (Gmail sender, Drive folder)
5. **Squircle/RoundedSquare containers for action icons** (Chrome, Drive)
6. **Stagger list entry animations** at 40–60ms per item
7. **Spring swipe/dismiss** with haptic rumble at boundary
8. **`onSurfaceVariant` for all secondary text** — no custom grey values

---

## Art Direction Rules Derived from Real Apps

| Rule | Evidence |
|---|---|
| One dramatic size in the hierarchy | Gmail sender name, Drive folder heading |
| Secondary text always `onSurfaceVariant` | Universal across all redesigns |
| Three surface depths on any content page | Universal: background → section → item |
| Actions need containers, not just icons | Chrome overflow, Drive file types |
| Shape morphing = state change | Chrome bookmark, Gmail swipe |
| Bottom toolbars → `DockedToolbar` | Gmail, Keep, Chrome |
| Search pill = pure search, nav outside | Gmail, Drive |
