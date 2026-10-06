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
  public float backgroundLight = 1f;
  private boolean weather = false;
  private float beforeFlash = 1f;
  private float beforeFlashBackground = 1f;

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

  public float getLightning() {
    return this.lightning;
  }

  public void toggleWeather() {
    weather = !weather;
  }

  public boolean getWeather() {
    return weather;
  }

  private void updateDarkness(float deltaTime) {
    // Prevent black items after flash
    if (darkness <= 0.25f) {
      darkness = 0.25f;
    }
    if (darkness > 0.25f) {
      darkness -= deltaTime / 100f;
    }
    lightning += deltaTime;
    lightning %= 20; // 40
    if (lightning > 5f && lightning < 5.5f) {
      darkness = 1f;
    } else if (lightning > 5.7f && lightning < 5.8f) {
      darkness = 1f;
    } else if (lightning > 13f && lightning < 13.3f) {
      darkness = 1f;
    } else {
      beforeFlash = darkness;
    }
  }

  private void updateBackgroundLight(float deltaTime) {
    // Prevent black screen after flash
    if (backgroundLight <= 0.125f) {
      backgroundLight = 0.125f;
    }
    // Keep decrementing light until full night reached
    if (backgroundLight > 0.125f) {
      backgroundLight -= deltaTime / 100f;
    }
    // if lightning currently striking
    if (darkness == 1f) {
      // only flash background if it is dark enough, limit how bright it may flash
      if (backgroundLight < 0.3f) {
        backgroundLight = 0.3f;
      }
    } else {
      beforeFlashBackground = backgroundLight;
    }
  }

  @Override
  public void update() {
    if (weather) {
      if (ServiceLocator.getTimeSource() != null) {
        float deltaTime = ServiceLocator.getTimeSource().getDeltaTime();
        darkness = beforeFlash;
        backgroundLight = beforeFlashBackground;
        updateDarkness(deltaTime);
        updateBackgroundLight(deltaTime);
        ServiceLocator.getLightingService().getEngine().setAmbientLight(darkness);
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
