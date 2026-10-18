package com.csse3200.game.components.minigames;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.ButtonSound;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.screens.minigames.MinigameType;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

@ExtendWith(GameExtension.class)
class MinigameSelectDisplayTest {
  @Test
  void menuButtonsUseMatchingArtworkAndDispatchSelectionAfterClickFeedback() {
    Stage stage = mock(Stage.class);
    RenderService renders = mock(RenderService.class);
    when(renders.getStage()).thenReturn(stage);
    ServiceLocator.registerRenderService(renders);
    ResourceService resources = mock(ResourceService.class);
    Map<String, Texture> textures = new HashMap<>();
    when(resources.getAsset(anyString(), eq(Texture.class)))
        .thenAnswer(
            call -> textures.computeIfAbsent(call.getArgument(0), key -> mock(Texture.class)));
    ServiceLocator.registerResourceService(resources);
    MinigameSelectDisplay display = new MinigameSelectDisplay();
    Entity ui = new Entity().addComponent(display);
    List<MinigameType> selected = new ArrayList<>();
    AtomicInteger back = new AtomicInteger();
    ui.getEvents().addListener("selectMinigame", (MinigameType type) -> selected.add(type));
    ui.getEvents().addListener("back", () -> back.incrementAndGet());
    display.create();
    ArgumentCaptor<Actor> actor = ArgumentCaptor.forClass(Actor.class);
    verify(stage).addActor(actor.capture());
    Table table = (Table) actor.getValue();
    Group container = new Group();
    container.setSize(800f, 600f);
    container.addActor(table);
    table.validate();
    assertEquals(800f, table.getWidth());
    assertEquals(600f, table.getHeight());
    assertEquals("Minigames", ((Label) table.getCells().get(0).getActor()).getText().toString());
    assertEquals(MinigameType.values().length + 2, table.getCells().size);
    assertEquals(2f, display.getZIndex());
    List<Runnable> transitions = new ArrayList<>();
    try (var sound = mockStatic(ButtonSound.class)) {
      sound
          .when(() -> ButtonSound.playClickThen(any(Runnable.class)))
          .thenAnswer(
              call -> {
                transitions.add(call.getArgument(0));
                return null;
              });
      int index = 1;
      for (MinigameType type : MinigameType.values()) {
        ImageButton button = (ImageButton) table.getCells().get(index++).getActor();
        assertSame(
            textures.get("images/Buttons/" + type.getAssetKey() + "_up_btn.png"),
            ((TextureRegionDrawable) button.getStyle().up).getRegion().getTexture());
        assertSame(
            textures.get("images/Buttons/" + type.getAssetKey() + "_down_btn.png"),
            ((TextureRegionDrawable) button.getStyle().down).getRegion().getTexture());
        button.fire(new ChangeListener.ChangeEvent());
      }
      ImageButton backButton = (ImageButton) table.getCells().get(index).getActor();
      assertSame(
          textures.get("images/Buttons/back_up_btn.png"),
          ((TextureRegionDrawable) backButton.getStyle().up).getRegion().getTexture());
      assertSame(
          textures.get("images/Buttons/back_down_btn.png"),
          ((TextureRegionDrawable) backButton.getStyle().down).getRegion().getTexture());
      backButton.fire(new ChangeListener.ChangeEvent());
      assertTrue(selected.isEmpty());
      assertEquals(0, back.get());
      assertEquals(MinigameType.values().length + 1, transitions.size());
      transitions.forEach(Runnable::run);
      assertEquals(List.of(MinigameType.values()), selected);
      assertEquals(1, back.get());
    }
    SpriteBatch batch = mock(SpriteBatch.class);
    display.draw(batch);
    verifyNoInteractions(batch);
    display.dispose();
    assertTrue(table.getChildren().isEmpty());
    verify(renders).unregister(display);
  }
}
