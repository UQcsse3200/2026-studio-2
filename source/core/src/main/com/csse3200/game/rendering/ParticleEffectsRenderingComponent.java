package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.BurnStatsComponent;
import com.csse3200.game.components.SlowStatsComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

/**
 * Renders a short-lived particle at the centre of an entity.
 *
 * <p>The effect is emitted once when the component is created.
 */
public class ParticleEffectsRenderingComponent extends RenderComponent {
  public enum ParticleShape {
    CIRCLE,
    SQUARE,
    TRIANGLE
  }

  private BurnStatsComponent burnStats;
  private SlowStatsComponent slowStats;
  private TextureRegion[] fireFrames;
  private TextureRegion[] iceFrames;

  private ParticleShape shape = ParticleShape.CIRCLE;
  private float startingSize = 5;
  private float endingSize = 1;
  private Color primaryColour = Color.WHITE;
  private Color secondaryColour = Color.WHITE;
  private Color tertiaryColour = Color.WHITE;
  private final float lifetime = 1;
  private final ShapeRenderer shapeRenderer = new ShapeRenderer();
  private float timeRemaining;

  /** Creates a particle effect. */
  public ParticleEffectsRenderingComponent() {}

  @Override
  public void create() {
    super.create();
    burnStats = entity.getComponent(BurnStatsComponent.class);
    slowStats = entity.getComponent(SlowStatsComponent.class);
  }

  @Override
  protected void draw(SpriteBatch batch) {
      GameTime timeSource = ServiceLocator.getTimeSource();
      if (timeSource == null) {
          return;
      }

      if (burnStats != null && burnStats.isBurning()) {
          this.primaryColour = Color.RED;
          this.secondaryColour = Color.ORANGE;
          this.tertiaryColour = Color.WHITE;
      }

      if (slowStats != null && slowStats.isSlowed()) {
          this.primaryColour = Color.CYAN;
          this.secondaryColour = Color.CYAN;
          this.tertiaryColour = Color.WHITE;
      }

      // Copy rather than mutate a shared static Colour instance.
      Color particleColour = new Color(primaryColour);

      float progress = 1f - timeRemaining / lifetime;
      float size = startingSize + (endingSize - startingSize) * progress;
      Vector2 centre = entity.getCenterPosition();
      particleColour.a *= 1f - progress;

      float halfSize = size / 2f;

      batch.end();
      shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
      shapeRenderer.begin(ShapeType.Filled);
      shapeRenderer.setColor(particleColour);

      switch (shape) {
          case CIRCLE:
              shapeRenderer.circle(centre.x, centre.y, halfSize);
              break;
          case SQUARE:
              shapeRenderer.rect(centre.x - halfSize, centre.y - halfSize, size, size);
              break;
          case TRIANGLE:
              shapeRenderer.triangle(
                  centre.x,
                  centre.y + halfSize,
                  centre.x - halfSize,
                  centre.y - halfSize,
                  centre.x + halfSize,
                  centre.y - halfSize);
              break;
          default:
              throw new IllegalStateException("Unsupported particle shape: " + shape);
      }

      shapeRenderer.end();
      batch.begin();
  }

  private void drawParticle(Vector2 centre, float size, long time, ParticleShape shape) {}

  @Override
  public float getZIndex() {
    return super.getZIndex() + 0.01f;
  }

  @Override
  public void dispose() {
    shapeRenderer.dispose();
    super.dispose();
  }
}
