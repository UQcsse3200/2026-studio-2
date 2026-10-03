package com.csse3200.game.components.level;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CheckpointComponentTest {
  /** Note: this test relies on checkpoint collect range being +- 1 for both x and y */
  private boolean collected;

  private GridPoint2 position;
  private Entity player;
  private Entity checkpointEntity;
  CheckpointComponent checkpointComponent;
  TextureRenderComponent textureRenderComponent;

  @BeforeEach
  void beforeEach() {
    Array<Entity> entities = new Array<>();
    checkpointComponent = new CheckpointComponent(false, new GridPoint2(10, 10));
    textureRenderComponent = mock(TextureRenderComponent.class);
    checkpointEntity = new Entity();
    checkpointEntity.addComponent(checkpointComponent);
    checkpointEntity.addComponent(textureRenderComponent);
    player = new Entity();
    player.addComponent(new PlayerActions());
    entities.add(player);
    EntityService entityService = mock(EntityService.class);
    when(entityService.getEntities()).thenReturn(entities);
    ServiceLocator.registerEntityService(entityService);

    ResourceService resourceService = mock(ResourceService.class);
    Texture mockTexture = mock(Texture.class);
    when(resourceService.getAsset("images/checkpoint_lit.png", Texture.class))
        .thenReturn(mockTexture);
    ServiceLocator.registerResourceService(resourceService);
  }

  @Test
  void shouldBeCollected() {
    assertFalse(checkpointComponent.isActive());
    player.setPosition(10, 10);
    checkpointComponent.update();
    assertTrue(checkpointComponent.isActive());
    checkpointComponent.deactivate();

    player.setPosition(10.99f, 10);
    checkpointComponent.update();
    assertTrue(checkpointComponent.isActive());
    checkpointComponent.deactivate();

    player.setPosition(9.01f, 10);
    checkpointComponent.update();
    assertTrue(checkpointComponent.isActive());
    checkpointComponent.deactivate();

    player.setPosition(10, 10.99f);
    checkpointComponent.update();
    assertTrue(checkpointComponent.isActive());
    checkpointComponent.deactivate();

    player.setPosition(10, 9.01f);
    checkpointComponent.update();
    assertTrue(checkpointComponent.isActive());
    checkpointComponent.deactivate();
  }

  @Test
  void shouldNotBeCollected() {
    player.setPosition(11, 10);
    checkpointComponent.update();
    assertFalse(checkpointComponent.isActive());
    player.setPosition(9, 10);
    checkpointComponent.update();
    assertFalse(checkpointComponent.isActive());
    player.setPosition(10, 11);
    checkpointComponent.update();
    assertFalse(checkpointComponent.isActive());
    player.setPosition(10, 9);
    checkpointComponent.update();
    assertFalse(checkpointComponent.isActive());
    player.setPosition(9, 9);
    checkpointComponent.update();
    assertFalse(checkpointComponent.isActive());
    player.setPosition(11, 11);
    checkpointComponent.update();
    assertFalse(checkpointComponent.isActive());
    player.setPosition(9, 11);
    checkpointComponent.update();
    assertFalse(checkpointComponent.isActive());
    player.setPosition(11, 9);
    checkpointComponent.update();
    assertFalse(checkpointComponent.isActive());
  }
}
