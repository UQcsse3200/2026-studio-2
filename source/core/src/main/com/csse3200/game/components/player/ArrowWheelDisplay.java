package com.csse3200.game.components.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.projectile.ArrowType;
import com.csse3200.game.ui.UIComponent;

/**
 * Draws the arrow wheel in the middle of the screen while it is open.
 *
 * <p>Wedges are laid out to match {@link ArrowType#forDirection}, so the wedge under the mouse is
 * the one the wheel will select. The highlighted wedge is drawn brighter, and wedges the player has
 * no arrows for are greyed out.
 */
public class ArrowWheelDisplay extends UIComponent {
  private static final float OUTER_RADIUS = 140f;
  private static final float INNER_RADIUS = ArrowType.DEADZONE_RADIUS;
  private static final float WEDGE_GAP_DEGREES = 3f;
  private static final float LABEL_RADIUS = (OUTER_RADIUS + INNER_RADIUS) / 2f;
  private static final int ARC_SEGMENTS = 24;

  private static final Color BACKDROP = new Color(0f, 0f, 0f, 0.55f);
  private static final Color UNAVAILABLE = new Color(0.35f, 0.35f, 0.35f, 0.8f);
  private static final float IDLE_ALPHA = 0.55f;
  private static final float HIGHLIGHT_ALPHA = 0.95f;

  private ArrowWheelComponent wheel;
  private InventoryComponent inventory;
  private ShapeRenderer shapeRenderer;
  private BitmapFont font;
  private final GlyphLayout layout = new GlyphLayout();
  private final Matrix4 screenProjection = new Matrix4();
  private final Color wedgeColour = new Color();

  @Override
  public void create() {
    super.create();
    wheel = entity.getComponent(ArrowWheelComponent.class);
    inventory = entity.getComponent(InventoryComponent.class);
    font = skin.getFont("font");
  }

  @Override
  protected void draw(SpriteBatch batch) {
    if (wheel == null || !wheel.isOpen()) {
      return;
    }

    float screenWidth = Gdx.graphics.getWidth();
    float screenHeight = Gdx.graphics.getHeight();
    float centreX = screenWidth / 2f;
    float centreY = screenHeight / 2f;
    // Work in raw screen pixels so the wheel lines up with the pointer offsets the input reports.
    screenProjection.setToOrtho2D(0f, 0f, screenWidth, screenHeight);

    batch.end();
    drawWedges(centreX, centreY);
    drawLabels(batch, centreX, centreY);
    batch.begin();
  }

  private void drawWedges(float centreX, float centreY) {
    if (shapeRenderer == null) {
      shapeRenderer = new ShapeRenderer();
    }
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

    shapeRenderer.setProjectionMatrix(screenProjection);
    shapeRenderer.begin(ShapeType.Filled);

    shapeRenderer.setColor(BACKDROP);
    shapeRenderer.circle(centreX, centreY, OUTER_RADIUS + 8f, ARC_SEGMENTS * 3);

    ArrowType[] types = ArrowType.getWheelTypes();
    float wedgeDegrees = 360f / types.length;
    for (int i = 0; i < types.length; i++) {
      ArrowType type = types[i];
      // Wedge i is centred i * wedgeDegrees clockwise from the top, matching forDirection().
      float centreDegrees = 90f - i * wedgeDegrees;
      float startDegrees = centreDegrees - wedgeDegrees / 2f + WEDGE_GAP_DEGREES / 2f;
      float sweepDegrees = wedgeDegrees - WEDGE_GAP_DEGREES;
      boolean highlighted = type == wheel.getHighlighted();

      shapeRenderer.setColor(wedgeColourFor(type, highlighted));
      float radius = highlighted ? OUTER_RADIUS + 6f : OUTER_RADIUS;
      shapeRenderer.arc(centreX, centreY, radius, startDegrees, sweepDegrees, ARC_SEGMENTS);
    }

    // Punch out the deadzone so the middle reads as "nothing selected".
    shapeRenderer.setColor(BACKDROP);
    shapeRenderer.circle(centreX, centreY, INNER_RADIUS, ARC_SEGMENTS);
    shapeRenderer.end();

    Gdx.gl.glDisable(GL20.GL_BLEND);
  }

  private Color wedgeColourFor(ArrowType type, boolean highlighted) {
    if (!wheel.isAvailable(type)) {
      return UNAVAILABLE;
    }
    wedgeColour.set(type.getTintColor());
    wedgeColour.a = highlighted ? HIGHLIGHT_ALPHA : IDLE_ALPHA;
    return wedgeColour;
  }

  private void drawLabels(SpriteBatch batch, float centreX, float centreY) {
    Matrix4 previousProjection = batch.getProjectionMatrix().cpy();
    batch.setProjectionMatrix(screenProjection);
    batch.begin();

    ArrowType[] types = ArrowType.getWheelTypes();
    float wedgeDegrees = 360f / types.length;
    for (int i = 0; i < types.length; i++) {
      ArrowType type = types[i];
      float centreRadians = (90f - i * wedgeDegrees) * MathUtils.degreesToRadians;
      float x = centreX + MathUtils.cos(centreRadians) * LABEL_RADIUS;
      float y = centreY + MathUtils.sin(centreRadians) * LABEL_RADIUS;

      layout.setText(font, labelFor(type));
      font.setColor(wheel.isAvailable(type) ? Color.WHITE : Color.LIGHT_GRAY);
      font.draw(batch, layout, x - layout.width / 2f, y + layout.height / 2f);
    }

    ArrowType selected = wheel.getSelected();
    if (selected != null) {
      layout.setText(font, selected.getLabel());
      font.setColor(Color.WHITE);
      font.draw(batch, layout, centreX - layout.width / 2f, centreY + layout.height / 2f);
    }

    batch.end();
    batch.setProjectionMatrix(previousProjection);
  }

  /** Label for a wedge, with the ammo count if the player has an inventory. */
  private String labelFor(ArrowType type) {
    if (inventory == null) {
      return type.getLabel();
    }
    ItemType arrowItem = ArrowWheelComponent.arrowItemFor(type);
    int count = arrowItem == null ? 0 : inventory.getItemCount(arrowItem);
    return type.getLabel() + " x" + count;
  }

  @Override
  public void dispose() {
    if (shapeRenderer != null) {
      shapeRenderer.dispose();
    }
    super.dispose();
  }
}
