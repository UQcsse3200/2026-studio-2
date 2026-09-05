package com.csse3200.game.components.player;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import java.util.ArrayList;
import java.util.List;

/** A UI component that displays the player's health as a row of hearts. */
public class PlayerStatsDisplay extends UIComponent {
  private static final String HEART_TEXTURE = "images/purple_heart.png";
  private static final float HEART_SIDE_LENGTH = 40f;
  private static final int HP_PER_HEART = 2;
  private static final int FLICKER_COUNT = 3;
  private static final float FLICKER_DURATION = 0.1f;

  private Table table;
  private final List<Image> heartImages = new ArrayList<>();
  private Texture heartTexture;
  private CombatStatsComponent combatStats;

  @Override
  public void create() {
    super.create();

    heartTexture = ServiceLocator.getResourceService().getAsset(HEART_TEXTURE, Texture.class);
    combatStats = entity.getComponent(CombatStatsComponent.class);

    table = new Table();
    table.top().left();
    table.setFillParent(true);
    table.padTop(45f).padLeft(5f);
    stage.addActor(table);

    entity.getEvents().addListener("updateHealth", this::updatePlayerHealthUI);

    if (combatStats != null) {
      updatePlayerHealthUI(combatStats.getHealth());
    }
  }

  @Override
  public void draw(SpriteBatch batch) {
    // draw is handled by the stage
  }

  /**
   * Updates the heart row to reflect the player's current health. Grows the row if the player's max
   * health has increased (e.g. an extra life earned), and flickers-then-hides any heart that has
   * just been lost.
   *
   * @param health the player's current health
   */
  public void updatePlayerHealthUI(int health) {
    if (combatStats == null) {
      return;
    }

    growHeartsTo(Math.max(1, roundUpToHearts(combatStats.getMaxHealth())));

    int cappedHealth = Math.max(0, Math.min(health, combatStats.getMaxHealth()));
    int heartsRemaining = roundUpToHearts(cappedHealth);

    for (int i = 0; i < heartImages.size(); i++) {
      Image heart = heartImages.get(i);
      boolean shouldBeVisible = i < heartsRemaining;

      if (heart.isVisible() && !shouldBeVisible) {
        flickerAndHide(heart);
      } else if (!heart.isVisible() && shouldBeVisible) {
        heart.clearActions();
        heart.setColor(1f, 1f, 1f, 1f);
        heart.setVisible(true);
      }
    }
  }

  /** Converts health points to hearts, keeping a partially filled heart visible. */
  private int roundUpToHearts(int health) {
    return (health + HP_PER_HEART - 1) / HP_PER_HEART;
  }

  /**
   * Ensures there are at least {@code desiredCount} hearts in the row, adding new ones (e.g. for an
   * earned extra life) as needed. Hearts are never removed once added.
   *
   * @param desiredCount the minimum number of hearts that should exist
   */
  private void growHeartsTo(int desiredCount) {
    while (heartImages.size() < desiredCount) {
      Image heart = new Image(heartTexture);
      heartImages.add(heart);
      table.add(heart).size(HEART_SIDE_LENGTH).pad(5f);
    }
  }

  /** Flickers the given heart a few times, then hides it and resets it for reuse later. */
  private void flickerAndHide(Image heart) {
    heart.clearActions();
    heart.addAction(
        Actions.sequence(
            Actions.repeat(
                FLICKER_COUNT,
                Actions.sequence(
                    Actions.fadeOut(FLICKER_DURATION), Actions.fadeIn(FLICKER_DURATION))),
            Actions.run(
                () -> {
                  heart.setVisible(false);
                  heart.setColor(1f, 1f, 1f, 1f);
                })));
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
