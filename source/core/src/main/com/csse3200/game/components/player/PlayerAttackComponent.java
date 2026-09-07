package com.csse3200.game.components.player;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import java.util.Objects;

/** Routes generic player attack events to the currently active attack behaviour. */
public class PlayerAttackComponent extends Component {
  private AttackBehaviour activeAttack;

  /**
   * Creates an attack coordinator with an initial attack behaviour.
   *
   * @param activeAttack initial attack behaviour
   */
  public PlayerAttackComponent(AttackBehaviour activeAttack) {
    setActiveAttack(activeAttack);
  }

  @Override
  public void create() {
    entity.getEvents().addListener("primaryAttack", this::attack);
  }

  private void attack(Vector2 direction) {
    activeAttack.attack(direction);
  }

  /**
   * Changes the behaviour used for subsequent attacks.
   *
   * @param activeAttack new attack behaviour
   */
  public void setActiveAttack(AttackBehaviour activeAttack) {
    this.activeAttack = Objects.requireNonNull(activeAttack, "Attack behaviour must not be null");
  }
}
