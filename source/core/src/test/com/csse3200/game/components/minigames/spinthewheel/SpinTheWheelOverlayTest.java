package com.csse3200.game.components.minigames.spinthewheel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.components.minigames.MinigameOverlayManager;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.input.InputService;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.ScreenBlur;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

@ExtendWith(GameExtension.class)
class SpinTheWheelOverlayTest {
  private static final List<WheelItem> ITEMS = List.of(new WheelItem(ItemType.STANDARD_ARROW, 3));

  private Stage stage;
  private EntityService entityService;
  private MockedStatic<ScreenBlur> blur;
  private Entity player;

  @BeforeEach
  void setUp() {
    stage = new Stage(new StretchViewport(800, 600), mock(SpriteBatch.class));
    RenderService renderService = mock(RenderService.class);
    when(renderService.getStage()).thenReturn(stage);
    ServiceLocator.registerRenderService(renderService);

    ResourceService resources = mock(ResourceService.class);
    when(resources.getAsset(anyString(), eq(Texture.class))).thenReturn(mock(Texture.class));
    ServiceLocator.registerResourceService(resources);

    PhysicsService physicsService = mock(PhysicsService.class);
    when(physicsService.getPhysics()).thenReturn(mock(PhysicsEngine.class));
    ServiceLocator.registerPhysicsService(physicsService);
    ServiceLocator.registerTimeSource(new GameTime());
    ServiceLocator.registerInputService(new InputService());

    entityService = new EntityService();
    ServiceLocator.registerEntityService(entityService);

    // Capturing the frame needs a real screen, so the blurred snapshot is faked
    TextureRegion snapshot = new TextureRegion(mock(Texture.class));
    blur = mockStatic(ScreenBlur.class);
    blur.when(ScreenBlur::capture).thenReturn(snapshot);

    player = new Entity().addComponent(new InventoryComponent(0, 3));
  }

  @AfterEach
  void tearDown() {
    blur.close();
    stage.dispose();
  }

  @Test
  void shouldPauseTheGameWhenRequested() {
    new SpinTheWheelOverlay(ITEMS, player).request();

    assertTrue(entityService.getPaused());
    assertNull(openWheel());
  }

  @Test
  void shouldOpenAtTheEndOfTheFrame() {
    SpinTheWheelOverlay overlay = new SpinTheWheelOverlay(ITEMS, player);

    overlay.request();
    overlay.afterRender();

    assertNotNull(openWheel());
  }

  @Test
  void shouldStayClosedWhenNotRequested() {
    new SpinTheWheelOverlay(ITEMS, player).afterRender();

    assertNull(openWheel());
    assertFalse(entityService.getPaused());
  }

  @Test
  void shouldOpenOnlyOnce() {
    SpinTheWheelOverlay overlay = new SpinTheWheelOverlay(ITEMS, player);

    overlay.request();
    overlay.request();
    overlay.afterRender();
    overlay.request();
    overlay.afterRender();

    assertEquals(1, entityService.getEntities().size);
  }

  @Test
  void shouldNotOpenOverAnotherMinigame() {
    MinigameOverlayManager manager = new MinigameOverlayManager();
    manager.tryOpen();
    SpinTheWheelOverlay overlay = new SpinTheWheelOverlay(ITEMS, player, manager);

    overlay.request();
    overlay.afterRender();

    assertNull(openWheel());
    assertFalse(entityService.getPaused());
  }

  @Test
  void shouldGiveThePrizeToThePlayer() {
    SpinTheWheelOverlay overlay = new SpinTheWheelOverlay(ITEMS, player);
    overlay.request();
    overlay.afterRender();

    openWheel().getComponent(SpinTheWheelDisplay.class).award(ITEMS.get(0));

    assertEquals(
        3, player.getComponent(InventoryComponent.class).getItemCount(ItemType.STANDARD_ARROW));
  }

  @Test
  void shouldCloseAndResumeOnBack() {
    MinigameOverlayManager manager = new MinigameOverlayManager();
    SpinTheWheelOverlay overlay = new SpinTheWheelOverlay(ITEMS, player, manager);
    overlay.request();
    overlay.afterRender();

    openWheel().getEvents().trigger("back");
    entityService.update();

    assertFalse(entityService.getPaused());
    assertFalse(manager.isActive());
    assertNull(openWheel());
    assertEquals(0, stage.getActors().size);
  }

  @Test
  void shouldCloseOnlyOnce() {
    MinigameOverlayManager manager = new MinigameOverlayManager();
    SpinTheWheelOverlay overlay = new SpinTheWheelOverlay(ITEMS, player, manager);
    overlay.request();
    overlay.afterRender();
    Entity wheel = openWheel();

    wheel.getEvents().trigger("back");
    manager.tryOpen();
    wheel.getEvents().trigger("back");

    // The second close must not release the minigame that opened after it
    assertTrue(manager.isActive());
  }

  /** The wheel's entity, or null when the wheel is not open. */
  private Entity openWheel() {
    for (Entity entity : entityService.getEntities()) {
      if (entity.getComponent(SpinTheWheelDisplay.class) != null) {
        return entity;
      }
    }
    return null;
  }
}
