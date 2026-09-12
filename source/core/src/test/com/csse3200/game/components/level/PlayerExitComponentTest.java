package com.csse3200.game.components.level;

import static org.mockito.Mockito.*;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.services.ServiceLocator;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class PlayerExitComponentTest {
  @Test
  void waitsForPlayerUnlockAndClosedOverlayThenCompletesOnce() {
    EntityService service = mock(EntityService.class);
    ServiceLocator.registerEntityService(service);
    Entity player = new Entity();
    Entity zone = new Entity();
    AtomicBoolean unlocked = new AtomicBoolean();
    Runnable exit = mock(Runnable.class);
    PlayerExitComponent component = new PlayerExitComponent(player, unlocked::get, exit);
    zone.addComponent(component);
    component.create();
    zone.getEvents().trigger("collisionStart", (Fixture) null, fixtureFor(new Entity()));
    unlocked.set(true);
    component.update();
    verifyNoInteractions(exit);
    unlocked.set(false);
    Fixture foot = fixtureFor(player);
    Fixture hitbox = fixtureFor(player);
    zone.getEvents().trigger("collisionStart", (Fixture) null, foot);
    zone.getEvents().trigger("collisionStart", (Fixture) null, hitbox);
    component.update();
    verifyNoInteractions(exit);
    zone.getEvents().trigger("collisionEnd", (Fixture) null, foot);
    unlocked.set(true);
    when(service.getPaused()).thenReturn(true);
    component.update();
    verifyNoInteractions(exit);
    when(service.getPaused()).thenReturn(false);
    component.update();
    component.update();
    verify(exit, times(1)).run();
  }

  private Fixture fixtureFor(Entity entity) {
    Fixture fixture = mock(Fixture.class);
    Body body = mock(Body.class);
    BodyUserData data = new BodyUserData();
    data.entity = entity;
    when(fixture.getBody()).thenReturn(body);
    when(body.getUserData()).thenReturn(data);
    return fixture;
  }
}
