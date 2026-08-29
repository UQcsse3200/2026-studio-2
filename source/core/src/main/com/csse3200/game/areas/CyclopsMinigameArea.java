package com.csse3200.game.areas;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.components.minigames.CyclopsTimingBar.CyclopsMinigameLogicComponent;
import com.csse3200.game.components.minigames.CyclopsTimingBar.TimingBarDisplay;
import com.csse3200.game.components.minigames.CyclopsTimingBar.TimingBarLogic;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.PlayerFactory;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyclopsMinigameArea extends GameArea {
  private static final Logger logger = LoggerFactory.getLogger(CyclopsMinigameArea.class);
  private static final String[] cyclopsMinigameTextures = {
    "images/box_boy_leaf.png", "images/transparent.png"
  };

  private final TerrainFactory terrainFactory;
  private Entity timingMinigameEntity;
  private CyclopsMinigameLogicComponent minigameLogicComponent;

  private Entity player;
  private GridPoint2 startPosition = new GridPoint2(-10, -10);

  public CyclopsMinigameArea(TerrainFactory terrainFactory) {
    super();
    this.terrainFactory = terrainFactory;
  }

  /** Create the game area in the world. */
  @Override
  public void create() {
    loadAssets();

    displayUI();
    spawnTerrain();

    player = spawnPlayer();

    startTimingMinigame();
  }

  private void startTimingMinigame() {
    TimingBarLogic timingBarLogic = new TimingBarLogic(20f);
    TimingBarDisplay timingBarDisplay = new TimingBarDisplay(timingBarLogic);
    minigameLogicComponent =
        new CyclopsMinigameLogicComponent(timingBarLogic, timingBarDisplay, terrain, player);

    timingMinigameEntity = new Entity();
    timingMinigameEntity.addComponent(timingBarDisplay);
    timingMinigameEntity.addComponent(minigameLogicComponent);
    spawnEntity(timingMinigameEntity);

    minigameLogicComponent.startMinigame();
  }

  private void displayUI() {
    Entity ui = new Entity();
    ui.addComponent(new GameAreaDisplay("Cyclops Minigame Room"));
    spawnEntity(ui);
  }

  private void spawnTerrain() {
    // Background terrain
    terrain = terrainFactory.createTerrain(TerrainFactory.TerrainType.CYCLOPS_ROOM);
    spawnEntity(new Entity().addComponent(terrain));
  }

  private Entity spawnPlayer() {
    Entity newPlayer = PlayerFactory.createPlayerDisplay();
    spawnEntityAt(newPlayer, new GridPoint2(10, 10), true, true);
    return newPlayer;
  }

  private void loadAssets() {
    logger.debug("Loading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.loadTextures(cyclopsMinigameTextures);

    while (!resourceService.loadForMillis(10)) {
      logger.info("Loading... {}%", resourceService.getProgress());
    }
  }

  private void unloadAssets() {
    logger.debug("Unloading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.unloadAssets(cyclopsMinigameTextures);
  }

  @Override
  public void dispose() {
    super.dispose();
    this.unloadAssets();
  }
}
