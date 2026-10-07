package com.csse3200.game.components.level;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

/** Shows the destination only while the player is beside an optional doorway. */
public class RoomDoorDisplay extends UIComponent {
  private Table prompt;
  private RoomDoorComponent door;

  @Override
  public void create() {
    super.create();
    door = entity.getComponent(RoomDoorComponent.class);
    prompt = new Table();
    prompt.setFillParent(true);
    prompt.bottom().padBottom(145);
    Label label = new Label("F  -  " + door.getLabel(), skin);
    Texture panel =
        ServiceLocator.getResourceService().getAsset("images/ui/menu_box.png", Texture.class);
    TextureRegionDrawable paper =
        new TextureRegionDrawable(
            new TextureRegion(
                panel,
                panel.getWidth() / 4,
                panel.getHeight() / 4,
                panel.getWidth() / 2,
                panel.getHeight() / 2));
    paper.setMinWidth(0);
    paper.setMinHeight(0);
    Table badge = new Table();
    badge.setBackground(paper);
    badge.add(label).pad(10, 18, 10, 18);
    prompt.add(badge);
    prompt.setTouchable(Touchable.disabled);
    prompt.setVisible(false);
    stage.addActor(prompt);
  }

  @Override
  public void update() {
    prompt.setVisible(door.canEnter());
  }

  @Override
  protected void draw(SpriteBatch batch) {}

  @Override
  public void dispose() {
    if (prompt != null) prompt.remove();
    super.dispose();
  }
}
