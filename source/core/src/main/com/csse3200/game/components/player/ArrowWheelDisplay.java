package com.csse3200.game.components.player;
import com.csse3200.game.ui.UIComponent;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Table;


public class ArrowWheelDisplay extends UIComponent {
  private ArrowWheelComponent arrowWheel;

  private Table root;

  @Override
  public void create() {
    arrowWheel = entity.getComponent(ArrowWheelComponent.class);
    root = new Table();
  }

  @Override 
    public void draw(SpriteBatch batch) {
       
    }

  @Override 
  public void dispose() {
    super.dispose();
  }
    
}
