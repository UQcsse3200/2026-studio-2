package com.csse3200.game.components.item;

public abstract class Consumable extends Item {
    protected int treatment;
    protected boolean consumedOnUse;

    public Consumable(String itemName, String description, int quantity, int treatment) {
        super(ItemType.CONSUMABLE, itemName, description, quantity);
        this.treatment = treatment;
        this.consumedOnUse = true;
    }

    public int getTreatment() {
        return treatment;
    }

    public boolean useConsumable() {
        if (quantity <= 0) {
            return false;
        }

        if (consumedOnUse) {
            quantity--;
        }

        return true;
    }
}