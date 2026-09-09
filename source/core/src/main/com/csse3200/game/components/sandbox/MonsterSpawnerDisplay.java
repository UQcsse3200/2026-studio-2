package com.csse3200.game.components.sandbox;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import java.util.function.BiConsumer;

/** Clickable Sandbox NPC and modal menu for choosing and spawning an enemy. */
public class MonsterSpawnerDisplay extends UIComponent {
  private static final int SLOT_COUNT = 8;
  private static final int GRID_COLUMNS = 4;
  private static final float CARD_WIDTH = 170f;
  private static final float CARD_HEIGHT = 135f;
  private static final float MONSTER_IMAGE_SIZE = 82f;
  private static final float NPC_PROMPT_GAP = 8f;
  private static final String NPC_PROMPT_TEXT = "Click me to spawn enemy";

  private final CameraComponent cameraComponent;
  private final BiConsumer<SandboxEnemyType, Boolean> spawnHandler;

  private Image npcImage;
  private Label npcPromptLabel;
  private Table modalOverlay;
  private TextButton spawnButton;
  private CheckBox activeCheckBox;
  private Button selectedCard;
  private SandboxEnemyType selectedEnemy;
  private boolean wasPausedBeforeOpening;

  public MonsterSpawnerDisplay(
      CameraComponent cameraComponent, BiConsumer<SandboxEnemyType, Boolean> spawnHandler) {
    this.cameraComponent = cameraComponent;
    this.spawnHandler = spawnHandler;
  }

  @Override
  public void create() {
    super.create();
    createNpcImage();
    createNpcPrompt();
    createModalOverlay();
    stage.addActor(npcImage);
    stage.addActor(npcPromptLabel);
    stage.addActor(modalOverlay);
  }

  private void createNpcImage() {
    TextureAtlas ghostAtlas =
        ServiceLocator.getResourceService().getAsset("images/ghost.atlas", TextureAtlas.class);
    npcImage = new Image(ghostAtlas.findRegion("default"));
    npcImage.setTouchable(Touchable.enabled);
    npcImage.setName("sandbox-monster-spawner-npc");
    npcImage.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            openPanel();
          }
        });
  }

  private void createNpcPrompt() {
    npcPromptLabel = new Label(NPC_PROMPT_TEXT, skin, "small");
    npcPromptLabel.setTouchable(Touchable.disabled);
    npcPromptLabel.setName("sandbox-monster-spawner-prompt");
    npcPromptLabel.pack();
  }

  private void createModalOverlay() {
    modalOverlay = new Table();
    modalOverlay.setFillParent(true);
    modalOverlay.setVisible(false);
    modalOverlay.setTouchable(Touchable.enabled);
    modalOverlay.setBackground(skin.newDrawable("white", new Color(0f, 0f, 0f, 0.6f)));
    modalOverlay.setName("sandbox-monster-spawner-overlay");
    modalOverlay.addListener(
        new InputListener() {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            return true;
          }

          @Override
          public boolean keyDown(InputEvent event, int keycode) {
            return true;
          }

          @Override
          public boolean keyUp(InputEvent event, int keycode) {
            return true;
          }
        });

    Table panel = new Table();
    panel.setBackground(skin.getDrawable("window-c"));
    panel.pad(20f);

    Label title = new Label("Spawn Monster", skin, "large");
    TextButton closeButton = new TextButton("X", skin);
    closeButton.setName("sandbox-monster-spawner-close");
    closeButton.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            closePanel();
          }
        });

    panel.add(title).colspan(3).expandX().center();
    panel.add(closeButton).right().size(48f);
    panel.row();

    Table monsterGrid = createMonsterGrid();
    panel.add(monsterGrid).colspan(4).padTop(16f);
    panel.row();

    activeCheckBox = new CheckBox(" Active", skin);
    activeCheckBox.setChecked(true);
    activeCheckBox.setName("sandbox-monster-active-checkbox");

    spawnButton = new TextButton("Spawn", skin);
    spawnButton.setDisabled(true);
    spawnButton.setName("sandbox-monster-spawn-button");
    spawnButton.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            spawnSelectedEnemy();
          }
        });

    panel.add(activeCheckBox).colspan(2).left().padTop(18f);
    panel.add(spawnButton).colspan(2).right().width(180f).height(55f).padTop(18f);
    modalOverlay.add(panel);
  }

  private Table createMonsterGrid() {
    Table grid = new Table();
    SandboxEnemyType[] availableEnemies = SandboxEnemyType.values();

    for (int slot = 0; slot < SLOT_COUNT; slot++) {
      if (slot < availableEnemies.length) {
        grid.add(createEnemyCard(availableEnemies[slot])).size(CARD_WIDTH, CARD_HEIGHT).pad(6f);
      } else {
        grid.add(createEmptyCard()).size(CARD_WIDTH, CARD_HEIGHT).pad(6f);
      }
      if ((slot + 1) % GRID_COLUMNS == 0) {
        grid.row();
      }
    }
    return grid;
  }

  private Button createEnemyCard(SandboxEnemyType enemyType) {
    Button card = new Button(skin);
    Button.ButtonStyle cardStyle = new Button.ButtonStyle(card.getStyle());
    cardStyle.checked = skin.getDrawable("selection");
    card.setStyle(cardStyle);
    card.setName("sandbox-enemy-card-" + enemyType.name().toLowerCase());

    Texture texture =
        ServiceLocator.getResourceService().getAsset(enemyType.getTexturePath(), Texture.class);
    card.add(new Image(texture)).size(MONSTER_IMAGE_SIZE).padTop(8f).row();
    card.add(new Label(enemyType.getDisplayName(), skin, "small")).pad(8f);
    card.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            selectEnemy(enemyType, card);
          }
        });
    return card;
  }

  private Table createEmptyCard() {
    Table emptyCard = new Table();
    emptyCard.setBackground(skin.getDrawable("button-c"));
    emptyCard.add(new Label("Empty", skin, "small"));
    emptyCard.setTouchable(Touchable.disabled);
    return emptyCard;
  }

  private void selectEnemy(SandboxEnemyType enemyType, Button card) {
    if (selectedCard != null) {
      selectedCard.setChecked(false);
    }
    selectedEnemy = enemyType;
    selectedCard = card;
    selectedCard.setChecked(true);
    spawnButton.setDisabled(false);
  }

  private void spawnSelectedEnemy() {
    if (selectedEnemy == null || spawnButton.isDisabled()) {
      return;
    }
    spawnHandler.accept(selectedEnemy, activeCheckBox.isChecked());
    closePanel();
  }

  private void openPanel() {
    if (modalOverlay.isVisible()) {
      return;
    }
    wasPausedBeforeOpening = ServiceLocator.getEntityService().getPaused();
    ServiceLocator.getEntityService().setPaused(true);
    modalOverlay.setVisible(true);
    modalOverlay.toFront();
    stage.setKeyboardFocus(modalOverlay);
  }

  private void closePanel() {
    if (!modalOverlay.isVisible()) {
      return;
    }
    modalOverlay.setVisible(false);
    if (stage.getKeyboardFocus() == modalOverlay) {
      stage.setKeyboardFocus(null);
    }
    if (!wasPausedBeforeOpening) {
      ServiceLocator.getEntityService().setPaused(false);
    }
  }

  @Override
  protected void draw(SpriteBatch batch) {
    Vector3 bottomLeft =
        cameraComponent
            .getCamera()
            .project(new Vector3(entity.getPosition().x, entity.getPosition().y, 0f));
    Vector3 topRight =
        cameraComponent
            .getCamera()
            .project(
                new Vector3(
                    entity.getPosition().x + entity.getScale().x,
                    entity.getPosition().y + entity.getScale().y,
                    0f));
    npcImage.setBounds(
        bottomLeft.x,
        bottomLeft.y,
        Math.abs(topRight.x - bottomLeft.x),
        Math.abs(topRight.y - bottomLeft.y));
    npcPromptLabel.setPosition(
        bottomLeft.x + (npcImage.getWidth() - npcPromptLabel.getWidth()) / 2f,
        Math.max(bottomLeft.y, topRight.y) + NPC_PROMPT_GAP);
  }

  @Override
  public void dispose() {
    if (modalOverlay != null && modalOverlay.isVisible() && !wasPausedBeforeOpening) {
      ServiceLocator.getEntityService().setPaused(false);
    }
    if (npcImage != null) {
      npcImage.remove();
    }
    if (npcPromptLabel != null) {
      npcPromptLabel.remove();
    }
    if (modalOverlay != null) {
      modalOverlay.remove();
    }
    super.dispose();
  }
}
