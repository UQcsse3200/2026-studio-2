package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.inventory.BackpackDisplay;
import com.csse3200.game.components.inventory.InventoryBarDisplay;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.weapons.WeaponComponent;
import com.csse3200.game.components.item.weapons.bow.BowComponent;
import com.csse3200.game.components.item.weapons.bow.grapple.GrappleComponent;
import com.csse3200.game.components.itemdictionary.ItemDictionaryComponent;
import com.csse3200.game.components.itemdictionary.ItemDictionaryDisplay;
import com.csse3200.game.components.level.RespawnComponent;
import com.csse3200.game.components.player.*;
import com.csse3200.game.components.shop.ShopComponent;
import com.csse3200.game.components.shop.ShopDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.PlayerConfig;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.item.GrappleRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/** Factory to create a player entity. */
public class PlayerFactory {
  private static final PlayerConfig stats =
      FileLoader.readClass(PlayerConfig.class, "configs/player.json");

  /**
   * Create a player entity.
   *
   * @return entity
   */
  public static Entity createPlayer() {
    InputComponent inputComponent =
        ServiceLocator.getInputService().getInputFactory().createForPlayer();
    BowComponent bowComponent = new BowComponent();

    AnimationRenderComponent animator =
        new AnimationRenderComponent(
            ServiceLocator.getResourceService()
                .getAsset("images/player.atlas", TextureAtlas.class));
    animator.addAnimation("idle", 0.2f, PlayMode.LOOP);
    animator.addAnimation("walk", 0.1f, PlayMode.LOOP);
    animator.addAnimation("sprint", 0.125f, PlayMode.LOOP);
    animator.addAnimation("jump", 0.075f, PlayMode.NORMAL);
    animator.addAnimation("hurt", 0.04f, PlayMode.NORMAL);
    animator.addAnimation("death", 0.1458f, PlayMode.NORMAL);
    animator.addAnimation("sleep", 0.1458f, PlayMode.LOOP);
    animator.addAnimation("dash", 0.025f, PlayMode.NORMAL, 134.5f, 39f);
    animator.addAnimation("air_dash", 0.025f, PlayMode.NORMAL, 94f, 39f);
    animator.addAnimation("bow_draw", 0.08f, PlayMode.NORMAL, 72f, 23f);
    animator.addAnimation("bow_hold", 0.1f, PlayMode.LOOP, 72f, 24f);
    animator.addAnimation("bow_shoot", 0.05f, PlayMode.NORMAL, 71f, 23f);

    Entity player =
        new Entity()
            .addComponent(animator)
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent())
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.PLAYER))
            .addComponent(new PlayerActions())
            .addComponent(
                new CombatStatsComponent(
                    stats.health, stats.baseAttack, stats.invulnerabilityDuration))
            .addComponent(bowComponent)
            .addComponent(new PoisonBuff())
            .addComponent(new ArrowWheelComponent())
            .addComponent(new WeaponComponent(bowComponent))
            .addComponent(new InventoryComponent(stats.gold))
            .addComponent(new InventoryBarDisplay())
            .addComponent(new BackpackDisplay())
            .addComponent(new ItemDictionaryComponent())
            .addComponent(new ItemDictionaryDisplay())
            .addComponent(new PlayerInteractionComponent())
            .addComponent(new ShopComponent())
            .addComponent(new ShopDisplay())
            .addComponent(new ItemUseComponent())
            .addComponent(inputComponent)
            .addComponent(new PlayerStatsDisplay())
            .addComponent(new GrappleComponent())
            .addComponent(new GrappleRenderComponent())
            .addComponent(new PlayerAnimationController())
            .addComponent(new RespawnComponent());

    player.getComponent(ColliderComponent.class).setDensity(1.5f);
    player.getComponent(AnimationRenderComponent.class).scaleEntity();
    player.scaleWidth(0.6f);
    PhysicsUtils.setScaledCollider(player, 1f, 1f);
    return player;
  }

  /**
   * Create a player display entity.
   *
   * <p>Takes away specific components from the user that are not needed in particular situations
   * like minigames and cutscenes.
   *
   * <p>Currently removed: - Grappling Components - Player Actions Components - Bow Components
   *
   * @return entity
   */
  public static Entity createPlayerDisplay() {
    InputComponent inputComponent =
        ServiceLocator.getInputService().getInputFactory().createForPlayer();

    AnimationRenderComponent animator =
        new AnimationRenderComponent(
            ServiceLocator.getResourceService()
                .getAsset("images/player.atlas", TextureAtlas.class));
    animator.addAnimation("idle", 0.15f, PlayMode.LOOP);
    animator.addAnimation("walk", 0.1f, PlayMode.LOOP);
    animator.addAnimation("sprint", 0.1f, PlayMode.LOOP);
    animator.addAnimation("jump", 0.05f, PlayMode.NORMAL);
    animator.addAnimation("hurt", 0.04f, PlayMode.NORMAL);
    animator.addAnimation("death", 0.1458f, PlayMode.NORMAL);
    animator.addAnimation("sleep", 0.1458f, PlayMode.LOOP);

    Entity player =
        new Entity()
            .addComponent(animator)
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent())
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.PLAYER))
            .addComponent(
                new CombatStatsComponent(
                    stats.health, stats.baseAttack, stats.invulnerabilityDuration))
            .addComponent(new PlayerStatsDisplay())
            .addComponent(new PlayerAnimationController())
            .addComponent(new RespawnComponent());

    PhysicsUtils.setScaledCollider(player, 0.6f, 0.3f);
    player.getComponent(ColliderComponent.class).setDensity(1.5f);
    player.getComponent(AnimationRenderComponent.class).scaleEntity();
    player.scaleWidth(0.75f);

    return player;
  }

  private PlayerFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
