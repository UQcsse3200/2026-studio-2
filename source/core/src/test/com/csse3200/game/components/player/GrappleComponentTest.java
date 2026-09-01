package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import com.csse3200.game.services.ServiceLocator;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class GrappleComponentTest {
  private EntityService entityService;

  @BeforeEach
  void setUp() {
    ServiceLocator.registerPhysicsService(new PhysicsService());
    entityService = new EntityService();
    ServiceLocator.registerEntityService(entityService);
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

    player.getEvents().trigger("grappleFire", Vector2.X.cpy());

    assertTrue(grapple.isAttached());
    assertEquals(1, inventory.getItemCount(ItemType.RopeArrow));
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
