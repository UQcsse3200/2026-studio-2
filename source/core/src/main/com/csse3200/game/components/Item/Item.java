public abstract class Item {
  public string itemName;
  public string description;
  public int quantity;

  public Item(String itemName, String description, int quantity) {
    this.itemName = itemName;
    this.description = description;
    this.quantity = quantity;
  }
}
