package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class KeyboardPlayerInputComponentTest {
  private Input input;
  private Camera camera;

  @BeforeEach
  void setUp() {
    input = mock(Input.class);
    Gdx.input = input;
    camera = mock(Camera.class);
    when(camera.unproject(any(Vector3.class)))
        .thenAnswer(
            invocation -> {
              Vector3 position = invocation.getArgument(0);
              return position.set(10f, 5f, 0f);
            });
  }

  private KeyboardPlayerInputComponent aimedComponent(Entity player) {
    KeyboardPlayerInputComponent component = new KeyboardPlayerInputComponent();
    player.addComponent(component);
    player.setPosition(0f, 0f);
    component.setCameraComponent(new CameraComponent(camera));
    return component;
  }

  @Test
  void shouldMeleeOnLeftClick() {
    Entity player = new Entity();
    KeyboardPlayerInputComponent component = aimedComponent(player);

    AtomicReference<Vector2> direction = new AtomicReference<>();
    player.getEvents().addListener("melee", (Vector2 aim) -> direction.set(aim));

    assertTrue(component.touchDown(4, 2, 0, Buttons.LEFT));
    assertTrue(direction.get().epsilonEquals(new Vector2(9.5f, 4.5f)));
  }

  @Test
  void shouldShootOnRightClick() {
    Entity player = new Entity();
    KeyboardPlayerInputComponent component = aimedComponent(player);

    AtomicInteger shots = new AtomicInteger();
    AtomicReference<Vector2> direction = new AtomicReference<>();
    player
        .getEvents()
        .addListener(
            "shoot",
            (Vector2 aim) -> {
              shots.incrementAndGet();
              direction.set(aim);
            });

    assertTrue(component.touchDown(4, 2, 0, Buttons.RIGHT));
    assertEquals(1, shots.get());
    assertTrue(direction.get().epsilonEquals(new Vector2(9.5f, 4.5f)));
  }

  @Test
  void shouldSignalStopShootOnRightRelease() {
    Entity player = new Entity();
    KeyboardPlayerInputComponent component = aimedComponent(player);

    AtomicInteger stops = new AtomicInteger();
    player.getEvents().addListener("stopShoot", (Vector2 ignored) -> stops.incrementAndGet());

    assertTrue(component.touchUp(4, 2, 0, Buttons.RIGHT));
    assertEquals(1, stops.get());
  }

  @Test
  void shouldResolveDirectionOnStopShoot() {
    Entity player = new Entity();
    KeyboardPlayerInputComponent component = aimedComponent(player);

    AtomicReference<Vector2> stopDirection = new AtomicReference<>();
    player
        .getEvents()
        .addListener("stopShoot", (Vector2 aimDirection) -> stopDirection.set(aimDirection));

    assertTrue(component.touchUp(4, 2, 0, Buttons.RIGHT));
    assertTrue(stopDirection.get().epsilonEquals(new Vector2(9.5f, 4.5f)));
  }

  @Test
  void shouldNotFireWithoutCamera() {
    KeyboardPlayerInputComponent component = new KeyboardPlayerInputComponent();
    Entity player = new Entity().addComponent(component);
    AtomicInteger shots = new AtomicInteger();
    player.getEvents().addListener("shoot", (Vector2 ignored) -> shots.incrementAndGet());

    assertFalse(component.touchDown(4, 2, 0, Buttons.RIGHT));
    assertEquals(0, shots.get());
  }

  @Test
  void shouldCycleArrowOnQ() {
    Entity player = new Entity();
    KeyboardPlayerInputComponent component = aimedComponent(player);

    AtomicInteger cycles = new AtomicInteger();
    player.getEvents().addListener("cycleArrow", cycles::incrementAndGet);

    assertTrue(component.keyDown(Keys.Q));
    assertEquals(1, cycles.get());
  }

  @Test
  void shouldUseSelectedItemOnE() {
    KeyboardPlayerInputComponent component = new KeyboardPlayerInputComponent();
    InventoryComponent inventory = new InventoryComponent(0);
    Entity player =
        new Entity()
            .addComponent(component)
            .addComponent(inventory)
            .addComponent(new ItemUseComponent());
    player.setPosition(0f, 0f);
    component.setCameraComponent(new CameraComponent(camera));
    inventory.addItem(ItemType.ARROW, 2);
    player.getComponent(ItemUseComponent.class).create();

    AtomicInteger attacks = new AtomicInteger();
    player.getEvents().addListener("primaryAttack", (Vector2 ignored) -> attacks.incrementAndGet());

    assertTrue(component.keyDown(Keys.E));
    assertEquals(1, attacks.get());
    assertEquals(1, inventory.getItemCount(ItemType.ARROW));

    assertTrue(component.keyUp(Keys.E));
    assertTrue(component.keyDown(Keys.E));
    assertEquals(2, attacks.get());
    assertEquals(0, inventory.getItemCount(ItemType.ARROW));
  }

  @Test
  void shouldNotUseItemWhenItemUseComponentIsMissing() {
    KeyboardPlayerInputComponent component = new KeyboardPlayerInputComponent();
    Entity player = new Entity().addComponent(component);
    AtomicInteger attacks = new AtomicInteger();
    player.getEvents().addListener("primaryAttack", (Vector2 ignored) -> attacks.incrementAndGet());

    assertTrue(component.keyDown(Keys.E));
    assertEquals(0, attacks.get());
  }

  @Test
  void shouldDropItemWithR() {
    KeyboardPlayerInputComponent component = new KeyboardPlayerInputComponent();
    Entity player = new Entity().addComponent(component);
    AtomicInteger drops = new AtomicInteger();
    player.getEvents().addListener("dropItem", drops::incrementAndGet);

    assertTrue(component.keyDown(Keys.R));
    assertEquals(1, drops.get());
  }

  @Test
  void shouldDeleteItemWithDelete() {
    KeyboardPlayerInputComponent component = new KeyboardPlayerInputComponent();
    Entity player = new Entity().addComponent(component);
    AtomicInteger deletions = new AtomicInteger();
    player.getEvents().addListener("deleteItem", deletions::incrementAndGet);

    assertTrue(component.keyDown(Keys.FORWARD_DEL));
    assertEquals(1, deletions.get());
  }

  @Test
  void shouldSelectQuickSlotWithNumberKey() {
    KeyboardPlayerInputComponent component = new KeyboardPlayerInputComponent();
    Entity player = new Entity().addComponent(component);
    AtomicReference<Integer> selectedSlot = new AtomicReference<>();
    player.getEvents().addListener("selectQuickSlot", selectedSlot::set);

    assertTrue(component.keyDown(Keys.NUM_3));
    assertEquals(2, selectedSlot.get());
  }

  @Test
  void shouldTriggerInteractionAndBackpackEvents() {
    KeyboardPlayerInputComponent component = new KeyboardPlayerInputComponent();
    Entity player = new Entity().addComponent(component);
    AtomicInteger interactions = new AtomicInteger();
    AtomicInteger backpackToggles = new AtomicInteger();
    player.getEvents().addListener("interact", interactions::incrementAndGet);
    player.getEvents().addListener("toggleBackpack", backpackToggles::incrementAndGet);

    assertTrue(component.keyDown(Keys.F));
    assertTrue(component.keyDown(Keys.B));
    assertEquals(1, interactions.get());
    assertEquals(1, backpackToggles.get());
  }

  @Test
  void shouldSwitchInventoryItemWithPeriodAndComma() {
    KeyboardPlayerInputComponent component = new KeyboardPlayerInputComponent();
    Entity player = new Entity().addComponent(component);
    AtomicReference<Integer> change = new AtomicReference<>();
    player.getEvents().addListener("switchItem", change::set);

    assertTrue(component.keyDown(Keys.PERIOD));
    assertEquals(1, change.get());
    assertTrue(component.keyDown(Keys.COMMA));
    assertEquals(-1, change.get());
  }

  @Test
  void shouldTriggerHorizontalMovementJumpAndSprintEvents() {
    KeyboardPlayerInputComponent component = new KeyboardPlayerInputComponent();
    Entity player = new Entity().addComponent(component);
    AtomicReference<Vector2> walkDirection = new AtomicReference<>();
    AtomicInteger walkStops = new AtomicInteger();
    AtomicInteger jumps = new AtomicInteger();
    AtomicInteger sprints = new AtomicInteger();
    AtomicInteger sprintStops = new AtomicInteger();

    player.getEvents().addListener("walk", (Vector2 direction) -> walkDirection.set(direction));
    player.getEvents().addListener("walkStop", walkStops::incrementAndGet);
    player.getEvents().addListener("jump", jumps::incrementAndGet);
    player.getEvents().addListener("sprint", sprints::incrementAndGet);
    player.getEvents().addListener("sprintStop", sprintStops::incrementAndGet);

    assertTrue(component.keyDown(Keys.A));
    assertTrue(walkDirection.get().epsilonEquals(new Vector2(-1f, 0f)));
    assertTrue(component.keyUp(Keys.A));
    assertEquals(1, walkStops.get());

    assertTrue(component.keyDown(Keys.RIGHT));
    assertTrue(walkDirection.get().epsilonEquals(new Vector2(1f, 0f)));
    assertTrue(component.keyUp(Keys.RIGHT));
    assertEquals(2, walkStops.get());

    assertTrue(component.keyDown(Keys.SPACE));
    assertEquals(1, jumps.get());
    assertTrue(component.keyDown(Keys.SHIFT_LEFT));
    assertEquals(1, sprints.get());
    assertTrue(component.keyUp(Keys.SHIFT_LEFT));
    assertEquals(1, sprintStops.get());
  }
}
