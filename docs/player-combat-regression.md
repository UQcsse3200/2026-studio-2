# Player combat regression testing

## Current integration status

The original regression work below has been integrated locally with PR #145, **Player Mechanics - Dash, Melee, and Bow Mechanics**. The integration branch is `test/player-submission-combat-regression`, based on Tristan's submission head `11b961d12b86cb6382c8d2209488ce0aa797c8b2`. Merge commit `0c08a7fbd3c2cea8b44d3401767deef1955d39d3` preserves all four original commits and Tristan's history. The original `test/player-combat-regression` branch remains at `70545fc`.

The integrated code has separate automated results below. The earlier user playtest applies to the original branch only, not the integrated charged-bow build. Nothing has been pushed or published, and delivery is intended through the existing PR #145 rather than a new PR.

## Original revision and environment

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

## Original automated results

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

## Original desktop run and manual checklist

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

## Additional findings on the original baseline: not fixed here

These findings were traced in the tested source. Their in-game reproduction steps remain unverified.

1. **Melee can hit through terrain:** `MeleeComponent.attack` raycasts only against `PhysicsLayer.NPC`. A wall between the player and an enemy within the one-unit reach cannot occlude that ray. Reproduce with a thin solid barrier and an enemy immediately behind it, then aim and left-click. Expected: the barrier blocks the hit.
2. **Movement can interrupt hurt feedback:** `PlayerAnimationController.hurt` starts the hurt animation and sets `hurt`; movement handlers guard `jumping` but not `hurt`. Movement can replace hurt with a looping animation, leaving the hurt flag uncleared until another finite animation completes. Reproduce by taking damage and immediately pressing or releasing A/D. Expected: hurt completes before returning to locomotion.
3. **Elemental effects have no receivers:** fire/cold arrows emit `applyFire`, `burn`, `applyCold` and `slow`. Searching the main Java source finds the emitters but no corresponding listeners. Direct impact damage is separate. Reproduce by shooting an isolated skeleton once and observing subsequent health/movement without additional hits. Expected: the documented burn/slow effect is applied for its duration.

## Charged-bow integration verification — 15 September 2026

Tested executable revision: `0c08a7fbd3c2cea8b44d3401767deef1955d39d3`. Later documentation-only commits do not change that code. Environment and Java 21 command setup are the same as above.

| Check | Result |
| --- | --- |
| Tristan's PR head before integration | 414 tests passed; formatCheck and javadoc passed |
| First adapted integration run before correction | 14 tests: 12 passed, 2 expected failures |
| Expanded integration regressions before correction | 19 tests: 15 passed, 4 expected failures |
| Direct charging-readiness regression before correction | 1 expected failure |
| Corrected focused combat suite | 52 passed: 21 integration, 21 item-use, 10 bow |
| Integrated full test suite | 435 passed, 0 failures, 0 errors, 0 skipped |
| Integrated formatCheck and javadoc | Passed; existing documentation warnings remain |

The RED runs exposed a missing `itemUseFailed` event on rejected right-click charges, an E shot being accepted during a reserved charge, repeated charge starts consuming extra ammo, and invalid charge aim consuming ammo. Arrow-type preservation, last-arrow auto-selection/release and death cancellation already passed their new integration tests before the corrections.

The integration preserves two input paths:

- **E:** immediate full-speed shot, once per key press. A rejected attempt consumes nothing and emits `itemUseFailed` without `itemUsed` or `primaryAttack`.
- **Right-click:** a valid press reserves/consumes one arrow and emits `itemUsed` plus `chargeStart`. No projectile is created until release. The existing speed multiplier ranges from 0.3 for a tap to 1.5 at a 1.5-second charge.
- **Busy/cooldown:** the bow is not ready while charging. Extra E attacks and charge starts are rejected without spending more ammo or changing the reserved arrow type. Release fires once and begins the existing 0.4-second shared cooldown.
- **Dependencies/aim:** missing coordinator, missing equipped primary weapon, invalid charge aim, and cooldown rejection preserve ammunition. Rope arrows and potions retain their existing paths.
- **Cancellation:** death cancels a charged shot without firing. The already-reserved ammunition is not refunded, preserving Tristan's existing semantics; no refund feature was added.

Mockito factory mocks now target the four-argument projectile overloads, including speed multiplier. Tests keep real input, inventory, item-use, weapon and bow components; static mocks and global services/input are cleaned up between tests. Existing tests continue covering projectile collisions and rendering separately.

### Integrated manual checks still unverified

1. Tap E and hold E: one immediate shot per press.
2. Hold and release right-click at short and long durations: no early shot, increasing release speed, correct draw/hold/shoot animations.
3. While charging, press E and change slots: no extra ammo consumed; release the originally reserved arrow once.
4. Immediately after release, try E/right-click: no shot/ammo loss during cooldown; retry after 0.4 seconds.
5. Charge the last arrow in a stack: inventory can advance selection without preventing release or leaving a stuck animation.
6. Confirm rope grapple, potion use and death cancellation, plus dash/melee animations and jump height before/after grappling.

These are follow-up steps, not claimed playtest results. No independent visual verification of the integrated build has been performed.

## Local review materials

The review directory `/private/tmp/csse3200-combat-review-20260914/` contains the full automated-check log, desktop log, bug-report drafts, proposed wiki page and PR-description draft. These have not been published. The wiki proposal distinguishes reviewed main-branch behaviour, this local fix and unmerged charged-bow/dash work.
