package com.csse3200.game.areas;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.minigames.cyclopsminigame.CyclopsAnimationController;
import com.csse3200.game.components.minigames.cyclopsminigame.CyclopsCameraFollowComponent;
import com.csse3200.game.components.minigames.cyclopsminigame.CyclopsFloorRenderComponent;
import com.csse3200.game.components.minigames.cyclopsminigame.CyclopsHurtSoundComponent;
import com.csse3200.game.components.minigames.cyclopsminigame.CyclopsMinigameLogic;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;

@ExtendWith(GameExtension.class)
class CyclopsMinigameAreaTest {
  private static final String MUSIC = "sounds/minigames/cyclops/cave_background_noise.mp3";

  private Audio previousAudio;
  private Input previousInput;
  private Music music;
  private GameTime time;
  private PhysicsService physics;
  private EntityService entities;
  private ResourceService resources;
  private Stage stage;
  private CameraComponent camera;
  private CyclopsMinigameArea area;
  private List<Entity> owned;
  private MockedConstruction<OrthogonalTiledMapRenderer> nativeTerrain;

  @BeforeEach
  void createRoom() {
    previousAudio = Gdx.audio;
    previousInput = Gdx.input;
    Audio audio = mock(Audio.class);
    music = mock(Music.class);
    when(audio.newMusic(any())).thenReturn(music);
    when(audio.newSound(any())).thenAnswer(call -> mock(Sound.class));
    Gdx.audio = audio;
    Gdx.input = mock(Input.class);
    time = mock(GameTime.class);
    when(time.getDeltaTime()).thenReturn(0.4f);
    ServiceLocator.registerTimeSource(time);
    physics = new PhysicsService();
    ServiceLocator.registerPhysicsService(physics);
    entities = new EntityService();
    ServiceLocator.registerEntityService(entities);
    resources = new ResourceService();
    ServiceLocator.registerResourceService(resources);
    ServiceLocator.registerCyclopsMinigameEventHandler(new EventHandler());
    RenderService renders = new RenderService();
    ServiceLocator.registerRenderService(renders);
    SpriteBatch batch = mock(SpriteBatch.class);
    stage = new Stage(new ScreenViewport(), batch);
    stage.getViewport().update(1600, 900, true);
    renders.setStage(stage);
    camera = new CameraComponent();
    camera.resize(1600, 900, 20f);
    area = new CyclopsMinigameArea(camera, new TerrainFactory(camera));
    // The screen owns this UI texture; every other asset is loaded by the actual area.
    resources.loadTextures(new String[] {"images/health/red_heart.png"});
    resources.loadAll();

    nativeTerrain = mockConstruction(OrthogonalTiledMapRenderer.class);
    area.create();
    owned = List.copyOf(area.areaEntities);
  }

  @AfterEach
  void disposeRoom() {
    try {
      if (area != null && !area.areaEntities.isEmpty()) area.dispose();
      if (entities != null) entities.dispose();
      if (physics != null) physics.getPhysics().dispose();
      if (resources != null) resources.dispose();
      if (stage != null) stage.dispose();
    } finally {
      if (nativeTerrain != null) nativeTerrain.close();
      Gdx.audio = previousAudio;
      Gdx.input = previousInput;
    }
  }

  @Test
  void createsPlayableRoomWithAlignedRocksCameraAndMusic() {
    Entity player = find(owned, CyclopsHurtSoundComponent.class);
    Entity cyclops = find(owned, CyclopsAnimationController.class);
    Entity cameraHolder = find(owned, CyclopsCameraFollowComponent.class);
    assertNotNull(find(owned, CyclopsFloorRenderComponent.class));
    assertEquals(0.5f, area.getTerrain().getTileSize());
    assertEquals(0.9f, player.getScale().x, 0.0001f);
    assertEquals(2.35f, player.getPosition().x, 0.0001f);
    assertEquals(1.5f, player.getPosition().y, 0.0001f);
    assertEquals(
        "sleep", cyclops.getComponent(AnimationRenderComponent.class).getCurrentAnimation());
    List<Entity> formations =
        owned.stream()
            .filter(
                entity ->
                    entity.getComponent(TextureRenderComponent.class) != null
                        && entity.getComponent(PhysicsComponent.class) != null)
            .toList();
    assertEquals(6, formations.size());
    for (int i = 0; i < formations.size(); i++) {
      Entity formation = formations.get(i);
      assertEquals(2.75f + 6.5f * i, formation.getCenterPosition().x, 0.0001f);
      assertEquals(1.51f, formation.getPosition().y, 0.0001f);
      assertEquals(player.getScale().y * 1.7f, formation.getScale().y, 0.0001f);
      assertTrue(
          player.getComponent(AnimationRenderComponent.class).getZIndex()
              > formation.getComponent(TextureRenderComponent.class).getZIndex());
    }
    cameraHolder.update();
    camera.update();
    assertEquals(10f, camera.getCamera().position.x, 0.0001f);
    assertEquals(4.875f, camera.getCamera().position.y, 0.0001f);
    verify(music).setLooping(true);
    verify(music).play();
  }

  @Test
  void configuredCourseSupportsCompletionRestartAndMisses() {
    Entity player = find(owned, CyclopsHurtSoundComponent.class);
    Entity cameraHolder = find(owned, CyclopsCameraFollowComponent.class);
    CyclopsMinigameLogic logic =
        find(owned, CyclopsMinigameLogic.class).getComponent(CyclopsMinigameLogic.class);
    // Drive the configured locations through the public event bus, retaining the player offset.
    ServiceLocator.getCyclopsMinigameEventHandler().trigger("start");
    for (int rock = 1; rock <= 6; rock++) {
      when(time.getDeltaTime()).thenReturn(0.4f);
      logic.update();
      ServiceLocator.getCyclopsMinigameEventHandler().trigger("success");
      when(time.getDeltaTime()).thenReturn(1f);
      logic.update();
      float expectedX = rock < 6 ? 2.35f + 6.5f * rock : 37.85f;
      assertEquals(expectedX, player.getPosition().x, 0.0001f);
      assertEquals(1.5f, player.getPosition().y, 0.0001f);
    }
    assertFalse(logic.areNextSafeLocations());
    cameraHolder.update();
    camera.update();
    assertEquals(30f, camera.getCamera().position.x, 0.0001f);

    // Restart must use the first rock and the same offset; a miss targets the gap after it.
    ServiceLocator.getCyclopsMinigameEventHandler().trigger("restart");
    assertEquals(2.35f, player.getPosition().x, 0.0001f);
    logic.update();
    ServiceLocator.getCyclopsMinigameEventHandler().trigger("failure");
    logic.update();
    assertEquals(5.35f, player.getPosition().x, 0.0001f);
    int health = player.getComponent(CombatStatsComponent.class).getHealth();
    ServiceLocator.getCyclopsMinigameEventHandler().trigger(CyclopsMinigameLogic.CYCLOPS_HIT_EVENT);
    assertEquals(health - 2, player.getComponent(CombatStatsComponent.class).getHealth());
  }

  @Test
  void disposesOwnedEntitiesPhysicsAndAssetsWhilePreservingScreenAssets() {
    area.dispose();
    assertTrue(area.areaEntities.isEmpty());
    assertTrue(owned.stream().noneMatch(entity -> entities.getEntities().contains(entity, true)));
    assertEquals(0, physics.getPhysics().getWorld().getBodyCount());
    verify(music).stop();
    assertFalse(resources.containsAsset(MUSIC, Music.class));
    assertFalse(resources.containsAsset(CyclopsMinigameArea.CAVE_FORMATION_1, Texture.class));
    assertTrue(resources.containsAsset("images/health/red_heart.png", Texture.class));
  }

  private static Entity find(
      List<Entity> entities, Class<? extends com.csse3200.game.components.Component> type) {
    return entities.stream()
        .filter(entity -> entity.getComponent(type) != null)
        .findFirst()
        .orElseThrow();
  }
}
