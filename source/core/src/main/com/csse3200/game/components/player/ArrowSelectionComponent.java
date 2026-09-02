package com.csse3200.game.components.player;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;

/** Tracks which arrow type the player has equipped and fires the right one. */
public class ArrowSelectionComponent extends Component {

  /** The kinds of arrow the player can switch between. */
  public enum ArrowType {
    STANDARD("Arrow"),
    GRAPPLE("Grapple");

    private final String label;

    ArrowType(String label) {
      this.label = label;
    }

    public String getLabel() {
      return label;
    }
  }

  private ArrowType selected = ArrowType.STANDARD;

  @Override
  public void create() {
    entity.getEvents().addListener("cycleArrow", this::cycle);
    entity.getEvents().addListener("shoot", this::shoot);
    entity.getEvents().addListener("stopShoot", this::stopShoot);
  }

  /** Switches to the next arrow type. */
  void cycle() {
    ArrowType[] all = ArrowType.values();
    selected = all[(selected.ordinal() + 1) % all.length];
    entity.getEvents().trigger("arrowChanged", selected);
  }

  /**
   * Starts firing whichever arrow is currently selected. The grapple fires immediately; the
   * standard bow instead starts charging up and only actually fires on release (see {@link
   * #stopShoot}).
   */
  void shoot(Vector2 direction) {
    if (selected == ArrowType.GRAPPLE) {
      entity.getEvents().trigger("grappleFire", direction);
    } else {
      entity.getEvents().trigger("attackChargeStart");
    }
  }

  /**
   * Handles the fire button being let go. The grapple releases the rope; the standard bow fires
   * toward the given (release-time) direction.
   *
   * @param direction release-time aim direction, or null if it couldn't be resolved (e.g. no camera
   *     set)
   */
  void stopShoot(Vector2 direction) {
    if (selected == ArrowType.GRAPPLE) {
      entity.getEvents().trigger("grappleRelease");
    } else if (direction != null && !direction.isZero()) {
      entity.getEvents().trigger("primaryAttack", direction);
    }
  }

  /** will be used for arrow variety */
  public ArrowType getSelected() {
    return selected;
  }
}
