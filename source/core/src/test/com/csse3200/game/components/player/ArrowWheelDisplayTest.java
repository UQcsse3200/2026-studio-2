package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.csse3200.game.components.projectile.ArrowType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.ui.UiTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ArrowWheelDisplayTest extends UiTestSupport {
  private static final float FAR = ArrowType.DEADZONE_RADIUS * 3f;

  private ArrowWheelComponent wheel;
  private ArrowWheelDisplay display;
  private Entity player;

  @BeforeEach
  void setUp() {
    wheel = new ArrowWheelComponent();
    display = new ArrowWheelDisplay();
    player = register(new Entity().addComponent(wheel).addComponent(display));
  }

  @Test
  void shouldBeHiddenWhileTheWheelIsClosed() {
    display.draw(batch);

    assertFalse(root().isVisible());
  }

  @Test
  void shouldShowOneSlicePerArrowType() {
    int slices = 0;
    for (var child : root().getChildren()) {
      if (child instanceof Image image && image.getWidth() == image.getHeight()) {
        slices++;
      }
    }

    // Three pie slices and the same number of icons are the largest square images; the centre
    // disc and its icon are smaller.
    assertTrue(slices >= ArrowType.getWheelTypes().length);
  }

  @Test
  void shouldCentreTheWheelOnTheScreen() {
    player.getEvents().trigger("openArrowWheel");

    display.draw(batch);

    assertTrue(root().isVisible());
    Vector2 centre = wheel.getScreenCentre();
    assertNotNull(centre);
    assertEquals(stage.getWidth() / 2f, root().getX() + root().getWidth() / 2f, 0.01f);
    assertEquals(stage.getHeight() / 2f, root().getY() + root().getHeight() / 2f, 0.01f);
    Vector2 screenCentre =
        stage.stageToScreenCoordinates(new Vector2(stage.getWidth() / 2f, stage.getHeight() / 2f));
    assertEquals(screenCentre.x, centre.x, 0.01f);
    assertEquals(screenCentre.y, centre.y, 0.01f);
  }

  @Test
  void shouldHighlightTheSliceUnderThePointerAndHideWhenClosed() {
    player.getEvents().trigger("openArrowWheel");
    display.draw(batch);
    assertNull(wheel.getHighlighted());

    player.getEvents().trigger("arrowWheelPointerMoved", new Vector2(FAR, 0f));
    display.draw(batch);
    assertEquals(ArrowType.FIRE, wheel.getHighlighted());

    player.getEvents().trigger("closeArrowWheel");
    display.draw(batch);
    assertFalse(root().isVisible());
  }

  private Group root() {
    return field(display, "root", Group.class);
  }
}
