package com.csse3200.game.components.level;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Causes a platform to crumble after the player stands on it. Once activated by player contact, the
 * platform waits for {@code timeBeforeCrumble} seconds, then spends {@code crumbleTime} seconds
 * crumbling before losing its collision and disappearing, allowing the player to fall through it.
 */
public class CrumblingPlatformComponent extends PlatformGrappleComponent {
  private enum CrumbleState {
    NORMAL,
    WAITING_TO_CRUMBLE,
    CRUMBLING,
    CRUMBLED
  }

  private final float timeBeforeCrumble;
  private final float crumbleTime;
  private CrumbleState state = CrumbleState.NORMAL;
  private float stateTime;

  /**
   * @param grappleSides the base 10 integer representing which sides can be grappled to
   * @param timeBeforeCrumble how long the player can stand on the platform before it starts to
   *     crumble, in seconds
   * @param crumbleTime how long the crumbling animation takes before the platform is destroyed, in
   *     seconds
   */
  public CrumblingPlatformComponent(int grappleSides, float timeBeforeCrumble, float crumbleTime) {
    super(grappleSides);
    this.timeBeforeCrumble = timeBeforeCrumble;
    this.crumbleTime = crumbleTime;
  }

  @Override
  public void create() {
    entity.getEvents().addListener("collisionStart", this::onCollisionStart);
  }

  /**
   * Starts the crumble countdown the first time the player makes contact with the platform.
   *
   * @param me this platform's fixture
   * @param other the fixture that made contact with the platform
   */
  private void onCollisionStart(Fixture me, Fixture other) {
    if (state != CrumbleState.NORMAL) {
      return;
    }

    if (!PhysicsLayer.contains(PhysicsLayer.PLAYER, other.getFilterData().categoryBits)) {
      return;
    }

    state = CrumbleState.WAITING_TO_CRUMBLE;
    stateTime = 0f;
  }

  @Override
  public void update() {
    if (state == CrumbleState.NORMAL || state == CrumbleState.CRUMBLED) {
      return;
    }

    stateTime += ServiceLocator.getTimeSource().getDeltaTime();

    if (state == CrumbleState.WAITING_TO_CRUMBLE && stateTime >= timeBeforeCrumble) {
      state = CrumbleState.CRUMBLING;
      stateTime = 0f;
    } else if (state == CrumbleState.CRUMBLING && stateTime >= crumbleTime) {
      crumble();
    }
  }

  /**
   * Finalises the crumble: disables the platform's physics so the player can fall through it, and
   * removes its texture so it disappears.
   */
  private void crumble() {
    state = CrumbleState.CRUMBLED;

    PhysicsComponent physicsComponent = entity.getComponent(PhysicsComponent.class);
    if (physicsComponent != null) {
      physicsComponent.setEnabled(false);
    }

    TextureRenderComponent renderComponent = entity.getComponent(TextureRenderComponent.class);
    if (renderComponent != null) {
      renderComponent.dispose();
    }
  }
}
