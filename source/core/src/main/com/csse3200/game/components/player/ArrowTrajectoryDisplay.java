package com.csse3200.game.components.player;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.item.weapons.bow.BowComponent;
import com.csse3200.game.components.projectile.ArrowProjectileComponent;
import com.csse3200.game.components.projectile.ArrowTrajectory;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

/**
 * Shows a dotted preview of where the arrow will fly while the player is drawing the bow. The path
 * uses the same speed, charge and gravity as the real arrow, and stops at the first terrain it
 * would hit.
 */
public class ArrowTrajectoryDisplay extends UIComponent {
  private static final int DOT_COUNT = 18;
  private static final float DOT_STEP_SECONDS = 0.06f;
  private static final float DOT_SIZE = 9f;
  private static final float MAX_ALPHA = 0.9f;
  private static final int DOT_TEXTURE_SIZE = 16;

  private final Vector2[] points = new Vector2[DOT_COUNT];
  private final Image[] dots = new Image[DOT_COUNT];
  private final Vector2 previous = new Vector2();
  private final Vector3 screen = new Vector3();
  private final RaycastHit hit = new RaycastHit();

  private Texture dotTexture;
  private BowComponent bow;
  private KeyboardPlayerInputComponent input;
  private boolean dead;

  @Override
  public void create() {
    super.create();
    bow = entity.getComponent(BowComponent.class);
    input = entity.getComponent(KeyboardPlayerInputComponent.class);
    entity.getEvents().addListener("death", () -> dead = true);

    dotTexture = createDotTexture();
    for (int i = 0; i < DOT_COUNT; i++) {
      points[i] = new Vector2();
      dots[i] = new Image(dotTexture);
      dots[i].setSize(DOT_SIZE, DOT_SIZE);
      dots[i].setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
      dots[i].setVisible(false);
      stage.addActor(dots[i]);
    }
  }

  @Override
  public void draw(SpriteBatch batch) {
    Vector2 aim = getAim();
    CameraComponent camera = input == null ? null : input.getCameraComponent();
    if (aim == null || camera == null) {
      hideDots(0);
      return;
    }

    Vector2 velocity = aim.cpy().nor().scl(bow.getLaunchSpeed());
    Vector2 start = bow.getSpawnPosition(aim);
    ArrowTrajectory.compute(start, velocity, bow.getArrowGravityY(), DOT_STEP_SECONDS, points);

    int visible = countUntilTerrain(start);
    for (int i = 0; i < visible; i++) {
      screen.set(points[i].x, points[i].y, 0f);
      camera.getCamera().project(screen);
      Image dot = dots[i];
      dot.setPosition(screen.x - DOT_SIZE / 2f, screen.y - DOT_SIZE / 2f);
      float fade = 1f - (float) i / DOT_COUNT;
      dot.setColor(1f, 1f, 1f, MAX_ALPHA * fade);
      dot.setVisible(true);
    }
    hideDots(visible);
  }

  /** Returns the aim to preview, or null when no preview should be shown. */
  private Vector2 getAim() {
    if (dead || bow == null || input == null || !bow.isCharging() || !input.isRightMouseHeld()) {
      return null;
    }
    Vector2 aim = input.getMouseAimDirection();
    return aim == null || aim.isZero() ? null : aim;
  }

  /** Returns how many leading points are reached before the arrow would hit terrain. */
  private int countUntilTerrain(Vector2 start) {
    if (ServiceLocator.getPhysicsService() == null) {
      return DOT_COUNT;
    }
    previous.set(start);
    for (int i = 0; i < DOT_COUNT; i++) {
      boolean blocked =
          ServiceLocator.getPhysicsService()
              .getPhysics()
              .raycast(previous, points[i], ArrowProjectileComponent.TERRAIN, hit);
      if (blocked) {
        return i;
      }
      previous.set(points[i]);
    }
    return DOT_COUNT;
  }

  private void hideDots(int from) {
    for (int i = from; i < DOT_COUNT; i++) {
      dots[i].setVisible(false);
    }
  }

  private static Texture createDotTexture() {
    Pixmap pixmap = new Pixmap(DOT_TEXTURE_SIZE, DOT_TEXTURE_SIZE, Pixmap.Format.RGBA8888);
    pixmap.setColor(Color.WHITE);
    pixmap.fillCircle(DOT_TEXTURE_SIZE / 2, DOT_TEXTURE_SIZE / 2, DOT_TEXTURE_SIZE / 2 - 1);
    Texture texture = new Texture(pixmap);
    pixmap.dispose();
    return texture;
  }

  @Override
  public void dispose() {
    super.dispose();
    for (Image dot : dots) {
      if (dot != null) {
        dot.remove();
      }
    }
    if (dotTexture != null) {
      dotTexture.dispose();
    }
  }
}
