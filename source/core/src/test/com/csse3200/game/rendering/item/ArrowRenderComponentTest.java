package com.csse3200.game.rendering.item;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.projectile.ArrowType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class ArrowRenderComponentTest {
  @Test
  void shouldDrawStandardArrowAtEntityScale() {
    Texture texture = mock(Texture.class);
    when(texture.getWidth()).thenReturn(300);
    when(texture.getHeight()).thenReturn(100);

    ResourceService resourceService = mock(ResourceService.class);
    when(resourceService.getAsset("images/arrow.png", Texture.class)).thenReturn(texture);
    ServiceLocator.registerResourceService(resourceService);

    SpriteBatch batch = mock(SpriteBatch.class);
    Entity arrow = new Entity().addComponent(new ArrowRenderComponent(ArrowType.STANDARD));
    arrow.setPosition(Vector2.Zero);
    arrow.setScale(0.3f, 0.1f);

    arrow.getComponent(ArrowRenderComponent.class).render(batch);

    verify(batch)
        .draw(texture, 0f, 0f, 0.15f, 0.05f, 0.3f, 0.1f, 1f, 1f, 0f, 0, 0, 300, 100, false, false);
  }
}
