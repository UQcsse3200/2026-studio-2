package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.services.ServiceLocator;

/** Displays the open (right-hand) frame of the existing two-frame desert door sheet. */
public class CaveEntranceRenderComponent extends RenderComponent {
  public static final String TEXTURE = "images/terrain/Level_1/Level_1_door.png";
  private final TextureRegion openDoor;

  public CaveEntranceRenderComponent() {
    Texture texture = ServiceLocator.getResourceService().getAsset(TEXTURE, Texture.class);
    int frameWidth = texture.getWidth() / 2;
    // Trim the sheet's outer padding so the stone threshold meets the ground.
    int insetX = frameWidth / 32;
    int insetY = texture.getHeight() / 24;
    openDoor =
        new TextureRegion(
            texture,
            frameWidth + insetX,
            insetY,
            frameWidth - 2 * insetX,
            texture.getHeight() - 3 * insetY);
  }

  @Override
  protected void draw(SpriteBatch batch) {
    Vector2 position = entity.getPosition();
    Vector2 size = entity.getScale();
    batch.draw(openDoor, position.x, position.y, size.x, size.y);
  }

  @Override
  public float getZIndex() {
    return -100f; // Behind the player, pickups and solid foreground terrain.
  }
}
