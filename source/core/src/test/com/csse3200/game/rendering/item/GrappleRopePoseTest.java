package com.csse3200.game.rendering.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.item.weapons.bow.grapple.GrappleComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.components.player.PlayerAnimationController;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * When the hanging pose shows: only while the player hangs from a rope that runs up from them. A
 * player standing on a platform with a rope out, or above a rope anchored below them, keeps the
 * normal animations. The pose hides the normal animation by stopping it, so "pose showing" is
 * observed here as the animator having no current animation.
 */
@ExtendWith(GameExtension.class)
class GrappleRopePoseTest {
  private Entity player;
  private GrappleComponent grapple;
  private PlayerActions actions;
  private PlayerAnimationController controller;
  private GrappleHoldRenderComponent hold;
  private AnimationRenderComponent animator;
  private TextureAtlas atlas;

  @BeforeEach
  void setUp() {
    ServiceLocator.registerPhysicsService(new PhysicsService());
    ServiceLocator.registerRenderService(mock(RenderService.class));
    EntityService entityService = mock(EntityService.class);
    when(entityService.getPaused()).thenReturn(false);
    ServiceLocator.registerEntityService(entityService);
    GameTime time = mock(GameTime.class);
    when(time.getDeltaTime()).thenReturn(0.016f);
    ServiceLocator.registerTimeSource(time);

    atlas = mock(TextureAtlas.class);
    for (String name : new String[] {"idle", "walk", "jump_fall", "jump_land"}) {
      Array<AtlasRegion> regions = new Array<>(1);
      regions.add(mock(AtlasRegion.class));
      when(atlas.findRegions(name)).thenReturn(regions);
    }
    animator = new AnimationRenderComponent(atlas);
    animator.addAnimation("idle", 1f, PlayMode.LOOP);
    animator.addAnimation("walk", 1f, PlayMode.LOOP);
    animator.addAnimation("jump_fall", 1f, PlayMode.LOOP);
    // Shorter than one render step, so a single render() finishes the crouch.
    animator.addAnimation("jump_land", 0.01f, PlayMode.NORMAL);

    grapple = new GrappleComponent();
    actions = new PlayerActions();
    controller = new PlayerAnimationController();
    hold = new GrappleHoldRenderComponent();
    player =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(actions)
            .addComponent(grapple)
            .addComponent(animator)
            .addComponent(controller)
            .addComponent(hold);
    player.create();
  }

  /** A floor just under the player's feet (the player sits at the origin, 1x1). */
  private void createGround() {
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyDef.BodyType.StaticBody;
    bodyDef.position.set(0.5f, -0.2f);
    Body ground = ServiceLocator.getPhysicsService().getPhysics().createBody(bodyDef);
    PolygonShape box = new PolygonShape();
    box.setAsBox(1f, 0.1f);
    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.shape = box;
    fixtureDef.filter.categoryBits = PhysicsLayer.GROUND;
    ground.createFixture(fixtureDef);
    box.dispose();
  }

  private void attachRopeTo(float anchorX, float anchorY) {
    BodyDef anchorDef = new BodyDef();
    anchorDef.type = BodyDef.BodyType.StaticBody;
    anchorDef.position.set(anchorX, anchorY);
    Body anchor = ServiceLocator.getPhysicsService().getPhysics().createBody(anchorDef);
    grapple.attachTo(anchor, new Vector2(anchorX, anchorY));
    grapple.update(); // builds the queued joint
    assertTrue(grapple.isAttached());
  }

  /** One frame, in the order the player's components update in game. */
  private void frame() {
    actions.update();
    controller.update();
    hold.update();
  }

  @Test
  void shouldShowThePoseWhileHangingFromARopeAbove() {
    attachRopeTo(0.5f, 5f);
    frame();

    // Control for the tests below: the pose does show here, so their "not shown" means something.
    assertNull(animator.getCurrentAnimation());
  }

  @Test
  void shouldWalkNormallyWhileStandingOnTheGroundWithARopeOut() {
    createGround();
    player.getEvents().trigger("walk", new Vector2(1f, 0f));
    attachRopeTo(0.5f, 5f);

    frame();

    // Regression: the hanging pose was drawn along the rope while the player stood on a platform.
    assertEquals("walk", animator.getCurrentAnimation());
  }

  @Test
  void shouldNotHangFromARopeThatRunsDownToAnAnchorBelow() {
    attachRopeTo(0.5f, -5f);

    frame();

    // Regression: the pose was drawn upside down, "hanging" from a slack rope below the player.
    assertEquals("idle", animator.getCurrentAnimation());
  }

  @Test
  void shouldPlayTheLandingCrouchWhenTouchingDownOnARope() {
    attachRopeTo(0.5f, 5f);
    player.getComponent(PhysicsComponent.class).getBody().setLinearVelocity(0f, -5f);
    frame(); // swinging down: the pose shows, and the descent counts as a fall underneath it
    assertNull(animator.getCurrentAnimation());

    createGround();
    frame();

    assertEquals("jump_land", animator.getCurrentAnimation());

    // The crouch must still be able to finish. Restoring the normal animation over it would have
    // left the controller waiting for a landing that never ends.
    animator.render(mock(SpriteBatch.class));
    frame();
    assertEquals("idle", animator.getCurrentAnimation());
  }

  @Test
  void shouldBringBackTheNormalAnimationWhenLettingGoInMidAir() {
    attachRopeTo(0.5f, 5f);
    frame();
    assertNull(animator.getCurrentAnimation());

    grapple.release();
    frame();

    assertEquals("idle", animator.getCurrentAnimation());
  }

  // ---- The pose must never cut off a one-shot clip ----

  private void registerBowAndInstrumentClips() {
    for (String name : new String[] {"bow_draw", "bow_shoot", "jump_takeoff", "instrument_draw"}) {
      Array<AtlasRegion> regions = new Array<>(1);
      regions.add(mock(AtlasRegion.class));
      when(atlas.findRegions(name)).thenReturn(regions);
    }
    // Shorter than one render step, so a single render() finishes them.
    animator.addAnimation("bow_draw", 0.01f, PlayMode.NORMAL);
    animator.addAnimation("bow_shoot", 0.01f, PlayMode.NORMAL);
    animator.addAnimation("jump_takeoff", 0.01f, PlayMode.NORMAL);
    animator.addAnimation("instrument_draw", 0.01f, PlayMode.NORMAL);
  }

  @Test
  void shouldLetTheGrappleShotFinishBeforeHangingFromItsRope() {
    registerBowAndInstrumentClips();
    // A grapple fired in mid-air plays the bow's shot, and the rope can land before it finishes.
    player.getEvents().trigger("grappleChargeStart", new Vector2(1f, 1f));
    player.getEvents().trigger("grappleChargeFire", new Vector2(1f, 1f));
    assertEquals("bow_shoot", animator.getCurrentAnimation());
    attachRopeTo(0.5f, 5f);

    frame();
    assertEquals("bow_shoot", animator.getCurrentAnimation(), "the shot plays out first");

    animator.render(mock(SpriteBatch.class));
    frame();
    assertNull(animator.getCurrentAnimation(), "then the pose takes over");

    grapple.release();
    frame();
    // Regression: the pose cut the shot off, so the bow state never cleared. Every jump, dash,
    // hit and swing animation stayed blocked after letting go.
    player.getEvents().trigger("jumpStart");
    assertEquals("jump_takeoff", animator.getCurrentAnimation());
  }

  @Test
  void shouldNotLeaveThePlayerInvisibleAfterTheInstrumentOnTheRope() {
    registerBowAndInstrumentClips();
    attachRopeTo(0.5f, 5f);
    frame();
    assertNull(animator.getCurrentAnimation());

    player.getEvents().trigger("instrumentStart");
    frame();
    grapple.release();
    frame();

    // Regression: the pose cut the instrument's draw off, and with the instrument still "out" the
    // controller refused to pick anything else - nothing was drawn at all after letting go.
    org.junit.jupiter.api.Assertions.assertNotNull(animator.getCurrentAnimation());
  }
}
