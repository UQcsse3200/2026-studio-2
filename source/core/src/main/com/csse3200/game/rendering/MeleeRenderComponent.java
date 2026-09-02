package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.services.ServiceLocator;

/** Draws a brief slash where the player just swung. */
public class MeleeRenderComponent extends RenderComponent {
  private static final float SWING_DURATION = 0.12f;
  private static final float REACH = 1.2f;
  private static final float LINE_WIDTH = 0.08f;

  private ShapeRenderer shapeRenderer;
  private Vector2 swingDirection;
  private float timeRemaining = 0f;

  @Override
  public void create() {
    super.create();
    entity.getEvents().addListener("melee", this::onMelee);
  }

  private void onMelee(Vector2 direction) {
    swingDirection = direction.cpy().nor();
    timeRemaining = SWING_DURATION;
  }

  @Override
  public void update() {
    timeRemaining -= ServiceLocator.getTimeSource().getDeltaTime();
  }

  @Override
  protected void draw(SpriteBatch batch) {
    if (timeRemaining <= 0f) {
      return;
    }

    Vector2 start = entity.getCenterPosition();
    Vector2 end = start.cpy().mulAdd(swingDirection, REACH);

    batch.end();
    if (shapeRenderer == null) {
      shapeRenderer = new ShapeRenderer();
    }
    shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
    shapeRenderer.begin(ShapeType.Filled);
    shapeRenderer.setColor(Color.WHITE);
    shapeRenderer.rectLine(start.x, start.y, end.x, end.y, LINE_WIDTH);
    shapeRenderer.end();
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
