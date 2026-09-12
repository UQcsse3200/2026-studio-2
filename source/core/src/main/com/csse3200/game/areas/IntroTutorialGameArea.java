package com.csse3200.game.areas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.PlatformConfig;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.components.item.Item;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.item.weapons.StandardArr;
import com.csse3200.game.components.level.EnterZoneTriggerComponent;
import com.csse3200.game.components.player.FallDeathComponent;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.components.player.WakeUpCinematicDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ItemFactory;
import com.csse3200.game.entities.factories.ObstacleFactory;
import com.csse3200.game.entities.factories.PlayerFactory;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.BackgroundRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.dialogue.InstructionOverlay;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A short, narrative-driven intro level: Odysseus wakes up on a beach, is taught the controls,
 * then crosses a few platform gaps (falling is instant death) before handing off into the
 * existing {@link TutorialGameArea}.
 */
public class IntroTutorialGameArea extends GameArea {
  private static final Logger logger = LoggerFactory.getLogger(IntroTutorialGameArea.class);

  private static final String BACKGROUND_TEXTURE = "images/tutorial.png";
  private static final String FLOOR_TEXTURE = "images/beach.png";
  private static final String JUMP_PLATFORM_TEXTURE = "images/beach_platform2.png";
  private static final String WAKEUP_SOUND = "sounds/odysseus_wakeup_sound.mp3";
  private static final String ATTACK_SOUND = "sounds/Impact4.ogg";
  // Fanfare for finding the bow. Optional: only loaded/played if the file exists, so the level
  // works before the clip has been added.
  private static final String ITEM_FOUND_SOUND = "sounds/item_found.mp3";

  private static final String SHIPWRECK_TEXTURE = "images/ship_wreck.png";
  private static final String CHEST_TEXTURE = "images/chest_box.png";
  private static final String BOW_TEXTURE = "images/odysseus_bow.png";
  private static final String ARROW_TEXTURE = "images/arrow.png";
  private static final int BOW_ARROW_QUANTITY = 5;
  private static final float CHEST_HEIGHT = 1.3f;

  // Uniform-grid frame sheets generated from the hand-drawn player_sleeping.png /
  // player_wakeup.png art (whose poses are packed freeform and can't be sliced evenly).
  private static final String SLEEPING_SHEET = "images/player_sleeping_frames.png";
  private static final int SLEEPING_FRAMES = 4;
  private static final String WAKEUP_SHEET = "images/player_wakeup_frames.png";
  private static final int WAKEUP_FRAMES = 13;

  // Cell and figure sizes (px) measured from the generated sheets, used to draw the cinematic
  // figure at the same scale as the real player sprite.
  private static final float WAKEUP_CELL_WIDTH_PX = 311f;
  private static final float WAKEUP_CELL_HEIGHT_PX = 374f;
  private static final float WAKEUP_STANDING_HEIGHT_PX = 370f;
  private static final float WAKEUP_LYING_WIDTH_PX = 305f;
  private static final float SLEEPING_CELL_WIDTH_PX = 416f;
  private static final float SLEEPING_CELL_HEIGHT_PX = 238f;
  private static final float SLEEPING_LYING_WIDTH_PX = 409f;

  private static final float SLEEPING_FRAME_DURATION = 0.35f;
  private static final float WAKEUP_FRAME_DURATION = 0.15f;
  // How tall the standing figure is on screen during the cinematic, as a fraction of screen height.
  private static final float CINEMATIC_STANDING_SCREEN_FRACTION = 0.45f;
  // Matches the trimmed wake-up voice clip's length.
  private static final float WAKEUP_HOLD_DURATION = 8f;

  private static final int LEVEL_WIDTH_TILES = 38;
  private static final int LEVEL_HEIGHT_TILES = 14;
  private static final float WALL_WIDTH = 0.1f;
  private static final float FALL_THRESHOLD_Y = -2f;

  private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(2, 3);

  // The wreck Odysseus washed up from sits on the end floor, with a chest holding his bow in
  // front of it.
  private static final GridPoint2 SHIPWRECK_POSITION = new GridPoint2(31, 3);
  private static final Vector2 SHIPWRECK_SIZE = new Vector2(6f, 4f); // 1536x1024 art, 3:2
  private static final GridPoint2 CHEST_POSITION = new GridPoint2(34, 3);
  private static final GridPoint2 WRECK_INSTRUCTION_ZONE = new GridPoint2(30, 0);
  private static final GridPoint2 EXIT_ZONE = new GridPoint2(37, 0);

  private static final PlatformConfig[] groundFloors = {
    new PlatformConfig(new GridPoint2(0, 0), 12, 3, 0), // start floor
    new PlatformConfig(new GridPoint2(30, 0), 8, 3, 0), // landing/end floor
  };

  // Every gap is 2 tiles wide — a comfortable jump for the player's jump distance.
  private static final PlatformConfig[] jumpPlatforms = {
    new PlatformConfig(new GridPoint2(14, 3), 3, 1, 0), // platform after gap 1
    new PlatformConfig(new GridPoint2(19, 3), 3, 1, 0), // platform after gap 2
  };

  // A moving platform shuttles across the final gap, which is too wide to jump directly (platform
  // 2 ends at x=22, the end floor starts at x=30), so the player has to wait for it and time the
  // hop on and off. At each end of its run it sits 1 tile from the nearest ledge.
  private static final Vector2 MOVING_PLATFORM_START = new Vector2(23f, 3f);
  private static final Vector2 MOVING_PLATFORM_END = new Vector2(27f, 3f);
  private static final Vector2 MOVING_PLATFORM_SPEED = new Vector2(2.5f, 0f);
  private static final int MOVING_PLATFORM_WIDTH = 2;

  // The sand surface in beach_platform2.png sits 7% below the top of the image (a rounded lip),
  // so the collider covers only the bottom 93% of the box; otherwise the player would stand on
  // the invisible top of the box, floating above the art. Re-measure this if the art changes.
  private static final float PLATFORM_SOLID_FRACTION = 0.93f;
  // Deliberately no floor spanning the gaps between platforms — falling through is meant to be
  // lethal (FallDeathComponent), unlike the old tutorial's fully-covered ground.

  private static final String MOVE_INSTRUCTIONS_TEXT = "Press A to move left, D to move right.";
  private static final String JUMP_INSTRUCTIONS_TEXT = "Press SPACE to jump.";
  private static final String WRECK_TEXT =
      "The wreck of your ship... a chest lies half-buried in the sand beside it. Walk up to it"
          + " and press F to open it.";
  private static final String FOUND_BOW_TEXT =
      "You opened the chest and found Odysseus' bow and " + BOW_ARROW_QUANTITY + " arrows!";
  private static final String SHOOT_INSTRUCTIONS_TEXT =
      "Press E to fire an arrow. Aim with the mouse.";

  private static final String[] introTextures = {
    BACKGROUND_TEXTURE,
    SLEEPING_SHEET,
    WAKEUP_SHEET,
    FLOOR_TEXTURE,
    JUMP_PLATFORM_TEXTURE,
    SHIPWRECK_TEXTURE,
    CHEST_TEXTURE,
    BOW_TEXTURE,
    ARROW_TEXTURE,
    "images/Buttons/skip_up_btn.png",
    "images/Buttons/skip_down_btn.png",
    "images/transparent.png"
  };

  private static final String[] introTextureAtlases = {"images/player.atlas"};

  private static final String[] introSounds = {WAKEUP_SOUND, ATTACK_SOUND};
  private static final String[] optionalSounds = {ITEM_FOUND_SOUND};

  private final List<String> loadedOptionalSounds = new ArrayList<>();
  private boolean bowFound = false;

  private final TerrainFactory terrainFactory;
  private final CameraComponent camera;
  private final InstructionOverlay instructionOverlay;
  private final Runnable onWakeUp;
  private final Runnable onLevelComplete;

  /**
   * @param terrainFactory terrain factory used to build this area's terrain
   * @param camera camera used by the parallax background and camera bounds
   * @param instructionOverlay shared instruction/dialogue overlay to drive
   * @param onWakeUp called once Odysseus has stood up and the player entity exists
   * @param onLevelComplete called once the player reaches the end of the level
   */
  public IntroTutorialGameArea(
      TerrainFactory terrainFactory,
      CameraComponent camera,
      InstructionOverlay instructionOverlay,
      Runnable onWakeUp,
      Runnable onLevelComplete) {
    super(camera);
    this.terrainFactory = terrainFactory;
    this.camera = camera;
    this.instructionOverlay = instructionOverlay;
    this.onWakeUp = onWakeUp;
    this.onLevelComplete = onLevelComplete;
  }

  @Override
  public void create() {
    loadAssets();
    displayUI();
    spawnBackground();
    spawnTerrain();
    spawnFloors();
    spawnJumpPlatforms();
    spawnMovingPlatform();
    spawnShipwreck();
    spawnChest();

    // Built now so its scale is known, but only spawned into the world once Odysseus stands up.
    player = createPlayer();
    spawnWakeUpCinematic();

    spawnJumpInstructionZone();
    spawnWreckInstructionZone();
    spawnExitZone();

    // Key releases during an overlay don't reach PlayerActions (input is gated while paused), so
    // re-sync the held-key state when the overlay closes or the player keeps walking on their own.
    instructionOverlay.setOnHidden(
        () -> {
          if (player != null) {
            player.getEvents().trigger("togglePause");
          }
        });
  }

  private void displayUI() {
    Entity ui = new Entity();
    ui.addComponent(new GameAreaDisplay("Intro"));
    spawnEntity(ui);
  }

  private void spawnBackground() {
    BackgroundRenderComponent backgroundComponent = new BackgroundRenderComponent(camera);
    // 1677x938 source image -> keep the same aspect ratio at a world-scale width.
    float worldWidth = 50f;
    float worldHeight = worldWidth * (938f / 1677f);
    backgroundComponent.addLayer(BACKGROUND_TEXTURE, 0.3f, worldWidth, worldHeight, -1.5f);

    Entity background = new Entity().addComponent(backgroundComponent);
    background.setPosition(-10f, -8f);
    spawnEntity(background);
  }

  private void spawnTerrain() {
    terrain = terrainFactory.createTerrain(TerrainType.BACKGROUND_DESERT);
    spawnEntity(new Entity().addComponent(terrain));

    float tileSize = terrain.getTileSize();
    float worldWidth = LEVEL_WIDTH_TILES * tileSize;
    float worldHeight = LEVEL_HEIGHT_TILES * tileSize;

    // Left, right and top walls physically stop the player; there is no bottom wall since gaps
    // must be lethal, not blocked.
    spawnEntityAt(
        ObstacleFactory.createWall(WALL_WIDTH, worldHeight), new GridPoint2(0, 0), false, false);
    spawnEntityAt(
        ObstacleFactory.createWall(WALL_WIDTH, worldHeight),
        new GridPoint2(LEVEL_WIDTH_TILES, 0),
        false,
        false);
    spawnEntityAt(
        ObstacleFactory.createWall(worldWidth, WALL_WIDTH),
        new GridPoint2(0, LEVEL_HEIGHT_TILES),
        false,
        false);

    // The camera stops panning at the walls instead of showing empty space past the map edge.
    camera.setHorizontalBounds(0f, worldWidth);
    // Frame above the player so the floor sits low on screen instead of dead-centre.
    camera.setVerticalFramingOffset(3.5f);
  }

  private void spawnFloors() {
    for (PlatformConfig config : groundFloors) {
      Entity floor = ObstacleFactory.createFloor(config.grappleSides, FLOOR_TEXTURE);
      floor.setScale(config.width, config.height);
      spawnEntityAt(floor, config.position, false, false);
    }
  }

  private void spawnJumpPlatforms() {
    for (PlatformConfig config : jumpPlatforms) {
      Entity platform = ObstacleFactory.createPlatform(config.grappleSides, JUMP_PLATFORM_TEXTURE);
      platform.setScale(config.width, config.height);
      fitColliderToSand(platform);
      spawnEntityAt(platform, config.position, false, false);
    }
  }

  /** Shrinks a beach-platform collider to the visible sand; must run before the entity spawns. */
  private void fitColliderToSand(Entity platform) {
    Vector2 scale = platform.getScale();
    platform
        .getComponent(ColliderComponent.class)
        .setAsBoxAligned(
            new Vector2(scale.x, scale.y * PLATFORM_SOLID_FRACTION),
            PhysicsComponent.AlignX.CENTER,
            PhysicsComponent.AlignY.BOTTOM);
  }

  private void spawnMovingPlatform() {
    Entity platform =
        ObstacleFactory.createMovingPlatform(
            0,
            MOVING_PLATFORM_START,
            MOVING_PLATFORM_END,
            MOVING_PLATFORM_SPEED,
            JUMP_PLATFORM_TEXTURE);
    platform.setScale(MOVING_PLATFORM_WIDTH, 1);
    fitColliderToSand(platform);
    spawnEntityAt(
        platform,
        new GridPoint2((int) MOVING_PLATFORM_START.x, (int) MOVING_PLATFORM_START.y),
        false,
        false);
  }

  private Entity createPlayer() {
    Entity newPlayer = PlayerFactory.createPlayer();
    newPlayer.addComponent(new FallDeathComponent(FALL_THRESHOLD_Y));

    KeyboardPlayerInputComponent input = newPlayer.getComponent(KeyboardPlayerInputComponent.class);
    if (input != null) {
      input.setCameraComponent(cameraComponent);
    }
    newPlayer.getEvents().addListener("itemPickedUp", (Item item) -> onItemPickedUp(item));
    return newPlayer;
  }

  /** Purely decorative: the wreck is drawn behind everything else in the world layer. */
  private void spawnShipwreck() {
    TextureRenderComponent render =
        new TextureRenderComponent(SHIPWRECK_TEXTURE) {
          @Override
          public float getZIndex() {
            return -100f;
          }
        };
    Entity wreck = new Entity().addComponent(render);
    wreck.setScale(SHIPWRECK_SIZE.x, SHIPWRECK_SIZE.y);
    spawnEntityAt(wreck, SHIPWRECK_POSITION, false, false);
  }

  /**
   * The chest is an item entity drawn with the chest art: opening it (F in range) picks up the
   * arrows it holds and removes the chest, and the found-bow overlay announces the discovery.
   */
  private void spawnChest() {
    Entity chest = ItemFactory.createItem(new StandardArr(BOW_ARROW_QUANTITY), CHEST_TEXTURE);
    chest.scaleHeight(CHEST_HEIGHT);
    spawnEntityAt(chest, CHEST_POSITION, true, false);
  }

  private void onItemPickedUp(Item item) {
    if (bowFound || item == null || item.getItemType() != ItemType.ARROW) {
      return;
    }
    bowFound = true;
    Texture bowTexture = ServiceLocator.getResourceService().getAsset(BOW_TEXTURE, Texture.class);
    instructionOverlay.showSequence(
        List.of(
            new InstructionOverlay.Line(FOUND_BOW_TEXT, ITEM_FOUND_SOUND, null, bowTexture),
            new InstructionOverlay.Line(SHOOT_INSTRUCTIONS_TEXT)));
  }

  /**
   * The world is blacked out while Odysseus sleeps centre-screen (looping breathing/Zzz animation)
   * for the wake-up voice, then rises to standing. Once standing, the real player is spawned and
   * the black fades out to reveal the world, then the first instruction is shown.
   */
  private void spawnWakeUpCinematic() {
    ResourceService resourceService = ServiceLocator.getResourceService();
    Animation<TextureRegion> sleeping =
        WakeUpCinematicDisplay.fromGrid(
            resourceService.getAsset(SLEEPING_SHEET, Texture.class),
            SLEEPING_FRAMES,
            1,
            SLEEPING_FRAME_DURATION,
            Animation.PlayMode.LOOP);
    Animation<TextureRegion> rising =
        WakeUpCinematicDisplay.fromGrid(
            resourceService.getAsset(WAKEUP_SHEET, Texture.class),
            WAKEUP_FRAMES,
            1,
            WAKEUP_FRAME_DURATION,
            Animation.PlayMode.NORMAL);

    // Size the rising frames so the standing figure fills a good chunk of the screen, and the
    // sleeping frames so their lying figure matches the rising sheet's lying figure.
    float standingHeightPx = Gdx.graphics.getHeight() * CINEMATIC_STANDING_SCREEN_FRACTION;
    float riseCellHeight = standingHeightPx * WAKEUP_CELL_HEIGHT_PX / WAKEUP_STANDING_HEIGHT_PX;
    float riseCellWidth = riseCellHeight * WAKEUP_CELL_WIDTH_PX / WAKEUP_CELL_HEIGHT_PX;
    float lyingWidth = riseCellWidth * WAKEUP_LYING_WIDTH_PX / WAKEUP_CELL_WIDTH_PX;
    float sleepCellWidth = lyingWidth * SLEEPING_CELL_WIDTH_PX / SLEEPING_LYING_WIDTH_PX;
    float sleepCellHeight = sleepCellWidth * SLEEPING_CELL_HEIGHT_PX / SLEEPING_CELL_WIDTH_PX;

    Entity cinematic = new Entity();
    cinematic.addComponent(
        new WakeUpCinematicDisplay(
            sleeping,
            WAKEUP_HOLD_DURATION,
            new Vector2(sleepCellWidth, sleepCellHeight),
            rising,
            new Vector2(riseCellWidth, riseCellHeight),
            resourceService.getAsset(WAKEUP_SOUND, Sound.class),
            this::onStoodUp,
            () -> instructionOverlay.show(MOVE_INSTRUCTIONS_TEXT)));
    spawnEntity(cinematic);
  }

  private void onStoodUp() {
    spawnEntityAt(player, PLAYER_SPAWN, true, true);
    camera.setTarget(player);
    if (onWakeUp != null) {
      onWakeUp.run();
    }
  }

  private void spawnJumpInstructionZone() {
    Entity zone = ObstacleFactory.createTriggerZone(new Vector2(1f, LEVEL_HEIGHT_TILES));
    zone.addComponent(
        new EnterZoneTriggerComponent(() -> instructionOverlay.show(JUMP_INSTRUCTIONS_TEXT)));
    spawnEntityAt(zone, new GridPoint2(10, 0), true, false);
  }

  private void spawnWreckInstructionZone() {
    Entity zone = ObstacleFactory.createTriggerZone(new Vector2(1f, LEVEL_HEIGHT_TILES));
    zone.addComponent(new EnterZoneTriggerComponent(() -> instructionOverlay.show(WRECK_TEXT)));
    spawnEntityAt(zone, WRECK_INSTRUCTION_ZONE, true, false);
  }

  private void spawnExitZone() {
    Entity zone = ObstacleFactory.createTriggerZone(new Vector2(1f, 3f));
    zone.addComponent(new EnterZoneTriggerComponent(onLevelComplete));
    spawnEntityAt(zone, EXIT_ZONE, true, false);
  }

  private void loadAssets() {
    logger.debug("Loading intro tutorial assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.loadTextures(introTextures);
    resourceService.loadTextureAtlases(introTextureAtlases);
    resourceService.loadSounds(introSounds);

    for (String sound : optionalSounds) {
      if (Gdx.files.internal(sound).exists()) {
        loadedOptionalSounds.add(sound);
      } else {
        logger.info("Optional sound {} not found, skipping", sound);
      }
    }
    resourceService.loadSounds(loadedOptionalSounds.toArray(new String[0]));

    resourceService.loadAll();
  }

  @Override
  public void dispose() {
    super.dispose();
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.unloadAssets(introTextures);
    resourceService.unloadAssets(introTextureAtlases);
    resourceService.unloadAssets(introSounds);
    resourceService.unloadAssets(loadedOptionalSounds.toArray(new String[0]));
  }

  /**
   * @return the player entity, or null until Odysseus has stood up and the player has spawned
   */
  public Entity getPlayer() {
    return player;
  }
}
