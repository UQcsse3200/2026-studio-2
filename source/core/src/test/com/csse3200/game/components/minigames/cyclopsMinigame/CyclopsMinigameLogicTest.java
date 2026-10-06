package com.csse3200.game.components.minigames.cyclopsMinigame;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.BlankTransitionScreenCover;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
public class CyclopsMinigameLogicTest {
  CyclopsMinigameLogic minigameLogic;
  TimingBarLogic timingBarLogic;
  TimingBarDisplay timingBarDisplay;
  TerrainComponent terrainComponent;
  Entity player;

  GameTime gameTime;
  Input mockInput;

  @BeforeEach
  void setup() {
    mockInput = mock(Input.class);
    Gdx.input = mockInput;

    timingBarLogic = mock(TimingBarLogic.class);
    timingBarDisplay = mock(TimingBarDisplay.class);
    terrainComponent = mock(TerrainComponent.class);
    player = mock(Entity.class);

    ServiceLocator.registerResourceService(new ResourceService());
    ServiceLocator.registerEntityService(new EntityService());
    ServiceLocator.registerCyclopsMinigameEventHandler(new EventHandler());
    ServiceLocator.getCyclopsMinigameEventHandler().addListener("win", () -> {});

    gameTime = mock(GameTime.class);
    ServiceLocator.registerTimeSource(gameTime);

    minigameLogic =
        new CyclopsMinigameLogic(timingBarLogic, timingBarDisplay, terrainComponent, player);

    minigameLogic.transitionScreenCover = mock(BlankTransitionScreenCover.class);
    minigameLogic.walkingSound = mock(Sound.class);
    minigameLogic.missSound = mock(Sound.class);
    minigameLogic.hitSound = mock(Sound.class);
  }

  @AfterEach
  void teardown() {
    Gdx.input = null;
  }

  /* Checking Getters & Setters */

  @Test
  void shouldSetWinLocation() {
    GridPoint2 win = new GridPoint2(6, 7);
    minigameLogic.setWinLocation(win);

    assertEquals(win, minigameLogic.getWinLocation());
  }

  @Test
  void shouldSetLossLocations() {
    List<GridPoint2> lossLocations =
        List.of(
            new GridPoint2(1, 0),
            new GridPoint2(2, 0),
            new GridPoint2(3, 0),
            new GridPoint2(4, 0),
            new GridPoint2(4, 0));

    minigameLogic.setLossLocations(lossLocations);
    assertEquals(lossLocations, minigameLogic.getLossLocations());
  }

  @Test
  void shouldSetSafeLocations() {
    List<GridPoint2> safeLocations =
        List.of(
            new GridPoint2(4, 2), new GridPoint2(2, 7), new GridPoint2(1, 2), new GridPoint2(0, 8));

    minigameLogic.setSafeLocations(safeLocations);
    assertEquals(safeLocations, minigameLogic.getSafeLocations());
  }

  /* Checking Movement Function Logic */

  @Test
  void shouldMovePlayerToNextSafeLocationOnSuccess() {
    GridPoint2 start = new GridPoint2(1, 0);
    GridPoint2 next = new GridPoint2(2, 0);
    GridPoint2 win = new GridPoint2(3, 0);

    minigameLogic.setSafeLocations(List.of(start, next));
    minigameLogic.setWinLocation(win);

    Vector2 nextPos = new Vector2(200, 0);
    when(terrainComponent.tileToWorldPosition(next)).thenReturn(nextPos);

    minigameLogic.timingSuccess();
    verify(player).setPosition(nextPos);
  }

  @Test
  void shouldMovePlayerToNextLossLocationOnFailure() {
    GridPoint2 start = new GridPoint2(1, 0);
    GridPoint2 nextLoss = new GridPoint2(2, 0);
    GridPoint2 nextWin = new GridPoint2(3, 0);

    minigameLogic.setSafeLocations(List.of(start, nextWin));
    minigameLogic.setLossLocations(List.of(nextLoss));

    Vector2 nextLossPos = new Vector2(200, 0);
    when(terrainComponent.tileToWorldPosition(nextLoss)).thenReturn(nextLossPos);

    minigameLogic.timingFailure();
    verify(player).setPosition(nextLossPos);
  }

  @Test
  void shouldMovePlayerToWinLocationOnFinalSuccess() {
    GridPoint2 start = new GridPoint2(1, 0);
    GridPoint2 win = new GridPoint2(2, 0);

    minigameLogic.setSafeLocations(List.of(start));
    minigameLogic.setWinLocation(win);

    Vector2 winPos = new Vector2(200, 0);
    when(terrainComponent.tileToWorldPosition(win)).thenReturn(winPos);

    minigameLogic.timingSuccess();
    verify(player).setPosition(winPos);
  }

  @Test
  public void restartMinigameMovesPlayerBackToFirstLocation() {
    GridPoint2 start = new GridPoint2(1, 0);
    GridPoint2 next = new GridPoint2(2, 0);

    minigameLogic.setSafeLocations(List.of(start, next));

    Vector2 startPos = new Vector2(100, 0);
    when(terrainComponent.tileToWorldPosition(start)).thenReturn(startPos);

    Vector2 nextPos = new Vector2(200, 0);
    when(terrainComponent.tileToWorldPosition(next)).thenReturn(nextPos);

    minigameLogic.moveToNextLocation(true);
    verify(player).setPosition(nextPos);

    minigameLogic.restartMinigame();
    verify(player).setPosition(startPos);
  }

  /* Test State Changing */

  @Test
  void changeStateUpdatesTimerAndState() {
    minigameLogic.changeState(CyclopsMinigameLogic.State.STOP);
    assertEquals(CyclopsMinigameLogic.State.STOP, minigameLogic.state);
    assertEquals(0f, minigameLogic.timeInState);

    /* Update Once to change timInState */
    when(gameTime.getDeltaTime()).thenReturn(0.5f);

    minigameLogic.update();
    assertNotEquals(0f, minigameLogic.timeInState);
    assertTrue(minigameLogic.timeInState > 0f);

    minigameLogic.changeState(CyclopsMinigameLogic.State.MOVING);
    assertEquals(CyclopsMinigameLogic.State.MOVING, minigameLogic.state);
    assertEquals(0f, minigameLogic.timeInState);
  }

  @Test
  void testElapsedTimeIsCorrectlyEvaluated() {
    minigameLogic.changeState(CyclopsMinigameLogic.State.STOP);
    assertEquals(0f, minigameLogic.timeInState);

    when(gameTime.getDeltaTime()).thenReturn(2f);
    minigameLogic.update();
    assertFalse(minigameLogic.elapsed(4f));
    minigameLogic.update();
    assertTrue(minigameLogic.elapsed(4f));
  }

  /* Test Game Start */
  @Test
  void startMinigameStartsTimingMinigame() {
    /* Call start minigame and check the state correctly changes */
    minigameLogic.startMinigame();

    /* Update as it is intended to happen after a small delay */
    when(gameTime.getDeltaTime()).thenReturn(2f);
    minigameLogic.update();

    /* Check all components are started for minigame */
    verify(timingBarDisplay).setVisible(true);
    verify(timingBarLogic).resetMarker();
    verify(timingBarLogic).startMarker();
  }

  private void stopMinigame() {
    minigameLogic.stopMinigame();
    assertEquals(CyclopsMinigameLogic.State.STOP, minigameLogic.state);
    verify(timingBarLogic).stopMarker();

    /* Verify that the sounds are stopped */
    verify(minigameLogic.walkingSound).stop(anyLong());
    verify(minigameLogic.missSound).stop(anyLong());
    verify(minigameLogic.hitSound).stop(anyLong());
  }

  @Test
  void stopMinigameCallsNecessaryFunctions() {
    stopMinigame();
  }

  @Test
  void minigameCanBeStoppedAnywhereInPlay() {
    when(gameTime.getDeltaTime()).thenReturn(0.1f);

    for (CyclopsMinigameLogic.State state : CyclopsMinigameLogic.State.values()) {
      reset(
          timingBarLogic,
          minigameLogic.walkingSound,
          minigameLogic.missSound,
          minigameLogic.hitSound);

      minigameLogic.startMinigame();
      minigameLogic.update();
      minigameLogic.changeState(state);
      minigameLogic.update();
      stopMinigame();
    }
  }

  @Test
  void timingMinigamePlaysSuccessSoundOnSuccessStop() {
    when(gameTime.getDeltaTime()).thenReturn(5f);
    minigameLogic.startMinigame();
    minigameLogic.update();

    verify(timingBarDisplay).setVisible(true);
    verify(timingBarLogic).resetMarker();
    verify(timingBarLogic).startMarker();
    assertEquals(CyclopsMinigameLogic.State.PLAY, minigameLogic.state);

    when(mockInput.isButtonJustPressed(CyclopsMinigameLogic.BUTTON)).thenReturn(true);
    when(timingBarLogic.checkHit()).thenReturn(true);
    minigameLogic.update();
    verify(timingBarLogic).stopMarker();
    verify(minigameLogic.hitSound).play();
    verify(minigameLogic.missSound, never()).play();
    assertEquals(CyclopsMinigameLogic.State.HIDE_DELAY, minigameLogic.state);
  }

  @Test
  void timingMinigamePlaysFailSoundOnFail() {
    when(gameTime.getDeltaTime()).thenReturn(5f);
    minigameLogic.startMinigame();
    minigameLogic.update();

    verify(timingBarDisplay).setVisible(true);
    verify(timingBarLogic).resetMarker();
    verify(timingBarLogic).startMarker();
    assertEquals(CyclopsMinigameLogic.State.PLAY, minigameLogic.state);

    when(mockInput.isButtonJustPressed(CyclopsMinigameLogic.BUTTON)).thenReturn(true);
    when(timingBarLogic.checkHit()).thenReturn(false);
    minigameLogic.update();
    verify(timingBarLogic).stopMarker();
    verify(minigameLogic.missSound).play();
    verify(minigameLogic.hitSound, never()).play();
    assertEquals(CyclopsMinigameLogic.State.HIDE_DELAY, minigameLogic.state);
  }

  /* Testing Transitioning */
  @Test
  void timingBarHidesAfterATurnOfTheMinigame() {
    when(gameTime.getDeltaTime()).thenReturn(10f);
    when(mockInput.isButtonJustPressed(CyclopsMinigameLogic.BUTTON)).thenReturn(true);

    minigameLogic.startMinigame();
    minigameLogic.changeState(CyclopsMinigameLogic.State.PLAY);
    minigameLogic.update(); /* Part where timing bar marker is sweeping across */
    assertEquals(CyclopsMinigameLogic.State.HIDE_DELAY, minigameLogic.state);
    minigameLogic.update();
    verify(timingBarDisplay).setVisible(false);
  }

  @Test
  void minigameHidesScreenAndPlaysSoundEffectAfterHidingTimingBarAndMovesToNextState() {
    when(gameTime.getDeltaTime()).thenReturn(10f);

    minigameLogic.changeState(CyclopsMinigameLogic.State.HIDE_DELAY);
    minigameLogic.update();
    assertEquals(CyclopsMinigameLogic.State.PRE_MOVE, minigameLogic.state);
    minigameLogic.update();
    verify(minigameLogic.transitionScreenCover).setVisible(true);
    verify(minigameLogic.walkingSound).play();
    assertEquals(CyclopsMinigameLogic.State.MOVING, minigameLogic.state);
  }

  @Test
  void minigameMovesPlayDuringTransitionWhenScreenIsHiddenOnSuccess() {
    GridPoint2 start = new GridPoint2(1, 0);
    GridPoint2 next = new GridPoint2(2, 0);
    GridPoint2 win = new GridPoint2(3, 0);

    minigameLogic.setSafeLocations(List.of(start, next));
    minigameLogic.setWinLocation(win);

    Vector2 nextPos = new Vector2(200, 0);
    when(terrainComponent.tileToWorldPosition(next)).thenReturn(nextPos);

    when(gameTime.getDeltaTime()).thenReturn(10f);
    when(timingBarLogic.checkHit()).thenReturn(true);
    minigameLogic.changeState(CyclopsMinigameLogic.State.MOVING);
    minigameLogic.update();
    verify(player).setPosition(nextPos);
  }

  /* Testing Creating and Dispose */
  @Test
  void disposeStopsAllSoundsAndDoesSuperCall() {
    minigameLogic.dispose();
    verify(minigameLogic.walkingSound).stop(anyLong());
    verify(minigameLogic.missSound).stop(anyLong());
    verify(minigameLogic.hitSound).stop(anyLong());
  }
}
