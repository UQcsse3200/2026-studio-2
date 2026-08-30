package com.csse3200.game.components.minigames.spinthewheel;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
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
  private static final float WHEEL_SIZE = 400f;
  private static final float SPOKE_THICKNESS = 6f;
  private static final float POINTER_SIZE = 32f;
  private static final float LABEL_RADIUS_RATIO = 0.65f;
  private static final float POINTER_ANGLE = 90f;
  private static final float SPIN_DURATION = 3.5f;
  private static final int FULL_TURNS = 4;

  private final WheelLogic wheel;
  private Table table;
  private Label resultLabel;
  private Group wheelGroup;

  public SpinTheWheelDisplay(List<WheelItem> items) {
    this.wheel = new WheelLogic(items);
  }

  @Override
  public void create() {
    super.create();
    addActors();
  }

  private void addActors() {
    table = new Table();
    table.setFillParent(true);
    resultLabel = new Label("", skin);

    TextButton spinBtn = new TextButton("Spin", skin);
    spinBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Spin button clicked");
            spin(spinBtn);
          }
        });

    table.row();
    table.add(createWheel()).size(WHEEL_SIZE).padTop(30f);
    table.row();
    table.add(resultLabel).padTop(30f);
    table.row();
    table.add(spinBtn).padTop(30f);

    TextButton backBtn = new TextButton("Back", skin);
    backBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Back button clicked");
            entity.getEvents().trigger("back");
          }
        });

    table.row();
    table.add(backBtn).padTop(30f);

    stage.addActor(table);
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
      wheelGroup.addActor(createLabel(items.get(i), (seg * i) + seg / 2f, centre, radius));
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
   * Places an item's label in the middle of its segment
   *
   * @param item the item to label
   * @param midAngle the angle at the centre of the item's segment
   * @param centre the hub's position within the wheel group
   * @param radius the wheel's radius
   * @return an actor holding the label
   */
  private Actor createLabel(WheelItem item, float midAngle, float centre, float radius) {
    Label label = new Label(item.name(), skin);
    label.pack();
    Group holder = new Group();
    holder.setSize(label.getWidth(), label.getHeight());
    holder.setOrigin(Align.center);
    holder.setTransform(true);
    holder.addActor(label);

    float distance = radius * LABEL_RADIUS_RATIO;
    holder.setPosition(
        centre + distance * MathUtils.cosDeg(midAngle) - label.getWidth() / 2f,
        centre + distance * MathUtils.sinDeg(midAngle) - label.getHeight() / 2f);
    holder.setRotation(midAngle - POINTER_ANGLE);
    return holder;
  }

  /**
   * Loads a texture through the resource service.
   *
   * @param path the path of the texture
   * @return an image
   */
  private Image loadImage(String path) {
    Texture texture = ServiceLocator.getResourceService().getAsset(path, Texture.class);
    texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    return new Image(texture);
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
                  resultLabel.setText(result.name() + " x" + result.value());
                  spinBtn.setDisabled(false);
                })));
  }

  @Override
  public void draw(SpriteBatch batch) {}

  @Override
  public float getZIndex() {
    return Z_INDEX;
  }

  @Override
  public void dispose() {
    table.clear();
    super.dispose();
  }
}
