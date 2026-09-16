package com.csse3200.game.components.minigames;

/** Coordinates minigame overlays so only one can be active at a time. */
public class MinigameOverlayManager {
  private boolean active;

  /**
   * Attempts to claim the minigame overlay.
   *
   * @return true if no other minigame overlay is active
   */
  public boolean tryOpen() {
    if (active) {
      return false;
    }

    active = true;
    return true;
  }

  /** Releases the active minigame overlay. */
  public void close() {
    active = false;
  }

  /**
   * @return whether a minigame overlay is currently active
   */
  public boolean isActive() {
    return active;
  }
}
