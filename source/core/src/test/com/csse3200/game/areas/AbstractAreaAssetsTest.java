package com.csse3200.game.areas;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ResourceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** How every level loads its assets: all four kinds, and not carrying on until they are ready. */
@ExtendWith(GameExtension.class)
abstract class AbstractAreaAssetsTest {
  /** The mock resource service the area under test was built with. */
  abstract ResourceService resourceService();

  /** Calls a private method of the area under test. */
  abstract void invokePrivate(String methodName) throws Exception;

  @Test
  void loadAssetsLoadsTheAreasResources() throws Exception {
    invokePrivate("loadAssets");

    ResourceService resources = resourceService();
    verify(resources).loadTextures(any(String[].class));
    verify(resources).loadTextureAtlases(any(String[].class));
    verify(resources).loadSounds(any(String[].class));
    verify(resources).loadMusic(any(String[].class));
    verify(resources).loadForMillis(10);
  }

  @Test
  void loadAssetsWaitsUntilLoadingCompletes() throws Exception {
    ResourceService resources = resourceService();
    when(resources.loadForMillis(10)).thenReturn(false, false, true);
    when(resources.getProgress()).thenReturn(25, 75);

    invokePrivate("loadAssets");

    verify(resources, times(3)).loadForMillis(10);
    verify(resources, times(2)).getProgress();
  }
}
