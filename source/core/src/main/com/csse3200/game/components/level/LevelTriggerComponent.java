package com.csse3200.game.components.level;

/**
 * Sends the player to the next level. They have to stand in the entity's sensor and press F, see
 * {@link LevelEndComponent}.
 */
public class LevelTriggerComponent extends LevelEndComponent {
  String levelName;

  /**
   * @param name the name of the level to swap to when the component is triggered
   */
  public LevelTriggerComponent(String name) {
    super();
    this.levelName = name;
  }

  @Override
  protected void activate() {
    entity.getEvents().trigger("triggerNextLevel", levelName);
  }
}
