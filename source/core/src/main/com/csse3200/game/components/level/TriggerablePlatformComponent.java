package com.csse3200.game.components.level;

import com.csse3200.game.components.Component;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.rendering.DynamicTextureRenderComponent;

public class TriggerablePlatformComponent extends Component {
  ColliderComponent colliderComponent;
  DynamicTextureRenderComponent textureComponent;

  @Override
  public void create() {
    super.create();
    colliderComponent = entity.getComponent(ColliderComponent.class);
    textureComponent = entity.getComponent(DynamicTextureRenderComponent.class);

    entity.getEvents().addListener("activatedMapComponent", this::handleActivation);
  }

  private void handleActivation(boolean activated) {
    String imageFilepath = activated ? "images/platform.png" : "images/Tile_2.png"; // PH
    colliderComponent.setLayer(activated ? PhysicsLayer.GROUND : PhysicsLayer.NONE);
    textureComponent.setTexture(imageFilepath);
  }
}
