package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.csse3200.game.components.item.weapons.bow.grapple.GrappleComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Exercises jump audio through real movement, grapple, and sound components. */
@ExtendWith(GameExtension.class)
class PlayerJumpSoundTest {
  private PhysicsService physics;
  private GameTime time;
  private Sound sound;
  private Entity player;

  @BeforeEach
  void setUp() {
    physics = new PhysicsService();
    ServiceLocator.registerPhysicsService(physics);
    time = mock(GameTime.class);
    ServiceLocator.registerTimeSource(time);
    ResourceService resources = mock(ResourceService.class);
    sound = mock(Sound.class);
    when(resources.containsAsset("sounds/jump.ogg", Sound.class)).thenReturn(true);
    when(resources.getAsset("sounds/jump.ogg", Sound.class)).thenReturn(sound);
    ServiceLocator.registerResourceService(resources);
    player =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new GrappleComponent())
            .addComponent(new PlayerActions())
            .addComponent(new PlayerSoundComponent());
    player.create();
  }

  @AfterEach
  void tearDown() {
    physics.getPhysics().dispose();
  }

  @Test
  void groundedJumpPlaysOnceThroughTakeoff() {
    groundPlayer();

    player.getEvents().trigger("jump");
    when(time.getTime()).thenReturn(100L);
    player.update();

    assertTrue(player.getComponent(PhysicsComponent.class).getBody().getLinearVelocity().y > 0f);
    verify(sound).play(0.5f);
  }

  @Test
  void airborneJumpRequestIsSilent() {
    player.update();

    player.getEvents().trigger("jump");

    verify(sound, never()).play(0.5f);
  }

  @Test
  void repeatedJumpRequestsDuringWindupDoNotReplaySound() {
    groundPlayer();

    player.getEvents().trigger("jump");
    when(time.getTime()).thenReturn(30L);
    player.update();
    player.getEvents().trigger("jump");
    player.getEvents().trigger("jump");

    verify(sound).play(0.5f);
  }

  @Test
  void deadPlayerJumpRequestIsSilent() {
    groundPlayer();
    player.getEvents().trigger("death");

    player.getEvents().trigger("jump");

    verify(sound, never()).play(0.5f);
  }

  @Test
  void grappleJumpPlaysOnceWithoutGroundTakeoffAnimation() {
    BodyDef anchorDef = new BodyDef();
    anchorDef.type = BodyDef.BodyType.StaticBody;
    anchorDef.position.set(0f, 5f);
    Body anchor = physics.getPhysics().createBody(anchorDef);
    GrappleComponent grapple = player.getComponent(GrappleComponent.class);
    grapple.attachTo(anchor, new Vector2(0f, 5f));
    grapple.update();
    assertTrue(grapple.isAttached());
    AtomicBoolean groundTakeoff = new AtomicBoolean();
    player.getEvents().addListener("jumpStart", () -> groundTakeoff.set(true));

    player.getEvents().trigger("jump");

    assertFalse(grapple.isAttached());
    assertTrue(player.getComponent(PhysicsComponent.class).getBody().getLinearVelocity().y > 0f);
    verify(sound).play(0.5f);
    assertFalse(groundTakeoff.get());
  }

  private void groundPlayer() {
    BodyDef floorDef = new BodyDef();
    floorDef.type = BodyDef.BodyType.StaticBody;
    floorDef.position.set(0.5f, -0.2f);
    Body floor = physics.getPhysics().createBody(floorDef);
    PolygonShape shape = new PolygonShape();
    shape.setAsBox(1f, 0.1f);
    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.shape = shape;
    fixtureDef.filter.categoryBits = PhysicsLayer.GROUND;
    floor.createFixture(fixtureDef);
    shape.dispose();
    player.update();
  }
}
