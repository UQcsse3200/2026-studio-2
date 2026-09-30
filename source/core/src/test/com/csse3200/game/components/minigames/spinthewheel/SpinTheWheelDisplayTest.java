package com.csse3200.game.components.minigames.spinthewheel;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.csse3200.game.components.inventory.InventoryComponent;
import com.csse3200.game.components.item.ItemType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.listeners.EventListener0;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class SpinTheWheelDisplayTest {
  private static final String SPIN_SOUND = "sounds/minigames/spinthewheel/wheel-spin.wav";
  private static final String PRIZE_SOUND = "sounds/minigames/spinthewheel/wheel-prize.wav";
  private static final List<WheelItem> TWO_ITEMS =
      List.of(new WheelItem(ItemType.STANDARD_ARROW, 1), new WheelItem(ItemType.HEALTH_POTION, 2));

  /** A wheel with a single segment always lands on it. */
  private static final List<WheelItem> ONE_ITEM =
      List.of(new WheelItem(ItemType.STANDARD_ARROW, 3));

  private Stage stage;
  private ResourceService resources;

  @BeforeEach
  void setUp() {
    stage = new Stage(new StretchViewport(800, 600), mock(SpriteBatch.class));
    RenderService renderService = mock(RenderService.class);
    when(renderService.getStage()).thenReturn(stage);
    ServiceLocator.registerRenderService(renderService);

    resources = mock(ResourceService.class);
    when(resources.getAsset(anyString(), eq(Texture.class))).thenReturn(mock(Texture.class));
    ServiceLocator.registerResourceService(resources);
  }

  @AfterEach
  void tearDown() {
    stage.dispose();
  }

  @Test
  void shouldIncludeASpriteForEveryItem() {
    List<String> paths = Arrays.asList(SpinTheWheelDisplay.texturesFor(TWO_ITEMS));

    for (WheelItem item : TWO_ITEMS) {
      assertTrue(paths.contains(item.type().getTexturePath()));
    }
  }

  @Test
  void shouldAddOnePathPerItem() {
    int shared = SpinTheWheelDisplay.texturesFor(List.of()).length;

    assertEquals(shared + TWO_ITEMS.size(), SpinTheWheelDisplay.texturesFor(TWO_ITEMS).length);
  }

  @Test
  void shouldStoreWhatTheWheelLandsOn() {
    InventoryComponent inventory = new InventoryComponent(0, 3);
    SpinTheWheelDisplay display = new SpinTheWheelDisplay(TWO_ITEMS, inventory);

    display.award(new WheelItem(ItemType.STANDARD_ARROW, 10));

    assertEquals(10, inventory.getItemCount(ItemType.STANDARD_ARROW));
  }

  @Test
  void shouldKeepNothingWithoutAnInventory() {
    SpinTheWheelDisplay display = new SpinTheWheelDisplay(TWO_ITEMS);

    assertDoesNotThrow(() -> display.award(new WheelItem(ItemType.STANDARD_ARROW, 10)));
  }

  @Test
  void shouldKeepNothingWhenTheInventoryIsFull() {
    InventoryComponent inventory = new InventoryComponent(0, 1);
    inventory.addItem(ItemType.HEALTH_POTION, 1);
    SpinTheWheelDisplay display = new SpinTheWheelDisplay(TWO_ITEMS, inventory);

    display.award(new WheelItem(ItemType.STANDARD_ARROW, 10));

    assertEquals(0, inventory.getItemCount(ItemType.STANDARD_ARROW));
  }

  @Test
  void shouldShowTheWheelWithThePrizeHidden() {
    show(new SpinTheWheelDisplay(TWO_ITEMS));

    assertNotNull(find("wheel-spin-button"));
    assertNotNull(find("wheel-back-button"));
    assertFalse(find("wheel-prize").isVisible());
  }

  @Test
  void shouldAwardAndShowThePrizeWhenTheWheelStops() {
    InventoryComponent inventory = new InventoryComponent(0, 3);
    show(new SpinTheWheelDisplay(ONE_ITEM, inventory));
    Button spin = find("wheel-spin-button");

    click(spin);

    assertTrue(spin.isDisabled());
    assertEquals(0, inventory.getItemCount(ItemType.STANDARD_ARROW));

    finishAnimations();
    Label name = find("wheel-prize-name");
    Label amount = find("wheel-prize-amount");

    assertFalse(spin.isDisabled());
    assertEquals(3, inventory.getItemCount(ItemType.STANDARD_ARROW));
    assertTrue(find("wheel-prize").isVisible());
    assertEquals("Standard Arrow", name.getText().toString());
    assertEquals("x3", amount.getText().toString());
  }

  @Test
  void shouldIgnoreClicksWhileSpinning() {
    InventoryComponent inventory = new InventoryComponent(0, 3);
    show(new SpinTheWheelDisplay(ONE_ITEM, inventory));
    Button spin = find("wheel-spin-button");

    click(spin);
    click(spin);
    finishAnimations();

    assertEquals(3, inventory.getItemCount(ItemType.STANDARD_ARROW));
  }

  @Test
  void shouldGoBackFromTheBackButton() {
    EventListener0 back = listenForBack(show(new SpinTheWheelDisplay(TWO_ITEMS)));

    click(find("wheel-back-button"));

    verify(back).handle();
  }

  @Test
  void shouldCloseWhenThePrizeIsClicked() {
    EventListener0 back = listenForBack(show(new SpinTheWheelDisplay(ONE_ITEM)));
    click(find("wheel-spin-button"));
    finishAnimations();

    click(find("wheel-prize"));

    verify(back).handle();
    assertFalse(find("wheel-prize").isVisible());
  }

  @Test
  void shouldCloseOnceWhenAKeyIsPressedOnThePrize() {
    EventListener0 back = listenForBack(show(new SpinTheWheelDisplay(ONE_ITEM)));
    click(find("wheel-spin-button"));
    finishAnimations();

    pressKey(find("wheel-prize"));
    pressKey(find("wheel-prize"));

    verify(back).handle();
  }

  @Test
  void shouldPlayTheSpinAndPrizeSounds() {
    Sound spinSound = loadSound(SPIN_SOUND);
    Sound prizeSound = loadSound(PRIZE_SOUND);
    when(spinSound.play()).thenReturn(7L);
    show(new SpinTheWheelDisplay(ONE_ITEM));

    click(find("wheel-spin-button"));

    verify(spinSound).play();
    verify(prizeSound, never()).play();

    finishAnimations();

    verify(spinSound).stop(7L);
    verify(prizeSound).play();
  }

  @Test
  void shouldStopTheSpinSoundWhenClosedMidSpin() {
    Sound spinSound = loadSound(SPIN_SOUND);
    when(spinSound.play()).thenReturn(7L);
    SpinTheWheelDisplay display = new SpinTheWheelDisplay(ONE_ITEM);
    show(display);
    click(find("wheel-spin-button"));

    display.dispose();

    verify(spinSound).stop(7L);
  }

  @Test
  void shouldNotStopASpinSoundThatWasUnloaded() {
    Sound spinSound = loadSound(SPIN_SOUND);
    when(resources.containsAsset(SPIN_SOUND, Sound.class)).thenReturn(true, false);
    SpinTheWheelDisplay display = new SpinTheWheelDisplay(ONE_ITEM);
    show(display);
    click(find("wheel-spin-button"));

    display.dispose();

    verify(spinSound, never()).stop(anyLong());
  }

  @Test
  void shouldRemoveItselfFromTheStageWhenDisposed() {
    SpinTheWheelDisplay display = new SpinTheWheelDisplay(TWO_ITEMS);
    show(display);

    display.dispose();

    assertEquals(0, stage.getActors().size);
  }

  @Test
  void shouldDisposeWithoutAResourceService() {
    SpinTheWheelDisplay display = new SpinTheWheelDisplay(TWO_ITEMS);
    show(display);
    ServiceLocator.registerResourceService(null);

    assertDoesNotThrow(display::dispose);
  }

  @Test
  void shouldLeaveDrawingToTheStage() {
    SpriteBatch batch = mock(SpriteBatch.class);

    new SpinTheWheelDisplay(TWO_ITEMS).draw(batch);

    verifyNoInteractions(batch);
  }

  @Test
  void shouldDrawAboveTheRestOfTheUi() {
    assertEquals(2f, new SpinTheWheelDisplay(TWO_ITEMS).getZIndex());
  }

  /** Creates the wheel on an entity, as a screen or overlay would. */
  private Entity show(SpinTheWheelDisplay display) {
    Entity ui = new Entity().addComponent(display);
    ui.create();
    return ui;
  }

  private static EventListener0 listenForBack(Entity ui) {
    EventListener0 back = mock(EventListener0.class);
    ui.getEvents().addListener("back", back);
    return back;
  }

  private <T extends Actor> T find(String name) {
    return stage.getRoot().findActor(name);
  }

  private Sound loadSound(String path) {
    Sound sound = mock(Sound.class);
    when(resources.containsAsset(path, Sound.class)).thenReturn(true);
    when(resources.getAsset(path, Sound.class)).thenReturn(sound);
    return sound;
  }

  /** Lets the spin and the prize card's animations run to the end. */
  private void finishAnimations() {
    for (int i = 0; i < 10; i++) {
      stage.act(1f);
    }
  }

  /** Presses and releases the mouse on an actor, as a player's click would. */
  private static void click(Actor actor) {
    for (InputEvent.Type type : List.of(InputEvent.Type.touchDown, InputEvent.Type.touchUp)) {
      InputEvent event = new InputEvent();
      event.setType(type);
      actor.fire(event);
    }
  }

  private static void pressKey(Actor actor) {
    InputEvent event = new InputEvent();
    event.setType(InputEvent.Type.keyDown);
    actor.fire(event);
  }
}
