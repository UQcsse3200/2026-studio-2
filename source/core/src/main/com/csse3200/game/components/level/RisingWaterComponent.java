package com.csse3200.game.components.level;

import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ServiceLocator;

public class RisingWaterComponent extends Component {
    float speed;
    float initialHeight;
    float currentHeight;

    /**
     * Creates a new rising water component for an entity
     * @param speed how much the water should rise each second
     * @param initialHeight what y level the water should start rising from
     */
    public RisingWaterComponent(float speed, float initialHeight) {
        this.speed = speed;
        this.initialHeight = initialHeight;
        currentHeight = initialHeight;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getSpeed() {
        return speed;
    }

    public void setCurrentHeight(float currentHeight) {
        this.currentHeight = currentHeight;
    }

    public float getCurrentHeight() {
        return currentHeight;
    }

    @Override
    public void create() {
        RenderService renderer = ServiceLocator.getRenderService();
        entity.setScale(renderer.getStage().getWidth(), initialHeight);
        entity.getEvents().addListener("changeSpeed", this::setSpeed);
        entity.getEvents().addListener("setHeight", this::setCurrentHeight);
    }

    @Override
    public void update() {
        // increases the height of this entity based on the speed and delta time
        currentHeight += (speed / 60f) * ServiceLocator.getTimeSource().getDeltaTime();
        entity.setScale(entity.getScale().x, currentHeight);
    }
}
