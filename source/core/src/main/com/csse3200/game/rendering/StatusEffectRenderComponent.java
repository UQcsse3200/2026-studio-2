package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.BurnStatsComponent;
import com.csse3200.game.components.SlowStatsComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

public class StatusEffectRenderComponent extends RenderComponent {
  private static final int FRAME_COUNT = 6;
  private static final long FRAME_DURATION_MS = 100L;
  private static final float EFFECT_SCALE = 1.1f;

  private BurnStatsComponent burnStats;
  private SlowStatsComponent slowStats;
  private TextureRegion[] fireFrames;
  private TextureRegion[] iceFrames;

  @Override
  public void create() {
    super.create();

    burnStats = entity.getComponent(BurnStatsComponent.class);
    slowStats = entity.getComponent(SlowStatsComponent.class);

    Texture fireTexture =
        ServiceLocator.getResourceService()
            .getAsset("images/fire_status_effect.png", Texture.class);
    Texture iceTexture =
        ServiceLocator.getResourceService().getAsset("images/ice_status_effect.png", Texture.class);

    fireFrames =
        TextureRegion.split(
            fireTexture, fireTexture.getWidth() / FRAME_COUNT, fireTexture.getHeight())[0];
    iceFrames =
        TextureRegion.split(
            iceTexture, iceTexture.getWidth() / FRAME_COUNT, iceTexture.getHeight())[0];
  }

  @Override
  protected void draw(SpriteBatch batch) {
    GameTime time = ServiceLocator.getTimeSource();
    if (time == null) {
      return;
    }

    if (burnStats != null && burnStats.isBurning()) {
      drawEffect(batch, fireFrames, time.getTime());
    }

    if (slowStats != null && slowStats.isSlowed()) {
      drawEffect(batch, iceFrames, time.getTime());
    }
  }

  private void drawEffect(SpriteBatch batch, TextureRegion[] frames, long timeMs) {
    int frame = (int) ((timeMs / FRAME_DURATION_MS) % frames.length);
    Vector2 position = entity.getPosition();
    Vector2 scale = entity.getScale();

    float width = scale.x * EFFECT_SCALE;
    float height = scale.y * EFFECT_SCALE;
    float x = position.x + (scale.x - width) / 2f;
    float y = position.y - scale.y * 0.25f;

    batch.draw(frames[frame], x, y, width, height);
  }

  @Override
  public float getZIndex() {
    return super.getZIndex() + 0.01f;
  }
}
