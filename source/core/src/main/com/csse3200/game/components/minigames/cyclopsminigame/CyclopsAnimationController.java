package com.csse3200.game.components.minigames.cyclopsminigame;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/** Plays the cyclops's animations and moves him when the player is caught. */
public class CyclopsAnimationController extends Component {
  public static final float ATTACK_FRAME_TIME = 0.08f;

  static final int IMPACT_FRAME = 8;

  static final float CLUB_REACH = 78f / 180f;

  static final float JUMP_DURATION = 0.7f;

  static final float JUMP_HEIGHT = 3f;

  static final String WOKE_UP_EVENT = "wokeUp";
  public static final String LANDED_EVENT = "landed";
  static final String CLUB_LANDED_EVENT = "clubLanded";
  static final String FELL_ASLEEP_EVENT = "fellAsleep";

  enum State {
    ASLEEP,
    CHARGING,
    JUMPING,
    ATTACKING,
    AWAKE,
    RETURNING
  }

  private final Entity target;
  private final float groundY;
  private AnimationRenderComponent animator;
  private Vector2 home;
  private Vector2 jumpStart;
  private Vector2 destination;
  private float jumpTime;
  private State state = State.ASLEEP;
  private float attackTime;
  private boolean hitLanded;

  public CyclopsAnimationController(Entity target, float groundY) {
    this(target, groundY, null);
  }

  /**
   * Creates a controller that drives the given animator. Use this when the animator is a subclass
   * (such as one with its own draw depth), since components are looked up by their exact class.
   *
   * @param target the entity he attacks
   * @param groundY the floor height he lands at
   * @param animator his animator, or null to look up the entity's AnimationRenderComponent
   */
  public CyclopsAnimationController(
      Entity target, float groundY, AnimationRenderComponent animator) {
    this.target = target;
    this.groundY = groundY;
    this.animator = animator;
  }

  @Override
  public void create() {
    super.create();
    if (animator == null) {
      animator = this.entity.getComponent(AnimationRenderComponent.class);
    }
    home = entity.getPosition();
    animator.startAnimation("sleep");

    EventHandler minigameEvents = ServiceLocator.getCyclopsMinigameEventHandler();
    minigameEvents.addListener(CyclopsMinigameLogic.CYCLOPS_WAKE_EVENT, this::chargeAtTarget);
    minigameEvents.addListener(CyclopsMinigameLogic.CYCLOPS_SLEEP_EVENT, this::returnHome);
  }

  @Override
  public void update() {
    float delta = ServiceLocator.getTimeSource().getDeltaTime();
    switch (state) {
      case CHARGING -> windUp();
      case JUMPING, RETURNING -> jump(delta);
      case ATTACKING -> swing(delta);
      default -> {
        // Other states leave the current animation unchanged.
      }
    }
  }

  State getState() {
    return state;
  }

  /**
   * Returns the animation his animator is playing. Tests use it because the animator is a subclass
   * and so can't be found on the entity by its usual class.
   *
   * @return the name of the current animation
   */
  public String getCurrentAnimation() {
    return animator.getCurrentAnimation();
  }

  /**
   * Draw depth for his renderer. Awake, he keeps the floor's depth for the whole jump, attack and
   * return, so he never drops behind a rock mid-air and pops in front of it on landing. Asleep, he
   * sorts by position as usual, which keeps him behind the rocks on his perch. The switch only
   * happens at home, where he doesn't overlap any rock.
   *
   * @return the z index to draw the cyclops at
   */
  public float getZIndex() {
    return state == State.ASLEEP ? -entity.getPosition().y : -groundY;
  }

  /** Starts the wind up animation and moves to next animation stage after finishing */
  private void windUp() {
    if (animator.isFinished()) {
      jumpTo(attackPosition(), State.JUMPING);
    }
  }

  /**
   * Jump / Charge to the target Entity.
   *
   * <p>Changing the facing location of the animated entity depending on location of the target
   * entity
   */
  private void chargeAtTarget() {
    face(target.getCenterPosition().x);
    animator.startAnimation("charge_start");
    state = State.CHARGING;
    entity.getEvents().trigger(WOKE_UP_EVENT);
  }

  /** Return the controlled entity to their idle position location on screen. */
  private void returnHome() {
    if (state != State.ASLEEP) {
      jumpTo(home, State.RETURNING);
    }
  }

  /**
   * Calculate the attack position to swing the club at the target entity
   *
   * @return a Vector2 of the position to finish the club swing
   */
  private Vector2 attackPosition() {
    float width = entity.getScale().x;
    float playerX = target.getCenterPosition().x;
    float reach = CLUB_REACH * width;
    float bodyX = bodyX() > playerX ? playerX + reach : playerX - reach;
    return new Vector2(bodyX - width / 2f, groundY);
  }

  /**
   * Move the controlled animated entity to the given position and change the state to the next
   * state
   *
   * @param position - The Vector2 position to move the controlled entity to
   * @param next - The next State to enter after starting this animation
   */
  private void jumpTo(Vector2 position, State next) {
    jumpStart = entity.getPosition();
    destination = position;
    jumpTime = 0f;
    face(position.x + entity.getScale().x / 2f);
    animator.startAnimation("jump");
    state = next;
  }

  /**
   * Move the controlled entity's position while jumping to a target location. After landing start
   * the swinging animation or return to idle base position
   *
   * @param delta - time since last frame
   */
  private void jump(float delta) {
    jumpTime += delta;
    float progress = Math.min(jumpTime / JUMP_DURATION, 1f);
    Vector2 position = jumpStart.cpy().lerp(destination, progress);
    position.y += 4f * JUMP_HEIGHT * progress * (1f - progress);
    entity.setPosition(position);
    if (progress < 1f) {
      return;
    }
    entity.getEvents().trigger(LANDED_EVENT);
    if (state == State.JUMPING) {
      startSwing();
    } else {
      animator.setFlipX(false);
      animator.startAnimation("sleep");
      state = State.ASLEEP;
      entity.getEvents().trigger(FELL_ASLEEP_EVENT);
    }
  }

  /** Start the swinging animation */
  private void startSwing() {
    face(target.getCenterPosition().x);
    animator.startAnimation("attack");
    attackTime = 0f;
    hitLanded = false;
    state = State.ATTACKING;
  }

  private void swing(float delta) {
    attackTime += delta;
    if (!hitLanded && attackTime >= IMPACT_FRAME * ATTACK_FRAME_TIME) {
      hitLanded = true;
      entity.getEvents().trigger(CLUB_LANDED_EVENT);
      ServiceLocator.getCyclopsMinigameEventHandler()
          .trigger(CyclopsMinigameLogic.CYCLOPS_HIT_EVENT);
    }
    if (animator.isFinished()) {
      animator.startAnimation("idle");
      state = State.AWAKE;
    }
  }

  private float bodyX() {
    return entity.getCenterPosition().x;
  }

  private void face(float x) {
    animator.setFlipX(x > bodyX());
  }
}
