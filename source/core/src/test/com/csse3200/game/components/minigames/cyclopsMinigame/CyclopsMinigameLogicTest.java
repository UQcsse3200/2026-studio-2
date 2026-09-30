package com.csse3200.game.components.minigames.cyclopsMinigame;

import static org.mockito.Mockito.*;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.components.player.PlayerAnimationController;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
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
  PlayerAnimationController animationController;

  @BeforeEach
  void setup() {
    timingBarLogic = mock(TimingBarLogic.class);
    timingBarDisplay = mock(TimingBarDisplay.class);
    terrainComponent = mock(TerrainComponent.class);
    player = mock(Entity.class);

    ServiceLocator.registerEntityService(new EntityService());

    animationController = mock(PlayerAnimationController.class);
    when(player.getComponent(PlayerAnimationController.class)).thenReturn(animationController);

    minigameLogic =
        new CyclopsMinigameLogic(timingBarLogic, timingBarDisplay, terrainComponent, player);
    minigameLogic.timer = spy(new Timer());
  }

  @AfterEach
  void tearDown() {
    minigameLogic.timer.clear();
  }

  @Test
  void shouldMovePlayerToNextSafeLocationOnSuccess() {
    GridPoint2 start = new GridPoint2(1, 0);
    GridPoint2 next = new GridPoint2(2, 0);
    GridPoint2 win = new GridPoint2(3, 0);

    minigameLogic.setSafeLocations(List.of(start, next));
    minigameLogic.setWinLocation(win);

    Vector2 nextPos = new Vector2(200, 0);
    when(terrainComponent.tileToWorldPosition(next)).thenReturn(nextPos);

    minigameLogic.moveToNextLocation(true);
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

    minigameLogic.moveToNextLocation(false);
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

    minigameLogic.moveToNextLocation(true);
    verify(player).setPosition(winPos);
  }

  @Test
  void startMinigameShouldScheduleStart() {
    minigameLogic.startMinigame();
    verify(minigameLogic.timer).scheduleTask(any(Timer.Task.class), anyFloat());
  }

  @Test
  void restartMinigameShouldMovePlayerToFirstSafeLocAndScheduleStart() {

    GridPoint2 first = new GridPoint2(1, 0);
    GridPoint2 second = new GridPoint2(2, 0);
    GridPoint2 win = new GridPoint2(3, 0);

    minigameLogic.setWinLocation(win);
    minigameLogic.setSafeLocations(List.of(first, second));

    Vector2 firstPos = new Vector2(100, 0);
    Vector2 secondPos = new Vector2(200, 0);
    when(terrainComponent.tileToWorldPosition(first)).thenReturn(firstPos);
    when(terrainComponent.tileToWorldPosition(second)).thenReturn(secondPos);

    minigameLogic.moveToNextLocation(true);
    verify(player).setPosition(secondPos);

    minigameLogic.restartMinigame();
    verify(player).setPosition(firstPos);
    verify(minigameLogic.timer).scheduleTask(any(Timer.Task.class), anyFloat());
  }
}
