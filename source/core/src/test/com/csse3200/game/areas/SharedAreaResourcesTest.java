package com.csse3200.game.areas;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.audio.Music;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.GameVolume;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.lang.reflect.Method;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

@ExtendWith(GameExtension.class)
class SharedAreaResourcesTest {
  private ResourceService resources;
  private float originalVolume;

  @BeforeEach
  void setUp() {
    resources = mock(ResourceService.class);
    ServiceLocator.registerResourceService(resources);
    originalVolume = GameVolume.get();
  }

  @AfterEach
  void tearDown() {
    GameVolume.set(originalVolume);
    ServiceLocator.clear();
  }

  @ParameterizedTest
  @ValueSource(strings = {"forest", "boss", "cyclops"})
  void loadsEveryAssetGroupBeforeWaitingAndUnloadsTheSameGroups(String name) throws Exception {
    GameArea area = createArea(name);
    when(resources.loadForMillis(10)).thenReturn(false, true);
    when(resources.getProgress()).thenReturn(50);
    invoke(area, "loadAssets");

    ArgumentCaptor<String[]> textures = ArgumentCaptor.forClass(String[].class);
    ArgumentCaptor<String[]> atlases = ArgumentCaptor.forClass(String[].class);
    ArgumentCaptor<String[]> sounds = ArgumentCaptor.forClass(String[].class);
    ArgumentCaptor<String[]> music = ArgumentCaptor.forClass(String[].class);
    var order = inOrder(resources);
    order.verify(resources).loadTextures(textures.capture());
    order.verify(resources).loadTextureAtlases(atlases.capture());
    order.verify(resources).loadSounds(sounds.capture());
    order.verify(resources).loadMusic(music.capture());
    order.verify(resources).loadForMillis(10);
    order.verify(resources).getProgress();
    order.verify(resources).loadForMillis(10);

    assertTrue(textures.getValue().length > 0);
    assertTrue(atlases.getValue().length > 0);
    assertTrue(sounds.getValue().length > 0);
    assertEquals(1, music.getValue().length);
    invoke(area, "unloadAssets");
    order.verify(resources).unloadAssets(textures.getValue());
    order.verify(resources).unloadAssets(atlases.getValue());
    order.verify(resources).unloadAssets(sounds.getValue());
    order.verify(resources).unloadAssets(music.getValue());
    order.verifyNoMoreInteractions();
  }

  @ParameterizedTest
  @ValueSource(strings = {"forest", "cyclops"})
  void startsTheLoadedBackgroundTrackAtTheAreaVolume(String name) throws Exception {
    GameArea area = createArea(name);
    when(resources.loadForMillis(10)).thenReturn(true);
    invoke(area, "loadAssets");
    ArgumentCaptor<String[]> tracks = ArgumentCaptor.forClass(String[].class);
    verify(resources).loadMusic(tracks.capture());
    String track = tracks.getValue()[0];
    Music music = mock(Music.class);
    when(resources.getAsset(track, Music.class)).thenReturn(music);
    GameVolume.set(0.5f);

    invoke(area, "playMusic");

    var order = inOrder(music);
    order.verify(music).setLooping(true);
    order.verify(music).setVolume((name.equals("forest") ? 0.3f : 0.4f) * 0.5f);
    order.verify(music).play();
    order.verifyNoMoreInteractions();
  }

  private GameArea createArea(String name) {
    CameraComponent camera = mock(CameraComponent.class);
    TerrainFactory terrain = mock(TerrainFactory.class);
    return switch (name) {
      case "forest" -> new ForestGameArea(camera, terrain);
      case "boss" -> new LevelBossGameArea(terrain, camera, new Entity());
      case "cyclops" -> new CyclopsMinigameArea(camera, terrain);
      default -> throw new IllegalArgumentException(name);
    };
  }

  private static void invoke(GameArea area, String name) throws Exception {
    Method method = area.getClass().getDeclaredMethod(name);
    method.setAccessible(true);
    method.invoke(area);
  }
}
