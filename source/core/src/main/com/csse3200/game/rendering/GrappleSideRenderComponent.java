package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.level.PlatformGrappleComponent;

public class GrappleSideRenderComponent extends RenderComponent {

  private static final float LINE_WIDTH = 0.12f;

  private final ShapeRenderer shapeRenderer = new ShapeRenderer();
  private PlatformGrappleComponent grappleComponent;

  @Override
  public void create() {
    super.create();
    grappleComponent = entity.getComponent(PlatformGrappleComponent.class);
  }

  @Override
  protected void draw(SpriteBatch batch) {

    if (grappleComponent == null) {
      return;
    }

    Vector2 pos = entity.getPosition();
    Vector2 scale = entity.getScale();

    float left = pos.x;
    float right = pos.x + scale.x;
    float bottom = pos.y;
    float top = pos.y + scale.y;

    // todo: change this shaperenderer into a tiled png of a rope or somethng
    batch.end();
    float light = getDarkness();
    shapeRenderer.setColor(light, light, light, 1f);
    shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
    shapeRenderer.begin(ShapeType.Filled);
    // shapeRenderer.setColor(Color.GOLD);

    drawSide(PlatformGrappleComponent.LEFT_SIDE, new Vector2(left, bottom), new Vector2(left, top));

    drawSide(PlatformGrappleComponent.TOP_SIDE, new Vector2(left, top), new Vector2(right, top));

    drawSide(
        PlatformGrappleComponent.RIGHT_SIDE, new Vector2(right, bottom), new Vector2(right, top));

    drawSide(
        PlatformGrappleComponent.BOTTOM_SIDE,
        new Vector2(left, bottom),
        new Vector2(right, bottom));

    shapeRenderer.setColor(Color.GOLD);
    shapeRenderer.end();
    batch.begin();
  }

  private void drawSide(int side, Vector2 start, Vector2 end) {

    if (grappleComponent.isSideGrappleable(side)) {
      shapeRenderer.rectLine(start, end, LINE_WIDTH);
    }
  }

  @Override
  public void dispose() {
    shapeRenderer.dispose();
    super.dispose();
  }
}
