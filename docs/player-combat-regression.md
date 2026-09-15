# Player combat regression testing

## Revision and environment

- Automated checks: 14 September 2026; user playtest follow-up: 15 September 2026.
- Branch: `test/player-combat-regression`.
- Baseline: `9fa2e67b763ba3f5b70b9551c616bc04e6e31603` (upstream main).
- Tested code: `a4af3e9ed7d4322e422961c8092959e525fe9b6a`; the subsequent documentation commit does not change executable code.
- macOS 26.6.2 (25G83), Apple Silicon; Homebrew OpenJDK 21.0.12.1; Gradle wrapper 8.5.
- JUnit 5.9.3 and Mockito 5.2.0, using the existing `GameExtension`.

## Change and regression evidence

`ItemUseComponent` now checks that an equipped primary weapon exists and is ready before dispatching a consumable-arrow attack. Rejection preserves ammo, returns false, emits `itemUseFailed`, and emits neither `primaryAttack` nor `itemUsed`. Successful shots retain the existing synchronous event flow. Weapon interfaces, cooldown values, grapple routing and potion logic are unchanged.

Before the fix, `shouldPreserveAmmoAndRejectSecondShotDuringCooldown` was run against commit `fd64fdd`. Starting with three arrows, it fired once and immediately tried again. The inventory assertion failed: **expected 2, actual 1**. Command exit status was 1. This is an automated reproduction, not an in-game observation.

The fix is in `26f0cec`. The same regression passes afterward: one projectile is registered, two arrows remain, the second attempt returns false and emits failure without another attack or success event.

## Automated results

| Check | Result |
| --- | --- |
| Baseline bow, weapon, item-use and keyboard-input tests | 35 passed |
| Commit 1: baseline combat tests plus initial integration tests | 39 passed |
| Commit 2: affected combat tests including rejection regressions | 44 passed |
| Commit 3: integration, item-use and keyboard-input tests | 40 passed |
| Full `./gradlew test` | 398 passed, 0 failures, 0 errors, 0 skipped |
| `./gradlew formatCheck` | Passed |
| `./gradlew javadoc` | Passed; 100 core and 2 desktop documentation warnings reported |

The final three checks were run together as `./gradlew test formatCheck javadoc --console=plain`, with Java 21. No unrelated test failures were encountered. Existing Sonar Gradle deprecation and JVM instrumentation notices remain. Documentation warnings concern existing source declarations; no unrelated cleanup was included.

`PlayerCombatIntegrationTest` contains 14 tests. Inventory, item use, weapon coordination, bow cooldown and keyboard processing are real. Mockito supplies time, camera/input values, resources, sound, event observers and entity registration. Scoped static factory mocks return mock projectiles, so these tests do not verify Box2D collisions or visual rendering. Static mocks close and global services/input are cleaned up after each test.

Coverage includes successful shots, empty inventory, last-arrow consumption, normalized aim, standard/fire/cold factory selection, rejected shots, cooldown expiry, arrow switching, missing weapons/coordinator, one attempt per E press, cooldown shared between E and right-click in both directions, rope-arrow routing/release without consumption, and potion use/full-health rejection.

Re-run from `source` with Java 21:

```bash
./gradlew :core:test --tests '*PlayerCombatIntegrationTest' --tests '*ItemUseComponentTest' --tests '*KeyboardPlayerInputComponentTest'
./gradlew test
./gradlew formatCheck
./gradlew javadoc
./gradlew desktop:run
```

## Desktop run and manual checklist

The desktop process was launched from the tested revision. Its log records main-menu startup, a Play event, tutorial initialisation, a LOSE event and exit via the game-end screen. The process completed normally. These are log observations only: the desktop-control tool did not expose the Java window, so visual gameplay interactions could not be independently verified.

On 15 September, Dhiren manually tested the same code on `test/player-combat-regression`. He confirmed the ammunition/count checks and inventory, then confirmed grappling works when fired while jumping. After a further launch, he reported that all remaining requested checks worked: arrow collisions/damage and skeleton death, potions, player death, pause/resume, and movement/sprint/jump. Results below are user-reported, not independent desktop-tool observations or frame-by-frame measurements. The three additional source-only findings below were not explicitly reproduced in this follow-up.

The log also reports an asset dependency error for `images/DevGridTile.png`. The baseline tutorial lists that asset but the baseline asset tree does not contain it. Loading continues in the log; its visible impact is unverified. A missing local settings-file message and an LWJGL unsupported-JNI warning also occurred. None was changed in this work.

| Scenario | Repeatable steps and expected result | Manual result |
| --- | --- | --- |
| Movement | In the tutorial, hold A/D and arrow keys; release each. Check movement, facing and stopping. | User-reported pass |
| Sprint | Compare travel over the same section using D versus Shift+D. Sprint should be faster. | User-reported pass |
| Jump | Press Space while grounded. Check a single jump and return to the floor. | User-reported pass |
| Grapple | Select a rope arrow, jump first, hold right-click toward an overhead platform and use A/D to swing. Release or press Space to detach. | Airborne attachment/swing passed per user; release variants and rope quantity not separately confirmed. Ground attachment does not reel the player upward; rope length is fixed. |
| Aiming | Pick up standard arrows, aim to either side and above, and fire. Check initial direction and subsequent arc. | Unverified |
| Projectile collisions | Shoot a skeleton and then terrain. Check direct damage, one hit per arrow and projectile removal. | User-reported pass for collisions, damage and removal; per-projectile hit count not separately measured |
| Enemy death | Kill a skeleton and check death/removal. | User-reported pass |
| Ammo/cooldown | Start with at least three arrows; attempt E then right-click within 0.4 s. Expect one projectile and one arrow consumed. Wait and fire again. | User confirmed ammo checklist/count correct; automated regression passed |
| Inventory | Use F near a pickup, B for backpack and number keys to select items. Check selection and displayed quantities. | User-reported pass |
| Potion | At reduced health, select a potion and press E. Check healing/consumption; at full health repeat and confirm no consumption. | User-reported pass; automated regression passed |
| Damage | Allow a skeleton to hit the player. Check health reduction, hurt feedback and the invulnerability window. | Player damage/death reported working; hurt timing and invulnerability window not separately verified |
| Death | Allow health to reach zero. Check death animation followed by the loss screen and disabled gameplay input. | User-reported pass for player death/loss screen; animation sequence and post-death input not separately confirmed |
| Pause/resume | Pause while moving and during combat; wait, then resume. Check frozen simulation, animations and correct resumed input. | User-reported pass |

## Additional findings: not fixed here

These findings were traced in the tested source. Their in-game reproduction steps remain unverified.

1. **Melee can hit through terrain:** `MeleeComponent.attack` raycasts only against `PhysicsLayer.NPC`. A wall between the player and an enemy within the one-unit reach cannot occlude that ray. Reproduce with a thin solid barrier and an enemy immediately behind it, then aim and left-click. Expected: the barrier blocks the hit.
2. **Movement can interrupt hurt feedback:** `PlayerAnimationController.hurt` starts the hurt animation and sets `hurt`; movement handlers guard `jumping` but not `hurt`. Movement can replace hurt with a looping animation, leaving the hurt flag uncleared until another finite animation completes. Reproduce by taking damage and immediately pressing or releasing A/D. Expected: hurt completes before returning to locomotion.
3. **Elemental effects have no receivers:** fire/cold arrows emit `applyFire`, `burn`, `applyCold` and `slow`. Searching the main Java source finds the emitters but no corresponding listeners. Direct impact damage is separate. Reproduce by shooting an isolated skeleton once and observing subsequent health/movement without additional hits. Expected: the documented burn/slow effect is applied for its duration.

## Local review materials

The review directory `/private/tmp/csse3200-combat-review-20260914/` contains the full automated-check log, desktop log, bug-report drafts, proposed wiki page and PR-description draft. These have not been published. The wiki proposal distinguishes reviewed main-branch behaviour, this local fix and unmerged charged-bow/dash work.
