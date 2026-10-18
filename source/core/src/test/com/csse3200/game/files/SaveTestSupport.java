package com.csse3200.game.files;

import com.csse3200.game.files.FileLoader.Location;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Test-only helper for redirecting {@link SaveManager} at a temporary directory, so save tests
 * never touch the real save folder in the user's home directory. Lives in the {@code files} package
 * because the override hooks ({@code SaveManager.saveDirectory} and {@code SaveManager.location})
 * are package-private.
 */
public final class SaveTestSupport {
  private SaveTestSupport() {
    throw new IllegalStateException("Instantiating static util class");
  }

  /** Creates a fresh temporary directory for save tests. */
  public static Path createTempDirectory(String prefix) throws IOException {
    return Files.createTempDirectory(prefix);
  }

  /** Points {@link SaveManager} at the given directory, using absolute file paths. */
  public static void useDirectory(Path directory) {
    SaveManager.saveDirectory = directory.toString();
    SaveManager.location = Location.ABSOLUTE;
  }

  /** Restores {@link SaveManager}'s real save location. */
  public static void restoreDefaults() {
    SaveManager.saveDirectory = SaveManager.DEFAULT_SAVE_DIR;
    SaveManager.location = Location.EXTERNAL;
  }

  /** Deletes every file in the temporary directory, then the directory itself. */
  public static void deleteTempDirectory(Path directory) throws IOException {
    try (Stream<Path> files = Files.list(directory)) {
      files.forEach(path -> path.toFile().delete());
    }
    Files.deleteIfExists(directory);
  }
}
