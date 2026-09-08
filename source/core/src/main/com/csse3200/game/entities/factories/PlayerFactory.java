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
import com.csse3200.game.components.item.weapons.melee.MeleeComponent;
import com.csse3200.game.components.player.*;
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
import com.csse3200.game.rendering.item.MeleeRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Factory to create a player entity.
 */
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
    animator.addAnimation("idle", 0.15f, PlayMode.LOOP);
    animator.addAnimation("walk", 0.1f, PlayMode.LOOP);
    animator.addAnimation("sprint", 0.1f, PlayMode.LOOP);
    animator.addAnimation("jump", 0.05f, PlayMode.NORMAL);
    animator.addAnimation("hurt", 0.04f, PlayMode.NORMAL);

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
                    .addComponent(new MeleeComponent())
                    .addComponent(new WeaponComponent(bowComponent))
                    .addComponent(new InventoryComponent(stats.gold))
                    .addComponent(new InventoryBarDisplay())
                    .addComponent(new BackpackDisplay())
                    .addComponent(new PlayerInteractionComponent())
                    .addComponent(new ItemUseComponent())
                    .addComponent(inputComponent)
                    .addComponent(new PlayerStatsDisplay())
                    .addComponent(new GrappleComponent())
                    .addComponent(new GrappleRenderComponent())
                    .addComponent(new PlayerAnimationController())
                    .addComponent(new MeleeRenderComponent());

    PhysicsUtils.setScaledCollider(player, 0.6f, 0.3f);
    player.getComponent(ColliderComponent.class).setDensity(1.5f);
    player.getComponent(AnimationRenderComponent.class).scaleEntity();
    player.scaleWidth(0.75f);
    return player;
  }

  /**
   * Create a player display entity.
   *
   * @return entity
   */
  public static Entity createPlayerDisplay() {
    Entity player =
            new Entity().addComponent(new TextureRenderComponent("images/box_boy_leaf.png"));
    player.getComponent(TextureRenderComponent.class).scaleEntity();
    return player;
  }

  private PlayerFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}