package com.csse3200.game.rendering.item;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.rendering.RenderComponent;
import com.csse3200.game.services.ServiceLocator;

/** Draws a brief arc slash where the player just swung. */
public class MeleeRenderComponent extends RenderComponent {
  private static final float SWING_DURATION = 0.12f;

  /** Keep in sync with RANGE in MeleeComponent so the visual matches the hit area. */
  private static final float REACH = 1.25f;

  /** Keep in sync with SWEEP_ARC_DEGREES in MeleeComponent. */
  private static final float ARC_DEGREES = 120f;

  private static final int ARC_SEGMENTS = 16;
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
    if (direction == null || direction.isZero()) {
      return;
    }
    swingDirection = direction.cpy().nor();
    timeRemaining = SWING_DURATION;
  }

  @Override
  public void update() {
    if (timeRemaining > 0f) {
      timeRemaining -= ServiceLocator.getTimeSource().getDeltaTime();
    }
  }

  @Override
  protected void draw(SpriteBatch batch) {
    if (timeRemaining <= 0f || swingDirection == null) {
      return;
    }

    // 0 at the start of the swing, 1 at the end
    float progress = 1f - timeRemaining / SWING_DURATION;
    float startAngle = -ARC_DEGREES / 2f;
    float sweptAngle = ARC_DEGREES * Math.min(progress, 1f);
    float alpha = Math.max(0f, 1f - progress);

    Vector2 centre = entity.getCenterPosition();
    int segments = Math.max(1, Math.round(ARC_SEGMENTS * Math.min(progress, 1f)));

    batch.end();
    if (shapeRenderer == null) {
      shapeRenderer = new ShapeRenderer();
    }
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
    shapeRenderer.begin(ShapeType.Filled);
    shapeRenderer.setColor(1f, 1f, 1f, alpha);

    // Trace the arc up to the current angle as a chain of short line segments
    Vector2 previous = pointOnArc(centre, startAngle);
    for (int i = 1; i <= segments; i++) {
      float angle = startAngle + sweptAngle * i / segments;
      Vector2 next = pointOnArc(centre, angle);
      shapeRenderer.rectLine(previous.x, previous.y, next.x, next.y, LINE_WIDTH);
      previous = next;
    }

    shapeRenderer.end();
    Gdx.gl.glDisable(GL20.GL_BLEND);
    batch.begin();
  }

  /** Point on the swing arc at the given angle offset from the swing direction. */
  private Vector2 pointOnArc(Vector2 centre, float angleDegrees) {
    return centre.cpy().mulAdd(swingDirection.cpy().rotateDeg(angleDegrees), REACH);
  }

  @Override
  public void dispose() {
    if (shapeRenderer != null) {
      shapeRenderer.dispose();
    }
    super.dispose();
  }
}