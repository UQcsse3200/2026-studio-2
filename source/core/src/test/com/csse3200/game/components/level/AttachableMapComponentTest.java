package com.csse3200.game.components.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class AttachableMapComponentTest {
  @Test
  void testInvalidEntity() {
    Entity e = mock(Entity.class); // entity with no physics or body

    AttachableMapComponent attachable = spy(new AttachableMapComponent());
    attachable.setEntity(e);
    attachable.update();

    verify(attachable, never()).discoverParent();
    verify(attachable, never()).moveWithParent();

    // test physics, no physics body
    PhysicsComponent physics = mock(PhysicsComponent.class);
    e.addComponent(physics);
    when(physics.getBody()).thenReturn(null);

    attachable.update();
    verify(attachable, never()).discoverParent();
    verify(attachable, never()).moveWithParent();
  }

  @Test
  void shouldDiscoverParent() {
    // mock physics service
    PhysicsService service = mock(PhysicsService.class);
    ServiceLocator.registerPhysicsService(service); // add mock to temporary services
    PhysicsEngine engine = mock(PhysicsEngine.class);
    when(service.getPhysics()).thenReturn(engine);

    // set up a child and a parent to discover
    Entity child = mock(Entity.class);
    PhysicsComponent childPhysics = mock(PhysicsComponent.class);
    Body childBody = mock(Body.class);
    when(child.getComponent(PhysicsComponent.class)).thenReturn(childPhysics);
    when(childPhysics.getBody()).thenReturn(childBody);
    when(child.getCenterPosition()).thenReturn(new Vector2(0, 0));
    when(childBody.getPosition()).thenReturn(new Vector2(0, 0));

    Entity parent = mock(Entity.class);
    PhysicsComponent parentPhysics = mock(PhysicsComponent.class);
    Body parentBody = mock(Body.class);
    when(parent.getComponent(PhysicsComponent.class)).thenReturn(parentPhysics);
    when(parentPhysics.getBody()).thenReturn(parentBody);
    when(parentBody.getPosition()).thenReturn(new Vector2(0, 1));

    // attachable component set up
    AttachableMapComponent attachable = new AttachableMapComponent();
    attachable.setEntity(child);

    // mock raycast
    when(engine.raycast(any(), any(), eq(PhysicsLayer.OBSTACLE), any()))
        .thenAnswer(
            invocation -> {
              RaycastHit hit = invocation.getArgument(3); // mock the out hit parameter

              BodyUserData data =
                  new BodyUserData(); // mock the body user data to be stored in the fixture
              data.entity = parent;

              hit.fixture = mock(Fixture.class);
              Body body = mock(Body.class);
              when(hit.fixture.getBody()).thenReturn(body);
              when(body.getUserData()).thenReturn(data);

              return true; // confirm valid contact
            });

    attachable.update(); // force update

    // test correct values
    assertTrue(attachable.attached);
    assertEquals(parent, attachable.parent);
    assertEquals(new Vector2(0, -1), attachable.offset);
  }

  @Test
  void shouldNotDiscoverParent() {
    AttachableMapComponent attachable = new AttachableMapComponent();
    attachable.attemptedDiscovery = true;
    attachable.physics = mock(PhysicsComponent.class);

    attachable.discoverParent();
    verify(attachable.physics, never()).getBody();
  }

  @Test
  void testCorrectParentMovement() {
    Entity parent = mock(Entity.class);
    Entity child = mock(Entity.class);

    // set up parent physics by ensuring the correct values are returned when requested
    PhysicsComponent parentPhysics = mock(PhysicsComponent.class);
    Body parentBody = mock(Body.class);
    when(parent.getComponent(PhysicsComponent.class)).thenReturn(parentPhysics);
    when(parentPhysics.getBody()).thenReturn(parentBody);
    when(parentBody.getPosition()).thenReturn(Vector2.Zero);

    // set up child physics component
    PhysicsComponent childPhysics = mock(PhysicsComponent.class);
    Body childBody = mock(Body.class);
    when(child.getComponent(PhysicsComponent.class)).thenReturn(childPhysics);
    when(childPhysics.getBody()).thenReturn(childBody);
    when(childBody.getAngle()).thenReturn(0f);

    // set up valid connection between child and parent
    AttachableMapComponent attachable = new AttachableMapComponent();
    attachable.setEntity(child);
    attachable.parent = parent;
    attachable.attached = true;

    // simulate movement
    Vector2 updatedPos = new Vector2(-1f, 0f);
    when(parentBody.getPosition()).thenReturn(updatedPos);
    attachable.update();

    // ensure the child follows
    verify(childBody).setTransform(updatedPos.x, updatedPos.y, 0f);
  }

  @Test
  void shouldNotMoveNoParent() {
    // set up child
    Entity child = mock(Entity.class);
    PhysicsComponent childPhysics = mock(PhysicsComponent.class);
    Body childBody = mock(Body.class);

    when(child.getComponent(PhysicsComponent.class)).thenReturn(childPhysics);
    when(childPhysics.getBody()).thenReturn(childBody);

    // set up component with no parent
    AttachableMapComponent attachable = new AttachableMapComponent();
    attachable.setEntity(child);
    attachable.attached = true;

    attachable.update(); // simulate movement
    verify(childBody, never()).setTransform(any(), anyFloat()); // ensure the child is never moved
  }
}
