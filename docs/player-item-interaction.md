# Player-Item Interaction

**Feature:** Inventory and Items
**Class:** `com.csse3200.game.components.player.PlayerInteractionComponent`
**Tests:** `com.csse3200.game.components.player.PlayerInteractionComponentTest`

Backend logic that detects items within the player's interaction range and
validates and performs pickup, drop, delete and switch interactions. This
component owns detection and validation only — item storage, capacity, and
selection are owned by `InventoryComponent`.

## Interaction flow

1. **Detect items in range** — `findNearestItem()` scans all registered
   entities, keeps the ones carrying an `ItemComponent`, and returns the
   closest one within `INTERACTION_RANGE` (1.5 metres), or `null` if none
   qualify.
2. **Identify interactability** — an entity is interactable if and only if it
   has an `ItemComponent`. Everything else is ignored.
3. **Trigger interaction** — the player triggers `interact()` via keyboard
   input, which looks up the nearest item and attempts to pick it up.
4. **Pick up / drop / delete / switch** — four independent actions, each
   exposed as its own method and wired to its own keyboard-triggered event.
5. **Re-validate range at interaction time** — `pickup()` re-checks
   `isInRange()` on the specific target entity before acting, since the
   player or item may have moved since detection.
6. **Re-validate availability** — `pickup()` re-checks that the target still
   has an `ItemComponent` and that its `Item` is non-null before granting it.
7. **Reject invalid interactions** — every action returns `false` and fires
   `interactionFailed` (or `itemPickupBlocked` for a full inventory) instead
   of throwing. Null targets, out-of-range targets, non-item entities, and a
   full inventory are all handled without exceptions.
8. **Mark success** — an action returns `true` and fires its success event
   only once every check above has passed.

## Keyboard bindings

Wired in `KeyboardPlayerInputComponent`:

| Key | Event fired | Action |
|-----|-------------|--------|
| `E` | `interact` | Find nearest item in range and pick it up |
| `Q` | `dropItem` | Drop the currently selected item stack into the world |
| `X` | `deleteItem` | Permanently delete the currently selected item stack |
| `.` | `switchItem` (`+1`) | Select the next owned item |
| `,` | `switchItem` (`-1`) | Select the previous owned item |

## Events

Fired on the player entity's `EventHandler` so UI/audio/etc. can react
without coupling to this class.

| Event | Payload | Fired when |
|-------|---------|------------|
| `itemPickedUp` | `Item` | Pickup succeeded |
| `itemPickupBlocked` | `Item` (may be `null`) | Item found but inventory rejected it (e.g. full) |
| `itemDropped` | `ItemType` | Drop succeeded |
| `itemDeleted` | `ItemType` | Delete succeeded |
| `interactionFailed` | none | Any other invalid interaction (out of range, no item, nothing selected) |

## Design notes

- **Detection is O(n) over all registered entities** (`EntityService.getEntities()`).
  This is deliberate: entity counts in this game are small, so a linear scan
  is simpler and cheaper to maintain than a spatial index.
- **Dropped items are recreated via `ItemFactory`** from `(ItemType, quantity)`
  rather than round-tripping the original `Item` object, since
  `InventoryComponent` only stores type + count, not the object itself.
- Core methods (`pickup`, `dropItem`, `deleteItem`, `switchItem`, `isInRange`,
  `findNearestItem`) are package-private rather than `private` so tests can
  exercise them directly, matching the existing convention in
  `PlayerActions`.

## Dependency

This component requires an `InventoryComponent` on the same entity, and the
world-item classes (`Item`, `ItemComponent`, `ItemType`, `ItemFactory`) from
the `feat/item&inventory` branch. It does not exist on `main` until that
branch is merged.
