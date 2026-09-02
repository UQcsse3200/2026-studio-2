package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.projectile.ArrowProjectileComponent;

/** Renders a standard arrow as a small shaft aligned with its travel direction. */
public class ArrowRenderComponent extends RenderComponent {
  private static final float SHAFT_LENGTH = 0.5f;
  private static final float SHAFT_WIDTH = 0.05f;

  // ShapeRenderer creates an OpenGL shader, so defer construction until the arrow is actually
  // drawn. This keeps entity/physics tests compatible with the headless test environment.
  private ShapeRenderer shapeRenderer;
  private ArrowProjectileComponent projectile;

  @Override
  public void create() {
    super.create();
    projectile = entity.getComponent(ArrowProjectileComponent.class);
  }

  @Override
  protected void draw(SpriteBatch batch) {
    if (shapeRenderer == null) {
      shapeRenderer = new ShapeRenderer();
    }
    Vector2 center = entity.getCenterPosition();
    Vector2 halfShaft = projectile.getDirection().scl(SHAFT_LENGTH / 2f);
    Vector2 start = center.cpy().sub(halfShaft);
    Vector2 end = center.cpy().add(halfShaft);

    batch.end();
    shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
    shapeRenderer.begin(ShapeType.Filled);
    shapeRenderer.setColor(Color.BROWN);
    shapeRenderer.rectLine(start, end, SHAFT_WIDTH);
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
