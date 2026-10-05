package com.csse3200.game.desktop;

import static org.lwjgl.system.JNI.invokePPP;
import static org.lwjgl.system.JNI.invokePPPP;
import static org.lwjgl.system.JNI.invokePPPPP;
import static org.lwjgl.system.macosx.ObjCRuntime.objc_getClass;
import static org.lwjgl.system.macosx.ObjCRuntime.sel_getUid;

import com.badlogic.gdx.Gdx;
import java.nio.ByteBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Platform;
import org.lwjgl.system.macosx.ObjCRuntime;

/**
 * Sets the macOS Dock icon. GLFW ignores window icons on macOS and the {@code -Xdock:icon} JVM flag
 * only takes effect when AWT is running, which libGDX's LWJGL3 backend never starts, so the icon is
 * set through Cocoa directly: {@code [NSApp setApplicationIconImage:[[NSImage alloc]
 * initWithData:...]]}.
 */
final class MacDockIcon {
  private MacDockIcon() {}

  /**
   * Sets the Dock icon from an image in the assets folder. Does nothing on other platforms, and
   * never fails the game if the icon cannot be set. Call from the main thread once the window
   * exists.
   *
   * @param internalPath libGDX internal path of a PNG, for example "images/ui/icon.png"
   */
  static void set(String internalPath) {
    if (Platform.get() != Platform.MACOSX) {
      return;
    }

    ByteBuffer buffer = null;
    try {
      byte[] bytes = Gdx.files.internal(internalPath).readBytes();
      buffer = MemoryUtil.memAlloc(bytes.length);
      buffer.put(bytes).flip();

      long send = ObjCRuntime.getLibrary().getFunctionAddress("objc_msgSend");
      // NSData copies the bytes, so the buffer can be freed afterwards.
      long data =
          invokePPPPP(
              objc_getClass("NSData"),
              sel_getUid("dataWithBytes:length:"),
              MemoryUtil.memAddress(buffer),
              bytes.length,
              send);
      long image = invokePPP(objc_getClass("NSImage"), sel_getUid("alloc"), send);
      image = invokePPPP(image, sel_getUid("initWithData:"), data, send);
      long app = invokePPP(objc_getClass("NSApplication"), sel_getUid("sharedApplication"), send);
      if (image != 0 && app != 0) {
        invokePPPP(app, sel_getUid("setApplicationIconImage:"), image, send);
      }
    } catch (Exception | LinkageError e) {
      System.err.println("Could not set the Dock icon: " + e);
    } finally {
      if (buffer != null) {
        MemoryUtil.memFree(buffer);
      }
    }
  }
}
