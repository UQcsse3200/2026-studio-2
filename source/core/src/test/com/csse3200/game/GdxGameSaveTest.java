package com.csse3200.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.areas.terrain.configs.LevelConfig;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.level.CheckpointComponent;
import com.csse3200.game.components.level.RespawnComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.files.SaveFile;
import com.csse3200.game.files.SaveManager;
import com.csse3200.game.files.SaveTestSupport;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class GdxGameSaveTest {
  private Path tempDir;
  private GdxGame game;

  @BeforeEach
  void setUp() throws IOException {
    tempDir = SaveTestSupport.createTempDirectory("odysseus-gdxgame-test");
    SaveTestSupport.useDirectory(tempDir);

    // The screen transitions themselves are covered elsewhere; stub them so the save flow can be
    // tested without creating real screens.
    game = spy(new GdxGame());
    doNothing().when(game).transitionTo(any(GdxGame.ScreenType.class));
    doNothing().when(game).startInitialCutscene();
  }

  @AfterEach
  void tearDown() throws IOException {
    SaveTestSupport.restoreDefaults();
    SaveTestSupport.deleteTempDirectory(tempDir);
  }

  @Test
  void shouldCreateSaveAndStartNewGameWhenSlotsAreEmpty() {
    game.startNewGame();

    assertEquals(0, game.getActiveSaveSlot());
    SaveFile save = SaveManager.read(0);
    assertNotNull(save);
    assertEquals("level1", save.currentLevel);
    assertNull(save.player);
    verify(game).startInitialCutscene();
    verify(game, never()).transitionTo(any(GdxGame.ScreenType.class));
  }

  @Test
  void shouldOpenSaveSelectWhenEverySlotIsFull() {
    for (int slot = 0; slot < SaveManager.SLOT_COUNT; slot++) {
      SaveManager.write(slot, SaveManager.newGameSave(slot));
    }

    game.startNewGame();

    assertEquals(-1, game.getActiveSaveSlot());
    verify(game).transitionTo(GdxGame.ScreenType.LOAD_SAVE);
    verify(game, never()).startInitialCutscene();
  }

  @Test
  void shouldLoadSaveAndApplyItsStateOnceForItsLevel() {
    SaveFile save = new SaveFile();
    save.currentLevel = "level2";
    save.player = new SaveFile.PlayerSaveData();
    save.player.health = 6;
    save.player.maxHealth = 10;
    save.player.gold = 99;
    save.activatedCheckpoints.add(new SaveFile.CheckpointSave(8, 9));
    SaveManager.write(1, save);

    game.loadSave(1);

    verify(game).transitionTo(GdxGame.ScreenType.LEVEL_2_GAME);
    assertEquals(1, game.getActiveSaveSlot());

    Entity player =
        new Entity()
            .addComponent(new CombatStatsComponent(10, 10, 1))
            .addComponent(new InventoryComponent(0))
            .addComponent(new RespawnComponent());
    game.applyPendingSavePlayer(player);
    assertEquals(6, player.getComponent(CombatStatsComponent.class).getHealth());
    assertEquals(99, player.getComponent(InventoryComponent.class).getGold());

    // Level 1 is not the save's level: nothing applied, and the save is kept for the swap.
    TestGameArea wrongLevel = new TestGameArea();
    assertFalse(game.applyPendingSaveWorld(wrongLevel, "level1"));

    TestGameArea level2 = new TestGameArea();
    CheckpointComponent checkpoint = level2.addCheckpoint(new GridPoint2(8, 9));
    assertTrue(game.applyPendingSaveWorld(level2, "level2"));
    assertTrue(checkpoint.isActive());
    // Consumed: applying again does nothing.
    assertFalse(game.applyPendingSaveWorld(level2, "level2"));
  }

  @Test
  void shouldApplyCheckpointlessSaveWithoutRespawning() {
    SaveFile save = new SaveFile();
    save.currentLevel = "level1";
    SaveManager.write(0, save);

    game.loadSave(0);

    // This area has no player spawn, so a respawn attempt would fail the test.
    TestGameArea level1 = new TestGameArea(null);
    assertTrue(game.applyPendingSaveWorld(level1, "level1"));
    assertFalse(game.applyPendingSaveWorld(level1, "level1"));
  }

  @Test
  void shouldRejectUnknownLevelAndLoadLevelOne() {
    SaveFile save = new SaveFile();
    save.currentLevel = "somewhere-else";
    SaveManager.write(0, save);

    game.loadSave(0);

    verify(game).transitionTo(GdxGame.ScreenType.LEVEL_1_GAME);
    assertEquals(0, game.getActiveSaveSlot());
  }

  @Test
  void shouldNotLoadAnEmptySlot() {
    game.loadSave(0);

    verify(game, never()).transitionTo(any(GdxGame.ScreenType.class));
    assertEquals(-1, game.getActiveSaveSlot());
  }

  @Test
  void shouldPersistTheTutorialPromptChoicePerSaveSlot() {
    game.startNewGame();
    assertTrue(game.areTutorialsAccepted());

    // Declining tut0's offer persists to the slot, so no prompt auto-plays for this save again.
    game.declineTutorials();
    assertFalse(game.areTutorialsAccepted());
    assertFalse(SaveManager.read(0).tutorialsAccepted);

    // Loading the slot restores whatever the file says, over the session default.
    SaveFile accepted = SaveManager.read(0);
    accepted.tutorialsAccepted = true;
    SaveManager.write(0, accepted);
    game.loadSave(0);
    assertTrue(game.areTutorialsAccepted());

    SaveFile declined = SaveManager.read(0);
    declined.tutorialsAccepted = false;
    SaveManager.write(0, declined);
    game.loadSave(0);
    assertFalse(game.areTutorialsAccepted());
  }

  /** Game area with a mocked level config and a player, for testing save restoration. */
  private static class TestGameArea extends GameArea {
    private final ArrayList<CheckpointComponent> checkpoints = new ArrayList<>();

    TestGameArea() {
      this(new GridPoint2(1, 4));
    }

    TestGameArea(GridPoint2 playerSpawn) {
      super(new CameraComponent());
      config = mock(LevelConfig.class);
      when(config.getPlayerSpawn()).thenReturn(playerSpawn);
      when(config.getCheckpoints()).thenReturn(checkpoints);
      player = new Entity().addComponent(new RespawnComponent());
    }

    @Override
    public void create() {
      // Test stub: no entities need to be spawned.
    }

    CheckpointComponent addCheckpoint(GridPoint2 position) {
      CheckpointComponent checkpoint =
          new Entity()
              .addComponent(new CheckpointComponent(false, position))
              .getComponent(CheckpointComponent.class);
      checkpoints.add(checkpoint);
      return checkpoint;
    }
  }
}
