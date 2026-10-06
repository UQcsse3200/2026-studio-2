package com.csse3200.game.components.minigames.cyclopsMinigame;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;

/** Moves its entity to follow a target's x, keeping the view inside the room and at a fixed y. */
public class CyclopsCameraFollowComponent extends Component {
  private final Entity target;
  private final Camera camera;
  private final float roomWidth;
  private final float y;

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
    entity.setPosition(new Vector2(x, y));
  }
}
