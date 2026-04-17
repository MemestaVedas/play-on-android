# PLAY-ON Architecture and Material 3 Integration

Purpose: Define the target architecture, UX system, and delivery plan for unifying AniList tracking (AniHyou domain) with Aniyomi media consumption (player, reader, extensions) in this repository.

Status: Implementation specification (v2, refined)

Last updated: 2026-04-15

---

## Quick Navigation

1. [Scope and Outcomes](#1-scope-and-outcomes)
2. [Current State in This Repository](#2-current-state-in-this-repository)
3. [Target Architecture](#3-target-architecture)
4. [Material 3 and Adaptive UX System](#4-material-3-and-adaptive-ux-system)
5. [Unified Media Detail Flow](#5-unified-media-detail-flow)
6. [Player Architecture](#6-player-architecture)
7. [Reader Architecture](#7-reader-architecture)
8. [Extension and Repository Platform](#8-extension-and-repository-platform)
9. [Tracking Telemetry and Offline Sync](#9-tracking-telemetry-and-offline-sync)
10. [Security, Reliability, and Performance Controls](#10-security-reliability-and-performance-controls)
11. [Implementation Roadmap (Refined)](#11-implementation-roadmap-refined)
12. [Testing and Acceptance Matrix](#12-testing-and-acceptance-matrix)
13. [Known Risks and Mitigations](#13-known-risks-and-mitigations)
14. [Glossary](#14-glossary)

---

## 1. Scope and Outcomes

### 1.1 Problem

Anime and manga users still switch between two app types:

- Tracking clients that sync AniList data.
- Consumption clients that stream episodes and render chapters.

That split creates repeated manual work: search again, resume manually, and update progress manually.

### 1.2 Outcome

PLAY-ON unifies these flows into one state-driven app:

- Discover, track, watch, and read from a single shell.
- Keep AniList progress in sync from actual playback and reading telemetry.
- Preserve Aniyomi power features (extensions, player controls, reader modes).
- Stay native to Material 3 while scaling from phone to tablet/foldable layouts.

### 1.3 Non-goals

- Building a centralized content host.
- Replacing extension architecture with hardcoded providers.
- Shipping social features before core tracking-consumption convergence is stable.

---

## 2. Current State in This Repository

This spec is intentionally aligned to the real codebase, not an idealized stack.

### 2.1 Architecture Facts

- The project already follows modular layering across `app`, `domain`, `data`, and `core` modules.
- Dependency injection uses Injekt patterns in the main app context.
- Tracking persistence already exists for anime and manga sync entities.
- Apollo GraphQL tooling is available in the workspace.
- Adaptive layouts already exist at app-shell level and should be extended, not replaced.

### 2.2 Integration Opportunity

AniHyou capability and Aniyomi capability are both present in the workspace, but not yet merged as one cohesive product flow. The integration seam is clear:

- AniHyou side: AniList metadata, lists, profile context.
- Aniyomi side: source mapping, playback/reader engines, download and extension stack.

### 2.3 Hard Constraints

- OAuth redirect and AniList login behavior must respect existing repo-level operational knowledge.
- Extension behavior must stay sandboxed through app-controlled networking.
- UI integration must respect existing app navigation shell rather than replacing it with unrelated patterns.

---

## 3. Target Architecture

### 3.1 System View

The unified app is a coordinated multi-state system with five major domains:

1. Account and Tracking Domain
2. Discovery and Metadata Domain
3. Source Mapping and Consumption Domain
4. Telemetry and Sync Domain
5. UI Shell and Adaptive Layout Domain

Each domain has clear ownership, interface boundaries, and failure behavior.

### 3.2 Module Responsibility Map

| Capability | Domain Layer | Data Layer | Presentation Layer |
|---|---|---|---|
| AniList auth/session | tracking auth use-cases | token/session store | login/profile screens |
| Media list sync | track interactors | track repositories | list/detail state models |
| Metadata queries | media query use-cases | GraphQL gateway | discover/detail UI |
| Source mapping | match/mapping use-cases | mapping cache + source adapters | source picker + state chips |
| Player telemetry | telemetry use-cases | mutation queue + sync worker | player overlay + playback state |
| Reader telemetry | chapter telemetry use-cases | mutation queue + read events | reader HUD + chapter controls |

### 3.3 Requirement Levels

- Must: required for V1 integrated experience.
- Should: strongly preferred for stability/quality.
- May: optional enhancement after acceptance gates.

### 3.4 Core Design Principles

- Single source of truth for user progress.
- Optimistic local UX with reliable eventual sync.
- Explicit fallback UX whenever confidence is low.
- Background work never blocks core viewing/reading interactions.

---

## 4. Material 3 Expressive and Adaptive UX System

This app uses Material 3 Expressive (M3E) as a system, not as isolated component swaps.

### 4.1 Theming Model

Two coordinated theme sources are supported:

- Global dynamic system theme (device-driven).
- Content-based accent extraction on media detail and consumption contexts.

Requirements:

- Theme root must support expressive motion tokens (`MotionScheme.expressive`).
- Must never break contrast or semantic role mapping when palette changes.
- Feature UIs must consume semantic tokens; no ad-hoc UI literals.

### 4.2 Color and Surface Role Policy

The app maps actions and states to M3 semantic roles.

| Role | Intended Use |
|---|---|
| Primary | Highest-emphasis play/read actions |
| Primary Container | Active destination, selected control states |
| Secondary | Secondary tracking controls |
| Tertiary | Distinct counters/badges (new, unread, alerts) |
| Surface / Surface Container | Base and card hierarchy |
| Surface Container Low/High | Group containers, sheets, and elevated clusters |
| Error / Error Container | Destructive actions, network/sync failure indicators |

Policy:

- If a composable has interaction state (`selected`, `pressed`, `active`), color must come from `MaterialTheme.colorScheme`.
- Hardcoded `Color(0x...)` is allowed only for content media (poster/artwork), not UI chrome.

### 4.3 Shape Rhythm Policy

Use large, confident, and role-consistent shape tokens:

- Page-level containers: `extraLarge`.
- Row-level containers: `large`.
- Chips/badges/compact controls: `small` to `medium`.
- Hero/contextual action surfaces: `extraLargeIncreased` where available.

Rules:

- Do not mix more than three corner radii on one screen.
- Prefer `MaterialTheme.shapes.*` tokens over inline `RoundedCornerShape(...)`.

### 4.4 Typography and Variable Font Policy

- Use expressive hierarchy for title moments and hero content.
- Keep synopsis/review text in high-readability contrast targets.
- Avoid decorative typography in dense utility screens.
- For selection/active states, prefer variable font axis transitions over static bold overrides.

### 4.5 Motion Policy

- Use tokenized motion from `MaterialTheme.motionScheme`.
- Use spatial specs for layout/position/shape transitions.
- Use effects specs for alpha/color/content transitions.
- Avoid arbitrary timing constants except where no tokenized spec exists.
- All motion must degrade safely on lower-performance devices.

### 4.6 Adaptive Navigation Policy

Compact widths:

- Bottom navigation with up to five top-level destinations.
- Use short bottom bar height (56dp) on phone form factors.

Medium/expanded widths:

- Replace bottom navigation emphasis with a rail-first shell.
- Use list-detail panes to reduce back-stack churn.

Must:

- Keep top-level navigation persistent.
- Avoid route loss when rotating/folding.
- Keep tablet/foldable rail behavior unchanged while phone bar is shortened.

### 4.7 M3E Component Remap Matrix for This App

This remap is the implementation target across Aniyomi-origin and AniHyou-origin surfaces.

| App Surface | Current/Legacy Pattern | M3 Expressive Target |
|---|---|---|
| Library, updates, history list rows | Flat `ListItem` rows or mixed ad-hoc containers | Per-row `Surface` container using `surfaceContainer` + `shapes.large` |
| Overflow actions across screens | Flat `DropdownMenu` grouping | Vertical menu with plain/gap/divider pattern based on action grouping |
| Primary + `MoreVert` action pairs | Separate button plus overflow icon | `SplitButton` when one action is dominant and variants share intent |
| Player and reader contextual controls | Custom overlay bars | `FloatingToolbar` for contextual page-level actions |
| Play/pause and high-salience toggles | Icon swaps only | Shape morph + icon/content transition with motion tokens |
| Loading states | `CircularProgressIndicator` and legacy linear indicators | `ContainedLoadingIndicator` or expressive loading variants |
| Tracking status controls | Isolated buttons/chips with mixed emphasis | `ConnectedButtonGroup` or split-status pattern with semantic containers |
| AniHyou profile/stats emphasis text | Static weight overrides | Variable font axis for state emphasis where supported |

### 4.8 Module-Level Migration Scope

Prioritize migration by module ownership in this repository:

- App shell and shared components: `app/`, `presentation-core/`.
- AniHyou-origin feature surfaces: `AniHyou-android/feature/` and `AniHyou-android/core/ui/`.
- Shared UI primitives and wrappers: `core/ui/`, `presentation-core/`.

Execution rule:

- Migrate wrappers and shared primitives first, then feature callsites, to avoid repetitive rework.

---

## 5. Unified Media Detail Flow

The media detail screen is the convergence point between tracking and consumption.

### 5.1 Screen Composition

1. Hero header: cover/banner, title, status, key metrics.
2. Action row: primary play/read CTA + tracking controls.
3. Segmented content: metadata tab and episodes/chapters tab.

### 5.2 Action Matrix

Primary action behavior is state-aware:

- Resume Episode N
- Start Episode 1
- Resume Chapter N
- Start Chapter 1

Secondary actions:

- Status transition
- Score update
- Custom list assignment

### 5.3 Source Mapping Logic

Source mapping cannot be treated as always-perfect automation.

Policy:

- Use confidence-based auto-linking only above strict threshold.
- Show manual source picker when confidence is ambiguous.
- Persist confirmed mapping locally for future launches.

### 5.4 Mapping Resolution States

| State | UI Behavior | Backend Behavior |
|---|---|---|
| High confidence | Auto-enable play/read CTA | Save mapping silently |
| Medium confidence | Prompt with suggested candidates | Keep temporary candidate set |
| Low confidence | Explicit source selection UI | No auto-linking |
| Failure/timeout | Retry affordance and fallback messaging | Cancel pending source probes |

### 5.5 Timeouts and Cancellation

- Probe sources concurrently with bounded timeout budget.
- Cancel stale probes when user exits screen.
- Never allow source probe failures to block metadata rendering.

---

## 6. Player Architecture

### 6.1 Immersive Overlay Zones

Player UI stays hidden by default and appears on interaction.

- Top zone: back, title context, subtitle/audio controls.
- Middle zone: play/pause and skip controls plus gesture surfaces.
- Bottom zone: scrubber, lock, rotation, speed, PiP.

### 6.2 Gesture Model

- Double-tap left/right for seek jumps.
- Vertical edge gestures for brightness and volume.
- Tap center toggles overlay.

All gestures must remain configurable.

### 6.3 Decoder and Subtitle Controls

Must support:

- HW/SW decode toggle.
- ASS subtitle rendering and typography overrides.
- Display-cutout behavior control.

### 6.4 Telemetry Hooks

Playback telemetry emits:

- start event
- progress threshold event
- completion event

Events are local-first and then queued for remote sync.

---

## 7. Reader Architecture

### 7.1 Reading Modes

- Paged right-to-left (default for manga).
- Paged left-to-right.
- Long-strip mode for webtoon-style content.

### 7.2 Rendering and Display

- Optional high-fidelity image decoding mode.
- OLED-optimized true black background mode.
- Reader HUD with fast access to mode and chapter controls.

### 7.3 Reader Telemetry

Tracking updates must be emitted only when chapter completion confidence is high.

- Trigger is based on final-page transition plus debounce rules.
- Rapid-fling behavior must not mark chapters as completed.

---

## 8. Extension and Repository Platform

### 8.1 Repository UX

Users can add and manage extension repositories from a dedicated browse/settings flow. The UI must clearly indicate trust model and repository provenance.

### 8.2 Security Model

Extensions are untrusted execution surfaces.

Requirements:

- Route extension networking through app-managed client stack.
- Enforce request limits and failure isolation.
- Prevent extension failures from destabilizing app shell.

### 8.3 Operational Model

- Global search fans out to available sources with limits.
- Source health state is cached and surfaced to users.
- Unsupported/failed sources degrade gracefully.

---

## 9. Tracking Telemetry and Offline Sync

### 9.1 Video Progress Policy

Use a configurable completion threshold with default baseline at 85 percent.

Important:

- Threshold is policy, not hardcoded truth.
- Keep user-configurable settings exposed.

### 9.2 Manga Progress Policy

Chapter completion is based on intentional end-of-chapter behavior plus debounce safeguards.

### 9.3 Mutation Queue

When offline or when API fails transiently:

- Serialize sync intents locally.
- Queue for retry via background worker.
- Preserve order and idempotency semantics.

### 9.4 Conflict Resolution

On replay, conflicts can occur (remote edits, deleted entries, status mismatch).

Policy:

- Auto-resolve simple monotonic progress updates.
- Surface conflict UI for non-monotonic or destructive mismatches.
- Log resolution outcome for diagnostics.

### 9.5 AniList Auth Reliability Policy

Operational requirements from existing repo behavior:

- Keep redirect URI and client constants aligned.
- Apply AniList env overrides atomically.
- Use token-flow handling path that is stable for this app context.

---

## 10. Security, Reliability, and Performance Controls

### 10.1 Security Controls

- Treat extension code and feeds as untrusted inputs.
- Validate repository payload formats strictly.
- Avoid direct plugin-controlled socket behavior.

### 10.2 Reliability Controls

- Isolate source probe failures.
- Isolate telemetry sync retries from playback loop.
- Preserve local progress even when remote sync is unavailable.

### 10.3 Performance Controls

- Cache content-derived color results by media ID.
- Bound concurrent network fan-out and use cancellation aggressively.
- Keep heavy processing off main thread.

### 10.4 Observability Controls

- Structured logs for source mapping decisions.
- Telemetry queue metrics (enqueued, replayed, failed).
- Auth flow diagnostics with redacted sensitive data.

---

## 11. Implementation Roadmap (Refined)

This roadmap is stronger than the previous narrative plan because it adds phase gates, dependency edges, and acceptance exits.

### 11.1 Workstreams

- A: Auth and tracking data integrity
- B: Unified detail and source mapping UX
- C: Telemetry and offline sync reliability
- D: Adaptive shell and design-system polish

### 11.2 Phases

#### Phase 1: Foundation and Auth Hardening

Goals:

- Stabilize AniList auth/session behavior.
- Finalize token and callback handling policy.
- Validate end-to-end list fetch/update basics.

Exit criteria:

- Login reliability above target threshold in repeated test cycles.
- Track list fetch and status mutation pass core tests.

#### Phase 2: Metadata and Unified Detail Skeleton

Goals:

- Render AniList metadata in integrated detail screen.
- Add primary action matrix with tracking controls.

Exit criteria:

- Detail screen supports both anime and manga contexts.
- No blocking regressions in navigation and screen recovery.

#### Phase 3: Source Mapping Engine and Fallback UX

Goals:

- Implement confidence-scored source matching.
- Add manual source selection fallback.

Exit criteria:

- High-confidence items auto-link correctly.
- Ambiguous items always surface safe manual flow.

#### Phase 4: Player and Reader Telemetry Integration

Goals:

- Emit and queue playback/reading telemetry.
- Apply threshold/debounce policy.

Exit criteria:

- Episode/chapter progress updates are correct in online and offline sessions.
- No false completion spikes in stress tests.

#### Phase 5: Offline Replay, Conflict UX, and Worker Stability

Goals:

- Build robust replay queue with idempotency and conflict handling.
- Add user-visible conflict resolution path.

Exit criteria:

- Replay success target met under intermittent-network test matrix.
- Conflict outcomes are deterministic and auditable.

#### Phase 6: Adaptive UX Refinement and Performance Pass

Goals:

- Finalize adaptive layouts for compact/medium/expanded classes.
- Add color-cache and source-fanout performance optimizations.

Exit criteria:

- Smoothness and memory targets met across test device tiers.
- UX parity across phone and tablet classes.

### 11.3 Dependency Map

- Phase 1 is prerequisite for Phases 2 to 5.
- Phase 2 is prerequisite for Phase 3 UX rollout.
- Phase 4 depends on Phase 3 mapping readiness.
- Phase 5 depends on Phase 4 telemetry event integrity.
- Phase 6 can run partially in parallel with Phases 4 and 5.

### 11.4 Rollback Strategy

For each phase, keep feature flags at integration boundaries:

- Unified detail action matrix flag
- Auto-mapping flag
- Auto-sync telemetry flag
- Conflict auto-resolution flag

This supports controlled rollout and safe disablement.

---

## 12. Testing and Acceptance Matrix

### 12.1 Auth and Session

- Login, relogin, token-refresh/recovery behavior.
- Callback parsing and redirect correctness.
- Invalid config detection and user-safe error messaging.

### 12.2 Source Mapping

- Precision/recall validation on curated title set.
- Romanization and alias edge-case coverage.
- Timeouts, cancellation, and stale-result handling.

### 12.3 Player Telemetry

- Threshold trigger correctness with seek/jump patterns.
- Skip intro/outro scenarios.
- Offline queue and replay behavior.

### 12.4 Reader Telemetry

- End-of-chapter confirmation logic.
- Fling/rapid-swipe false-positive protection.
- Multi-chapter session correctness.

### 12.5 Adaptive UX

- Compact/medium/expanded behavior validation.
- Rotation/fold transitions preserve route and state.
- Accessibility checks for contrast and touch target sizes.

### 12.6 Reliability

- Network drop/recovery runs.
- Source failure isolation tests.
- Queue idempotency and conflict replay tests.

---

## 13. Known Risks and Mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Over-aggressive auto-mapping | Wrong episode source links | Strict threshold + manual fallback |
| Sync drift during offline periods | Incorrect AniList state | Durable queue + conflict UI |
| Extension instability | UX interruptions | Isolation and request controls |
| Palette extraction overhead | Jank/memory spikes | Cache by media ID + bounded processing |
| Navigation divergence by size class | User confusion | Shared route contracts across layouts |

---

## 14. Glossary

- AniList tracking: remote list, status, score, and progress state.
- Source mapping: linking one AniList media item to one extension result set.
- Confidence threshold: score cutoff for safe auto-linking.
- Telemetry queue: local buffer of sync intents to replay later.
- Conflict resolution: policy for handling remote and local progress divergence.
- Adaptive shell: layout/navigation behavior that changes by window size.

---

## Related Documents

- `md files/port_anihyou_blueprint.md`
- `md files/refresh.md`
- `README.md`

These documents remain complementary. This file is the canonical architecture and delivery specification for the unified integration.
