package com.csse3200.game.components.minigames.cyclopsMinigame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
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

  @AfterEach
  void afterEach() {
    ServiceLocator.clear();
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

  private float shakenY(float seconds) {
    GameTime gameTime = mock(GameTime.class);
    when(gameTime.getDeltaTime()).thenReturn(seconds);
    ServiceLocator.registerTimeSource(gameTime);
    target.setPosition(new Vector2(25f, 0f));
    follow.update();
    return holder.getPosition().y;
  }

  @Test
  void shakesUpAndDownWhenTold() {
    follow.shake();
    float y = shakenY(0.01f);

    assertNotEquals(CAMERA_Y, y);
    assertTrue(Math.abs(y - CAMERA_Y) <= CyclopsCameraFollowComponent.SHAKE_STRENGTH);
  }

  @Test
  void settlesBackOnceTheShakeIsOver() {
    follow.shake();
    shakenY(CyclopsCameraFollowComponent.SHAKE_DURATION);

    assertEquals(CAMERA_Y, shakenY(0.01f), 0.0001f);
  }
}
