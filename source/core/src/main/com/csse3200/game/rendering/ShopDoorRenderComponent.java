package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.services.ServiceLocator;

/**
 * Renders the shop door: a texture holding two frames side by side (closed, then open). The door
 * opens when the shop does and closes when it closes, and a bobbing arrow with a "SHOP" label above
 * it catches the player's eye.
 */
public class ShopDoorRenderComponent extends RenderComponent {
  // Drawn behind the player, so they walk in front of it rather than being hidden by it.
  private static final float BEHIND_PLAYER_Z_INDEX = -100f;
  private static final int FRAME_COUNT = 2;
  private static final int CLOSED_FRAME = 0;
  private static final int OPEN_FRAME = 1;

  // Measurements of shop_door.png (1259 px wide), in pixels. The two frames aren't drawn in the
  // same place within their halves of the image: the open door sits about 80 px further right. So
  // each frame is cropped to its drawing (the same 553 px window around it), which keeps the door
  // from jumping sideways as it opens, and the arrow is centred on the shop's sign.
  private static final float REFERENCE_WIDTH = 1259f;
  private static final float[] FRAME_X = {0f, 706f};
  private static final float FRAME_WIDTH = 553f;
  private static final float SIGN_CENTRE_X = 243f;

  private final Texture texture;
  private final TextureRegion[] frames = new TextureRegion[FRAME_COUNT];
  private final BobbingArrowIndicator indicator = new BobbingArrowIndicator("SHOP");
  private boolean open;

  /**
   * @param texturePath Internal path of a texture holding the door frames side by side.
   */
  public ShopDoorRenderComponent(String texturePath) {
    this(ServiceLocator.getResourceService().getAsset(texturePath, Texture.class));
  }

  /**
   * @param texture texture holding the door frames side by side
   */
  public ShopDoorRenderComponent(Texture texture) {
    this.texture = texture;
    float pixelsPerReferencePixel = texture.getWidth() / REFERENCE_WIDTH;
    for (int i = 0; i < FRAME_COUNT; i++) {
      frames[i] =
          new TextureRegion(
              texture,
              Math.round(FRAME_X[i] * pixelsPerReferencePixel),
              0,
              Math.round(FRAME_WIDTH * pixelsPerReferencePixel),
              texture.getHeight());
    }
  }

  /** Scale the entity to a width of 1 and a height matching a single frame's ratio. */
  public void scaleEntity() {
    entity.setScale(1f, texture.getHeight() / (texture.getWidth() / (float) FRAME_COUNT));
  }

  @Override
  public void create() {
    super.create();
    indicator.create();
    entity.getEvents().addListener("shopOpened", () -> open = true);
    entity.getEvents().addListener("shopClosed", () -> open = false);
  }

  @Override
  public void update() {
    indicator.update(ServiceLocator.getTimeSource().getDeltaTime());
  }

  @Override
  protected void draw(SpriteBatch batch) {
    Vector2 position = entity.getPosition();
    Vector2 scale = entity.getScale();

    // The entity is as wide as one half of the image, but only the cropped window is drawn, from
    // the same left edge for both frames.
    float unitsPerReferencePixel = scale.x / (REFERENCE_WIDTH / FRAME_COUNT);
    TextureRegion frame = frames[open ? OPEN_FRAME : CLOSED_FRAME];
    batch.draw(frame, position.x, position.y, FRAME_WIDTH * unitsPerReferencePixel, scale.y);
    indicator.draw(
        batch, position.x + SIGN_CENTRE_X * unitsPerReferencePixel, position.y + scale.y);
  }

  @Override
  public float getZIndex() {
    return BEHIND_PLAYER_Z_INDEX;
  }

  @Override
  public void dispose() {
    super.dispose();
    indicator.dispose();
  }
}
