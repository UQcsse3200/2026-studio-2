package com.csse3200.game.components.minigames.CyclopsTimingBar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.csse3200.game.components.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyclopsMinigameLogicComponent extends Component {
  private static final Logger logger = LoggerFactory.getLogger(CyclopsMinigameLogicComponent.class);

  private final TimingBarLogic logic;

  public CyclopsMinigameLogicComponent(TimingBarLogic logic) {
    this.logic = logic;
  }

  @Override
  public void update() {
    // Check input and do stuff with timingBar etc
    if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
      logic.stopMarker();
    }

    if (logic != null && !logic.isStopped) {
      logic.update(Gdx.graphics.getDeltaTime());
    }
  }

  public void startMinigame() {}
}
