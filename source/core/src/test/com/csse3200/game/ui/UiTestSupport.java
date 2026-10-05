package com.csse3200.game.ui;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

/** Real Scene2D actors and entity events, with only graphics and texture loading mocked. */
@ExtendWith(GameExtension.class)
public abstract class UiTestSupport {
  protected Stage stage;
  protected SpriteBatch batch;
  protected EntityService entities;
  protected ResourceService resources;
  private PhysicsService physics;

  @BeforeEach
  void setUpUi() {
    AtomicInteger textureHandles = new AtomicInteger(1);
    when(Gdx.gl.glGenTexture()).thenAnswer(call -> textureHandles.getAndIncrement());
    batch = mock(SpriteBatch.class);
    stage = new Stage(new ScreenViewport(), batch);
    stage.getViewport().update(1600, 900, true);
    RenderService renders = mock(RenderService.class);
    when(renders.getStage()).thenReturn(stage);
    ServiceLocator.registerRenderService(renders);
    resources = mock(ResourceService.class);
    Texture icon = mock(Texture.class);
    when(icon.getWidth()).thenReturn(64);
    when(icon.getHeight()).thenReturn(64);
    when(resources.getAsset(anyString(), eq(Texture.class))).thenReturn(icon);
    when(resources.containsAsset(anyString(), eq(Texture.class))).thenReturn(true);
    ServiceLocator.registerResourceService(resources);
    entities = new EntityService();
    ServiceLocator.registerEntityService(entities);
    ServiceLocator.registerTimeSource(new GameTime());
    physics = new PhysicsService();
    ServiceLocator.registerPhysicsService(physics);
  }

  @AfterEach
  void tearDownUi() {
    entities.dispose();
    stage.dispose();
    physics.getPhysics().dispose();
  }

  protected Entity register(Entity entity) {
    entities.register(entity);
    return entity;
  }

  protected static <T> T field(Object owner, String name, Class<T> type) {
    try {
      Field field = owner.getClass().getDeclaredField(name);
      field.setAccessible(true);
      return type.cast(field.get(owner));
    } catch (ReflectiveOperationException exception) {
      throw new AssertionError(exception);
    }
  }

  protected static List<String> labels(Actor actor) {
    List<String> texts = new ArrayList<>();
    if (actor instanceof Label label) {
      texts.add(label.getText().toString());
    }
    if (actor instanceof Group group) {
      for (Actor child : group.getChildren()) {
        texts.addAll(labels(child));
      }
    }
    return texts;
  }

  protected static void click(Actor actor) {
    InputEvent event = new InputEvent();
    event.setTarget(actor);
    for (var listener : actor.getListeners()) {
      if (listener instanceof ClickListener click) {
        click.clicked(event, 0, 0);
      }
    }
  }
}
