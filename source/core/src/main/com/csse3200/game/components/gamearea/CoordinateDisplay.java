package com.csse3200.game.components.gamearea;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

/**
 * Debug overlay showing the player's position and the world position under the mouse, in the same
 * units used by the level configs. Only visible while debug mode is on (F3, or the terminal's
 * "debug on" command).
 */
public class CoordinateDisplay extends UIComponent {
  private static final float Z_INDEX = 5f;
  private final CameraComponent camera;
  private Label label;
  private Entity player;

  public CoordinateDisplay(CameraComponent camera) {
    this.camera = camera;
  }

  @Override
  public void create() {
    super.create();
    label = new Label("", skin, "small");
    stage.addActor(label);
  }

  @Override
  public void draw(SpriteBatch batch) {
    if (!ServiceLocator.getRenderService().getDebug().getActive()) {
      label.setVisible(false);
      return;
    }
    label.setVisible(true);
    label.setText(getText());
    label.setPosition(5f, stage.getViewport().getScreenHeight() - 240f);
  }

  private String getText() {
    Vector3 mouse =
        camera.getCamera().unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f));
    String text = String.format("Mouse: %.1f, %.1f%n", mouse.x, mouse.y);

    Entity p = findPlayer();
    if (p != null) {
      Vector2 pos = p.getPosition();
      text += String.format("Player: %.1f, %.1f", pos.x, pos.y);
    }
    return text;
  }

  private Entity findPlayer() {
    if (player == null || player.getComponent(PlayerActions.class) == null) {
      player = null;
      for (Entity e : ServiceLocator.getEntityService().getEntities()) {
        if (e.getComponent(PlayerActions.class) != null) {
          player = e;
          break;
        }
      }
    }
    return player;
  }

  @Override
  public float getZIndex() {
    return Z_INDEX;
  }

  @Override
  public void dispose() {
    super.dispose();
    label.remove();
  }
}
