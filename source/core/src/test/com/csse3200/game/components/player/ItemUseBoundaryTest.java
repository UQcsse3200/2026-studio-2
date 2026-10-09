package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.item.weapons.WeaponComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class ItemUseBoundaryTest {
  @Test
  void shouldIgnoreUseShootAndReleaseWithoutInventory() {
    ItemUseComponent use = new ItemUseComponent();
    Entity player = new Entity().addComponent(use);
    player.create();
    List<String> actions = new ArrayList<>();
    player.getEvents().addListener("grappleRelease", () -> actions.add("release"));
    player.getEvents().<Vector2>addListener("chargeStart", ignored -> actions.add("charge"));
    player.getEvents().<ItemType>addListener("itemUsed", ignored -> actions.add("used"));
    assertFalse(use.useSelectedItem());
    use.shootSelectedArrow(new Vector2(1, 0));
    use.stopShootSelectedArrow();
    assertTrue(actions.isEmpty());
  }

  @Test
  void shouldRejectStaleSelectionWithoutPublishingAnAttack() {
    // A failed inventory transaction may leave a stale selected type during dispatch.
    InventoryComponent inventory = mock(InventoryComponent.class);
    when(inventory.getSelectedItem()).thenReturn(ItemType.FIRE_ARROW);
    ItemUseComponent use = new ItemUseComponent();
    Entity player = new Entity().addComponent(inventory).addComponent(use);
    use.create();
    List<ItemType> used = new ArrayList<>();
    player.getEvents().<ItemType>addListener("itemUsed", used::add);
    assertFalse(use.useSelectedItem());
    use.shootSelectedArrow(new Vector2(1, 0));
    assertTrue(used.isEmpty());
    verify(inventory, never()).removeItem(any(), anyInt());
  }

  @Test
  void shouldRejectNullAndZeroShootDirectionsWithoutSpendingArrows() {
    InventoryComponent inventory = new InventoryComponent(0);
    inventory.addItem(ItemType.FIRE_ARROW, 2);
    ItemUseComponent use = new ItemUseComponent();
    Entity player = new Entity().addComponent(inventory).addComponent(use);
    player.create();
    List<ItemType> failed = new ArrayList<>();
    player.getEvents().<ItemType>addListener("itemUseFailed", failed::add);
    use.shootSelectedArrow(null);
    use.shootSelectedArrow(new Vector2());
    assertEquals(List.of(ItemType.FIRE_ARROW, ItemType.FIRE_ARROW), failed);
    assertEquals(2, inventory.getItemCount(ItemType.FIRE_ARROW));
  }

  @Test
  void shouldRejectBowUseAndChargeWithNoPrimaryWeapon() {
    for (boolean equippedComponent : new boolean[] {false, true}) {
      InventoryComponent inventory = new InventoryComponent(0);
      inventory.addItem(ItemType.STANDARD_ARROW, 2);
      ItemUseComponent use = new ItemUseComponent();
      Entity player = new Entity().addComponent(inventory).addComponent(use);
      if (equippedComponent) {
        player.addComponent(new WeaponComponent());
      }
      player.create();
      List<ItemType> failed = new ArrayList<>();
      player.getEvents().<ItemType>addListener("itemUseFailed", failed::add);
      assertFalse(use.useSelectedItem());
      use.shootSelectedArrow(new Vector2(1, 0));
      assertEquals(List.of(ItemType.STANDARD_ARROW, ItemType.STANDARD_ARROW), failed);
      assertEquals(2, inventory.getItemCount(ItemType.STANDARD_ARROW));
    }
  }

  @Test
  void shouldRejectHealingWithoutCombatStatsAndPreservePotion() {
    InventoryComponent inventory = new InventoryComponent(0);
    inventory.addItem(ItemType.HEALTH_POTION, 1);
    ItemUseComponent use = new ItemUseComponent();
    Entity player = new Entity().addComponent(inventory).addComponent(use);
    player.create();
    List<ItemType> failed = new ArrayList<>();
    player.getEvents().<ItemType>addListener("itemUseFailed", failed::add);
    assertFalse(use.useSelectedItem());
    assertEquals(List.of(ItemType.HEALTH_POTION), failed);
    assertEquals(1, inventory.getItemCount(ItemType.HEALTH_POTION));
  }

  @Test
  void shouldRejectPoisonThrowWithoutPhysicsEvenWhenEntityServiceExists() {
    ServiceLocator.registerEntityService(new EntityService());
    InventoryComponent inventory = new InventoryComponent(0);
    inventory.addItem(ItemType.PoisonPotion, 1);
    ItemUseComponent use = new ItemUseComponent();
    Entity player = new Entity().addComponent(inventory).addComponent(use);
    player.create();
    List<ItemType> failed = new ArrayList<>();
    player.getEvents().<ItemType>addListener("itemUseFailed", failed::add);
    assertFalse(use.useSelectedItem());
    assertEquals(List.of(ItemType.PoisonPotion), failed);
    assertEquals(1, inventory.getItemCount(ItemType.PoisonPotion));
    assertTrue(ServiceLocator.getEntityService().getEntities().isEmpty());
  }

  @Test
  void shouldApplySpeedBuffWhenPlayerActionsExistsWithoutAnActiveBuff() {
    InventoryComponent inventory = new InventoryComponent(0);
    inventory.addItem(ItemType.SpeedPotion, 1);
    ItemUseComponent use = new ItemUseComponent();
    // PlayerActions owns physics/input state; this test isolates its inactive-buff query.
    PlayerActions actions = mock(PlayerActions.class);
    Entity player = new Entity().addComponent(inventory).addComponent(actions).addComponent(use);
    use.create();
    List<Vector2> buffs = new ArrayList<>();
    player
        .getEvents()
        .addListener(
            "speedPotionUsed",
            (Float boost, Float duration) -> buffs.add(new Vector2(boost, duration)));
    assertTrue(use.useSelectedItem());
    assertEquals(List.of(new Vector2(0.7f, 3f)), buffs);
    assertEquals(0, inventory.getItemCount(ItemType.SpeedPotion));
  }

  @Test
  void shouldUseDefaultAimWhenCameraMissingAimsAtPlayerOrThrows() {
    Input original = Gdx.input;
    try {
      Gdx.input = mock(Input.class);
      InventoryComponent inventory = new InventoryComponent(0);
      inventory.addItem(ItemType.ROPE_ARROW, 1);
      KeyboardPlayerInputComponent input = new KeyboardPlayerInputComponent();
      ItemUseComponent use = new ItemUseComponent();
      Entity player = new Entity().addComponent(inventory).addComponent(input).addComponent(use);
      use.create();
      List<Vector2> shots = new ArrayList<>();
      player.getEvents().<Vector2>addListener("grappleFire", shots::add);
      assertTrue(use.useSelectedItem());
      Camera camera = mock(Camera.class);
      when(camera.unproject(any(Vector3.class))).thenReturn(new Vector3(0.5f, 0.5f, 0));
      input.setCameraComponent(new CameraComponent(camera));
      assertTrue(use.useSelectedItem());
      when(camera.unproject(any(Vector3.class)))
          .thenThrow(new IllegalStateException("camera unavailable"));
      assertTrue(use.useSelectedItem());
      assertEquals(List.of(new Vector2(1, 0), new Vector2(1, 0), new Vector2(1, 0)), shots);
      assertEquals(1, inventory.getItemCount(ItemType.ROPE_ARROW));
    } finally {
      Gdx.input = original;
    }
  }
}
