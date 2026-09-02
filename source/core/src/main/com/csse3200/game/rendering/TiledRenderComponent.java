package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.services.ServiceLocator;

public class TiledRenderComponent extends RenderComponent {
  private final TextureRegion textureRegion;
  private final float tileWorldSize;

  public TiledRenderComponent(String texturePath, float tileWorldSize) {
    Texture texture = ServiceLocator.getResourceService().getAsset(texturePath, Texture.class);

    texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

    textureRegion = new TextureRegion(texture);
    this.tileWorldSize = tileWorldSize;
  }

  @Override
  protected void draw(SpriteBatch batch) {
    Vector2 position = entity.getPosition();
    Vector2 scale = entity.getScale();

    float tilesX = scale.x / tileWorldSize;
    float tilesY = scale.y / tileWorldSize;

    textureRegion.setU2(tilesX);
    textureRegion.setV2(tilesY);

    batch.draw(textureRegion, position.x, position.y, scale.x, scale.y);
  }
}
