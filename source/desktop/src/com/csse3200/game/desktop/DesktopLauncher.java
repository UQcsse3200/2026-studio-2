package com.csse3200.game.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.csse3200.game.GdxGame;

/** This is the launch class for the desktop game. Passes control to libGDX to run GdxGame(). */
public class DesktopLauncher {
  private static final String GAME_NAME = "Odysseus";

  public static void main(String[] arg) {
    Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
    config.setTitle(GAME_NAME);
    // Several sizes let the OS pick the best fit for the title bar, taskbar and alt-tab.
    // GLFW ignores window icons on macOS, so the Dock icon is set separately once the game starts.
    config.setWindowIcon(
        Files.FileType.Internal,
        "images/ui/desktop_icon_256.png",
        "images/ui/desktop_icon_64.png",
        "images/ui/desktop_icon_32.png");
    new Lwjgl3Application(
        new GdxGame() {
          @Override
          public void create() {
            super.create();
            MacDockIcon.set("images/ui/desktop_icon_256.png");
          }
        },
        config);
  }
}
