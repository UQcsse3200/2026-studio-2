package com.csse3200.game.components.player;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

import java.util.ArrayList;
import java.util.List;

/** A UI component for displaying player stats, e.g. health. */
public class PlayerStatsDisplay extends UIComponent {
  private Table table;
  private final int maxHearts = 2;
  private final int HITS_PER_HEART = 2;
  private final List<Image> heartImages = new ArrayList<>();

  private Texture fullHeartTexture;
  private Texture brokenHeartTexture;

  @Override
  public void create() {
    super.create();
    
    fullHeartTexture = ServiceLocator.getResourceService().getAsset("images/purple_heart.png", Texture.class);
    brokenHeartTexture = ServiceLocator.getResourceService().getAsset("images/brown_brokenHeart.png", Texture.class);
    
    addActors();
    entity.getEvents().addListener("updateHealth", this::updatePlayerHealthUI);
  }

  private void addActors() {
    table = new Table();
    table.top().left();
    table.setFillParent(true);
    table.padTop(45f).padLeft(5f);

    float heartSideLength = 40f;

    for (int i = 0; i < maxHearts; i++) {
      Image heart = new Image(fullHeartTexture);
      heartImages.add(heart);
      table.add(heart).size(heartSideLength).pad(5f);
    }

    stage.addActor(table);

    CombatStatsComponent stats = entity.getComponent(CombatStatsComponent.class);
    if (stats != null) {
      updatePlayerHealthUI(stats.getHealth());
    }
  }

  @Override
  public void draw(SpriteBatch batch) {
    // draw is handled by the stage
  }

  /**
   * Updates the player's health on the UI.
   *
   * @param health player health (e.g. 100 max, where 25 health = 1 hit step)
   */
  public void updatePlayerHealthUI(int health) {
    // 100 total HP across 2 hearts (4 hits total) -> 25 HP per hit
    int hpPerHit = 2; 
    int remainingHits = health / hpPerHit;

    for (int i = 0; i < heartImages.size(); i++) {
      int hitsForThisHeart = remainingHits - (i * HITS_PER_HEART);
      int clampedHits = Math.max(0, Math.min(hitsForThisHeart, HITS_PER_HEART));
      Image heart = heartImages.get(i);

      if (clampedHits == 2) {
        heart.setVisible(true);
        heart.setDrawable(new TextureRegionDrawable(fullHeartTexture));
      } else if (clampedHits == 1) {
        heart.setVisible(true);
        heart.setDrawable(new TextureRegionDrawable(brokenHeartTexture));
      } else {
        heart.setVisible(false); // Disappears after 2 hits
      }
    }
  }

  @Override
  public void dispose() {
    super.dispose();
    if (table != null) {
      table.remove();
    }
    heartImages.clear();
  }
}
