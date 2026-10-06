package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.TextBoxComponent;
import com.csse3200.game.components.minigames.cyclopsMinigame.CyclopsCameraFollowComponent;
import com.csse3200.game.components.minigames.cyclopsMinigame.CyclopsHurtSoundComponent;
import com.csse3200.game.components.minigames.cyclopsMinigame.CyclopsMinigameLogic;
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
  private static final String[] cyclopsMinigameTextures = {
    "images/backgrounds/black_roof.png",
    "images/health/purple_heart.png",
    "images/ui/transparent.png",
    "images/terrain/Others/platform.png",
    "images/ui/transparent.png",
    "images/Greek Statues Pack I/Brute.png",
    "images/backgrounds/CyclopsMinigameFloor.png",
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
  private Vector2 playerOffset = new Vector2();
  private static final float PLAYER_SCALE = 1.5f;

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
    spawnStatues();
    displayFloor();

    player = spawnPlayer();
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
   * Keeps the camera on the player's x, clamped so the view never passes the room's ends. The
   * height stays fixed at the middle of the room.
   */
  private void spawnCamera() {
    float roomWidth = terrain.tileToWorldPosition(MAP_SIZE.x, 0).x;
    float cameraY = terrain.tileToWorldPosition(0, MAP_SIZE.y / 2).y;
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

  private void spawnStatues() {
    this.statueLocations = new ArrayList<>(NUM_STATUES);
    this.statueGapLocations = new ArrayList<>(NUM_STATUES);

    for (int i = 1; i <= NUM_STATUES; i++) {
      int x = statueTileX(i);
      GridPoint2 location = new GridPoint2(x, statueYLevel);
      statueLocations.add(location);

      Entity statue = ObstacleFactory.createStatue();
      statue.setScale(new Vector2(3, 6));
      spawnEntityAt(statue, new GridPoint2(x, statueYLevel), true, false);
      statue.setPosition(statue.getPosition().cpy().add(0, STATUE_DEPTH_OFFSET));
      logger.info(
          "Spawned statue {} at: {}",
          i,
          terrain.tileToWorldPosition(new GridPoint2(x, statueYLevel)));

      GridPoint2 gapLocation = new GridPoint2(gapTileX(i), statueYLevel);
      statueGapLocations.add(gapLocation);
    }
  }

  /** Creates and displays the floor entity that spans the entire room */
  private void displayFloor() {
    float roomWidth = terrain.tileToWorldPosition(MAP_SIZE.x, 0).x;
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
    spawnEntityAt(newPlayer, statueLocations.getFirst(), false, false);
    newPlayer.setPosition(
        terrain.tileToWorldPosition(statueLocations.getFirst()).add(playerOffset));
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
    music.setVolume(0.4f);
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
