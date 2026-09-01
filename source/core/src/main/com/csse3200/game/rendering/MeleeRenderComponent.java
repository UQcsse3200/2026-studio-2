package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.services.ServiceLocator;

/** Draws a brief arc where the player just swung. */
public class MeleeRenderComponent extends RenderComponent {
  private static final float SWING_DURATION = 0.15f;
  private static final float ARC_RADIUS = 1.2f;
  private static final float ARC_DEGREES = 70f;
  private static final int SEGMENTS = 12;
  private static final float LINE_WIDTH = 0.06f;

  private final ShapeRenderer shapeRenderer = new ShapeRenderer();
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

    Vector2 centre = entity.getCenterPosition();
    float baseAngle = swingDirection.angleDeg();
    float step = ARC_DEGREES / SEGMENTS;
    float startAngle = baseAngle - ARC_DEGREES / 2f;

    // Fade the arc out over its short lifetime
    float alpha = timeRemaining / SWING_DURATION;

    batch.end();

    shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
    shapeRenderer.begin(ShapeType.Filled);
    shapeRenderer.setColor(1f, 1f, 1f, alpha);

    Vector2 previous = pointOnArc(centre, startAngle);
    for (int i = 1; i <= SEGMENTS; i++) {
      Vector2 next = pointOnArc(centre, startAngle + step * i);
      shapeRenderer.rectLine(previous.x, previous.y, next.x, next.y, LINE_WIDTH);
      previous = next;
    }

    shapeRenderer.end();

    batch.begin();
  }

  private Vector2 pointOnArc(Vector2 centre, float angleDeg) {
    return new Vector2(ARC_RADIUS, 0f).rotateDeg(angleDeg).add(centre);
  }

  @Override
  public void dispose() {
    shapeRenderer.dispose();
    super.dispose();
  }
}
