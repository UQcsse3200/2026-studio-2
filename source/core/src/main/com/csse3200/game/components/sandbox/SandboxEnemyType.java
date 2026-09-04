package com.csse3200.game.components.sandbox;

/** Enemy types currently available from the Sandbox monster spawner. */
public enum SandboxEnemyType {
  SKELETON_WARRIOR("Skeleton Warrior", "images/skeleton_warrior.png"),
  SKELETON_ARCHER("Skeleton Archer", "images/skeleton_archer.png");

  private final String displayName;
  private final String texturePath;

  SandboxEnemyType(String displayName, String texturePath) {
    this.displayName = displayName;
    this.texturePath = texturePath;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getTexturePath() {
    return texturePath;
  }
}
