package com.csse3200.game.components.minigames.cyclopsMinigame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.player.PlayerStatsDisplay;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import java.util.ArrayList;
import java.util.List;

/**
 * The cyclops minigame's heart bar. It uses the same art and layout as the player's usual heart bar
 * (PlayerStatsDisplay), but shows odd health as a half heart, so each 1.5-heart miss is visible.
 * The usual bar is hidden while this one is on screen.
 */
public class CyclopsHeartsDisplay extends UIComponent {
  static final String HEART_TEXTURE = "images/health/red_heart.png";
  static final String FULL_BAR_TEXTURE = "images/health/PixelArt_HeartBack.png";
  static final String DAMAGED_BAR_TEXTURE = "images/health/Damaged_heart.png";
  static final String LAST_HEART_BAR_TEXTURE = "images/health/Last_Health.png";

  static final int HP_PER_HEART = 2;
  static final float HEART_SIZE = 35f;
  private static final float HEART_SPACING = 43.4f;
  private static final float BAR_WIDTH = 400f;
  private static final float BAR_HEIGHT = 150f;
  private static final int FLICKER_COUNT = 3;
  private static final float FLICKER_DURATION = 0.1f;

  // Where the first heart sits on each bar background, matching PlayerStatsDisplay.
  private static final float FULL_BAR_HEART_X = 124f;
  private static final float FULL_BAR_HEART_Y = 55f;
  private static final float DAMAGED_BAR_HEART_X = 123f;
  private static final float DAMAGED_BAR_HEART_Y = 56f;
  private static final float LAST_HEART_BAR_HEART_X = 124f;
  private static final float LAST_HEART_BAR_HEART_Y = 58f;

  private final List<Image> hearts = new ArrayList<>();
  private CombatStatsComponent combatStats;
  private Table root;
  private Image background;
  private Group heartGroup;
  private Drawable fullHeart;
  private Drawable halfHeart;
  private boolean usualBarHidden = false;

  @Override
  public void create() {
    super.create();
    combatStats = entity.getComponent(CombatStatsComponent.class);

    Texture heartTexture =
        ServiceLocator.getResourceService().getAsset(HEART_TEXTURE, Texture.class);
    fullHeart = new TextureRegionDrawable(new TextureRegion(heartTexture));
    halfHeart =
        new TextureRegionDrawable(
            new TextureRegion(
                heartTexture, 0, 0, heartTexture.getWidth() / 2, heartTexture.getHeight()));

    root = new Table();
    root.top().left();
    root.setFillParent(true);
    root.padTop(45f);
    root.padLeft(5f);

    Stack stack = new Stack();
    background = new Image(barTexture(FULL_BAR_TEXTURE));
    Table backgroundContainer = new Table();
    backgroundContainer.add(background).size(BAR_WIDTH, BAR_HEIGHT).center();
    stack.add(backgroundContainer);
    heartGroup = new Group();
    heartGroup.setSize(BAR_WIDTH, BAR_HEIGHT);
    stack.add(heartGroup);
    root.add(stack).size(BAR_WIDTH, BAR_HEIGHT).left();

    entity.getEvents().addListener("updateHealth", this::showHealth);
    if (combatStats != null) {
      showHealth(combatStats.getHealth());
    }
    stage.addActor(root);
  }

  @Override
  public void draw(SpriteBatch batch) {
    // The usual bar may be created after this one, so hide it once everything is built.
    if (!usualBarHidden) {
      PlayerStatsDisplay usualBar = entity.getComponent(PlayerStatsDisplay.class);
      if (usualBar != null) {
        usualBar.hide();
      }
      usualBarHidden = true;
    }
  }

  /**
   * Shows the given health as whole hearts plus a half heart when it's odd.
   *
   * @param health the player's current health
   */
  void showHealth(int health) {
    if (combatStats == null) {
      return;
    }
    int maxHealth = combatStats.getMaxHealth();
    int slots = Math.max(1, (maxHealth + HP_PER_HEART - 1) / HP_PER_HEART);
    while (hearts.size() < slots) {
      Image heart = new Image(fullHeart);
      heart.setSize(HEART_SIZE, HEART_SIZE);
      hearts.add(heart);
      heartGroup.addActor(heart);
    }

    int capped = Math.clamp(health, 0, maxHealth);
    int fullHearts = capped / HP_PER_HEART;
    boolean hasHalf = capped % HP_PER_HEART != 0;
    for (int i = 0; i < hearts.size(); i++) {
      Image heart = hearts.get(i);
      if (i < fullHearts) {
        showHeart(heart, fullHeart, HEART_SIZE);
      } else if (i == fullHearts && hasHalf) {
        showHeart(heart, halfHeart, HEART_SIZE / 2f);
      } else if (heart.isVisible()) {
        flickerAndHide(heart);
      }
    }
    showBarFor(fullHearts + (hasHalf ? 1 : 0));
  }

  private void showHeart(Image heart, Drawable drawable, float width) {
    heart.clearActions();
    heart.setDrawable(drawable);
    heart.setSize(width, HEART_SIZE);
    heart.setColor(1f, 1f, 1f, 1f);
    heart.setVisible(true);
  }

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

  /** Swaps the bar background as hearts run out, like the usual bar does. */
  private void showBarFor(int heartsShown) {
    String texture;
    float x;
    float y;
    if (heartsShown <= 1) {
      texture = LAST_HEART_BAR_TEXTURE;
      x = LAST_HEART_BAR_HEART_X;
      y = LAST_HEART_BAR_HEART_Y;
    } else if (heartsShown <= 3) {
      texture = DAMAGED_BAR_TEXTURE;
      x = DAMAGED_BAR_HEART_X;
      y = DAMAGED_BAR_HEART_Y;
    } else {
      texture = FULL_BAR_TEXTURE;
      x = FULL_BAR_HEART_X;
      y = FULL_BAR_HEART_Y;
    }
    background.setDrawable(new TextureRegionDrawable(new TextureRegion(barTexture(texture))));
    for (int i = 0; i < hearts.size(); i++) {
      hearts.get(i).setPosition(x + i * HEART_SPACING, y);
    }
  }

  private static Texture barTexture(String path) {
    return ServiceLocator.getResourceService().getAsset(path, Texture.class);
  }

  List<Image> getHearts() {
    return hearts;
  }

  @Override
  public void dispose() {
    super.dispose();
    if (root != null) {
      root.remove();
    }
    hearts.clear();
  }
}
