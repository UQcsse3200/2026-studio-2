package com.csse3200.game.components.minigames.cyclopsMinigame;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.csse3200.game.rendering.RenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Draws the cave behind the room in two parts, both on the render layer behind the entities.
 *
 * <ol>
 *   <li>The backdrop: ONE image (no tiling, no mirrored copy) that covers the whole camera view and
 *       stays still on screen, so it never appears to move.
 *   <li>The stone floor: a strip locked to the WORLD, tiled along the room. It scrolls at exactly
 *       the same speed as the props, the platform, the cyclops and the player, so only the camera
 *       appears to move.
 * </ol>
 *
 * <p>The floor strip covers the backdrop's own stone floor row, so that row is never seen.
 */
public class CyclopsCaveBackgroundComponent extends RenderComponent {
  /** How much the backdrop slides as the camera moves along the room (0 = it stays still). */
  static final float BACKDROP_PAN_FRACTION = 0f;

  private final String backdropPath;
  private final String floorPath;
  private final OrthographicCamera camera;
  private final float roomWidth;
  private final float floorY;
  private final int floorRowFromTop;
  private Texture backdrop;
  private Texture floorTile;

  /**
   * @param backdropPath asset path of the backdrop (must be loaded by the resource service)
   * @param floorPath asset path of the floor tile, which must tile seamlessly sideways (must be
   *     loaded by the resource service)
   * @param camera camera that is rendering the room
   * @param roomWidth world width of the room (only used if the backdrop is allowed to slide)
   * @param floorY world y of the floor line
   * @param floorRowFromTop row of the backdrop (0 = top) where its stone floor's top edge is; the
   *     floor tile is cut from that row down to the bottom of the backdrop
   */
  public CyclopsCaveBackgroundComponent(
      String backdropPath,
      String floorPath,
      OrthographicCamera camera,
      float roomWidth,
      float floorY,
      int floorRowFromTop) {
    this.backdropPath = backdropPath;
    this.floorPath = floorPath;
    this.camera = camera;
    this.roomWidth = roomWidth;
    this.floorY = floorY;
    this.floorRowFromTop = floorRowFromTop;
  }

  @Override
  public void create() {
    super.create();
    backdrop = ServiceLocator.getResourceService().getAsset(backdropPath, Texture.class);
    backdrop.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    floorTile = ServiceLocator.getResourceService().getAsset(floorPath, Texture.class);
    floorTile.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
  }

  /** Layer 0 draws behind the entities (same layer the terrain uses). */
  @Override
  public int getLayer() {
    return 0;
  }

  /**
   * Works out where to draw the single backdrop image.
   *
   * @return {left, bottom, width, height} in world units
   */
  static float[] layout(
      float textureWidth,
      float textureHeight,
      float viewWidth,
      float viewHeight,
      float cameraX,
      float roomWidth,
      float floorY,
      int floorRowFromTop,
      float panFraction) {
    float scale = Math.max(viewHeight / textureHeight, viewWidth / textureWidth);
    float width = textureWidth * scale;
    float height = textureHeight * scale;
    float bottom = floorY - (textureHeight - floorRowFromTop) * scale;
    float progress = MathUtils.clamp(cameraX / roomWidth, 0f, 1f);
    float left = cameraX - viewWidth / 2f - progress * (width - viewWidth) * panFraction;
    return new float[] {left, bottom, width, height};
  }

  /**
   * Which floor tiles (numbered from the room's left end, tile i spanning i*tileWidth to
   * (i+1)*tileWidth in world space) are needed to cover the view.
   *
   * @return {first, last}
   */
  static int[] floorTiles(float viewLeft, float viewRight, float tileWidth) {
    return new int[] {
      MathUtils.floor(viewLeft / tileWidth), MathUtils.floor(viewRight / tileWidth)
    };
  }

  /** World height of the floor strip: it reaches exactly to the bottom of the camera view. */
  static float floorStripHeight(float viewHeight, float textureHeight, int floorRowFromTop) {
    return (textureHeight - floorRowFromTop) * viewHeight / textureHeight;
  }

  @Override
  protected void draw(SpriteBatch batch) {
    float viewWidth = camera.viewportWidth * camera.zoom;
    float viewHeight = camera.viewportHeight * camera.zoom;

    float[] box =
        layout(
            backdrop.getWidth(),
            backdrop.getHeight(),
            viewWidth,
            viewHeight,
            camera.position.x,
            roomWidth,
            floorY,
            floorRowFromTop,
            BACKDROP_PAN_FRACTION);
    batch.draw(backdrop, box[0], box[1], box[2], box[3]);

    float worldPerPixel = viewHeight / backdrop.getHeight();
    float stripHeight = floorStripHeight(viewHeight, backdrop.getHeight(), floorRowFromTop);
    float tileWidth = floorTile.getWidth() * worldPerPixel;
    int[] tiles =
        floorTiles(
            camera.position.x - viewWidth / 2f, camera.position.x + viewWidth / 2f, tileWidth);
    for (int i = tiles[0]; i <= tiles[1]; i++) {
      batch.draw(floorTile, i * tileWidth, floorY - stripHeight, tileWidth, stripHeight);
    }
  }
}
