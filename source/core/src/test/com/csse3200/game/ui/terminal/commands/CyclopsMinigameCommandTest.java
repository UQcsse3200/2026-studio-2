package com.csse3200.game.ui.terminal.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.csse3200.game.GdxGame;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class CyclopsMinigameCommandTest {
  @AfterEach
  void tearDown() {
    ServiceLocator.clear();
  }

  @Test
  void constructorRegistersEventHandlerWhenNoneExists() {
    assertTrue(ServiceLocator.getCyclopsMinigameEventHandler() == null);

    new CyclopsMinigameCommand(mock(GdxGame.class));

    assertNotNull(ServiceLocator.getCyclopsMinigameEventHandler());
  }

  @Test
  void loadSetsCyclopsMinigameScreen() {
    GdxGame game = mock(GdxGame.class);
    CyclopsMinigameCommand command = new CyclopsMinigameCommand(game);

    assertTrue(command.action(new ArrayList<>(List.of("load"))));

    verify(game).setScreen(GdxGame.ScreenType.CYCLOPS_MINIGAME);
  }

  @Test
  void commandsTriggerMatchingEvents() {
    EventHandler events = mock(EventHandler.class);
    ServiceLocator.registerCyclopsMinigameEventHandler(events);
    CyclopsMinigameCommand command = new CyclopsMinigameCommand(mock(GdxGame.class));

    assertTrue(command.action(new ArrayList<>(List.of("success"))));
    verify(events).trigger("success");
    assertTrue(command.action(new ArrayList<>(List.of("failure"))));
    verify(events).trigger("failure");
    assertTrue(command.action(new ArrayList<>(List.of("start"))));
    verify(events).trigger("start");
    assertTrue(command.action(new ArrayList<>(List.of("stop"))));
    verify(events).trigger("stop");
    assertTrue(command.action(new ArrayList<>(List.of("restart"))));
    verify(events).trigger("restart");
    assertTrue(command.action(new ArrayList<>(List.of("show"))));
    verify(events).trigger("showBar");
    assertTrue(command.action(new ArrayList<>(List.of("hide"))));
    verify(events).trigger("hideBar");
  }

  @Test
  void rejectsNoArgumentsTooManyArgumentsAndUnknownArguments() {
    ServiceLocator.registerCyclopsMinigameEventHandler(mock(EventHandler.class));
    CyclopsMinigameCommand command = new CyclopsMinigameCommand(mock(GdxGame.class));

    assertFalse(command.action(new ArrayList<>()));
    assertFalse(command.action(new ArrayList<>(List.of("load", "extra"))));
    assertFalse(command.action(new ArrayList<>(List.of("faliure"))));
  }
}
