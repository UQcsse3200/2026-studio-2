package com.csse3200.game.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ItemDrop;
import com.csse3200.game.entities.factories.ItemFactory;
import com.csse3200.game.events.listeners.EventListener1;
import com.csse3200.game.extensions.GameExtension;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

/** Loot an enemy drops as its health falls past set points. */
@ExtendWith(GameExtension.class)
class EnemyItemDropComponentTest {
  private Application originalApp;
  private MockedStatic<ItemFactory> itemFactory;
  private final List<Entity> spawned = new ArrayList<>();
  private final List<Entity> madeItems = new ArrayList<>();

  @BeforeEach
  void setUp() {
    // Drops are spawned through Gdx.app.postRunnable: run those at once so the test can see them
    originalApp = Gdx.app;
    Application app = mock(Application.class);
    doAnswer(
            call -> {
              ((Runnable) call.getArgument(0)).run();
              return null;
            })
        .when(app)
        .postRunnable(any(Runnable.class));
    Gdx.app = app;

    // Making a real item needs textures and lights: what is dropped is all that matters here
    itemFactory = mockStatic(ItemFactory.class);
    itemFactory
        .when(() -> ItemFactory.createItem(anyString(), anyInt()))
        .thenAnswer(
            call -> {
              Entity item = new Entity();
              madeItems.add(item);
              return item;
            });
  }

  @AfterEach
  void tearDown() {
    itemFactory.close();
    Gdx.app = originalApp;
  }

  private static ItemDrop drop(String item, int atHealth, int quantity) {
    ItemDrop drop = new ItemDrop();
    drop.item = item;
    drop.dropHealth = atHealth;
    drop.quantity = quantity;
    return drop;
  }

  private Entity enemyWith(int health, ItemDrop... drops) {
    Entity enemy =
        new Entity()
            .addComponent(new CombatStatsComponent(health, 1))
            .addComponent(new EnemyItemDropComponent(List.of(drops)));
    enemy.setPosition(7f, 3f);
    enemy.create();
    EventListener1<Entity> collectSpawned = spawned::add;
    enemy.getEvents().addListener("spawnEntity", collectSpawned);
    return enemy;
  }

  private static void healthBecomes(Entity enemy, int health) {
    enemy.getEvents().trigger("updateHealth", health);
  }

  @Test
  void shouldDropItsLootWhenHealthFallsToTheDropPoint() {
    Entity enemy = enemyWith(10, drop("healthPotion", 5, 2));

    healthBecomes(enemy, 5);

    itemFactory.verify(() -> ItemFactory.createItem("healthPotion", 2));
    assertEquals(1, spawned.size());
    assertSame(madeItems.get(0), spawned.get(0));
  }

  @Test
  void shouldDropItWhereTheEnemyIs() {
    Entity enemy = enemyWith(10, drop("fireArrow", 5, 1));
    enemy.setPosition(12f, 8f);

    healthBecomes(enemy, 3);

    assertEquals(enemy.getPosition(), spawned.get(0).getPosition());
    assertEquals(12f, spawned.get(0).getPosition().x);
    assertEquals(8f, spawned.get(0).getPosition().y);
  }

  @Test
  void shouldDropWhenHealthFallsRightPastThePoint() {
    Entity enemy = enemyWith(10, drop("healthPotion", 5, 1));

    healthBecomes(enemy, 1);

    assertEquals(1, spawned.size());
  }

  @Test
  void shouldNotDropWhileHealthIsStillAboveThePoint() {
    Entity enemy = enemyWith(10, drop("healthPotion", 5, 1));

    healthBecomes(enemy, 9);
    healthBecomes(enemy, 6);

    assertTrue(spawned.isEmpty());
    itemFactory.verify(() -> ItemFactory.createItem(anyString(), anyInt()), never());
  }

  @Test
  void shouldDropOnlyOnceWhenHealthKeepsFalling() {
    Entity enemy = enemyWith(10, drop("healthPotion", 5, 1));

    healthBecomes(enemy, 5);
    healthBecomes(enemy, 3);
    healthBecomes(enemy, 1);

    assertEquals(1, spawned.size(), "it only drops as health crosses the point, not below it");
  }

  @Test
  void shouldNotDropWhenAlreadyAtOrBelowThePointToBeginWith() {
    Entity enemy = enemyWith(5, drop("healthPotion", 5, 1));

    healthBecomes(enemy, 2);

    assertTrue(spawned.isEmpty(), "it started at the point, so it never crossed it");
  }

  @Test
  void shouldDropEveryItemWhoseHealthPointWasPassedInOneHit() {
    Entity enemy =
        enemyWith(10, drop("healthPotion", 8, 1), drop("fireArrow", 5, 3), drop("iceArrow", 2, 4));

    healthBecomes(enemy, 4); // passes 8 and 5, but not 2

    itemFactory.verify(() -> ItemFactory.createItem("healthPotion", 1));
    itemFactory.verify(() -> ItemFactory.createItem("fireArrow", 3));
    itemFactory.verify(() -> ItemFactory.createItem("iceArrow", 4), never());
    assertEquals(2, spawned.size());
  }

  @Test
  void shouldDropEachItemAsItsOwnPointIsReached() {
    Entity enemy = enemyWith(10, drop("healthPotion", 8, 1), drop("fireArrow", 4, 1));

    healthBecomes(enemy, 7);
    assertEquals(1, spawned.size(), "the first point passed");

    healthBecomes(enemy, 3);
    assertEquals(2, spawned.size(), "and then the second");
  }

  @Test
  void shouldNotDropWhenHealthGoesUp() {
    Entity enemy = enemyWith(10, drop("healthPotion", 5, 1));

    healthBecomes(enemy, 15);

    assertTrue(spawned.isEmpty());
  }

  @Test
  void shouldDropAgainIfItHealsAboveThePointAndFallsBelowItAgain() {
    Entity enemy = enemyWith(10, drop("healthPotion", 5, 1));
    healthBecomes(enemy, 4); // first drop
    healthBecomes(enemy, 9); // heals back

    healthBecomes(enemy, 5);

    verifyCreated("healthPotion", 2);
    assertEquals(2, spawned.size());
  }

  @Test
  void shouldDoNothingWithNoLootConfigured() {
    Entity enemy = enemyWith(10);

    healthBecomes(enemy, 1);

    assertTrue(spawned.isEmpty());
  }

  private void verifyCreated(String item, int times) {
    itemFactory.verify(() -> ItemFactory.createItem(eq(item), anyInt()), times(times));
  }
}
