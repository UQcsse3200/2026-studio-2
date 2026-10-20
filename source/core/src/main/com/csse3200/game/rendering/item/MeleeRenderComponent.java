package com.csse3200.game.rendering.item;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.item.weapons.melee.MeleeComponent;
import com.csse3200.game.rendering.RenderComponent;

/**
 * Draws the outer edge of a slash arc in front of the player while a melee swing is in progress.
 */
public class MeleeRenderComponent extends RenderComponent {
  /** Fraction of the swing spent sweeping the arc round. After that it just fades out. */
  private static final float SWEEP_FRACTION = 0.3f;

  private static final float MAX_ALPHA = 0.9f;
  private static final int ARC_SEGMENTS = 24;

  /** Thickness of the drawn edge, in world units. */
  private static final float EDGE_WIDTH = 0.08f;

  // Created on first draw so the component can be constructed without a graphics context
  private ShapeRenderer shapeRenderer;
  private MeleeComponent melee;

  @Override
  public void create() {
    super.create();
    melee = entity.getComponent(MeleeComponent.class);
  }

  @Override
  protected void draw(SpriteBatch batch) {
    if (melee == null || !melee.isSwinging()) {
      return;
    }
    float progress = melee.getSwingProgress();
    float sweep = Math.min(1f, progress / SWEEP_FRACTION) * MeleeComponent.ARC_DEGREES;
    float alpha = MAX_ALPHA * (1f - progress);
    if (sweep <= 0f || alpha <= 0f) {
      return;
    }

    // The arc sweeps from the top down to the bottom of the facing direction, mirrored when facing
    // left.
    float half = MeleeComponent.ARC_DEGREES / 2f;
    float start = melee.getFacing() >= 0 ? half - sweep : 180f - half;
    Vector2 centre = entity.getCenterPosition();

    if (shapeRenderer == null) {
      shapeRenderer = new ShapeRenderer();
    }

    // Pause standard sprite rendering to avoid pipeline conflict with primitive geometry
    batch.end();

    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
    shapeRenderer.begin(ShapeType.Filled);
    shapeRenderer.setColor(1f, 1f, 1f, alpha);
    // Only the curved outer edge is drawn, as a chain of short thick lines along the arc.
    float previousX = 0f;
    float previousY = 0f;
    for (int i = 0; i <= ARC_SEGMENTS; i++) {
      float angle = start + sweep * i / ARC_SEGMENTS;
      float x = centre.x + MeleeComponent.RANGE * MathUtils.cosDeg(angle);
      float y = centre.y + MeleeComponent.RANGE * MathUtils.sinDeg(angle);
      if (i > 0) {
        shapeRenderer.rectLine(previousX, previousY, x, y, EDGE_WIDTH);
      }
      previousX = x;
      previousY = y;
    }
    shapeRenderer.end();
    Gdx.gl.glDisable(GL20.GL_BLEND);

    batch.begin();
  }

  @Override
  public void dispose() {
    if (shapeRenderer != null) {
      shapeRenderer.dispose();
    }
    super.dispose();
  }
}
