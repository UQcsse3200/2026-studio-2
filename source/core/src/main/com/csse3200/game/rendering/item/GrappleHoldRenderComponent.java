package com.csse3200.game.rendering.item;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.item.weapons.bow.grapple.GrappleComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.components.player.PlayerAnimationController;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.RenderComponent;
import java.util.List;

/**
 * Draws the player hanging from the grapple rope. The art contains a short length of rope, and the
 * whole image is turned every frame so that rope lies along the real rope where it leaves the
 * player. While it is drawing, the player's normal animation is hidden.
 */
public class GrappleHoldRenderComponent extends RenderComponent {
  private static final String TEXTURE_PATH = "images/player/player_rope_hold.png";
  private static final float TEXTURE_SIZE_PX = 256f;

  /** World units per pixel of the art. Raise it to draw the character bigger. */
  private static final float UNITS_PER_PIXEL = 0.0138f;

  /** Column of the rope's centre in the art (the rope fills pixels 125 to 130). */
  private static final float ROPE_AXIS_X_PX = 127.5f;

  /** Row, counted down from the top of the art, where the hands grip the rope. */
  private static final float GRIP_ROW_PX = 125f;

  /** How far above the player's centre, along the rope, the hands sit. Raise it to hang lower. */
  private static final float GRIP_OFFSET = 0.55f;

  /** Rope points closer to the player than this are skipped when working out the direction. */
  private static final float MIN_ROPE_LENGTH = 0.2f;

  private Texture texture;
  private GrappleComponent grapple;
  private AnimationRenderComponent animator;
  private PlayerAnimationController controller;
  private PlayerActions actions;

  // Whether the pose was drawn last frame, so the normal animation can be restored after.
  private boolean wasHolding = false;
  private boolean holding = false;
  private boolean flipped = false;
  private float rotation = 0f;
  private final Vector2 gripPosition = new Vector2();

  @Override
  public void create() {
    super.create();
    grapple = entity.getComponent(GrappleComponent.class);
    animator = entity.getComponent(AnimationRenderComponent.class);
    controller = entity.getComponent(PlayerAnimationController.class);
    actions = entity.getComponent(PlayerActions.class);

    // Skip the pose, rather than crash, if the image is missing.
    if (Gdx.files != null && Gdx.files.internal(TEXTURE_PATH).exists()) {
      texture = new Texture(Gdx.files.internal(TEXTURE_PATH));
      texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }
  }

  @Override
  public void update() {
    holding = updateHolding();
    if (wasHolding && !holding && controller != null) {
      // The pose stopped the normal animation while it drew. It has stopped drawing - the player
      // landed, let go, or the rope fell below them - so bring the normal animation back.
      controller.refreshAnimation();
    }
    wasHolding = holding;
  }

  /**
   * Works out this frame's pose and, when it should show, hides the normal animation under it.
   *
   * @return whether the rope pose draws this frame
   */
  private boolean updateHolding() {
    boolean attached = grapple != null && grapple.isAttached();
    if (!attached || animator == null || texture == null) {
      return false;
    }
    // Only while hanging. A player standing on a platform with a rope out walks normally.
    if (actions != null && !actions.isSwinging()) {
      return false;
    }

    // Let the hurt and death animations show instead of the rope pose.
    // Let a hit, death, shot, swing, dash or landing play out instead of cutting it off.
    if (controller != null && !controller.canShowRopePose()) {
      return false;
    }
    // Also protect hurt and death when no controller is present.
    String current = animator.getCurrentAnimation();
    if ("hurt".equals(current) || "death".equals(current)) {
      return false;
    }

    Vector2 centre = entity.getCenterPosition();
    Vector2 next = nextRopePoint(centre);
    if (next == null) {
      return false;
    }
    Vector2 up = next.cpy().sub(centre).nor();
    if (up.y <= 0f) {
      // The rope runs down to an anchor below or level with the player: it's slack and isn't
      // holding them up, so hanging from it (upside down, with this art) would make no sense.
      return false;
    }

    gripPosition.set(centre).mulAdd(up, GRIP_OFFSET);
    // The art's rope points straight up (90 degrees); turn it to point along the rope instead.
    rotation = up.angleDeg() - 90f;
    flipped = animator.isFlipX();

    animator.stopAnimation();
    return true;
  }

  /**
   * Finds the point along the rope, starting from the player, that sets the direction the rope
   * leaves the player in. The rope can bend around platform corners, so this is the first point
   * that is far enough away, not necessarily the hook.
   *
   * @return the point, or null if the rope has no usable direction
   */
  private Vector2 nextRopePoint(Vector2 centre) {
    List<Vector2> path = grapple.getRopePath();
    if (path == null || path.size() < 2) {
      return null;
    }
    int last = path.size() - 1;
    // The path isn't guaranteed to start at the player, so work out which end they are at.
    boolean startsAtPlayer = path.get(0).dst2(centre) <= path.get(last).dst2(centre);
    float minDistanceSquared = MIN_ROPE_LENGTH * MIN_ROPE_LENGTH;

    for (int k = 1; k <= last; k++) {
      Vector2 point = path.get(startsAtPlayer ? k : last - k);
      if (point.dst2(centre) >= minDistanceSquared) {
        return point;
      }
    }
    return null;
  }

  @Override
  protected void draw(SpriteBatch batch) {
    if (!holding || texture == null) {
      return;
    }
    float size = TEXTURE_SIZE_PX * UNITS_PER_PIXEL;
    float originX = ROPE_AXIS_X_PX * UNITS_PER_PIXEL;
    float originY = (TEXTURE_SIZE_PX - GRIP_ROW_PX) * UNITS_PER_PIXEL;

    // Mirroring happens around the pivot, which sits on the rope, so the rope stays in place.
    batch.draw(
        texture,
        gripPosition.x - originX,
        gripPosition.y - originY,
        originX,
        originY,
        size,
        size,
        flipped ? -1f : 1f,
        1f,
        rotation,
        0,
        0,
        (int) TEXTURE_SIZE_PX,
        (int) TEXTURE_SIZE_PX,
        false,
        false);
  }

  @Override
  public void dispose() {
    if (texture != null) {
      texture.dispose();
      texture = null;
    }
    super.dispose();
  }
}
