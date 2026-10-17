package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.services.ServiceLocator;

/**
 * Renders the portal at the end of a level: a texture holding two frames side by side (dark, then
 * glowing), with a bobbing arrow and a "NEXT LEVEL" label above it. The portal stays dark until the
 * player uses it, then lights up.
 */
public class LevelPortalRenderComponent extends RenderComponent {
  // Drawn behind the player, so they walk in front of it rather than being hidden by it.
  private static final float BEHIND_PLAYER_Z_INDEX = -100f;
  private static final int FRAME_COUNT = 2;
  private static final int DARK_FRAME = 0;
  private static final int GLOWING_FRAME = 1;

  private final Texture texture;
  private final TextureRegion[] frames = new TextureRegion[FRAME_COUNT];
  private final BobbingArrowIndicator indicator = new BobbingArrowIndicator("NEXT LEVEL");
  private boolean lit;

  /**
   * @param texturePath Internal path of a texture holding the portal frames side by side.
   */
  public LevelPortalRenderComponent(String texturePath) {
    this(ServiceLocator.getResourceService().getAsset(texturePath, Texture.class));
  }

  /**
   * @param texture texture holding the portal frames side by side
   */
  public LevelPortalRenderComponent(Texture texture) {
    this.texture = texture;
    int frameWidth = texture.getWidth() / FRAME_COUNT;
    for (int i = 0; i < FRAME_COUNT; i++) {
      frames[i] = new TextureRegion(texture, i * frameWidth, 0, frameWidth, texture.getHeight());
    }
  }

  /** Scale the entity to a width of 1 and a height matching a single frame's ratio. */
  public void scaleEntity() {
    entity.setScale(1f, (float) texture.getHeight() / (texture.getWidth() / (float) FRAME_COUNT));
  }

  @Override
  public void create() {
    super.create();
    indicator.create();
    entity.getEvents().addListener("levelEndActivated", () -> lit = true);
  }

  @Override
  public void update() {
    indicator.update(ServiceLocator.getTimeSource().getDeltaTime());
  }

  @Override
  protected void draw(SpriteBatch batch) {
    Vector2 position = entity.getPosition();
    Vector2 scale = entity.getScale();

    batch.draw(frames[lit ? GLOWING_FRAME : DARK_FRAME], position.x, position.y, scale.x, scale.y);
    indicator.draw(batch, position.x + scale.x / 2f, position.y + scale.y);
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
