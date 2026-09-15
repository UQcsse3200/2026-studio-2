package com.csse3200.game.input;

import com.badlogic.gdx.Input.Keys;

/** Routes TAB and mouse/touch release events to a cutscene advance action. */
public class CutsceneInputComponent extends InputComponent {
  private final Runnable advanceAction;

  public CutsceneInputComponent(Runnable advanceAction) {
    super(100);
    this.advanceAction = advanceAction;
  }

  @Override
  public boolean keyDown(int keycode) {
    if (keycode == Keys.TAB) {
      advanceAction.run();
      return true;
    }
    return false;
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    advanceAction.run();
    return true;
  }
}
