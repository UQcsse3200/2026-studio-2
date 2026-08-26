# Player-Item Interaction — Backend Testing

**Test class:** `source/core/src/test/com/csse3200/game/components/player/PlayerInteractionComponentTest.java`
**Framework:** JUnit 5 + Mockito, run via `GameExtension` (headless libGDX)

## Running the tests

```
cd source
./gradlew :core:test --tests "com.csse3200.game.components.player.PlayerInteractionComponentTest"
```

Requires a JDK 21 toolchain. If Gradle reports "No matching toolchains
found", point it at a local JDK 21 install with
`-Porg.gradle.java.installations.paths=<path-to-jdk21>` (this repo's
`settings.gradle` has no auto-download resolver configured).

## Test setup

Each test registers `PhysicsService`, `EntityService`, and `RenderService`
(item entities carry `PhysicsComponent`/`HitboxComponent`/render components
that need them to `create()`), plus a mocked `ResourceService` that returns a
mocked `Texture` — this avoids depending on real asset/texture loading in a
headless test environment.

## Coverage

| Test | Verifies |
|------|----------|
| `shouldFindItemInRange` | Nearest item within range is detected |
| `shouldNotFindItemOutOfRange` | Item beyond `INTERACTION_RANGE` is not detected |
| `shouldIgnoreNonItemEntities` | Entities without `ItemComponent` are never returned as interactable |
| `shouldPickUpItemInRange` | Pickup adds the correct item type/quantity to the inventory |
| `shouldTriggerItemPickedUpEvent` | Successful pickup fires `itemPickedUp` |
| `shouldRejectPickupWhenOutOfRange` | Pickup on an out-of-range target fails and fires `interactionFailed` |
| `shouldRejectPickupWhenInventoryFull` | Pickup against a full inventory fails and fires `itemPickupBlocked` |
| `shouldRejectPickupOfNullEntity` | Pickup with no target returns `false` without throwing |
| `shouldDropSelectedItem` | Drop removes the selected stack from the inventory |
| `shouldRejectDropWhenInventoryEmpty` | Drop with nothing selected returns `false` |
| `shouldDeleteSelectedItem` | Delete removes the selected stack from the inventory |
| `shouldRejectDeleteWhenInventoryEmpty` | Delete with nothing selected returns `false` |
| `shouldSwitchSelectedItem` | Switch moves selection forward and back between owned items |

13 tests total, all passing. Full `:core:test` suite (100+ tests across the
project) also passes with these changes — no regressions.

## Known gaps

- Not yet exercised in a running instance of the game (manual playtest with
  `E`/`Q`/`X`/`,`/`.` against the items spawned in `ForestGameArea`).
- No test spawns two items at equal distance to check tie-breaking in
  `findNearestItem` (first found wins; not asserted).
