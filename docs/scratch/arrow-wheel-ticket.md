# **Description**
**Task:** Arrow Selection Wheel — detection and validation.
**Feature:** Advanced Inventory System (#93)

Implement the backend for a radial arrow wheel that lets the player switch arrow
types while holding the bow. A new `ArrowWheelComponent` tracks whether the wheel
is open, which arrow type the mouse is pointing at, and which type the bow
currently fires. A new `ArrowType` enum holds the four types and maps a pointer
direction to the wedge it lands on. The player holds `Tab` to open the wheel,
moves the mouse to highlight a type, and releases `Tab` to confirm it. Wedges are
shared evenly between the enum constants, so adding a fifth arrow type needs no
change to the selection logic. This ticket covers state, input handling,
validation and events only — the UI team owns drawing the wheel, reading state
through the API below and reacting to its events.

## **Example**
- **Opening**: Player holds `Tab` while a bow is equipped. `arrowWheelOpened`
  fires and the wheel appears.
- **Highlighting**: Player moves the mouse right. The component maps the
  direction to `FIRE` and fires `arrowHighlighted`. The UI highlights that wedge.
- **Selecting**: Player releases `Tab`. `arrowSelected` fires with `FIRE`, the
  wheel closes, and the bow fires fire arrows from then on.
- **Locked type**: If `POISON` is not unlocked it cannot be selected. The
  component fires `arrowSelectionRejected` and the previous type stays active.
- **Neutral direction**: If the mouse sits in the centre deadzone nothing is
  highlighted, and releasing `Tab` simply cancels the interaction.

### API for the UI team
| Method / Event | Purpose |
|---|---|
| `isOpen()` | Whether the wheel should currently be drawn |
| `getHighlighted()` | Arrow type currently under the mouse, or null |
| `getSelected()` | Arrow type the bow currently fires |
| `isAvailable(ArrowType)` | Whether a wedge should be drawn as unlocked |
| `arrowWheelOpened` / `arrowWheelClosed` | Show and hide the wheel |
| `arrowHighlighted` | Move the highlight to a new wedge |
| `arrowSelected` | Selection confirmed |
| `arrowSelectionRejected` | Locked type chosen; play the failure feedback |

# **Dependencies**
- [x] `BowComponent` exists on the team branch and can receive the selected type.
- [x] `Item` and `ItemType` are on `main` via #55.
- [x] Poison arrows exist, but as a buff rather than an item type. Riki's
      `feat/advanced-items` adds `PoisonPotion`, `PoisonBuff` and poison
      parameters on `ArrowProjectileComponent`: while the buff is active the bow
      fires poison arrows. `ItemType` still has only `ARROW`, `RopeArrow`,
      `CONSUMABLE`, `FireArrow` and `ColdArrow`, so `ArrowType.POISON` has no
      backing item and nothing maps the wheel's choice onto the buff.
- [ ] Open question: `BowComponent` tracks the selected `ArrowType` and triggers
      `arrowFired` with it, but nothing listens to that event and `attack()`
      does not use the type to change the projectile. Poison comes only from the
      potion buff, and `FIRE` and `COLD` have no effect at all. Whether wiring
      the selected type into projectile behaviour belongs to this ticket or a
      follow-up needs to be agreed with the team.
- [x] Naming agreed: the wheel uses `COLD` to match the existing `ColdArrow`
      item, rather than introducing a second name for the same thing.
- [ ] API and event names above need to be agreed with the UI team so both sides
      can work in parallel.

# **Milestones**
- [x] `ArrowType` created with the four types and direction resolution (Sep. 6)
- [x] `ArrowWheelComponent` created with open / highlight / select state (Sep. 7)
- [x] Selection validation: bow equipped, type highlighted, type available (Sep. 7)
- [x] `Tab` binding and mouse-direction highlighting wired through
      `KeyboardPlayerInputComponent` (Sep. 8)
- [x] Weapon input blocked while the wheel is open (Sep. 8)
- [x] `BowComponent` receives and fires the selected arrow type (Sep. 8)
- [x] Unit tests for direction mapping, deadzone, rejection and open/close state (Sep. 8)
- [ ] Feature documented on the wiki
- [ ] Handover to the UI team

**Completion Deadline:** Sep. 7

# **Testing**
24 unit tests, all passing with `./gradlew :core:test`.

| Test class | Covers |
|---|---|
| `ArrowTypeTest` | Direction to type in all four directions, directions between wedge centres, the centre deadzone, a null direction |
| `ArrowWheelComponentTest` | Opening only with a bow, repeated opens, highlighting, selecting on release, rejecting locked types, cancelling from the centre, closing when the bow is lost |
| `BowComponentTest` | The bow fires the type chosen on the wheel |
| `KeyboardPlayerInputComponentTest` | `Tab` opens and closes, pointer offset highlights, weapon input blocked while open |

# **Documentation**
- [Main description of feature](../wiki/Arrow-Selection-Wheel) (to be written)
- [JavaDoc](JavaDoc/com/csse3200/game/components/player/ArrowWheelComponent)
- Merged in PR #101

# **Member**
- Aayush Singh Parmar (@AayushSinghParmar)
