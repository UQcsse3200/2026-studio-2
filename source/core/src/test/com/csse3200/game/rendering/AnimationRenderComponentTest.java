package com.csse3200.game.rendering;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class AnimationRenderComponentTest {
  @Test
  void shouldAddRemoveAnimation() {
    TextureAtlas atlas = createMockAtlas("test_name", 1);
    AnimationRenderComponent animator = new AnimationRenderComponent(atlas);

    assertTrue(animator.addAnimation("test_name", 0.1f));
    assertTrue(animator.removeAnimation("test_name"));
    assertFalse(animator.removeAnimation("test_name"));
  }

  @Test
  void shouldFailRemoveInvalidAnimation() {
    TextureAtlas atlas = mock(TextureAtlas.class);
    when(atlas.findRegions("test_name")).thenReturn(null);
    AnimationRenderComponent animator = new AnimationRenderComponent(atlas);

    assertFalse(animator.addAnimation("test_name", 0.1f));
    assertFalse(animator.removeAnimation("test_name"));
  }

  @Test
  void shouldFailDuplicateAddAnimation() {
    TextureAtlas atlas = createMockAtlas("test_name", 1);
    AnimationRenderComponent animator = new AnimationRenderComponent(atlas);

    assertTrue(animator.addAnimation("test_name", 0.1f));
    assertFalse(animator.addAnimation("test_name", 0.2f));
  }

  @Test
  void shouldHaveAnimation() {
    TextureAtlas atlas = createMockAtlas("test_name", 1);
    AnimationRenderComponent animator = new AnimationRenderComponent(atlas);

    animator.addAnimation("test_name", 0.1f);
    assertTrue(animator.hasAnimation("test_name"));
    animator.removeAnimation("test_name");
    assertFalse(animator.hasAnimation("test_name"));
  }

  @Test
  void shouldPlayAnimation() {
    int numFrames = 5;
    String animName = "test_name";
    float frameTime = 1f;
    int frameWidth = 10;
    int frameHeight = 20;

    // Real (non-mock) regions backed by a mock texture, so each frame has distinct,
    // inspectable UV coordinates.
    Texture texture = mock(Texture.class);
    when(texture.getWidth()).thenReturn(frameWidth * numFrames);
    when(texture.getHeight()).thenReturn(frameHeight);

    Array<AtlasRegion> regions = new Array<>(numFrames);
    for (int i = 0; i < numFrames; i++) {
      regions.add(new AtlasRegion(texture, i * frameWidth, 0, frameWidth, frameHeight));
    }
    TextureAtlas atlas = mock(TextureAtlas.class);
    when(atlas.findRegions(animName)).thenReturn(regions);

    SpriteBatch batch = mock(SpriteBatch.class);

    // Mock game time
    GameTime gameTime = mock(GameTime.class);
    ServiceLocator.registerTimeSource(gameTime);
    when(gameTime.getDeltaTime()).thenReturn(frameTime);

    // Start animation
    AnimationRenderComponent animator = new AnimationRenderComponent(atlas);
    Entity entity = new Entity();
    entity.setScale(3f, 999f);
    animator.setEntity(entity);
    animator.addAnimation(animName, frameTime);
    animator.startAnimation(animName);

    float expectedWidth = entity.getScale().x;
    float expectedHeight = entity.getScale().y;

    for (int i = 0; i < numFrames; i++) {
      // Each draw advances 1 frame, check that it matches for each
      animator.draw(batch);
      AtlasRegion expected = regions.get(i);
      // v/v2 are swapped relative to the region's own fields, matching what
      // SpriteBatch#draw(TextureRegion, ...) does internally so the frame isn't drawn upside down.
      verify(batch)
          .draw(
              expected.getTexture(),
              entity.getPosition().x,
              entity.getPosition().y,
              expectedWidth,
              expectedHeight,
              expected.getU(),
              expected.getV2(),
              expected.getU2(),
              expected.getV());
    }
  }

  @Test
  void shouldScaleFramesUniformlyAfterScaleEntity() {
    String animName = "test_name";
    float frameTime = 1f;

    Texture texture = mock(Texture.class);
    when(texture.getWidth()).thenReturn(100);
    when(texture.getHeight()).thenReturn(100);

    AtlasRegion defaultRegion = new AtlasRegion(texture, 0, 0, 10, 40);
    AtlasRegion sameSizeAsDefault = new AtlasRegion(texture, 0, 40, 10, 40);
    AtlasRegion biggerAndDifferentAspect = new AtlasRegion(texture, 0, 60, 20, 20);
    Array<AtlasRegion> regions = new Array<>(2);
    regions.add(sameSizeAsDefault);
    regions.add(biggerAndDifferentAspect);

    TextureAtlas atlas = mock(TextureAtlas.class);
    when(atlas.findRegion("default")).thenReturn(defaultRegion);
    when(atlas.findRegions(animName)).thenReturn(regions);

    SpriteBatch batch = mock(SpriteBatch.class);

    GameTime gameTime = mock(GameTime.class);
    ServiceLocator.registerTimeSource(gameTime);
    when(gameTime.getDeltaTime()).thenReturn(frameTime);

    AnimationRenderComponent animator = new AnimationRenderComponent(atlas);
    Entity entity = new Entity();
    animator.setEntity(entity);
    animator.scaleEntity();
    animator.addAnimation(animName, frameTime);
    animator.startAnimation(animName);

    animator.draw(batch); // frame 0: same size as default (10x40) -> 1 x 4
    verify(batch)
        .draw(
            eq(texture),
            eq(entity.getPosition().x),
            eq(entity.getPosition().y),
            eq(1f),
            eq(4f),
            anyFloat(),
            anyFloat(),
            anyFloat(),
            anyFloat());

    animator.draw(batch); // frame 1: 20x20, double default's pixel width -> 2 x 2, not squashed
    verify(batch)
        .draw(
            eq(texture),
            eq(entity.getPosition().x),
            eq(entity.getPosition().y),
            eq(2f),
            eq(2f),
            anyFloat(),
            anyFloat(),
            anyFloat(),
            anyFloat());
  }

  @Test
  void shouldFlipHorizontally() {
    String animName = "test_name";
    float frameTime = 1f;
    int frameWidth = 10;

    Texture texture = mock(Texture.class);
    when(texture.getWidth()).thenReturn(frameWidth);
    when(texture.getHeight()).thenReturn(frameWidth);

    AtlasRegion region = new AtlasRegion(texture, 0, 0, frameWidth, frameWidth);
    Array<AtlasRegion> regions = new Array<>(1);
    regions.add(region);
    TextureAtlas atlas = mock(TextureAtlas.class);
    when(atlas.findRegions(animName)).thenReturn(regions);

    SpriteBatch batch = mock(SpriteBatch.class);

    GameTime gameTime = mock(GameTime.class);
    ServiceLocator.registerTimeSource(gameTime);
    when(gameTime.getDeltaTime()).thenReturn(frameTime);

    AnimationRenderComponent animator = new AnimationRenderComponent(atlas);
    Entity entity = new Entity();
    animator.setEntity(entity);
    animator.addAnimation(animName, frameTime);
    animator.startAnimation(animName);

    animator.setFlipX(true);
    animator.draw(batch);

    // Flipping swaps u and u2 relative to the unflipped region; v/v2 are still swapped relative
    // to the region's own fields (see shouldPlayAnimation), independent of the flip.
    verify(batch)
        .draw(
            region.getTexture(),
            entity.getPosition().x,
            entity.getPosition().y,
            entity.getScale().x,
            entity.getScale().y,
            region.getU2(),
            region.getV2(),
            region.getU(),
            region.getV());
  }

  @Test
  void shouldFinish() {
    TextureAtlas atlas = createMockAtlas("test_name", 1);
    SpriteBatch batch = mock(SpriteBatch.class);

    GameTime gameTime = mock(GameTime.class);
    ServiceLocator.registerTimeSource(gameTime);
    when(gameTime.getDeltaTime()).thenReturn(1f);

    AnimationRenderComponent animator = new AnimationRenderComponent(atlas);
    Entity entity = new Entity();
    animator.setEntity(entity);
    animator.addAnimation("test_name", 1f);
    assertFalse(animator.isFinished());

    animator.startAnimation("test_name");
    assertFalse(animator.isFinished());

    animator.draw(batch);
    assertTrue(animator.isFinished());
  }

  @Test
  void shouldStopAnimation() {
    TextureAtlas atlas = createMockAtlas("test_name", 1);
    AnimationRenderComponent animator = new AnimationRenderComponent(atlas);
    animator.addAnimation("test_name", 1f);
    assertFalse(animator.stopAnimation());

    animator.startAnimation("test_name");
    assertTrue(animator.stopAnimation());
    assertNull(animator.getCurrentAnimation());
  }

  static TextureAtlas createMockAtlas(String animationName, int numRegions) {
    TextureAtlas atlas = mock(TextureAtlas.class);
    Array<AtlasRegion> regions = new Array<>(numRegions);
    for (int i = 0; i < numRegions; i++) {
      regions.add(mock(AtlasRegion.class));
    }
    when(atlas.findRegions(animationName)).thenReturn(regions);
    return atlas;
  }
}
