package com.csse3200.game.components.tasks;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class EdgeDetectTaskTest {
  private TaskTestWorld world;
  private Entity enemy;
  private EdgeDetectTask detector;

  @BeforeEach
  void setUp() {
    world = new TaskTestWorld();
    enemy = world.enemyAt(0f, 0f); // a one unit square standing on y = 0
    detector = new EdgeDetectTask(0.3f);
  }

  @Test
  void shouldFindGroundAheadWhenFloorContinuesOnTheRight() {
    world.addGround(-5f, 5f);

    assertTrue(detector.isGroundAhead(enemy, 1f));
  }

  @Test
  void shouldFindGroundAheadWhenFloorContinuesOnTheLeft() {
    world.addGround(-5f, 5f);

    assertTrue(detector.isGroundAhead(enemy, -1f));
  }

  @Test
  void shouldNotFindGroundWhenThereIsNone() {
    assertFalse(detector.isGroundAhead(enemy, 1f));
    assertFalse(detector.isGroundAhead(enemy, -1f));
  }

  @Test
  void shouldLookOnlyInTheDirectionOfTravel() {
    // The floor ends at x = 1: it is under the enemy and just behind, but not ahead on the right
    world.addGround(-5f, 1f);

    assertTrue(detector.isGroundAhead(enemy, -1f), "floor continues to the left");
    assertFalse(detector.isGroundAhead(enemy, 1f), "but drops away to the right");
  }

  @Test
  void shouldLookBeyondTheEnemysWidthWhenGoingRight() {
    // A larger enemy looks from its far edge, so a gap just past its width is spotted
    enemy.setScale(2f, 2f);
    world.addGround(-5f, 2.2f); // ends before the probe at x + width + 0.3 = 2.3

    assertFalse(detector.isGroundAhead(enemy, 1f));
    world.addGround(2.2f, 5f);
    assertTrue(detector.isGroundAhead(enemy, 1f));
  }

  @Test
  void shouldCountObstaclesAsGroundToo() {
    world.addBlock(PhysicsLayer.OBSTACLE, 1.5f, -1f, 1f, 1f);

    assertTrue(detector.isGroundAhead(enemy, 1f));
  }

  @Test
  void shouldIgnoreThingsThatAreNotGroundOrObstacles() {
    world.addBlock(PhysicsLayer.NPC, 1.5f, -1f, 1f, 1f);

    assertFalse(detector.isGroundAhead(enemy, 1f));
  }

  @Test
  void shouldNotSeeGroundMoreThanAShortDropBelow() {
    // The probe starts half a unit above the enemy's feet and reaches two units down
    world.addBlock(PhysicsLayer.GROUND, 1.5f, -6f, 1f, 1f); // top surface at y = -5

    assertFalse(detector.isGroundAhead(enemy, 1f));
  }

  @Test
  void shouldCountLedgesAsGroundWhenNoGroundOrObstacleIsPresent() {
    world.addBlock(PhysicsLayer.LEDGE, 1.5f, -1f, 1f, 1f);

    assertTrue(detector.isGroundAhead(enemy, 1f));
  }
}
