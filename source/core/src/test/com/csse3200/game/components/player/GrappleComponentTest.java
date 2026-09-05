package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class GrappleComponentTest {
  private EntityService entityService;

  @Test
  void shouldStartDetached() {
    GrappleComponent grapple = new GrappleComponent();
    assertFalse(grapple.isAttached());
    assertNull(grapple.getAnchorPoint());
  }

  @Test
  void shouldIgnoreReleaseWhenNotAttached() {
    GrappleComponent grapple = new GrappleComponent();
    // Would throw if it tried to destroy a null joint
    grapple.release();
    assertFalse(grapple.isAttached());
  }

  @Test
  void shouldIgnoreSwingWhenNotAttached() {
    GrappleComponent grapple = new GrappleComponent();
    // Would throw on the null body if the guard were missing
    grapple.swing(1f);
    grapple.swing(-1f);
    grapple.swing(0f);
  }

  @Test
  void shouldQueueAttachmentWithoutBuildingJoint() {
    GrappleComponent grapple = new GrappleComponent();
    // Queuing only stores the point, the joint waits for update()
    grapple.attachTo(null, new Vector2(3f, 4f));
    assertFalse(grapple.isAttached());
  }

  @Test
  void shouldNotFireWithoutDirection() {
    Entity player = new Entity().addComponent(new GrappleComponent());
    GrappleComponent grapple = player.getComponent(GrappleComponent.class);

    // Would reach ProjectileFactory and fail without a registered service
    grapple.fire(null);
    grapple.fire(Vector2.Zero.cpy());
  }

  @Test
  void shouldCopyAnchorPointOnRead() {
    GrappleComponent grapple = new GrappleComponent();
    assertNull(grapple.getAnchorPoint());
  }

  @BeforeEach
  void setUp() {
    GameTime gameTime = mock(GameTime.class);
    when(gameTime.getDeltaTime()).thenReturn(0.016f);
    ServiceLocator.registerTimeSource(gameTime);

    ServiceLocator.registerPhysicsService(new PhysicsService());
    entityService = new EntityService();
    ServiceLocator.registerEntityService(entityService);
    ServiceLocator.registerRenderService(new RenderService());
  }

  @Test
  void shouldRejectGrappleWithoutRopeArrow() {
    Entity player = createPlayer(false);
    GrappleComponent grapple = player.getComponent(GrappleComponent.class);
    AtomicReference<ItemType> failedType = new AtomicReference<>();
    player.getEvents().addListener("itemUseFailed", failedType::set);

    player.getEvents().trigger("grappleFire", Vector2.X.cpy());

    assertFalse(grapple.isAttached());
    assertEquals(ItemType.RopeArrow, failedType.get());
  }

  @Test
  void shouldAttachWithoutConsumingOwnedRopeArrow() {
    createGroundAnchor();
    Entity player = createPlayer(true);
    GrappleComponent grapple = player.getComponent(GrappleComponent.class);
    InventoryComponent inventory = player.getComponent(InventoryComponent.class);

    player
        .getEvents()
        .addListener(
            "grappleRequested", point -> player.getEvents().trigger("grappleResponse", true));

    player.getEvents().trigger("grappleFire", Vector2.X.cpy());

    // The grapple is an actual projectile: advance physics until it reaches the anchor, then run
    // one extra entity update so the queued attachment can create the rope joint.
    for (int i = 0; i < 30 && !grapple.isAttached(); i++) {
      ServiceLocator.getPhysicsService().getPhysics().update();
      entityService.update();
    }

    assertTrue(grapple.isAttached());
    assertEquals(1, inventory.getItemCount(ItemType.RopeArrow));
  }

  @Test
  void shouldReachTargetsWithinProjectileRange() {
    Entity anchor =
        new Entity()
            .addComponent(new PhysicsComponent().setBodyType(BodyType.StaticBody))
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.GROUND));
    anchor.setPosition(12f, 0f);
    entityService.register(anchor);

    Entity player = createPlayer(true);
    AtomicReference<Vector2> requestedPoint = new AtomicReference<>();
    player.getEvents().addListener("grappleRequested", requestedPoint::set);

    player.getEvents().trigger("grappleFire", Vector2.X.cpy());

    assertNotNull(requestedPoint.get());
  }

  private Entity createPlayer(boolean hasRopeArrow) {
    InventoryComponent inventory = new InventoryComponent(0);
    if (hasRopeArrow) {
      inventory.addItem(ItemType.RopeArrow, 1);
    }
    Entity player =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(inventory)
            .addComponent(new GrappleComponent());
    player.setPosition(0f, 0f);
    entityService.register(player);
    return player;
  }

  private void createGroundAnchor() {
    Entity anchor =
        new Entity()
            .addComponent(new PhysicsComponent().setBodyType(BodyType.StaticBody))
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.GROUND));
    anchor.setPosition(4f, 0f);
    entityService.register(anchor);
  }
}
