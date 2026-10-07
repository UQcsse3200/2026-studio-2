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
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.item.weapons.bow.grapple.GrappleComponent;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.item.GrappleIndicatorRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;

/** Verifies the dotted grapple aim preview without creating an OpenGL context. */
@ExtendWith(GameExtension.class)
class GrappleIndicatorRenderComponentTest {

  private RenderService renderService;
  private PhysicsService physicsService;
  private SpriteBatch batch;
  private GrappleComponent grapple;
  private InventoryComponent inventory;
  private KeyboardPlayerInputComponent input;

  @BeforeEach
  void setUp() {
    renderService = mock(RenderService.class);
    ServiceLocator.registerRenderService(renderService);
    physicsService = new PhysicsService();
    ServiceLocator.registerPhysicsService(physicsService);

    batch = mock(SpriteBatch.class);
    when(batch.getProjectionMatrix()).thenReturn(new Matrix4());

    grapple = mock(GrappleComponent.class);
    inventory = mock(InventoryComponent.class);
    input = mock(KeyboardPlayerInputComponent.class);
    when(inventory.getSelectedItem()).thenReturn(ItemType.ROPE_ARROW);
    when(grapple.isCharging()).thenReturn(true);
    when(grapple.currentSpeedMultiplier()).thenReturn(1f);
  }

  @AfterEach
  void tearDown() {
    physicsService.getPhysics().dispose();
  }

  private GrappleIndicatorRenderComponent renderer(
      GrappleComponent grappleComponent,
      InventoryComponent inventoryComponent,
      KeyboardPlayerInputComponent inputComponent) {
    Entity entity = new Entity();
    if (grappleComponent != null) {
      entity.addComponent(grappleComponent);
    }
    if (inventoryComponent != null) {
      entity.addComponent(inventoryComponent);
    }
    if (inputComponent != null) {
      entity.addComponent(inputComponent);
    }
    GrappleIndicatorRenderComponent renderer = new GrappleIndicatorRenderComponent();
    entity.addComponent(renderer);
    renderer.create();
    return renderer;
  }

  private Fixture createSolidWall(float centerX, float centerY, float halfWidth, float halfHeight) {
    BodyDef def = new BodyDef();
    def.position.set(centerX, centerY);
    Body wall = physicsService.getPhysics().createBody(def);
    PolygonShape shape = new PolygonShape();
    shape.setAsBox(halfWidth, halfHeight);
    Fixture fixture = wall.createFixture(shape, 0f);
    shape.dispose();
    Filter filter = fixture.getFilterData();
    filter.categoryBits = PhysicsLayer.GROUND;
    fixture.setFilterData(filter);
    return fixture;
  }

  @Test
  void shouldLeaveSpriteBatchAloneWhenGrappleIsMissing() {
    GrappleIndicatorRenderComponent renderer = renderer(null, inventory, input);
    renderer.render(batch);
    verifyNoInteractions(batch);
  }

  @Test
  void shouldLeaveSpriteBatchAloneWhenNotCharging() {
    // Covers "not attached" and "on cooldown" too, since isCharging() is false in both real cases.
    when(grapple.isCharging()).thenReturn(false);
    GrappleIndicatorRenderComponent renderer = renderer(grapple, inventory, input);
    renderer.render(batch);
    verifyNoInteractions(batch);
  }

  @Test
  void shouldLeaveSpriteBatchAloneWhenWrongItemSelected() {
    when(inventory.getSelectedItem()).thenReturn(ItemType.STANDARD_ARROW);
    GrappleIndicatorRenderComponent renderer = renderer(grapple, inventory, input);
    renderer.render(batch);
    verifyNoInteractions(batch);
  }

  @Test
  void shouldLeaveSpriteBatchAloneWhenAimIsUnavailable() {
    when(input.getMouseAimDirection()).thenReturn(null);
    GrappleIndicatorRenderComponent renderer = renderer(grapple, inventory, input);
    renderer.render(batch);
    verifyNoInteractions(batch);
  }

  @Test
  void shouldLeaveSpriteBatchAloneWhenAimIsZero() {
    when(input.getMouseAimDirection()).thenReturn(Vector2.Zero.cpy());
    GrappleIndicatorRenderComponent renderer = renderer(grapple, inventory, input);
    renderer.render(batch);
    verifyNoInteractions(batch);
  }

  @Test
  void shouldDrawWhiteDottedArcEndingOnSolidSurfaceWithinRange() {
    when(input.getMouseAimDirection()).thenReturn(new Vector2(10f, 0f));

    GrappleIndicatorRenderComponent renderer = renderer(grapple, inventory, input);
    Vector2 origin = renderer.getEntity().getCenterPosition();

    // A tall wall a bit further along the aim direction than the player, so the ray hits its near
    // face regardless of how far the arc has dropped by the time it gets there.
    createSolidWall(origin.x + 10.5f, origin.y - 5f, 0.5f, 15f);

    try (MockedConstruction<ShapeRenderer> shapes = mockConstruction(ShapeRenderer.class)) {
      renderer.render(batch);
      assertEquals(1, shapes.constructed().size());
      ShapeRenderer shape = shapes.constructed().getFirst();

      verify(shape).setColor(Color.WHITE);

      // Small dots spaced along the arc, plus one bigger dot at the landing point.
      verify(shape, atLeastOnce()).circle(anyFloat(), anyFloat(), eq(0.05f), eq(12));

      ArgumentCaptor<Float> xCaptor = ArgumentCaptor.forClass(Float.class);
      ArgumentCaptor<Float> yCaptor = ArgumentCaptor.forClass(Float.class);
      verify(shape).circle(xCaptor.capture(), yCaptor.capture(), eq(0.15f), eq(12));
      // The wall pins the landing x, but under gravity the arc has already dropped by the time it
      // gets there. The arrow spawns 0.6 ahead of the player, so it only has 9.4 left to cover:
      // time-to-wall = 9.4 / GRAPPLE_ARROW_SPEED, drop = 0.5 * effective gravity (20) * t^2.
      assertEquals(origin.x + 10f, xCaptor.getValue(), 0.05f);
      assertEquals(origin.y - 1.826f, yCaptor.getValue(), 0.15f);
    }
  }

  @Test
  void shouldStopDottedArcAtMaximumRangeWhenNothingToGrappleOnto() {
    when(input.getMouseAimDirection()).thenReturn(new Vector2(10f, 0f));

    GrappleIndicatorRenderComponent renderer = renderer(grapple, inventory, input);
    Vector2 origin = renderer.getEntity().getCenterPosition();

    try (MockedConstruction<ShapeRenderer> shapes = mockConstruction(ShapeRenderer.class)) {
      renderer.render(batch);
      ShapeRenderer shape = shapes.constructed().getFirst();

      ArgumentCaptor<Float> xCaptor = ArgumentCaptor.forClass(Float.class);
      ArgumentCaptor<Float> yCaptor = ArgumentCaptor.forClass(Float.class);
      verify(shape).circle(xCaptor.capture(), yCaptor.capture(), eq(0.15f), eq(12));
      // No obstruction, so the preview reaches all the way out to the grapple's maximum range - as
      // a straight-line distance from the player, not just a fixed horizontal offset any more.
      float distance = new Vector2(xCaptor.getValue(), yCaptor.getValue()).dst(origin.x, origin.y);
      assertEquals(50f, distance, 1.5f);
      // And it's an arc, not a straight line: it's fallen well below the player by then.
      assertTrue(yCaptor.getValue() < origin.y - 5f);
    }
  }

  @Test
  void shouldLandLowerForAWeakerChargeBecauseTheArrowIsSlower() {
    // Barely charged, the grapple is launched at 22 * 0.5 = 11, so it hangs in the air longer on
    // the way to the wall and sags a lot more than a normal-speed shot would.
    when(grapple.currentSpeedMultiplier()).thenReturn(0.5f);
    when(input.getMouseAimDirection()).thenReturn(new Vector2(10f, 0f));

    GrappleIndicatorRenderComponent renderer = renderer(grapple, inventory, input);
    Vector2 origin = renderer.getEntity().getCenterPosition();
    createSolidWall(origin.x + 10.5f, origin.y - 20f, 0.5f, 40f);

    try (MockedConstruction<ShapeRenderer> shapes = mockConstruction(ShapeRenderer.class)) {
      renderer.render(batch);
      ShapeRenderer shape = shapes.constructed().getFirst();

      ArgumentCaptor<Float> xCaptor = ArgumentCaptor.forClass(Float.class);
      ArgumentCaptor<Float> yCaptor = ArgumentCaptor.forClass(Float.class);
      verify(shape).circle(xCaptor.capture(), yCaptor.capture(), eq(0.15f), eq(12));
      // 9.4 left to cover at speed 11: t = 0.8545s, drop = 0.5 * 20 * t^2 = 7.30.
      assertEquals(origin.x + 10f, xCaptor.getValue(), 0.05f);
      assertEquals(origin.y - 7.303f, yCaptor.getValue(), 0.25f);
    }
  }

  @Test
  void shouldDisposeShapeRendererAndUnregister() {
    when(input.getMouseAimDirection()).thenReturn(new Vector2(10f, 0f));
    GrappleIndicatorRenderComponent renderer = renderer(grapple, inventory, input);

    try (MockedConstruction<ShapeRenderer> shapes = mockConstruction(ShapeRenderer.class)) {
      renderer.render(batch);
      ShapeRenderer shape = shapes.constructed().getFirst();
      renderer.dispose();
      verify(shape).dispose();
    }
    verify(renderService).unregister(renderer);
  }
}
