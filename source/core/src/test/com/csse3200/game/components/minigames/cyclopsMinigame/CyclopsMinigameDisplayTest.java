package com.csse3200.game.components.minigames.cyclopsMinigame;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.listeners.EventListener0;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

@ExtendWith(GameExtension.class)
class CyclopsMinigameDisplayTest {
  private Stage stage;

  @BeforeEach
  void setup() {
    stage = mock(Stage.class);
    RenderService renderService = mock(RenderService.class);
    when(renderService.getStage()).thenReturn(stage);
    ServiceLocator.registerRenderService(renderService);

    ResourceService resourceService = mock(ResourceService.class);
    when(resourceService.getAsset(anyString(), eq(Texture.class))).thenReturn(mock(Texture.class));
    ServiceLocator.registerResourceService(resourceService);
  }

  @AfterEach
  void tearDown() {
    ServiceLocator.clear();
  }

  @Test
  void createAddsTheExitTableToTheStage() {
    CyclopsMinigameDisplay display = new CyclopsMinigameDisplay();
    new Entity().addComponent(display);

    display.create();

    ArgumentCaptor<Actor> actors = ArgumentCaptor.forClass(Actor.class);
    verify(stage, atLeastOnce()).addActor(actors.capture());
    assertTrue(actors.getValue() instanceof Table);
  }

  @Test
  void clickingExitTriggersExitOnTheEntity() {
    CyclopsMinigameDisplay display = new CyclopsMinigameDisplay();
    Entity entity = new Entity().addComponent(display);
    EventListener0 exitListener = mock(EventListener0.class);
    entity.getEvents().addListener("exit", exitListener);

    display.create();

    ArgumentCaptor<Actor> actors = ArgumentCaptor.forClass(Actor.class);
    verify(stage).addActor(actors.capture());
    Table table = (Table) actors.getValue();
    ImageButton exitButton = (ImageButton) table.getCells().first().getActor();
    exitButton.fire(new ChangeListener.ChangeEvent());

    verify(exitListener).handle();
  }

  @Test
  void disposeAfterCreateDoesNotThrow() {
    CyclopsMinigameDisplay display = new CyclopsMinigameDisplay();
    new Entity().addComponent(display);
    display.create();

    assertDoesNotThrow(display::dispose);
  }
}
