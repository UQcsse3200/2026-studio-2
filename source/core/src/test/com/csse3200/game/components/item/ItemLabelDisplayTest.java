package com.csse3200.game.components.item;

import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.item.weapons.bow.arrow.Arrow;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.ui.UiTestSupport;
import org.junit.jupiter.api.Test;

class ItemLabelDisplayTest extends UiTestSupport {
  @Test
  void shouldFollowItemInScreenCoordinatesAndRemoveLabelOnDisposal() {
    OrthographicCamera camera =
        new OrthographicCamera() {
          @Override
          public Vector3 project(Vector3 position) {
            return super.project(position, 0, 0, 1600, 900);
          }
        };
    camera.setToOrtho(false, 20f, 10f);
    ItemLabelDisplay display = new ItemLabelDisplay(new CameraComponent(camera));
    Entity item =
        new Entity()
            .addComponent(new ItemComponent(new Arrow(ItemType.FIRE_ARROW, 1)))
            .addComponent(display);
    item.setPosition(4f, 5f);
    item.setScale(2f, 3f);
    register(item);
    display.render(batch);
    Label label = (Label) stage.getActors().first();
    assertEquals("ID: 4\nFire Arrow", label.getText().toString());
    assertEquals(400f, label.getX() + label.getWidth() / 2f, 0.01f);
    assertEquals(728f, label.getY(), 0.01f);
    item.setPosition(6f, 5f);
    display.render(batch);
    assertEquals(560f, label.getX() + label.getWidth() / 2f, 0.01f);
    item.dispose();
    assertNull(label.getStage());
    assertEquals(0, stage.getActors().size);
  }
}
