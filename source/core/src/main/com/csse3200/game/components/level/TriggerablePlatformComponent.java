package com.csse3200.game.components.level;

import com.badlogic.gdx.graphics.Texture;
import com.csse3200.game.components.Component;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.rendering.DynamicTextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

public class TriggerablePlatformComponent extends Component {
  ColliderComponent colliderComponent;
  DynamicTextureRenderComponent textureComponent;
  ActivatableComponent activatableComponent;
  Texture texture;
  Texture transparentTexture;

  @Override
  public void create() {
    super.create();
    colliderComponent = entity.getComponent(ColliderComponent.class);
    textureComponent = entity.getComponent(DynamicTextureRenderComponent.class);
    activatableComponent = entity.getComponent(ActivatableComponent.class);

    handleActivation(activatableComponent.isActive());
    entity.getEvents().addListener("activatedMapComponent", this::handleActivation);
  }

  private void handleActivation(boolean activated) {
    if (texture == null) {
      texture = textureComponent.getTexture();
      transparentTexture =
          ServiceLocator.getResourceService().getAsset("images/ui/transparent.png", Texture.class);
    }

    Texture newTexture = activated ? texture : transparentTexture;
    colliderComponent.setLayer(activated ? PhysicsLayer.GROUND : PhysicsLayer.NONE);
    textureComponent.setTexture(newTexture);
  }
}
