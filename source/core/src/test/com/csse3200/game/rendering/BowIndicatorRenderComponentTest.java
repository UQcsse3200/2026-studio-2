package com.csse3200.game.rendering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.csse3200.game.components.item.weapons.bow.BowComponent;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.components.projectile.ArrowType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.item.BowIndicatorRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;

/** Verifies the bow's dotted arc preview follows the draw, without creating an OpenGL context. */
@ExtendWith(GameExtension.class)
class BowIndicatorRenderComponentTest {

  // The arrow spawns this far in front of the player's centre, so it has that much less to cover.
  private static final float SPAWN_GAP = 0.8f;

  private RenderService renderService;
  private PhysicsService physicsService;
  private SpriteBatch batch;
  private BowComponent bow;
  private KeyboardPlayerInputComponent input;

  @BeforeEach
  void setUp() {
    renderService = mock(RenderService.class);
    ServiceLocator.registerRenderService(renderService);
    physicsService = new PhysicsService();
    ServiceLocator.registerPhysicsService(physicsService);

    batch = mock(SpriteBatch.class);
    when(batch.getProjectionMatrix()).thenReturn(new Matrix4());

    bow = mock(BowComponent.class);
    input = mock(KeyboardPlayerInputComponent.class);
    when(bow.isCharging()).thenReturn(true);
    when(bow.getArrowType()).thenReturn(ArrowType.STANDARD);
    when(bow.currentSpeedMultiplier()).thenReturn(1f);
    when(input.getMouseAimDirection()).thenReturn(new Vector2(10f, 0f));
  }

  @AfterEach
  void tearDown() {
    physicsService.getPhysics().dispose();
  }

  private BowIndicatorRenderComponent renderer(
      BowComponent bowComponent, KeyboardPlayerInputComponent inputComponent) {
    Entity entity = new Entity();
    if (bowComponent != null) {
      entity.addComponent(bowComponent);
    }
    if (inputComponent != null) {
      entity.addComponent(inputComponent);
    }
    BowIndicatorRenderComponent renderer = new BowIndicatorRenderComponent();
    entity.addComponent(renderer);
    renderer.create();
    return renderer;
  }

  /** A tall wall whose near face is {@code distance} ahead of the player, however far it drops. */
  private void createWallAhead(Vector2 origin, float distance) {
    createWallAhead(origin, distance, PhysicsLayer.GROUND);
  }

  private void createWallAhead(Vector2 origin, float distance, short layer) {
    BodyDef def = new BodyDef();
    def.position.set(origin.x + distance + 0.5f, origin.y - 20f);
    Body wall = physicsService.getPhysics().createBody(def);
    PolygonShape shape = new PolygonShape();
    shape.setAsBox(0.5f, 40f);
    Fixture fixture = wall.createFixture(shape, 0f);
    shape.dispose();
    Filter filter = fixture.getFilterData();
    filter.categoryBits = layer;
    fixture.setFilterData(filter);
  }

  /** Renders once and returns where the arc's landing dot was drawn. */
  private Vector2 renderAndGetLanding(BowIndicatorRenderComponent renderer) {
    try (MockedConstruction<ShapeRenderer> shapes = mockConstruction(ShapeRenderer.class)) {
      renderer.render(batch);
      ShapeRenderer shape = shapes.constructed().getFirst();
      ArgumentCaptor<Float> xCaptor = ArgumentCaptor.forClass(Float.class);
      ArgumentCaptor<Float> yCaptor = ArgumentCaptor.forClass(Float.class);
      verify(shape).circle(xCaptor.capture(), yCaptor.capture(), eq(0.15f), eq(12));
      return new Vector2(xCaptor.getValue(), yCaptor.getValue());
    }
  }

  @Test
  void shouldLeaveSpriteBatchAloneWhenBowIsMissing() {
    BowIndicatorRenderComponent renderer = renderer(null, input);
    renderer.render(batch);
    verifyNoInteractions(batch);
  }

  @Test
  void shouldLeaveSpriteBatchAloneWhenNotDrawing() {
    when(bow.isCharging()).thenReturn(false);
    BowIndicatorRenderComponent renderer = renderer(bow, input);
    renderer.render(batch);
    verifyNoInteractions(batch);
  }

  @Test
  void shouldLeaveSpriteBatchAloneWhenAimIsUnavailable() {
    when(input.getMouseAimDirection()).thenReturn(null);
    BowIndicatorRenderComponent renderer = renderer(bow, input);
    renderer.render(batch);
    verifyNoInteractions(batch);
  }

  @Test
  void shouldLeaveSpriteBatchAloneWhenAimIsZero() {
    when(input.getMouseAimDirection()).thenReturn(Vector2.Zero.cpy());
    BowIndicatorRenderComponent renderer = renderer(bow, input);
    renderer.render(batch);
    verifyNoInteractions(batch);
  }

  @Test
  void shouldDrawWhiteDottedArcEndingOnSolidSurface() {
    // A fully drawn standard arrow: 18 base speed * 1.5 charge = 27.
    when(bow.currentSpeedMultiplier()).thenReturn(1.5f);
    BowIndicatorRenderComponent renderer = renderer(bow, input);
    Vector2 origin = renderer.getEntity().getCenterPosition();
    createWallAhead(origin, 10f);

    try (MockedConstruction<ShapeRenderer> shapes = mockConstruction(ShapeRenderer.class)) {
      renderer.render(batch);
      assertEquals(1, shapes.constructed().size());
      ShapeRenderer shape = shapes.constructed().getFirst();
      verify(shape).setColor(Color.WHITE);
      // Small dots along the arc, plus one bigger dot where it lands.
      verify(shape, atLeastOnce()).circle(anyFloat(), anyFloat(), eq(0.05f), eq(12));

      ArgumentCaptor<Float> xCaptor = ArgumentCaptor.forClass(Float.class);
      ArgumentCaptor<Float> yCaptor = ArgumentCaptor.forClass(Float.class);
      verify(shape).circle(xCaptor.capture(), yCaptor.capture(), eq(0.15f), eq(12));
      // Lands on the wall's near face, having dropped by 0.5 * 20 * t^2 with t = (10 - 0.8) / 27.
      assertEquals(origin.x + 10f, xCaptor.getValue(), 0.05f);
      assertEquals(origin.y - 1.161f, yCaptor.getValue(), 0.15f);
    }
  }

  @Test
  void shouldLandLowerForAWeakerDrawBecauseTheArrowIsSlower() {
    // A barely drawn standard arrow: 18 * 0.5 = 9, so it's in the air over a second and sags a lot.
    when(bow.currentSpeedMultiplier()).thenReturn(0.5f);
    BowIndicatorRenderComponent renderer = renderer(bow, input);
    Vector2 origin = renderer.getEntity().getCenterPosition();
    createWallAhead(origin, 10f);

    Vector2 landing = renderAndGetLanding(renderer);

    assertEquals(origin.x + 10f, landing.x, 0.05f);
    assertEquals(origin.y - 10.449f, landing.y, 0.25f);
  }

  @Test
  void shouldUseTheLoadedArrowTypesOwnSpeed() {
    // Ice arrows are slower than standard ones (16 vs 18), so at the same draw they sag more.
    when(bow.getArrowType()).thenReturn(ArrowType.ICE);
    BowIndicatorRenderComponent renderer = renderer(bow, input);
    Vector2 origin = renderer.getEntity().getCenterPosition();
    createWallAhead(origin, 10f);

    Vector2 landing = renderAndGetLanding(renderer);

    assertEquals(origin.x + 10f, landing.x, 0.05f);
    assertEquals(origin.y - 3.306f, landing.y, 0.15f);
  }

  @Test
  void shouldStopTheArcAtMaximumRangeWhenNothingIsInTheWay() {
    BowIndicatorRenderComponent renderer = renderer(bow, input);
    Vector2 origin = renderer.getEntity().getCenterPosition();

    Vector2 landing = renderAndGetLanding(renderer);

    // Range is a straight-line distance from the player, and the arc has sagged well below them.
    assertEquals(50f, landing.dst(origin), 1.5f);
    assertTrue(landing.y < origin.y - 5f);
  }

  @Test
  void shouldDisposeShapeRendererAndUnregister() {
    BowIndicatorRenderComponent renderer = renderer(bow, input);

    try (MockedConstruction<ShapeRenderer> shapes = mockConstruction(ShapeRenderer.class)) {
      renderer.render(batch);
      ShapeRenderer shape = shapes.constructed().getFirst();
      renderer.dispose();
      verify(shape).dispose();
    }
    verify(renderService).unregister(renderer);
  }

  @Test
  void shouldFlyStraightThroughAnythingThatIsNotSolidTerrain() {
    BowIndicatorRenderComponent renderer = renderer(bow, input);
    Vector2 origin = renderer.getEntity().getCenterPosition();
    // An enemy-layer collider in the way isn't something the preview stops at.
    createWallAhead(origin, 10f, PhysicsLayer.NPC);

    Vector2 landing = renderAndGetLanding(renderer);

    assertTrue(landing.x > origin.x + 11f, "stopped at x=" + landing.x);
    assertEquals(50f, landing.dst(origin), 1.5f);
  }

  @Test
  void shouldGiveUpAfterAFixedNumberOfStepsForAShotThatBarelyMoves() {
    // With no gravity and almost no speed the arc never reaches terrain or the range limit, so the
    // simulation has to stop on its own rather than loop forever.
    physicsService.getPhysics().getWorld().setGravity(Vector2.Zero.cpy());
    when(bow.currentSpeedMultiplier()).thenReturn(0.0001f);
    BowIndicatorRenderComponent renderer = renderer(bow, input);
    Vector2 origin = renderer.getEntity().getCenterPosition();

    Vector2 landing = renderAndGetLanding(renderer);

    // It never got more than a hair away from where the arrow spawns.
    assertEquals(origin.x + BowComponent.SPAWN_OFFSET, landing.x, 0.1f);
    assertEquals(origin.y, landing.y, 0.1f);
  }

  @Test
  void shouldDisposeCleanlyEvenIfItNeverDrewAnything() {
    BowIndicatorRenderComponent renderer = renderer(bow, input);

    renderer.dispose();

    verify(renderService).unregister(renderer);
  }
}
