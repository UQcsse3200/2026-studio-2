package com.csse3200.game.components.item.weapons.bow.grapple;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.level.SlipperyPlatformComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class GrapplePlatformAttachmentTest {
  private PhysicsService physics;
  private EntityService entities;

  @BeforeEach
  void setUp() {
    physics = new PhysicsService();
    entities = new EntityService();
    ServiceLocator.registerPhysicsService(physics);
    ServiceLocator.registerEntityService(entities);
    GameTime time = mock(GameTime.class);
    when(time.getDeltaTime()).thenReturn(0.25f);
    ServiceLocator.registerTimeSource(time);
  }

  @AfterEach
  void tearDown() {
    entities.dispose();
    physics.getPhysics().dispose();
  }

  private Entity platform(boolean slippery) {
    Entity platform =
        new Entity()
            .addComponent(new PhysicsComponent().setBodyType(BodyDef.BodyType.StaticBody))
            .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));
    if (slippery) {
      platform.addComponent(new SlipperyPlatformComponent(0.5f, 0.5f));
    }
    platform.setPosition(5f, 0f);
    entities.register(platform);
    return platform;
  }

  private Entity arrow(Entity shooter) {
    Entity arrow = new Entity().addComponent(new GrappleArrowComponent(shooter));
    arrow.setPosition(5f, 0f);
    entities.register(arrow);
    return arrow;
  }

  @Test
  void attachingToASlipperyPlatformShouldStartItsReleaseTimer() {
    GrappleComponent grapple = new GrappleComponent();
    Entity player = new Entity().addComponent(new PhysicsComponent()).addComponent(grapple);
    entities.register(player);
    Entity platform = platform(true);
    AtomicInteger releases = new AtomicInteger();
    platform.getEvents().addListener("grappleTimeExceeded", releases::incrementAndGet);
    Entity arrow = arrow(player);
    var fixture = platform.getComponent(ColliderComponent.class).getFixture();

    arrow.getEvents().trigger("collisionStart", fixture, fixture);
    entities.update();
    assertTrue(grapple.isAttached());
    assertFalse(entities.getEntities().contains(arrow, true));
    assertEquals(0, releases.get());
    entities.update();
    assertEquals(1, releases.get());
    entities.update();
    entities.update();
    assertEquals(1, releases.get(), "one attachment starts exactly one timer");
  }

  @Test
  void ordinaryPlatformShouldExposeTheQueuedSurfaceAnchorBeforeTheJointIsCreated() {
    GrappleComponent grapple = new GrappleComponent();
    Entity player = new Entity().addComponent(new PhysicsComponent()).addComponent(grapple);
    entities.register(player);
    Entity platform = platform(false);
    Entity arrow = arrow(player);
    var fixture = platform.getComponent(ColliderComponent.class).getFixture();
    arrow.getEvents().trigger("collisionStart", fixture, fixture);

    assertFalse(grapple.isAttached());
    assertEquals(2, grapple.getRopePath().size());
    assertEquals(new Vector2(5.5f, 0f), grapple.getRopePath().getLast());
    grapple.fire(Vector2.X);
    assertEquals(3, entities.getEntities().size, "queued attachment prevents another shot");
    entities.update();
    assertTrue(grapple.isAttached());
    assertEquals(new Vector2(5.5f, 0f), grapple.getAnchorPoint());
  }

  @Test
  void removingAnArrowWhoseShooterHasNoGrappleShouldStillRemoveItCleanly() {
    Entity arrow = arrow(new Entity());
    Entity platform = platform(false);
    var fixture = platform.getComponent(ColliderComponent.class).getFixture();
    arrow.getEvents().trigger("collisionStart", fixture, fixture);
    entities.update();
    assertFalse(entities.getEntities().contains(arrow, true));
    assertEquals(1, entities.getEntities().size);
  }
}
