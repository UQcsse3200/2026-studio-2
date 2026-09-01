# Radial Selection Wheel

**Feature:** Inventory and Items
**Class:** `com.csse3200.game.components.player.SelectionWheelComponent`
**Supporting enums:** `WheelSlot`, `WheelType`
**Tests:** `SelectionWheelComponentTest`, `KeyboardPlayerInputComponentTest`

Backend for the player's radial selection wheels. Holds four category slots,
tracks whether a wheel is open and which category is highlighted, and equips
the highlighted category when the wheel closes. Drawing the wheel belongs to
the UI, which reads the API below and reacts to the events.

The equipped loadout is deliberately separate from the backpack grid, so this
never touches `InventoryComponent`. Opening a wheel does not slow or pause the
game.

## Slots and layout

`WheelSlot` names both the category and where its wedge sits, so the backend
and the UI agree on the layout without passing angles around:

| Slot | Wedge |
|---|---|
| `HEAVY` | Top |
| `MELEE` | Right |
| `SIDE` | Bottom |
| `LIGHT` | Left |

## Controls

| Key | Action |
|---|---|
| Hold `Tab` | Open the weapon wheel |
| Hold `C` | Open the consumable wheel |
| Move mouse | Highlight the category under the pointer |
| Release the key | Equip the highlighted category and close |

Both wheels are driven by one `SelectionWheelComponent`, because an entity can
only carry a single component of a class. `WheelType` selects which set of
slots an open wheel reads from, and each wheel keeps its own contents and its
own equipped slot.

## Interaction flow

1. **Open** — `Tab`/`C` key-down fires `openWheel` with a `WheelType`. The
   component opens that wheel, clears any previous highlight and fires
   `wheelOpened`. Opening a wheel that is already open is ignored, so key
   repeat while holding does not restart the selection.
2. **Highlight** — mouse movement fires `wheelPointerMoved` with the pointer's
   offset from the centre of the screen. The component maps that direction to a
   wedge and fires `slotHighlighted` when the wedge changes. A pointer inside
   the centre deadzone (`DEADZONE_RADIUS`, 40px) highlights nothing, which stops
   the highlight flickering near the middle.
3. **Close** — key-up fires `closeWheel`. The wheel hides and fires
   `wheelClosed`, then resolves the selection:
   - Highlighted category holds an item → it becomes equipped and
     `weaponEquipped` fires.
   - Highlighted category is empty → `selectionRejected` fires and the
     previously equipped category stays.
   - Nothing was highlighted → the wheel simply closes.

## API for the UI team

| Method / Event | Purpose |
|---|---|
| `isOpen()` | Whether the wheel should currently be drawn |
| `getActiveWheel()` | Which wheel is open (`WEAPON` or `CONSUMABLE`) |
| `getHighlightedSlot()` | Category currently under the pointer, or null |
| `getEquippedSlot()` | Category currently equipped on the active wheel |
| `getItemInSlot(WheelSlot)` | What each wedge should show (may be null) |
| `wheelOpened` (`WheelType`) | Show the wheel |
| `wheelClosed` | Hide the wheel |
| `slotHighlighted` (`WheelSlot`) | Move the highlight to a new wedge |
| `weaponEquipped` (`WheelSlot`) | Selection confirmed |
| `selectionRejected` (`WheelSlot`) | Empty category chosen; play failure feedback |

## Filling the wheel

`setItemInSlot(WheelType, WheelSlot, Item)` assigns an item to a category, or
clears it when passed null. Clearing a category that is currently equipped also
unequips it.

**Weapon item types do not exist yet.** `ItemType` currently only has `ARROW`,
`RopeArrow` and `CONSUMABLE`, and weapons are still being built on the
`ranged-attack` and `melee-attack` branches. Until those land there is nothing
to assign to the weapon categories, so the weapon wheel stays empty and every
selection on it is rejected — which is correct behaviour, not a bug. Wiring the
weapons in is a matter of calling `setItemInSlot` once the types exist.

## Testing

```
cd source
./gradlew :core:test --tests "com.csse3200.game.components.player.SelectionWheelComponentTest"
```

Requires a JDK 21 toolchain. If Gradle reports "No matching toolchains found",
pass `-Porg.gradle.java.installations.paths=<path-to-jdk21>`; this repo's
`settings.gradle` has no auto-download resolver configured.

22 tests cover open/close state, repeated open and close, highlight changes and
repeats, pointer-direction mapping for all four wedges, the centre deadzone,
equipping, empty-slot rejection with the previous selection preserved, closing
with nothing highlighted, wheel independence, clearing a slot, null arguments,
and the full path driven through entity events. Three further tests in
`KeyboardPlayerInputComponentTest` cover the `Tab` and `C` bindings.

### Known gaps

- Not yet exercised in a running instance of the game; the UI that draws the
  wheel does not exist yet.
- `mouseMoved` measures the pointer against the centre of the screen. If the UI
  ends up drawing the wheel somewhere other than screen centre, that origin
  needs to move with it.
