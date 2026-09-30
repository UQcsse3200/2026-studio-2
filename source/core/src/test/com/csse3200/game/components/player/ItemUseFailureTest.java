package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class ItemUseFailureTest {
  private InventoryComponent inventory;
  private ItemUseComponent use;
  private Entity player;
  private final List<ItemType> failed = new ArrayList<>();
  private final List<ItemType> used = new ArrayList<>();

  @BeforeEach
  void setUp() {
    inventory = mock(InventoryComponent.class);
    use = new ItemUseComponent();
    player = new Entity().addComponent(inventory).addComponent(use);
    player.getEvents().<ItemType>addListener("itemUseFailed", failed::add);
    player.getEvents().<ItemType>addListener("itemUsed", used::add);
  }

  private void select(ItemType item) {
    when(inventory.getSelectedItem()).thenReturn(item);
    when(inventory.hasItem(item)).thenReturn(true);
  }

  private void assertRejected(ItemType item) {
    assertFalse(use.useSelectedItem());
    assertEquals(List.of(item), failed);
    assertTrue(used.isEmpty());
  }

  @Test
  void shouldNotHealWhenInventoryCannotRemovePotion() {
    CombatStatsComponent health = new CombatStatsComponent(40, 100, 1);
    player.addComponent(health);
    select(ItemType.HEALTH_POTION);
    use.create();
    assertRejected(ItemType.HEALTH_POTION);
    assertEquals(40, health.getHealth());
    verify(inventory).removeItem(ItemType.HEALTH_POTION, 1);
  }

  @Test
  void shouldNotConsumeSpeedPotionWhileBuffIsActive() {
    PlayerActions actions = mock(PlayerActions.class);
    when(actions.isSpeedPotionActive()).thenReturn(true);
    player.addComponent(actions);
    select(ItemType.SpeedPotion);
    use.create();
    assertRejected(ItemType.SpeedPotion);
    verify(inventory, never()).removeItem(any(), anyInt());
  }

  @Test
  void shouldNotPublishSpeedBuffWhenRemovalFails() {
    select(ItemType.SpeedPotion);
    use.create();
    List<Vector2> buffs = new ArrayList<>();
    player
        .getEvents()
        .addListener(
            "speedPotionUsed",
            (Float boost, Float duration) -> buffs.add(new Vector2(boost, duration)));
    assertRejected(ItemType.SpeedPotion);
    assertTrue(buffs.isEmpty());
    verify(inventory).removeItem(ItemType.SpeedPotion, 1);
  }

  @Test
  void shouldRejectPoisonThrowWithoutWorldServicesAndPreservePotion() {
    select(ItemType.PoisonPotion);
    use.create();
    assertRejected(ItemType.PoisonPotion);
    verify(inventory, never()).removeItem(any(), anyInt());
  }

  @Test
  void shouldNotSpawnPoisonProjectileWhenRemovalFails() {
    select(ItemType.PoisonPotion);
    EntityService entities = mock(EntityService.class);
    ServiceLocator.registerEntityService(entities);
    ServiceLocator.registerPhysicsService(mock(PhysicsService.class));
    use.create();
    assertRejected(ItemType.PoisonPotion);
    verify(entities, never()).register(any());
    verify(inventory).removeItem(ItemType.PoisonPotion, 1);
  }

  @Test
  void shouldRejectItemsThatDisappearBeforeDispatch() {
    use.create();
    // Inventory may change between selection validation and the action's own availability check.
    for (ItemType item :
        new ItemType[] {
          ItemType.STANDARD_ARROW, ItemType.Sword, ItemType.SpeedPotion, ItemType.PoisonPotion
        }) {
      select(item);
      when(inventory.hasItem(item)).thenReturn(true, false);
      failed.clear();
      assertRejected(item);
      verify(inventory, never()).removeItem(item, 1);
    }
  }

  @Test
  void shouldFireGrappleEvenWhenReleaseSoundCannotLoad() {
    select(ItemType.ROPE_ARROW);
    ResourceService resources = mock(ResourceService.class);
    when(resources.getAsset(eq("sounds/Arrow_release.wav"), any()))
        .thenThrow(new IllegalStateException("Audio unavailable"));
    ServiceLocator.registerResourceService(resources);
    use.create();
    List<Vector2> shots = new ArrayList<>();
    player.getEvents().<Vector2>addListener("grappleFire", shots::add);
    use.shootSelectedArrow(Vector2.X.cpy());
    assertEquals(List.of(Vector2.X), shots);
    assertEquals(List.of(ItemType.ROPE_ARROW), used);
    assertTrue(failed.isEmpty());
    verify(inventory, never()).removeItem(any(), anyInt());
  }
}
