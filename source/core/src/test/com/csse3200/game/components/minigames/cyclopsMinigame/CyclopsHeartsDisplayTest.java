package com.csse3200.game.components.minigames.cyclopsMinigame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.player.PlayerStatsDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class CyclopsHeartsDisplayTest {
  private static final float FULL = CyclopsHeartsDisplay.HEART_SIZE;
  private static final float HALF = CyclopsHeartsDisplay.HEART_SIZE / 2f;
  private static final int HEART_TEXTURE_SIZE = 64;

  private EntityService entityService;
  private Stage stage;
  private CombatStatsComponent stats;
  private CyclopsHeartsDisplay display;

  @BeforeEach
  void setUp() {
    stage = new Stage(new ScreenViewport(), mock(SpriteBatch.class));
    RenderService renderService = mock(RenderService.class);
    when(renderService.getStage()).thenReturn(stage);
    ServiceLocator.registerRenderService(renderService);

    ResourceService resourceService = mock(ResourceService.class);
    Texture heart = mock(Texture.class);
    when(heart.getWidth()).thenReturn(HEART_TEXTURE_SIZE);
    when(heart.getHeight()).thenReturn(HEART_TEXTURE_SIZE);
    when(resourceService.getAsset(CyclopsHeartsDisplay.HEART_TEXTURE, Texture.class))
        .thenReturn(heart);
    for (String texture :
        new String[] {
          CyclopsHeartsDisplay.FULL_BAR_TEXTURE,
          CyclopsHeartsDisplay.DAMAGED_BAR_TEXTURE,
          CyclopsHeartsDisplay.LAST_HEART_BAR_TEXTURE
        }) {
      when(resourceService.getAsset(texture, Texture.class)).thenReturn(mock(Texture.class));
    }
    ServiceLocator.registerResourceService(resourceService);

    entityService = new EntityService();
    ServiceLocator.registerEntityService(entityService);

    stats = new CombatStatsComponent(10, 1);
    display = new CyclopsHeartsDisplay();
    entityService.register(new Entity().addComponent(stats).addComponent(display));
  }

  @AfterEach
  void tearDown() {
    entityService.dispose();
    stage.dispose();
  }

  /** Runs the stage long enough for lost hearts to finish flickering out. */
  private void finishFlicker() {
    for (int i = 0; i < 10; i++) {
      stage.act(0.1f);
    }
  }

  /** The width of each visible heart, in order; a hidden heart is 0. */
  private float[] heartWidths() {
    List<Image> hearts = display.getHearts();
    float[] widths = new float[hearts.size()];
    for (int i = 0; i < hearts.size(); i++) {
      widths[i] = hearts.get(i).isVisible() ? hearts.get(i).getWidth() : 0f;
    }
    return widths;
  }

  private void assertHearts(float... expected) {
    float[] actual = heartWidths();
    assertEquals(expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], actual[i], 0.001f, "heart " + i);
    }
  }

  @Test
  void fullHealthShowsFiveWholeHearts() {
    assertHearts(FULL, FULL, FULL, FULL, FULL);
  }

  @Test
  void eachMissTakesOneAndAHalfHearts() {
    stats.addHealth(-CyclopsMinigameLogic.MISS_DAMAGE);
    finishFlicker();
    assertHearts(FULL, FULL, FULL, HALF, 0f);

    stats.addHealth(-CyclopsMinigameLogic.MISS_DAMAGE);
    finishFlicker();
    assertHearts(FULL, FULL, 0f, 0f, 0f);

    stats.addHealth(-CyclopsMinigameLogic.MISS_DAMAGE);
    finishFlicker();
    assertHearts(HALF, 0f, 0f, 0f, 0f);

    stats.addHealth(-CyclopsMinigameLogic.MISS_DAMAGE);
    finishFlicker();
    assertHearts(0f, 0f, 0f, 0f, 0f);
  }

  @Test
  void halfHeartShowsTheLeftHalfOfTheHeart() {
    stats.setHealth(7);

    Image half = display.getHearts().get(3);
    TextureRegion region = ((TextureRegionDrawable) half.getDrawable()).getRegion();
    assertEquals(HALF, half.getWidth(), 0.001f);
    assertEquals(0, region.getRegionX());
    assertEquals(HEART_TEXTURE_SIZE / 2, region.getRegionWidth());
    assertEquals(HEART_TEXTURE_SIZE, region.getRegionHeight());
  }

  @Test
  void healingRefillsAHalfHeart() {
    stats.setHealth(7);
    stats.setHealth(8);
    finishFlicker();

    assertHearts(FULL, FULL, FULL, FULL, 0f);
  }

  @Test
  void hidesTheUsualHeartBarOnceDrawn() {
    PlayerStatsDisplay usualBar = new PlayerStatsDisplay();
    CyclopsHeartsDisplay cyclopsBar = new CyclopsHeartsDisplay();
    entityService.register(
        new Entity()
            .addComponent(new CombatStatsComponent(10, 1))
            .addComponent(usualBar)
            .addComponent(cyclopsBar));
    int visibleBefore = visibleRoots();

    cyclopsBar.draw(mock(SpriteBatch.class));

    assertEquals(visibleBefore - 1, visibleRoots());
    assertTrue(cyclopsBar.getHearts().getFirst().isVisible());
    assertFalse(cyclopsBar.getHearts().isEmpty());
  }

  private int visibleRoots() {
    int visible = 0;
    for (Actor actor : stage.getRoot().getChildren()) {
      if (actor.isVisible()) {
        visible++;
      }
    }
    return visible;
  }
}
