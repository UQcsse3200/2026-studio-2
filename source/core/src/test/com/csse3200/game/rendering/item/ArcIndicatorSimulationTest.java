package com.csse3200.game.rendering.item;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

@ExtendWith(GameExtension.class)
class ArcIndicatorSimulationTest {
  private PhysicsService physics;
  private SpriteBatch batch;

  @BeforeEach
  void setUp() {
    physics = new PhysicsService();
    physics.getPhysics().getWorld().setGravity(Vector2.Zero);
    ServiceLocator.registerPhysicsService(physics);
    ServiceLocator.registerRenderService(mock(RenderService.class));
    batch = mock(SpriteBatch.class);
  }

  @AfterEach
  void disposePhysics() {
    physics.getPhysics().dispose();
  }

  private ArcIndicatorRenderComponent preview(float range) {
    ArcIndicatorRenderComponent preview =
        new ArcIndicatorRenderComponent() {
          protected boolean isActive() {
            return true;
          }

          protected Vector2 aimOffset() {
            return Vector2.X;
          }

          protected Vector2 launchPoint(Vector2 aim) {
            return entity.getCenterPosition();
          }

          protected float launchSpeed() {
            return 1f;
          }

          protected float maxRange() {
            return range;
          }
        };
    new Entity().addComponent(preview);
    preview.create();
    return preview;
  }

  @Test
  void simulationBudgetBoundsLongFlightsAndReusesTheRenderer() {
    ArcIndicatorRenderComponent preview = preview(Float.MAX_VALUE);
    try (var shapes = mockConstruction(ShapeRenderer.class)) {
      preview.render(batch);
      preview.render(batch);
      assertEquals(1, shapes.constructed().size());
      ShapeRenderer shape = shapes.constructed().getFirst();
      ArgumentCaptor<Float> endpointX = ArgumentCaptor.forClass(Float.class);
      verify(shape, times(2))
          .circle(
              endpointX.capture(),
              eq(0.5f),
              eq(ArcIndicatorRenderComponent.LANDING_DOT_RADIUS),
              eq(12));
      assertEquals(12.5f, endpointX.getValue(), 0.001f);
      verify(shape, times(800))
          .circle(anyFloat(), eq(0.5f), eq(ArcIndicatorRenderComponent.DOT_RADIUS), eq(12));
      verify(batch, times(2)).begin();
    }
  }

  @Test
  void aDrawReachStopsTheArcAtTheCrosshairWithoutALandingDot() {
    // Range would let it fly 2 units, but the crosshair is only 1 unit along the aim.
    ArcIndicatorRenderComponent preview =
        new ArcIndicatorRenderComponent() {
          protected boolean isActive() {
            return true;
          }

          protected Vector2 aimOffset() {
            return new Vector2(1f, 0f);
          }

          protected Vector2 launchPoint(Vector2 aim) {
            return entity.getCenterPosition();
          }

          protected float launchSpeed() {
            return 1f;
          }

          protected float maxRange() {
            return 2f;
          }

          @Override
          protected float maxDrawReach(Vector2 aimOffset) {
            return aimOffset.len();
          }
        };
    new Entity().addComponent(preview);
    preview.create();

    try (var shapes = mockConstruction(ShapeRenderer.class)) {
      preview.render(batch);
      ShapeRenderer shape = shapes.constructed().getFirst();
      verify(shape, never())
          .circle(
              anyFloat(), anyFloat(), eq(ArcIndicatorRenderComponent.LANDING_DOT_RADIUS), eq(12));
      ArgumentCaptor<Float> dotX = ArgumentCaptor.forClass(Float.class);
      verify(shape, atLeastOnce())
          .circle(dotX.capture(), eq(0.5f), eq(ArcIndicatorRenderComponent.DOT_RADIUS), eq(12));
      // The entity's centre is at x = 0.5, so the crosshair is at x = 1.5.
      for (float x : dotX.getAllValues()) {
        assertTrue(x <= 1.5f, "dot at x = " + x + " is past the crosshair");
      }
    }
  }

  @Test
  void nonSolidCollidersDoNotShortenThePreview() {
    assertEquals(
        2.51f, previewEndWithBlocker(PhysicsLayer.NPC, false, BodyDef.BodyType.StaticBody), 0.001f);
  }

  @Test
  void theWaterSurfaceDoesNotShortenThePreview() {
    // A sensor on a terrain layer: the grapple arrow passes through it, so the preview must too.
    assertEquals(
        2.51f,
        previewEndWithBlocker(PhysicsLayer.OBSTACLE, true, BodyDef.BodyType.StaticBody),
        0.001f);
  }

  @Test
  void spikyBallsDoNotShortenThePreview() {
    // A moving solid on a terrain layer: the grapple arrow passes through it, so the preview must
    // too.
    assertEquals(
        2.51f,
        previewEndWithBlocker(PhysicsLayer.OBSTACLE, false, BodyDef.BodyType.DynamicBody),
        0.001f);
  }

  @Test
  void solidTerrainStillShortensThePreview() {
    assertTrue(
        previewEndWithBlocker(PhysicsLayer.OBSTACLE, false, BodyDef.BodyType.StaticBody) < 1.5f);
  }

  /**
   * Puts a thin wall across the preview's path at x = 1.5 and reports where the preview's landing
   * dot ends up. With nothing in the way it reaches its range, at x = 2.51.
   */
  private float previewEndWithBlocker(short layer, boolean sensor, BodyDef.BodyType type) {
    BodyDef def = new BodyDef();
    def.type = type;
    Body body = physics.getPhysics().createBody(def);
    body.setGravityScale(0f);
    PolygonShape box = new PolygonShape();
    box.setAsBox(0.1f, 1f, new Vector2(1.5f, 0.5f), 0f);
    Fixture fixture = body.createFixture(box, 0f);
    box.dispose();
    fixture.setSensor(sensor);
    Filter filter = fixture.getFilterData();
    filter.categoryBits = layer;
    fixture.setFilterData(filter);
    try (var shapes = mockConstruction(ShapeRenderer.class)) {
      preview(2f).render(batch);
      ArgumentCaptor<Float> endpointX = ArgumentCaptor.forClass(Float.class);
      verify(shapes.constructed().getFirst())
          .circle(
              endpointX.capture(),
              eq(0.5f),
              eq(ArcIndicatorRenderComponent.LANDING_DOT_RADIUS),
              eq(12));
      return endpointX.getValue();
    }
  }
}
