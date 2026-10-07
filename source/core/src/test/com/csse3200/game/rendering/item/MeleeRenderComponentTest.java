package com.csse3200.game.rendering.item;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.item.weapons.melee.MeleeComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The swing arc is drawn with a ShapeRenderer, which needs a real graphics context, so these cover
 * the guards that decide whether anything is drawn at all. Those guards matter: drawing interrupts
 * the shared sprite batch with {@code batch.end()}, so a component that draws when it shouldn't
 * breaks rendering for every entity after it.
 */
@ExtendWith(GameExtension.class)
class MeleeRenderComponentTest {

  @BeforeEach
  void setUp() {
    ServiceLocator.registerRenderService(mock(RenderService.class));
    ServiceLocator.registerTimeSource(mock(GameTime.class));
    ServiceLocator.registerEntityService(mock(EntityService.class));

    PhysicsEngine physics = mock(PhysicsEngine.class);
    when(physics.raycastAll(any(Vector2.class), any(Vector2.class), anyShort()))
        .thenReturn(new RaycastHit[0]);
    ServiceLocator.registerPhysicsService(new PhysicsService(physics));
  }

  private Entity createPlayerWithMelee() {
    Entity player =
        new Entity()
            .addComponent(new MeleeComponent())
            .addComponent(new CombatStatsComponent(100, 20))
            .addComponent(new MeleeRenderComponent());
    player.create();
    return player;
  }

  @Test
  void shouldLeaveTheSpriteBatchAloneWhileNoSwingIsInProgress() {
    Entity player = createPlayerWithMelee();
    SpriteBatch batch = mock(SpriteBatch.class);

    player.getComponent(MeleeRenderComponent.class).render(batch);

    verifyNoInteractions(batch);
  }

  @Test
  void shouldLeaveTheSpriteBatchAloneWithoutAMeleeComponent() {
    Entity entity = new Entity().addComponent(new MeleeRenderComponent());
    entity.create();
    SpriteBatch batch = mock(SpriteBatch.class);

    entity.getComponent(MeleeRenderComponent.class).render(batch);

    verifyNoInteractions(batch);
  }

  @Test
  void shouldStopDrawingOnceTheSwingHasFinished() {
    Entity player = createPlayerWithMelee();
    GameTime time = mock(GameTime.class);
    ServiceLocator.registerTimeSource(time);
    MeleeComponent melee = player.getComponent(MeleeComponent.class);

    player.getEvents().trigger("meleeStart");
    // One step longer than the whole swing, so the component is past its last frame.
    when(time.getDeltaTime()).thenReturn(MeleeComponent.SWING_DURATION + 0.01f);
    melee.update();

    SpriteBatch batch = mock(SpriteBatch.class);
    player.getComponent(MeleeRenderComponent.class).render(batch);

    verifyNoInteractions(batch);
  }

  @Test
  void shouldDisposeCleanlyWhenNothingWasEverDrawn() {
    Entity player = createPlayerWithMelee();

    // The ShapeRenderer is only built on the first draw, so dispose has to cope with it being null.
    assertDoesNotThrow(() -> player.getComponent(MeleeRenderComponent.class).dispose());
  }
}
