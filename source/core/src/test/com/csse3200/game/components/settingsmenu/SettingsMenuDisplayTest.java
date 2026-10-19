package com.csse3200.game.components.settingsmenu;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.ButtonSound;
import com.csse3200.game.components.GameVolume;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UiTestSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class SettingsMenuDisplayTest extends UiTestSupport {
  private Graphics previousGraphics;
  private float previousVolume;
  private MockedStatic<UserSettings> userSettings;
  private MockedStatic<ButtonSound> sounds;
  private UserSettings.Settings saved;
  private GdxGame game;

  private static class DisplayMode extends Graphics.DisplayMode {
    DisplayMode(int width, int height, int refreshRate) {
      super(width, height, refreshRate, 32);
    }
  }

  @BeforeEach
  void setUpSettings() {
    previousGraphics = Gdx.graphics;
    previousVolume = GameVolume.get();
    Graphics graphics = mock(Graphics.class);
    Graphics.Monitor monitor = mock(Graphics.Monitor.class);
    DisplayMode firstMode = new DisplayMode(1280, 720, 60);
    DisplayMode activeMode = new DisplayMode(1920, 1080, 144);
    when(graphics.getWidth()).thenReturn(1600);
    when(graphics.getHeight()).thenReturn(900);
    when(graphics.getMonitor()).thenReturn(monitor);
    when(graphics.getDisplayModes(monitor))
        .thenReturn(new Graphics.DisplayMode[] {firstMode, activeMode});
    when(graphics.getDisplayMode()).thenReturn(activeMode);
    Gdx.graphics = graphics;
    saved = new UserSettings.Settings();
    saved.fps = 75;
    saved.volume = 0.4f;
    saved.uiScale = 1.2f;
    saved.fullscreen = false;
    saved.vsync = true;
    userSettings = mockStatic(UserSettings.class);
    userSettings.when(UserSettings::get).thenReturn(saved);
    sounds = mockStatic(ButtonSound.class);
    game = mock(GdxGame.class);
  }

  @AfterEach
  void restoreSettings() {
    sounds.close();
    userSettings.close();
    GameVolume.set(previousVolume);
    Gdx.graphics = previousGraphics;
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void bothMenusPopulateAndApplyTheSharedSettingsControls(boolean inGame) {
    SettingsMenuDisplay display = createMenu(inGame, () -> {});
    TextField fps = controls(TextField.class).getFirst();
    List<CheckBox> checks = controls(CheckBox.class);
    List<Slider> sliders = controls(Slider.class);
    SelectBox<?> resolutions = controls(SelectBox.class).getFirst();
    assertEquals("75", fps.getText());
    assertFalse(checks.get(0).isChecked());
    assertTrue(checks.get(1).isChecked());
    assertEquals(1.2f, sliders.get(0).getValue(), 0.001f);
    assertEquals(0.4f, sliders.get(1).getValue(), 0.001f);
    assertEquals(1, resolutions.getSelectedIndex());
    assertEquals("1920x1080, 144hz", resolutions.getSelected().toString());

    fps.setText("120");
    checks.get(0).setChecked(true);
    checks.get(1).setChecked(false);
    sliders.get(0).setValue(1.5f);
    sliders.get(1).setValue(0.75f);
    resolutions.setSelectedIndex(0);
    assertEquals(0.75f, GameVolume.get(), 0.001f);
    assertTrue(labels(stage.getRoot()).contains("75%"));
    assertTrue(labels(stage.getRoot()).contains("1.50x"));
    controls(ImageButton.class).get(1).fire(new ChangeListener.ChangeEvent());

    ArgumentCaptor<UserSettings.Settings> applied =
        ArgumentCaptor.forClass(UserSettings.Settings.class);
    userSettings.verify(() -> UserSettings.set(applied.capture(), eq(true)));
    assertEquals(120, applied.getValue().fps);
    assertTrue(applied.getValue().fullscreen);
    assertFalse(applied.getValue().vsync);
    assertEquals(1.5f, applied.getValue().uiScale, 0.001f);
    assertEquals(0.75f, applied.getValue().volume, 0.001f);
    assertEquals(1280, applied.getValue().displayMode.width);
    assertEquals(720, applied.getValue().displayMode.height);
    assertEquals(60, applied.getValue().displayMode.refreshRate);
    sounds.verify(ButtonSound::playClick);
    display.dispose();
  }

  @Test
  void invalidFpsLeavesTheSavedCapWhileApplyingOtherChanges() {
    SettingsMenuDisplay display = createMenu(false, () -> {});
    controls(TextField.class).getFirst().setText("not a number");
    controls(Slider.class).get(1).setValue(0.6f);
    controls(ImageButton.class).get(1).fire(new ChangeListener.ChangeEvent());
    ArgumentCaptor<UserSettings.Settings> applied =
        ArgumentCaptor.forClass(UserSettings.Settings.class);
    userSettings.verify(() -> UserSettings.set(applied.capture(), eq(true)));
    assertEquals(75, applied.getValue().fps);
    assertEquals(0.6f, applied.getValue().volume, 0.001f);
    display.dispose();
  }

  @Test
  void standaloneExitRestoresSavedVolumeAndNavigatesAfterTheClickSound() {
    AtomicReference<Runnable> afterSound = new AtomicReference<>();
    sounds
        .when(() -> ButtonSound.playClickThen(any(Runnable.class)))
        .thenAnswer(
            call -> {
              afterSound.set(call.getArgument(0));
              return null;
            });
    SettingsMenuDisplay display = createMenu(false, () -> {});
    controls(Slider.class).get(1).setValue(0.9f);
    controls(ImageButton.class).getFirst().fire(new ChangeListener.ChangeEvent());
    verifyNoInteractions(game);
    assertEquals(0.9f, GameVolume.get(), 0.001f);
    assertNotNull(afterSound.get());
    afterSound.get().run();
    assertEquals(0.4f, GameVolume.get(), 0.001f);
    verify(game).setScreen(GdxGame.ScreenType.MAIN_MENU);
    display.dispose();
  }

  @Test
  void inGameExitClosesImmediatelyAndRemovesItsContentFromTheSharedStage() {
    AtomicInteger closes = new AtomicInteger();
    entities.setSettingsOpen(true);
    SettingsMenuDisplay display =
        createMenu(
            true,
            () -> {
              assertFalse(entities.getSettingsOpen());
              assertEquals(0.4f, GameVolume.get(), 0.001f);
              closes.incrementAndGet();
            });
    Actor gameActor = new Actor();
    stage.addActor(gameActor);
    controls(Slider.class).get(1).setValue(0.8f);
    controls(ImageButton.class).getFirst().fire(new ChangeListener.ChangeEvent());
    assertEquals(1, closes.get());
    sounds.verify(ButtonSound::playClick);
    verifyNoInteractions(game);
    display.dispose();
    assertSame(stage, gameActor.getStage());
    assertTrue(controls(ImageButton.class).isEmpty());
    assertTrue(controls(TextField.class).isEmpty());
    assertNull(display.background.getStage());
    assertTrue(display.menuBoxTable.getChildren().isEmpty());
    verify(ServiceLocator.getRenderService()).unregister(display);
  }

  @Test
  void updateAdvancesStageActionsUsingTheGameTimeDelta() {
    SettingsMenuDisplay display = createMenu(false, () -> {});
    GameTime time = mock(GameTime.class);
    when(time.getDeltaTime()).thenReturn(0.25f);
    ServiceLocator.registerTimeSource(time);
    Actor animated = new Actor();
    animated.addAction(Actions.moveBy(40f, 0f, 1f));
    stage.addActor(animated);
    display.update();
    assertEquals(10f, animated.getX(), 0.001f);
    display.dispose();
  }

  private SettingsMenuDisplay createMenu(boolean inGame, Runnable onClose) {
    SettingsMenuDisplay display =
        inGame
            ? new SettingsMenuDisplayInGame(game, onClose)
            : new SettingsMenuDisplay(game, GdxGame.ScreenType.MAIN_MENU);
    new Entity().addComponent(display);
    display.create();
    return display;
  }

  private <T extends Actor> List<T> controls(Class<T> type) {
    List<T> found = new ArrayList<>();
    collect(stage.getRoot(), type, found);
    return found;
  }

  private static <T extends Actor> void collect(Actor actor, Class<T> type, List<T> found) {
    if (type.isInstance(actor)) {
      found.add(type.cast(actor));
    }
    if (actor instanceof Group group) {
      for (Actor child : group.getChildren()) {
        collect(child, type, found);
      }
    }
  }
}
