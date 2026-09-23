package com.csse3200.game.entities.factories;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.item.weapons.bow.BowComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.input.InputFactory;
import com.csse3200.game.input.InputService;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class PlayerFactoryTest {
  @BeforeEach
  void setUp() {
    ServiceLocator.registerPhysicsService(new PhysicsService());
    InputFactory inputFactory = mock(InputFactory.class);
    when(inputFactory.createForPlayer()).thenReturn(mock(InputComponent.class));
    InputService inputService = mock(InputService.class);
    when(inputService.getInputFactory()).thenReturn(inputFactory);
    ServiceLocator.registerInputService(inputService);

    ResourceService resources = mock(ResourceService.class);
    TextureAtlas atlas = mock(TextureAtlas.class);
    Array<AtlasRegion> regions = new Array<>();
    AtlasRegion region = mock(AtlasRegion.class);
    when(region.getRegionWidth()).thenReturn(1);
    when(region.getRegionHeight()).thenReturn(1);
    regions.add(region);
    when(atlas.findRegions(anyString())).thenReturn(regions);
    when(atlas.findRegion(anyString())).thenReturn(region);
    when(resources.getAsset(anyString(), eq(TextureAtlas.class))).thenReturn(atlas);
    ServiceLocator.registerResourceService(resources);
  }

  @Test
  void shouldCreatePlayerWithActionsAndAnimations() {
    Entity player = PlayerFactory.createPlayer();

    assertNotNull(player.getComponent(PlayerActions.class));
    assertNotNull(player.getComponent(AnimationRenderComponent.class));
  }

  @Test
  void shouldCreateDisplayPlayerWithoutPlayerActions() {
    Entity player = PlayerFactory.createPlayerDisplay();

    assertNotNull(player.getComponent(AnimationRenderComponent.class));
    org.junit.jupiter.api.Assertions.assertNull(player.getComponent(PlayerActions.class));
  }

  @Test
  void shouldCreateFullPlayerWithBowComponent() {
    Entity player = PlayerFactory.createPlayer();

    assertNotNull(player.getComponent(BowComponent.class));
  }
}
