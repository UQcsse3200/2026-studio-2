package com.csse3200.game.rendering.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.item.weapons.bow.grapple.GrappleComponent;
import com.csse3200.game.components.player.PlayerAnimationController;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

/**
 * The rope pose itself needs a real texture to draw, so these cover the state handling around it:
 * when it draws at all, and that letting go of the rope hands control back to the normal animation.
 */
@ExtendWith(GameExtension.class)
class GrappleHoldRenderComponentTest {
  private Entity player;
  private GrappleComponent grapple;

  @BeforeEach
  void setUp() {
    ServiceLocator.registerPhysicsService(new PhysicsService());
    ServiceLocator.registerRenderService(mock(RenderService.class));
    ServiceLocator.registerEntityService(mock(EntityService.class));
    GameTime time = mock(GameTime.class);
    when(time.getDeltaTime()).thenReturn(0.016f);
    ServiceLocator.registerTimeSource(time);

    TextureAtlas atlas = mock(TextureAtlas.class);
    for (String name :
        new String[] {
          "idle", "walk", "jump_fall", "bow_draw", "bow_hold", "bow_shoot", "jump_land"
        }) {
      Array<AtlasRegion> regions = new Array<>(1);
      regions.add(mock(AtlasRegion.class));
      when(atlas.findRegions(name)).thenReturn(regions);
    }
    AnimationRenderComponent animator = new AnimationRenderComponent(atlas);
    animator.addAnimation("idle", 1f, PlayMode.LOOP);
    animator.addAnimation("walk", 1f, PlayMode.LOOP);
    animator.addAnimation("jump_fall", 1f, PlayMode.LOOP);
    animator.addAnimation("bow_draw", 1f, PlayMode.NORMAL);
    animator.addAnimation("bow_hold", 1f, PlayMode.LOOP);
    animator.addAnimation("bow_shoot", 1f, PlayMode.NORMAL);
    animator.addAnimation("jump_land", 1f, PlayMode.NORMAL);

    grapple = new GrappleComponent();
    player =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(grapple)
            .addComponent(animator)
            .addComponent(new PlayerAnimationController())
            .addComponent(new GrappleHoldRenderComponent());
    player.create();
  }

  /** Latches the player onto a rope anchored above them. */
  private void attachRope() {
    BodyDef anchorDef = new BodyDef();
    anchorDef.type = BodyDef.BodyType.StaticBody;
    anchorDef.position.set(0f, 5f);
    Body anchor = ServiceLocator.getPhysicsService().getPhysics().createBody(anchorDef);
    grapple.attachTo(anchor, new Vector2(0f, 5f));
    grapple.update(); // builds the queued joint
    assertTrue(grapple.isAttached());
  }

  @Test
  void shouldRestoreTheMovementAnimationWhenTheRopeIsLetGo() {
    GrappleHoldRenderComponent hold = player.getComponent(GrappleHoldRenderComponent.class);
    AnimationRenderComponent animator = player.getComponent(AnimationRenderComponent.class);
    player.getEvents().trigger("walk", new Vector2(1f, 0f));
    assertEquals("walk", animator.getCurrentAnimation());

    attachRope();
    hold.update();
    // While the pose draws, it stops the animator so the normal sprite is hidden underneath it.
    // Done explicitly here because the pose's texture needs a graphics context to load.
    animator.stopAnimation();

    grapple.release();
    hold.update();

    assertEquals("walk", animator.getCurrentAnimation());
  }

  @Test
  void shouldHandBackToIdleInMidAirUntilTheFallIsAnnouncedAgain() {
    GrappleHoldRenderComponent hold = player.getComponent(GrappleHoldRenderComponent.class);
    AnimationRenderComponent animator = player.getComponent(AnimationRenderComponent.class);
    player.getEvents().trigger("fallStart");

    attachRope();
    hold.update();
    animator.stopAnimation();

    grapple.release();
    hold.update();

    // Latching on cleared the controller's fall state, so the refresh has nothing airborne to go
    // back to and picks idle. PlayerActions re-announces the fall as soon as the player is
    // descending freely again, which is what actually restores the loop.
    assertEquals("idle", animator.getCurrentAnimation());

    player.getEvents().trigger("fallStart");
    assertEquals("jump_fall", animator.getCurrentAnimation());
  }

  @Test
  void shouldNotRefreshTheAnimationWhileStillAttached() {
    GrappleHoldRenderComponent hold = player.getComponent(GrappleHoldRenderComponent.class);
    AnimationRenderComponent animator = player.getComponent(AnimationRenderComponent.class);

    attachRope();
    hold.update();
    animator.stopAnimation();
    hold.update();

    // Still swinging, so the normal animation must stay hidden rather than flickering back in
    // underneath the rope pose.
    org.junit.jupiter.api.Assertions.assertNull(animator.getCurrentAnimation());
  }

  /** Latches the player onto a rope anchored at the given world point. */
  private void attachRopeAt(float x, float y) {
    BodyDef anchorDef = new BodyDef();
    anchorDef.type = BodyDef.BodyType.StaticBody;
    anchorDef.position.set(x, y);
    Body anchor = ServiceLocator.getPhysicsService().getPhysics().createBody(anchorDef);
    grapple.attachTo(anchor, new Vector2(x, y));
    grapple.update(); // builds the queued joint
    assertTrue(grapple.isAttached());
  }

  /** A hold component that has its pose image, so it can decide whether to hang the player. */
  private GrappleHoldRenderComponent holdWithPose() {
    GrappleHoldRenderComponent hold = player.getComponent(GrappleHoldRenderComponent.class);
    hold.setTexture(mock(Texture.class));
    return hold;
  }

  /**
   * Moves the player so the rope to the anchor leaves them {@code run} across and {@code rise} up.
   */
  private void placePlayerBelowAnchor(Vector2 anchor, float run, float rise) {
    // The entity is one unit square, so its centre is half a unit from where it is positioned.
    player.setPosition(anchor.x - run - 0.5f, anchor.y - rise - 0.5f);
  }

  @Test
  void shouldHangFromARopeThatGoesUp() {
    GrappleHoldRenderComponent hold = holdWithPose();
    attachRopeAt(0.5f, 6f);

    hold.update();

    assertTrue(hold.isHolding());
    org.junit.jupiter.api.Assertions.assertNull(
        player.getComponent(AnimationRenderComponent.class).getCurrentAnimation(),
        "the normal sprite is hidden underneath the pose");
  }

  @Test
  void shouldNotHangUpsideDownFromAHookBelowThem() {
    GrappleHoldRenderComponent hold = holdWithPose();
    attachRopeAt(0.5f, -5f);

    hold.update();

    org.junit.jupiter.api.Assertions.assertFalse(hold.isHolding());
    org.junit.jupiter.api.Assertions.assertNotNull(
        player.getComponent(AnimationRenderComponent.class).getCurrentAnimation(),
        "they keep their normal animation instead");
  }

  @Test
  void shouldGiveTheNormalAnimationBackWhenThePlayerEndsUpAboveTheHook() {
    GrappleHoldRenderComponent hold = holdWithPose();
    attachRopeAt(0.5f, 6f);
    hold.update();
    assertTrue(hold.isHolding());

    // Climbed or swung up and over the hook: it is now below them.
    player.setPosition(0f, 12f);
    hold.update();

    org.junit.jupiter.api.Assertions.assertFalse(hold.isHolding());
    org.junit.jupiter.api.Assertions.assertNotNull(
        player.getComponent(AnimationRenderComponent.class).getCurrentAnimation(),
        "the normal sprite must come back, not leave them invisible");
  }

  @Test
  void shouldHangAgainWhenTheRopeGoesUpAgain() {
    GrappleHoldRenderComponent hold = holdWithPose();
    attachRopeAt(0.5f, 6f);
    hold.update();
    player.setPosition(0f, 12f);
    hold.update();
    org.junit.jupiter.api.Assertions.assertFalse(hold.isHolding());

    player.setPosition(0f, 0f);
    hold.update();

    assertTrue(hold.isHolding());
  }

  @Test
  void shouldDrawNothingWhileTheRopePointsDown() {
    GrappleHoldRenderComponent hold = holdWithPose();
    attachRopeAt(0.5f, -5f);
    hold.update();
    SpriteBatch batch = mock(SpriteBatch.class);

    hold.render(batch);

    verifyNoInteractions(batch);
  }

  @Test
  void shouldNeverTurnThePlayerHeadDownWhateverDirectionTheRopeGoes() {
    GrappleHoldRenderComponent hold = holdWithPose();
    Vector2 anchor = new Vector2(20.5f, 20.5f);
    attachRopeAt(anchor.x, anchor.y);

    for (int degrees = 0; degrees < 360; degrees += 5) {
      // Put the player on a circle round the hook, so the rope leaves them at every angle in turn.
      Vector2 offset = new Vector2(6f, 0f).rotateDeg((float) degrees);
      player.setPosition(anchor.x - offset.x - 0.5f, anchor.y - offset.y - 0.5f);
      hold.update();
      SpriteBatch batch = mock(SpriteBatch.class);

      hold.render(batch);

      if (!hold.isHolding()) {
        continue;
      }
      ArgumentCaptor<Float> rotation = ArgumentCaptor.forClass(Float.class);
      verify(batch)
          .draw(
              any(Texture.class),
              anyFloat(),
              anyFloat(),
              anyFloat(),
              anyFloat(),
              anyFloat(),
              anyFloat(),
              anyFloat(),
              anyFloat(),
              rotation.capture(),
              anyInt(),
              anyInt(),
              anyInt(),
              anyInt(),
              anyBoolean(),
              anyBoolean());
      float turned = ((rotation.getValue() + 180f) % 360f + 360f) % 360f - 180f;
      // Lying sideways along a level rope is the furthest it may turn; any further and the head
      // would drop below the hands.
      assertTrue(
          Math.abs(turned) <= 90f, "turned " + turned + " degrees with the hook at " + degrees);
    }
  }

  @Test
  void shouldLeaveTheSpriteBatchAloneWhileNotOnARope() {
    SpriteBatch batch = mock(SpriteBatch.class);

    player.getComponent(GrappleHoldRenderComponent.class).update();
    player.getComponent(GrappleHoldRenderComponent.class).render(batch);

    verifyNoInteractions(batch);
  }

  @Test
  void shouldFinishTheGrappleShotBeforeHidingItsAnimationAndRestoreOnDetach() {
    assertActionCompletesBeforePose(
        () -> {
          player.getEvents().trigger("grappleChargeStart", Vector2.X);
          player.getEvents().trigger("grappleChargeFire", Vector2.X);
        },
        "bow_shoot");
  }

  @Test
  void shouldFinishLandingWhenTheRopeAttachesDuringRecovery() {
    assertActionCompletesBeforePose(
        () -> {
          player.getEvents().trigger("fallStart");
          player.getEvents().trigger("landed");
        },
        "jump_land");
  }

  private void assertActionCompletesBeforePose(Runnable startAction, String animation) {
    Files previousFiles = Gdx.files;
    Gdx.files = mock(Files.class);
    FileHandle file = mock(FileHandle.class);
    when(file.exists()).thenReturn(true);
    when(Gdx.files.internal("images/player/player_rope_hold.png")).thenReturn(file);
    try (var textures = mockConstruction(Texture.class)) {
      GrappleHoldRenderComponent hold = player.getComponent(GrappleHoldRenderComponent.class);
      AnimationRenderComponent animator = player.getComponent(AnimationRenderComponent.class);
      PlayerAnimationController controller = player.getComponent(PlayerAnimationController.class);
      hold.create();
      startAction.run();
      attachRope();
      hold.update();
      assertEquals(animation, animator.getCurrentAnimation());
      when(ServiceLocator.getTimeSource().getDeltaTime()).thenReturn(1f);
      animator.render(mock(SpriteBatch.class));
      controller.update();
      assertEquals("idle", animator.getCurrentAnimation());
      hold.update();
      org.junit.jupiter.api.Assertions.assertNull(animator.getCurrentAnimation());
      grapple.release();
      hold.update();
      assertEquals("idle", animator.getCurrentAnimation());
      player.getEvents().trigger("walk", Vector2.X);
      assertEquals("walk", animator.getCurrentAnimation());
      assertEquals(1, textures.constructed().size());
    } finally {
      Gdx.files = previousFiles;
    }
  }
}
