package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.components.PhysicsComponent;
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
  private static final String SPEED_LABEL_NAME = "player-speed-label";

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
    assertEquals("Health: 6 / 10", healthLabel.getText().toString());
  }

  @Test
  void shouldUpdateHealthTextWhenHealthChanges() {
    CombatStatsComponent stats = createPlayerWithHealth(6, 10);

    stats.setHealth(4);

    Label healthLabel = stage.getRoot().findActor(HEALTH_LABEL_NAME);
    assertNotNull(healthLabel);
    assertEquals("Health: 4 / 10", healthLabel.getText().toString());
  }

  @Test
  void shouldShowAbsoluteHorizontalSpeedWithTwoDecimalPlaces() {
    CombatStatsComponent stats = new CombatStatsComponent(6, 10, 1);
    Body body = mock(Body.class);
    when(body.getLinearVelocity()).thenReturn(new Vector2(-3.456f, 42f));
    PhysicsComponent physics = mock(PhysicsComponent.class);
    when(physics.getBody()).thenReturn(body);
    PlayerStatsDisplay display = new PlayerStatsDisplay();
    Entity player = new Entity().addComponent(stats).addComponent(physics).addComponent(display);
    entityService.register(player);

    display.draw(mock(SpriteBatch.class));

    Label speedLabel = stage.getRoot().findActor(SPEED_LABEL_NAME);
    assertNotNull(speedLabel);
    assertEquals("Speed: 3.46", speedLabel.getText().toString());
  }

  private CombatStatsComponent createPlayerWithHealth(int health, int maxHealth) {
    CombatStatsComponent stats = new CombatStatsComponent(health, maxHealth, 1);
    Entity player = new Entity().addComponent(stats).addComponent(new PlayerStatsDisplay());
    entityService.register(player);
    return stats;
  }
}
