package com.csse3200.game.components.level;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.DynamicTextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Causes a platform to crumble after the player touches it.
 *
 * <p>The platform starts in a normal state and does nothing until the player makes contact. Once
 * touched, the platform waits for timeBeforeCrumble seconds, then crumbles for crumbleTime seconds.
 * After crumbling, the platform disappears and its collision is disabled. It then waits for
 * respawnTime seconds before becoming active again.
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
  private final float respawnTime;
  private Texture platformTexture;

  private CrumbleState state = CrumbleState.NORMAL;
  private float stateTime = 0f;

  /**
   * Creates a crumbling platform component.
   *
   * @param grappleSides the base 10 integer representing which sides can be grappled to
   * @param timeBeforeCrumble how long to wait after player contact before crumbling, in seconds
   * @param crumbleTime how long the platform spends crumbling, in seconds
   * @param respawnTime how long the platform stays gone before returning, in seconds
   */
  public CrumblingPlatformComponent(
      int grappleSides, float timeBeforeCrumble, float crumbleTime, float respawnTime) {

    super(grappleSides);

    this.timeBeforeCrumble = timeBeforeCrumble;
    this.crumbleTime = crumbleTime;
    this.respawnTime = respawnTime;
  }

  @Override
  public void create() {
    entity.getEvents().addListener("collisionStart", this::onCollisionStart);
  }

  /**
   * Starts the crumble countdown when the player touches the platform.
   *
   * @param me this platform's fixture
   * @param other the fixture that made contact with the platform
   */
  private void onCollisionStart(Fixture me, Fixture other) {
    // Only allow activation while the platform is in its normal state.
    if (state != CrumbleState.NORMAL) {
      return;
    }

    // Ignore anything that is not the player.
    if (!PhysicsLayer.contains(PhysicsLayer.PLAYER, other.getFilterData().categoryBits)) {
      return;
    }

    // Player has touched the platform, so start the countdown.
    state = CrumbleState.WAITING_TO_CRUMBLE;
    stateTime = 0f;
  }

  @Override
  public void update() {
    float deltaTime = ServiceLocator.getTimeSource().getDeltaTime();

    switch (state) {
      case NORMAL:
        // Platform is waiting for the player to touch it.
        break;

      case WAITING_TO_CRUMBLE:
        stateTime += deltaTime;

        if (stateTime >= timeBeforeCrumble) {
          state = CrumbleState.CRUMBLING;
          stateTime = 0f;
        }
        break;

      case CRUMBLING:
        stateTime += deltaTime;

        if (stateTime >= crumbleTime) {
          crumble();
        }
        break;

      case CRUMBLED:
        stateTime += deltaTime;

        if (stateTime >= respawnTime) {
          respawn();
        }
        break;
    }
  }

  /** Makes the platform disappear and disables its collision. */
  private void crumble() {
    state = CrumbleState.CRUMBLED;
    stateTime = 0f;

    // Disable collision so the player can fall through.
    PhysicsComponent physicsComponent = entity.getComponent(PhysicsComponent.class);
    if (physicsComponent != null) {
      physicsComponent.setEnabled(false);
    }

    // Hide the platform without destroying its render component.
    DynamicTextureRenderComponent renderComponent =
        entity.getComponent(DynamicTextureRenderComponent.class);
    platformTexture = renderComponent.getTexture();
    renderComponent.setTexture("images/ui/transparent.png");
  }

  /** Restores the platform after the respawn timer finishes. */
  private void respawn() {
    state = CrumbleState.NORMAL;
    stateTime = 0f;

    // Enable collision again.
    PhysicsComponent physicsComponent = entity.getComponent(PhysicsComponent.class);
    if (physicsComponent != null) {
      physicsComponent.setEnabled(true);
    }

    // Show the platform again.
    DynamicTextureRenderComponent renderComponent =
        entity.getComponent(DynamicTextureRenderComponent.class);
    renderComponent.setTexture(platformTexture);
  }
}
