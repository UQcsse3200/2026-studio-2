package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.GameVolume;
import com.csse3200.game.components.TextBoxComponent;
import com.csse3200.game.components.minigames.cyclopsMinigame.CyclopsBackdropSpriteComponent;
import com.csse3200.game.components.minigames.cyclopsMinigame.CyclopsCameraFollowComponent;
import com.csse3200.game.components.minigames.cyclopsMinigame.CyclopsCaveBackgroundComponent;
import com.csse3200.game.components.minigames.cyclopsMinigame.CyclopsHurtSoundComponent;
import com.csse3200.game.components.minigames.cyclopsMinigame.CyclopsMinigameLogic;
import com.csse3200.game.components.minigames.cyclopsMinigame.SleepingCyclopsRenderComponent;
import com.csse3200.game.components.minigames.cyclopsMinigame.TimingBarDisplay;
import com.csse3200.game.components.minigames.cyclopsMinigame.TimingBarLogic;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ObstacleFactory;
import com.csse3200.game.entities.factories.PlayerFactory;
import com.csse3200.game.input.CutsceneInputComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyclopsMinigameArea extends GameArea {
  private static final Logger logger = LoggerFactory.getLogger(CyclopsMinigameArea.class);

  static final float PLAYER_SCALE = 1.5f;
  private static final float FORMATION_HEIGHT_PER_PLAYER_HEIGHT = 1.7f;

  // The cave backdrop is scaled to exactly the camera's height, so its stone floor (row 812 of
  // 941) has to sit this far up the screen for everything to line up.
  private static final String CAVE_BACKGROUND_TEXTURE =
      "images/minigames/Cyclops/cave_background.png";
  private static final String CAVE_FLOOR_TEXTURE = "images/minigames/Cyclops/cave_floor.png";
  private static final int CAVE_BACKGROUND_HEIGHT = 941;
  private static final int CAVE_BACKGROUND_FLOOR_ROW = 812;
  private static final float FLOOR_SCREEN_FRACTION =
      (CAVE_BACKGROUND_HEIGHT - CAVE_BACKGROUND_FLOOR_ROW) / (float) CAVE_BACKGROUND_HEIGHT;

  // The "stones" the player hops between: barrel, cart, vase (repeating).
  static final String CAVE_FORMATION_1 = "images/minigames/Cyclops/cave_prop_barrel.png";
  static final String CAVE_FORMATION_2 = "images/minigames/Cyclops/cave_prop_cart.png";
  static final String CAVE_FORMATION_3 = "images/minigames/Cyclops/cave_prop_vase.png";
  static final String[] CAVE_FORMATION_TEXTURES = {
    CAVE_FORMATION_1, CAVE_FORMATION_2, CAVE_FORMATION_3
  };

  // The sleeping cyclops. One of his sprite pixels is this many prop-texture pixels wide, which
  // keeps him at the same scale relative to the props as in the design preview.
  private static final String CYCLOPS_SLEEP_SHEET = "images/minigames/Cyclops/sleeping_cyclops.png";
  private static final float CYCLOPS_PIXEL_IN_PROP_PIXELS = 4.2f;
  // He sleeps at the back, on a rock platform that stands on the floor line. The platform sprite
  // is drawn at the same pixel size as the cyclops.
  private static final String CAVE_PLATFORM_TEXTURE =
      "images/minigames/Cyclops/cave_rock_platform.png";

  /** Rows of the platform's top face that he sinks into, so he lies on it rather than hovering. */
  private static final float PLATFORM_REST_ROWS = 5f;

  /** Platform and cyclops are tinted slightly dark and cool so they read as further back. */
  private static final float BACK_TINT_RED = 0.80f;

  private static final float BACK_TINT_GREEN = 0.80f;
  private static final float BACK_TINT_BLUE = 0.88f;

  /** Pushes the platform just behind the cyclops (higher y draws behind) and both behind props. */
  private static final float BACK_DEPTH_OFFSET = 0.02f;

  /** Extra camera zoom-out on top of whatever is needed to fit the whole design in view. */
  private static final float CAMERA_ZOOM_OUT_EXTRA = 1.15f;

  private static final String[] cyclopsMinigameTextures = {
    "images/backgrounds/black_roof.png",
    "images/health/purple_heart.png",
    "images/ui/transparent.png",
    "images/terrain/Others/platform.png",
    "images/ui/transparent.png",
    CAVE_BACKGROUND_TEXTURE,
    CAVE_FLOOR_TEXTURE,
    CAVE_PLATFORM_TEXTURE,
    CYCLOPS_SLEEP_SHEET,
    CAVE_FORMATION_1,
    CAVE_FORMATION_2,
    CAVE_FORMATION_3,
    "images/health/PixelArt_HeartBack.png",
    "images/health/Damaged_heart.png",
    "images/health/Last_Health.png",
    "images/minigames/Cyclops/timing_bar_frame.png"
  };

  private static final String[] cyclopsMinigameTexturesAtlases = {"images/player/player.atlas"};

  private static final String[] cyclopsMinigameMusic = {
    "sounds/minigames/cyclops/cave_background_noise.mp3"
  };

  // Indexed by hearts remaining after a hit. 0 hearts is the fatal hit, which uses placeholder 4.
  public static final String[] HURT_VOICE_PATH_BY_HEARTS = {
    "sounds/hurt_player_4.wav",
    "sounds/hurt_player_2.wav",
    "sounds/hurt_player_2.wav",
    "sounds/hurt_player_1.wav",
    "sounds/hurt_player_1.wav"
  };
  public static final float[] HURT_VOLUME_BY_HEARTS = {0.6f, 0.3f, 0.3f, 0.3f, 0.3f};

  private static final String[] cyclopsMinigameSounds = {
    "sounds/walkingSounds/walkingSound.mp3",
    "sounds/minigames/cyclops/marker-hit.ogg",
    "sounds/minigames/cyclops/marker-miss.ogg",
    "sounds/hurt_player_1.wav",
    "sounds/hurt_player_2.wav",
    "sounds/hurt_player_3.wav",
    "sounds/hurt_player_4.wav"
  };

  private final TerrainFactory terrainFactory;

  private Entity player;
  private Entity minigame;
  private Entity cyclops;
  private Vector2 playerOffset = new Vector2();
  private float formationHeight;

  /** World units per prop-texture pixel (shared, so the props keep their relative sizes). */
  private float propWorldPerPixel;

  /** Height in texture pixels of the tallest prop (the vase). */
  private float tallestPropPixels;

  static final GridPoint2 MAP_SIZE = new GridPoint2(80, 30);
  static final int NUM_STATUES = 6;
  static final float STATUE_DEPTH_OFFSET = 0.01f;
  private int statueYLevel;
  private GridPoint2 winLocation;
  private ArrayList<GridPoint2> statueLocations;
  private ArrayList<GridPoint2> statueGapLocations;

  private CutsceneInputComponent input;
  private TextBoxComponent textBox;

  public CyclopsMinigameArea(CameraComponent camera, TerrainFactory terrainFactory) {
    super(camera);
    this.terrainFactory = terrainFactory;
  }

  /** Create the game area in the world. */
  @Override
  public void create() {
    loadAssets();

    spawnTerrain();
    player = spawnPlayer();
    spawnStatues();
    spawnSleepingCyclops();
    displayFloor();
    spawnCamera();

    playMusic();

    setupTimingMinigame();
  }

  /**
   * Initialises all timing minigame components. Creates the TimingBarLogic component,
   * TimingBarDisplay component, and the CyclopsMinigameLogic.
   *
   * <p>Sets the win, safe and loss locations that the player moves to as the statue locations and
   * statue gap locations.
   */
  private void setupTimingMinigame() {
    /* Timing Minigame Components */
    TimingBarLogic timingBarLogic = new TimingBarLogic(20f);
    TimingBarDisplay timingBarDisplay = new TimingBarDisplay(timingBarLogic);
    CyclopsMinigameLogic cyclopsMinigameLogic =
        new CyclopsMinigameLogic(timingBarLogic, timingBarDisplay, terrain, player);
    cyclopsMinigameLogic.setWinLocation(winLocation);
    cyclopsMinigameLogic.setSafeLocations(statueLocations);
    cyclopsMinigameLogic.setLossLocations(statueGapLocations);
    cyclopsMinigameLogic.setPlayerOffset(playerOffset);

    minigame = new Entity();
    minigame.addComponent(timingBarDisplay);
    minigame.addComponent(cyclopsMinigameLogic);
    spawnEntity(minigame);
  }

  private void spawnTerrain() {
    // Background terrain
    terrain = terrainFactory.createTerrain(TerrainFactory.TerrainType.CYCLOPS_ROOM);
    spawnEntity(new Entity().addComponent(terrain));

    statueYLevel = 3;
    winLocation = new GridPoint2(winTileX(), statueYLevel);
  }

  /**
   * Zoom needed so the whole backdrop design fits in view: the vase is designed to be {@code
   * tallestPropPixels / CAVE_BACKGROUND_HEIGHT} of the view height, with the platform and cyclops
   * above it. Never zooms in, then zooms out a little more by {@link #CAMERA_ZOOM_OUT_EXTRA}.
   */
  static float zoomFor(float formationHeight, float tallestPropPixels, float viewportHeight) {
    float designViewHeight = formationHeight * CAVE_BACKGROUND_HEIGHT / tallestPropPixels;
    return Math.max(1f, designViewHeight / viewportHeight) * CAMERA_ZOOM_OUT_EXTRA;
  }

  /**
   * Keeps the camera on the player's x, clamped so the view never passes the room's ends. The
   * camera is zoomed out (see {@link #zoomFor}) and its height is fixed so the floor line sits
   * {@link #FLOOR_SCREEN_FRACTION} of the way up the screen, which is where the backdrop's stone
   * floor ends up.
   */
  private void spawnCamera() {
    float roomWidth = terrain.tileToWorldPosition(MAP_SIZE.x, 0).x;
    float floorY = terrain.tileToWorldPosition(0, statueYLevel).y;
    OrthographicCamera camera = (OrthographicCamera) cameraComponent.getCamera();
    camera.zoom = zoomFor(formationHeight, tallestPropPixels, camera.viewportHeight);
    camera.update();
    float viewHeight = camera.viewportHeight * camera.zoom;
    float cameraY = floorY + (0.5f - FLOOR_SCREEN_FRACTION) * viewHeight;
    Entity cameraEntityHolder =
        new Entity()
            .addComponent(
                new CyclopsCameraFollowComponent(
                    player, cameraComponent.getCamera(), roomWidth, cameraY));
    spawnEntity(cameraEntityHolder);
    this.cameraComponent.setTarget(cameraEntityHolder);
  }

  /*
   * Statues are spaced evenly every (MAP_SIZE.x / NUM_STATUES) tiles. Each one is offset by
   * (MAP_SIZE.x / (NUM_STATUES * 2)) to centre it in its section, and -2 nudges it slightly. Gaps
   * sit in the same sections, and the win location sits just past the last statue.
   */
  static int statueTileX(int statueNumber) {
    return ((MAP_SIZE.x / NUM_STATUES) * statueNumber) - (MAP_SIZE.x / (NUM_STATUES * 2)) - 2;
  }

  static int gapTileX(int statueNumber) {
    return (MAP_SIZE.x / NUM_STATUES) * statueNumber - 2;
  }

  static int winTileX() {
    return statueTileX(NUM_STATUES) + (MAP_SIZE.x / NUM_STATUES) / 2;
  }

  static String caveFormationTexture(int statueNumber) {
    return CAVE_FORMATION_TEXTURES[(statueNumber - 1) % CAVE_FORMATION_TEXTURES.length];
  }

  /** Tile column the platform and cyclops are centred on: the exact middle of the room. */
  static int cyclopsTileX() {
    return MAP_SIZE.x / 2;
  }

  static float formationHeightFor(float playerHeight) {
    return FORMATION_HEIGHT_PER_PLAYER_HEIGHT * playerHeight;
  }

  /**
   * World units per texture pixel such that the tallest prop is {@code tallestPropWorldHeight}
   * tall. Every prop uses the same factor, so a barrel stays smaller than the vase.
   */
  static float propWorldPerPixelFor(float tallestPropWorldHeight, float tallestPropPixels) {
    return tallestPropWorldHeight / tallestPropPixels;
  }

  private void spawnStatues() {
    this.statueLocations = new ArrayList<>(NUM_STATUES);
    this.statueGapLocations = new ArrayList<>(NUM_STATUES);
    formationHeight = formationHeightFor(player.getScale().y);

    tallestPropPixels = 0f;
    for (String path : CAVE_FORMATION_TEXTURES) {
      Texture image = ServiceLocator.getResourceService().getAsset(path, Texture.class);
      tallestPropPixels = Math.max(tallestPropPixels, image.getHeight());
    }
    propWorldPerPixel = propWorldPerPixelFor(formationHeight, tallestPropPixels);

    for (int i = 1; i <= NUM_STATUES; i++) {
      int x = statueTileX(i);
      GridPoint2 location = new GridPoint2(x, statueYLevel);
      statueLocations.add(location);

      String formationTexture = caveFormationTexture(i);
      Texture formationImage =
          ServiceLocator.getResourceService().getAsset(formationTexture, Texture.class);
      Entity formation = ObstacleFactory.createCaveFormation(formationTexture);
      formation.setScale(
          new Vector2(
              formationImage.getWidth() * propWorldPerPixel,
              formationImage.getHeight() * propWorldPerPixel));
      spawnEntityAt(formation, new GridPoint2(x, statueYLevel), true, false);
      formation.setPosition(formation.getPosition().cpy().add(0, STATUE_DEPTH_OFFSET));
      logger.info(
          "Spawned cave formation {} at: {}",
          i,
          terrain.tileToWorldPosition(new GridPoint2(x, statueYLevel)));

      GridPoint2 gapLocation = new GridPoint2(gapTileX(i), statueYLevel);
      statueGapLocations.add(gapLocation);
    }
  }

  /**
   * Spawns the rock platform (standing on the floor line, at the back) and the sleeping cyclops
   * lying on top of it. He wakes and sleeps on the {@code cyclopsWake} / {@code cyclopsSleep}
   * events that {@link CyclopsMinigameLogic} already triggers. Set the last two constructor flags
   * to false to stop him twitching or opening his eye on his own.
   */
  private void spawnSleepingCyclops() {
    float pixelWorldSize = CYCLOPS_PIXEL_IN_PROP_PIXELS * propWorldPerPixel;
    Texture platformImage =
        ServiceLocator.getResourceService().getAsset(CAVE_PLATFORM_TEXTURE, Texture.class);
    Vector2 floorPoint = terrain.tileToWorldPosition(new GridPoint2(cyclopsTileX(), statueYLevel));
    float platformTopY = floorPoint.y + platformImage.getHeight() * pixelWorldSize;

    Entity platform =
        new Entity()
            .addComponent(
                new CyclopsBackdropSpriteComponent(CAVE_PLATFORM_TEXTURE, pixelWorldSize)
                    .setTint(BACK_TINT_RED, BACK_TINT_GREEN, BACK_TINT_BLUE));
    spawnEntity(platform);
    platform.setPosition(floorPoint.x, platformTopY + BACK_DEPTH_OFFSET);

    cyclops =
        new Entity()
            .addComponent(
                new SleepingCyclopsRenderComponent(CYCLOPS_SLEEP_SHEET, pixelWorldSize, true, true)
                    .setTint(BACK_TINT_RED, BACK_TINT_GREEN, BACK_TINT_BLUE));
    spawnEntity(cyclops);
    cyclops.setPosition(floorPoint.x, platformTopY - PLATFORM_REST_ROWS * pixelWorldSize);
  }

  /**
   * Adds the cave backdrop (one still image) with the stone floor locked to the world on the floor
   * line, and the physics floor.
   */
  private void displayFloor() {
    float roomWidth = terrain.tileToWorldPosition(MAP_SIZE.x, 0).x;
    float floorY = terrain.tileToWorldPosition(0, statueYLevel).y;

    Entity background =
        new Entity()
            .addComponent(
                new CyclopsCaveBackgroundComponent(
                    CAVE_BACKGROUND_TEXTURE,
                    CAVE_FLOOR_TEXTURE,
                    (OrthographicCamera) cameraComponent.getCamera(),
                    roomWidth,
                    floorY,
                    CAVE_BACKGROUND_FLOOR_ROW));
    spawnEntity(background);

    spawnEntityAt(
        ObstacleFactory.createWall(roomWidth, 0.1f),
        new GridPoint2(0, statueYLevel - 1),
        false,
        false);
  }

  /**
   * Spawns the player at the first statue location.
   *
   * @return the created player Entity
   */
  private Entity spawnPlayer() {
    Entity newPlayer = PlayerFactory.createPlayerDisplay();
    Sound deathVoice =
        ServiceLocator.getResourceService().getAsset("sounds/hurt_player_4.wav", Sound.class);
    newPlayer.addComponent(
        new CyclopsHurtSoundComponent(loadHurtVoices(), HURT_VOLUME_BY_HEARTS, deathVoice));
    Vector2 baseScale = newPlayer.getScale().cpy();
    newPlayer.setScale(baseScale.cpy().scl(PLAYER_SCALE));
    // Growing the sprite moves its centre right, so shift its anchor left to keep it over the tile.
    playerOffset = new Vector2(-baseScale.x * (PLAYER_SCALE - 1f) / 2f, 0f);
    GridPoint2 firstStatue = new GridPoint2(statueTileX(1), statueYLevel);
    spawnEntityAt(newPlayer, firstStatue, false, false);
    newPlayer.setPosition(terrain.tileToWorldPosition(firstStatue).add(playerOffset));
    return newPlayer;
  }

  private Sound[] loadHurtVoices() {
    Sound[] voices = new Sound[HURT_VOICE_PATH_BY_HEARTS.length];
    for (int i = 0; i < HURT_VOICE_PATH_BY_HEARTS.length; i++) {
      voices[i] =
          ServiceLocator.getResourceService().getAsset(HURT_VOICE_PATH_BY_HEARTS[i], Sound.class);
    }
    return voices;
  }

  private void playMusic() {
    Music music =
        ServiceLocator.getResourceService()
            .getAsset("sounds/minigames/cyclops/cave_background_noise.mp3", Music.class);
    music.setLooping(true);
    GameVolume.setMusicVolume(music, 0.4f);
    music.play();
  }

  private void loadAssets() {
    logger.debug("Loading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.loadTextures(cyclopsMinigameTextures);
    resourceService.loadTextureAtlases(cyclopsMinigameTexturesAtlases);
    resourceService.loadSounds(cyclopsMinigameSounds);
    resourceService.loadMusic(cyclopsMinigameMusic);

    while (!resourceService.loadForMillis(10)) {
      logger.info("Loading... {}%", resourceService.getProgress());
    }
  }

  private void unloadAssets() {
    logger.debug("Unloading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.unloadAssets(cyclopsMinigameTextures);
    resourceService.unloadAssets(cyclopsMinigameTexturesAtlases);
    resourceService.unloadAssets(cyclopsMinigameSounds);
    resourceService.unloadAssets(cyclopsMinigameMusic);
  }

  @Override
  public void dispose() {
    super.dispose();
    ServiceLocator.getResourceService()
        .getAsset("sounds/minigames/cyclops/cave_background_noise.mp3", Music.class)
        .stop();
    this.unloadAssets();
  }
}
