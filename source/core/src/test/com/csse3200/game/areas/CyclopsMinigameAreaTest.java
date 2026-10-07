package com.csse3200.game.areas;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.RenderComponent;
import java.util.List;
import org.junit.jupiter.api.Test;

class CyclopsMinigameAreaTest {
  private static final float STATUE_BASE_Y = 90f;
  private static final List<Float> STATUE_X = List.of(100f, 300f, 500f);

  private static class TestRender extends RenderComponent {
    @Override
    protected void draw(SpriteBatch batch) {}
  }

  private static Entity entityAt(Vector2 position) {
    Entity entity = new Entity().addComponent(new TestRender());
    entity.setPosition(position);
    return entity;
  }

  private static float zIndex(Entity entity) {
    return entity.getComponent(TestRender.class).getZIndex();
  }

  private static List<Entity> spawnStatues() {
    return STATUE_X.stream()
        .map(x -> entityAt(new Vector2(x, STATUE_BASE_Y + CyclopsMinigameArea.STATUE_DEPTH_OFFSET)))
        .toList();
  }

  @Test
  void playerDrawsInFrontOfEveryStatueAfterSpawning() {
    List<Entity> statues = spawnStatues();
    Entity player = entityAt(new Vector2(STATUE_X.getFirst(), STATUE_BASE_Y));

    for (Entity statue : statues) {
      assertTrue(zIndex(player) > zIndex(statue));
    }
  }

  @Test
  void playerDrawsInFrontOfEveryStatueAfterMoving() {
    List<Entity> statues = spawnStatues();
    Entity player = entityAt(new Vector2(STATUE_X.getFirst(), STATUE_BASE_Y));

    for (float x : STATUE_X) {
      player.setPosition(new Vector2(x, STATUE_BASE_Y));
      for (Entity statue : statues) {
        assertTrue(zIndex(player) > zIndex(statue));
      }
    }
  }
}
