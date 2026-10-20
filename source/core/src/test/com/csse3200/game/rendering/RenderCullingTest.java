package com.csse3200.game.rendering;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.floatThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.math.Frustum;
import org.junit.jupiter.api.Test;

class RenderCullingTest {
  @Test
  void delegatesBoundsChecksToCameraFrustum() {
    Frustum frustum = mock(Frustum.class);
    when(frustum.boundsInFrustum(0f, 0f, 0f, 1f, 1f, 0f)).thenReturn(true);

    assertTrue(RenderCulling.isVisible(frustum, -1f, -1f, 2f, 2f, 0f));

    verify(frustum).boundsInFrustum(0f, 0f, 0f, 1f, 1f, 0f);
  }

  @Test
  void accountsForRotationWhenCheckingBounds() {
    Frustum frustum = mock(Frustum.class);
    when(frustum.boundsInFrustum(
            eq(5f),
            eq(5f),
            eq(0f),
            floatThat(value -> Math.abs(value - (float) Math.sqrt(2f)) < 0.001f),
            floatThat(value -> Math.abs(value - (float) Math.sqrt(2f)) < 0.001f),
            eq(0f)))
        .thenReturn(false);

    assertFalse(RenderCulling.isVisible(frustum, 4f, 4f, 2f, 2f, 45f));
  }
}
