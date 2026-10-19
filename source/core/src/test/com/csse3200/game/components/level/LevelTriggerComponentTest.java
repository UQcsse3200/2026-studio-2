package com.csse3200.game.components.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class LevelTriggerComponentTest {
  @Test
  void onlyPlayerCanTriggerAndOnlyOnce() {
    Entity door = new Entity().addComponent(new LevelTriggerComponent("level2"));
    AtomicInteger transitions = new AtomicInteger();
    door.getEvents()
        .addListener("triggerNextLevel", (String level) -> transitions.incrementAndGet());
    door.getComponent(LevelTriggerComponent.class).create();
    Fixture other = mock(Fixture.class);
    Body body = mock(Body.class);
    when(other.getBody()).thenReturn(body);
    BodyUserData data = new BodyUserData();
    data.entity = new Entity();
    when(body.getUserData()).thenReturn(data);
    door.getEvents().trigger("collisionStart", mock(Fixture.class), other);
    assertEquals(0, transitions.get());
    data.entity.addComponent(new PlayerActions());
    door.getEvents().trigger("collisionStart", mock(Fixture.class), other);
    door.getEvents().trigger("collisionStart", mock(Fixture.class), other);
    assertEquals(1, transitions.get());
  }
}
