package com.csse3200.game.components.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.EnemyFactory;
import com.csse3200.game.extensions.GameExtension;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

/** A necromancer calling up skeleton warriors beside itself when the player is close. */
@ExtendWith(GameExtension.class)
class SummonTaskTest {
  private static final int PRIORITY = 30;
  private static final float RANGE = 6f;
  private static final float COOLDOWN = 5f;

  private TaskTestWorld world;
  private Entity player;
  private Entity necromancer;
  private MockedStatic<EnemyFactory> factory;
  private final List<Entity> summoned = new ArrayList<>();
  private final List<Entity> spawned = new ArrayList<>();

  @BeforeEach
  void setUp() {
    world = new TaskTestWorld();
    player = new Entity();
    player.setPosition(3f, 0f);
    necromancer = world.enemyAt(10f, 4f);
    necromancer.getEvents().addListener("spawnEntity", (Entity entity) -> spawned.add(entity));

    // Making a real skeleton needs the whole asset pipeline: only what gets summoned matters here
    factory = mockStatic(EnemyFactory.class);
    factory
        .when(() -> EnemyFactory.createSkeletonWarrior(player))
        .thenAnswer(
            call -> {
              Entity skeleton = new Entity();
              summoned.add(skeleton);
              return skeleton;
            });
  }

  @AfterEach
  void tearDown() {
    factory.close();
  }

  private SummonTask task() {
    SummonTask task = new SummonTask(player, PRIORITY, RANGE, COOLDOWN);
    task.create(() -> necromancer);
    return task;
  }

  // --- deciding to summon ---

  @Test
  void shouldWaitOutTheCooldownFromTheStartOfTheGame() {
    player.setPosition(10f, 4f); // right beside it
    SummonTask task = task();

    assertEquals(-1, task.getPriority(), "the clock starts at zero, so it is on cooldown");

    world.advance((long) (COOLDOWN * 1000));
    assertEquals(PRIORITY, task.getPriority());
  }

  @Test
  void shouldNotSummonForAPlayerOutOfRange() {
    SummonTask task = task();
    world.advance(10_000);
    player.setPosition(10f + RANGE + 1f, 4f);

    assertEquals(-1, task.getPriority());
  }

  @Test
  void shouldSummonForAPlayerExactlyAtTheEdgeOfRange() {
    SummonTask task = task();
    world.advance(10_000);
    player.setPosition(10f - RANGE, 4f);

    assertEquals(PRIORITY, task.getPriority());
  }

  // --- summoning ---

  @Test
  void shouldSummonASkeletonWarriorAgainstThePlayer() {
    SummonTask task = task();
    world.advance(10_000);

    task.update();

    factory.verify(() -> EnemyFactory.createSkeletonWarrior(player), times(1));
    assertEquals(1, spawned.size());
    assertSame(summoned.get(0), spawned.get(0));
  }

  @Test
  void shouldPlaceTheSkeletonOneUnitToTheRightOfTheNecromancer() {
    SummonTask task = task();
    world.advance(10_000);

    task.update();

    assertEquals(11f, spawned.get(0).getPosition().x, 1e-4f);
    assertEquals(4f, spawned.get(0).getPosition().y, 1e-4f);
  }

  @Test
  void shouldNotSummonBeforeTheCooldownIsOver() {
    SummonTask task = task();
    world.advance((long) (COOLDOWN * 1000) - 1);

    task.update();

    assertTrue(spawned.isEmpty());
    factory.verify(() -> EnemyFactory.createSkeletonWarrior(player), never());
  }

  @Test
  void shouldSummonAgainOnlyAfterEachCooldown() {
    SummonTask task = task();
    world.advance(10_000);
    task.update(); // first

    world.advance((long) (COOLDOWN * 1000) - 1);
    task.update();
    assertEquals(1, spawned.size(), "not yet");

    world.advance(1);
    task.update();
    assertEquals(2, spawned.size(), "a second one");
  }

  @Test
  void shouldGoOnCooldownStraightAfterSummoning() {
    player.setPosition(10f, 4f);
    SummonTask task = task();
    world.advance(10_000);
    assertEquals(PRIORITY, task.getPriority());

    task.update();

    assertEquals(-1, task.getPriority());
  }

  @Test
  void shouldNotCheckTheRangeAgainOnceItHasBeenChosenToRun() {
    SummonTask task = task();
    world.advance(10_000);
    player.setPosition(100f, 100f); // far away: the task would not have been picked

    task.update();

    assertEquals(1, spawned.size(), "update trusts that the AI only runs it in range");
  }
}
