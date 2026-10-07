package com.csse3200.game.components.minigames.cyclopsMinigame;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.Test;

class CyclopsCameraFollowComponentTest {
  private static final float ROOM_WIDTH = 40f;
  private static final float CAMERA_Y = 7.5f;
  private static final float VIEW_WIDTH = 20f;

  private final Entity target = new Entity();
  private final OrthographicCamera camera = new OrthographicCamera();
  private final Entity holder = new Entity();
  private final CyclopsCameraFollowComponent follow;

  CyclopsCameraFollowComponentTest() {
    camera.viewportWidth = VIEW_WIDTH;
    target.setScale(new Vector2(0f, 0f));
    follow = new CyclopsCameraFollowComponent(target, camera, ROOM_WIDTH, CAMERA_Y);
    holder.addComponent(follow);
  }

  private float followedX(float targetCenterX) {
    target.setPosition(new Vector2(targetCenterX, 0f));
    follow.update();
    return holder.getPosition().x;
  }

  @Test
  void followsTargetInsideRoom() {
    assertEquals(20f, followedX(20f), 0.0001f);
  }

  @Test
  void clampsAtRoomStart() {
    assertEquals(VIEW_WIDTH / 2, followedX(0f), 0.0001f);
  }

  @Test
  void clampsAtRoomEnd() {
    assertEquals(ROOM_WIDTH - VIEW_WIDTH / 2, followedX(ROOM_WIDTH), 0.0001f);
  }

  @Test
  void keepsVerticalPositionFixed() {
    target.setPosition(new Vector2(25f, 1f));
    follow.update();
    assertEquals(CAMERA_Y, holder.getPosition().y, 0.0001f);
  }
}
