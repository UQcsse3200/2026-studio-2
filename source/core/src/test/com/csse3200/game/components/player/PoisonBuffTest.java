package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class PoisonBuffTest {
  private static final float DAMAGE_PER_SECOND = 7f;
  private static final float DURATION_SECONDS = 5f;

  private GameTime gameTime;
  private PoisonBuff buff;
  private Entity player;

  @BeforeEach
  void setUp() {
    gameTime = mock(GameTime.class);
    ServiceLocator.registerTimeSource(gameTime);
    buff = new PoisonBuff();
    player = new Entity().addComponent(buff);
    player.create();
  }

  @Test
  void shouldStartInactive() {
    when(gameTime.getTime()).thenReturn(0L);

    assertFalse(buff.isActive());
    assertEquals(0f, buff.getPoisonDamagePerSecond());
    assertEquals(0f, buff.getPoisonDuration());
  }

  @Test
  void shouldApplyTheBuffWhenAPoisonPotionIsUsed() {
    when(gameTime.getTime()).thenReturn(0L);

    player.getEvents().trigger("poisonPotionUsed", DAMAGE_PER_SECOND, DURATION_SECONDS);

    assertTrue(buff.isActive());
    assertEquals(DAMAGE_PER_SECOND, buff.getPoisonDamagePerSecond());
    assertEquals(DURATION_SECONDS, buff.getPoisonDuration());
  }

  @Test
  void shouldStayActiveUntilTheDurationHasPassed() {
    // Applied at 0ms for 5s, so it is still live at 4999ms and over at 5000ms.
    when(gameTime.getTime()).thenReturn(0L, 4999L, 5000L);

    player.getEvents().trigger("poisonPotionUsed", DAMAGE_PER_SECOND, DURATION_SECONDS);

    assertTrue(buff.isActive());
    assertFalse(buff.isActive());
  }

  @Test
  void shouldReportNothingOnceItHasExpired() {
    when(gameTime.getTime()).thenReturn(0L, 9000L, 9000L);

    player.getEvents().trigger("poisonPotionUsed", DAMAGE_PER_SECOND, DURATION_SECONDS);

    assertEquals(0f, buff.getPoisonDamagePerSecond());
    assertEquals(0f, buff.getPoisonDuration());
  }

  @Test
  void shouldKeepMillisecondPrecisionOverALongSession() {
    // A float cannot hold every millisecond past about 16.7 million, so a long end time is what
    // keeps a buff applied hours in from expiring at the wrong moment.
    long lateStart = 50_000_000L;
    when(gameTime.getTime()).thenReturn(lateStart, lateStart + 4999L, lateStart + 5000L);

    player.getEvents().trigger("poisonPotionUsed", DAMAGE_PER_SECOND, DURATION_SECONDS);

    assertTrue(buff.isActive());
    assertFalse(buff.isActive());
  }

  @Test
  void shouldIgnoreNegativeDamage() {
    when(gameTime.getTime()).thenReturn(0L);

    player.getEvents().trigger("poisonPotionUsed", -1f, DURATION_SECONDS);

    assertFalse(buff.isActive());
  }

  @Test
  void shouldIgnoreNegativeDuration() {
    when(gameTime.getTime()).thenReturn(0L);

    player.getEvents().trigger("poisonPotionUsed", DAMAGE_PER_SECOND, -1f);

    assertFalse(buff.isActive());
  }

  @Test
  void shouldReplaceAnEarlierBuffWithTheLatestOne() {
    when(gameTime.getTime()).thenReturn(0L);

    player.getEvents().trigger("poisonPotionUsed", DAMAGE_PER_SECOND, DURATION_SECONDS);
    player.getEvents().trigger("poisonPotionUsed", 2f, 9f);

    assertEquals(2f, buff.getPoisonDamagePerSecond());
    assertEquals(9f, buff.getPoisonDuration());
  }
}
