package com.csse3200.game.components.npc;

import static org.junit.jupiter.api.Assertions.*;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MerchantComponentTest {
  private Entity player;
  private InventoryComponent inventory;
  private MerchantComponent merchant;

  @BeforeEach
  void setup() {
    inventory = new InventoryComponent(50, 1);
    player = new Entity().addComponent(inventory).addComponent(new CombatStatsComponent(10, 0));
    player.setPosition(75, 37);
    merchant = new MerchantComponent(player);
    Entity npc = new Entity().addComponent(merchant);
    npc.setPosition(76, 37);
  }

  @Test
  void purchaseAddsBundleAndChargesOnce() {
    assertEquals(MerchantComponent.Result.PURCHASED, merchant.buy(0));
    assertEquals(10, inventory.getItemCount(ItemType.STANDARD_ARROW));
    assertEquals(45, inventory.getGold());
  }

  @Test
  void insufficientFundsChangesNothing() {
    inventory.setGold(4);
    assertEquals(MerchantComponent.Result.NOT_ENOUGH_GOLD, merchant.buy(0));
    assertEquals(0, inventory.getItemCount(ItemType.STANDARD_ARROW));
    assertEquals(4, inventory.getGold());
  }

  @Test
  void fullBackpackDoesNotCharge() {
    inventory.addItem(ItemType.ROPE_ARROW, 1);
    assertEquals(MerchantComponent.Result.INVENTORY_FULL, merchant.buy(0));
    assertEquals(50, inventory.getGold());
  }

  @Test
  void stackingExistingItemWorksInFullBackpack() {
    inventory.addItem(ItemType.STANDARD_ARROW, 2);
    assertEquals(MerchantComponent.Result.PURCHASED, merchant.buy(0));
    assertEquals(12, inventory.getItemCount(ItemType.STANDARD_ARROW));
  }

  @Test
  void remoteOrDeadPlayerCannotTrade() {
    player.setPosition(76, 47);
    assertEquals(MerchantComponent.Result.OUT_OF_RANGE, merchant.buy(1));
    player.setPosition(75, 37);
    player.getComponent(CombatStatsComponent.class).setHealth(0);
    assertEquals(MerchantComponent.Result.OUT_OF_RANGE, merchant.buy(1));
    assertEquals(50, inventory.getGold());
  }

  @Test
  void invalidOfferChangesNothing() {
    assertEquals(MerchantComponent.Result.INVALID_OFFER, merchant.buy(-1));
    assertEquals(MerchantComponent.Result.INVALID_OFFER, merchant.buy(99));
    assertEquals(50, inventory.getGold());
  }
}
