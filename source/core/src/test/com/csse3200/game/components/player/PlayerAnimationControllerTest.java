package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Covers the player's death-state animation handling: entering it, locking it in, and signalling
 * completion.
 */
@ExtendWith(GameExtension.class)
class PlayerAnimationControllerTest {

  @BeforeEach
  void beforeEach() {
    GameTime gameTime = mock(GameTime.class);
    when(gameTime.getDeltaTime()).thenReturn(1f);
    ServiceLocator.registerTimeSource(gameTime);

    EntityService entityService = mock(EntityService.class);
    when(entityService.getPaused()).thenReturn(false);
    ServiceLocator.registerEntityService(entityService);
  }

  private static TextureAtlas mockAtlasWithRegions(String... names) {
    TextureAtlas atlas = mock(TextureAtlas.class);
    for (String name : names) {
      Array<AtlasRegion> regions = new Array<>(1);
      regions.add(mock(AtlasRegion.class));
      when(atlas.findRegions(name)).thenReturn(regions);
    }
    return atlas;
  }

  /**
   * Builds a player-like entity with a single-frame "death" animation (so one draw() call completes
   * it) and wires up the controller without needing a registered RenderService.
   */
  private PlayerAnimationController createController(
      Entity entity, AnimationRenderComponent animator) {
    animator.addAnimation("idle", 1f, PlayMode.LOOP);
    animator.addAnimation("death", 1f, PlayMode.NORMAL);
    entity.addComponent(animator);
    PlayerAnimationController controller = new PlayerAnimationController();
    entity.addComponent(controller);
    controller.create();
    return controller;
  }

  @Test
  void shouldPlayDeathAnimationOnDeathEvent() {
    AnimationRenderComponent animator =
        new AnimationRenderComponent(mockAtlasWithRegions("idle", "death"));
    Entity entity = new Entity();
    createController(entity, animator);

    entity.getEvents().trigger("death");

    assertEquals("death", animator.getCurrentAnimation());
  }

  @Test
  void shouldIgnoreMovementAndHurtEventsAfterDeath() {
    AnimationRenderComponent animator =
        new AnimationRenderComponent(mockAtlasWithRegions("idle", "death"));
    Entity entity = new Entity();
    createController(entity, animator);

    entity.getEvents().trigger("death");
    entity.getEvents().trigger("walk", new Vector2(1f, 0f));
    entity.getEvents().trigger("sprint");
    entity.getEvents().trigger("jumpStart");
    entity.getEvents().trigger("hurt");

    // None of the above should have replaced the death animation.
    assertEquals("death", animator.getCurrentAnimation());
  }

  @Test
  void shouldNotFireDeathAnimationFinishedBeforeAnimationCompletes() {
    AnimationRenderComponent animator =
        new AnimationRenderComponent(mockAtlasWithRegions("idle", "death"));
    Entity entity = new Entity();
    PlayerAnimationController controller = createController(entity, animator);
    AtomicInteger finishedEvents = new AtomicInteger();
    entity.getEvents().addListener("deathAnimationFinished", finishedEvents::incrementAndGet);

    entity.getEvents().trigger("death");
    controller.update(); // No frame has been drawn yet, so the animation isn't finished.

    assertEquals(0, finishedEvents.get());
  }

  @Test
  void shouldFireDeathAnimationFinishedExactlyOnceWhenAnimationCompletes() {
    AnimationRenderComponent animator =
        new AnimationRenderComponent(mockAtlasWithRegions("idle", "death"));
    Entity entity = new Entity();
    PlayerAnimationController controller = createController(entity, animator);
    AtomicInteger finishedEvents = new AtomicInteger();
    entity.getEvents().addListener("deathAnimationFinished", finishedEvents::incrementAndGet);

    entity.getEvents().trigger("death");
    animator.render(mock(SpriteBatch.class)); // Advances past the single-frame death animation.

    controller.update();
    controller.update(); // Should not fire a second time.

    assertEquals(1, finishedEvents.get());
  }
}
