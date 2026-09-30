package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.item.weapons.PrimaryWeapon;
import com.csse3200.game.components.item.weapons.WeaponComponent;
import com.csse3200.game.components.npc.ShopNpcComponent;
import com.csse3200.game.components.projectile.ArrowType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.events.listeners.EventListener0;
import com.csse3200.game.events.listeners.EventListener1;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.input.InputService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class KeyboardPlayerInputComponentTest {
  private Camera camera;
  private EntityService entityService;

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
    entityService = mock(EntityService.class);
    ServiceLocator.registerEntityService(entityService);
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
    PrimaryWeapon primary = mock(PrimaryWeapon.class);
    when(primary.isReady()).thenReturn(true);
    KeyboardPlayerInputComponent component = new KeyboardPlayerInputComponent();
    InventoryComponent inventory = new InventoryComponent(0);
    Entity player =
        new Entity()
            .addComponent(component)
            .addComponent(inventory)
            .addComponent(new WeaponComponent(primary))
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
  void shouldStartAndStopGrappleClimbWithW() {
    KeyboardPlayerInputComponent component = new KeyboardPlayerInputComponent();
    Entity player = new Entity().addComponent(component);
    AtomicInteger climbStarts = new AtomicInteger();
    AtomicInteger climbStops = new AtomicInteger();

    player.getEvents().addListener("grappleClimbStart", climbStarts::incrementAndGet);
    player.getEvents().addListener("grappleClimbStop", climbStops::incrementAndGet);

    assertTrue(component.keyDown(Keys.W));
    assertEquals(1, climbStarts.get());
    assertTrue(component.keyUp(Keys.W));
    assertEquals(1, climbStops.get());
  }

  @Test
  void shouldStartAndStopGrappleDescentWithS() {
    KeyboardPlayerInputComponent component = new KeyboardPlayerInputComponent();
    Entity player = new Entity().addComponent(component);
    AtomicInteger descentStarts = new AtomicInteger();
    AtomicInteger descentStops = new AtomicInteger();

    player.getEvents().addListener("grappleDescendStart", descentStarts::incrementAndGet);
    player.getEvents().addListener("grappleDescendStop", descentStops::incrementAndGet);

    assertTrue(component.keyDown(Keys.S));
    assertEquals(1, descentStarts.get());
    assertTrue(component.keyUp(Keys.S));
    assertEquals(1, descentStops.get());
  }

  @Test
  @SuppressWarnings("unchecked")
  void shouldPreserveLedgeDropAlongsideGrappleDescent() {
    KeyboardPlayerInputComponent component = new KeyboardPlayerInputComponent();
    Entity player = new Entity().addComponent(component);
    EventListener0 descend = mock(EventListener0.class);
    EventListener0 stop = mock(EventListener0.class);
    EventListener1<Boolean> drop = mock(EventListener1.class);
    player.getEvents().addListener("grappleDescendStart", descend);
    player.getEvents().addListener("grappleDescendStop", stop);
    player.getEvents().addListener("updateLedgeDrop", drop);

    assertTrue(component.keyDown(Keys.S));
    verify(descend).handle();
    verify(drop).handle(true);
    assertTrue(component.keyUp(Keys.S));
    verify(stop).handle();
    // Physics clears the drop flag after passing through the ledge, not on key release.
    verifyNoMoreInteractions(drop);
  }

  @Test
  void shouldClearVerticalCheatInputAfterReleasingGrappleKeys() {
    KeyboardPlayerInputComponent component = new KeyboardPlayerInputComponent();
    PhysicsComponent physics = mock(PhysicsComponent.class);
    Body body = mock(Body.class);
    when(physics.getBody()).thenReturn(body);
    when(body.getWorldCenter()).thenReturn(new Vector2());
    new Entity().addComponent(component).addComponent(physics);
    component.toggleCheats();

    component.keyDown(Keys.W);
    verify(body).applyLinearImpulse(new Vector2(0, 10f), new Vector2(), true);
    component.keyUp(Keys.W);
    component.keyDown(Keys.S);
    verify(body).applyLinearImpulse(new Vector2(0, -10f), new Vector2(), true);
    component.keyUp(Keys.S);
    clearInvocations(body);

    component.keyDown(Keys.D);
    component.keyUp(Keys.D);
    verifyNoMoreInteractions(body);
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

  @Test
  void shouldOpenAndCloseTheArrowWheelWithTab() {
    KeyboardPlayerInputComponent component = new KeyboardPlayerInputComponent();
    ArrowWheelComponent wheel = new ArrowWheelComponent();
    Entity player = new Entity().addComponent(component).addComponent(wheel);
    wheel.create();

    assertTrue(component.keyDown(Keys.TAB));
    assertTrue(wheel.isOpen());

    assertTrue(component.keyUp(Keys.TAB));
    assertFalse(wheel.isOpen());
  }

  @Test
  void shouldHighlightFromThePointerOffsetToTheScreenCentre() {
    Graphics graphics = mock(Graphics.class);
    when(graphics.getWidth()).thenReturn(800);
    when(graphics.getHeight()).thenReturn(600);
    Gdx.graphics = graphics;
    KeyboardPlayerInputComponent component = new KeyboardPlayerInputComponent();
    ArrowWheelComponent wheel = new ArrowWheelComponent();
    Entity player = new Entity().addComponent(component).addComponent(wheel);
    wheel.create();
    component.keyDown(Keys.TAB);

    assertFalse(component.mouseMoved(400, 100));

    assertEquals(ArrowType.STANDARD, wheel.getHighlighted());
  }

  @Test
  void shouldBlockWeaponInputWhileTheArrowWheelIsOpen() {
    KeyboardPlayerInputComponent component = new KeyboardPlayerInputComponent();
    ArrowWheelComponent wheel = new ArrowWheelComponent();
    Entity player = new Entity().addComponent(component).addComponent(wheel);
    player.setPosition(0f, 0f);
    wheel.create();
    component.setCameraComponent(new CameraComponent(camera));
    AtomicInteger melee = new AtomicInteger();
    player.getEvents().addListener("melee", (Vector2 ignored) -> melee.incrementAndGet());

    component.keyDown(Keys.TAB);
    assertFalse(component.touchDown(4, 2, 0, Buttons.LEFT));
    assertEquals(0, melee.get());

    component.keyUp(Keys.TAB);
    assertTrue(component.touchDown(4, 2, 0, Buttons.LEFT));
    assertEquals(1, melee.get());
  }

  @Test
  void shouldIgnoreGameplayKeysWhileShopIsOpen() {
    ServiceLocator.registerEntityService(new EntityService());
    KeyboardPlayerInputComponent component = new KeyboardPlayerInputComponent();
    InventoryComponent inventory = new InventoryComponent(0);
    PlayerInteractionComponent interaction = new PlayerInteractionComponent();
    Entity player =
        new Entity()
            .addComponent(component)
            .addComponent(inventory)
            .addComponent(new ItemUseComponent())
            .addComponent(interaction);
    player.setPosition(0f, 0f);
    component.setCameraComponent(new CameraComponent(camera));
    inventory.addItem(ItemType.STANDARD_ARROW, 2);
    player.getComponent(ItemUseComponent.class).create();
    interaction.create();

    Entity shopNpc = new Entity().addComponent(new ShopNpcComponent());
    shopNpc.setPosition(0.5f, 0f);
    ServiceLocator.getEntityService().register(shopNpc);

    AtomicInteger jumps = new AtomicInteger();
    player.getEvents().addListener("jump", jumps::incrementAndGet);

    assertTrue(interaction.interact());
    assertTrue(interaction.isShopOpen());

    assertTrue(component.keyDown(Keys.E));
    assertEquals(2, inventory.getItemCount(ItemType.STANDARD_ARROW));
    assertTrue(component.keyDown(Keys.SPACE));
    assertEquals(0, jumps.get());
    assertTrue(component.touchDown(4, 2, 0, Buttons.LEFT));

    assertTrue(component.keyDown(Keys.F));
    assertFalse(interaction.isShopOpen());
  }

  @Test
  void shouldCancelChargeWhenShopOpensAndWhenRightMouseIsReleasedWhileShopIsOpen() {
    ServiceLocator.registerInputService(mock(InputService.class));
    ServiceLocator.registerEntityService(new EntityService());
    KeyboardPlayerInputComponent component = new KeyboardPlayerInputComponent();
    InventoryComponent inventory = new InventoryComponent(0);
    PlayerInteractionComponent interaction = new PlayerInteractionComponent();
    Entity player =
        new Entity()
            .addComponent(component)
            .addComponent(inventory)
            .addComponent(new ItemUseComponent())
            .addComponent(interaction);
    player.setPosition(0f, 0f);
    component.setCameraComponent(new CameraComponent(camera));
    player.create();

    Entity shopNpc = new Entity().addComponent(new ShopNpcComponent());
    shopNpc.setPosition(0.5f, 0f);
    ServiceLocator.getEntityService().register(shopNpc);

    AtomicInteger cancels = new AtomicInteger();
    AtomicInteger stops = new AtomicInteger();
    player.getEvents().addListener("chargeCancel", cancels::incrementAndGet);
    player.getEvents().addListener("stopShoot", stops::incrementAndGet);

    assertTrue(component.touchDown(4, 2, 0, Buttons.RIGHT));
    assertTrue(component.isRightMouseHeld());

    assertTrue(interaction.interact());
    assertTrue(interaction.isShopOpen());
    assertFalse(component.isRightMouseHeld());
    assertEquals(1, cancels.get());
    assertEquals(0, stops.get());

    assertTrue(component.touchUp(4, 2, 0, Buttons.RIGHT));
    assertEquals(2, cancels.get());
    assertEquals(0, stops.get());

    when(Gdx.input.isButtonPressed(Buttons.RIGHT)).thenReturn(false);
    assertTrue(interaction.interact());
    assertFalse(interaction.isShopOpen());
    assertEquals(2, cancels.get());
    assertEquals(0, stops.get());
  }

  @Test
  void shouldCancelSwallowedChargeOnShopCloseWhenOpenDidNotClearIt() {
    ServiceLocator.registerInputService(mock(InputService.class));
    KeyboardPlayerInputComponent component = new KeyboardPlayerInputComponent();
    Entity player = new Entity().addComponent(component);
    player.setPosition(0f, 0f);
    component.setCameraComponent(new CameraComponent(camera));
    player.create();

    AtomicInteger cancels = new AtomicInteger();
    AtomicInteger stops = new AtomicInteger();
    player.getEvents().addListener("chargeCancel", cancels::incrementAndGet);
    player.getEvents().addListener("stopShoot", stops::incrementAndGet);

    assertTrue(component.touchDown(4, 2, 0, Buttons.RIGHT));
    assertTrue(component.isRightMouseHeld());

    when(Gdx.input.isButtonPressed(Buttons.RIGHT)).thenReturn(false);
    player.getEvents().trigger("closeShop");

    assertFalse(component.isRightMouseHeld());
    assertEquals(1, cancels.get());
    assertEquals(0, stops.get());
  }
}
