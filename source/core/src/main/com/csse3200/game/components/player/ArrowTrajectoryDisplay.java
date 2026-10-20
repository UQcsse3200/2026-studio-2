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
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

/**
 * Shows a dotted preview of where a shot will fly: the arrow while the player is drawing the bow,
 * or the poison potion while they hold right click to throw it. The path uses the same speed,
 * charge and gravity as the real projectile, and stops at the first terrain it would hit.
 */
public class ArrowTrajectoryDisplay extends UIComponent {
  private static final int DOT_COUNT = 18;
  private static final float DOT_STEP_SECONDS = 0.06f;
  private static final float DOT_SIZE = 4.5f;
  // Opacity of the nearest dot; the rest fade out further along the arc.
  private static final float MAX_ALPHA = 0.75f;
  private static final int DOT_TEXTURE_SIZE = 16;

  private final Vector2[] points = new Vector2[DOT_COUNT];
  private final Image[] dots = new Image[DOT_COUNT];
  private final Vector2 previous = new Vector2();
  private final Vector3 screen = new Vector3();
  private final RaycastHit hit = new RaycastHit();

  private Texture dotTexture;
  private BowComponent bow;
  private ItemUseComponent itemUse;
  private KeyboardPlayerInputComponent input;
  private boolean dead;

  @Override
  public void create() {
    super.create();
    bow = entity.getComponent(BowComponent.class);
    itemUse = entity.getComponent(ItemUseComponent.class);
    input = entity.getComponent(KeyboardPlayerInputComponent.class);
    entity.getEvents().addListener("death", () -> dead = true);
    // A checkpoint restart brings the player back; without this the arc stayed off for good.
    entity.getEvents().addListener("revive", () -> dead = false);

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

    Vector2 direction = aim.cpy().nor();
    Vector2 velocity;
    Vector2 start;
    if (isDrawingBow()) {
      velocity = direction.cpy().scl(bow.getLaunchSpeed());
      start = bow.getSpawnPosition(aim);
    } else {
      // Same launch as ItemUseComponent gives a thrown potion.
      start =
          entity
              .getCenterPosition()
              .mulAdd(direction, entity.getScale().x * ItemUseComponent.POTION_SPAWN_OFFSET);
      velocity = direction.cpy().scl(ProjectileFactory.POISON_POTION_SPEED);
    }
    float gravityY = PhysicsEngine.GRAVITY_Y * ArrowProjectileComponent.ARC_GRAVITY_SCALE;
    ArrowTrajectory.compute(start, velocity, gravityY, DOT_STEP_SECONDS, points);

    // The aim runs from the player's centre to the crosshair, so stop the line once it gets there.
    int visible =
        Math.min(
            countUntilTerrain(start), countWithinReach(entity.getCenterPosition(), aim, points));
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
    if (dead
        || input == null
        || !input.isRightMouseHeld()
        || !(isDrawingBow() || isAimingPotion())) {
      return null;
    }
    Vector2 aim = input.getMouseAimDirection();
    return aim == null || aim.isZero() ? null : aim;
  }

  private boolean isDrawingBow() {
    return bow != null && bow.isCharging();
  }

  private boolean isAimingPotion() {
    return itemUse != null && itemUse.isAimingPotion();
  }

  /**
   * Counts the leading points that are no further along the aim than the crosshair, so the line
   * ends at the crosshair instead of running past it.
   *
   * @param origin where the aim is measured from (the player's centre)
   * @param aim vector from {@code origin} to the crosshair
   * @param points the predicted path
   * @return how many points to show
   */
  static int countWithinReach(Vector2 origin, Vector2 aim, Vector2[] points) {
    float reach = aim.len();
    Vector2 direction = aim.cpy().nor();
    for (int i = 0; i < points.length; i++) {
      float along = (points[i].x - origin.x) * direction.x + (points[i].y - origin.y) * direction.y;
      if (along > reach) {
        return i;
      }
    }
    return points.length;
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
