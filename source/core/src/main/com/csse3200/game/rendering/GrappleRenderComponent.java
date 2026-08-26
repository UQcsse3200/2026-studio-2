package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.player.GrappleComponent;

/** Draws the grapple rope between the player and its anchor point. */
public class GrappleRenderComponent extends RenderComponent {

    /** Thickness of the rendered rope line in world units */
    private static final float LINE_WIDTH = 0.05f;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private GrappleComponent grapple;

    @Override
    public void create() {
        super.create();
        grapple = entity.getComponent(GrappleComponent.class);
    }

    /**
     * Draws the grapple rope line if the grapple is currently attached to a target surface.
     *
     * @param batch Active SpriteBatch used by the main render pipeline
     */
    @Override
    protected void draw(SpriteBatch batch) {
        if (grapple == null || !grapple.isAttached()) {
            return;
        }
        Vector2 anchor = grapple.getAnchorPoint();
        if (anchor == null) {
            return;
        }
        Vector2 playerPos = entity.getCenterPosition();

        // Pause standard sprite rendering to avoid pipeline conflict with primitive geometry
        batch.end();

        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(Color.BROWN);
        shapeRenderer.rectLine(playerPos.x, playerPos.y, anchor.x, anchor.y, LINE_WIDTH);
        shapeRenderer.end();

        batch.begin();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        super.dispose();
    }
}