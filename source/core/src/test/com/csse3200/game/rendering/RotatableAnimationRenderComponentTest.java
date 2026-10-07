package com.csse3200.game.rendering;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.csse3200.game.components.level.RotatableMapComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class RotatableAnimationRenderComponentTest {

  private TextureAtlas atlas;
  private SpriteBatch batch;
  private GameTime timeSource;
  private RenderService renderService;

  @BeforeEach
  void setUp() {
    atlas = mock(TextureAtlas.class);
    batch = mock(SpriteBatch.class);
    timeSource = mock(GameTime.class);
    renderService = mock(RenderService.class);

    ServiceLocator.registerTimeSource(timeSource);
    ServiceLocator.registerRenderService(renderService);

    when(batch.getColor()).thenReturn(new Color(Color.WHITE));
    when(timeSource.getDeltaTime()).thenReturn(0.1f);
  }

  private TestSetup createSetup(float rotation) {
    RotatableAnimationRenderComponent component = new RotatableAnimationRenderComponent(atlas);

    RotatableMapComponent rotateComponent = new RotatableMapComponent(rotation);

    Entity entity = new Entity().addComponent(rotateComponent).addComponent(component);

    entity.create();

    return new TestSetup(component, entity);
  }

  private TextureRegion createRegion(int width, int height) {
    Texture texture = mock(Texture.class);

    when(texture.getWidth()).thenReturn(width);
    when(texture.getHeight()).thenReturn(height);

    return new TextureRegion(texture, 0, 0, width, height);
  }

  private static class TestSetup {
    final RotatableAnimationRenderComponent component;
    final Entity entity;

    TestSetup(RotatableAnimationRenderComponent component, Entity entity) {
      this.component = component;
      this.entity = entity;
    }
  }

  @Test
  void shouldRegisterWithRenderServiceOnCreate() {
    TestSetup setup = createSetup(45f);

    verify(renderService).register(setup.component);
  }

  @Test
  void shouldNotDrawWhenNoAnimationIsPlaying() {
    TestSetup setup = createSetup(45f);

    setup.component.draw(batch);

    verify(batch, never())
        .draw(
            any(TextureRegion.class),
            anyFloat(),
            anyFloat(),
            anyFloat(),
            anyFloat(),
            anyFloat(),
            anyFloat(),
            anyFloat(),
            anyFloat(),
            anyFloat());
  }

  @Test
  void shouldDrawUsingRotationFromMapComponent() {
    TestSetup setup = createSetup(90f);

    TextureRegion region = createRegion(32, 32);
    Animation<TextureRegion> animation = mock(Animation.class);

    when(animation.getKeyFrame(anyFloat())).thenReturn(region);

    setup.component.currentAnimation = animation;
    setup.component.defaultRegionWidthPx = 0f;

    setup.entity.setPosition(2f, 3f);
    setup.entity.setScale(4f, 6f);

    setup.component.draw(batch);

    verify(batch)
        .draw(
            any(TextureRegion.class),
            eq(2f),
            eq(3f),
            eq(2f),
            eq(3f),
            eq(4f),
            eq(6f),
            eq(1f),
            eq(1f),
            eq(90f));
  }

  @Test
  void shouldUseTextureRegionAspectRatioWhenDefaultWidthIsSet() {
    TestSetup setup = createSetup(30f);

    TextureRegion region = createRegion(50, 25);
    Animation<TextureRegion> animation = mock(Animation.class);

    when(animation.getKeyFrame(anyFloat())).thenReturn(region);

    setup.component.currentAnimation = animation;
    setup.component.defaultRegionWidthPx = 100f;

    setup.entity.setPosition(1f, 2f);
    setup.entity.setScale(4f, 8f);

    setup.component.draw(batch);

    verify(batch)
        .draw(
            any(TextureRegion.class),
            eq(1f),
            eq(2f),
            eq(1f),
            eq(0.5f),
            eq(2f),
            eq(1f),
            eq(1f),
            eq(1f),
            eq(30f));
  }

  @Test
  void shouldAdvanceAnimationTimeAfterDrawing() {
    TestSetup setup = createSetup(0f);

    TextureRegion region = createRegion(32, 32);
    Animation<TextureRegion> animation = mock(Animation.class);

    when(animation.getKeyFrame(anyFloat())).thenReturn(region);
    when(timeSource.getDeltaTime()).thenReturn(0.25f);

    setup.component.currentAnimation = animation;
    setup.component.defaultRegionWidthPx = 0f;

    float before = setup.component.animationPlayTime;

    setup.component.draw(batch);

    assertEquals(before + 0.25f, setup.component.animationPlayTime, 0.001f);
  }

  @Test
  void shouldRestoreBatchColorAfterDrawing() {
    TestSetup setup = createSetup(0f);

    TextureRegion region = createRegion(32, 32);
    Animation<TextureRegion> animation = mock(Animation.class);

    Color originalColor = new Color(0.2f, 0.3f, 0.4f, 1f);

    when(batch.getColor()).thenReturn(originalColor);
    when(animation.getKeyFrame(anyFloat())).thenReturn(region);

    setup.component.currentAnimation = animation;
    setup.component.defaultRegionWidthPx = 0f;

    setup.component.draw(batch);

    verify(batch).setColor(originalColor);
  }
}
