package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class ArrowWheelComponentTest {
  private static final float FAR = ArrowType.DEADZONE_RADIUS * 3f;
  private static final Vector2 TOWARDS_FIRE = new Vector2(FAR, 0f);
  private static final Vector2 TOWARDS_ICE = new Vector2(0f, -FAR);
  private static final Vector2 CENTRE = new Vector2(0f, 0f);

  private ArrowWheelComponent wheel;
  private Entity player;

  @BeforeEach
  void setUp() {
    wheel = new ArrowWheelComponent();
    player = new Entity().addComponent(wheel);
    player.create();
  }

  @Test
  void shouldStartClosedWithNormalArrowsSelected() {
    assertFalse(wheel.isOpen());
    assertNull(wheel.getHighlighted());
    assertEquals(ArrowType.NORMAL, wheel.getSelected());
  }

  @Test
  void shouldOpenOnlyWhileHoldingABow() {
    wheel.setBowEquipped(false);
    assertFalse(wheel.open());
    assertFalse(wheel.isOpen());

    wheel.setBowEquipped(true);
    assertTrue(wheel.open());
    assertTrue(wheel.isOpen());
  }

  @Test
  void shouldIgnoreOpeningAnAlreadyOpenWheel() {
    AtomicInteger opens = new AtomicInteger();
    player.getEvents().addListener("arrowWheelOpened", opens::incrementAndGet);

    wheel.open();
    wheel.highlightFromPointer(TOWARDS_FIRE);
    assertFalse(wheel.open());

    assertEquals(1, opens.get());
    assertEquals(ArrowType.FIRE, wheel.getHighlighted());
  }

  @Test
  void shouldHighlightTheTypeUnderThePointer() {
    AtomicReference<ArrowType> announced = new AtomicReference<>();
    player.getEvents().addListener("arrowHighlighted", (ArrowType type) -> announced.set(type));

    wheel.open();
    assertTrue(wheel.highlightFromPointer(TOWARDS_ICE));

    assertEquals(ArrowType.ICE, wheel.getHighlighted());
    assertEquals(ArrowType.ICE, announced.get());
  }

  @Test
  void shouldReportNoChangeWhenThePointerStaysInTheSameWedge() {
    wheel.open();
    wheel.highlightFromPointer(TOWARDS_ICE);

    assertFalse(wheel.highlightFromPointer(new Vector2(FAR * 0.2f, -FAR)));
    assertEquals(ArrowType.ICE, wheel.getHighlighted());
  }

  @Test
  void shouldIgnoreThePointerWhileClosed() {
    assertFalse(wheel.highlightFromPointer(TOWARDS_FIRE));
    assertNull(wheel.getHighlighted());
  }

  @Test
  void shouldApplyTheHighlightedTypeOnClose() {
    AtomicReference<ArrowType> applied = new AtomicReference<>();
    player.getEvents().addListener("arrowSelected", (ArrowType type) -> applied.set(type));

    wheel.open();
    wheel.highlightFromPointer(TOWARDS_FIRE);
    assertTrue(wheel.close());

    assertEquals(ArrowType.FIRE, wheel.getSelected());
    assertEquals(ArrowType.FIRE, applied.get());
    assertFalse(wheel.isOpen());
    assertNull(wheel.getHighlighted());
  }

  @Test
  void shouldKeepThePreviousTypeWhenTheHighlightIsUnavailable() {
    AtomicReference<ArrowType> rejected = new AtomicReference<>();
    player
        .getEvents()
        .addListener("arrowSelectionRejected", (ArrowType type) -> rejected.set(type));
    wheel.setAvailable(ArrowType.ICE, false);

    wheel.open();
    wheel.highlightFromPointer(TOWARDS_ICE);
    assertFalse(wheel.close());

    assertEquals(ArrowType.NORMAL, wheel.getSelected());
    assertEquals(ArrowType.ICE, rejected.get());
  }

  @Test
  void shouldKeepThePreviousTypeWhenThePointerIsAtTheCentre() {
    AtomicInteger rejections = new AtomicInteger();
    player
        .getEvents()
        .addListener("arrowSelectionRejected", (ArrowType ignored) -> rejections.incrementAndGet());

    wheel.open();
    wheel.highlightFromPointer(TOWARDS_FIRE);
    wheel.highlightFromPointer(CENTRE);
    assertFalse(wheel.close());

    assertEquals(ArrowType.NORMAL, wheel.getSelected());
    assertEquals(0, rejections.get());
  }

  @Test
  void shouldCloseWithoutSelectingWhenTheBowIsLost() {
    AtomicInteger closes = new AtomicInteger();
    player.getEvents().addListener("arrowWheelClosed", closes::incrementAndGet);

    wheel.open();
    wheel.highlightFromPointer(TOWARDS_FIRE);
    wheel.setBowEquipped(false);

    assertFalse(wheel.isOpen());
    assertEquals(ArrowType.NORMAL, wheel.getSelected());
    assertEquals(1, closes.get());
  }

  @Test
  void shouldDoNothingWhenClosingAWheelThatIsNotOpen() {
    AtomicInteger closes = new AtomicInteger();
    player.getEvents().addListener("arrowWheelClosed", closes::incrementAndGet);

    assertFalse(wheel.close());
    assertEquals(0, closes.get());
  }

  @Test
  void shouldLockAndUnlockTypes() {
    assertTrue(wheel.isAvailable(ArrowType.POISON));

    wheel.setAvailable(ArrowType.POISON, false);
    assertFalse(wheel.isAvailable(ArrowType.POISON));

    wheel.setAvailable(ArrowType.POISON, true);
    assertTrue(wheel.isAvailable(ArrowType.POISON));
  }

  @Test
  void shouldDriveTheWheelFromPlayerEvents() {
    player.getEvents().trigger("openArrowWheel");
    player.getEvents().trigger("arrowWheelPointerMoved", TOWARDS_FIRE);
    player.getEvents().trigger("closeArrowWheel");

    assertEquals(ArrowType.FIRE, wheel.getSelected());
    assertFalse(wheel.isOpen());
  }
}
