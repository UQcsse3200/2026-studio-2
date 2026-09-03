package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class PlayerStatsDisplayTest {
  private static final String HEALTH_LABEL_NAME = "player-health-label";

  private EntityService entityService;
  private Stage stage;

  @BeforeEach
  void setUp() {
    stage = new Stage(new ScreenViewport(), mock(SpriteBatch.class));

    RenderService renderService = mock(RenderService.class);
    when(renderService.getStage()).thenReturn(stage);
    ServiceLocator.registerRenderService(renderService);

    ResourceService resourceService = mock(ResourceService.class);
    when(resourceService.getAsset("images/purple_heart.png", Texture.class))
        .thenReturn(mock(Texture.class));
    ServiceLocator.registerResourceService(resourceService);

    entityService = new EntityService();
    ServiceLocator.registerEntityService(entityService);
  }

  @AfterEach
  void tearDown() {
    entityService.dispose();
    stage.dispose();
  }

  @Test
  void shouldShowCurrentAndMaximumHealth() {
    createPlayerWithHealth(6, 10);

    Label healthLabel = stage.getRoot().findActor(HEALTH_LABEL_NAME);
    assertNotNull(healthLabel);
    assertEquals("HP: 6 / 10", healthLabel.getText().toString());
  }

  @Test
  void shouldUpdateHealthTextWhenHealthChanges() {
    CombatStatsComponent stats = createPlayerWithHealth(6, 10);

    stats.setHealth(4);

    Label healthLabel = stage.getRoot().findActor(HEALTH_LABEL_NAME);
    assertNotNull(healthLabel);
    assertEquals("HP: 4 / 10", healthLabel.getText().toString());
  }

  private CombatStatsComponent createPlayerWithHealth(int health, int maxHealth) {
    CombatStatsComponent stats = new CombatStatsComponent(health, maxHealth, 1);
    Entity player = new Entity().addComponent(stats).addComponent(new PlayerStatsDisplay());
    entityService.register(player);
    return stats;
  }
}
