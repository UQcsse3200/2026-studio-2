package com.csse3200.game.components.mainmenu;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.csse3200.game.GdxGame;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.files.SaveManager;
import com.csse3200.game.files.SaveTestSupport;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class LoadSaveActionsTest {
  private GdxGame game;
  private Entity ui;
  private Path tempDir;

  @BeforeEach
  void setUp() throws IOException {
    tempDir = SaveTestSupport.createTempDirectory("odysseus-loadsave-test");
    SaveTestSupport.useDirectory(tempDir);

    game = mock(GdxGame.class);
    ui = new Entity().addComponent(new LoadSaveActions(game));
    ui.create();
  }

  @AfterEach
  void tearDown() throws IOException {
    SaveTestSupport.restoreDefaults();
    SaveTestSupport.deleteTempDirectory(tempDir);
  }

  @Test
  void shouldLoadChosenSlot() {
    ui.getEvents().trigger("Load", 2);

    verify(game).loadSave(2);
  }

  @Test
  void shouldReturnToMainMenuOnBack() {
    ui.getEvents().trigger("Back");

    verify(game).setScreen(GdxGame.ScreenType.MAIN_MENU);
  }

  @Test
  void shouldDeleteSlotAndTellTheListToRefresh() {
    SaveManager.write(1, SaveManager.newGameSave(1));
    AtomicBoolean refreshed = new AtomicBoolean(false);
    ui.getEvents().addListener("SlotsChanged", () -> refreshed.set(true));

    ui.getEvents().trigger("Delete", 1);

    assertNull(SaveManager.read(1));
    assertTrue(refreshed.get());
  }
}
