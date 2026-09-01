package com.csse3200.game.rendering;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(GameExtension.class)
@ExtendWith(MockitoExtension.class)
class MeleeRenderComponentTest {
  @Mock RenderService service;
  @Mock GameTime gameTime;

  @BeforeEach
  void setUp() {
    ServiceLocator.registerRenderService(service);
    ServiceLocator.registerTimeSource(gameTime);
  }

  @Test
  void shouldRecordSwingOnMeleeEvent() {
    Entity player = new Entity().addComponent(new MeleeRenderComponent());
    player.create();

    assertDoesNotThrow(() -> player.getEvents().trigger("melee", new Vector2(1f, 0f)));
  }

  @Test
  void shouldTickDownWithoutSwinging() {
    when(gameTime.getDeltaTime()).thenReturn(0.05f);
    Entity player = new Entity().addComponent(new MeleeRenderComponent());
    player.create();

    assertDoesNotThrow(player::update);
  }
}
