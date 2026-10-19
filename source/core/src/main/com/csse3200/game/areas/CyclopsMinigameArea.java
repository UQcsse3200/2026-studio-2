package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.minigames.cyclopsMinigame.CyclopsAnimationController;
import com.csse3200.game.components.minigames.cyclopsMinigame.CyclopsCameraFollowComponent;
import com.csse3200.game.components.minigames.cyclopsMinigame.CyclopsFloorRenderComponent;
import com.csse3200.game.components.minigames.cyclopsMinigame.CyclopsHurtSoundComponent;
import com.csse3200.game.components.minigames.cyclopsMinigame.CyclopsMinigameLogic;
import com.csse3200.game.components.minigames.cyclopsMinigame.CyclopsSoundComponent;
import com.csse3200.game.components.minigames.cyclopsMinigame.TimingBarDisplay;
import com.csse3200.game.components.minigames.cyclopsMinigame.TimingBarLogic;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ObstacleFactory;
import com.csse3200.game.entities.factories.PlayerFactory;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.BackgroundRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyclopsMinigameArea extends GameArea {
  private static final String CAVE_BACKGROUND_MUSIC =
      "sounds/minigames/cyclops/cave_background_noise.mp3";

  private static final String HURT_PLAYER_ONE_SOUND = "sounds/hurt_player_1.wav";

  private static final String HURT_PLAYER_TWO_SOUND = "sounds/hurt_player_2.wav";

  private static final String HURT_PLAYER_FOUR_SOUND = "sounds/hurt_player_4.wav";

  private static final Logger logger = LoggerFactory.getLogger(CyclopsMinigameArea.class);

  static final float PLAYER_SCALE = 1.5f;
  private static final float FORMATION_HEIGHT_PER_PLAYER_HEIGHT = 1.7f;
  private static final int CAVE_FLOOR_GROUND_TOP_ROW = 289;
  private static final int CAVE_FLOOR_GROUND_ROWS = 35;
  private static final float FLOOR_SCREEN_FRACTION = 0.2f;
  private static final float BACKGROUND_PARALLAX = 0.1f;
  // Row of the background image (0 = top) where its stone floor starts.
  private static final int CAVE_BACKGROUND_FLOOR_ROW = 812;

  private static final String CAVE_BACKGROUND_TEXTURE =
      "images/minigames/Cyclops/cave_background.png";
  private static final String CAVE_FLOOR_TEXTURE = "images/minigames/Cyclops/CyclopsCaveFloor.png";
  static final String CAVE_FORMATION_1 = "images/minigames/Cyclops/cave_formation_1.png";
  static final String CAVE_FORMATION_2 = "images/minigames/Cyclops/cave_formation_2.png";
  static final String CAVE_FORMATION_3 = "images/minigames/Cyclops/cave_formation_3.png";
  static final String[] CAVE_FORMATION_TEXTURES = {
    CAVE_FORMATION_1, CAVE_FORMATION_2, CAVE_FORMATION_3
  };

  private static final String CAVE_ROCK_PLATFORM =
      "images/minigames/Cyclops/cave_rock_platform.png";
  private static final String CYCLOPS_ATLAS = "images/minigames/Cyclops/cyclops.atlas";
  private static final float CAVE_ROCK_WIDTH = 7.5f;
  private static final float CYCLOPS_FRAME_WIDTH = 10.75f;
  private static final float CYCLOPS_BODY_COLUMN = 114f;
  // Rock pixels he sinks into its top face, so he stands on it rather than on its back edge.
  private static final int SLEEPING_CYCLOPS_SINK_PIXELS = 5;

  private static final String[] cyclopsMinigameTextures = {
    "images/backgrounds/black_roof.png",
    "images/health/purple_heart.png",
    "images/ui/transparent.png",
    "images/terrain/Others/platform.png",
    "images/ui/transparent.png",
    CAVE_BACKGROUND_TEXTURE,
    CAVE_FLOOR_TEXTURE,
    CAVE_FORMATION_1,
    CAVE_FORMATION_2,
    CAVE_FORMATION_3,
    CAVE_ROCK_PLATFORM,
    "images/health/PixelArt_HeartBack.png",
    "images/health/Damaged_heart.png",
    "images/health/Last_Health.png",
    "images/items/gold_coin.png",
    "images/backgrounds/coin_bg.png",
    "images/minigames/Cyclops/timing_bar_frame.png"
  };

  private static final String[] cyclopsMinigameTexturesAtlases = {
    "images/player/player.atlas", CYCLOPS_ATLAS
  };

  private static final String[] cyclopsMinigameMusic = {CAVE_BACKGROUND_MUSIC};

  // Indexed by hearts remaining after a hit. 0 hearts is the fatal hit, which uses placeholder 4.
  public static final List<String> HURT_VOICE_PATH_BY_HEARTS =
      List.of(
          HURT_PLAYER_FOUR_SOUND,
          HURT_PLAYER_TWO_SOUND,
          HURT_PLAYER_TWO_SOUND,
          HURT_PLAYER_ONE_SOUND,
          HURT_PLAYER_ONE_SOUND);
  public static final List<Float> HURT_VOLUME_BY_HEARTS = List.of(0.6f, 0.3f, 0.3f, 0.3f, 0.3f);

  private static final String[] cyclopsMinigameSounds = {
    "sounds/walkingSounds/walkingSound.mp3",
    "sounds/minigames/cyclops/marker-hit.ogg",
    "sounds/minigames/cyclops/marker-miss.ogg",
    HURT_PLAYER_ONE_SOUND,
    HURT_PLAYER_TWO_SOUND,
    "sounds/hurt_player_3.wav",
    HURT_PLAYER_FOUR_SOUND,
    CyclopsSoundComponent.SNORE_PATH,
    CyclopsSoundComponent.ROAR_PATH,
    CyclopsSoundComponent.LANDING_PATH,
    CyclopsSoundComponent.CLUB_HIT_PATH
  };

  private final TerrainFactory terrainFactory;

  private Entity minigamePlayer;
  private Vector2 playerOffset = new Vector2();
  private float formationHeight;

  static final GridPoint2 MAP_SIZE = new GridPoint2(80, 30);
  static final int NUM_STATUES = 6;
  static final float STATUE_DEPTH_OFFSET = 0.01f;
  private int statueYLevel;
  private GridPoint2 winLocation;
  private ArrayList<GridPoint2> statueLocations;
  private ArrayList<GridPoint2> statueGapLocations;

  public CyclopsMinigameArea(CameraComponent camera, TerrainFactory terrainFactory) {
    super(camera);
    this.terrainFactory = terrainFactory;
  }

  /** Create the game area in the world. */
  @Override
  public void create() {
    loadAssets();

    spawnTerrain();
    spawnBackground();
    minigamePlayer = spawnPlayer();
    spawnStatues();
    Entity cyclops = spawnSleepingCyclops();
    displayFloor();
    spawnCamera(cyclops);

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
        new CyclopsMinigameLogic(timingBarLogic, timingBarDisplay, terrain, minigamePlayer);
    cyclopsMinigameLogic.setWinLocation(winLocation);
    cyclopsMinigameLogic.setSafeLocations(statueLocations);
    cyclopsMinigameLogic.setLossLocations(statueGapLocations);
    cyclopsMinigameLogic.setPlayerOffset(playerOffset);

    Entity minigame = new Entity();
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

  private void spawnBackground() {
    float roomWidth = terrain.tileToWorldPosition(MAP_SIZE.x, 0).x;
    float floorY = terrain.tileToWorldPosition(0, statueYLevel).y;
    float viewWidth = cameraComponent.getCamera().viewportWidth;

    Texture image =
        ServiceLocator.getResourceService().getAsset(CAVE_BACKGROUND_TEXTURE, Texture.class);
    float width = viewWidth + BACKGROUND_PARALLAX * (roomWidth - viewWidth);
    float height = width * image.getHeight() / image.getWidth();
    float floorHeight =
        height * (image.getHeight() - CAVE_BACKGROUND_FLOOR_ROW) / image.getHeight();
    Vector2 backgroundPos =
        new Vector2(-(1f - BACKGROUND_PARALLAX) * viewWidth / 2f, floorY - floorHeight);

    BackgroundRenderComponent backgroundComponent =
        new BackgroundRenderComponent(
            cameraComponent, backgroundPos, terrain.tileToWorldPosition(MAP_SIZE));
    backgroundComponent.addLayer(
        CAVE_BACKGROUND_TEXTURE,
        new Vector2(BACKGROUND_PARALLAX, 0f),
        width,
        height,
        new Vector2(0f, 0f),
        new Vector2(0f, 0f),
        RepeatMode.NONE,
        0f,
        1f,
        false,
        0,
        -1);

    Entity background = new Entity().addComponent(backgroundComponent);
    background.setPosition(backgroundPos);
    spawnEntity(background);
  }

  /**
   * Keeps the camera on the player's x, clamped so the view never passes the room's ends. The
   * height is fixed so the floor line sits a fifth of the way up the screen.
   */
  private void spawnCamera(Entity cyclops) {
    float roomWidth = terrain.tileToWorldPosition(MAP_SIZE.x, 0).x;
    float floorY = terrain.tileToWorldPosition(0, statueYLevel).y;
    float viewHeight = cameraComponent.getCamera().viewportHeight;
    float cameraY = floorY + (0.5f - FLOOR_SCREEN_FRACTION) * viewHeight;
    CyclopsCameraFollowComponent follow =
        new CyclopsCameraFollowComponent(
            minigamePlayer, cameraComponent.getCamera(), roomWidth, cameraY);
    cyclops.getEvents().addListener(CyclopsAnimationController.LANDED_EVENT, follow::shake);
    Entity cameraEntityHolder = new Entity().addComponent(follow);
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

  static float formationHeightFor(float playerHeight) {
    return FORMATION_HEIGHT_PER_PLAYER_HEIGHT * playerHeight;
  }

  private void spawnStatues() {
    this.statueLocations = new ArrayList<>(NUM_STATUES);
    this.statueGapLocations = new ArrayList<>(NUM_STATUES);
    formationHeight = formationHeightFor(minigamePlayer.getScale().y);

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
              formationHeight * formationImage.getWidth() / formationImage.getHeight(),
              formationHeight));
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
   * Spawns the cyclops dozing on his feet, club on his shoulder, on a rock in the middle of the
   * room. Both sit just behind the cave formations so the player and formations draw in front of
   * them.
   */
  private Entity spawnSleepingCyclops() {
    Vector2 middle = terrain.tileToWorldPosition(MAP_SIZE.x / 2, statueYLevel);

    TextureRenderComponent rockRender =
        new TextureRenderComponent(CAVE_ROCK_PLATFORM) {
          // Sorted by its top rather than its base, so the cyclops standing on it draws in front.
          @Override
          public float getZIndex() {
            return -(entity.getPosition().y + entity.getScale().y);
          }
        };
    Entity rock = new Entity().addComponent(rockRender);
    rockRender.scaleEntity();
    rock.scaleWidth(CAVE_ROCK_WIDTH);
    rock.setPosition(middle.x - CAVE_ROCK_WIDTH / 2f, middle.y + 2f * STATUE_DEPTH_OFFSET);
    spawnEntity(rock);

    AnimationRenderComponent cyclopsAnimator =
        new AnimationRenderComponent(
            ServiceLocator.getResourceService().getAsset(CYCLOPS_ATLAS, TextureAtlas.class));
    cyclopsAnimator.addAnimation("sleep", 0.1f, Animation.PlayMode.LOOP, CYCLOPS_BODY_COLUMN, 0f);
    cyclopsAnimator.addAnimation("idle", 0.083f, Animation.PlayMode.LOOP, CYCLOPS_BODY_COLUMN, 0f);
    cyclopsAnimator.addAnimation(
        "charge_start", 0.073f, Animation.PlayMode.NORMAL, CYCLOPS_BODY_COLUMN, 0f);
    cyclopsAnimator.addAnimation("jump", 0.1f, Animation.PlayMode.LOOP, CYCLOPS_BODY_COLUMN, 0f);
    cyclopsAnimator.addAnimation(
        "attack",
        CyclopsAnimationController.ATTACK_FRAME_TIME,
        Animation.PlayMode.NORMAL,
        CYCLOPS_BODY_COLUMN,
        0f);
    Entity cyclops =
        new Entity()
            .addComponent(cyclopsAnimator)
            .addComponent(
                new CyclopsAnimationController(minigamePlayer, middle.y + STATUE_DEPTH_OFFSET / 2f))
            .addComponent(new CyclopsSoundComponent());
    cyclopsAnimator.scaleEntity();
    cyclops.scaleWidth(CYCLOPS_FRAME_WIDTH);
    Texture rockImage =
        ServiceLocator.getResourceService().getAsset(CAVE_ROCK_PLATFORM, Texture.class);
    float rockPixelSize = CAVE_ROCK_WIDTH / rockImage.getWidth();
    float rockTop = rock.getPosition().y + rock.getScale().y;
    cyclops.setPosition(
        middle.x - CYCLOPS_FRAME_WIDTH / 2f,
        rockTop - SLEEPING_CYCLOPS_SINK_PIXELS * rockPixelSize);
    spawnEntity(cyclops);
    return cyclops;
  }

  /**
   * Draws the cave floor's ground strip across the room with its top edge on the floor line, and
   * adds the physics floor.
   */
  private void displayFloor() {
    float roomWidth = terrain.tileToWorldPosition(MAP_SIZE.x, 0).x;
    float floorY = terrain.tileToWorldPosition(0, statueYLevel).y;

    Texture floorImage =
        ServiceLocator.getResourceService().getAsset(CAVE_FLOOR_TEXTURE, Texture.class);
    TextureRegion ground =
        new TextureRegion(
            floorImage,
            0,
            CAVE_FLOOR_GROUND_TOP_ROW,
            floorImage.getWidth(),
            CAVE_FLOOR_GROUND_ROWS);
    Texture formationImage =
        ServiceLocator.getResourceService().getAsset(CAVE_FORMATION_1, Texture.class);
    float worldPerPixel = formationHeight / formationImage.getHeight();
    float tileWidth = floorImage.getWidth() * worldPerPixel;
    float stripHeight = CAVE_FLOOR_GROUND_ROWS * worldPerPixel;
    TextureRegion darkestRow =
        new TextureRegion(
            floorImage,
            0,
            CAVE_FLOOR_GROUND_TOP_ROW + CAVE_FLOOR_GROUND_ROWS - 1,
            floorImage.getWidth(),
            1);
    float depth = 2f * cameraComponent.getCamera().viewportHeight;
    Entity floor =
        new Entity()
            .addComponent(
                new CyclopsFloorRenderComponent(
                    ground, darkestRow, tileWidth, stripHeight, roomWidth, depth));
    // Two texture pixels above the feet, so the ground overlaps them and nothing shows between.
    floor.setPosition(0f, floorY + STATUE_DEPTH_OFFSET + 2f * worldPerPixel);
    spawnEntity(floor);

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
        ServiceLocator.getResourceService().getAsset(HURT_PLAYER_FOUR_SOUND, Sound.class);
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
    Sound[] voices = new Sound[HURT_VOICE_PATH_BY_HEARTS.size()];
    for (int i = 0; i < HURT_VOICE_PATH_BY_HEARTS.size(); i++) {
      voices[i] =
          ServiceLocator.getResourceService()
              .getAsset(HURT_VOICE_PATH_BY_HEARTS.get(i), Sound.class);
    }
    return voices;
  }

  private void playMusic() {
    playLoopingMusic(CAVE_BACKGROUND_MUSIC, 0.4f);
  }

  private void loadAssets() {
    loadAreaAssets(
        cyclopsMinigameTextures,
        cyclopsMinigameTexturesAtlases,
        cyclopsMinigameSounds,
        cyclopsMinigameMusic);
  }

  private void unloadAssets() {
    unloadAreaAssets(
        cyclopsMinigameTextures,
        cyclopsMinigameTexturesAtlases,
        cyclopsMinigameSounds,
        cyclopsMinigameMusic);
  }

  @Override
  public void dispose() {
    super.dispose();
    ServiceLocator.getResourceService().getAsset(CAVE_BACKGROUND_MUSIC, Music.class).stop();
    this.unloadAssets();
  }
}
