package com.csse3200.game.components.minigames.blackjack;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.services.ServiceLocator;
import java.util.List;

/** Shared look for the Blackjack UI: wooden image buttons and the parchment scroll panel. */
final class BlackjackUiStyles {
  private BlackjackUiStyles() {}

  /**
   * Creates a button from two loaded textures, with a greyed-out version for the disabled state.
   *
   * @param upPath texture shown normally
   * @param downPath texture shown while pressed
   * @return the new button
   */
  static ImageButton imageButton(String upPath, String downPath) {
    Texture upTexture = ServiceLocator.getResourceService().getAsset(upPath, Texture.class);
    Texture downTexture = ServiceLocator.getResourceService().getAsset(downPath, Texture.class);

    upTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    downTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

    ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
    style.up = new TextureRegionDrawable(upTexture);
    style.down = new TextureRegionDrawable(downTexture);
    style.disabled = disabledDrawable(upTexture);

    return new ImageButton(style);
  }

  private static SpriteDrawable disabledDrawable(Texture texture) {
    Sprite sprite = new Sprite(texture);
    sprite.setColor(0.45f, 0.45f, 0.45f, 0.75f);
    return new SpriteDrawable(sprite);
  }

  /**
   * Draws the parchment scroll with wooden rods used behind Blackjack pop-ups.
   *
   * @param ownedTextures list the generated texture is added to, so the caller can dispose it
   * @return a stretchable parchment background
   */
  static NinePatchDrawable parchment(List<Texture> ownedTextures) {
    int rodHeight = 24;
    int paperEdge = 16;
    int width = 80;
    int height = 72;

    Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

    Color paper = new Color(0.82f, 0.68f, 0.43f, 1f);
    Color wood = new Color(0.35f, 0.18f, 0.08f, 1f);
    Color edge = new Color(0.55f, 0.38f, 0.20f, 1f);

    pixmap.setColor(paper);
    pixmap.fillRectangle(paperEdge, 0, width - paperEdge * 2, height);

    pixmap.setColor(edge);
    pixmap.fillRectangle(paperEdge, 0, 4, height);
    pixmap.fillRectangle(width - paperEdge - 4, 0, 4, height);

    pixmap.setColor(wood);
    pixmap.fillRectangle(0, 0, width, rodHeight);
    pixmap.fillRectangle(0, height - rodHeight, width, rodHeight);

    int knobRadius = 8;
    pixmap.fillCircle(knobRadius, knobRadius, knobRadius);
    pixmap.fillCircle(width - knobRadius, knobRadius, knobRadius);
    pixmap.fillCircle(knobRadius, height - knobRadius, knobRadius);
    pixmap.fillCircle(width - knobRadius, height - knobRadius, knobRadius);

    Texture texture = new Texture(pixmap);
    pixmap.dispose();
    ownedTextures.add(texture);

    NinePatch patch = new NinePatch(texture, paperEdge, paperEdge, rodHeight, rodHeight);
    return new NinePatchDrawable(patch);
  }
}
