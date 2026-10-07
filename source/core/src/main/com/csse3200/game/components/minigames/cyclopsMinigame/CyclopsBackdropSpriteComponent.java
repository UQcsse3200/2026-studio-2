package com.csse3200.game.components.minigames.cyclopsMinigame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.rendering.RenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Draws a static pixel-art sprite (the cyclops' rock platform) at a fixed pixel size.
 *
 * <p>The entity position is the TOP-CENTRE of the sprite, so the sprite hangs downward from it.
 * Entities with a higher y are drawn behind, so putting the platform's top just above the cyclops
 * keeps it behind him.
 */
public class CyclopsBackdropSpriteComponent extends RenderComponent {
  private final String texturePath;
  private final float pixelWorldSize;
  private float tintRed = 1f;
  private float tintGreen = 1f;
  private float tintBlue = 1f;
  private Texture texture;

  /**
   * @param texturePath asset path of the sprite (must be loaded by the resource service)
   * @param pixelWorldSize world units covered by one sprite pixel
   */
  public CyclopsBackdropSpriteComponent(String texturePath, float pixelWorldSize) {
    this.texturePath = texturePath;
    this.pixelWorldSize = pixelWorldSize;
  }

  /** Multiplies the sprite's colours (1,1,1 = unchanged), e.g. to push it into the background. */
  public CyclopsBackdropSpriteComponent setTint(float red, float green, float blue) {
    this.tintRed = red;
    this.tintGreen = green;
    this.tintBlue = blue;
    return this;
  }

  @Override
  public void create() {
    super.create();
    texture = ServiceLocator.getResourceService().getAsset(texturePath, Texture.class);
    texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
  }

  @Override
  protected void draw(SpriteBatch batch) {
    Vector2 position = entity.getPosition();
    float width = texture.getWidth() * pixelWorldSize;
    float height = texture.getHeight() * pixelWorldSize;
    float previous = batch.getPackedColor();
    batch.setColor(tintRed, tintGreen, tintBlue, 1f);
    batch.draw(texture, position.x - width / 2f, position.y - height, width, height);
    batch.setPackedColor(previous);
  }
}
