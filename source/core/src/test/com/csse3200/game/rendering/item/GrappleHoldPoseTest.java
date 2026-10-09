package com.csse3200.game.rendering.item;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.item.weapons.bow.grapple.GrappleComponent;
import com.csse3200.game.components.player.PlayerAnimationController;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ServiceLocator;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

/** Checks rope-pose alignment with texture construction intercepted at the graphics boundary. */
@ExtendWith(GameExtension.class)
class GrappleHoldPoseTest {
  private Files previousFiles;
  private GrappleComponent grapple;
  private AnimationRenderComponent animator;
  private PlayerAnimationController controller;
  private GrappleHoldRenderComponent renderer;
  private SpriteBatch batch;

  @BeforeEach
  void setUp() {
    previousFiles = Gdx.files;
    Gdx.files = mock(Files.class);
    FileHandle file = mock(FileHandle.class);
    when(file.exists()).thenReturn(true);
    when(Gdx.files.internal("images/player/player_rope_hold.png")).thenReturn(file);
    ServiceLocator.registerRenderService(mock(RenderService.class));
    grapple = mock(GrappleComponent.class);
    when(grapple.isAttached()).thenReturn(true);
    when(grapple.getRopePath())
        .thenReturn(List.of(new Vector2(0.5f, 0.5f), new Vector2(0.5f, 5.5f)));
    animator = mock(AnimationRenderComponent.class);
    when(animator.getCurrentAnimation()).thenReturn("walk");
    controller = mock(PlayerAnimationController.class);
    renderer = new GrappleHoldRenderComponent();
    new Entity()
        .addComponent(grapple)
        .addComponent(animator)
        .addComponent(controller)
        .addComponent(renderer);
    batch = mock(SpriteBatch.class);
  }

  @AfterEach
  void restoreFiles() {
    Gdx.files = previousFiles;
  }

  @Test
  void shouldAlignMirroredHandsToTheRopeAndDisposeTextureOnce() {
    when(animator.isFlipX()).thenReturn(true);
    try (var textures = mockConstruction(Texture.class)) {
      renderer.create();
      renderer.update();
      renderer.render(batch);
      Texture texture = textures.constructed().getFirst();
      verify(texture).setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
      verify(animator).stopAnimation();
      assertPose(texture, 0.5f, 1.05f, -1f, 0f);
      renderer.dispose();
      renderer.dispose();
      verify(texture).dispose();
      clearInvocations(batch);
      renderer.render(batch);
      verifyNoInteractions(batch);
    }
  }

  @Test
  void shouldFollowFirstUsableBendWhenPathRunsFromAnchorToPlayer() {
    when(grapple.getRopePath())
        .thenReturn(
            List.of(
                new Vector2(-5f, 7f), new Vector2(-3f, 0.5f),
                new Vector2(0.45f, 0.5f), new Vector2(0.5f, 0.5f)));
    try (var textures = mockConstruction(Texture.class)) {
      renderer.create();
      renderer.update();
      renderer.render(batch);
      assertPose(textures.constructed().getFirst(), -0.05f, 0.5f, 1f, 90f);
    }
  }

  @Test
  void shouldLeaveHurtAndDeathAnimationsVisible() {
    try (var textures = mockConstruction(Texture.class)) {
      renderer.create();
      for (String animation : List.of("hurt", "death")) {
        when(animator.getCurrentAnimation()).thenReturn(animation);
        renderer.update();
        renderer.render(batch);
      }
      verify(animator, never()).stopAnimation();
      verifyNoInteractions(batch);
      assertEquals(1, textures.constructed().size());
    }
  }

  @Test
  void shouldNotDrawWithoutAUsableRopeDirection() {
    try (var textures = mockConstruction(Texture.class)) {
      renderer.create();
      when(grapple.getRopePath()).thenReturn(null);
      renderer.update();
      renderer.render(batch);
      for (List<Vector2> path :
          List.of(
              List.<Vector2>of(),
              List.of(new Vector2(0.5f, 0.5f)),
              List.of(new Vector2(0.5f, 0.5f), new Vector2(0.51f, 0.5f)))) {
        when(grapple.getRopePath()).thenReturn(path);
        renderer.update();
        renderer.render(batch);
      }
      verify(animator, never()).stopAnimation();
      verifyNoInteractions(batch);
      assertEquals(1, textures.constructed().size());
    }
  }

  @Test
  void shouldRestoreAnimationOnceOnDetachAndStopDrawing() {
    try (var textures = mockConstruction(Texture.class)) {
      renderer.create();
      renderer.update();
      when(grapple.isAttached()).thenReturn(false);
      renderer.update();
      renderer.update();
      renderer.render(batch);
      verify(controller).refreshAnimation();
      verifyNoInteractions(batch);
      assertEquals(1, textures.constructed().size());
    }
  }

  @Test
  void missingFilesOrTexturePreventsDrawing() {
    Files files = Gdx.files;
    Gdx.files = null;
    renderer.create();
    renderer.update();
    renderer.render(batch);
    Gdx.files = files;
    when(files.internal("images/player/player_rope_hold.png").exists()).thenReturn(false);
    renderer.create();
    renderer.update();
    renderer.render(batch);
    verifyNoInteractions(batch);
  }

  @Test
  void missingGrappleOrAnimatorPreventsDrawing() {
    try (var textures = mockConstruction(Texture.class)) {
      GrappleHoldRenderComponent noGrapple = new GrappleHoldRenderComponent();
      new Entity().addComponent(animator).addComponent(noGrapple);
      noGrapple.create();
      noGrapple.update();
      noGrapple.render(batch);
      GrappleHoldRenderComponent noAnimator = new GrappleHoldRenderComponent();
      new Entity().addComponent(grapple).addComponent(noAnimator);
      noAnimator.create();
      noAnimator.update();
      noAnimator.render(batch);
      verifyNoInteractions(batch);
      assertEquals(2, textures.constructed().size());
    }
  }

  @Test
  void ropePoseCanDetachWithoutAnAnimationController() {
    GrappleHoldRenderComponent noController = new GrappleHoldRenderComponent();
    new Entity().addComponent(grapple).addComponent(animator).addComponent(noController);
    try (var textures = mockConstruction(Texture.class)) {
      noController.create();
      noController.update();
      verify(animator).stopAnimation();
      when(grapple.isAttached()).thenReturn(false);
      noController.update();
      noController.render(batch);
      verifyNoInteractions(batch);
      assertEquals(1, textures.constructed().size());
    }
  }

  private void assertPose(Texture texture, float gripX, float gripY, float flip, float angle) {
    float originX = 127.5f * 0.0138f;
    float originY = 131f * 0.0138f;
    float size = 256f * 0.0138f;
    ArgumentCaptor<Float> x = ArgumentCaptor.forClass(Float.class);
    ArgumentCaptor<Float> y = ArgumentCaptor.forClass(Float.class);
    ArgumentCaptor<Float> rotation = ArgumentCaptor.forClass(Float.class);
    verify(batch)
        .draw(
            eq(texture),
            x.capture(),
            y.capture(),
            eq(originX),
            eq(originY),
            eq(size),
            eq(size),
            eq(flip),
            eq(1f),
            rotation.capture(),
            eq(0),
            eq(0),
            eq(256),
            eq(256),
            eq(false),
            eq(false));
    assertEquals(gripX, x.getValue() + originX, 0.0001f);
    assertEquals(gripY, y.getValue() + originY, 0.0001f);
    assertEquals(angle, rotation.getValue(), 0.0001f);
  }
}
