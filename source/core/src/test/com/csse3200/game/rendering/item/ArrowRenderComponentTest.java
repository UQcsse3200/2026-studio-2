package com.csse3200.game.rendering.item;

import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.floatThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class ArrowRenderComponentTest {
  private ResourceService resourceService;
  private SpriteBatch batch;

  @BeforeEach
  void setUp() {
    resourceService = mock(ResourceService.class);
    ServiceLocator.registerResourceService(resourceService);
    ServiceLocator.registerRenderService(mock(RenderService.class));
    batch = mock(SpriteBatch.class);
  }

  private Texture arrowTexture(int width, int height) {
    Texture texture = mock(Texture.class);
    when(texture.getWidth()).thenReturn(width);
    when(texture.getHeight()).thenReturn(height);
    when(resourceService.getAsset(ArrowType.STANDARD.getTexturePath(), Texture.class))
        .thenReturn(texture);
    return texture;
  }

  /** Float arguments come out of vector maths, so compare them with a little slack. */
  private static float near(float expected) {
    return floatThat(actual -> Math.abs(actual - expected) < 1e-4f);
  }

  @Test
  void shouldDrawStandardArrowAtEntityScale() {
    Texture texture = arrowTexture(300, 100);

    Entity arrow = new Entity().addComponent(new ArrowRenderComponent(ArrowType.STANDARD));
    arrow.setPosition(Vector2.Zero);
    arrow.setScale(0.3f, 0.1f);

    arrow.getComponent(ArrowRenderComponent.class).render(batch);

    verify(batch)
        .draw(
            eq(texture),
            eq(0f),
            eq(0f),
            eq(0.15f),
            eq(0.05f),
            eq(0.3f),
            eq(0.1f),
            eq(1f),
            eq(1f),
            near(-18.43495f),
            eq(0),
            eq(0),
            eq(300),
            eq(100),
            eq(false),
            eq(false));
  }

  @Test
  void shouldDrawAtTheProjectilesRealCentreAndFacingNotTheEntitysUnturnedOne() {
    Texture texture = arrowTexture(300, 100);
    ArrowProjectileComponent projectile = mock(ArrowProjectileComponent.class);
    // Flying straight up, centred at (4, 6) - nowhere near the entity's own recorded position.
    when(projectile.getWorldCenter()).thenReturn(new Vector2(4f, 6f));
    when(projectile.getCurrentDirection()).thenReturn(new Vector2(0f, 1f));

    Entity arrow =
        new Entity()
            .addComponent(projectile)
            .addComponent(new ArrowRenderComponent(ArrowType.STANDARD));
    arrow.setPosition(100f, 100f);
    arrow.setScale(0.6f, 0.3f);
    ArrowRenderComponent renderer = arrow.getComponent(ArrowRenderComponent.class);
    renderer.create();

    renderer.render(batch);

    // Bottom-left is the centre less half the size. Correct the stretched diagonal artwork so
    // its shaft points up, with rotation about its middle.
    verify(batch)
        .draw(
            eq(texture),
            near(3.7f),
            near(5.85f),
            near(0.3f),
            near(0.15f),
            near(0.6f),
            near(0.3f),
            eq(1f),
            eq(1f),
            near(63.43495f),
            eq(0),
            eq(0),
            eq(300),
            eq(100),
            eq(false),
            eq(false));
  }

  @Test
  void shouldScaleAWideSpriteToTheRenderSizeKeepingItsAspectRatio() {
    Texture texture = arrowTexture(300, 100);
    Entity arrow =
        new Entity().addComponent(new ArrowRenderComponent(ArrowType.STANDARD).setRenderSize(1f));
    arrow.setPosition(Vector2.Zero);
    arrow.setScale(0.3f, 0.1f);

    arrow.getComponent(ArrowRenderComponent.class).render(batch);

    // Longest edge is the width: 1 wide, a third as tall.
    verify(batch)
        .draw(
            eq(texture),
            anyFloat(),
            anyFloat(),
            near(0.5f),
            near(1f / 6f),
            near(1f),
            near(1f / 3f),
            eq(1f),
            eq(1f),
            anyFloat(),
            eq(0),
            eq(0),
            eq(300),
            eq(100),
            eq(false),
            eq(false));
  }

  @Test
  void shouldScaleATallSpriteToTheRenderSizeKeepingItsAspectRatio() {
    Texture texture = arrowTexture(100, 300);
    Entity arrow =
        new Entity().addComponent(new ArrowRenderComponent(ArrowType.STANDARD).setRenderSize(1f));
    arrow.setPosition(Vector2.Zero);
    arrow.setScale(0.3f, 0.1f);

    arrow.getComponent(ArrowRenderComponent.class).render(batch);

    // Longest edge is the height: 1 tall, a third as wide.
    verify(batch)
        .draw(
            eq(texture),
            anyFloat(),
            anyFloat(),
            near(1f / 6f),
            near(0.5f),
            near(1f / 3f),
            near(1f),
            eq(1f),
            eq(1f),
            anyFloat(),
            eq(0),
            eq(0),
            eq(100),
            eq(300),
            eq(false),
            eq(false));
  }

  @Test
  void shouldDrawNothingWhenTheSpriteCannotBeLoaded() {
    when(resourceService.getAsset(ArrowType.STANDARD.getTexturePath(), Texture.class))
        .thenThrow(new RuntimeException("not loaded"));
    Entity arrow = new Entity().addComponent(new ArrowRenderComponent(ArrowType.STANDARD));

    arrow.getComponent(ArrowRenderComponent.class).render(batch);

    verifyNoInteractions(batch);
  }

  @Test
  void shouldDefaultToAStandardArrowWhenNoTypeIsGiven() {
    Texture texture = arrowTexture(300, 100);
    Entity arrow = new Entity().addComponent(new ArrowRenderComponent());
    arrow.setScale(0.3f, 0.1f);

    arrow.getComponent(ArrowRenderComponent.class).render(batch);

    verify(batch)
        .draw(
            eq(texture),
            anyFloat(),
            anyFloat(),
            anyFloat(),
            anyFloat(),
            anyFloat(),
            anyFloat(),
            anyFloat(),
            anyFloat(),
            anyFloat(),
            anyInt(),
            anyInt(),
            anyInt(),
            anyInt(),
            eq(false),
            eq(false));
  }

  @Test
  void shouldDrawTheGrappleArrowWithTheSameSpriteAsAnyOtherArrow() {
    Texture texture = arrowTexture(26, 26);
    Entity arrow = new Entity().addComponent(new ArrowRenderComponent(ArrowType.GRAPPLE));
    arrow.setPosition(Vector2.Zero);
    arrow.setScale(0.6f, 0.3f);

    arrow.getComponent(ArrowRenderComponent.class).render(batch);

    // It used to be a one-pixel texture stretched into a light-grey box. Now it's the arrow sprite,
    // untinted, at the usual arrow size.
    verify(batch)
        .draw(
            eq(texture),
            eq(0f),
            eq(0f),
            eq(0.3f),
            eq(0.15f),
            eq(0.6f),
            eq(0.3f),
            eq(1f),
            eq(1f),
            near(-26.56505f),
            eq(0),
            eq(0),
            eq(26),
            eq(26),
            eq(false),
            eq(false));
    verify(batch).setColor(Color.WHITE);
    verify(batch, never()).setColor(Color.LIGHT_GRAY);
  }

  @Test
  void shouldDrawNothingForAGrappleArrowWhenTheSpriteCannotBeLoaded() {
    when(resourceService.getAsset(ArrowType.STANDARD.getTexturePath(), Texture.class))
        .thenThrow(new RuntimeException("not loaded"));
    Entity arrow = new Entity().addComponent(new ArrowRenderComponent(ArrowType.GRAPPLE));

    arrow.getComponent(ArrowRenderComponent.class).render(batch);

    verifyNoInteractions(batch);
  }
}
