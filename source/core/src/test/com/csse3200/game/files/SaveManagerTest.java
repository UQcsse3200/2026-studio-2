package com.csse3200.game.files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class SaveManagerTest {
  private Path tempDir;

  @BeforeEach
  void setUp() throws IOException {
    tempDir = SaveTestSupport.createTempDirectory("odysseus-saves-test");
    SaveTestSupport.useDirectory(tempDir);
  }

  @AfterEach
  void tearDown() throws IOException {
    SaveTestSupport.restoreDefaults();
    SaveTestSupport.deleteTempDirectory(tempDir);
  }

  @Test
  void shouldTreatEverySlotAsEmptyWhenNoFilesExist() {
    SaveFile[] saves = SaveManager.listAll();

    assertEquals(SaveManager.SLOT_COUNT, saves.length);
    for (SaveFile save : saves) {
      assertNull(save);
    }
    assertEquals(0, SaveManager.findEmptySlot(saves));
  }

  @Test
  void shouldRoundTripSaveFileThroughDisk() {
    SaveFile save = SaveManager.newGameSave(1);
    save.player = new SaveFile.PlayerSaveData();
    save.player.health = 7;
    save.player.maxHealth = 12;
    save.player.gold = 250;
    save.player.slotCount = 24;
    save.player.inventory.add(new SaveFile.ItemSave(3, ItemType.STANDARD_ARROW.name(), 5));
    save.activatedCheckpoints.add(new SaveFile.CheckpointSave(47, 22));

    SaveManager.write(1, save);
    SaveFile loaded = SaveManager.read(1);

    assertNotNull(loaded);
    assertEquals(1, loaded.slotIndex);
    assertEquals("level1", loaded.currentLevel);
    assertTrue(loaded.lastModifiedEpochMs > 0);
    assertEquals(7, loaded.player.health);
    assertEquals(12, loaded.player.maxHealth);
    assertEquals(250, loaded.player.gold);
    assertEquals(24, loaded.player.slotCount);
    assertEquals(1, loaded.player.inventory.size());
    assertEquals(3, loaded.player.inventory.get(0).index);
    assertEquals(ItemType.STANDARD_ARROW.name(), loaded.player.inventory.get(0).itemType);
    assertEquals(5, loaded.player.inventory.get(0).quantity);
    assertEquals(1, loaded.activatedCheckpoints.size());
    assertEquals(47, loaded.activatedCheckpoints.get(0).x);
    assertEquals(22, loaded.activatedCheckpoints.get(0).y);
  }

  @Test
  void shouldReturnNullForCorruptFile() throws IOException {
    Files.writeString(tempDir.resolve("slot_0.json"), "{ this is not valid json");

    assertNull(SaveManager.read(0));
  }

  @Test
  void shouldFindFirstEmptySlot() {
    SaveManager.write(0, SaveManager.newGameSave(0));

    assertEquals(1, SaveManager.findEmptySlot(SaveManager.listAll()));

    SaveManager.write(1, SaveManager.newGameSave(1));
    SaveManager.write(2, SaveManager.newGameSave(2));

    assertEquals(-1, SaveManager.findEmptySlot(SaveManager.listAll()));
  }

  @Test
  void shouldDeleteSlotFile() {
    SaveManager.write(2, SaveManager.newGameSave(2));
    assertNotNull(SaveManager.read(2));

    SaveManager.delete(2);

    assertNull(SaveManager.read(2));
    // Deleting an already-empty slot is a harmless no-op.
    SaveManager.delete(2);
  }

  @Test
  void shouldCapturePlayerAndCheckpointsAndPreserveCompletion() {
    SaveFile previous = SaveManager.newGameSave(0);
    previous.completed = true;
    SaveManager.write(0, previous);

    Entity player =
        new Entity()
            .addComponent(new CombatStatsComponent(7, 12, 3))
            .addComponent(new InventoryComponent(250));
    player.getComponent(InventoryComponent.class).addItem(ItemType.STANDARD_ARROW, 5);

    SaveFile captured = SaveManager.capture(0, "level2", player, List.of(new GridPoint2(47, 22)));

    assertTrue(captured.completed);
    assertEquals(0, captured.slotIndex);
    assertEquals("level2", captured.currentLevel);
    assertEquals(7, captured.player.health);
    assertEquals(12, captured.player.maxHealth);
    assertEquals(250, captured.player.gold);
    assertEquals(1, captured.player.inventory.size());
    assertEquals(ItemType.STANDARD_ARROW.name(), captured.player.inventory.get(0).itemType);
    assertEquals(1, captured.activatedCheckpoints.size());
    assertEquals(47, captured.activatedCheckpoints.get(0).x);

    // A null player (a new game with no started level) is safe to capture.
    assertNull(SaveManager.capture(1, "level1", null, List.of()).player);
  }

  @Test
  void shouldPreserveTutorialCompletedAcrossCaptures() {
    SaveFile previous = SaveManager.newGameSave(0);
    previous.tutorialCompleted = true;
    SaveManager.write(0, previous);

    // An autosave must not make the movement tutorial replay for a save that has seen it.
    SaveFile captured = SaveManager.capture(0, "level1", null, List.of());

    assertTrue(captured.tutorialCompleted);
    // A brand new save starts with the tutorial unseen.
    assertFalse(SaveManager.newGameSave(1).tutorialCompleted);
  }

  @Test
  void shouldCaptureAndApplyPlayerStateRoundTrip() {
    Entity player =
        new Entity()
            .addComponent(new CombatStatsComponent(7, 12, 3))
            .addComponent(new InventoryComponent(250));
    player.getComponent(InventoryComponent.class).addItem(ItemType.STANDARD_ARROW, 5);

    SaveFile.PlayerSaveData data = SaveFile.PlayerSaveData.capture(player);

    Entity restored =
        new Entity()
            .addComponent(new CombatStatsComponent(10, 10, 1))
            .addComponent(new InventoryComponent(0));
    data.applyTo(restored);

    CombatStatsComponent stats = restored.getComponent(CombatStatsComponent.class);
    InventoryComponent inventory = restored.getComponent(InventoryComponent.class);
    assertEquals(7, stats.getHealth());
    assertEquals(12, stats.getMaxHealth());
    assertEquals(250, inventory.getGold());
    assertEquals(5, inventory.getItemCount(ItemType.STANDARD_ARROW));
  }

  @Test
  void shouldClampSavedHealthToAtLeastOne() {
    Entity player =
        new Entity()
            .addComponent(new CombatStatsComponent(0, 10, 1))
            .addComponent(new InventoryComponent(0));

    SaveFile.PlayerSaveData data = SaveFile.PlayerSaveData.capture(player);

    assertEquals(1, data.health);
  }
}
