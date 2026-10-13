package com.csse3200.game.rendering.item;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.projectile.ArrowProjectileComponent;
import com.csse3200.game.components.projectile.ArrowType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

@ExtendWith(GameExtension.class)
class ArrowSpriteAlignmentTest {
  private PhysicsService physics;
  private ResourceService resources;

  @BeforeEach
  void setUp() {
    physics = new PhysicsService();
    ServiceLocator.registerPhysicsService(physics);
    ServiceLocator.registerRenderService(mock(RenderService.class));
    resources = mock(ResourceService.class);
    ServiceLocator.registerResourceService(resources);
  }

  @AfterEach
  void tearDown() {
    physics.getPhysics().dispose();
  }

  @Test
  void diagonalSpriteShaftShouldFaceTheVelocityThroughoutFlight() {
    for (ArrowType type :
        new ArrowType[] {ArrowType.STANDARD, ArrowType.GRAPPLE, ArrowType.POISON}) {
      verifySpriteAlignment(type);
    }
  }

  @Test
  void horizontalElementalSpriteShaftShouldFaceTheVelocityThroughoutFlight() {
    for (ArrowType type : new ArrowType[] {ArrowType.FIRE, ArrowType.ICE}) {
      verifySpriteAlignment(type);
    }
  }

  private void verifySpriteAlignment(ArrowType type) {
    Texture texture = mock(Texture.class);
    // Standard, grapple and poison use diagonal arrow.png; elemental textures point right.
    boolean diagonal = type != ArrowType.FIRE && type != ArrowType.ICE;
    when(texture.getWidth()).thenReturn(diagonal ? 1280 : 2048);
    when(texture.getHeight()).thenReturn(diagonal ? 1280 : 683);
    when(resources.getAsset(type.getTexturePath(), Texture.class)).thenReturn(texture);
    PhysicsComponent body = new PhysicsComponent();
    ArrowRenderComponent renderer = new ArrowRenderComponent(type);
    Entity arrow =
        new Entity()
            .addComponent(body)
            .addComponent(new ArrowProjectileComponent(Vector2.X, 10f, 50f, type))
            .addComponent(renderer);
    arrow.setScale(0.6f, 0.3f);
    arrow.create();

    for (Vector2 velocity :
        new Vector2[] {
          new Vector2(10f, 0f), new Vector2(0f, 10f), new Vector2(-10f, 3f), new Vector2(5f, -8f)
        }) {
      body.getBody().setLinearVelocity(velocity);
      SpriteBatch batch = mock(SpriteBatch.class);
      renderer.render(batch);
      ArgumentCaptor<Float> width = ArgumentCaptor.forClass(Float.class);
      ArgumentCaptor<Float> height = ArgumentCaptor.forClass(Float.class);
      ArgumentCaptor<Float> rotation = ArgumentCaptor.forClass(Float.class);
      verify(batch)
          .draw(
              eq(texture),
              anyFloat(),
              anyFloat(),
              anyFloat(),
              anyFloat(),
              width.capture(),
              height.capture(),
              eq(1f),
              eq(1f),
              rotation.capture(),
              eq(0),
              eq(0),
              anyInt(),
              anyInt(),
              eq(false),
              eq(false));

      // The shaft runs bottom-left to top-right in arrow.png, and left-to-right otherwise.
      // Recover its displayed direction after scaling and rotating, rather than just checking
      // the rotation constant. This also catches a skew caused by a rectangular draw size.
      Vector2 displayedShaft =
          new Vector2(width.getValue(), diagonal ? height.getValue() : 0f)
              .rotateDeg(rotation.getValue())
              .nor();
      assertTrue(
          displayedShaft.epsilonEquals(velocity.cpy().nor(), 0.001f),
          type + " shaft " + displayedShaft + " should face " + velocity);
    }
  }
}
