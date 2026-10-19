package com.csse3200.game.rendering.item;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.rendering.RenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Marks where a grapple arrow landed but could not hook, with a red cross that fades out. It shows
 * for every kind of failed grapple alike - landing on the floor or anything else below the player,
 * or too close to them to hook - so the player can tell it was refused and not just missed.
 *
 * <p>Listens for the "grappleFailed" event, sent with the world point the arrow landed at.
 */
public class GrappleFailedIndicatorRenderComponent extends RenderComponent {
  /** How long the cross stays on screen, in seconds. */
  static final float DURATION = 0.8f;

  /** Half the width and height of the cross, in world units. */
  static final float HALF_SIZE = 0.25f;

  private static final float LINE_WIDTH = 0.07f;

  private ShapeRenderer shapeRenderer;
  private Vector2 point;
  private float remaining;

  @Override
  public void create() {
    super.create();
    entity.getEvents().addListener("grappleFailed", this::show);
  }

  private void show(Vector2 landedAt) {
    if (landedAt == null) {
      return;
    }
    point = landedAt.cpy();
    remaining = DURATION;
  }

  @Override
  public void update() {
    if (remaining > 0f) {
      remaining = Math.max(0f, remaining - ServiceLocator.getTimeSource().getDeltaTime());
    }
  }

  @Override
  protected void draw(SpriteBatch batch) {
    if (point == null || remaining <= 0f) {
      return;
    }
    float alpha = remaining / DURATION;

    if (shapeRenderer == null) {
      shapeRenderer = new ShapeRenderer();
    }

    // Pause sprite rendering so the primitive shapes don't fight the batch's pipeline.
    batch.end();

    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
    shapeRenderer.begin(ShapeType.Filled);
    shapeRenderer.setColor(0.9f, 0.1f, 0.1f, alpha);
    shapeRenderer.rectLine(
        point.x - HALF_SIZE,
        point.y - HALF_SIZE,
        point.x + HALF_SIZE,
        point.y + HALF_SIZE,
        LINE_WIDTH);
    shapeRenderer.rectLine(
        point.x - HALF_SIZE,
        point.y + HALF_SIZE,
        point.x + HALF_SIZE,
        point.y - HALF_SIZE,
        LINE_WIDTH);
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
