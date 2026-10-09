package com.csse3200.game.rendering.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
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
