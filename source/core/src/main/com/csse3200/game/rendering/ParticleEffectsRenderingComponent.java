package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.BurnStatsComponent;
import com.csse3200.game.components.PoisonStatsComponent;
import com.csse3200.game.components.SlowStatsComponent;
import com.csse3200.game.components.lighting.PointLightComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

/**
 * Procedurally renders a fire or ice particle effect above an entity, built out of small shapes
 * ("pixels") drawn with {@link ShapeRenderer} rather than a sprite sheet.
 */
public class ParticleEffectsRenderingComponent extends RenderComponent {

  private static final int PIXEL_COUNT = 5;
  private static final int SEGMENTS_PER_PIXEL = 6;

  private static final Color fireBaseColour = new Color(0.55f, 0.05f, 0.02f, 1f);
  private static final Color fireMidColour = new Color(0.95f, 0.35f, 0.05f, 1f);
  private static final Color fireTipColour = new Color(1f, 0.9f, 0.3f, 1f);

  private static final Color iceBaseColour = new Color(0.05f, 0.25f, 0.65f, 1f);
  private static final Color iceMidColour = new Color(0.25f, 0.6f, 0.95f, 1f);
  private static final Color iceTipColour = new Color(0.85f, 0.95f, 1f, 1f);

  private static final Color poisonBaseColour = new Color(0.20f, 0.05f, 0.25f, 1f);
  private static final Color poisonMidColour = new Color(0.45f, 0.15f, 0.55f, 1f);
  private static final Color poisonTipColour = new Color(0.55f, 0.85f, 0.15f, 1f);

  private BurnStatsComponent burnStats;
  private SlowStatsComponent slowStats;
  private PoisonStatsComponent poisonStats;
  private ShapeRenderer shapeRenderer;
  private final Color workingColour = new Color();
  private PointLightComponent pointLightComponent;

  @Override
  public void create() {
    super.create();
    burnStats = entity.getComponent(BurnStatsComponent.class);
    slowStats = entity.getComponent(SlowStatsComponent.class);
    poisonStats = entity.getComponent(PoisonStatsComponent.class);
    try {
      pointLightComponent = entity.getComponent(PointLightComponent.class);
    } catch (Exception e) {
      pointLightComponent = null;
    }
  }

  @Override
  protected void draw(SpriteBatch batch) {
    GameTime time = ServiceLocator.getTimeSource();
    if (time == null) {
      return;
    }

    boolean burning = burnStats != null && burnStats.isBurning();
    boolean slowed = slowStats != null && slowStats.isSlowed();
    boolean poisoned = poisonStats != null && poisonStats.isPoisoned();
    if (!burning && !slowed && !poisoned) {
      if (pointLightComponent != null) {
        pointLightComponent.setActive(false);
      }
      return;
    }
    if (pointLightComponent != null) {
      pointLightComponent.setActive(true);
    }

    float x = entity.getCenterPosition().x;
    float y = entity.getPosition().y;
    Vector2 scale = entity.getScale();
    float baseSize = Math.max(scale.x / 4, scale.y / 4);
    float seconds = time.getTime() / 1000f;

    if (shapeRenderer == null) {
      shapeRenderer = new ShapeRenderer();
    }

    batch.end();
    shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
    shapeRenderer.begin(ShapeType.Filled);

    if (burning) {
      drawEffect(x, y, baseSize, seconds, fireBaseColour, fireMidColour, fireTipColour);
    }
    if (slowed) {
      drawEffect(x, y, baseSize, seconds, iceBaseColour, iceMidColour, iceTipColour);
    }
    if (poisoned) {
      drawEffect(x, y, baseSize, seconds, poisonBaseColour, poisonMidColour, poisonTipColour);
    }

    shapeRenderer.end();
    batch.begin();
  }

  /**
   * Draws {@value #PIXEL_COUNT} tapering, swaying streaks rising from the entity's centre, each
   * built from small stacked quads colour-graded from base -> mid -> tip.
   */
  private void drawEffect(
      float x, float y, float baseSize, float seconds, Color base, Color mid, Color tip) {
    if (pointLightComponent != null) {
      pointLightComponent.setColor(base);
    }
    for (int i = 0; i < PIXEL_COUNT; i++) {
      float hash = MathUtils.random(11.0f + i * 17.3f);
      float sway = MathUtils.sin(seconds * (1.6f + hash) + i) * baseSize * 0.08f;
      float height = baseSize * (0.55f + 0.4f * MathUtils.random(11.0f + i));
      float flicker = 0.85f + 0.15f * MathUtils.sin(seconds * (4f + hash * 3f) + i * 2f);
      height *= flicker;

      float pixel = Math.max(0.15f, baseSize * 0.05f);

      for (int s = 0; s < SEGMENTS_PER_PIXEL; s++) {
        float t = s / (float) (SEGMENTS_PER_PIXEL - 1);
        float segY = y + height * t;
        float segSway = sway * t + MathUtils.sin(seconds * 3f + s + i) * pixel;
        float segX = x + segSway;
        float segWidth = pixel * (1.6f - 1.1f * t);

        colourGradient(base, mid, tip, t, workingColour);
        shapeRenderer.setColor(workingColour);
        shapeRenderer.rect(segX - segWidth / 2f, segY, segWidth, pixel * 1.3f);
      }
    }
  }

  private static void colourGradient(Color base, Color mid, Color tip, float t, Color out) {
    if (t < 0.5f) {
      out.set(base).lerp(mid, t / 0.5f);
    } else {
      out.set(mid).lerp(tip, (t - 0.5f) / 0.5f);
    }
  }

  @Override
  public float getZIndex() {
    return super.getZIndex() + 0.01f;
  }

  @Override
  public void dispose() {
    if (pointLightComponent != null) {
      pointLightComponent.dispose();
    }
    if (shapeRenderer != null) {
      shapeRenderer.dispose();
    }
    super.dispose();
  }
}
