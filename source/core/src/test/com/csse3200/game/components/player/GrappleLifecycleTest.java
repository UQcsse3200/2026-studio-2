package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.item.weapons.bow.grapple.GrappleComponent;
import com.csse3200.game.components.projectile.ArrowProjectileComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class GrappleLifecycleTest {
  private PhysicsService physics;
  private EntityService entities;
  private GameTime time;
  private Entity player;
  private GrappleComponent grapple;
  private Body playerBody;

  @BeforeEach
  void setUp() {
    time = mock(GameTime.class);
    ServiceLocator.registerTimeSource(time);
    physics = new PhysicsService();
    entities = new EntityService();
    ServiceLocator.registerPhysicsService(physics);
    ServiceLocator.registerEntityService(entities);
    ServiceLocator.registerRenderService(mock(RenderService.class));
    grapple = new GrappleComponent();
    player = new Entity().addComponent(new PhysicsComponent()).addComponent(grapple);
    entities.register(player);
    playerBody = player.getComponent(PhysicsComponent.class).getBody();
    playerBody.setGravityScale(0f);
  }

  @AfterEach
  void tearDown() {
    entities.dispose();
    physics.getPhysics().dispose();
  }

  private Body anchor(float x, float y) {
    BodyDef def = new BodyDef();
    def.position.set(x, y);
    return physics.getPhysics().createBody(def);
  }

  @Test
  void shouldFireNormalizedProjectileAndKeepCooldownAcrossRejectedShots() {
    Vector2 aim = new Vector2(3f, 4f);
    grapple.fire(aim);
    assertEquals(2, entities.getEntities().size);
    Entity arrow = entities.getEntities().get(1);
    assertTrue(
        arrow
            .getComponent(ArrowProjectileComponent.class)
            .getDirection()
            .epsilonEquals(0.6f, 0.8f, 0.001f));
    assertEquals(new Vector2(3f, 4f), aim);
    when(time.getDeltaTime()).thenReturn(1f);
    grapple.update();
    grapple.fire(Vector2.X);
    assertEquals(2, entities.getEntities().size);
    grapple.update();
    grapple.fire(Vector2.X);
    assertEquals(3, entities.getEntities().size);
  }

  @Test
  void shouldNotSpawnAnotherArrowWhileAttached() {
    grapple.attachTo(anchor(0f, 5f), new Vector2(0f, 5f));
    grapple.update();
    grapple.fire(Vector2.X);
    assertEquals(1, entities.getEntities().size);
    assertTrue(grapple.isAttached());
  }

  @Test
  void shouldKeepFirstPendingAttachmentAndCopyItsPoint() {
    Vector2 point = new Vector2(0f, 5f);
    grapple.attachTo(anchor(0f, 5f), point);
    point.set(9f, 9f);
    grapple.attachTo(anchor(8f, 8f), new Vector2(8f, 8f));
    assertFalse(grapple.isAttached());
    grapple.update();
    assertEquals(new Vector2(0f, 5f), grapple.getAnchorPoint());
    assertEquals(1, physics.getPhysics().getWorld().getJointCount());
    grapple.attachTo(anchor(3f, 3f), new Vector2(3f, 3f));
    grapple.update();
    assertEquals(new Vector2(0f, 5f), grapple.getAnchorPoint());
  }

  @Test
  void shouldTrackMovingAnchorWithoutExposingMutableRopeState() {
    Body anchor = anchor(0f, 5f);
    grapple.attachTo(anchor, new Vector2(0f, 5f));
    grapple.update();
    grapple.getAnchorPoint().setZero();
    var path = grapple.getRopePath();
    path.get(1).setZero();
    path.clear();
    anchor.setTransform(2f, 6f, 0f);
    grapple.update();
    assertEquals(new Vector2(2f, 6f), grapple.getAnchorPoint());
    assertEquals(new Vector2(2f, 6f), grapple.getRopePath().get(1));
    assertEquals(5f, grapple.getRopeLength(), 0.001f);
  }

  @Test
  void shouldClearRopeAndRestoreCustomDampingWithoutLosingMomentumOnRelease() {
    playerBody.setLinearDamping(3.25f);
    grapple.attachTo(anchor(0f, 5f), new Vector2(0f, 5f));
    grapple.update();
    playerBody.setLinearVelocity(4f, -2f);
    grapple.release();
    assertFalse(grapple.isAttached());
    assertTrue(grapple.getRopePath().isEmpty());
    assertNull(grapple.getAnchorPoint());
    assertEquals(0f, grapple.getRopeLength());
    assertEquals(0f, grapple.getInitialRopeLength());
    assertEquals(3.25f, playerBody.getLinearDamping());
    assertEquals(new Vector2(4f, -2f), playerBody.getLinearVelocity());
    assertEquals(0, physics.getPhysics().getWorld().getJointCount());
  }

  @Test
  void shouldPushSwingInRequestedDirection() {
    grapple.attachTo(anchor(0f, 5f), new Vector2(0f, 5f));
    grapple.update();
    grapple.swing(1f);
    physics.getPhysics().getWorld().step(0.01f, 6, 2);
    assertTrue(playerBody.getLinearVelocity().x > 0f);
    playerBody.setTransform(0f, 0f, 0f);
    playerBody.setLinearVelocity(0f, 0f);
    grapple.swing(-1f);
    physics.getPhysics().getWorld().step(0.01f, 6, 2);
    assertTrue(playerBody.getLinearVelocity().x < 0f);
  }

  @Test
  void shouldNotAccelerateSwingBeyondSpeedCap() {
    grapple.attachTo(anchor(0f, 5f), new Vector2(0f, 5f));
    grapple.update();
    playerBody.setLinearDamping(0f);
    playerBody.setLinearVelocity(9f, 0f);
    grapple.swing(1f);
    physics.getPhysics().getWorld().step(0.01f, 6, 2);
    assertTrue(playerBody.getLinearVelocity().x <= 9.001f);
  }

  @Test
  void shouldNotAccelerateSwingWithoutDirection() {
    grapple.attachTo(anchor(0f, 5f), new Vector2(0f, 5f));
    grapple.update();
    grapple.swing(0f);
    physics.getPhysics().getWorld().step(0.01f, 6, 2);
    assertTrue(playerBody.getLinearVelocity().isZero(0.0001f));
  }
}
