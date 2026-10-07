package com.csse3200.game.physics;

public class PhysicsLayer {
  public static final short NONE = 0;
  public static final short DEFAULT = (1 << 0);
  public static final short PLAYER = (1 << 1);
  // Terrain obstacle, e.g. trees
  public static final short OBSTACLE = (1 << 2);
  // NPC (Non-Playable Character) colliders
  public static final short NPC = (1 << 3);
  // Ground and platforms used by side-view player movement
  public static final short GROUND = (1 << 4);
  // Solids used for grappling + groundedness
  public static final short SOLID = GROUND | OBSTACLE;
  // Projectiles fired by the player
  public static final short PLAYER_PROJECTILE = (1 << 5);
  // Invisible level-boundary walls: keep characters in, let projectiles pass
  public static final short WALL = (1 << 6);
  public static final short ALL = ~0;

  public static final short ENEMY_PROJECTILE = (1 << 7);
  // The solid body of a character, as opposed to the NPC sensor used for damage. Separate so the
  // player can stand on an enemy without arrows treating that body as a second thing to hit.
  public static final short CHARACTER = (1 << 8);
  // Everything the player can land on. Enemies count: standing on one is standing on ground.
  public static final short STANDABLE = SOLID | CHARACTER;

  public static boolean contains(short filterBits, short layer) {
    return (filterBits & layer) != 0;
  }

  private PhysicsLayer() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
