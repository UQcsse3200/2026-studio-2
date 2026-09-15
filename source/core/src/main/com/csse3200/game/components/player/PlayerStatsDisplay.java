package com.csse3200.game.components.player;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** A UI component that displays the player's health as a row of hearts. */
public class PlayerStatsDisplay extends UIComponent {
  private static final String HEART_TEXTURE = "images/red_heart.png";
  private static final String HEALTH_BAR_BACKGROUND_TEXTURE = "images/PixelArt_HeartBack.png";
  private static final String DAMAGED_HEART_TEXTURE = "images/Damaged_heart.png";
  private static final String LAST_HEALTH_TEXTURE = "images/Last_Health.png";

  private static final float HEART_SIDE_LENGTH = 35f;
  private static final float HEART_SIDE_HEIGHT = 35f;
  private static final float HEART_SPACING = 43.4f;
  private static final int HP_PER_HEART = 2;
  private static final int FLICKER_COUNT = 3;
  private static final float FLICKER_DURATION = 0.1f;
  private static final String HEALTH_LABEL_NAME = "player-health-label";
  private static final String SPEED_LABEL_NAME = "player-speed-label";

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
  private PhysicsComponent physicsComponent;
  private Label healthLabel;
  private Label speedLabel;
  private Image background;
  private boolean showStatText;

  /** Configures the optional sandbox readouts before this component is created. */
  public void setShowStatText(boolean showStatText) {
    this.showStatText = showStatText;
  }

  @Override
  public void create() {
    super.create();

    root = new Table();
    root.top().left();
    root.setFillParent(true);
    root.padTop(showStatText ? 5f : 45f);
    root.padLeft(5f);

    if (showStatText) {
      healthLabel = new Label("", skin);
      healthLabel.setName(HEALTH_LABEL_NAME);
      root.add(healthLabel).left().row();
    }

    Stack stack = new Stack();
    Texture backgroundTexture =
        ServiceLocator.getResourceService().getAsset(HEALTH_BAR_BACKGROUND_TEXTURE, Texture.class);
    background = new Image(backgroundTexture);

    Table backgroundContainer = new Table();
    backgroundContainer.add(background).size(400f, 150f).center();
    stack.add(backgroundContainer);

    heartTexture = ServiceLocator.getResourceService().getAsset(HEART_TEXTURE, Texture.class);
    heartGroup = new Group();
    heartGroup.setSize(400f, 150f);
    stack.add(heartGroup);

    combatStats = entity.getComponent(CombatStatsComponent.class);
    physicsComponent = entity.getComponent(PhysicsComponent.class);
    entity.getEvents().addListener("updateHealth", this::updatePlayerHealthUI);

    if (combatStats != null) {
      updatePlayerHealthUI(combatStats.getHealth());
    }

    root.add(stack).size(400f, 150f).left().row();

    if (showStatText) {
      speedLabel = new Label("Speed: 0.00", skin);
      speedLabel.setName(SPEED_LABEL_NAME);
      root.add(speedLabel).left();
    }

    stage.addActor(root);
  }

  @Override
  public void draw(SpriteBatch batch) {
    if (speedLabel == null || physicsComponent == null || physicsComponent.getBody() == null) {
      return;
    }
    float horizontalSpeed = Math.abs(physicsComponent.getBody().getLinearVelocity().x);
    speedLabel.setText(String.format(Locale.ROOT, "Speed: %.2f", horizontalSpeed));
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

    int maximumHearts = Math.max(1, (combatStats.getMaxHealth() + HP_PER_HEART - 1) / HP_PER_HEART);
    growHeartsTo(maximumHearts);

    int cappedHealth = Math.max(0, Math.min(health, combatStats.getMaxHealth()));
    if (healthLabel != null) {
      healthLabel.setText("Health: " + cappedHealth + " / " + combatStats.getMaxHealth());
    }

    int heartsRemaining = (cappedHealth + HP_PER_HEART - 1) / HP_PER_HEART;
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

    updateHealthBarBackground(heartsRemaining);
  }

  private void growHeartsTo(int desiredCount) {
    while (heartImages.size() < desiredCount) {
      int index = heartImages.size();
      Image heart = new Image(heartTexture);
      heart.setSize(HEART_SIDE_LENGTH, HEART_SIDE_HEIGHT);
      heart.setPosition(NORMAL_HEART_X + index * HEART_SPACING, NORMAL_HEART_Y);
      heartImages.add(heart);
      heartGroup.addActor(heart);
    }
  }

  private void updateHealthBarBackground(int heartsRemaining) {
    Texture newBackground;
    float startX;
    float startY;

    if (heartsRemaining <= 1) {
      newBackground =
          ServiceLocator.getResourceService().getAsset(LAST_HEALTH_TEXTURE, Texture.class);
      startX = LAST_HEART_X;
      startY = LAST_HEART_Y;
    } else if (heartsRemaining <= 3) {
      newBackground =
          ServiceLocator.getResourceService().getAsset(DAMAGED_HEART_TEXTURE, Texture.class);
      startX = DAMAGED_HEART_X;
      startY = DAMAGED_HEART_Y;
    } else {
      newBackground =
          ServiceLocator.getResourceService()
              .getAsset(HEALTH_BAR_BACKGROUND_TEXTURE, Texture.class);
      startX = NORMAL_HEART_X;
      startY = NORMAL_HEART_Y;
    }

    background.setDrawable(new Image(newBackground).getDrawable());
    positionHearts(startX, startY);
  }

  private void positionHearts(float startX, float startY) {
    for (int i = 0; i < heartImages.size(); i++) {
      Image heart = heartImages.get(i);
      heart.setPosition(startX + i * HEART_SPACING, startY);
    }
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

  @Override
  public void dispose() {
    super.dispose();
    if (root != null) {
      root.remove();
    }
    heartImages.clear();
  }
}
