package com.csse3200.game.components.minigames.CyclopsTimingBar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.csse3200.game.components.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyclopsMinigameLogicComponent extends Component {
  private static final Logger logger = LoggerFactory.getLogger(CyclopsMinigameLogicComponent.class);

  private final TimingBarLogic logic;
  private final TimingBarDisplay display;

  private boolean running;

  public CyclopsMinigameLogicComponent(TimingBarLogic logic, TimingBarDisplay display) {
    this.logic = logic;
    this.display = display;
    this.display.setVisible(false);
    this.logic.stopMarker();

    this.running = false;
  }

  @Override
  public void update() {
    if (running) {
      // Check input and do stuff with timingBar etc
      if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
        logger.info("sliding marker stopped");
        logic.stopMarker();
        running = false;
      }

      if (logic != null && !logic.isStopped) {
        logic.update(Gdx.graphics.getDeltaTime());
      }
    } else if (Gdx.input.isKeyPressed(Input.Keys.PERIOD)) { // DEV TOOL Sort of
      logger.info("DEV: activated timing bar");
      logic.startMarker();
      running = true;
    }
  }

  public void startMinigame() {
    logger.info("starting timing bar minigame");
    this.running = true;
    this.display.setVisible(true);
    this.logic.startMarker();
  }
}
