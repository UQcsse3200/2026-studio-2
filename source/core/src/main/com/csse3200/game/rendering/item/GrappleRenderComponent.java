package com.csse3200.game.rendering.item;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.item.weapons.bow.grapple.GrappleComponent;
import com.csse3200.game.rendering.RenderComponent;
import java.util.List;

/** Draws the grapple rope between the player and its anchor point. */
public class GrappleRenderComponent extends RenderComponent {

  /** Thickness of the rendered rope line in world units */
  private static final float LINE_WIDTH = 0.05f;

  // Created on first draw so the component can be constructed without a graphics context
  private ShapeRenderer shapeRenderer;
  private GrappleComponent grapple;

  @Override
  public void create() {
    super.create();
    grapple = entity.getComponent(GrappleComponent.class);
  }

  /**
   * Draws the grapple rope line if the grapple is currently attached to a target surface.
   *
   * @param batch Active SpriteBatch used by the main render pipeline
   */
  @Override
  protected void draw(SpriteBatch batch) {
    if (grapple == null || !grapple.isAttached()) {
      return;
    }
    List<Vector2> ropePath = grapple.getRopePath();
    if (ropePath.size() < 2) {
      return;
    }

    if (shapeRenderer == null) {
      shapeRenderer = new ShapeRenderer();
    }

    // Pause standard sprite rendering to avoid pipeline conflict with primitive geometry
    batch.end();

    shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
    shapeRenderer.begin(ShapeType.Filled);
    shapeRenderer.setColor(Color.BROWN);
    for (int i = 1; i < ropePath.size(); i++) {
      Vector2 from = ropePath.get(i - 1);
      Vector2 to = ropePath.get(i);
      shapeRenderer.rectLine(from.x, from.y, to.x, to.y, LINE_WIDTH);
    }
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
