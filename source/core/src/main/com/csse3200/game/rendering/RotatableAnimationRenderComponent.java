package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.level.RotatableMapComponent;

public class RotatableAnimationRenderComponent extends AnimationRenderComponent {
  private final TextureRegion renderRegion = new TextureRegion();
  private RotatableMapComponent rotateComponent;
  private float rotation;

  public RotatableAnimationRenderComponent(TextureAtlas atlas) {
    super(atlas);
  }

  @Override
  public void create() {
    super.create();
    rotateComponent = entity.getComponent(RotatableMapComponent.class);
  }

  @Override
  protected void draw(SpriteBatch batch) {
    if (currentAnimation == null) {
      return;
    }
    TextureRegion region = currentAnimation.getKeyFrame(animationPlayTime);
    Vector2 pos = entity.getPosition();
    Vector2 scale = entity.getScale();

    float width;
    float height;
    if (defaultRegionWidthPx > 0f) {
      float unitsPerPixel = scale.x / defaultRegionWidthPx;
      width = region.getRegionWidth() * unitsPerPixel;
      height = region.getRegionHeight() * unitsPerPixel;
    } else {
      width = scale.x;
      height = scale.y;
    }

    rotation = rotateComponent.getRotation();

    // recreate the adjustments in a local texture region so we can use the correct overload
    renderRegion.setRegion(region);
    if (renderRegion.isFlipX() != flipX) {
      renderRegion.flip(true, false);
    }

    batch.draw(
        renderRegion, pos.x, pos.y, width / 2f, height / 2f, width, height, 1f, 1f, rotation);
    animationPlayTime += timeSource.getDeltaTime();
  }
}
