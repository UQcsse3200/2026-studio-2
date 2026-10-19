package com.csse3200.game.entities.factories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.PoisonStatsComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.npc.GhostAnimationController;
import com.csse3200.game.components.npc.ShopNpcComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class NPCFactoryTest {
  @BeforeEach
  void setUp() {
    ServiceLocator.registerPhysicsService(new PhysicsService());
    RenderService renderService = mock(RenderService.class);
    when(renderService.getDebug())
        .thenReturn(mock(com.csse3200.game.rendering.DebugRenderer.class));
    ServiceLocator.registerRenderService(renderService);

    ResourceService resources = mock(ResourceService.class);
    TextureAtlas atlas = mockAtlas();
    when(resources.getAsset(anyString(), eq(TextureAtlas.class))).thenReturn(atlas);
    Texture texture = mock(Texture.class);
    when(texture.getWidth()).thenReturn(1);
    when(texture.getHeight()).thenReturn(1);
    when(resources.getAsset(anyString(), eq(Texture.class))).thenReturn(texture);
    ServiceLocator.registerResourceService(resources);
  }

  @Test
  void shouldCreateShopkeeperWithShopComponent() {
    Entity shopkeeper = NPCFactory.createShopkeeper();

    assertNotNull(shopkeeper.getComponent(ShopNpcComponent.class));
  }

  @Test
  void shouldUseConfiguredShopkeeperTexture() {
    Entity shopkeeper = NPCFactory.createShopkeeper();

    assertNotNull(shopkeeper.getComponent(TextureRenderComponent.class));
    org.junit.jupiter.api.Assertions.assertEquals(1.5f, shopkeeper.getScale().y);
  }

  @Test
  void shouldCreateGhostWithAnimationAndNpcComponents() {
    Entity ghost = NPCFactory.createGhost(new Entity());

    assertNotNull(ghost.getComponent(GhostAnimationController.class));
    assertNotNull(ghost.getComponent(AnimationRenderComponent.class));
    assertNpcBaseComponents(ghost);
  }

  @Test
  void shouldCreateZombieWithAnimationAndNpcComponents() {
    Entity zombie = NPCFactory.createZombie(new Entity());

    assertNotNull(zombie.getComponent(GhostAnimationController.class));
    assertNotNull(zombie.getComponent(AnimationRenderComponent.class));
    assertNpcBaseComponents(zombie);
  }

  @Test
  void shouldCreateGhostKingWithAnimationAndNpcComponents() {
    Entity ghostKing = NPCFactory.createGhostKing(new Entity());

    assertNotNull(ghostKing.getComponent(GhostAnimationController.class));
    assertNotNull(ghostKing.getComponent(AnimationRenderComponent.class));
    assertNpcBaseComponents(ghostKing);
  }

  @Test
  void shouldCreateNpcWithoutGravity() {
    Entity npc = NPCFactory.createGhost(new Entity());

    assertEquals(0f, npc.getComponent(PhysicsComponent.class).getBody().getGravityScale());
  }

  private static void assertNpcBaseComponents(Entity npc) {
    assertNotNull(npc.getComponent(PhysicsComponent.class));
    assertNotNull(npc.getComponent(PhysicsMovementComponent.class));
    assertNotNull(npc.getComponent(CombatStatsComponent.class));
    assertNotNull(npc.getComponent(PoisonStatsComponent.class));
    assertNotNull(npc.getComponent(TouchAttackComponent.class));
    assertNotNull(npc.getComponent(AITaskComponent.class));
  }

  private static TextureAtlas mockAtlas() {
    TextureAtlas atlas = mock(TextureAtlas.class);
    Array<AtlasRegion> regions = new Array<>();
    AtlasRegion region = mock(AtlasRegion.class);
    when(region.getRegionWidth()).thenReturn(1);
    when(region.getRegionHeight()).thenReturn(1);
    regions.add(region);
    when(atlas.findRegions(anyString())).thenReturn(regions);
    when(atlas.findRegion(anyString())).thenReturn(region);
    return atlas;
  }
}
