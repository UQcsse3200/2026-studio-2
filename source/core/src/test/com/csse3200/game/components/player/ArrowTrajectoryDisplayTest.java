package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.item.weapons.bow.BowComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.input.InputService;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

/** The dotted arc previewing a combat arrow's flight while the bow is drawn. */
@ExtendWith(GameExtension.class)
class ArrowTrajectoryDisplayTest {
  private Entity player;
  private BowComponent bow;
  private KeyboardPlayerInputComponent input;
  private ArrowTrajectoryDisplay display;
  private List<Actor> dots;

  @BeforeEach
  void setUp() {
    ServiceLocator.registerPhysicsService(new PhysicsService());
    ServiceLocator.registerTimeSource(mock(GameTime.class));
    ServiceLocator.registerEntityService(mock(EntityService.class));
    ServiceLocator.registerInputService(mock(InputService.class));
    RenderService renderService = mock(RenderService.class);
    Stage stage = mock(Stage.class);
    when(renderService.getStage()).thenReturn(stage);
    ServiceLocator.registerRenderService(renderService);

    // Aim at a world point well out to the right of the player.
    Gdx.input = mock(Input.class);
    Camera camera = mock(Camera.class);
    when(camera.unproject(any(Vector3.class)))
        .thenAnswer(invocation -> ((Vector3) invocation.getArgument(0)).set(10f, 3f, 0f));

    bow = new BowComponent();
    input = new KeyboardPlayerInputComponent();
    display = new ArrowTrajectoryDisplay();
    player = new Entity().addComponent(bow).addComponent(input).addComponent(display);
    player.create();
    input.setCameraComponent(new CameraComponent(camera));

    ArgumentCaptor<Actor> added = ArgumentCaptor.forClass(Actor.class);
    verify(stage, atLeastOnce()).addActor(added.capture());
    dots = added.getAllValues();
  }

  /** Holds the shoot button and starts drawing the bow, as a right-click with an arrow does. */
  private void drawTheBow() {
    input.touchDown(0, 0, 0, Buttons.RIGHT);
    bow.startCharge(new Vector2(1f, 0f));
  }

  private boolean anyDotVisible() {
    display.draw(mock(SpriteBatch.class));
    return dots.stream().anyMatch(Actor::isVisible);
  }

  @Test
  void shouldShowTheArcWhileTheBowIsDrawn() {
    drawTheBow();

    assertTrue(anyDotVisible());
  }

  @Test
  void shouldHideTheArcWhenTheBowIsNotDrawn() {
    assertFalse(anyDotVisible());
  }

  @Test
  void shouldHideTheArcWhileDead() {
    drawTheBow();
    player.getEvents().trigger("death");

    assertFalse(anyDotVisible());
  }

  @Test
  void shouldShowTheArcAgainAfterRevivingAtACheckpoint() {
    player.getEvents().trigger("death");
    player.getEvents().trigger("revive");

    drawTheBow();

    // Regression: the arc latched off on death and never came back after a checkpoint restart.
    assertTrue(anyDotVisible());
  }
}
