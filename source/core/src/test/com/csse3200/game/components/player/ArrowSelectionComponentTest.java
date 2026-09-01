package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class ArrowSelectionComponentTest {

  private Entity playerWithSelection() {
    Entity player = new Entity().addComponent(new ArrowSelectionComponent());
    player.create();
    return player;
  }

  @Test
  void shouldFireStandardArrowByDefault() {
    Entity player = playerWithSelection();
    AtomicInteger attacks = new AtomicInteger();
    AtomicInteger grapples = new AtomicInteger();
    player.getEvents().addListener("primaryAttack", (Vector2 d) -> attacks.incrementAndGet());
    player.getEvents().addListener("grappleFire", (Vector2 d) -> grapples.incrementAndGet());

    player.getEvents().trigger("shoot", new Vector2(1f, 0f));

    assertEquals(1, attacks.get());
    assertEquals(0, grapples.get());
  }

  @Test
  void shouldFireGrappleAfterCycling() {
    Entity player = playerWithSelection();
    AtomicInteger attacks = new AtomicInteger();
    AtomicInteger grapples = new AtomicInteger();
    player.getEvents().addListener("primaryAttack", (Vector2 d) -> attacks.incrementAndGet());
    player.getEvents().addListener("grappleFire", (Vector2 d) -> grapples.incrementAndGet());

    player.getEvents().trigger("cycleArrow");
    player.getEvents().trigger("shoot", new Vector2(1f, 0f));

    assertEquals(0, attacks.get());
    assertEquals(1, grapples.get());
  }

  @Test
  void shouldWrapBackToStandard() {
    Entity player = playerWithSelection();
    ArrowSelectionComponent selection = player.getComponent(ArrowSelectionComponent.class);

    player.getEvents().trigger("cycleArrow");
    player.getEvents().trigger("cycleArrow");

    assertEquals(ArrowSelectionComponent.ArrowType.STANDARD, selection.getSelected());
  }

  @Test
  void shouldOnlyReleaseGrappleWhenGrappleSelected() {
    Entity player = playerWithSelection();
    AtomicInteger releases = new AtomicInteger();
    player.getEvents().addListener("grappleRelease", releases::incrementAndGet);

    player.getEvents().trigger("stopShoot");
    assertEquals(0, releases.get());

    player.getEvents().trigger("cycleArrow");
    player.getEvents().trigger("stopShoot");
    assertEquals(1, releases.get());
  }

  @Test
  void shouldPassAimDirectionThrough() {
    Entity player = playerWithSelection();
    Vector2 aim = new Vector2(3f, 4f);
    AtomicInteger matches = new AtomicInteger();
    player
        .getEvents()
        .addListener(
            "primaryAttack",
            (Vector2 direction) -> {
              if (direction.epsilonEquals(aim)) {
                matches.incrementAndGet();
              }
            });

    player.getEvents().trigger("shoot", aim);

    assertEquals(1, matches.get());
  }
}
