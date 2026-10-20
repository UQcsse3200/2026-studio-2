package com.csse3200.game.rendering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.csse3200.game.components.BurnStatsComponent;
import com.csse3200.game.components.PoisonStatsComponent;
import com.csse3200.game.components.SlowStatsComponent;
import com.csse3200.game.components.lighting.PointLightComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(GameExtension.class)
@ExtendWith(MockitoExtension.class)
class ParticleEffectsRenderingComponentTest {
  private static final int PARTICLE_SEGMENTS = 5 * 6;

  @Mock RenderService renderService;
  @Mock SpriteBatch batch;
  @Mock GameTime gameTime;

  @BeforeEach
  void setUp() {
    ServiceLocator.registerRenderService(renderService);
  }

  /** The clock and projection a draw needs; the clock reads a second in. */
  private void useClockAndProjection() {
    ServiceLocator.registerTimeSource(gameTime);
    when(batch.getProjectionMatrix()).thenReturn(new Matrix4());
    when(gameTime.getTime()).thenReturn(1000L);
  }

  private static void verifyRects(ShapeRenderer shapes, int count) {
    verify(shapes, times(count)).rect(anyFloat(), anyFloat(), anyFloat(), anyFloat());
  }

  private static void verifyRectAtHeight(ShapeRenderer shapes, float y) {
    verify(shapes, atLeastOnce()).rect(anyFloat(), eq(y), anyFloat(), anyFloat());
  }

  /** A single effect should have been drawn in one batch pass, at the entity's height. */
  private void verifyDrawnOneEffectAtHeight(ShapeRenderer shapes, float y) {
    verify(batch).end();
    verify(batch).begin();
    verify(shapes).begin(ShapeRenderer.ShapeType.Filled);
    verifyRects(shapes, PARTICLE_SEGMENTS);
    verifyRectAtHeight(shapes, y);
  }

  @Test
  void shouldRegisterAndUnregisterWithRenderService() {
    useClockAndProjection();

    try (MockedConstruction<ShapeRenderer> renderers = mockConstruction(ShapeRenderer.class)) {
      Entity entity =
          new Entity()
              .addComponent(new BurnStatsComponent())
              .addComponent(new ParticleEffectsRenderingComponent());
      entity.create();
      entity.getComponent(BurnStatsComponent.class).applyBurn(1f, 5f);

      ParticleEffectsRenderingComponent component =
          entity.getComponent(ParticleEffectsRenderingComponent.class);
      component.render(batch);
      component.dispose();

      assertEquals(1, renderers.constructed().size());
      verify(renderService).register(component);
      verify(renderService).unregister(component);
      verify(renderers.constructed().get(0)).dispose();
    }
  }

  @Test
  void shouldNotDrawWhenEntityHasNoActiveEffect() {
    try (MockedConstruction<ShapeRenderer> renderers = mockConstruction(ShapeRenderer.class)) {
      Entity entity = new Entity().addComponent(new ParticleEffectsRenderingComponent());
      ParticleEffectsRenderingComponent component =
          entity.getComponent(ParticleEffectsRenderingComponent.class);
      entity.create();

      component.render(batch);

      verify(batch, never()).end();
      component.dispose();
    }
  }

  @Test
  void shouldDrawFireParticlesWhenEntityIsBurning() {
    useClockAndProjection();

    try (MockedConstruction<ShapeRenderer> renderers = mockConstruction(ShapeRenderer.class)) {
      Entity entity =
          new Entity()
              .addComponent(new BurnStatsComponent())
              .addComponent(new ParticleEffectsRenderingComponent());
      entity.setPosition(0f, 4f);
      entity.create();
      entity.getComponent(BurnStatsComponent.class).applyBurn(1f, 5f);

      ParticleEffectsRenderingComponent component =
          entity.getComponent(ParticleEffectsRenderingComponent.class);
      component.render(batch);

      verifyDrawnOneEffectAtHeight(renderers.constructed().get(0), 4f);
      component.dispose();
    }
  }

  @Test
  void shouldDrawPoisonParticlesWhenEntityIsPoisoned() {
    useClockAndProjection();

    try (MockedConstruction<ShapeRenderer> renderers = mockConstruction(ShapeRenderer.class)) {
      Entity entity =
          new Entity()
              .addComponent(new PoisonStatsComponent())
              .addComponent(new ParticleEffectsRenderingComponent());
      entity.create();
      entity.getEvents().trigger("applyPoison", 1f, 5f);

      ParticleEffectsRenderingComponent component =
          entity.getComponent(ParticleEffectsRenderingComponent.class);
      component.render(batch);

      verify(batch).end();
      verify(renderers.constructed().get(0)).begin(ShapeRenderer.ShapeType.Filled);
      component.dispose();
    }
  }

  @Test
  void shouldDrawIceParticlesWhenEntityIsSlowed() {
    useClockAndProjection();

    try (MockedConstruction<ShapeRenderer> renderers = mockConstruction(ShapeRenderer.class)) {
      Entity entity =
          new Entity()
              .addComponent(new SlowStatsComponent())
              .addComponent(new PhysicsMovementComponent())
              .addComponent(new ParticleEffectsRenderingComponent());
      entity.setPosition(0f, 4f);
      entity.create();
      entity.getEvents().trigger("applySlow", 0.5f, 5f);

      ParticleEffectsRenderingComponent component =
          entity.getComponent(ParticleEffectsRenderingComponent.class);
      component.render(batch);

      verifyDrawnOneEffectAtHeight(renderers.constructed().get(0), 4f);
      component.dispose();
    }
  }

  @Test
  void shouldNotDrawIceParticlesWhenEntityIsNotSlowed() {
    ServiceLocator.registerTimeSource(gameTime);

    Entity entity =
        new Entity()
            .addComponent(new SlowStatsComponent())
            .addComponent(new PhysicsMovementComponent())
            .addComponent(new ParticleEffectsRenderingComponent());
    entity.create();

    ParticleEffectsRenderingComponent component =
        entity.getComponent(ParticleEffectsRenderingComponent.class);
    component.render(batch);

    verify(batch, never()).end();
    component.dispose();
  }

  @Test
  void shouldDrawFireAndIceParticlesWhenEntityIsBurningAndSlowed() {
    useClockAndProjection();

    try (MockedConstruction<ShapeRenderer> renderers = mockConstruction(ShapeRenderer.class)) {
      Entity entity =
          new Entity()
              .addComponent(new BurnStatsComponent())
              .addComponent(new SlowStatsComponent())
              .addComponent(new PhysicsMovementComponent())
              .addComponent(new ParticleEffectsRenderingComponent());
      entity.create();
      entity.getComponent(BurnStatsComponent.class).applyBurn(1f, 5f);
      entity.getEvents().trigger("applySlow", 0.5f, 5f);

      ParticleEffectsRenderingComponent component =
          entity.getComponent(ParticleEffectsRenderingComponent.class);
      component.render(batch);

      verify(batch).end();
      verify(batch).begin();
      verifyRects(renderers.constructed().get(0), PARTICLE_SEGMENTS * 2);
      component.dispose();
    }
  }

  @Test
  void shouldRenderSlightlyInFrontOfEntity() {
    try (MockedConstruction<ShapeRenderer> renderers = mockConstruction(ShapeRenderer.class)) {
      Entity entity = new Entity().addComponent(new ParticleEffectsRenderingComponent());
      entity.setPosition(0f, 4f);
      entity.create();

      ParticleEffectsRenderingComponent component =
          entity.getComponent(ParticleEffectsRenderingComponent.class);

      assertEquals(-3.99f, component.getZIndex(), 0.0001f);
      component.dispose();
    }
  }

  @Test
  void shouldEnableAndColourTheLightOnlyWhileAnEffectIsActive() {
    useClockAndProjection();
    PointLightComponent light = mock(PointLightComponent.class);

    try (MockedConstruction<ShapeRenderer> renderers = mockConstruction(ShapeRenderer.class)) {
      BurnStatsComponent burn = new BurnStatsComponent();
      Entity entity =
          new Entity()
              .addComponent(burn)
              .addComponent(light)
              .addComponent(new ParticleEffectsRenderingComponent());
      entity.create();
      ParticleEffectsRenderingComponent component =
          entity.getComponent(ParticleEffectsRenderingComponent.class);

      component.render(batch);
      burn.applyBurn(1f, 5f);
      component.render(batch);

      verify(light).setActive(false);
      verify(light).setActive(true);
      verify(light).setColor(argThat(color -> color.equals(new Color(0.55f, 0.05f, 0.02f, 1f))));
      component.dispose();
    }
  }
}
