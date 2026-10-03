package com.csse3200.game.rendering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.csse3200.game.components.BurnStatsComponent;
import com.csse3200.game.components.PoisonStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
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

  @Test
  void shouldRegisterAndUnregisterWithRenderService() {
    try (MockedConstruction<ShapeRenderer> renderers = mockConstruction(ShapeRenderer.class)) {
      ParticleEffectsRenderingComponent component = new ParticleEffectsRenderingComponent();
      Entity entity = new Entity().addComponent(component);

      entity.create();
      component.dispose();

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
    ServiceLocator.registerTimeSource(gameTime);
    when(batch.getProjectionMatrix()).thenReturn(new Matrix4());
    when(gameTime.getTime()).thenReturn(1000L);

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

      verify(batch).end();
      verify(batch).begin();
      verify(renderers.constructed().get(0)).begin(ShapeRenderer.ShapeType.Filled);
      verify(renderers.constructed().get(0), times(PARTICLE_SEGMENTS))
          .rect(
              org.mockito.ArgumentMatchers.anyFloat(),
              org.mockito.ArgumentMatchers.anyFloat(),
              org.mockito.ArgumentMatchers.anyFloat(),
              org.mockito.ArgumentMatchers.anyFloat());
      component.dispose();
    }
  }

  @Test
  void shouldDrawPoisonParticlesWhenEntityIsPoisoned() {
    ServiceLocator.registerTimeSource(gameTime);
    when(batch.getProjectionMatrix()).thenReturn(new Matrix4());
    when(gameTime.getTime()).thenReturn(1000L);

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
}
