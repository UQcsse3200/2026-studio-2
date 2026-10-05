package com.csse3200.game.rendering.item;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.projectile.ArrowProjectileComponent;
import com.csse3200.game.components.projectile.ArrowType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class ArrowRenderBehaviourTest {
  private ResourceService resources;
  private SpriteBatch batch;

  @BeforeEach
  void setUp() {
    resources = mock(ResourceService.class);
    batch = mock(SpriteBatch.class);
    ServiceLocator.registerResourceService(resources);
    ServiceLocator.registerRenderService(mock(RenderService.class));
  }

  private Texture texture(int width, int height) {
    Texture texture = mock(Texture.class);
    when(texture.getWidth()).thenReturn(width);
    when(texture.getHeight()).thenReturn(height);
    when(resources.getAsset("images/projectiles/arrow.png", Texture.class)).thenReturn(texture);
    return texture;
  }

  private ArrowRenderComponent renderer(Float size, ArrowProjectileComponent projectile) {
    ArrowRenderComponent renderer = new ArrowRenderComponent();
    if (size != null) renderer.setRenderSize(size);
    Entity entity = new Entity().addComponent(renderer);
    entity.setScale(2f, 2f);
    entity.setPosition(0f, 0f); // Center at (1, 1).
    if (projectile != null) entity.addComponent(projectile);
    renderer.create();
    return renderer;
  }

  @Test
  void shouldPreserveLandscapeAspectRatioAndCenter() {
    Texture texture = texture(200, 100);
    renderer(2f, null).render(batch);
    verify(batch)
        .draw(texture, 0f, 0.5f, 1f, 0.5f, 2f, 1f, 1f, 1f, 0f, 0, 0, 200, 100, false, false);
  }

  @Test
  void shouldPreservePortraitAspectRatioAndCenter() {
    Texture texture = texture(100, 200);
    renderer(2f, null).render(batch);
    verify(batch)
        .draw(texture, 0.5f, 0f, 0.5f, 1f, 1f, 2f, 1f, 1f, 0f, 0, 0, 100, 200, false, false);
  }

  @Test
  void shouldRotateSpriteToProjectileDirection() {
    Texture texture = texture(100, 100);
    ArrowProjectileComponent projectile = new ArrowProjectileComponent(Vector2.Y, 10f, 20f);
    renderer(null, projectile).render(batch);
    verify(batch).draw(texture, 0f, 0f, 1f, 1f, 2f, 2f, 1f, 1f, 90f, 0, 0, 100, 100, false, false);
  }

  @Test
  void shouldRetryTextureAfterTemporaryLoadFailure() {
    Texture texture = texture(100, 100);
    when(resources.getAsset("images/projectiles/arrow.png", Texture.class))
        .thenThrow(new IllegalStateException("Asset not loaded yet"))
        .thenReturn(texture);
    ArrowRenderComponent renderer = renderer(null, null);
    renderer.render(batch);
    verifyNoInteractions(batch);
    renderer.render(batch);
    verify(batch).draw(texture, 0f, 0f, 1f, 1f, 2f, 2f, 1f, 1f, 0f, 0, 0, 100, 100, false, false);
  }

  @Test
  void shouldReuseLoadedTextureOnLaterFrames() {
    Texture texture = texture(100, 100);
    ArrowRenderComponent renderer = renderer(null, null);
    renderer.render(batch);
    renderer.render(batch);
    verify(resources, times(1)).getAsset("images/projectiles/arrow.png", Texture.class);
    verify(batch, times(2))
        .draw(texture, 0f, 0f, 1f, 1f, 2f, 2f, 1f, 1f, 0f, 0, 0, 100, 100, false, false);
  }

  @Test
  void shouldSkipDrawingWithoutResourceService() {
    ServiceLocator.clear();
    Entity entity = new Entity().addComponent(new ArrowRenderComponent(ArrowType.FIRE));
    entity.getComponent(ArrowRenderComponent.class).render(batch);
    verifyNoInteractions(batch);
  }

  @Test
  void shouldDrawTheGrappleArrowWithTheArrowSpriteInsteadOfAPlainBox() {
    Texture texture = mock(Texture.class);
    when(texture.getWidth()).thenReturn(26);
    when(texture.getHeight()).thenReturn(26);
    when(resources.getAsset(ArrowType.GRAPPLE.getTexturePath(), Texture.class)).thenReturn(texture);
    ArrowRenderComponent renderer = new ArrowRenderComponent(ArrowType.GRAPPLE).setRenderSize(2f);
    Entity entity = new Entity().addComponent(renderer);
    entity.setPosition(0f, 0f);
    entity.setScale(2f, 2f);

    renderer.render(batch);

    // It used to be a one-pixel texture stretched into a light-grey box. Now it's the arrow sprite,
    // untinted, sized like any other arrow.
    verify(batch).setColor(Color.WHITE);
    verify(batch, never()).setColor(Color.LIGHT_GRAY);
    verify(batch).draw(texture, 0f, 0f, 1f, 1f, 2f, 2f, 1f, 1f, 0f, 0, 0, 26, 26, false, false);
  }
}
