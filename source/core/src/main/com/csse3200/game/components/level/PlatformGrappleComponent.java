package com.csse3200.game.components.level;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;

public class PlatformGrappleComponent extends Component {
  /// Helper constants provided for more readable code :)
  private static final int LEFT_SIDE = 8;
  private static final int TOP_SIDE = 4;
  private static final int RIGHT_SIDE = 2;
  private static final int BOTTOM_SIDE = 1;

  /**
   * Represents the sides of the platform that can be grappled to through a base 10 integer used to
   * track each side's state through the binary representation of the integer. Therefore, as each
   * platform only has 4 sides, the maximum number this can be set to is 15 (1111) and would allow
   * for all sides to be grappled to. Whenever this is assigned to, it should be capped at 15 to
   * ensure correct functionality. When assigning a value, the above constants can be used. <br>
   *
   * <p>The sides are represented as follows: <br>
   * Most Significant Bit = Left Side of Platform (1000) <br>
   * Second Most Significant Bit = Top of Platform (0100) <br>
   * Second Least Significant Bit = Right Side of Platform (0010) <br>
   * Least Significant Bit = Bottom of Platform (0001) <br>
   *
   * <p>Some common examples: <br>
   * 10 = 1010 = Left + Right Grappleable Sides <br>
   * 5 = 0101 = Top + Bottom Grappleable Sides
   */
  private int grappleSides;

  /**
   * Default constructor for a new platform grapple component
   *
   * @param grappleSides the base 10 integer representing which sides can be grappled to
   */
  public PlatformGrappleComponent(int grappleSides) {
    this.grappleSides = Math.min(grappleSides, 15);
  }

  public int getGrappleSides() {
    return grappleSides;
  }

  /**
   * Allows the grappleable sides to be updated during the game, for example if a button is pressed.
   *
   * @param grappleSides the base 10 integer representing which sides can be grappled to
   */
  public void updateGrappleSides(int grappleSides) {
    this.grappleSides = Math.min(grappleSides, 15);
  }

  /**
   * Checks the raycast end location of the grapple on the platform and converts it into a usable
   * side int that can be compared against the valid grapple sides for this platform
   *
   * @param platform the entity that should be checked against the raycast. Note: should be a known
   *     platform!
   * @param raycastEnd the Vector2 object created by the physics engine's raycast that corresponds
   *     to the final point in the world the grapple hit
   * @return a base 10 integer representing which side of the platform was hit by the player's
   *     grapple. Can only be 1 (BOTTOM_SIDE), 2 (RIGHT_SIDE), 4 (TOP_SIDE) or 8 (LEFT_SIDE).
   */
  public int checkSideHit(Entity platform, Vector2 raycastEnd) {
    // Invalid entity provided, so no grapple should be registered
    if (platform.getComponent(PlatformGrappleComponent.class) == null) {
      return 0;
    }

    // get provided platform bounds
    Vector2[] bounds = calculatePlatformBounds(platform);
    Vector2 min = bounds[0];
    Vector2 max = bounds[1];

    // check against provided raycast end for which side was hit
    float floatLenience = 0.005f; // account for any floating point inaccuracies

    if (min.y <= raycastEnd.y && raycastEnd.y <= max.y) {
      if (Math.abs(min.x - raycastEnd.x) <= floatLenience) {
        return LEFT_SIDE;
      } else if (Math.abs(max.x - raycastEnd.x) <= floatLenience) {
        return RIGHT_SIDE;
      }
    }

    if (min.x <= raycastEnd.x && raycastEnd.x <= max.x) {
      if (Math.abs(min.y - raycastEnd.y) <= floatLenience) {
        return TOP_SIDE;
      } else if (Math.abs(max.y - raycastEnd.y) <= floatLenience) {
        return BOTTOM_SIDE;
      }
    }

    // no valid side was hit
    return 0;
  }

  /**
   * Uses an entity's get center and position methods to determine the bounding box for the entity,
   * assuming it's a square/rectangle. Note: this function does not check if the provided Entity is
   * a valid platform or not. If this is a concern, please ensure the calling method does this check
   *
   * @param platform the platform to determine the bounding box coordinates for
   * @return an array with 2 Vector2 elements: the top left coordinates and the bottom right
   *     coordinates that represent the outer bounds of this platform
   */
  protected Vector2[] calculatePlatformBounds(Entity platform) {
    Vector2 topLeft;
    Vector2 bottomRight;

    topLeft = platform.getPosition();
    Vector2 center = platform.getCenterPosition();

    float diffX = center.x - topLeft.x;
    float diffY = center.y - topLeft.y;
    bottomRight = new Vector2(center.x + diffX, center.y + diffY);

    return new Vector2[] {topLeft, bottomRight};
  }

  /**
   * Tests whether the side of the platform hit with the player's grapple is a valid grapple side
   * for this platform.
   *
   * @param hitSide the base 10 integer representing which side of the platform was hit by the
   *     player's grapple. Can only be 1, 2, 4, or 8
   * @return true if the grapple was successful, or false if grapple was unsuccessful or an invalid
   *     hitSide integer was provided
   */
  public boolean successfulGrapple(int hitSide) {
    // ensure hit side is valid
    if (hitSide != LEFT_SIDE
        && hitSide != TOP_SIDE
        && hitSide != RIGHT_SIDE
        && hitSide != BOTTOM_SIDE) {
      return false;
    }
    // if bit comparison is 0, no bits are shared between hit side and valid grapple sides
    return (hitSide & grappleSides) != 0;
  }
}
