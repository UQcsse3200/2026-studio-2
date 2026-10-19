# Arrow Selection Wheel

## Introduction

The arrow selection wheel lets the player switch between arrow types without leaving combat. Holding `Tab` opens a radial menu, moving the mouse points at a wedge, and letting go of `Tab` picks it. The wheel then selects the inventory slot holding that arrow, so the bow fires it from then on.

This is the backend half of the feature. It owns the state, input handling and validation, and exposes that through a small API and a set of events. Drawing the wheel is UI work and is not covered here, but the last section explains what the UI needs to hook into.

## Controls

| Input | Action |
|---|---|
| Hold `Tab` (bow equipped) | Open the wheel |
| Move mouse | Highlight the arrow type under the cursor |
| Release `Tab` | Select the highlighted type and close the wheel |
| Release `Tab` over the centre | Cancel, keep the current type |

While the wheel is open, attack input is ignored so the player can't fire an arrow mid-selection.

## Architecture

There is one new class, `ArrowWheelComponent`, plus additions to `ArrowType` and `KeyboardPlayerInputComponent`. The input component sends events to the wheel, the wheel works out what was picked and selects the matching inventory slot, and the existing item-use path fires it.

### `ArrowType`

The shared arrow enum in `components.projectile`, used by the bow, projectiles and the wheel. The wheel offers four of its values: `STANDARD`, `FIRE`, `ICE`, `POISON`. `GRAPPLE` is also an `ArrowType` but is fired through the rope arrow item, not the wheel, so it is left off.

The enum also does the direction maths. `forDirection(Vector2 offsetFromCentre)` takes the mouse position relative to the wheel's centre and returns the type whose wedge it lands in:

```java
float wedgeDegrees = 360f / WHEEL_TYPES.length;
float clockwiseFromTop = (90f - offsetFromCentre.angleDeg() + 360f) % 360f;
return WHEEL_TYPES[Math.round(clockwiseFromTop / wedgeDegrees) % WHEEL_TYPES.length];
```

Wedges are shared evenly between the entries in `WHEEL_TYPES`, clockwise from the top, so `STANDARD` is up, `FIRE` is right, `ICE` is down and `POISON` is left. Adding a type to the wheel is just adding it to that array; the wedge count follows its length so nothing else changes.

If the mouse is within `DEADZONE_RADIUS` (40px) of the centre, `forDirection` returns `null`. This stops a tiny mouse twitch from picking a type the player didn't mean to, and gives a way to cancel without selecting.

### `ArrowWheelComponent`

Sits on the player entity and holds the wheel's state: whether it's open, which type is highlighted, which type is selected, and which types are unlocked.

Arrows are inventory items (`STANDARD_ARROW`, `FIRE_ARROW`, `ICE_ARROW`) with ammo counts, and `ItemUseComponent` already fires whatever is in the selected inventory slot and consumes one. So rather than telling the bow directly, the wheel selects the inventory slot holding the chosen arrow and lets the normal item path do the firing. This keeps one source of truth for which arrow is fired and means ammo is always consumed correctly.

Because of that, a type is only available if the player actually has arrows of that type. `isAvailable()` checks the inventory, so a wedge for arrows the player has run out of is shown locked and can't be picked. `POISON` has no arrow item yet, so it stays locked. If the player has no inventory at all (as in some tests) every type is treated as available.

It listens for four events:

- `openArrowWheel` – opens the wheel if a bow is equipped and it isn't already open
- `closeArrowWheel` – closes the wheel and applies whatever is highlighted
- `arrowWheelPointerMoved` – updates the highlight from a `Vector2` offset
- `bowEquipped` – tracks whether the player still has a bow; losing it mid-selection cancels the wheel

And fires five for anything that wants to react:

- `arrowWheelOpened` / `arrowWheelClosed`
- `arrowHighlighted` with the `ArrowType` now under the cursor
- `arrowSelected` with the confirmed `ArrowType`
- `arrowSelectionRejected` with the `ArrowType` that was refused

The state can also be read directly with `isOpen()`, `getHighlighted()`, `getSelected()` and `isAvailable(type)`. `setAvailable(type, unlocked)` locks or unlocks a type on top of the inventory check, for things like progression gating; a locked type still shows on the wheel but can't be picked.

### `KeyboardPlayerInputComponent`

`Tab` down triggers `openArrowWheel` and `Tab` up triggers `closeArrowWheel`. While the wheel is open, mouse movement is converted to an offset from the screen centre (with y flipped so up is positive) and sent as `arrowWheelPointerMoved`. The wheel is assumed to be drawn at the centre of the screen.

The input component checks `isOpen()` before handling attack input and skips it while the wheel is up.

## Selection Lifecycle

1. Player presses `Tab`. If a bow is equipped the wheel opens and `arrowWheelOpened` fires. If not, nothing happens.
2. Player moves the mouse. Each time the cursor crosses into a different wedge, `arrowHighlighted` fires with the new type. Moving back into the deadzone clears the highlight silently.
3. Player releases `Tab`. `arrowWheelClosed` fires, then one of:
   - Nothing highlighted: the previous type stays selected, no further event.
   - Highlighted type is locked, the player has no arrows of that type, or the bow was lost: `arrowSelectionRejected` fires with that type. The previous type stays selected.
   - Highlighted type is available: the inventory slot holding that arrow is selected, then `arrowSelected` fires.

Losing the bow while the wheel is open closes it straight away without selecting anything.

`arrowWheelClosed` always fires before `arrowSelected` or `arrowSelectionRejected`, so the UI can hide the wheel first and then play feedback.

## Hooking up the UI

The UI needs to:

1. Listen for `arrowWheelOpened` and `arrowWheelClosed` to show and hide the wheel.
2. On each frame while open, read `getHighlighted()`, `getSelected()` and `isAvailable(type)` to draw each wedge. Or listen for `arrowHighlighted` and only redraw when it changes.
3. Listen for `arrowSelectionRejected` to play a "locked" or "no ammo" effect.
4. Draw the centre deadzone using `ArrowType.DEADZONE_RADIUS` so what the player sees matches what the code detects.

For example:

```java
ArrowWheelComponent wheel = player.getComponent(ArrowWheelComponent.class);
player.getEvents().addListener("arrowWheelOpened", this::show);
player.getEvents().addListener("arrowWheelClosed", this::hide);
player.getEvents().addListener("arrowHighlighted", (ArrowType type) -> highlight(type));
player.getEvents().addListener("arrowSelectionRejected", (ArrowType type) -> shake(type));
```

## Testing

Automated tests cover:

- Direction to type in all four directions, and directions between wedge centres
- The centre deadzone and a null direction
- The wheel only opening when a bow is equipped
- Opening twice being a no-op
- Highlighting as the pointer moves between wedges
- Selecting on release moves the inventory selection to that arrow's slot
- Types the player has no arrows for are unavailable and get rejected
- Rejecting a locked type and keeping the old one
- Each wheel type mapping to the right inventory item
- Cancelling from the deadzone
- Closing when the bow is lost
- `Tab` opening and closing through the input component
- Attack input being blocked while the wheel is open

Relevant test classes:

- `ArrowTypeTest`
- `ArrowWheelComponentTest`
- `KeyboardPlayerInputComponentTest`

The wheel has no rendering yet, so there's nothing to verify visually. The state can be checked from the debug terminal or by adding a temporary log to the event listeners.

## Known Limitations

Poison arrows come from the `PoisonBuff` (via `PoisonPotion`) rather than from an arrow item. `ArrowType.POISON` has no backing `ItemType`, so the wheel always shows it locked. Once a poison arrow item exists it will work with no changes to the wheel.

Nothing calls `setAvailable` yet, so the only gating is "do you have the arrows". Progression-based unlocks are future work.

If the same arrow type sits in more than one inventory slot, the wheel picks the first one.

The input component assumes the wheel is drawn at the screen centre. If the UI puts it somewhere else, the offset calculation needs updating.

## Relevant Development Links

- [Advanced Inventory System #93](https://github.com/UQcsse3200/2026-studio-2/issues/93)
- [Arrow selection wheel PR #101](https://github.com/UQcsse3200/2026-studio-2/pull/101)
- [Wheel selects the inventory slot PR #158](https://github.com/UQcsse3200/2026-studio-2/pull/158)

## Contribution Attribution

The arrow selection wheel, direction mapping, input wiring, inventory linking and associated tests were implemented by Aayush Singh Parmar (AayushSinghParmar). The shared `ArrowType` enum and `BowComponent` refactor it sits on are from Darcy Franklin's weapon refactor, and the inventory and item-use path it selects into is from Riki's advanced items work.
