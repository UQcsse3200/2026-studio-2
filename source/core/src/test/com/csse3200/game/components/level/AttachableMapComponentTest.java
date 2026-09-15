package com.csse3200.game.components.level;

import static org.mockito.Mockito.*;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.components.PhysicsComponent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class AttachableMapComponentTest {
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
    attachable.offset = Vector2.Zero;

    // simulate movement
    Vector2 updatedPos = new Vector2(-1f, 0f);
    when(parentBody.getPosition()).thenReturn(updatedPos);
    attachable.update();

    // ensure the child follows
    verify(childBody).setTransform(updatedPos.x, updatedPos.y, 0f);
  }
}
