package com.csse3200.game.areas.terrain.configs.levelconfigs;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.configs.CrumblingPlatformConfig;
import com.csse3200.game.areas.terrain.configs.LevelConfig;
import com.csse3200.game.areas.terrain.configs.PlatformConfig;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ObstacleFactory;

public class Level2Config extends LevelConfig {

  public Level2Config() {

    // Player starting position
    playerSpawn = new GridPoint2(2, 5);

    // Temporary win condition
    winConditionSpawn = new GridPoint2(80, 18);

    // =========================
    // LEVEL 2 PLATFORMS
    // =========================

    platforms =
        new PlatformConfig[] {

          // P1 - starting platform
          new PlatformConfig(
              new GridPoint2(2, 4),
              3,
              1,
              0),

          // P2
          new PlatformConfig(
              new GridPoint2(10, 7),
              3,
              1,
              0),

          // P3
          new PlatformConfig(
              new GridPoint2(18, 9),
              3,
              1,
              0)
        };

    // =========================
    // LEVEL 2 CRUMBLING PLATFORMS
    // =========================

    crumblingPlatforms =
        new CrumblingPlatformConfig[] {

          // TEST C1 - TEMPORARY: kept at the SAME height as the spawn platform (P1) with only a
          // small horizontal gap, since Level 2's gravity is strong (-50 m/s^2 in
          // PhysicsEngine.GRAVITY) and this game's big gaps (e.g. P1 -> P2) are meant to be
          // crossed with the grapple, not a plain jump. A same-height gap needs no vertical lift,
          // just walking speed carried into a short hop. Short timings (2s / 2s) so the crumbling
          // platform chain can be manually verified in-game.
          // Revert to GridPoint2(14, 8), 1.5f, 0.5f once testing is complete.
          new CrumblingPlatformConfig(
              new GridPoint2(6, 4),
              3,
              1,
              0,
              2f,
              2f)
        };

    // =========================
    // TEMPORARY GROUND
    // =========================

    floors =
        new PlatformConfig[] {

          // Ground_level-2.png is 2172x724px (an exact 3:1 aspect ratio). Width/height below
          // keep that same 3:1 ratio so the image isn't stretched, and the y position is
          // shifted down so the walkable top surface still sits at y=1, same as before.
          new PlatformConfig(
              new GridPoint2(0, -12),
              39,
              13,
              0)
        };
  }

  @Override
  protected Entity createPlatformEntity(int grappleSides) {
    return ObstacleFactory.createLevel2Platform(grappleSides);
  }

  @Override
  protected Entity createMovingPlatformEntity(
      int grappleSides,
      Vector2 firstTarget,
      Vector2 secondTarget,
      Vector2 maxSpeed,
      String activateId) {
    return ObstacleFactory.createLevel2MovingPlatform(
        grappleSides, firstTarget, secondTarget, maxSpeed, activateId);
  }

  @Override
  protected Entity createTriggerablePlatformEntity(int grappleSides) {
    return ObstacleFactory.createLevel2TriggerablePlatform(grappleSides);
  }

  @Override
  protected Entity createCrumblingPlatformEntity(
      int grappleSides, float timeBeforeCrumble, float crumbleTime) {
    return ObstacleFactory.createLevel2CrumblingPlatform(
        grappleSides, timeBeforeCrumble, crumbleTime);
  }

  @Override
  protected Entity createFloorEntity(int grappleSides) {
    return ObstacleFactory.createLevel2Floor(grappleSides);
  }
}