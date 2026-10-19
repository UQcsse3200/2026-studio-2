package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.csse3200.game.components.item.weapons.bow.grapple.GrappleArrowComponent;
import com.csse3200.game.components.item.weapons.bow.grapple.GrappleComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Fires a real grapple arrow at a real wall through real physics, so the rope's anchor is checked
 * against where the arrow actually hits - not against numbers the test made up.
 */
@ExtendWith(GameExtension.class)
class GrappleArrowAnchorIntegrationTest {
  private static final float WALL_FACE_X = -10f;
  private static final float STEP = 0.016f;

  private PhysicsService physicsService;
  private EntityService entityService;
  private GrappleComponent grapple;
  private Entity shooter;

  @BeforeEach
  void setUp() {
    GameTime time = mock(GameTime.class);
    when(time.getDeltaTime()).thenReturn(STEP);
    ServiceLocator.registerTimeSource(time);
    physicsService = new PhysicsService();
    ServiceLocator.registerPhysicsService(physicsService);
    entityService = new EntityService();
    ServiceLocator.registerEntityService(entityService);
    ServiceLocator.registerRenderService(mock(RenderService.class));

    grapple = spy(new GrappleComponent());
    shooter = new Entity().addComponent(grapple);
    shooter.setPosition(0f, 0f);

    // A tall wall on the left whose right-hand face is the surface the rope should land on.
    BodyDef def = new BodyDef();
    def.position.set(WALL_FACE_X - 2f, 0f);
    Body wall = physicsService.getPhysics().createBody(def);
    PolygonShape shape = new PolygonShape();
    shape.setAsBox(2f, 60f);
    Fixture fixture = wall.createFixture(shape, 0f);
    shape.dispose();
    Filter filter = fixture.getFilterData();
    filter.categoryBits = PhysicsLayer.GROUND;
    fixture.setFilterData(filter);
  }

  @AfterEach
  void tearDown() {
    physicsService.getPhysics().dispose();
  }

  /** Fires from beside the shooter and steps physics until the arrow lands, or gives up. */
  private Vector2 fireAndGetAnchor(Vector2 aim, float speedMultiplier) {
    Vector2 spawn = shooter.getCenterPosition().mulAdd(aim.cpy().nor(), 0.4f);
    Entity arrow = ProjectileFactory.createGrappleArrow(shooter, spawn, aim, speedMultiplier);
    arrow.addComponent(new GrappleArrowComponent(shooter));
    entityService.register(arrow);

    for (int i = 0; i < 600; i++) {
      physicsService.getPhysics().update();
      entityService.update();
      boolean attached =
          mockingDetails(grapple).getInvocations().stream()
              .anyMatch(call -> call.getMethod().getName().equals("attachTo"));
      if (attached) {
        org.mockito.ArgumentCaptor<Vector2> point =
            org.mockito.ArgumentCaptor.forClass(Vector2.class);
        org.mockito.Mockito.verify(grapple).attachTo(any(Body.class), point.capture());
        return point.getValue();
      }
    }
    return null;
  }

  @Test
  void shouldAnchorOnTheWallFaceForAnArrowFlyingUpAndLeft() {
    Vector2 anchor = fireAndGetAnchor(new Vector2(-1f, 0.6f), 1f);

    assertNotNull(anchor, "the arrow should have reached the wall and attached");
    // Rotating the arrow used to swing its collision box ahead of its sprite, so this landed in
    // mid-air well short of the face.
    assertEquals(WALL_FACE_X, anchor.x, 0.05f);
  }

  @Test
  void shouldAnchorOnTheWallFaceForAnArrowFlyingNearlyStraightUp() {
    // Shallow approaches to a wall need the longest reach to find the surface from.
    Vector2 anchor = fireAndGetAnchor(new Vector2(-1f, 6f), 1f);

    assertNotNull(anchor, "the arrow should have reached the wall and attached");
    assertEquals(WALL_FACE_X, anchor.x, 0.05f);
  }

  @Test
  void shouldAnchorOnTheWallFaceForAChargedArrowFlyingLeft() {
    Vector2 anchor = fireAndGetAnchor(new Vector2(-1f, 0.2f), 1.5f);

    assertNotNull(anchor, "the arrow should have reached the wall and attached");
    assertEquals(WALL_FACE_X, anchor.x, 0.05f);
  }
}
