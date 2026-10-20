package com.csse3200.game.components.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.lighting.PointLightComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CheckpointComponentTest {
  /** Note: this test relies on checkpoint collect range being +- 1 for both x and y */
  private Entity player;

  private Entity checkpointEntity;
  CheckpointComponent checkpointComponent;
  TextureRenderComponent textureRenderComponent;
  EntityService entityService;

  @BeforeEach
  void beforeEach() {
    Array<Entity> entities = new Array<>();
    checkpointComponent = new CheckpointComponent(false, new GridPoint2(10, 10));
    textureRenderComponent = mock(TextureRenderComponent.class);
    checkpointEntity = new Entity();
    checkpointEntity.addComponent(checkpointComponent);
    checkpointEntity.addComponent(textureRenderComponent);

    player = new Entity();
    player.addComponent(new PlayerActions());
    entities.add(player);
    entityService = mock(EntityService.class);
    when(entityService.getEntities()).thenReturn(entities);
    ServiceLocator.registerEntityService(entityService);
  }

  @Test
  void shouldBeCollected() {
    assertFalse(checkpointComponent.isActive());
    player.setPosition(10, 10);
    checkpointComponent.update();
    assertTrue(checkpointComponent.isActive());
    checkpointComponent.deactivate();

    player.setPosition(10.99f, 10);
    checkpointComponent.update();
    assertTrue(checkpointComponent.isActive());
    checkpointComponent.deactivate();

    player.setPosition(9.01f, 10);
    checkpointComponent.update();
    assertTrue(checkpointComponent.isActive());
    checkpointComponent.deactivate();

    player.setPosition(10, 10.99f);
    checkpointComponent.update();
    assertTrue(checkpointComponent.isActive());
    checkpointComponent.deactivate();

    player.setPosition(10, 9.01f);
    checkpointComponent.update();
    assertTrue(checkpointComponent.isActive());
    checkpointComponent.deactivate();
  }

  @Test
  void shouldNotBeCollected() {
    player.setPosition(11, 10);
    checkpointComponent.update();
    assertFalse(checkpointComponent.isActive());
    player.setPosition(9, 10);
    checkpointComponent.update();
    assertFalse(checkpointComponent.isActive());
    player.setPosition(10, 11);
    checkpointComponent.update();
    assertFalse(checkpointComponent.isActive());
    player.setPosition(10, 9);
    checkpointComponent.update();
    assertFalse(checkpointComponent.isActive());
    player.setPosition(9, 9);
    checkpointComponent.update();
    assertFalse(checkpointComponent.isActive());
    player.setPosition(11, 11);
    checkpointComponent.update();
    assertFalse(checkpointComponent.isActive());
    player.setPosition(9, 11);
    checkpointComponent.update();
    assertFalse(checkpointComponent.isActive());
    player.setPosition(11, 9);
    checkpointComponent.update();
    assertFalse(checkpointComponent.isActive());
  }

  @Test
  void shouldNotActivatedCollectedCheckpoint() {
    checkpointComponent = new CheckpointComponent(true, new GridPoint2(0, 0));
    assertTrue(checkpointComponent.isActive());
    checkpointComponent.activate();
    assertTrue(checkpointComponent.isActive());

    // ensure activate method doesn't attempt to spawn another lit checkpoint entity
    // ensures early return
    verify(entityService, never()).register(any(Entity.class));
  }

  @Test
  void shouldRepeatPlayerFindingIfNoPlayerFound() {
    entityService.scheduleForDisposal(player);

    checkpointComponent.update();
    verify(entityService, atLeastOnce()).getEntities();
    checkpointComponent.update();
    verify(entityService, atLeastOnce()).getEntities();
  }

  @Test
  void shouldContinueSearchingUntilItFindsThePlayer() {
    Array<Entity> candidates = new Array<>();
    candidates.add(new Entity());
    candidates.add(player);
    when(entityService.getEntities()).thenReturn(candidates);
    player.setPosition(10f, 10f);

    checkpointComponent.update();

    assertTrue(checkpointComponent.isActive());
  }

  @Test
  void shouldRemainInactiveWhenThereIsNoPlayer() {
    when(entityService.getEntities()).thenReturn(new Array<>());

    checkpointComponent.update();

    assertFalse(checkpointComponent.isActive());
  }

  @Test
  void shouldSwitchOwnAnimationOnActivateAndDeactivate() {
    TextureAtlas atlas = mock(TextureAtlas.class);
    Array<AtlasRegion> regions = new Array<>();
    regions.add(mock(AtlasRegion.class));
    when(atlas.findRegions(anyString())).thenReturn(regions);
    ServiceLocator.registerRenderService(new RenderService());

    AnimationRenderComponent animator = new AnimationRenderComponent(atlas);
    animator.addAnimation("unlit", 0.2f, Animation.PlayMode.LOOP);
    animator.addAnimation("lit", 0.2f, Animation.PlayMode.LOOP);
    CheckpointComponent checkpoint = new CheckpointComponent(false, new GridPoint2(0, 0));
    Entity entity = new Entity().addComponent(checkpoint).addComponent(animator);
    entity.create();
    animator.startAnimation("unlit");

    checkpoint.activate();
    assertEquals("lit", animator.getCurrentAnimation());
    checkpoint.deactivate();
    assertEquals("unlit", animator.getCurrentAnimation());
  }

  @Test
  void shouldActivateAndDeactivateItsLight() {
    PointLightComponent light = mock(PointLightComponent.class);
    CheckpointComponent checkpoint = new CheckpointComponent(false, new GridPoint2(10, 10));
    Entity entity = new Entity().addComponent(checkpoint).addComponent(light);
    entity.create();

    checkpoint.update();
    checkpoint.activate();
    checkpoint.activate();
    checkpoint.deactivate();

    verify(light).update();
    verify(light)
        .setColor(
            argThat(color -> color.equals(new Color(228f / 255f, 112f / 255f, 37f / 255f, 1f))));
    verify(light).setActive(true);
    verify(light).setActive(false);
  }

  @Test
  void shouldPlayActivateSoundOnlyTheFirstTime() {
    ResourceService resources = mock(ResourceService.class);
    Sound sound = mock(Sound.class);
    when(resources.containsAsset(CheckpointComponent.ACTIVATE_SOUND, Sound.class)).thenReturn(true);
    when(resources.getAsset(CheckpointComponent.ACTIVATE_SOUND, Sound.class)).thenReturn(sound);
    ServiceLocator.registerResourceService(resources);

    checkpointComponent.activate();
    checkpointComponent.deactivate();
    checkpointComponent.activate();

    verify(sound, times(1)).play(0.5f);
  }

  @Test
  void shouldNotPlayActivateSoundForAnAlreadyCollectedCheckpoint() {
    ResourceService resources = mock(ResourceService.class);
    Sound sound = mock(Sound.class);
    when(resources.containsAsset(CheckpointComponent.ACTIVATE_SOUND, Sound.class)).thenReturn(true);
    when(resources.getAsset(CheckpointComponent.ACTIVATE_SOUND, Sound.class)).thenReturn(sound);
    ServiceLocator.registerResourceService(resources);
    CheckpointComponent collected = new CheckpointComponent(true, new GridPoint2(5, 5));
    new Entity().addComponent(collected);

    collected.activate();

    verify(sound, never()).play(anyFloat());
  }
}
