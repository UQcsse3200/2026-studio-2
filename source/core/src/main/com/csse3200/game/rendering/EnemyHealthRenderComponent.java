package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;

/**
 * Renders a health bar above a given enemy
 *
 * <p>Health bar contains a pixel for each HP. Sits slightly above enemy and is scaled up.
 */
public class EnemyHealthRenderComponent extends RenderComponent {
  private int hp;
  private int spawnHp;

  private static Texture barTexture;

  // Customisable bar UI values
  private static final float BAR_HEIGHT = 0.05f;
  private static final float BAR_OFFSET = 0.1f; // height between top of enemy and bottom of bar
  private static final float BORDER_WIDTH = 0.02f;
  private static final float BAR_WIDTH_PERCENT =
      0.75f; // horizontal length of bar as percent of enemy sprite width

  @Override
  public void create() {
    super.create();

    CombatStatsComponent stats = entity.getComponent(CombatStatsComponent.class);
    spawnHp = stats.getMaxHealth();
    hp = spawnHp;

    // Listen for health updates
    entity.getEvents().addListener("updateHealth", this::onHealthUpdate);

    // Initial health bar
    if (barTexture == null) {
      Pixmap pmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
      pmap.setColor(Color.WHITE);
      pmap.fill();
      barTexture = new Texture(pmap);
      pmap.dispose();
    }
  }

  /* update internal hp state based on health update listening */
  private void onHealthUpdate(int newHp) {
    hp = newHp;
  }

  @Override
  protected void draw(SpriteBatch batch) {
    Vector2 entityPos = entity.getPosition();
    Vector2 entityScale = entity.getScale();

    // bar position & scale
    float barW = entityScale.x * BAR_WIDTH_PERCENT;
    float barX = entityPos.x + (entityScale.x - barW) / 2f;
    float barY = entityPos.y + entityScale.y + BAR_OFFSET;

    // draw border
    batch.setColor(Color.BLACK);
    batch.draw(
        barTexture,
        barX - BORDER_WIDTH,
        barY - BORDER_WIDTH,
        barW + 2 * BORDER_WIDTH,
        BAR_HEIGHT + 2 * BORDER_WIDTH);
    // draw bar background
    batch.setColor(Color.DARK_GRAY);
    batch.draw(barTexture, barX, barY, barW, BAR_HEIGHT);
    // draw remaining hp
    batch.setColor(Color.RED);
    batch.draw(barTexture, barX, barY, barW * hp / spawnHp, BAR_HEIGHT);

    // just in case
    batch.setColor(Color.WHITE);
  }

  @Override
  public void dispose() {
    super.dispose();
  }
}
