package com.csse3200.game.components.player;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.csse3200.game.components.projectile.ArrowType;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Draws the arrow wheel in the middle of the screen while it is open, as a pie split evenly into
 * one slice per arrow type.
 *
 * <p>Slice i is centred i * 120 degrees clockwise from the top (the same angles {@link
 * ArrowType#forDirection} uses), so the slice under the pointer is the one the wheel will select.
 * The wheel tells {@link ArrowWheelComponent} where its centre is on screen so the pointer can be
 * measured from it. The highlighted slice lights up, slices the player has no arrows for are faded,
 * and a disc in the middle shows the arrow currently equipped.
 */
public class ArrowWheelDisplay extends UIComponent {
  private static final float WHEEL_RADIUS = 170f;
  private static final float ICON_SIZE = 76f;
  private static final float CENTRE_ICON_SIZE = 44f;
  // How far from the centre the icons sit, as a share of the wheel's radius.
  private static final float ICON_RING_SHARE = 0.62f;
  private static final float LABEL_GAP = 18f;
  private static final float UNAVAILABLE_ALPHA = 0.35f;

  // The pie is drawn into textures at a fixed resolution, then scaled to WHEEL_RADIUS.
  private static final int TEXTURE_RADIUS = 256;
  private static final float SLICE_GAP_PIXELS = 5f;
  private static final float BORDER_PIXELS = 5f;

  private static final Color SLICE_FILL = new Color(0.07f, 0.16f, 0.18f, 0.88f);
  private static final Color SLICE_HIGHLIGHT_FILL = new Color(0.78f, 0.55f, 0.12f, 0.95f);
  private static final Color SLICE_BORDER = new Color(0.45f, 0.26f, 0.1f, 1f);
  private static final Color SLICE_HIGHLIGHT_BORDER = new Color(1f, 0.82f, 0.25f, 1f);
  private static final Color LABEL_COLOR = new Color(1f, 0.82f, 0.25f, 1f);

  private static final String STANDARD_ARROW_TEXTURE = "images/projectiles/arrow.png";
  private static final String FIRE_ARROW_TEXTURE = "images/projectiles/fire_arrow.png";
  private static final String ICE_ARROW_TEXTURE = "images/projectiles/ice_arrow.png";

  private final Map<ArrowType, Image> slices = new EnumMap<>(ArrowType.class);
  private final Map<ArrowType, Image> icons = new EnumMap<>(ArrowType.class);
  private final Map<ArrowType, TextureRegionDrawable> normalSlices = new EnumMap<>(ArrowType.class);
  private final Map<ArrowType, TextureRegionDrawable> highlightedSlices =
      new EnumMap<>(ArrowType.class);
  private final List<Texture> generatedTextures = new ArrayList<>();

  private ArrowWheelComponent wheel;
  private Group root;
  private Image centreIcon;
  private Label label;

  @Override
  public void create() {
    super.create();
    wheel = entity.getComponent(ArrowWheelComponent.class);

    root = new Group();
    root.setTouchable(Touchable.disabled);
    root.setVisible(false);

    ArrowType[] types = ArrowType.getWheelTypes();
    float diameter = WHEEL_RADIUS * 2f;
    for (int i = 0; i < types.length; i++) {
      ArrowType type = types[i];
      normalSlices.put(type, createSlice(i, types.length, SLICE_FILL, SLICE_BORDER));
      highlightedSlices.put(
          type, createSlice(i, types.length, SLICE_HIGHLIGHT_FILL, SLICE_HIGHLIGHT_BORDER));

      Image slice = new Image(normalSlices.get(type));
      slice.setSize(diameter, diameter);
      slices.put(type, slice);
      root.addActor(slice);
    }

    // The centre disc fills the hole in the pie, which is the pointer's deadzone.
    Image centre = new Image(createCentreDisc());
    float centreDiameter = ArrowType.DEADZONE_RADIUS * 2f - SLICE_GAP_PIXELS;
    centre.setSize(centreDiameter, centreDiameter);
    centre.setPosition(WHEEL_RADIUS - centreDiameter / 2f, WHEEL_RADIUS - centreDiameter / 2f);
    root.addActor(centre);

    centreIcon = new Image();
    centreIcon.setScaling(Scaling.fit);
    centreIcon.setSize(CENTRE_ICON_SIZE, CENTRE_ICON_SIZE);
    centreIcon.setPosition(
        WHEEL_RADIUS - CENTRE_ICON_SIZE / 2f, WHEEL_RADIUS - CENTRE_ICON_SIZE / 2f);
    root.addActor(centreIcon);

    for (int i = 0; i < types.length; i++) {
      Image icon = new Image(getTexture(iconPathFor(types[i])));
      icon.setScaling(Scaling.fit);
      icon.setSize(ICON_SIZE, ICON_SIZE);
      float radians = i * 360f / types.length * MathUtils.degreesToRadians;
      float distance = ICON_RING_SHARE * WHEEL_RADIUS;
      icon.setPosition(
          WHEEL_RADIUS + MathUtils.sin(radians) * distance - ICON_SIZE / 2f,
          WHEEL_RADIUS + MathUtils.cos(radians) * distance - ICON_SIZE / 2f);
      icons.put(types[i], icon);
      root.addActor(icon);
    }

    Label.LabelStyle labelStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
    labelStyle.fontColor = LABEL_COLOR;
    label = new Label("", labelStyle);
    label.setAlignment(Align.center);
    label.setWidth(diameter);
    label.setPosition(0f, -LABEL_GAP - label.getHeight());
    root.addActor(label);

    root.setSize(diameter, diameter);
    stage.addActor(root);
  }

  @Override
  public void draw(SpriteBatch batch) {
    boolean open = wheel != null && wheel.isOpen();
    root.setVisible(open);
    if (!open) {
      return;
    }

    // Centred on the screen. The input measures the pointer from the wheel's centre, in screen
    // pixels from the top left.
    float centreX = stage.getWidth() / 2f;
    float centreY = stage.getHeight() / 2f;
    root.setPosition(centreX - WHEEL_RADIUS, centreY - WHEEL_RADIUS);
    Vector2 centreOnScreen = stage.stageToScreenCoordinates(new Vector2(centreX, centreY));
    wheel.setScreenCentre(centreOnScreen.x, centreOnScreen.y);

    ArrowType highlighted = wheel.getHighlighted();
    for (ArrowType type : ArrowType.getWheelTypes()) {
      boolean available = wheel.isAvailable(type);
      boolean lit = available && type == highlighted;
      Image slice = slices.get(type);
      slice.setDrawable(lit ? highlightedSlices.get(type) : normalSlices.get(type));
      float alpha = available ? 1f : UNAVAILABLE_ALPHA;
      slice.getColor().a = alpha;
      icons.get(type).getColor().a = alpha;
    }

    ArrowType shown = highlighted != null ? highlighted : wheel.getSelected();
    centreIcon.setDrawable(
        new TextureRegionDrawable(new TextureRegion(getTexture(iconPathFor(wheel.getSelected())))));
    label.setText(highlighted != null ? highlighted.getLabel() + " Arrow" : shown.getLabel());
  }

  private static String iconPathFor(ArrowType type) {
    return switch (type) {
      case FIRE -> FIRE_ARROW_TEXTURE;
      case ICE -> ICE_ARROW_TEXTURE;
      default -> STANDARD_ARROW_TEXTURE;
    };
  }

  private static Texture getTexture(String path) {
    return ServiceLocator.getResourceService().getAsset(path, Texture.class);
  }

  /**
   * Draws one slice of the pie into a texture: a wedge centred slice * 360 / count degrees
   * clockwise from the top, with a hole in the middle for the centre disc, a gap between
   * neighbouring slices and a border all round.
   */
  private TextureRegionDrawable createSlice(int slice, int count, Color fill, Color border) {
    int size = TEXTURE_RADIUS * 2;
    float innerRadius = ArrowType.DEADZONE_RADIUS / WHEEL_RADIUS * TEXTURE_RADIUS;
    float halfWedge = 180f / count;
    float centreAngle = slice * 360f / count;

    Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
    for (int y = 0; y < size; y++) {
      for (int x = 0; x < size; x++) {
        float dx = x + 0.5f - TEXTURE_RADIUS;
        float dy = TEXTURE_RADIUS - (y + 0.5f);
        float radius = (float) Math.sqrt(dx * dx + dy * dy);
        if (radius > TEXTURE_RADIUS || radius < innerRadius) {
          continue;
        }

        float clockwise = (MathUtils.atan2(dx, dy) * MathUtils.radiansToDegrees + 360f) % 360f;
        float fromCentre = Math.abs(((clockwise - centreAngle + 540f) % 360f) - 180f);
        float toEdge =
            radius * MathUtils.sin((halfWedge - fromCentre) * MathUtils.degreesToRadians);
        if (fromCentre > halfWedge || toEdge < SLICE_GAP_PIXELS) {
          continue;
        }

        boolean onBorder =
            TEXTURE_RADIUS - radius < BORDER_PIXELS
                || radius - innerRadius < BORDER_PIXELS
                || toEdge < SLICE_GAP_PIXELS + BORDER_PIXELS;
        pixmap.setColor(onBorder ? border : fill);
        pixmap.drawPixel(x, y);
      }
    }
    return toDrawable(pixmap);
  }

  /** Draws the disc in the middle of the pie, with the same border as the slices. */
  private TextureRegionDrawable createCentreDisc() {
    int size = TEXTURE_RADIUS;
    float radius = size / 2f;
    Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
    for (int y = 0; y < size; y++) {
      for (int x = 0; x < size; x++) {
        float dx = x + 0.5f - radius;
        float dy = y + 0.5f - radius;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance <= radius) {
          pixmap.setColor(radius - distance < BORDER_PIXELS ? SLICE_BORDER : SLICE_FILL);
          pixmap.drawPixel(x, y);
        }
      }
    }
    return toDrawable(pixmap);
  }

  private TextureRegionDrawable toDrawable(Pixmap pixmap) {
    Texture texture = new Texture(pixmap);
    texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    pixmap.dispose();
    generatedTextures.add(texture);
    return new TextureRegionDrawable(new TextureRegion(texture));
  }

  @Override
  public void dispose() {
    super.dispose();
    if (root != null) {
      root.remove();
    }
    for (Texture texture : generatedTextures) {
      texture.dispose();
    }
    generatedTextures.clear();
  }
}
