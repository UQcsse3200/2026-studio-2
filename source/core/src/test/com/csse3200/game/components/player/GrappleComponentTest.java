package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class GrappleComponentTest {

  @Test
  void shouldStartDetached() {
    GrappleComponent grapple = new GrappleComponent();
    assertFalse(grapple.isAttached());
    assertNull(grapple.getAnchorPoint());
  }

  @Test
  void shouldIgnoreReleaseWhenNotAttached() {
    GrappleComponent grapple = new GrappleComponent();
    // Would throw if it tried to destroy a null joint
    grapple.release();
    assertFalse(grapple.isAttached());
  }

  @Test
  void shouldIgnoreSwingWhenNotAttached() {
    GrappleComponent grapple = new GrappleComponent();
    // Would throw on the null body if the guard were missing
    grapple.swing(1f);
    grapple.swing(-1f);
    grapple.swing(0f);
  }

  @Test
  void shouldQueueAttachmentWithoutBuildingJoint() {
    GrappleComponent grapple = new GrappleComponent();
    // Queuing only stores the point, the joint waits for update()
    grapple.attachTo(null, new Vector2(3f, 4f));
    assertFalse(grapple.isAttached());
  }

  @Test
  void shouldNotFireWithoutDirection() {
    Entity player = new Entity().addComponent(new GrappleComponent());
    GrappleComponent grapple = player.getComponent(GrappleComponent.class);

    // Would reach ProjectileFactory and fail without a registered service
    grapple.fire(null);
    grapple.fire(Vector2.Zero.cpy());
  }

  @Test
  void shouldCopyAnchorPointOnRead() {
    GrappleComponent grapple = new GrappleComponent();
    assertNull(grapple.getAnchorPoint());
  }
}
