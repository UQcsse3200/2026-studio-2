package com.csse3200.game.components.level;

import com.csse3200.game.components.Component;

public class PlatformGrappleComponent extends Component {
  /**
   * Represents the sides of the platform that can be grappled to through a base 10 integer used to
   * track each side's state through the binary representation of the integer. Therefore, as each
   * platform only has 4 sides, the maximum number this can be set to is 15 (1111) and would allow
   * for all sides to be grappled to. Whenever this is assigned to, it should be capped at 15 to
   * ensure correct functionality <br>
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

  /**
   * Allows the grappleable sides to be updated during the game, for example if a button is pressed.
   *
   * @param grappleSides the base 10 integer representing which sides can be grappled to
   */
  public void updateGrappleSides(int grappleSides) {
    this.grappleSides = Math.min(grappleSides, 15);
  }

  /**
   * <b>Stub - Awaiting grapple implementation from player team</b><br>
   * Checks the hit location of the grapple on the platform and converts it into a usable side int
   * that can be compared against the valid grapple sides for this platform
   *
   * @return a base 10 integer representing which side of the platform was hit by the player's
   *     grapple. Can only be 1, 2, 4 or 8.
   */
  public int checkSideHit() {
    return 0;
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
    if (hitSide != 1 && hitSide != 2 && hitSide != 4 && hitSide != 8) {
      return false;
    }
    // if bit comparison is 0, no bits are shared between hit side and valid grapple sides
    return (hitSide & grappleSides) != 0;
  }
}
