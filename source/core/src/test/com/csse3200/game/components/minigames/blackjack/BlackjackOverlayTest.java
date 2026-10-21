package com.csse3200.game.components.minigames.blackjack;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.audio.Music;
import com.csse3200.game.components.minigames.MinigameOverlayManager;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Tests the parts of {@link BlackjackOverlay} that decide whether it may open and how it controls
 * the music. Opening the full Blackjack UI is covered by the display tests, so it is not repeated
 * here.
 */
@ExtendWith(GameExtension.class)
class BlackjackOverlayTest {
  private static final String MUSIC = "sounds/minigames/blackjack/blackjack-bgm.mp3";

  private EntityService entities;
  private ResourceService resources;
  private MinigameOverlayManager manager;

  @BeforeEach
  void registerServices() {
    entities = mock(EntityService.class);
    resources = mock(ResourceService.class);
    manager = new MinigameOverlayManager();
    ServiceLocator.registerEntityService(entities);
    ServiceLocator.registerResourceService(resources);
  }

  private static void setSoundEnabled(BlackjackOverlay overlay, boolean enabled) throws Exception {
    Method method = BlackjackOverlay.class.getDeclaredMethod("setSoundEnabled", boolean.class);
    method.setAccessible(true);
    method.invoke(overlay, enabled);
  }

  @Test
  void requestClaimsTheOverlayAndPausesGameplay() {
    BlackjackOverlay overlay = new BlackjackOverlay(null, manager);

    overlay.request();

    assertTrue(manager.isActive());
    verify(entities).setPaused(true);
  }

  @Test
  void secondRequestWhileOpeningIsIgnored() {
    BlackjackOverlay overlay = new BlackjackOverlay(null, manager);

    overlay.request();
    overlay.request();

    verify(entities, times(1)).setPaused(true);
  }

  @Test
  void requestIsRefusedWhileAnotherMinigameOverlayIsActive() {
    manager.tryOpen();
    BlackjackOverlay overlay = new BlackjackOverlay(null, manager);

    overlay.request();

    verify(entities, never()).setPaused(true);
    assertTrue(manager.isActive());
  }

  @Test
  void afterRenderDoesNothingWhenNothingWasRequested() {
    BlackjackOverlay overlay = new BlackjackOverlay(null, manager);

    overlay.afterRender();

    verifyNoInteractions(resources);
    verifyNoInteractions(entities);
    assertFalse(manager.isActive());
  }

  @Test
  void defaultAndPlayerConstructorsDoNotThrow() {
    org.junit.jupiter.api.function.Executable createOverlay = BlackjackOverlay::new;
    assertDoesNotThrow(createOverlay);
    assertDoesNotThrow(() -> new BlackjackOverlay(null));
  }

  @Test
  void disablingSoundPausesTheMusic() throws Exception {
    Music music = mock(Music.class);
    when(resources.containsAsset(MUSIC, Music.class)).thenReturn(true);
    when(resources.getAsset(MUSIC, Music.class)).thenReturn(music);

    setSoundEnabled(new BlackjackOverlay(null, manager), false);

    verify(music).pause();
    verify(music, never()).play();
  }

  @Test
  void enablingSoundPlaysTheMusic() throws Exception {
    Music music = mock(Music.class);
    when(resources.containsAsset(MUSIC, Music.class)).thenReturn(true);
    when(resources.getAsset(MUSIC, Music.class)).thenReturn(music);

    setSoundEnabled(new BlackjackOverlay(null, manager), true);

    verify(music).play();
    verify(music, never()).pause();
  }

  @Test
  void soundToggleIsIgnoredWhenMusicIsNotLoaded() throws Exception {
    when(resources.containsAsset(MUSIC, Music.class)).thenReturn(false);

    setSoundEnabled(new BlackjackOverlay(null, manager), true);

    verify(resources, never()).getAsset(MUSIC, Music.class);
  }
}
