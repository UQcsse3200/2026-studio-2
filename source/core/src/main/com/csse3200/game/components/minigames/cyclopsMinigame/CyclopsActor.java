package com.csse3200.game.components.minigames.cyclopsMinigame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Purely visual cyclops sprite: idle bob, slight brandishing sway, and an occasional blink. Has no
 * game/timing logic of its own - safe to drop in alongside whoever is building the timing-bar
 * mechanic.
 */
public class CyclopsActor extends Actor {
  private static final float BOB_PERIOD = 2.4f; // seconds per bob cycle
  private static final float BOB_AMPLITUDE = 6f; // pixels
  private static final float SWAY_PERIOD = 3.6f; // seconds per sway cycle
  private static final float SWAY_MAX_DEGREES = 5f;

  private static final float BLINK_CYCLE = 5.5f; // seconds between blinks
  private static final float BLINK_DURATION = 0.18f; // how long the eyes stay shut

  private final TextureRegion openEyes;
  private final TextureRegion closedEyes;

  private float elapsed = 0f;
  private float baseY = Float.NaN;

  public CyclopsActor(Texture openEyesTexture, Texture closedEyesTexture) {
    this.openEyes = new TextureRegion(openEyesTexture);
    this.closedEyes = new TextureRegion(closedEyesTexture);
    setSize(openEyesTexture.getWidth(), openEyesTexture.getHeight());
    // origin at the feet (bottom-centre) so the sway/rotation doesn't slide the feet
    setOrigin(getWidth() / 2f, 0f);
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    elapsed += delta;
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    if (Float.isNaN(baseY)) {
      baseY = getY(); // capture the Y it was placed at, once, on first draw
    }

    float bobOffset =
        BOB_AMPLITUDE * (MathUtils.sin(elapsed * MathUtils.PI2 / BOB_PERIOD) + 1f) / 2f;
    float sway = SWAY_MAX_DEGREES * MathUtils.sin(elapsed * MathUtils.PI2 / SWAY_PERIOD);

    setY(baseY + bobOffset);
    setRotation(sway);

    float phaseInCycle = elapsed % BLINK_CYCLE;
    TextureRegion frame = (phaseInCycle < BLINK_DURATION) ? closedEyes : openEyes;

    batch.setColor(1f, 1f, 1f, parentAlpha);
    batch.draw(
        frame,
        getX(),
        getY(),
        getOriginX(),
        getOriginY(),
        getWidth(),
        getHeight(),
        getScaleX(),
        getScaleY(),
        getRotation());
  }
}
