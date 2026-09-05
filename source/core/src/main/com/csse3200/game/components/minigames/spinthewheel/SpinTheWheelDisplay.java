package com.csse3200.game.components.minigames.spinthewheel;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A ui component for displaying the spin the wheel minigame. */
public class SpinTheWheelDisplay extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(SpinTheWheelDisplay.class);
  private static final float Z_INDEX = 2f;

  private static final String DISC_TEXTURE = "images/minigames/spinthewheel/wheel-disc.png";
  private static final String SPOKE_TEXTURE = "images/minigames/spinthewheel/wheel-spoke.png";
  private static final String POINTER_TEXTURE = "images/minigames/spinthewheel/wheel-pointer.png";
  private static final String BUTTON_UP_TEXTURE = "images/minigames/spinthewheel/button-up.png";
  private static final String BUTTON_OVER_TEXTURE = "images/minigames/spinthewheel/button-over.png";
  private static final String BUTTON_DOWN_TEXTURE = "images/minigames/spinthewheel/button-down.png";
  private static final String GLOW_TEXTURE = "images/minigames/spinthewheel/glow-radial.png";
  private static final String RAYS_TEXTURE = "images/minigames/spinthewheel/glow-rays.png";

  private static final float WHEEL_SIZE = 400f;
  private static final float SPOKE_THICKNESS = 6f;
  private static final float POINTER_SIZE = 32f;
  private static final float POINTER_ANGLE = 90f;
  private static final float SPIN_DURATION = 3.5f;
  private static final int FULL_TURNS = 4;
  private static final float SEGMENT_RADIUS_RATIO = 0.62f;
  private static final float ICON_SIZE = 48f;

  private static final int BUTTON_PATCH = 12;

  private static final float BUTTON_WIDTH = 220f;
  private static final float BUTTON_HEIGHT = 56f;
  private static final Color BUTTON_TEXT_COLOUR = new Color(0.16f, 0.18f, 0.18f, 1f);
  private static final Color BUTTON_PRESSED_TEXT_COLOUR = new Color(0.28f, 0.22f, 0.14f, 1f);

  private static final float CARD_SIZE = 340f;
  private static final float PRIZE_ICON_SIZE = 112f;
  private static final float RAY_SPIN_DURATION = 16f;
  private static final float CARD_GROW_DURATION = 0.4f;
  private static final float CARD_FADE_DURATION = 0.3f;
  private static final float CARD_START_SCALE = 0.6f;
  private static final float WHEEL_FADE_DURATION = 0.25f;
  private static final Color GLOW_COLOUR = new Color(1f, 0.82f, 0.42f, 0.9f);
  private static final Color RAYS_COLOUR = new Color(1f, 0.88f, 0.55f, 0.55f);
  private static final Color PRIZE_NAME_COLOUR = new Color(0.99f, 0.9f, 0.79f, 1f);
  private static final Color PRIZE_AMOUNT_COLOUR = new Color(1f, 0.8f, 0.35f, 1f);

  private final WheelLogic wheel;
  private Table table;
  private Group wheelGroup;
  private Table prizeLayer;
  private Group prizeCard;
  private Table prizeContent;
  private Image prizeIcon;
  private Label prizeName;
  private Label prizeAmount;

  public SpinTheWheelDisplay(List<WheelItem> items) {
    this.wheel = new WheelLogic(items);
  }

  /**
   * The textures the wheel needs loaded.
   *
   * @param items the items that will be shown on the wheel
   * @return the wheel's textures and one sprite per item
   */
  public static String[] texturesFor(List<WheelItem> items) {
    List<String> paths =
        new ArrayList<>(
            List.of(
                DISC_TEXTURE,
                SPOKE_TEXTURE,
                POINTER_TEXTURE,
                BUTTON_UP_TEXTURE,
                BUTTON_OVER_TEXTURE,
                BUTTON_DOWN_TEXTURE,
                GLOW_TEXTURE,
                RAYS_TEXTURE));
    items.forEach(item -> paths.add(item.type().getTexturePath()));
    return paths.toArray(new String[0]);
  }

  @Override
  public void create() {
    super.create();
    addActors();
  }

  private void addActors() {
    table = new Table();
    table.setFillParent(true);

    TextButton.TextButtonStyle style = buttonStyle();
    TextButton spinBtn = new TextButton("Spin", style);
    spinBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Spin button clicked");
            spin(spinBtn);
          }
        });

    TextButton backBtn = new TextButton("Back", style);
    backBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Back button clicked");
            entity.getEvents().trigger("back");
          }
        });

    table.add(createWheel()).size(WHEEL_SIZE).padTop(30f);
    table.row();
    table.add(spinBtn).size(BUTTON_WIDTH, BUTTON_HEIGHT).padTop(24f);
    table.row();
    table.add(backBtn).size(BUTTON_WIDTH, BUTTON_HEIGHT).padTop(12f);

    prizeLayer = createPrizeLayer();

    stage.addActor(table);
    stage.addActor(prizeLayer);
  }

  /**
   * Builds the style shared by the wheel's buttons. The art is stretched as a nine patch so the
   * corners stay crisp at any button size.
   *
   * @return the wheel's button style
   */
  private TextButton.TextButtonStyle buttonStyle() {
    TextButton.TextButtonStyle style =
        new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));
    style.up = ninePatch(BUTTON_UP_TEXTURE);
    style.over = ninePatch(BUTTON_OVER_TEXTURE);
    style.down = ninePatch(BUTTON_DOWN_TEXTURE);
    style.fontColor = BUTTON_TEXT_COLOUR;
    style.overFontColor = BUTTON_TEXT_COLOUR;
    style.downFontColor = BUTTON_PRESSED_TEXT_COLOUR;
    return style;
  }

  /**
   * @param path the path of the button texture
   * @return the texture as a stretchable nine patch
   */
  private NinePatchDrawable ninePatch(String path) {
    Texture texture = texture(path, Texture.TextureFilter.Nearest);
    return new NinePatchDrawable(
        new NinePatch(texture, BUTTON_PATCH, BUTTON_PATCH, BUTTON_PATCH, BUTTON_PATCH));
  }

  /**
   * Copies one of the skin's label styles so the wheel can recolour it. Colouring the label itself
   * would tint the skin's colour rather than replace it, and the skin draws these styles in black
   * and orange.
   *
   * @param name the name of the style in the skin
   * @param colour the colour to draw the text in
   * @return the recoloured style
   */
  private Label.LabelStyle labelStyle(String name, Color colour) {
    Label.LabelStyle style = new Label.LabelStyle(skin.get(name, Label.LabelStyle.class));
    style.fontColor = colour;
    return style;
  }

  /**
   * Builds the prize card and the full screen layer that catches the input dismissing it. The layer
   * stays hidden until the wheel lands on an item.
   *
   * @return the layer holding the prize card
   */
  private Table createPrizeLayer() {
    Image rays = loadImage(RAYS_TEXTURE, Texture.TextureFilter.Linear);
    rays.setSize(CARD_SIZE, CARD_SIZE);
    rays.setOrigin(CARD_SIZE / 2f, CARD_SIZE / 2f);
    rays.setColor(RAYS_COLOUR);
    rays.addAction(Actions.forever(Actions.rotateBy(360f, RAY_SPIN_DURATION)));

    Image glow = loadImage(GLOW_TEXTURE);
    glow.setSize(CARD_SIZE, CARD_SIZE);
    glow.setColor(GLOW_COLOUR);

    prizeIcon = new Image();
    prizeIcon.setScaling(Scaling.fit);
    prizeName = new Label("", labelStyle("title", PRIZE_NAME_COLOUR));
    prizeAmount = new Label("", labelStyle("large", PRIZE_AMOUNT_COLOUR));

    prizeContent = new Table();
    prizeContent.add(prizeIcon).size(PRIZE_ICON_SIZE);
    prizeContent.row();
    prizeContent.add(prizeName).padTop(12f);
    prizeContent.row();
    prizeContent.add(prizeAmount).padTop(2f);

    prizeCard = new Group();
    prizeCard.setSize(CARD_SIZE, CARD_SIZE);
    prizeCard.setOrigin(CARD_SIZE / 2f, CARD_SIZE / 2f);
    prizeCard.setTransform(true);
    prizeCard.addActor(rays);
    prizeCard.addActor(glow);
    prizeCard.addActor(prizeContent);

    Table layer = new Table();
    layer.setFillParent(true);
    layer.setTouchable(Touchable.enabled);
    layer.setVisible(false);
    layer.add(prizeCard).size(CARD_SIZE, CARD_SIZE);

    layer.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            dismissPrize();
          }
        });
    layer.addListener(
        new InputListener() {
          @Override
          public boolean keyDown(InputEvent event, int keycode) {
            dismissPrize();
            return true;
          }
        });
    return layer;
  }

  /**
   * Shows what the wheel landed on, growing the card in over a glow. Any click or key press then
   * dismisses it.
   *
   * @param item the item that was won
   */
  private void showPrize(WheelItem item) {
    Texture icon = texture(item.type().getTexturePath(), Texture.TextureFilter.Nearest);
    prizeIcon.setDrawable(new TextureRegionDrawable(new TextureRegion(icon)));
    prizeName.setText(item.type().getDisplayName());
    prizeAmount.setText("x" + item.value());

    prizeContent.pack();
    prizeContent.setPosition(
        (CARD_SIZE - prizeContent.getWidth()) / 2f, (CARD_SIZE - prizeContent.getHeight()) / 2f);

    table.clearActions();
    table.addAction(Actions.sequence(Actions.fadeOut(WHEEL_FADE_DURATION), Actions.visible(false)));

    prizeLayer.setVisible(true);
    prizeLayer.toFront();
    stage.setKeyboardFocus(prizeLayer);

    prizeCard.clearActions();
    prizeCard.setScale(CARD_START_SCALE);
    prizeCard.getColor().a = 0f;
    prizeCard.addAction(
        Actions.parallel(
            Actions.scaleTo(1f, 1f, CARD_GROW_DURATION, Interpolation.swingOut),
            Actions.fadeIn(CARD_FADE_DURATION)));
  }

  /** Hides the prize and closes the wheel. */
  private void dismissPrize() {
    if (!prizeLayer.isVisible()) {
      return;
    }
    logger.debug("Prize dismissed");
    prizeLayer.setVisible(false);
    stage.setKeyboardFocus(null);
    entity.getEvents().trigger("back");
  }

  /**
   * Builds the wheel. The disc sits in a group that rotates while the pointer stays put while the
   * wheel turns beneath it.
   *
   * @return the container holding the wheel and its pointer
   */
  private Group createWheel() {
    List<WheelItem> items = wheel.getItems();
    float centre = WHEEL_SIZE / 2f;
    float radius = WHEEL_SIZE / 2f;
    float seg = 360f / items.size();

    wheelGroup = new Group();
    wheelGroup.setSize(WHEEL_SIZE, WHEEL_SIZE);
    wheelGroup.setOrigin(centre, centre);
    wheelGroup.setTransform(true);

    Image disc = loadImage(DISC_TEXTURE);
    disc.setSize(WHEEL_SIZE, WHEEL_SIZE);
    wheelGroup.addActor(disc);

    for (int i = 0; i < items.size(); i++) {
      wheelGroup.addActor(createSpoke(seg * i, centre, radius));
    }

    for (int i = 0; i < items.size(); i++) {
      wheelGroup.addActor(createSegment(items.get(i), (seg * i) + seg / 2f, centre, radius));
    }

    Image pointer = loadImage(POINTER_TEXTURE);
    pointer.setSize(POINTER_SIZE, POINTER_SIZE);
    pointer.setPosition(centre - POINTER_SIZE / 2f, WHEEL_SIZE - POINTER_SIZE / 2f);

    Group container = new Group();
    container.setSize(WHEEL_SIZE, WHEEL_SIZE);
    container.addActor(wheelGroup);
    container.addActor(pointer);
    return container;
  }

  /**
   * Creates a divider between two segments
   *
   * @param angle the boundary's angle in degrees
   * @param centre the hub's position within the wheel group
   * @param radius the wheel's radius
   * @return the spoke
   */
  private Image createSpoke(float angle, float centre, float radius) {
    Image spoke = loadImage(SPOKE_TEXTURE);
    spoke.setSize(radius, SPOKE_THICKNESS);
    spoke.setPosition(centre, centre - SPOKE_THICKNESS / 2f);
    spoke.setOrigin(0f, SPOKE_THICKNESS / 2f);
    spoke.setRotation(angle);
    return spoke;
  }

  /**
   * Places an item's sprite and the amount it awards in the middle of its segment.
   *
   * @param item the item to show
   * @param midAngle the angle at the centre of the item's segment
   * @param centre the hub's position within the wheel group
   * @param radius the wheel's radius
   * @return an actor holding the sprite and amount
   */
  private Actor createSegment(WheelItem item, float midAngle, float centre, float radius) {
    Image icon = loadImage(item.type().getTexturePath());
    icon.setScaling(Scaling.fit);

    Table content = new Table();
    content.add(icon).size(ICON_SIZE);
    content.row();
    content.add(new Label("x" + item.value(), skin));
    content.pack();
    content.setOrigin(Align.center);
    content.setTransform(true);

    float distance = radius * SEGMENT_RADIUS_RATIO;
    content.setPosition(
        centre + distance * MathUtils.cosDeg(midAngle) - content.getWidth() / 2f,
        centre + distance * MathUtils.sinDeg(midAngle) - content.getHeight() / 2f);
    content.setRotation(midAngle - POINTER_ANGLE);
    return content;
  }

  /**
   * Loads a texture through the resource service.
   *
   * @param path the path of the texture
   * @param filter how the texture is sampled when it is not drawn at its own size
   * @return the texture
   */
  private Texture texture(String path, Texture.TextureFilter filter) {
    Texture texture = ServiceLocator.getResourceService().getAsset(path, Texture.class);
    texture.setFilter(filter, filter);
    return texture;
  }

  /**
   * @param path the path of the texture
   * @return an image, sampled so its pixels stay sharp
   */
  private Image loadImage(String path) {
    return loadImage(path, Texture.TextureFilter.Nearest);
  }

  /**
   * @param path the path of the texture
   * @param filter how the texture is sampled when it is not drawn at its own size
   * @return an image
   */
  private Image loadImage(String path, Texture.TextureFilter filter) {
    return new Image(texture(path, filter));
  }

  /**
   * Spins the wheel and animates it to a stop with the winning segment under the pointer.
   *
   * @param spinBtn the button that started the spin
   */
  private void spin(TextButton spinBtn) {
    spinBtn.setDisabled(true);
    WheelItem result = wheel.spin();
    float target = wheel.getTargetRotation(wheelGroup.getRotation(), POINTER_ANGLE, FULL_TURNS);

    wheelGroup.addAction(
        Actions.sequence(
            Actions.rotateTo(target, SPIN_DURATION, Interpolation.pow4Out),
            Actions.run(
                () -> {
                  wheelGroup.setRotation(wheelGroup.getRotation() % 360f);
                  spinBtn.setDisabled(false);
                  showPrize(result);
                })));
  }

  /** Brings the wheel above everything */
  public void toFront() {
    table.toFront();
    prizeLayer.toFront();
  }

  @Override
  public void draw(SpriteBatch batch) {}

  @Override
  public float getZIndex() {
    return Z_INDEX;
  }

  @Override
  public void dispose() {
    table.remove();
    prizeLayer.remove();
    super.dispose();
  }
}
