package com.csse3200.game.rendering;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.csse3200.game.components.level.RotatableMapComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameExtension.class)
class DynamicTextureRenderComponentTest {

  private ResourceService resourceService;
  private RenderService renderService;

  @BeforeEach
  void setUp() {
    resourceService = mock(ResourceService.class);
    renderService = mock(RenderService.class);

    ServiceLocator.registerResourceService(resourceService);
    ServiceLocator.registerRenderService(renderService);
  }

  @Test
  void shouldCreateFromTexturePath() {
    Texture texture = mock(Texture.class);
    when(resourceService.getAsset("images/test.png", Texture.class)).thenReturn(texture);

    DynamicTextureRenderComponent component = new DynamicTextureRenderComponent("images/test.png");

    assertSame(texture, component.getTexture());
    verify(resourceService).getAsset("images/test.png", Texture.class);
  }

  @Test
  void shouldCreateFromTexture() {
    Texture texture = mock(Texture.class);

    DynamicTextureRenderComponent component = new DynamicTextureRenderComponent(texture);

    assertSame(texture, component.getTexture());
  }

  @Test
  void shouldSetTextureFromPath() {
    Texture firstTexture = mock(Texture.class);
    Texture secondTexture = mock(Texture.class);

    when(resourceService.getAsset("images/first.png", Texture.class)).thenReturn(firstTexture);
    when(resourceService.getAsset("images/second.png", Texture.class)).thenReturn(secondTexture);

    DynamicTextureRenderComponent component = new DynamicTextureRenderComponent("images/first.png");

    component.setTexture("images/second.png");

    assertSame(secondTexture, component.getTexture());
    verify(resourceService).getAsset("images/second.png", Texture.class);
  }

  @Test
  void shouldSetTextureDirectly() {
    Texture firstTexture = mock(Texture.class);
    Texture secondTexture = mock(Texture.class);

    DynamicTextureRenderComponent component = new DynamicTextureRenderComponent(firstTexture);

    component.setTexture(secondTexture);

    assertSame(secondTexture, component.getTexture());
  }

  @Test
  void shouldScaleEntityUsingTextureAspectRatio() {
    Texture texture = mock(Texture.class);

    when(texture.getWidth()).thenReturn(200);
    when(texture.getHeight()).thenReturn(100);

    DynamicTextureRenderComponent component = new DynamicTextureRenderComponent(texture);

    Entity entity = new Entity().addComponent(component);

    component.scaleEntity();

    assertEquals(1f, entity.getScale().x, 0.001f);
    assertEquals(0.5f, entity.getScale().y, 0.001f);
  }

  @Test
  void shouldCreateWithoutRotatableMapComponent() {
    Texture texture = mock(Texture.class);

    DynamicTextureRenderComponent component = new DynamicTextureRenderComponent(texture);

    Entity entity = new Entity().addComponent(component);

    component.create();

    assertSame(texture, component.getTexture());
    verify(renderService).register(component);
  }

  @Test
  void shouldCreateWithRotatableMapComponent() {
    Texture texture = mock(Texture.class);

    RotatableMapComponent rotateComponent = new RotatableMapComponent(45f);

    DynamicTextureRenderComponent component = new DynamicTextureRenderComponent(texture);

    new Entity().addComponent(rotateComponent).addComponent(component);

    component.create();

    assertSame(texture, component.getTexture());
    verify(renderService).register(component);
  }

  @Test
  void shouldDrawWithoutRotationComponent() {
    Texture texture = mock(Texture.class);
    SpriteBatch batch = mock(SpriteBatch.class);

    when(batch.getColor()).thenReturn(new Color(Color.WHITE));

    DynamicTextureRenderComponent component = new DynamicTextureRenderComponent(texture);

    Entity entity = new Entity().addComponent(component);
    entity.setPosition(2f, 3f);
    entity.setScale(4f, 5f);

    component.create();
    component.draw(batch);

    verify(batch)
        .draw(any(), eq(2f), eq(3f), eq(2f), eq(2.5f), eq(4f), eq(5f), eq(1f), eq(1f), eq(0f));

    verify(batch).setColor(any(Color.class));
  }

  @Test
  void shouldDrawWithRotationComponent() {
    Texture texture = mock(Texture.class);
    SpriteBatch batch = mock(SpriteBatch.class);

    when(batch.getColor()).thenReturn(new Color(Color.WHITE));

    RotatableMapComponent rotateComponent = new RotatableMapComponent(90f);

    DynamicTextureRenderComponent component = new DynamicTextureRenderComponent(texture);

    Entity entity = new Entity().addComponent(rotateComponent).addComponent(component);

    entity.setPosition(1f, 2f);
    entity.setScale(6f, 8f);

    component.create();
    component.draw(batch);

    verify(batch)
        .draw(any(), eq(1f), eq(2f), eq(3f), eq(4f), eq(6f), eq(8f), eq(1f), eq(1f), eq(90f));

    verify(batch).setColor(any(Color.class));
  }
}
