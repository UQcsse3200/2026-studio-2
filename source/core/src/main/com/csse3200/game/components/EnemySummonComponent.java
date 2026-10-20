package com.csse3200.game.components;

import com.csse3200.game.entities.Entity;

public class EnemySummonComponent extends Component {
  private boolean summoned;
  private Entity summoner;

  public EnemySummonComponent(boolean summoned) {
    this.summoned = summoned;
  }

  public void setSummoner(Entity enemy) {
    summoner = enemy;
  }

  public Entity getSummoner() {
    return summoner;
  }

  public void setSummonedStatus(boolean status) {
    summoned = status;
  }

  public boolean getSummonedStatus() {
    return summoned;
  }
}
