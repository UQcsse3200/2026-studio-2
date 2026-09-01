/*
package com.csse3200.game.components;

import static org.mockito.mockito.donothing;
import static org.mockito.mockito.spy;
import static org.mockito.mockito.times;
import static org.mockito.mockito.verify;

import com.csse3200.game.entities.entity;
import com.csse3200.game.extensions.gameextension;
import org.junit.jupiter.api.extension.extendwith;
import org.junit.jupiter.api.test;

@extendwith(gameextension.class)
class enemydeathcomponenttest {

  @test
  void shoulddisposeentityatzerohealth() {
    entity entity = spy(entity.class);
    donothing().when(entity).dispose();

    combatstatscomponent combatstats = new combatstatscomponent(100, 10);

    entity.addcomponent(combatstats);
    entity.addcomponent(new enemydeathcomponent());
    entity.create();

    combatstats.sethealth(0);

    verify(entity).dispose();
  }

  @test
  void shouldnotdisposeentityatpositivehealth() {
    entity entity = spy(entity.class);
    donothing().when(entity).dispose();

    combatstatscomponent combatstats = new combatstatscomponent(100, 10);

    entity.addcomponent(combatstats);
    entity.addcomponent(new enemydeathcomponent());
    entity.create();

    combatstats.sethealth(50);

    verify(entity, times(0)).dispose();
  }
}
 */
