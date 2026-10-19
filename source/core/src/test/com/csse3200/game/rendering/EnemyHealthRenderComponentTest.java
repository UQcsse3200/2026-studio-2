package com.csse3200.game.rendering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

/** The health bar above an enemy: a black border, a grey backing, and red for what is left. */
@ExtendWith(GameExtension.class)
class EnemyHealthRenderComponentTest {
  // The bar is three quarters of the enemy's width, sits 0.1 above its head, is 0.05 tall, with a
  // border of 0.02 all round
  private static final float BAR_HEIGHT = 0.05f;
  private static final float BORDER = 0.02f;
  private static final float TOLERANCE = 1e-4f;

  private SpriteBatch batch;
  private Entity enemy;
  private EnemyHealthRenderComponent bar;

  @BeforeEach
  void setUp() {
    ServiceLocator.registerRenderService(mock(RenderService.class));
    batch = mock(SpriteBatch.class);
    enemy = enemyWithHealth(20, 20);
  }

  private Entity enemyWithHealth(int health, int maxHealth) {
    bar = new EnemyHealthRenderComponent();
    Entity entity =
        new Entity().addComponent(new CombatStatsComponent(health, maxHealth, 1)).addComponent(bar);
    entity.setPosition(2f, 3f);
    entity.setScale(2f, 1f);
    entity.create();
    return entity;
  }

  private void drawAndCapture(
      ArgumentCaptor<Float> x,
      ArgumentCaptor<Float> y,
      ArgumentCaptor<Float> width,
      ArgumentCaptor<Float> height) {
    bar.draw(batch);
    verify(batch, times(3))
        .draw(any(Texture.class), x.capture(), y.capture(), width.capture(), height.capture());
  }

  private float[] drawn(int index) {
    ArgumentCaptor<Float> x = ArgumentCaptor.forClass(Float.class);
    ArgumentCaptor<Float> y = ArgumentCaptor.forClass(Float.class);
    ArgumentCaptor<Float> w = ArgumentCaptor.forClass(Float.class);
    ArgumentCaptor<Float> h = ArgumentCaptor.forClass(Float.class);
    drawAndCapture(x, y, w, h);
    return new float[] {
      x.getAllValues().get(index),
      y.getAllValues().get(index),
      w.getAllValues().get(index),
      h.getAllValues().get(index)
    };
  }

  @Test
  void shouldDrawABorderAroundTheWholeBar() {
    float[] border = drawn(0);

    // Bar: width 2 * 0.75 = 1.5, centred over the enemy, 0.1 above its top (y = 3 + 1)
    assertEquals(2.25f - BORDER, border[0], TOLERANCE);
    assertEquals(4.1f - BORDER, border[1], TOLERANCE);
    assertEquals(1.5f + 2 * BORDER, border[2], TOLERANCE);
    assertEquals(BAR_HEIGHT + 2 * BORDER, border[3], TOLERANCE);
  }

  @Test
  void shouldDrawTheBackingExactlyInsideTheBorder() {
    float[] backing = drawn(1);

    assertEquals(2.25f, backing[0], TOLERANCE);
    assertEquals(4.1f, backing[1], TOLERANCE);
    assertEquals(1.5f, backing[2], TOLERANCE);
    assertEquals(BAR_HEIGHT, backing[3], TOLERANCE);
  }

  @Test
  void shouldFillTheWholeBarWhenAtFullHealth() {
    float[] remaining = drawn(2);

    assertEquals(2.25f, remaining[0], TOLERANCE);
    assertEquals(1.5f, remaining[2], TOLERANCE, "all of the bar");
  }

  @Test
  void shouldShrinkTheRedPartInProportionToHealthLost() {
    enemy.getEvents().trigger("updateHealth", 5); // a quarter of 20

    float[] remaining = drawn(2);

    assertEquals(1.5f / 4f, remaining[2], TOLERANCE);
    assertEquals(
        2.25f, remaining[0], TOLERANCE, "it empties from the right, so the left stays put");
  }

  @Test
  void shouldShowAnEmptyBarAtZeroHealth() {
    enemy.getEvents().trigger("updateHealth", 0);

    assertEquals(0f, drawn(2)[2], TOLERANCE);
  }

  @Test
  void shouldFollowEachHealthUpdateInTurn() {
    enemy.getEvents().trigger("updateHealth", 10);
    enemy.getEvents().trigger("updateHealth", 15);

    assertEquals(1.5f * 15 / 20, drawn(2)[2], TOLERANCE, "the latest value wins");
  }

  @Test
  void shouldStartFullEvenBeforeAnyHealthUpdate() {
    // Starts from the enemy's maximum health, so a freshly spawned enemy shows a full bar
    enemy = enemyWithHealth(8, 20);

    assertEquals(1.5f, drawn(2)[2], TOLERANCE);
  }

  @Test
  void shouldMoveWithTheEnemy() {
    enemy.setPosition(10f, 6f);

    float[] backing = drawn(1);

    assertEquals(10.25f, backing[0], TOLERANCE);
    assertEquals(7.1f, backing[1], TOLERANCE);
  }

  @Test
  void shouldScaleWithTheEnemysSize() {
    enemy.setScale(4f, 3f); // at (2, 3)

    float[] backing = drawn(1);

    assertEquals(3f, backing[2], TOLERANCE, "three quarters of 4");
    assertEquals(2.5f, backing[0], TOLERANCE, "centred: 2 + (4 - 3) / 2");
    assertEquals(6.1f, backing[1], TOLERANCE, "above the taller head: 3 + 3 + 0.1");
  }

  @Test
  void shouldPaintBorderThenBackingThenHealthThenResetTheColour() {
    bar.draw(batch);

    InOrder order = inOrder(batch);
    order.verify(batch).setColor(Color.BLACK);
    order.verify(batch).setColor(Color.DARK_GRAY);
    order.verify(batch).setColor(Color.RED);
    order.verify(batch).setColor(Color.WHITE);
  }

  @Test
  void shouldUseOneSharedTextureForEveryBar() {
    ArgumentCaptor<Texture> first = ArgumentCaptor.forClass(Texture.class);
    bar.draw(batch);
    verify(batch, times(3)).draw(first.capture(), anyFloat(), anyFloat(), anyFloat(), anyFloat());

    SpriteBatch otherBatch = mock(SpriteBatch.class);
    enemyWithHealth(10, 10);
    bar.draw(otherBatch);
    ArgumentCaptor<Texture> second = ArgumentCaptor.forClass(Texture.class);
    verify(otherBatch, times(3))
        .draw(second.capture(), anyFloat(), anyFloat(), anyFloat(), anyFloat());

    assertSame(first.getValue(), second.getValue());
  }
}
