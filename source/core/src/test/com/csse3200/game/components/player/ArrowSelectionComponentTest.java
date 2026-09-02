package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.player.ArrowSelectionComponent.ArrowType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class ArrowSelectionComponentTest {

  private Entity createPlayerWithSelector(ArrowSelectionComponent component) {
    Entity player = new Entity().addComponent(component);
    component.create();
    return player;
  }

  @Test
  void shouldFireStandardArrowByDefault() {
    ArrowSelectionComponent arrow_comp = new ArrowSelectionComponent();
    Entity player = createPlayerWithSelector(arrow_comp);
    AtomicInteger attacks = new AtomicInteger();
    AtomicInteger grapples = new AtomicInteger();
    player.getEvents().addListener("primaryAttack", (Vector2 d) -> attacks.incrementAndGet());
    player.getEvents().addListener("grappleFire", (Vector2 d) -> grapples.incrementAndGet());

    Vector2 direction = new Vector2(1f, 0f);
    player.getEvents().trigger("shoot", direction);
    player.getEvents().trigger("stopShoot", direction);

    assertEquals(1, attacks.get());
    assertEquals(0, grapples.get());
  }

  @Test
  void shouldFireGrappleAfterCycling() {
    ArrowSelectionComponent arrow_comp = new ArrowSelectionComponent();
    Entity player = createPlayerWithSelector(arrow_comp);
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
    ArrowSelectionComponent arrow_comp = new ArrowSelectionComponent();
    Entity player = createPlayerWithSelector(arrow_comp);

    player.getEvents().trigger("cycleArrow");
    player.getEvents().trigger("cycleArrow");

    assertEquals(ArrowSelectionComponent.ArrowType.STANDARD, arrow_comp.getSelected());
  }

  @Test
  void shouldOnlyReleaseGrappleWhenGrappleSelected() {
    ArrowSelectionComponent arrow_comp = new ArrowSelectionComponent();
    Entity player = createPlayerWithSelector(arrow_comp);
    AtomicInteger releases = new AtomicInteger();
    player.getEvents().addListener("grappleRelease", releases::incrementAndGet);

    player.getEvents().trigger("stopShoot", (Vector2) null);
    assertEquals(0, releases.get());

    player.getEvents().trigger("cycleArrow");
    player.getEvents().trigger("stopShoot", (Vector2) null);
    assertEquals(1, releases.get());
  }

  @Test
  void shouldPassAimDirectionThrough() {
    ArrowSelectionComponent arrow_comp = new ArrowSelectionComponent();
    Entity player = createPlayerWithSelector(arrow_comp);
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
    player.getEvents().trigger("stopShoot", aim);

    assertEquals(1, matches.get());
  }

  @Test
  void shouldDefaultToStandardArrow() {
    ArrowSelectionComponent component = new ArrowSelectionComponent();
    assertEquals(ArrowType.STANDARD, component.getSelected());
  }

  @Test
  void shouldCycleBetweenArrowTypes() {
    ArrowSelectionComponent component = new ArrowSelectionComponent();
    Entity player = createPlayerWithSelector(component);
    AtomicReference<ArrowType> changedTo = new AtomicReference<>();
    player.getEvents().addListener("arrowChanged", (ArrowType type) -> changedTo.set(type));

    player.getEvents().trigger("cycleArrow");
    assertEquals(ArrowType.GRAPPLE, component.getSelected());
    assertEquals(ArrowType.GRAPPLE, changedTo.get());

    player.getEvents().trigger("cycleArrow");
    assertEquals(ArrowType.STANDARD, component.getSelected());
  }

  @Test
  void standardArrowShouldStartChargeOnShootAndFireOnRelease() {
    ArrowSelectionComponent component = new ArrowSelectionComponent();
    Entity player = createPlayerWithSelector(component);

    AtomicInteger chargeStarts = new AtomicInteger();
    AtomicReference<Vector2> attackDirection = new AtomicReference<>();
    AtomicInteger grappleFires = new AtomicInteger();
    AtomicInteger grappleReleases = new AtomicInteger();
    player.getEvents().addListener("attackChargeStart", chargeStarts::incrementAndGet);
    player.getEvents().addListener("primaryAttack", (Vector2 dir) -> attackDirection.set(dir));
    player.getEvents().addListener("grappleFire", (Vector2 dir) -> grappleFires.incrementAndGet());
    player.getEvents().addListener("grappleRelease", grappleReleases::incrementAndGet);

    Vector2 direction = new Vector2(1f, 0f);
    player.getEvents().trigger("shoot", direction);
    assertEquals(1, chargeStarts.get());
    assertNull(attackDirection.get()); // no shot fired yet, still charging

    player.getEvents().trigger("stopShoot", direction);
    assertEquals(direction, attackDirection.get());
    assertEquals(0, grappleFires.get());
    assertEquals(0, grappleReleases.get());
  }

  @Test
  void standardArrowShouldNotFireOnReleaseWithoutValidDirection() {
    ArrowSelectionComponent component = new ArrowSelectionComponent();
    Entity player = createPlayerWithSelector(component);
    AtomicInteger attacks = new AtomicInteger();
    player.getEvents().addListener("primaryAttack", (Vector2 dir) -> attacks.incrementAndGet());

    player.getEvents().trigger("shoot", new Vector2(1f, 0f));
    player.getEvents().trigger("stopShoot", (Vector2) null);
    assertEquals(0, attacks.get());

    player.getEvents().trigger("stopShoot", Vector2.Zero.cpy());
    assertEquals(0, attacks.get());
  }

  @Test
  void grappleArrowShouldFireImmediatelyAndNeverTriggerChargeEvents() {
    ArrowSelectionComponent component = new ArrowSelectionComponent();
    Entity player = createPlayerWithSelector(component);
    player.getEvents().trigger("cycleArrow"); // switch to GRAPPLE

    AtomicInteger chargeStarts = new AtomicInteger();
    AtomicInteger attacks = new AtomicInteger();
    AtomicReference<Vector2> grappleFireDirection = new AtomicReference<>();
    AtomicInteger grappleReleases = new AtomicInteger();
    player.getEvents().addListener("attackChargeStart", chargeStarts::incrementAndGet);
    player.getEvents().addListener("primaryAttack", (Vector2 dir) -> attacks.incrementAndGet());
    player.getEvents().addListener("grappleFire", (Vector2 dir) -> grappleFireDirection.set(dir));
    player.getEvents().addListener("grappleRelease", grappleReleases::incrementAndGet);

    Vector2 direction = new Vector2(0f, 1f);
    player.getEvents().trigger("shoot", direction);
    assertEquals(direction, grappleFireDirection.get());
    assertEquals(0, chargeStarts.get());
    assertEquals(0, attacks.get());

    player.getEvents().trigger("stopShoot", direction);
    assertEquals(1, grappleReleases.get());
  }
}
