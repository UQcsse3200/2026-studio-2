package com.csse3200.game.components.minigames.cyclopsMinigame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class TimingBarDisplayTests {

  TimingBarDisplay display;
  TimingBarLogic logic;
  RenderService mockRenderService;
  Stage mockStage;

  @BeforeEach
  void setup() {
    logic = mock(TimingBarLogic.class);
    display = new TimingBarDisplay(logic);

    mockStage = mock(Stage.class);
    mockRenderService = mock(RenderService.class);
    ServiceLocator.registerRenderService(mockRenderService);

    when(mockRenderService.getStage()).thenReturn(mockStage);
    when(mockStage.getWidth()).thenReturn(800f);
    when(mockStage.getHeight()).thenReturn(600f);

    ResourceService resourceService = mock(ResourceService.class);
    when(resourceService.getAsset(anyString(), eq(Texture.class))).thenReturn(mock(Texture.class));
    ServiceLocator.registerResourceService(resourceService);
  }

  @Test
  void timingBarDisplayIsCreatedAndAddsAnActor() {
    when(logic.getMarkerX()).thenReturn(0f);
    display.create();
    verify(mockStage, atLeastOnce()).addActor(any(Actor.class));
  }

  @Test
  void scoringZoneMatchesLogicWidthAndIsCentred() {
    TimingBarLogic realLogic = new TimingBarLogic(20f);
    TimingBarDisplay realDisplay = new TimingBarDisplay(realLogic);
    realDisplay.create();

    realLogic.changeScoringAreaWidth(10f);
    realDisplay.update();

    float width = realDisplay.scoringZone.getWidth();
    assertEquals(0.10f * TimingBarDisplay.BAR_WIDTH, width, 1e-4f);
    assertEquals(TimingBarDisplay.BAR_WIDTH / 2 - width / 2, realDisplay.scoringZone.getX(), 1e-4f);
  }

  @Test
  void shrinkingFlashesScoringZoneWhenBarAppears() {
    TimingBarLogic realLogic = new TimingBarLogic(20f);
    TimingBarDisplay realDisplay = new TimingBarDisplay(realLogic);
    realDisplay.create();

    realLogic.changeScoringAreaWidth(10f);
    realDisplay.setVisible(true);
    realDisplay.update();

    assertEquals(Color.WHITE, realDisplay.scoringZone.getColor());
  }

  @Test
  void zoneLayersAllMatchLogicWidthAfterShrink() {
    TimingBarLogic realLogic = new TimingBarLogic(20f);
    TimingBarDisplay realDisplay = new TimingBarDisplay(realLogic);
    realDisplay.create();

    realLogic.changeScoringAreaWidth(10f);
    realDisplay.update();

    float width = 0.10f * TimingBarDisplay.BAR_WIDTH;
    float x = TimingBarDisplay.BAR_WIDTH / 2 - width / 2;
    assertEquals(width, realDisplay.scoringZone.getWidth(), 1e-4f);
    assertEquals(x, realDisplay.scoringZone.getX(), 1e-4f);
    assertEquals(width, realDisplay.zoneHighlight.getWidth(), 1e-4f);
    assertEquals(x, realDisplay.zoneHighlight.getX(), 1e-4f);
    assertEquals(width, realDisplay.zoneShadow.getWidth(), 1e-4f);
    assertEquals(x, realDisplay.zoneShadow.getX(), 1e-4f);
    assertEquals(x, realDisplay.zoneLeftEdge.getX(), 1e-4f);
    assertEquals(x + width - 2f, realDisplay.zoneRightEdge.getX(), 1e-4f);
  }

  @Test
  void scoringZoneLayersUseTheSpecifiedColours() {
    TimingBarLogic realLogic = new TimingBarLogic(20f);
    TimingBarDisplay realDisplay = new TimingBarDisplay(realLogic);
    realDisplay.create();

    assertEquals(Color.valueOf("#E8B04A"), realDisplay.scoringZone.getColor());
    assertEquals(Color.valueOf("#FFD97A"), realDisplay.zoneHighlight.getColor());
    assertEquals(Color.valueOf("#B07628"), realDisplay.zoneShadow.getColor());
    assertEquals(Color.valueOf("#FFF0BE"), realDisplay.zoneLeftEdge.getColor());
    assertEquals(Color.valueOf("#FFF0BE"), realDisplay.zoneRightEdge.getColor());
  }

  @Test
  void markerKeepsItsCentreAndSize() {
    TimingBarLogic realLogic = new TimingBarLogic(20f);
    TimingBarDisplay realDisplay = new TimingBarDisplay(realLogic);
    realDisplay.create();

    realLogic.startMarker();
    realLogic.update(0.5f);
    realDisplay.update();

    float centre = realDisplay.marker.getX() + realDisplay.marker.getWidth() / 2;
    assertEquals(
        0.5f * TimingBarDisplay.BAR_WIDTH + TimingBarDisplay.MARKER_WIDTH / 2, centre, 1e-4f);
    assertEquals(6f, realDisplay.marker.getWidth(), 1e-4f);
    assertEquals(46f, realDisplay.marker.getHeight(), 1e-4f);
    assertEquals(-8f, realDisplay.marker.getY(), 1e-4f);
  }

  @Test
  void frameIsDrawnAtTheTrackInset() {
    TimingBarLogic realLogic = new TimingBarLogic(20f);
    TimingBarDisplay realDisplay = new TimingBarDisplay(realLogic);
    realDisplay.create();

    assertEquals(-34f, realDisplay.frame.getX(), 1e-4f);
    assertEquals(-34f, realDisplay.frame.getY(), 1e-4f);
    assertEquals(478f, realDisplay.frame.getWidth(), 1e-4f);
    assertEquals(98f, realDisplay.frame.getHeight(), 1e-4f);
  }
}
