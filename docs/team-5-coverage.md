# Team 5 local coverage — 28 September 2026

Branch: `team-5-testing-bug-fixes`, based on upstream main `a993d955`.
All changes and results are local. No Sonar analysis was submitted.

## Results

The baseline is the full 628-test run after the local grapple null-check change and wrap/unwrap test. It is newer than Sonar's 21 September snapshot. The final full run has **652 tests, zero failures, zero errors and zero skips**. Java 21 / Gradle `test` and `formatCheck` passed.

| Measure | Before this coverage pass | After |
| --- | ---: | ---: |
| Combined line/branch estimate | 69.9% | 80.8% |
| Line coverage | 72.9% (1,145 / 1,571) | 84.3% (1,324 / 1,571) |
| Branch coverage | 64.2% (523 / 815) | 74.0% (603 / 815) |

The combined estimate is `(covered lines + covered branches) / (total lines + total branches)`, calculated from JaCoCo source-file counters, not an average of class percentages. This remains an estimated Team 5 scope, not an official Sonar team metric.

| Changed coverage | Before | After |
| --- | ---: | ---: |
| MeleeComponent | 0.0% | 100.0% |
| GrappleRenderComponent | 10.5% | 100.0% |
| PlayerAnimationController | 59.9% | 94.4% |
| PlayerFactory | 0.0% | 97.5% |
| PlayerConfig | 0.0% | 100.0% |

## Scope

The same 18 Java source files are used before and after, based on Team 5's documented movement, grapple, combat, health, animation and player-assembly responsibilities:

- `components/player`: KeyboardPlayerInputComponent, PlayerActions, PlayerAnimationController, ItemUseComponent.
- `components/item/weapons`: WeaponComponent, bow/BowComponent, melee/MeleeComponent, bow/grapple/GrappleComponent, bow/grapple/GrappleArrowComponent.
- `components/projectile`: ArrowProjectileComponent, ArrowType.
- `components`: CombatStatsComponent.
- `entities/factories`: PlayerFactory, ProjectileFactory.
- `entities/configs`: PlayerConfig.
- `rendering/item`: GrappleRenderComponent, ArrowRenderComponent, MeleeRenderComponent.

Paths are relative to `source/core/src/main/com/csse3200/game`. Inventory, HUD, arrow-wheel, poison-potion and generic engine files are excluded. Shared code inside the selected files is included at file granularity; this is not author-by-author coverage.

## Tests added or strengthened

- MeleeComponentTest: real Box2D targeting, nearest enemy, both directions, normalized range, damage, cooldown/recovery, invalid aim, layer filtering and missing target metadata.
- PlayerAnimationTransitionsTest: real animation playback for locomotion, sprint, jump, dash, hurt and melee recovery; bow cancellation and death priority.
- PlayerFactoryTest: real assembly, equipped bow identity, combat clips, physical hitbox, configured health/damage/invulnerability and display-only player setup.
- GrappleRenderComponentTest: replaces construction-only checks with missing/detached/short-path guards, every rope segment, batch ordering, updated paths, renderer reuse and disposal.

This pass adds 26 tests and replaces two construction-only tests: net +24. It makes no production changes beyond the previously completed grapple null-check change.

Two temporary mutations verified meaningful assertions, with production sources restored in `finally` blocks:

1. Removing melee damage failed with expected health 7, actual 10.
2. Removing dash animation recovery failed with expected `sprint`, actual `air_dash`.

## Reproduce

Run from `source` with Java 21:

```sh
./gradlew test formatCheck --console=plain
```

Full report: `source/core/build/reports/jacoco/test/html/index.html`.
Raw counters: `source/core/build/reports/jacoco/test/jacocoTestReport.xml`.
A focused test run replaces the coverage report with that subset, so run the full suite before comparing totals.

Graphics tests verify drawing commands at a mocked graphics boundary; they do not verify pixels or playability. Factory tests exercise assembly and selected components without launching the full inventory/shop UI. Existing Sonar Gradle deprecation and JVM instrumentation notices remain.

Remaining gaps include MeleeRenderComponent (29.4%), ArrowRenderComponent (47.4%), ProjectileFactory (55.0%), and branches in input, item use, movement, projectiles and grapple behaviour. No unrelated gameplay fixes were included.
