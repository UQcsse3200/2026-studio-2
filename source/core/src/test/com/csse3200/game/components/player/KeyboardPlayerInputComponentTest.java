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
  private Camera camera;

  @BeforeEach
  void setUp() {
    Gdx.input = mock(Input.class);
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
    player.getEvents().addListener("stopShoot", stops::incrementAndGet);

    assertTrue(component.touchUp(4, 2, 0, Buttons.RIGHT));
    assertEquals(1, stops.get());
  }

  @Test
  void shouldSignalStopMeleeOnLeftRelease() {
    Entity player = new Entity();
    KeyboardPlayerInputComponent component = aimedComponent(player);

    AtomicInteger stops = new AtomicInteger();
    player.getEvents().addListener("stopMelee", stops::incrementAndGet);

    assertTrue(component.touchUp(4, 2, 0, Buttons.LEFT));
    assertEquals(1, stops.get());
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
  void shouldNotHandleQ() {
    Entity player = new Entity();
    KeyboardPlayerInputComponent component = aimedComponent(player);

    AtomicInteger events = new AtomicInteger();
    player.getEvents().addListener("cycleArrow", events::incrementAndGet);

    assertFalse(component.keyDown(Keys.Q));
    assertEquals(0, events.get());
  }

  @Test
  void shouldUseSelectedItemOncePerEPress() {
    KeyboardPlayerInputComponent component = new KeyboardPlayerInputComponent();
    InventoryComponent inventory = new InventoryComponent(0);
    Entity player =
        new Entity()
            .addComponent(component)
            .addComponent(inventory)
            .addComponent(new ItemUseComponent());
    player.setPosition(0f, 0f);
    Entity cameraEntity = new Entity().addComponent(new CameraComponent(camera));
    component.setCameraComponent(cameraEntity.getComponent(CameraComponent.class));
    inventory.addItem(ItemType.STANDARD_ARROW, 2);
    player.getComponent(ItemUseComponent.class).create();

    AtomicInteger shots = new AtomicInteger();
    AtomicReference<Vector2> direction = new AtomicReference<>();
    player
        .getEvents()
        .addListener(
            "primaryAttack",
            (Vector2 aimDirection) -> {
              shots.incrementAndGet();
              direction.set(aimDirection);
            });

    assertTrue(component.keyDown(Keys.E));
    assertTrue(component.keyDown(Keys.E));
    assertEquals(1, shots.get());
    assertEquals(1, inventory.getItemCount(ItemType.STANDARD_ARROW));
    assertTrue(direction.get().epsilonEquals(new Vector2(9.5f, 4.5f)));

    assertTrue(component.keyUp(Keys.E));
    assertTrue(component.keyDown(Keys.E));
    assertEquals(2, shots.get());
    assertEquals(0, inventory.getItemCount(ItemType.STANDARD_ARROW));
  }

  @Test
  void shouldNotAttackWhenItemUseComponentIsMissing() {
    KeyboardPlayerInputComponent component = new KeyboardPlayerInputComponent();
    Entity player = new Entity().addComponent(component);
    player.setPosition(0f, 0f);
    component.setCameraComponent(new CameraComponent(camera));
    AtomicInteger shots = new AtomicInteger();
    player.getEvents().addListener("primaryAttack", (Vector2 ignored) -> shots.incrementAndGet());

    assertTrue(component.keyDown(Keys.E));
    assertEquals(0, shots.get());
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
  void shouldDeleteItemWithDeleteInsteadOfX() {
    KeyboardPlayerInputComponent component = new KeyboardPlayerInputComponent();
    Entity player = new Entity().addComponent(component);
    AtomicInteger deletions = new AtomicInteger();
    player.getEvents().addListener("deleteItem", deletions::incrementAndGet);

    assertTrue(component.keyDown(Keys.FORWARD_DEL));
    assertEquals(1, deletions.get());

    assertFalse(component.keyDown(Keys.X));
    assertEquals(1, deletions.get());
  }
}
