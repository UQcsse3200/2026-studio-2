package com.csse3200.game.components.player;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import java.util.ArrayList;
import java.util.List;

/** A UI component that displays the player's health as a row of hearts. */
public class PlayerStatsDisplay extends UIComponent {

  private static final String HEART_TEXTURE = "images/red_heart.png";
  private static final String HEALTH_BAR_BACKGROUND_TEXTURE = "images/PixelArt_HeartBack.png";
  private static final String DAMAGED_HEART_TEXTURE = "images/Damaged_heart.png";
  private static final String LAST_HEALTH_TEXTURE = "images/Last_Health.png";

  private static final float HEART_SIDE_LENGTH = 35f;
  private static final float HEART_SIDE_HEIGHT = 35f;

  // Distance between the start of one heart and the next.
  private static final float HEART_SPACING = 43.4f;

  private static final int HP_PER_HEART = 2;

  private static final int FLICKER_COUNT = 3;
  private static final float FLICKER_DURATION = 0.1f;

  /*
   * Heart positions for each background.
   *
   * Adjust these values until the hearts line up exactly
   * with the slots in each background image.
   */
  private static final float NORMAL_HEART_X = 124f;
  private static final float NORMAL_HEART_Y = 55f;

  private static final float DAMAGED_HEART_X = 123f;
  private static final float DAMAGED_HEART_Y = 56f;

  private static final float LAST_HEART_X = 124f;
  private static final float LAST_HEART_Y = 58f;

  private Table root;

  private Group heartGroup;

  private final List<Image> heartImages = new ArrayList<>();

  private Texture heartTexture;

  private CombatStatsComponent combatStats;

  private Image background;

  @Override
  public void create() {
    super.create();

    /*
     * Root UI table.
     */
    root = new Table();
    root.top().left();
    root.setFillParent(true);
    root.padTop(45f);
    root.padLeft(5f);

    /*
     * Main stack.
     *
     * Background is drawn first.
     * Hearts are drawn on top of it.
     */
    Stack stack = new Stack();

    /*
     * Background.
     */
    Texture backgroundTexture =
        ServiceLocator.getResourceService().getAsset(HEALTH_BAR_BACKGROUND_TEXTURE, Texture.class);

    background = new Image(backgroundTexture);

    Table backgroundContainer = new Table();

    backgroundContainer.add(background).size(400f, 150f).center();

    stack.add(backgroundContainer);

    /*
     * Heart texture.
     */
    heartTexture = ServiceLocator.getResourceService().getAsset(HEART_TEXTURE, Texture.class);

    /*
     * Group allows precise pixel positioning of hearts.
     */
    heartGroup = new Group();

    /*
     * Give the group the same size as the health bar.
     */
    heartGroup.setSize(400f, 150f);

    stack.add(heartGroup);

    /*
     * Get player combat stats.
     */
    combatStats = entity.getComponent(CombatStatsComponent.class);

    /*
     * Listen for health changes.
     */
    entity.getEvents().addListener("updateHealth", this::updatePlayerHealthUI);

    /*
     * Initial health display.
     */
    if (combatStats != null) {
      updatePlayerHealthUI(combatStats.getHealth());
    }

    /*
     * Add everything to stage.
     */
    root.add(stack).size(400f, 150f);

    stage.addActor(root);
  }

  @Override
  public void draw(SpriteBatch batch) {
    // Drawing is handled by the Scene2D stage.
  }

  /**
   * Updates the health UI whenever the player's health changes.
   *
   * @param health current player health
   */
  public void updatePlayerHealthUI(int health) {
    if (combatStats == null) {
      return;
    }

    /*
     * Maximum number of hearts.
     *
     * Ceiling division is used so odd max-health values
     * can still display correctly.
     */
    int maximumHearts = Math.max(1, (combatStats.getMaxHealth() + HP_PER_HEART - 1) / HP_PER_HEART);

    growHeartsTo(maximumHearts);

    /*
     * Clamp health between 0 and max health.
     */
    int cappedHealth = Math.max(0, Math.min(health, combatStats.getMaxHealth()));

    /*
     * Calculate visible hearts.
     *
     * Ceiling division means:
     *
     * 10 HP -> 5 hearts
     *  9 HP -> 5 hearts
     *  8 HP -> 4 hearts
     *  7 HP -> 4 hearts
     *  2 HP -> 1 heart
     *  1 HP -> 1 heart
     *  0 HP -> 0 hearts
     */
    int heartsRemaining = (cappedHealth + HP_PER_HEART - 1) / HP_PER_HEART;

    /*
     * Update heart visibility.
     */
    for (int i = 0; i < heartImages.size(); i++) {

      Image heart = heartImages.get(i);

      boolean shouldBeVisible = i < heartsRemaining;

      /*
       * Heart was visible but has now been lost.
       */
      if (heart.isVisible() && !shouldBeVisible) {

        flickerAndHide(heart);
      }

      /*
       * Heart needs to become visible again.
       */
      else if (!heart.isVisible() && shouldBeVisible) {

        heart.clearActions();

        heart.setColor(1f, 1f, 1f, 1f);

        heart.setVisible(true);
      }
    }

    /*
     * Change background and reposition hearts.
     */
    updateHealthBarBackground(heartsRemaining);
  }

  /**
   * Creates additional heart images when required.
   *
   * <p>Hearts are never deleted. They are simply hidden when health decreases.
   *
   * @param desiredCount number of heart actors required
   */
  private void growHeartsTo(int desiredCount) {

    while (heartImages.size() < desiredCount) {

      int index = heartImages.size();

      Image heart = new Image(heartTexture);

      heart.setSize(HEART_SIDE_LENGTH, HEART_SIDE_HEIGHT);

      /*
       * Initial position.
       *
       * This will also be updated whenever
       * the health-bar background changes.
       */
      heart.setPosition(NORMAL_HEART_X + index * HEART_SPACING, NORMAL_HEART_Y);

      heartImages.add(heart);

      heartGroup.addActor(heart);
    }
  }

  /**
   * Changes the health-bar background depending on the player's remaining hearts.
   *
   * <p>Also moves the hearts so they align with the artwork of the selected background.
   *
   * @param heartsRemaining visible hearts
   */
  private void updateHealthBarBackground(int heartsRemaining) {

    Texture newBackground;

    float startX;
    float startY;

    /*
     * Last-health background.
     */
    if (heartsRemaining <= 1) {

      newBackground =
          ServiceLocator.getResourceService().getAsset(LAST_HEALTH_TEXTURE, Texture.class);

      startX = LAST_HEART_X;
      startY = LAST_HEART_Y;
    }

    /*
     * Damaged background.
     */
    else if (heartsRemaining <= 3) {

      newBackground =
          ServiceLocator.getResourceService().getAsset(DAMAGED_HEART_TEXTURE, Texture.class);

      startX = DAMAGED_HEART_X;
      startY = DAMAGED_HEART_Y;
    }

    /*
     * Normal background.
     */
    else {

      newBackground =
          ServiceLocator.getResourceService()
              .getAsset(HEALTH_BAR_BACKGROUND_TEXTURE, Texture.class);

      startX = NORMAL_HEART_X;
      startY = NORMAL_HEART_Y;
    }

    /*
     * Update background.
     */
    background.setDrawable(new Image(newBackground).getDrawable());

    /*
     * Move hearts so they align with
     * the selected background.
     */
    positionHearts(startX, startY);
  }

  /**
   * Positions every heart manually.
   *
   * <p>This provides much more control than using padding inside a Table.
   *
   * @param startX x position of the first heart
   * @param startY y position of the hearts
   */
  private void positionHearts(float startX, float startY) {

    for (int i = 0; i < heartImages.size(); i++) {

      Image heart = heartImages.get(i);

      heart.setPosition(startX + i * HEART_SPACING, startY);
    }
  }

  /**
   * Flickers a lost heart before hiding it.
   *
   * @param heart heart being lost
   */
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

                  /*
                   * Reset alpha so the heart
                   * appears normally if restored.
                   */
                  heart.setColor(1f, 1f, 1f, 1f);
                })));
  }

  @Override
  public void dispose() {
    super.dispose();

    if (root != null) {
      root.remove();
    }

    heartImages.clear();
  }
}
