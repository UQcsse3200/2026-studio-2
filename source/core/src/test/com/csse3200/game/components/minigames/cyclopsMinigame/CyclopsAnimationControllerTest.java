package com.csse3200.game.components.minigames.cyclopsMinigame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class CyclopsAnimationControllerTest {
  private static final Vector2 HOME = new Vector2(20f, 5f);
  private static final float WIDTH = 10f;
  private static final float GROUND_Y = 0f;

  private GameTime gameTime;
  private EventHandler minigameEvents;
  private AnimationRenderComponent animator;
  private CyclopsAnimationController controller;
  private Entity cyclops;
  private Entity player;
  private final AtomicInteger hits = new AtomicInteger();

  @BeforeEach
  void beforeEach() {
    gameTime = mock(GameTime.class);
    ServiceLocator.registerTimeSource(gameTime);
    EntityService entityService = mock(EntityService.class);
    when(entityService.getPaused()).thenReturn(false);
    ServiceLocator.registerEntityService(entityService);
    minigameEvents = new EventHandler();
    ServiceLocator.registerCyclopsMinigameEventHandler(minigameEvents);
    minigameEvents.addListener(CyclopsMinigameLogic.CYCLOPS_HIT_EVENT, hits::incrementAndGet);

    TextureAtlas atlas = mock(TextureAtlas.class);
    String[] names = {"sleep", "idle", "charge_start", "jump", "attack"};
    for (String name : names) {
      Array<AtlasRegion> regions = new Array<>(1);
      regions.add(mock(AtlasRegion.class));
      when(atlas.findRegions(name)).thenReturn(regions);
    }
    animator = new AnimationRenderComponent(atlas);
    animator.addAnimation("sleep", 1f, PlayMode.LOOP);
    animator.addAnimation("idle", 1f, PlayMode.LOOP);
    animator.addAnimation("charge_start", 1f, PlayMode.NORMAL);
    animator.addAnimation("jump", 1f, PlayMode.LOOP);
    animator.addAnimation("attack", 1f, PlayMode.NORMAL);

    player = new Entity();
    player.setPosition(2f, GROUND_Y);
    controller = new CyclopsAnimationController(player, GROUND_Y);
    cyclops = new Entity().addComponent(animator).addComponent(controller);
    cyclops.setScale(WIDTH, WIDTH / 2f);
    cyclops.setPosition(HOME);
    controller.create();
  }

  private void step(float seconds) {
    when(gameTime.getDeltaTime()).thenReturn(seconds);
    animator.render(mock(SpriteBatch.class));
    controller.update();
  }

  private void runToPlayer() {
    minigameEvents.trigger(CyclopsMinigameLogic.CYCLOPS_WAKE_EVENT);
    step(2f);
    step(100f);
  }

  @Test
  void startsAsleep() {
    assertEquals("sleep", animator.getCurrentAnimation());
    assertEquals(CyclopsAnimationController.State.ASLEEP, controller.getState());
  }

  @Test
  void windsUpFacingThePlayerWhenTheyAreCaught() {
    minigameEvents.trigger(CyclopsMinigameLogic.CYCLOPS_WAKE_EVENT);

    assertEquals("charge_start", animator.getCurrentAnimation());
    assertFalse(animator.isFlipX());
  }

  @Test
  void jumpsAfterWindingUp() {
    minigameEvents.trigger(CyclopsMinigameLogic.CYCLOPS_WAKE_EVENT);
    step(2f);

    assertEquals("jump", animator.getCurrentAnimation());
    assertEquals(CyclopsAnimationController.State.JUMPING, controller.getState());
  }

  @Test
  void arcsThroughTheAirHalfwayThroughTheJump() {
    minigameEvents.trigger(CyclopsMinigameLogic.CYCLOPS_WAKE_EVENT);
    step(2f);
    step(CyclopsAnimationController.JUMP_DURATION / 2f);

    float straightLineY = (HOME.y + GROUND_Y) / 2f;
    assertEquals(
        straightLineY + CyclopsAnimationController.JUMP_HEIGHT, cyclops.getPosition().y, 0.001f);
    assertEquals("jump", animator.getCurrentAnimation());
  }

  @Test
  void stopsOnTheFloorWithHisClubReachingThePlayer() {
    runToPlayer();

    float expectedX = player.getCenterPosition().x + CyclopsAnimationController.CLUB_REACH * WIDTH;
    assertEquals(expectedX, cyclops.getCenterPosition().x, 0.001f);
    assertEquals(GROUND_Y, cyclops.getPosition().y, 0.001f);
    assertEquals("attack", animator.getCurrentAnimation());
  }

  @Test
  void hitsThePlayerOnceWhenHisClubLands() {
    runToPlayer();
    assertEquals(0, hits.get());

    step(CyclopsAnimationController.IMPACT_FRAME * CyclopsAnimationController.ATTACK_FRAME_TIME);
    assertEquals(1, hits.get());
    assertEquals("attack", animator.getCurrentAnimation());

    step(1f);
    step(1f);
    assertEquals(1, hits.get());
    assertEquals("idle", animator.getCurrentAnimation());
  }

  @Test
  void attacksFromTheLeftWhenThePlayerIsToHisRight() {
    player.setPosition(40f, GROUND_Y);
    runToPlayer();

    float expectedX = player.getCenterPosition().x - CyclopsAnimationController.CLUB_REACH * WIDTH;
    assertEquals(expectedX, cyclops.getCenterPosition().x, 0.001f);
    assertTrue(animator.isFlipX());
  }

  @Test
  void jumpsHomeAndFallsAsleepWhenSentBackToSleep() {
    runToPlayer();
    step(1f);

    minigameEvents.trigger(CyclopsMinigameLogic.CYCLOPS_SLEEP_EVENT);
    assertEquals("jump", animator.getCurrentAnimation());
    assertTrue(animator.isFlipX());

    step(100f);
    assertEquals(HOME, cyclops.getPosition());
    assertEquals("sleep", animator.getCurrentAnimation());
    assertFalse(animator.isFlipX());
  }

  @Test
  void announcesEachMomentOfTheAttackInOrder() {
    List<String> moments = new ArrayList<>();
    String[] events = {
      CyclopsAnimationController.WOKE_UP_EVENT,
      CyclopsAnimationController.LANDED_EVENT,
      CyclopsAnimationController.CLUB_LANDED_EVENT,
      CyclopsAnimationController.FELL_ASLEEP_EVENT
    };
    for (String event : events) {
      cyclops.getEvents().addListener(event, () -> moments.add(event));
    }

    runToPlayer();
    step(1f);
    step(1f);
    minigameEvents.trigger(CyclopsMinigameLogic.CYCLOPS_SLEEP_EVENT);
    step(100f);

    assertEquals(
        List.of(
            CyclopsAnimationController.WOKE_UP_EVENT,
            CyclopsAnimationController.LANDED_EVENT,
            CyclopsAnimationController.CLUB_LANDED_EVENT,
            CyclopsAnimationController.LANDED_EVENT,
            CyclopsAnimationController.FELL_ASLEEP_EVENT),
        moments);
  }

  @Test
  void asleepHeSortsByHisPositionOnHisRock() {
    assertEquals(-HOME.y, controller.getZIndex(), 0.001f);
  }

  @Test
  void keepsTheFloorDepthForTheWholeJump() {
    minigameEvents.trigger(CyclopsMinigameLogic.CYCLOPS_WAKE_EVENT);
    assertEquals(-GROUND_Y, controller.getZIndex(), 0.001f);

    step(2f);
    int steps = 10;
    for (int i = 0; i < steps; i++) {
      step(CyclopsAnimationController.JUMP_DURATION / steps / 2f);
      assertTrue(cyclops.getPosition().y > GROUND_Y);
      assertEquals(-GROUND_Y, controller.getZIndex(), 0.001f);
    }
  }

  @Test
  void keepsTheFloorDepthWhileAttackingAndJumpingHome() {
    runToPlayer();
    assertEquals(-GROUND_Y, controller.getZIndex(), 0.001f);
    step(1f);
    step(1f);
    assertEquals(CyclopsAnimationController.State.AWAKE, controller.getState());
    assertEquals(-GROUND_Y, controller.getZIndex(), 0.001f);

    minigameEvents.trigger(CyclopsMinigameLogic.CYCLOPS_SLEEP_EVENT);
    step(CyclopsAnimationController.JUMP_DURATION / 2f);
    assertTrue(cyclops.getPosition().y > HOME.y);
    assertEquals(-GROUND_Y, controller.getZIndex(), 0.001f);
  }

  @Test
  void returnsToSortingByPositionOnceBackAsleep() {
    runToPlayer();
    step(1f);
    minigameEvents.trigger(CyclopsMinigameLogic.CYCLOPS_SLEEP_EVENT);
    step(100f);

    assertEquals(CyclopsAnimationController.State.ASLEEP, controller.getState());
    assertEquals(-HOME.y, controller.getZIndex(), 0.001f);
  }

  @Test
  void drivesAnAnimatorSubclassHandedToIt() {
    // Wired as in CyclopsMinigameArea: the animator is a subclass that takes its depth from the
    // controller, so getComponent(AnimationRenderComponent.class) can't find it.
    TextureAtlas atlas = mock(TextureAtlas.class);
    Array<AtlasRegion> regions = new Array<>(1);
    regions.add(mock(AtlasRegion.class));
    when(atlas.findRegions("sleep")).thenReturn(regions);
    AnimationRenderComponent subclassAnimator =
        new AnimationRenderComponent(atlas) {
          @Override
          public float getZIndex() {
            return entity.getComponent(CyclopsAnimationController.class).getZIndex();
          }
        };
    subclassAnimator.addAnimation("sleep", 1f, PlayMode.LOOP);
    CyclopsAnimationController subclassController =
        new CyclopsAnimationController(player, GROUND_Y, subclassAnimator);
    Entity subclassCyclops =
        new Entity().addComponent(subclassAnimator).addComponent(subclassController);
    subclassCyclops.setPosition(HOME);

    subclassController.create();

    assertEquals("sleep", subclassAnimator.getCurrentAnimation());
    assertEquals(-HOME.y, subclassAnimator.getZIndex(), 0.001f);
  }

  @Test
  void sleepEventWhileAsleepLeavesHimOnHisRock() {
    minigameEvents.trigger(CyclopsMinigameLogic.CYCLOPS_SLEEP_EVENT);
    step(1f);

    assertEquals(HOME, cyclops.getPosition());
    assertEquals("sleep", animator.getCurrentAnimation());
  }
}
