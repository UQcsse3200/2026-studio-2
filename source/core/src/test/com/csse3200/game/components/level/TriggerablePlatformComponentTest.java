package com.csse3200.game.components.level;

import static org.junit.jupiter.api.Assertions.*;
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

  @BeforeEach
  void setUp() {
    resourceService = mock(ResourceService.class);
    normalTexture = mock(Texture.class);
    transparentTexture = mock(Texture.class);

    ServiceLocator.registerResourceService(resourceService);

    when(resourceService.getAsset("images/ui/transparent.png", Texture.class))
        .thenReturn(transparentTexture);
  }

  @Test
  void shouldCreateActivePlatform() {
    ColliderComponent collider = mock(ColliderComponent.class);
    DynamicTextureRenderComponent textureComponent =
        mock(DynamicTextureRenderComponent.class);
    ActivatableComponent activatable = mock(ActivatableComponent.class);

    when(textureComponent.getTexture()).thenReturn(normalTexture);
    when(activatable.isActive()).thenReturn(true);

    TriggerablePlatformComponent component =
        new TriggerablePlatformComponent();

    Entity entity =
        new Entity()
            .addComponent(collider)
            .addComponent(textureComponent)
            .addComponent(activatable)
            .addComponent(component);

    entity.create();

    verify(collider).setLayer(PhysicsLayer.GROUND);
    verify(textureComponent).setTexture(normalTexture);
  }

  @Test
  void shouldCreateInactivePlatform() {
    ColliderComponent collider = mock(ColliderComponent.class);
    DynamicTextureRenderComponent textureComponent =
        mock(DynamicTextureRenderComponent.class);
    ActivatableComponent activatable = mock(ActivatableComponent.class);

    when(textureComponent.getTexture()).thenReturn(normalTexture);
    when(activatable.isActive()).thenReturn(false);

    TriggerablePlatformComponent component =
        new TriggerablePlatformComponent();

    Entity entity =
        new Entity()
            .addComponent(collider)
            .addComponent(textureComponent)
            .addComponent(activatable)
            .addComponent(component);

    entity.create();

    verify(collider).setLayer(PhysicsLayer.NONE);
    verify(textureComponent).setTexture(transparentTexture);
  }

  @Test
  void shouldActivatePlatformFromEvent() {
    ColliderComponent collider = mock(ColliderComponent.class);
    DynamicTextureRenderComponent textureComponent =
        mock(DynamicTextureRenderComponent.class);
    ActivatableComponent activatable = mock(ActivatableComponent.class);

    when(textureComponent.getTexture()).thenReturn(normalTexture);
    when(activatable.isActive()).thenReturn(false);

    TriggerablePlatformComponent component =
        new TriggerablePlatformComponent();

    Entity entity =
        new Entity()
            .addComponent(collider)
            .addComponent(textureComponent)
            .addComponent(activatable)
            .addComponent(component);

    entity.create();

    entity.getEvents().trigger("activatedMapComponent", true);

    verify(collider).setLayer(PhysicsLayer.GROUND);
    verify(textureComponent).setTexture(normalTexture);
  }

  @Test
  void shouldDeactivatePlatformFromEvent() {
    ColliderComponent collider = mock(ColliderComponent.class);
    DynamicTextureRenderComponent textureComponent =
        mock(DynamicTextureRenderComponent.class);
    ActivatableComponent activatable = mock(ActivatableComponent.class);

    when(textureComponent.getTexture()).thenReturn(normalTexture);
    when(activatable.isActive()).thenReturn(true);

    TriggerablePlatformComponent component =
        new TriggerablePlatformComponent();

    Entity entity =
        new Entity()
            .addComponent(collider)
            .addComponent(textureComponent)
            .addComponent(activatable)
            .addComponent(component);

    entity.create();

    entity.getEvents().trigger("activatedMapComponent", false);

    verify(collider).setLayer(PhysicsLayer.NONE);
    verify(textureComponent).setTexture(transparentTexture);
  }

  @Test
  void shouldOnlyLoadTexturesOnce() {
    ColliderComponent collider = mock(ColliderComponent.class);
    DynamicTextureRenderComponent textureComponent =
        mock(DynamicTextureRenderComponent.class);
    ActivatableComponent activatable = mock(ActivatableComponent.class);

    when(textureComponent.getTexture()).thenReturn(normalTexture);
    when(activatable.isActive()).thenReturn(true);

    TriggerablePlatformComponent component =
        new TriggerablePlatformComponent();

    Entity entity =
        new Entity()
            .addComponent(collider)
            .addComponent(textureComponent)
            .addComponent(activatable)
            .addComponent(component);

    entity.create();

    entity.getEvents().trigger("activatedMapComponent", false);
    entity.getEvents().trigger("activatedMapComponent", true);

    verify(resourceService, times(1))
        .getAsset("images/ui/transparent.png", Texture.class);
    verify(textureComponent, times(1)).getTexture();
  }
}