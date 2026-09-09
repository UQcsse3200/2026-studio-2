package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ItemDrop;
import com.csse3200.game.entities.factories.ItemFactory;
import com.csse3200.game.services.ServiceLocator;
import java.util.List;

/**
 * Controls item dropping for Enemy Class.
 *
 * <p>EnemyItemDropComponent listens for health updates on a given enemy and checks its config for
 * any items to be dropped at the updated health, acting accordingly.
 */
public class EnemyItemDropComponent extends Component {

  private final List<ItemDrop> itemDrops;

  public EnemyItemDropComponent(List<ItemDrop> itemDrops) {
    this.itemDrops = itemDrops;
  }

  // Store enemy previous health for determining health change
  private int prevHealth;

  /* Create, with a listener for health updates. */
  @Override
  public void create() {
    prevHealth = entity.getComponent(CombatStatsComponent.class).getHealth();
    entity.getEvents().addListener("updateHealth", this::onHealthUpdate);
  }

  /* Execute enemy behaviour for a given health update. */
  private void onHealthUpdate(int enemyHealth) {

    // Loop itemDrops in enemy config
    for (ItemDrop drop : itemDrops) {
      // Check against health for any items to drop
      if ((prevHealth > drop.dropHealth) && (enemyHealth <= drop.dropHealth)) {
        // Drop the item
        Gdx.app.postRunnable(
            () -> {
              Entity item = ItemFactory.createItem(drop.item, drop.quantity);
              item.setPosition(entity.getPosition());
              ServiceLocator.getEntityService().register(item);
            });
      }
    }

    prevHealth = enemyHealth;
  }
}
