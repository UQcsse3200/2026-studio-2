package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

/**
 * A generic component for rendering an entity. Registers itself with the render service in order to
 * be rendered each frame. Child classes can implement different kinds of rendering behaviour.
 */
public abstract class RenderComponent extends Component implements Renderable, Disposable {
  private static final int DEFAULT_LAYER = 1;
  public float darkness = 1f;
  public float lightning = 0f;

  @Override
  public void create() {
    ServiceLocator.getRenderService().register(this);
  }

  @Override
  public void dispose() {
    ServiceLocator.getRenderService().unregister(this);
  }

  @Override
  public void render(SpriteBatch batch) {
    draw(batch);
  }

  @Override
  public int compareTo(Renderable o) {
    return Float.compare(getZIndex(), o.getZIndex());
  }

  @Override
  public int getLayer() {
    return DEFAULT_LAYER;
  }

  public float getDarkness() {
    return this.darkness;
  }

  @Override
  public void update() {
    if (ServiceLocator.getTimeSource() != null) {
      if (darkness <= 0.2f) {
        darkness = 0.2f;
      }
      if (darkness > 0.2f) {
        // darkness -= ServiceLocator.getTimeSource().getDeltaTime() / 100f;
        darkness = 1 - (ServiceLocator.getTimeSource().getTime() / 50000f); // 50,000
        darkness = 1;
      }
      lightning += ServiceLocator.getTimeSource().getDeltaTime();
      lightning %= 20; // 40
      if (lightning > 5f && lightning < 5.5f) {
        darkness = 1f;
      }
      if (lightning > 5.7f && lightning < 5.8f) {
        darkness = 1f;
      }
      if (lightning > 13f && lightning < 13.3f) {
        darkness = 1f;
      }
    }
  }

  @Override
  public float getZIndex() {
    // The smaller the Y value, the higher the Z index, so that closer entities are drawn in front
    return -entity.getPosition().y;
  }

  /**
   * Draw the renderable. Should be called only by the renderer, not manually.
   *
   * @param batch Batch to render to.
   */
  protected abstract void draw(SpriteBatch batch);
}
