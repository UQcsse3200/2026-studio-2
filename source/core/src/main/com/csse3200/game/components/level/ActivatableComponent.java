package com.csse3200.game.components.level;

import com.csse3200.game.components.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActivatableComponent extends Component {
  private static final Logger logger = LoggerFactory.getLogger(ActivatableComponent.class);

  private final boolean activatable;
  private boolean active;
  private final String[] ids;

  /**
   * Minimal constructor for an activatable constructor
   *
   * @param id the id to keep a reference of and react to activation events with the same id key
   */
  public ActivatableComponent(String[] id) {
    active = checkId(id);
    activatable = !active;
    this.ids = id;
  }

  /**
   * Constructor overload that provides more control over the creation of this component
   *
   * @param active the initial activation state of the entity this is attached to
   * @param ids the id to keep a reference of and react to activation events with the same id key
   */
  public ActivatableComponent(boolean active, String[] ids) {
    boolean emptyId = checkId(ids);

    this.active = active && emptyId;
    activatable = !emptyId;
    this.ids = ids;
  }

  /**
   * A helper method for constructors to use to ensure the provided id is valid
   *
   * @param ids the ids to keep a reference of and react to activation events with the same id key
   * @return true if the id is empty or "", false otherwise
   * @throws IllegalArgumentException if the id is null
   */
  private boolean checkId(String[] ids) {
    if (ids == null) {
      throw new IllegalArgumentException("Invalid id - cannot be null");
    }

    return ids.length == 0;
  }

  /**
   * Fetches the state of this component
   *
   * @return a boolean representing whether this component's parent entity should be active
   */
  public boolean isActive() {
    return active;
  }

  /**
   * Updates the active status of this component. Note: If an empty id was provided, this component
   * is considered unactivatable. If this kind of component receives any attempt to update the
   * active status, a warning will be logged, and the active status will remain as it's current
   * value
   *
   * @param active the new active state to set this component's flag to
   */
  public void setActive(boolean active) {
    // if the component was set up to not have an activation id, it is not activatable, and so
    // any attempts to activate the component should make a warning in the logger
    if (!activatable) {
      logger.warn("A component that cannot be activated received an attempt to activate");
      return;
    }

    this.active = active;
    entity.getEvents().trigger("activatedMapComponent", active);
  }

  /**
   * Fetches the id of this component
   *
   * @return An array of strings representing all IDs this entity should respond to
   */
  public String[] getIds() {
    return ids;
  }
}
