package com.csse3200.game.entities.factories;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

/**
 * Temporary test factory for creating a static ceiling entity, used to test player
 * grapple before real terrain exists
 */
public class TestCeilingFactory {

    public static Entity createTestCeiling() {
        Entity ceiling = new Entity();
        ceiling.addComponent(new TextureRenderComponent("images/black_box.png"));
        ceiling.addComponent(new PhysicsComponent().setBodyType(BodyType.StaticBody));
        ceiling.addComponent(new ColliderComponent().setLayer(PhysicsLayer.GROUND));
        ceiling.setScale(8f, 0.5f);
        return ceiling;
    }

    private TestCeilingFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }
}