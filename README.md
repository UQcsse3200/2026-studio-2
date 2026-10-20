# Odysseus

*Odysseus* is a 2D action platformer based on Homer's *Odyssey*. After the fall of Troy, Odysseus washes up on Calypso's island with no memory of his voyage home. Each level and minigame retells one of the trials he has forgotten, from escaping the Cyclops' cave to facing the enchantress Circe. The game ends with a final battle against Calypso herself.

The game is being built by six teams of students in Studio 2 of [CSSE3200](https://programs-courses.uq.edu.au/course.html?course_code=CSSE3200) at the University of Queensland. It is written in Java on the UQ game engine, which is based on [libGDX](https://libgdx.com/).

[Wiki](https://github.com/UQcsse3200/2026-studio-2/wiki) · [Getting Started](https://github.com/UQcsse3200/2026-studio-2/wiki/Getting-Started) · [SonarCloud](https://sonarcloud.io/project/overview?id=UQcsse3200_2026-studio-2) · [Engine JavaDoc](https://uqcsse3200.github.io/game-engine/)

## Features

- **Story levels:** three platforming levels and a boss arena against Calypso. Cutscenes between them tell the story.
- **Combat:** melee attacks, plus a bow that charges while held. Choose standard, fire or ice arrows from the arrow wheel.
- **Grappling:** fire a rope arrow at a platform, then climb, descend or swing from it.
- **Enemies:** skeleton warriors and archers, vultures, and necromancers that summon skeletons.
- **Items and economy:**
  - collect gold and items, and manage them in your hotbar and backpack;
  - buy arrows and potions from the shopkeeper;
  - track what you've found in the item dictionary.
- **Minigames:**
  - the Cyclops timing challenge;
  - Blackjack against Circe;
  - Spin the Wheel.
- **Saving:** three save slots, with checkpoints inside each level.
- **Atmosphere:** dynamic lighting, weather, particle effects, music and sound effects.

## Controls

| Action | Key / button |
| --- | --- |
| Move | `A` / `D` or `←` / `→` |
| Sprint | `Shift` |
| Jump | `Space` |
| Melee attack | Left click |
| Aim and shoot the bow (hold to charge) | Right click |
| Arrow wheel | Hold `Tab` |
| Use the selected item | `E` |
| Interact / talk / shop | `F` |
| Select a hotbar slot | `1`–`9`, or `,` / `.` to cycle |
| Backpack | `B` |
| Drop / delete the selected item | `R` / `Delete` |
| Climb or descend a rope, drop through a ledge | `W` / `S` |
| Play the instrument | `Q` |
| Level map | `M` |
| Pause | `Esc` |
| Debug terminal | `F1` |

## Getting started

### Requirements

- **JDK 21.** The build uses a Java 21 toolchain.
- **Git.** No Gradle install is needed, because the Gradle wrapper is included.
- **Recommended:** IntelliJ IDEA. Open the `source/` folder as a Gradle project.
- **macOS:** also read the [macOS setup guide](https://github.com/UQcsse3200/2026-studio-2/wiki/macOS-Setup-Guide).

### Build and run

All Gradle commands are run from the `source/` folder. On Windows, use `gradlew.bat` instead of `./gradlew`.

```bash
git clone https://github.com/UQcsse3200/2026-studio-2.git
cd 2026-studio-2/source

./gradlew desktop:run     # launch the game
./gradlew test            # run the unit tests
./gradlew format          # format the code with google-java-format
./gradlew formatCheck     # check formatting, as CI does
./gradlew desktop:dist    # build a runnable jar in desktop/build/libs
```

To debug, run `./gradlew desktop:debug` and attach your IDE's debugger to port 5005.

### Debug terminal

Press `F1` in game to open the terminal, then type a command and press `Enter`. This is useful for jumping straight to the part of the game you're working on.

| Command | What it does |
| --- | --- |
| `debug` | Toggles the physics debug renderer |
| `killAllEnemies` | Removes every enemy in the level |
| `win` / `lose` | Shows the victory or defeat screen |
| `cutscene` | Plays a cutscene |
| `cyclopsMinigame`, `blackjackMinigame`, `spinTheWheel` | Opens a minigame |
| `textbox` | Shows the example text boxes |
| `tutorial` | Shows the tutorial |

See the [Debug Terminal](https://github.com/UQcsse3200/2026-studio-2/wiki/Debug-Terminal) wiki page for arguments and details.

## Project structure

```
source/
├── core/
│   ├── assets/              images, sounds, fonts, cutscenes and JSON configs
│   ├── src/main/com/csse3200/game/
│   │   ├── GdxGame.java     entry point and screen switching
│   │   ├── screens/         main menu, levels, settings, cutscenes, minigame screens
│   │   ├── areas/           level layouts (Level 1–3, boss arena) and terrain
│   │   ├── entities/        entities and the factories that build players, enemies and items
│   │   ├── components/      gameplay behaviour: player, combat, inventory, shop, minigames …
│   │   ├── physics/         Box2D wrapper and collision handling
│   │   ├── rendering/       render service, backgrounds, lighting
│   │   ├── services/        resources, time, input and other shared services
│   │   ├── files/           save files and settings
│   │   └── ui/              HUD, menus, text boxes, debug terminal
│   └── src/test/            JUnit 5 + Mockito unit tests
└── desktop/                 desktop launcher (LWJGL3)
```

The engine uses an **Entity Component System**. An entity is an empty container, and all of its behaviour comes from the components attached to it. Components communicate through the entity's **event system** and find shared services, such as rendering, physics and resources, through the `ServiceLocator`. If you're new to the code, start with [Entity Component System](https://github.com/UQcsse3200/2026-studio-2/wiki/Entity-Component-System-%28ECS%29), [Event System](https://github.com/UQcsse3200/2026-studio-2/wiki/Event-System) and [Service Locator](https://github.com/UQcsse3200/2026-studio-2/wiki/Service-Locator) in the wiki.

## Teams

| Team | Area | Wiki |
| --- | --- | --- |
| Team 1 | Enemies, AI and the boss | [Enemies](https://github.com/UQcsse3200/2026-studio-2/wiki/Enemies) |
| Team 2 | Items, inventory, shop and gold | [Item System](https://github.com/UQcsse3200/2026-studio-2/wiki/Item-System) |
| Team 3 | UI, menus, cutscenes and audio | [UI](https://github.com/UQcsse3200/2026-studio-2/wiki/UI) |
| Team 4 | Levels, maps, lighting and weather | [Game Levels](https://github.com/UQcsse3200/2026-studio-2/wiki/Game-Levels) |
| Team 5 | Player movement, combat and grappling | [Player](https://github.com/UQcsse3200/2026-studio-2/wiki/Player) |
| Team 6 | Minigames | [Minigames](https://github.com/UQcsse3200/2026-studio-2/wiki/Minigames) |

## Contributing

1. Take, or create, a ticket on the issue board using the matching issue template.
2. Create a branch off `main`, named after your work (e.g. `feature/...`, `fix/...`).
3. Add unit tests for new behaviour, then run `./gradlew test` and `./gradlew format` before pushing.
4. Open a pull request into `main` that links the ticket. SonarCloud analyses every pull request.

## Licence and acknowledgements

Built on the UQ game engine, developed at the [University of Queensland](https://uq.edu.au/) for studio-based project courses and released under the [MIT licence](LICENSE). Acknowledgement of the engine's source is required. Third-party art, music and sound are credited on the [Asset Attribution](https://github.com/UQcsse3200/2026-studio-2/wiki/Asset-Attribution) wiki page.
