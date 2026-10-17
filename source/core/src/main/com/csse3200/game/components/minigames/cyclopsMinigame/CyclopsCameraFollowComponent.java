package com.csse3200.game.components.minigames.cyclopsMinigame;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

/** Moves its entity to follow a target's x, keeping the view inside the room and at a fixed y. */
public class CyclopsCameraFollowComponent extends Component {
  static final float SHAKE_DURATION = 0.4f;
  static final float SHAKE_STRENGTH = 0.25f;

  private final Entity target;
  private final Camera camera;
  private final float roomWidth;
  private final float y;
  private float shakeTime;

  public CyclopsCameraFollowComponent(Entity target, Camera camera, float roomWidth, float y) {
    this.target = target;
    this.camera = camera;
    this.roomWidth = roomWidth;
    this.y = y;
  }

  @Override
  public void update() {
    float halfView = camera.viewportWidth / 2f;
    float x = MathUtils.clamp(target.getCenterPosition().x, halfView, roomWidth - halfView);
    entity.setPosition(new Vector2(x, y + shakeOffset()));
  }

  public void shake() {
    shakeTime = SHAKE_DURATION;
  }

  private float shakeOffset() {
    if (shakeTime <= 0f) {
      return 0f;
    }
    shakeTime = Math.max(shakeTime - ServiceLocator.getTimeSource().getDeltaTime(), 0f);
    float strength = SHAKE_STRENGTH * shakeTime / SHAKE_DURATION;
    return MathUtils.random(-strength, strength);
  }
}
