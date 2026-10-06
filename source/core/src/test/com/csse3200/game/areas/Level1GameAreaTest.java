package com.csse3200.game.areas;

import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class Level1GameAreaTest {

  private Level1GameArea gameArea;

  @BeforeEach
  void setUp() {
    gameArea = new Level1GameArea(null, null);
  }

  @Test
  void shouldCreateLevel1GameArea() {
    assertNotNull(gameArea);
  }

  @Test
  void shouldInitiallyHaveNoInput() {
    assertNull(gameArea.getInput());
  }

  @Test
  void shouldHaveCorrectShopkeeperSpawn() {
    assertEquals(new GridPoint2(5, 3), Level1GameArea.SHOPKEEPER_SPAWN);
  }

  @Test
  void shouldHaveCorrectItemSpawns() {
    assertEquals(new GridPoint2(2, 3), Level1GameArea.ROPE_ARROW_SPAWN);
    assertEquals(new GridPoint2(4, 3), Level1GameArea.STANDARD_ARROW_SPAWN);
    assertEquals(new GridPoint2(6, 3), Level1GameArea.FIRE_ARROW_SPAWN);
    assertEquals(new GridPoint2(8, 5), Level1GameArea.ICE_ARROW_SPAWN);
    assertEquals(new GridPoint2(10, 5), Level1GameArea.POISON_ARROW_SPAWN);
    assertEquals(new GridPoint2(12, 5), Level1GameArea.HEALTH_POTION_SPAWN);
  }

  @Test
  void shouldHaveCorrectItemQuantities() {
    assertEquals(5, Level1GameArea.STANDARD_ARROW_QUANTITY);
    assertEquals(5, Level1GameArea.FIRE_ARROW_QUANTITY);
    assertEquals(5, Level1GameArea.ICE_ARROW_QUANTITY);
    assertEquals(5, Level1GameArea.POISON_ARROW_QUANTITY);
    assertEquals(3, Level1GameArea.HEALTH_POTION_QUANTITY);
  }

  @Test
  void shouldHaveCorrectGoldSpawns() {
    assertArrayEquals(
        new GridPoint2[] {
          new GridPoint2(9, 5),
          new GridPoint2(15, 7),
          new GridPoint2(20, 8)
        },
        Level1GameArea.GOLD_SPAWNS);
  }

  @Test
  void shouldHaveCorrectWheelTokenSpawn() {
    assertEquals(new GridPoint2(32, 3), Level1GameArea.WHEEL_TOKEN_SPAWN);
  }
}