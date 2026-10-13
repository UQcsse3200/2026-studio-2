package com.csse3200.game.components.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.DynamicTextureRenderComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
public class CrumblingPlatformTest {
  @Test
  void shouldAddCollisionEvent() {
    // setup entity and event handler
    Entity e = mock(Entity.class);
    EventHandler events = new EventHandler();
    when(e.getEvents()).thenReturn(events);

    // setup crumbling platform
    CrumblingPlatformComponent platform = spy(new CrumblingPlatformComponent(0, 0f, 0f, 0f));
    // ensure nothing runs when a fake collision starts
    doNothing().when(platform).onCollisionStart(any(Fixture.class), any(Fixture.class));
    platform.setEntity(e);
    platform.create();

    Fixture f = mock(Fixture.class);
    e.getEvents().trigger("collisionStart", f, f);
    // ensures the method is called, but doNothing stops it from running so we don't need to
    // create real Fixtures that actually do physics checks
    verify(platform, atLeastOnce()).onCollisionStart(any(Fixture.class), any(Fixture.class));
  }

  @Test
  void shouldNotRestartCrumble() {
    // create a currently crumbling platform
    CrumblingPlatformComponent platform = new CrumblingPlatformComponent(0, 0f, 0f, 0f);
    platform.state = CrumblingPlatformComponent.CrumbleState.CRUMBLING;

    // simulate a collision and ensure it early returns
    Fixture f = mock(Fixture.class);
    platform.onCollisionStart(f, f);
    verify(f, never()).getFilterData();
  }

  @Test
  void shouldNotStartCrumble() {
    CrumblingPlatformComponent platform = new CrumblingPlatformComponent(0, 0f, 0f, 0f);

    Fixture f = mock(Fixture.class);
    Filter filter = mock(Filter.class);
    filter.categoryBits = 0;
    when(f.getFilterData()).thenReturn(filter);

    platform.onCollisionStart(f, f);
    assertEquals(CrumblingPlatformComponent.CrumbleState.NORMAL, platform.state);
  }

  @Test
  void shouldStartCrumble() {
    CrumblingPlatformComponent platform = new CrumblingPlatformComponent(0, 0f, 0f, 0f);

    Fixture f = mock(Fixture.class);
    Filter filter = mock(Filter.class);
    filter.categoryBits = 0b0010; // set to player (1 << 1)
    when(f.getFilterData()).thenReturn(filter);

    platform.onCollisionStart(f, f);
    assertEquals(CrumblingPlatformComponent.CrumbleState.WAITING_TO_CRUMBLE, platform.state);
  }

  @Test
  void updateShouldDoNothing() {
    GameTime gameTime = mock(GameTime.class);
    when(gameTime.getDeltaTime()).thenReturn(1f);
    ServiceLocator.registerTimeSource(gameTime);

    CrumblingPlatformComponent platform = new CrumblingPlatformComponent(0, 0f, 0f, 0f);
    platform.update();
    assertEquals(0f, platform.stateTime);
  }

  @Test
  void shouldWaitToCrumble() {
    GameTime gameTime = mock(GameTime.class);
    when(gameTime.getDeltaTime()).thenReturn(1f);
    ServiceLocator.registerTimeSource(gameTime);

    CrumblingPlatformComponent platform = new CrumblingPlatformComponent(0, 2f, 0f, 0f);
    platform.state = CrumblingPlatformComponent.CrumbleState.WAITING_TO_CRUMBLE;

    platform.update();
    assertEquals(1f, platform.stateTime);

    platform.update();
    assertEquals(0f, platform.stateTime);
    assertEquals(CrumblingPlatformComponent.CrumbleState.CRUMBLING, platform.state);
  }

  @Test
  void shouldAttemptCrumble() {
    GameTime gameTime = mock(GameTime.class);
    when(gameTime.getDeltaTime()).thenReturn(1f);
    ServiceLocator.registerTimeSource(gameTime);

    CrumblingPlatformComponent platform = spy(new CrumblingPlatformComponent(0, 0f, 2f, 0f));
    doNothing().when(platform).crumble();
    platform.state = CrumblingPlatformComponent.CrumbleState.CRUMBLING;

    platform.update();
    assertEquals(1f, platform.stateTime);

    platform.update();
    assertEquals(2f, platform.stateTime);
    verify(platform, atLeastOnce()).crumble();
  }

  @Test
  void shouldAttemptRespawn() {
    GameTime gameTime = mock(GameTime.class);
    when(gameTime.getDeltaTime()).thenReturn(1f);
    ServiceLocator.registerTimeSource(gameTime);

    CrumblingPlatformComponent platform = spy(new CrumblingPlatformComponent(0, 0f, 0f, 2f));
    doNothing().when(platform).respawn();
    platform.state = CrumblingPlatformComponent.CrumbleState.CRUMBLED;

    platform.update();
    assertEquals(1f, platform.stateTime);

    platform.update();
    assertEquals(2f, platform.stateTime);
    verify(platform, atLeastOnce()).respawn();
  }

  @Test
  void crumbleShouldDisablePlatform() {
    Entity e = mock(Entity.class);

    PhysicsComponent physics = mock(PhysicsComponent.class);
    physics.setEntity(e);

    DynamicTextureRenderComponent textureRenderer = mock(DynamicTextureRenderComponent.class);
    Texture texture = mock(Texture.class);
    textureRenderer.setEntity(e);
    when(textureRenderer.getTexture()).thenReturn(texture);
    doNothing().when(textureRenderer).setTexture(anyString());

    when(e.getComponent(PhysicsComponent.class)).thenReturn(physics);
    when(e.getComponent(DynamicTextureRenderComponent.class)).thenReturn(textureRenderer);

    CrumblingPlatformComponent platform = new CrumblingPlatformComponent(0, 0f, 2f, 0f);
    platform.setEntity(e);
    platform.state = CrumblingPlatformComponent.CrumbleState.CRUMBLING;
    platform.stateTime = 2f;

    platform.crumble();
    assertEquals(CrumblingPlatformComponent.CrumbleState.CRUMBLED, platform.state);
    assertEquals(0f, platform.stateTime);
    // physics being disabled is tested appropriately in game
    assertEquals(texture, platform.platformTexture);
  }

  @Test
  void respawnShouldEnablePlatform() {
    Entity e = mock(Entity.class);

    PhysicsComponent physics = mock(PhysicsComponent.class);
    physics.setEntity(e);

    DynamicTextureRenderComponent textureRenderer = mock(DynamicTextureRenderComponent.class);
    Texture texture = mock(Texture.class);
    textureRenderer.setEntity(e);
    when(textureRenderer.getTexture()).thenReturn(texture);
    doNothing().when(textureRenderer).setTexture(anyString());

    when(e.getComponent(PhysicsComponent.class)).thenReturn(physics);
    when(e.getComponent(DynamicTextureRenderComponent.class)).thenReturn(textureRenderer);

    CrumblingPlatformComponent platform = new CrumblingPlatformComponent(0, 0f, 0f, 2f);
    platform.setEntity(e);
    platform.state = CrumblingPlatformComponent.CrumbleState.CRUMBLED;
    platform.stateTime = 2f;
    platform.platformTexture = texture;

    platform.respawn();
    assertEquals(CrumblingPlatformComponent.CrumbleState.NORMAL, platform.state);
    assertEquals(0f, platform.stateTime);
    // physics being enabled is tested appropriately in game
    assertEquals(platform.platformTexture, textureRenderer.getTexture());
  }
}
