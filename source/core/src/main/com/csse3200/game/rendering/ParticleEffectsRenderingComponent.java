package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.BurnStatsComponent;
import com.csse3200.game.components.SlowStatsComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

/**
 * Procedurally renders a fire or ice particle effect above an entity, built
 * out of small shapes ("pixels") drawn with {@link ShapeRenderer} rather than
 * a sprite sheet.
 *
 * <p>Fire is a handful of swaying flame tongues with twinkling embers; ice is
 * a handful of jagged crystal spikes with twinkling frost sparkles. Both are
 * colour-graded from a dark base to a bright tip, and animate purely as a
 * function of elapsed time so no per-instance simulation state is needed.
 */
public class ParticleEffectsRenderingComponent extends RenderComponent {

    private static final int pixelCount = 5;
    private static final int segmentsPerPixel = 6;

    private static final Color FIRE_BASE = new Color(0.55f, 0.05f, 0.02f, 1f);
    private static final Color FIRE_MID = new Color(0.95f, 0.35f, 0.05f, 1f);
    private static final Color FIRE_TIP = new Color(1f, 0.9f, 0.3f, 1f);

    private static final Color ICE_BASE = new Color(0.05f, 0.25f, 0.65f, 1f);
    private static final Color ICE_MID = new Color(0.25f, 0.6f, 0.95f, 1f);
    private static final Color ICE_TIP = new Color(0.85f, 0.95f, 1f, 1f);

    private BurnStatsComponent burnStats;
    private SlowStatsComponent slowStats;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final Color workingColour = new Color();

    @Override
    public void create() {
        super.create();
        burnStats = entity.getComponent(BurnStatsComponent.class);
        slowStats = entity.getComponent(SlowStatsComponent.class);
    }

    @Override
    protected void draw(SpriteBatch batch) {
        GameTime time = ServiceLocator.getTimeSource();
        if (time == null) {
            return;
        }

        boolean burning = burnStats != null && burnStats.isBurning();
        boolean slowed = slowStats != null && slowStats.isSlowed();
        if (!burning && !slowed) {
            return;
        }

        Vector2 centre = entity.getCenterPosition();
        Vector2 scale = entity.getScale();
        float baseSize = Math.max(scale.x / 10, scale.y / 10);
        float seconds = time.getTime() / 1000f;

        batch.end();
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeType.Filled);

        if (burning) {
            drawEffect(centre, baseSize, seconds, 11f, FIRE_BASE, FIRE_MID, FIRE_TIP);
        }
        if (slowed) {
            drawEffect(centre, baseSize, seconds, 53f, ICE_BASE, ICE_MID, ICE_TIP);
        }

        shapeRenderer.end();
        batch.begin();
    }

    /**
     * Draws {@value #pixelCount} tapering, swaying streaks rising from the
     * entity's centre, each built from small stacked quads colour-graded from
     * base -> mid -> tip.
     */
    private void drawEffect(
        Vector2 centre, float baseSize, float seconds, float seed,
        Color base, Color mid, Color tip) {
        for (int i = 0; i < pixelCount; i++) {
            float hash = MathUtils.random(seed + i * 17.3f);
            float xOffset = 0.5f * baseSize * 0.7f;
            float sway = MathUtils.sin(seconds * (1.6f + hash) + i) * baseSize * 0.08f;
            float height = baseSize * (0.55f + 0.4f * MathUtils.random(seed + i));
            float flicker = 0.85f + 0.15f * MathUtils.sin(seconds * (4f + hash * 3f) + i * 2f);
            height *= flicker;

            float pixel = Math.max(0.15f, baseSize * 0.05f);
            float startX = centre.x + xOffset;
            float startY = centre.y - baseSize * 0.1f;

            for (int s = 0; s < segmentsPerPixel; s++) {
                float t = s / (float) (segmentsPerPixel - 1);
                float segY = startY + height * t;
                float segSway = sway * t + MathUtils.sin(seconds * 3f + s + i) * pixel * 0.6f;
                float segX = startX + segSway;
                float segWidth = pixel * (1.6f - 1.1f * t);

                colourGradient(base, mid, tip, t, workingColour);
                shapeRenderer.setColor(workingColour);
                shapeRenderer.rect(segX - segWidth / 2f, segY, segWidth, pixel * 1.3f);
            }
        }
    }

    private static void colourGradient(Color base, Color mid, Color tip, float t, Color out) {
        if (t < 0.5f) {
            out.set(base).lerp(mid, t / 0.5f);
        } else {
            out.set(mid).lerp(tip, (t - 0.5f) / 0.5f);
        }
    }

    @Override
    public float getZIndex() {
        return super.getZIndex() + 0.01f;
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        super.dispose();
    }
}