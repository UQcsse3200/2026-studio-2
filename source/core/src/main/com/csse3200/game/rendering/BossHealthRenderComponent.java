package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.CombatStatsComponent;

/** Renders a larger health bar above a boss. */
public class BossHealthRenderComponent extends RenderComponent {
  private int hp;
  private int spawnHp;

  private Texture barTexture;
  private final CameraComponent camera;

  private static final float BORDER_WIDTH = 0.03f;
  private static final float healthBarWidth = 12f;
  private static final float healthBarHeight = 0.25f;

  public BossHealthRenderComponent(CameraComponent camera) {
    this.camera = camera;
  }

  @Override
  public void create() {
    super.create();

    CombatStatsComponent stats = entity.getComponent(CombatStatsComponent.class);

    spawnHp = stats.getMaxHealth();
    hp = stats.getHealth();

    entity.getEvents().addListener("updateHealth", this::onHealthUpdate);

    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(Color.WHITE);
    pixmap.fill();

    barTexture = new Texture(pixmap);
    pixmap.dispose();
  }

  private void onHealthUpdate(int newHp) {
    hp = newHp;
  }

  @Override
  protected void draw(SpriteBatch batch) {
    OrthographicCamera worldCamera = (OrthographicCamera) camera.getCamera();

    float barX = worldCamera.position.x - healthBarWidth / 2f;
    float barY = worldCamera.position.y + 5.5f;

    Color previousColor = batch.getColor().cpy();

    batch.setColor(Color.BLACK);
    batch.draw(
        barTexture,
        barX - BORDER_WIDTH,
        barY - BORDER_WIDTH,
        healthBarWidth + 2 * BORDER_WIDTH,
        healthBarHeight + 2 * BORDER_WIDTH);

    batch.setColor(Color.DARK_GRAY);
    batch.draw(barTexture, barX, barY, healthBarWidth, healthBarHeight);

    float healthPercent = (float) hp / spawnHp;
    batch.setColor(Color.RED);
    batch.draw(barTexture, barX, barY, healthBarWidth * healthPercent, healthBarHeight);

    batch.setColor(previousColor);
  }

  @Override
  public void dispose() {
    if (barTexture != null) {
      barTexture.dispose();
    }

    super.dispose();
  }
}
