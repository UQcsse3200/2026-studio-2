package com.csse3200.game.components.player;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class PlayerAttackComponentTest {
  @Test
  void shouldDelegatePrimaryAttackToActiveBehaviour() {
    AttackBehaviour bow = mock(AttackBehaviour.class);
    PlayerAttackComponent component = new PlayerAttackComponent(bow);
    Entity player = new Entity().addComponent(component);
    player.create();
    Vector2 direction = new Vector2(3f, 4f);

    player.getEvents().trigger("primaryAttack", direction);

    verify(bow).attack(direction);
  }

  @Test
  void shouldSwitchActiveAttackBehaviour() {
    AttackBehaviour bow = mock(AttackBehaviour.class);
    AttackBehaviour replacement = mock(AttackBehaviour.class);
    PlayerAttackComponent component = new PlayerAttackComponent(bow);
    Entity player = new Entity().addComponent(component);
    player.create();
    component.setActiveAttack(replacement);
    Vector2 direction = Vector2.X.cpy();

    player.getEvents().trigger("primaryAttack", direction);

    verify(replacement).attack(direction);
  }
}
