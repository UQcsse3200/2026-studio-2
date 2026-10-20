package com.csse3200.game.screens.minigames;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.minigames.blackjack.Blackjack;
import com.csse3200.game.components.minigames.blackjack.BlackjackActions;
import com.csse3200.game.components.minigames.blackjack.BlackjackConfig;
import com.csse3200.game.components.minigames.blackjack.BlackjackDialogue;
import com.csse3200.game.components.minigames.blackjack.BlackjackDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Tests the stable parts of {@link BlackjackScreen}: which assets it asks for, the components it
 * builds, and how it handles the background music.
 */
@ExtendWith(GameExtension.class)
class BlackjackScreenTest {
  private static final String MUSIC = "sounds/minigames/blackjack/blackjack-bgm.mp3";

  private BlackjackScreen screen;
  private ResourceService resources;
  private Music music;

  @BeforeEach
  void createScreen() {
    screen = new BlackjackScreen(mock(GdxGame.class));
    resources = mock(ResourceService.class);
    music = mock(Music.class);
    when(resources.getAsset(MUSIC, Music.class)).thenReturn(music);
  }

  private void callSetSoundEnabled(boolean enabled) throws Exception {
    Method method = BlackjackScreen.class.getDeclaredMethod("setSoundEnabled", boolean.class);
    method.setAccessible(true);
    method.invoke(screen, enabled);
  }

  @Test
  void requestsTheTexturesListedInTheBlackjackConfig() {
    String[] textures = screen.getTextures();

    assertTrue(textures.length > 0);
    assertArrayEquals(BlackjackConfig.TEXTURES.toArray(String[]::new), textures);
  }

  @Test
  void requestsTheSoundsListedInTheBlackjackConfig() {
    String[] sounds = screen.getSounds();

    assertTrue(sounds.length > 0);
    assertArrayEquals(BlackjackConfig.SOUNDS.toArray(String[]::new), sounds);
  }

  @Test
  void createUiBuildsDisplayActionsAndInputComponents() {
    RenderService renderService = mock(RenderService.class);
    when(renderService.getStage()).thenReturn(mock(Stage.class));
    ServiceLocator.registerRenderService(renderService);

    Entity ui = screen.createUI();

    assertNotNull(ui);
    assertNotNull(ui.getComponent(BlackjackDisplay.class));
    assertNotNull(ui.getComponent(BlackjackActions.class));
    assertNotNull(ui.getComponent(InputDecorator.class));
  }

  @Test
  void disablingSoundPausesTheMusic() throws Exception {
    ServiceLocator.registerResourceService(resources);

    callSetSoundEnabled(false);

    verify(music).pause();
    verify(music, never()).play();
  }

  @Test
  void enablingSoundPlaysTheMusic() throws Exception {
    ServiceLocator.registerResourceService(resources);

    callSetSoundEnabled(true);

    verify(music).play();
    verify(music, never()).pause();
  }

  @Test
  void disposeStopsAndUnloadsTheMusicAndClearsTheServices() {
    Renderer renderer = mock(Renderer.class);
    screen.renderer = renderer;
    ServiceLocator.registerResourceService(resources);
    ServiceLocator.registerRenderService(mock(RenderService.class));
    ServiceLocator.registerEntityService(mock(EntityService.class));

    screen.dispose();

    verify(music).stop();
    verify(resources).unloadAssets(new String[] {MUSIC});
    verify(renderer).dispose();
    assertNull(ServiceLocator.getResourceService());
    assertNull(ServiceLocator.getRenderService());
    assertNull(ServiceLocator.getEntityService());
  }

  @Test
  void disposeUnloadsBlackjackTexturesAndSounds() {
    screen.renderer = mock(Renderer.class);
    ServiceLocator.registerResourceService(resources);
    ServiceLocator.registerRenderService(mock(RenderService.class));
    ServiceLocator.registerEntityService(mock(EntityService.class));

    screen.dispose();

    verify(resources).unloadAssets(BlackjackConfig.TEXTURES.toArray(String[]::new));
    verify(resources).unloadAssets(BlackjackConfig.SOUNDS.toArray(String[]::new));
  }

  private Entity createUiFor(GdxGame game, boolean storyMode) {
    screen = new BlackjackScreen(game, storyMode);
    RenderService renderService = mock(RenderService.class);
    when(renderService.getStage()).thenReturn(mock(Stage.class));
    ServiceLocator.registerRenderService(renderService);
    return screen.createUI();
  }

  /** The inventory used for betting is held by the display, not retrieved by component subclass. */
  private static InventoryComponent displayInventory(BlackjackDisplay display) throws Exception {
    Field field = BlackjackDisplay.class.getDeclaredField("inventory");
    field.setAccessible(true);
    return (InventoryComponent) field.get(display);
  }

  @Test
  void storyBlackjackStartsWithSavedGoldAndSynchronizesChanges() throws Exception {
    GdxGame game = mock(GdxGame.class);
    when(game.getSavedPlayerGold()).thenReturn(25);

    Entity ui = createUiFor(game, true);
    BlackjackDisplay display = ui.getComponent(BlackjackDisplay.class);
    InventoryComponent inventory = displayInventory(display);

    assertNotNull(inventory);
    assertEquals(25, inventory.getGold());
    inventory.setGold(24);
    verify(game).updateSavedPlayerGold(24);
  }

  @Test
  void storyBlackjackProvidesVictoryCallbackWithoutAdvancingOnSingleRoundEvent() throws Exception {
    GdxGame game = mock(GdxGame.class);
    Entity ui = createUiFor(game, true);
    BlackjackDisplay display = ui.getComponent(BlackjackDisplay.class);
    Field field = BlackjackDisplay.class.getDeclaredField("storyWinCallback");
    field.setAccessible(true);

    assertNotNull(field.get(display));
    // The legacy single-round event must not skip the best-of-five match.
    ui.getEvents().trigger("roundCompleted", true);
    verify(game, never()).playCutscene("cutscene5", GdxGame.ScreenType.LEVEL_3_GAME);
  }

  @Test
  void outOfGoldDialogueRefillsTenGoldAndAllowsMatchRetry() {
    Blackjack blackjack = new Blackjack(1);
    blackjack.setBalance(0);

    BlackjackDialogue.of(BlackjackDialogue.Kind.OUT_OF_GOLD).pressButton(blackjack, null);

    assertEquals(Blackjack.REFILL_GOLD, blackjack.getBalance());
    assertEquals(0, blackjack.getPlayerWins());
    assertEquals(0, blackjack.getDealerWins());
  }

  @Test
  void blackjackWithoutSavedPlayerKeepsPracticeBalance() throws Exception {
    GdxGame game = mock(GdxGame.class);
    when(game.getSavedPlayerGold()).thenReturn((Integer) null);
    Entity ui = createUiFor(game, false);
    InventoryComponent inventory = displayInventory(ui.getComponent(BlackjackDisplay.class));

    assertEquals(100, inventory.getGold());
    inventory.setGold(99);
    verify(game, never()).updateSavedPlayerGold(99);
  }
}
