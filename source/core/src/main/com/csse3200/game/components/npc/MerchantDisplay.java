package com.csse3200.game.components.npc;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

/** A proximity prompt and non-modal shop in the merchant's safe room. */
public class MerchantDisplay extends UIComponent {
  private MerchantComponent merchant;
  private Table prompt;
  private Window shop;
  private Label gold;
  private Label message;
  private InputComponent input;

  @Override
  public void create() {
    super.create();
    merchant = entity.getComponent(MerchantComponent.class);
    prompt = new Table();
    prompt.setFillParent(true);
    prompt.bottom().padBottom(145);

    prompt.setTouchable(Touchable.disabled);
    prompt.setVisible(false);
    stage.addActor(prompt);

    Texture panel =
        ServiceLocator.getResourceService().getAsset("images/ui/menu_box.png", Texture.class);
    TextureRegionDrawable paper =
        new TextureRegionDrawable(
            new TextureRegion(
                panel,
                panel.getWidth() * 13 / 100,
                panel.getHeight() * 6 / 100,
                panel.getWidth() * 74 / 100,
                panel.getHeight() * 88 / 100));
    paper.setMinWidth(0);
    paper.setMinHeight(0);
    Table badge = new Table();
    badge.setBackground(paper);
    badge.add(new Label("F  -  Trade with the cliff merchant", skin)).pad(10, 18, 10, 18);
    prompt.add(badge);
    Window.WindowStyle windowStyle = new Window.WindowStyle(skin.get(Window.WindowStyle.class));
    windowStyle.background = paper;
    windowStyle.titleFontColor = Color.valueOf("482715");
    shop = new Window("", windowStyle);
    shop.setMovable(false);
    shop.pad(35);
    shop.add(new Label("CLIFF MERCHANT", skin)).colspan(2).padBottom(18).row();
    TextButton.TextButtonStyle buttonStyle =
        new TextButton.TextButtonStyle(skin.get(TextButton.TextButtonStyle.class));
    TextureRegionDrawable buttonPaper =
        new TextureRegionDrawable(
            new TextureRegion(
                panel,
                panel.getWidth() * 14 / 100,
                panel.getHeight() * 8 / 100,
                panel.getWidth() * 72 / 100,
                panel.getHeight() * 83 / 100));
    buttonPaper.setMinWidth(0);
    buttonPaper.setMinHeight(0);
    buttonStyle.up = buttonPaper.tint(Color.valueOf("D9B47B"));
    buttonStyle.over = buttonPaper.tint(Color.valueOf("F8DDA2"));
    buttonStyle.down = buttonPaper.tint(Color.valueOf("BA8D51"));
    buttonStyle.fontColor = Color.valueOf("482715");
    buttonStyle.overFontColor = buttonStyle.fontColor;
    buttonStyle.downFontColor = buttonStyle.fontColor;
    gold = new Label("", skin);
    message = new Label("Supplies for the climb.", skin);
    shop.add(gold).left().colspan(2).padBottom(15).row();
    for (int i = 0; i < MerchantComponent.OFFERS.size(); i++) {
      int index = i;
      var offer = MerchantComponent.OFFERS.get(i);
      shop.add(new Label(offer.quantity() + " x " + offer.item().getDisplayName(), skin))
          .left()
          .padRight(25)
          .padBottom(10);
      TextButton buy = new TextButton("Buy - " + offer.price() + " gold", buttonStyle);
      buy.addListener(
          new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
              purchase(index);
            }
          });
      shop.add(buy).width(170).height(42).padBottom(10).row();
    }
    shop.add(message).colspan(2).padTop(10).row();
    TextButton close = new TextButton("Continue climbing", buttonStyle);
    close.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            shop.setVisible(false);
          }
        });
    shop.add(close).colspan(2).height(42).width(260).padTop(15).row();
    shop.pack();
    shop.setVisible(false);
    stage.addActor(shop);

    // Stage controls retain priority over gameplay; F toggles trade without also picking up items.
    input =
        new InputComponent(8) {
          @Override
          public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.F
                && merchant.canTrade()
                && !ServiceLocator.getEntityService().getPaused()) {
              shop.setVisible(!shop.isVisible());
              if (shop.isVisible()) shop.toFront();
              return true;
            }
            return false;
          }
        };
    ServiceLocator.getInputService().register(input);
  }

  private void purchase(int index) {
    message.setText(
        switch (merchant.buy(index)) {
          case PURCHASED -> "Added to your backpack.";
          case NOT_ENOUGH_GOLD -> "You need more gold.";
          case INVENTORY_FULL -> "Make room in your backpack first.";
          case OUT_OF_RANGE -> "Come closer to trade.";
          case INVALID_OFFER -> "That item is unavailable.";
        });
    gold.setText("Your gold: " + merchant.getGold());
  }

  @Override
  public void update() {
    boolean nearby = merchant.canTrade();
    if (!nearby) shop.setVisible(false);
    prompt.setVisible(nearby && !shop.isVisible());
    gold.setText("Your gold: " + merchant.getGold());
    shop.setPosition(
        (stage.getWidth() - shop.getWidth()) / 2, (stage.getHeight() - shop.getHeight()) / 2);
  }

  @Override
  protected void draw(SpriteBatch batch) {}

  @Override
  public void dispose() {
    if (prompt != null) prompt.remove();
    if (shop != null) shop.remove();
    if (input != null) ServiceLocator.getInputService().unregister(input);
    super.dispose();
  }
}
