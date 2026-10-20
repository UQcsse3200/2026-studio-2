package com.csse3200.game.files;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.files.FileLoader.Location;
import java.io.File;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reading, writing and listing save files, one JSON file per fixed slot in the user's home
 * directory (mirrors {@link UserSettings}, which stores settings the same way).
 *
 * <p>Slots live at {@code DECO2800Game/saves/slot_n.json}. A slot with no readable file is simply
 * empty; a corrupt file is treated the same way rather than failing the game.
 */
public class SaveManager {
  private static final Logger logger = LoggerFactory.getLogger(SaveManager.class);

  /** Number of fixed save slots shown on the save-select screen. */
  public static final int SLOT_COUNT = 3;

  static final String DEFAULT_SAVE_DIR = "DECO2800Game" + File.separator + "saves";

  /**
   * Directory holding the slot files (external: relative to the home directory). Package-private,
   * along with {@link #location}, so tests can point saves at a temporary folder.
   */
  static String saveDirectory = DEFAULT_SAVE_DIR;

  /** Where {@link #saveDirectory} is resolved from. */
  static Location location = Location.EXTERNAL;

  private SaveManager() {
    throw new IllegalStateException("Instantiating static util class");
  }

  /** Absolute-ish path (external location) of the given slot's file. */
  public static String slotPath(int slot) {
    return saveDirectory + File.separator + "slot_" + slot + ".json";
  }

  /**
   * Reads one slot's save.
   *
   * @param slot slot index
   * @return the save, or null when the slot is empty or the file cannot be parsed
   */
  public static SaveFile read(int slot) {
    return FileLoader.readClass(SaveFile.class, slotPath(slot), location);
  }

  /**
   * Reads every slot, in slot order. Empty or unreadable slots are null entries.
   *
   * @return an array of exactly {@link #SLOT_COUNT} entries
   */
  public static SaveFile[] listAll() {
    SaveFile[] saves = new SaveFile[SLOT_COUNT];
    for (int slot = 0; slot < SLOT_COUNT; slot++) {
      saves[slot] = read(slot);
    }
    return saves;
  }

  /**
   * Writes a save to its slot, stamping the last-modified time used by the save-select screen.
   *
   * @param slot slot index
   * @param save save contents (its slotIndex field is corrected here)
   */
  public static void write(int slot, SaveFile save) {
    save.slotIndex = slot;
    save.lastModifiedEpochMs = System.currentTimeMillis();
    FileLoader.writeClass(save, slotPath(slot), location);
    logger.debug("Saved slot {}", slot);
  }

  /**
   * Deletes a slot's file if it exists. No-op on an already empty slot.
   *
   * @param slot slot index
   */
  public static void delete(int slot) {
    FileHandle file =
        location == Location.ABSOLUTE
            ? Gdx.files.absolute(slotPath(slot))
            : Gdx.files.external(slotPath(slot));
    boolean deleted = file.delete();
    logger.info("Deleted save slot {} (existed: {})", slot, deleted);
  }

  /**
   * Finds the first slot without a save.
   *
   * @param saves result of {@link #listAll()}
   * @return the first empty slot index, or -1 when every slot is filled
   */
  public static int findEmptySlot(SaveFile[] saves) {
    for (int slot = 0; slot < Math.min(SLOT_COUNT, saves.length); slot++) {
      if (saves[slot] == null) {
        return slot;
      }
    }
    return -1;
  }

  /**
   * The initial contents of a brand-new game: level 1, no recorded player state (the level's
   * defaults apply) and no activated checkpoints. Written as soon as Play creates a game, so the
   * slot shows up on the save-select screen immediately.
   *
   * @param slot slot index
   * @return an empty save ready to be written
   */
  public static SaveFile newGameSave(int slot) {
    SaveFile save = new SaveFile();
    save.slotIndex = slot;
    save.currentLevel = "level1";
    return save;
  }

  /**
   * Builds a save from the live game state at a checkpoint activation.
   *
   * @param slot slot to capture into
   * @param level current level name ("level1".."boss")
   * @param player player entity to capture stats from (null-safe)
   * @param activeCheckpoints positions of activated checkpoints in this level
   * @return a save ready for {@link #write}, preserving any previous completion flag
   */
  public static SaveFile capture(
      int slot, String level, Entity player, List<GridPoint2> activeCheckpoints) {
    SaveFile save = new SaveFile();
    save.slotIndex = slot;
    save.currentLevel = level;
    save.player = SaveFile.PlayerSaveData.capture(player);
    save.activatedCheckpoints.clear();
    if (activeCheckpoints != null) {
      for (GridPoint2 position : activeCheckpoints) {
        save.activatedCheckpoints.add(new SaveFile.CheckpointSave(position.x, position.y));
      }
    }

    // Writing after a win must not clear the completion flag, and an autosave must not make
    // the movement tutorial replay for a save that has already seen it, nor silently re-enable
    // tutorials for a save whose owner declined them.
    SaveFile previous = read(slot);
    save.completed = previous != null && previous.completed;
    save.tutorialCompleted = previous != null && previous.tutorialCompleted;
    save.tutorialsAccepted = previous == null || previous.tutorialsAccepted;
    return save;
  }
}
