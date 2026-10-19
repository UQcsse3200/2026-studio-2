package com.csse3200.game.components.level;

import static org.mockito.Mockito.*;

import com.badlogic.gdx.graphics.Texture;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.rendering.DynamicTextureRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class TriggerablePlatformComponentTest {

  private ResourceService resourceService;
  private Texture normalTexture;
  private Texture transparentTexture;
  private ColliderComponent collider;
  private DynamicTextureRenderComponent textureComponent;

  @BeforeEach
  void setUp() {
    resourceService = mock(ResourceService.class);
    normalTexture = mock(Texture.class);
    transparentTexture = mock(Texture.class);

    ServiceLocator.registerResourceService(resourceService);

    when(resourceService.getAsset("images/ui/transparent.png", Texture.class))
        .thenReturn(transparentTexture);
  }

  /** Creates a platform that starts out active or inactive, with mock collider and texture. */
  private Entity createPlatform(boolean active) {
    collider = mock(ColliderComponent.class);
    textureComponent = mock(DynamicTextureRenderComponent.class);
    ActivatableComponent activatable = mock(ActivatableComponent.class);

    when(textureComponent.getTexture()).thenReturn(normalTexture);
    when(activatable.isActive()).thenReturn(active);

    Entity entity =
        new Entity()
            .addComponent(collider)
            .addComponent(textureComponent)
            .addComponent(activatable)
            .addComponent(new TriggerablePlatformComponent());

    entity.create();
    return entity;
  }

  @Test
  void shouldCreateActivePlatform() {
    createPlatform(true);

    verify(collider).setLayer(PhysicsLayer.GROUND);
    verify(textureComponent).setTexture(normalTexture);
  }

  @Test
  void shouldCreateInactivePlatform() {
    createPlatform(false);

    verify(collider).setLayer(PhysicsLayer.NONE);
    verify(textureComponent).setTexture(transparentTexture);
  }

  @Test
  void shouldActivatePlatformFromEvent() {
    Entity entity = createPlatform(false);

    entity.getEvents().trigger("activatedMapComponent", true);

    verify(collider).setLayer(PhysicsLayer.GROUND);
    verify(textureComponent).setTexture(normalTexture);
  }

  @Test
  void shouldDeactivatePlatformFromEvent() {
    Entity entity = createPlatform(true);

    entity.getEvents().trigger("activatedMapComponent", false);

    verify(collider).setLayer(PhysicsLayer.NONE);
    verify(textureComponent).setTexture(transparentTexture);
  }

  @Test
  void shouldOnlyLoadTexturesOnce() {
    Entity entity = createPlatform(true);

    entity.getEvents().trigger("activatedMapComponent", false);
    entity.getEvents().trigger("activatedMapComponent", true);

    verify(resourceService, times(1)).getAsset("images/ui/transparent.png", Texture.class);
    verify(textureComponent, times(1)).getTexture();
  }
}
