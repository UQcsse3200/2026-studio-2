package com.csse3200.game.areas;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.item.ItemLabelDisplay;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.components.sandbox.MonsterSpawnerDisplay;
import com.csse3200.game.components.sandbox.SandboxEnemyType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.EnemyFactory;
import com.csse3200.game.entities.factories.ItemFactory;
import com.csse3200.game.entities.factories.ObstacleFactory;
import com.csse3200.game.entities.factories.PlayerFactory;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.math.GridPoint2Utils;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/** Minimal developer area for displaying every current item type. */
public class SandboxGameArea extends GameArea {
  // Deterministic left-to-right developer layout, measured in terrain tiles.
  private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(2, 2);
  private static final int GROUND_START_X = 0;
  private static final int GROUND_Y = 0;
  private static final int GROUND_LENGTH = 50;
  private static final int GROUND_PLATFORM_WIDTH = 2;
  private static final float GROUND_HEIGHT = 1f;
  private static final float GRAPPLE_PLATFORM_WIDTH = 3f;
  private static final float GRAPPLE_PLATFORM_HEIGHT = 1f;
  private static final int ALL_GRAPPLE_SIDES = 15;
  private static final GridPoint2[] GRAPPLE_PLATFORM_POSITIONS = {
    new GridPoint2(8, 4), new GridPoint2(12, 6), new GridPoint2(16, 8)
  };
  private static final float WALL_WIDTH = 0.1f;
  private static final int ITEM_START_X = 8;
  private static final int ITEM_Y = 2;
  private static final int ITEM_SPACING = 3;
  private static final int ITEM_QUANTITY = 99;
  private static final float ITEM_DISPLAY_HEIGHT = 1f;
  private static final GridPoint2 MONSTER_SPAWNER_NPC_POSITION = new GridPoint2(-4, 2);
  private static final GridPoint2 SPAWNED_MONSTER_POSITION = new GridPoint2(-8, 2);
  private static final float MONSTER_SPAWNER_NPC_WIDTH = 1.16f;
  private static final float MONSTER_SPAWNER_NPC_HEIGHT = 1.5f;
  private static final float SANDBOX_ACTIVE_CHASE_DISTANCE = 20f;
  private static final String TRANSPARENT_TEXTURE = "images/transparent.png";
  private static final String PLATFORM_TEXTURE = "images/platform.png";
  private static final String PLAYER_HEALTH_TEXTURE = "images/purple_heart.png";
  private static final String SKELETON_WARRIOR_TEXTURE = "images/skeleton_warrior.png";
  private static final String SKELETON_ARCHER_TEXTURE = "images/skeleton_archer.png";
  private static final String[] SANDBOX_ATLASES = {"images/player.atlas", "images/ghost.atlas"};
  private static final String[] SANDBOX_SOUNDS = {"sounds/Impact4.ogg"};

  private final TerrainFactory terrainFactory;
  private final String[] sandboxTextures;
  private Entity spawnedMonster;

  /**
   * Creates a sandbox area using the supplied terrain factory and camera.
   *
   * @param terrainFactory factory used to create the transparent terrain
   * @param cameraComponent active camera component
   */
  public SandboxGameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
    super(cameraComponent);

    this.terrainFactory = terrainFactory;
    sandboxTextures = getSandboxTextures();
  }

  @Override
  public void create() {
    loadAssets();
    spawnTerrain();
    spawnGround();
    spawnGrapplePlatforms();
    player = spawnPlayer();
    spawnMonsterSpawnerNpc();
    spawnItems();
  }

  /**
   * @return the controllable player after this area has been created
   */
  public Entity getPlayer() {
    return player;
  }

  static List<ItemType> getOrderedItemTypes() {
    return Arrays.stream(ItemType.values())
        .sorted(Comparator.comparingInt(ItemType::getId))
        .toList();
  }

  static GridPoint2 getPlayerSpawn() {
    return PLAYER_SPAWN.cpy();
  }

  static GridPoint2 getItemPosition(int index) {
    return new GridPoint2(ITEM_START_X + index * ITEM_SPACING, ITEM_Y);
  }

  private static String[] getSandboxTextures() {
    return Stream.concat(
            Stream.of(
                TRANSPARENT_TEXTURE,
                PLATFORM_TEXTURE,
                PLAYER_HEALTH_TEXTURE,
                SKELETON_WARRIOR_TEXTURE,
                SKELETON_ARCHER_TEXTURE),
            Arrays.stream(ItemType.values()).map(ItemType::getTexturePath))
        .distinct()
        .toArray(String[]::new);
  }

  private void loadAssets() {
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.loadTextures(sandboxTextures);
    resourceService.loadTextureAtlases(SANDBOX_ATLASES);
    resourceService.loadSounds(SANDBOX_SOUNDS);
    resourceService.loadAll();
  }

  private void spawnTerrain() {
    terrain = terrainFactory.createTerrain(TerrainType.BACKGROUND_DESERT);
    spawnEntity(new Entity().addComponent(terrain));

    float tileSize = terrain.getTileSize();
    GridPoint2 tileBounds = terrain.getMapBounds(0);
    Vector2 worldBounds = new Vector2(tileBounds.x * tileSize, tileBounds.y * tileSize);

    // Left wall
    // spawnEntityAt(
    //     ObstacleFactory.createWall(WALL_WIDTH, worldBounds.y), GridPoint2Utils.ZERO, false,
    // false);

    // Right wall
    /*
    spawnEntityAt(
        ObstacleFactory.createWall(
            WALL_WIDTH,
            worldBounds.y
        ),
        new GridPoint2(tileBounds.x, 0),
        false,
        false
    );
    */

    // Top wall
    spawnEntityAt(
        ObstacleFactory.createWall(worldBounds.x, WALL_WIDTH),
        new GridPoint2(0, tileBounds.y),
        false,
        false);

    // Bottom wall
    spawnEntityAt(
        ObstacleFactory.createWall(worldBounds.x, WALL_WIDTH), GridPoint2Utils.ZERO, false, false);
  }

  private void spawnGround() {
    for (int offset = -GROUND_LENGTH; offset < GROUND_LENGTH; offset += GROUND_PLATFORM_WIDTH) {
      spawnPlatform(
          0,
          GROUND_PLATFORM_WIDTH,
          GROUND_HEIGHT,
          new GridPoint2(GROUND_START_X + offset, GROUND_Y));
    }
  }

  private void spawnGrapplePlatforms() {
    for (GridPoint2 position : GRAPPLE_PLATFORM_POSITIONS) {
      spawnPlatform(ALL_GRAPPLE_SIDES, GRAPPLE_PLATFORM_WIDTH, GRAPPLE_PLATFORM_HEIGHT, position);
    }
  }

  private void spawnPlatform(int grappleSides, float width, float height, GridPoint2 position) {
    Entity platform = ObstacleFactory.createPlatform(grappleSides);
    platform.setScale(width, height);
    spawnEntityAt(platform, position, false, false);
  }

  private Entity spawnPlayer() {
    Entity newPlayer = PlayerFactory.createPlayer();
    newPlayer.getEvents().addListener("grappleRequested", this::checkSuccessfulGrapple);
    KeyboardPlayerInputComponent input = newPlayer.getComponent(KeyboardPlayerInputComponent.class);
    if (input != null) {
      input.setCameraComponent(cameraComponent);
    }
    spawnEntityAt(newPlayer, getPlayerSpawn(), true, true);
    return newPlayer;
  }

  private void spawnMonsterSpawnerNpc() {
    Entity spawnerNpc =
        new Entity()
            .addComponent(new MonsterSpawnerDisplay(cameraComponent, this::replaceSpawnedMonster));
    spawnerNpc.setScale(MONSTER_SPAWNER_NPC_WIDTH, MONSTER_SPAWNER_NPC_HEIGHT);
    spawnEntityAt(spawnerNpc, MONSTER_SPAWNER_NPC_POSITION, true, true);
  }

  private void replaceSpawnedMonster(SandboxEnemyType enemyType, boolean active) {
    removeSpawnedMonster();

    spawnedMonster = createSelectedMonster(enemyType, active);
    spawnEntityAt(spawnedMonster, SPAWNED_MONSTER_POSITION, true, true);
  }

  private Entity createSelectedMonster(SandboxEnemyType enemyType, boolean active) {
    if (!active) {
      return switch (enemyType) {
        case SKELETON_WARRIOR -> EnemyFactory.createPassiveSkeletonWarrior();
        case SKELETON_ARCHER -> EnemyFactory.createPassiveSkeletonArcher();
      };
    }

    return switch (enemyType) {
      case SKELETON_WARRIOR ->
          EnemyFactory.createSkeletonWarrior(
              player, SANDBOX_ACTIVE_CHASE_DISTANCE, SANDBOX_ACTIVE_CHASE_DISTANCE);
      case SKELETON_ARCHER ->
          EnemyFactory.createSkeletonArcher(
              player, SANDBOX_ACTIVE_CHASE_DISTANCE, SANDBOX_ACTIVE_CHASE_DISTANCE);
    };
  }

  private void removeSpawnedMonster() {
    if (spawnedMonster == null) {
      return;
    }

    areaEntities.remove(spawnedMonster);
    if (ServiceLocator.getEntityService().getEntities().contains(spawnedMonster, true)) {
      spawnedMonster.dispose();
    }
    spawnedMonster = null;
  }

  private void spawnItems() {
    List<ItemType> itemTypes = getOrderedItemTypes();
    for (int index = 0; index < itemTypes.size(); index++) {
      Entity item = ItemFactory.createItem(itemTypes.get(index), ITEM_QUANTITY);
      item.addComponent(new ItemLabelDisplay(cameraComponent));
      item.scaleHeight(ITEM_DISPLAY_HEIGHT);
      spawnEntityAt(item, getItemPosition(index), true, false);
    }
  }

  @Override
  public void dispose() {
    super.dispose();
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.unloadAssets(sandboxTextures);
    resourceService.unloadAssets(SANDBOX_ATLASES);
    resourceService.unloadAssets(SANDBOX_SOUNDS);
  }
}
